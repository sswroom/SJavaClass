package org.sswr.util.web;

import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sswr.util.crypto.token.JWSignature;
import org.sswr.util.crypto.token.JWTHandler;
import org.sswr.util.crypto.token.JWTParam;
import org.sswr.util.crypto.token.JWToken;
import org.sswr.util.data.DateTimeUtil;
import org.sswr.util.data.StringUtil;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class JWTSessionManager
{
	public interface JWTSesionInitializator
	{
		public void initSession(@Nonnull JWTSession sess);
	}
	protected long lastId;
	protected JWTHandler jwt;
	protected int timeoutMs;
	protected Map<Long, JWTSession> sessMap;
	protected JWTSessionListener listener;
	protected JWTSesionInitializator sessInit;

	public JWTSessionManager(@Nonnull String password, int timeoutMs, @Nonnull JWTSesionInitializator sessInit)
	{
		this.lastId = 0;
		this.timeoutMs = timeoutMs;
		this.sessInit = sessInit;
		this.jwt = JWTHandler.createHMAC(JWSignature.Algorithm.HS512, password.getBytes(StandardCharsets.UTF_8));
		this.sessMap = new HashMap<Long, JWTSession>();
	}

	public synchronized JWTSession newSession(@Nonnull String userName, @Nonnull List<String> roleList)
	{
		long id = System.currentTimeMillis();
		if (id <= this.lastId)
		{
			id = this.lastId + 1;
		}
		this.lastId = id;
		JWTSession sess = new JWTSession(id, userName, roleList);
		sessMap.put(id, sess);
		sess.setLastAccessTime(DateTimeUtil.timestampNow());
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
		Timestamp currTime = DateTimeUtil.timestampNow();
		Timestamp lastAccessTime;
		int i = 0;
		int j = sessArr.length;
		while (i < j)
		{
			sess = (JWTSession)sessArr[i];
			lastAccessTime = sess.getLastAccessTime();
			if (currTime.getTime() - lastAccessTime.getTime() >= timeoutMs)
			{
				this.listener.sessionDestroy(sess);
				this.sessMap.remove(sess.getSessId());
			}
			i++;
		}
	}

	public synchronized boolean removeSession(@Nonnull JWTSession sess)
	{
		JWTSession removedSess = this.sessMap.remove(sess.getSessId());
		if (removedSess != null)
		{
			this.listener.sessionDestroy(removedSess);
			return true;
		}
		return false;
	}

	public synchronized void removeSessions(@Nonnull String userName)
	{
		Object[] sessArr = this.sessMap.values().toArray();
		JWTSession sess;
		int i = 0;
		int j = sessArr.length;
		while (i < j)
		{
			sess = (JWTSession)sessArr[i];
			if (sess.getUserName().equals(userName))
			{
				this.listener.sessionDestroy(sess);
				this.sessMap.remove(sess.getSessId());
			}
			i++;
		}
	}

	@Nullable
	public String createToken(@Nonnull JWTSession sess)
	{
		JWTParam param = new JWTParam();
		param.setJWTId(""+sess.getSessId());
		return jwt.generate(new HashMap<String, String>(), param);
	}

	@Nullable
	public synchronized JWTSession getSession(@Nonnull String token)
	{
		JWTParam param = new JWTParam();
		JWToken t = JWToken.parse(token, null);
		if (t == null)
			return null;
		t.parsePayload(param, false, null);
		Long sessId = StringUtil.toLong(param.getJWTId());
		if (sessId != null)
		{
			JWTSession sess = this.sessMap.get(sessId);
			if (sess != null)
			{
				sess.setLastAccessTime(DateTimeUtil.timestampNow());
			}
			return sess;
		}
		return null;
	}

	public synchronized void setSessionListener(@Nullable JWTSessionListener listener)
	{
		this.listener = listener;
	}
}
