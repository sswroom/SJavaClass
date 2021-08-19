package org.sswr.util.web;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sswr.util.crypto.JWTHandler;
import org.sswr.util.crypto.JWTParam;
import org.sswr.util.crypto.JWTHandler.Algorithm;
import org.sswr.util.data.StringUtil;

public class JWTSessionManager
{
	public interface JWTSesionInitializator
	{
		public void initSession(JWTSession sess);
	}
	protected long lastId;
	protected JWTHandler jwt;
	protected int timeoutMs;
	protected Map<Long, JWTSession> sessMap;
	protected JWTSessionListener listener;
	protected JWTSesionInitializator sessInit;

	public JWTSessionManager(String password, int timeoutMs, JWTSesionInitializator sessInit)
	{
		this.lastId = 0;
		this.timeoutMs = timeoutMs;
		this.sessInit = sessInit;
		this.jwt = JWTHandler.createHMAC(Algorithm.HS512, password.getBytes(StandardCharsets.UTF_8));
		this.sessMap = new HashMap<Long, JWTSession>();
	}

	public synchronized JWTSession newSession(String userName, List<String> roleList)
	{
		long id = System.currentTimeMillis();
		if (id <= this.lastId)
		{
			id = this.lastId + 1;
		}
		this.lastId = id;
		JWTSession sess = new JWTSession(id, userName, roleList);
		sessMap.put(id, sess);
		sess.setLastAccessTime(System.currentTimeMillis());
		if (this.sessInit != null)
		{
			this.sessInit.initSession(sess);
		}
		return sess;
	}

	public synchronized void checkTimeout()
	{
		Object[] sessArr = this.sessMap.values().toArray();
		JWTSession sess;
		long currTime = System.currentTimeMillis();
		int i = 0;
		int j = sessArr.length;
		while (i < j)
		{
			sess = (JWTSession)sessArr[i];
			if (currTime - sess.getLastAccessTime() >= timeoutMs)
			{
				this.listener.sessionDestroy(sess);
				this.sessMap.remove(sess.getSessId());
			}
			i++;
		}
	}

	public synchronized boolean removeSession(JWTSession sess)
	{
		JWTSession removedSess = this.sessMap.remove(sess.getSessId());
		if (removedSess != null)
		{
			this.listener.sessionDestroy(removedSess);
			return true;
		}
		return false;
	}

	public String createToken(JWTSession sess)
	{
		JWTParam param = new JWTParam();
		param.setJWTId(""+sess.getSessId());
		return jwt.generate(new HashMap<String, String>(), param);
	}

	public synchronized JWTSession getSession(String token)
	{
		JWTParam param = new JWTParam();
		jwt.parse(token, param);
		Long sessId = StringUtil.toLong(param.getJWTId());
		if (sessId != null)
		{
			JWTSession sess = this.sessMap.get(sessId);
			if (sess != null)
			{
				sess.setLastAccessTime(System.currentTimeMillis());
			}
			return sess;
		}
		return null;
	}

	public synchronized void setSessionListener(JWTSessionListener listener)
	{
		this.listener = listener;
	}
}
