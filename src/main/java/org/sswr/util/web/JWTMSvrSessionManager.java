package org.sswr.util.web;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.sswr.util.basic.ThreadEvent;
import org.sswr.util.crypto.JWTParam;
import org.sswr.util.data.JSONMapper;
import org.sswr.util.data.JSONParser;
import org.sswr.util.data.StringUtil;
import org.sswr.util.net.MQTTClient;
import org.sswr.util.net.MQTTEventHdlr;

public class JWTMSvrSessionManager extends JWTSessionManager implements MQTTEventHdlr
{
	class JWTRequest
	{
		ThreadEvent evt;
		boolean reqEnd;
		int reqId;
		boolean reqResult;
		String reqUserName;
		List<String> reqRoles;
		int reqCnt;
	}
	private MQTTClient cli;
	private int serverId;
	private Map<Integer, Map<Long, JWTSession>> remoteSessMap;
	private Map<Integer, JWTRequest> reqMap;
	private int reqNextId;
	private String topicName;

	public JWTMSvrSessionManager(String password, int timeoutMs, JWTSesionInitializator sessInit, MQTTClient cli, int serverId, String topicName)
	{
		super(password, timeoutMs, sessInit);

		this.topicName = topicName;
		this.cli = cli;
		this.serverId = serverId;
		this.reqMap = new HashMap<Integer, JWTRequest>();
		this.reqNextId = 0;
		this.remoteSessMap = new HashMap<Integer, Map<Long, JWTSession>>();
		this.cli.handleEvents(this);
		this.cli.subscribe(this.topicName, null);
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
		synchronized(this.remoteSessMap)
		{
			Iterator<Map<Long, JWTSession>> itSessMap = this.remoteSessMap.values().iterator();
			Map<Long, JWTSession> sessMap;
			Iterator<JWTSession> itSess;
			while (itSessMap.hasNext())
			{
				sessMap = itSessMap.next();
				itSess = sessMap.values().iterator();
				while (itSess.hasNext())
				{
					sess = itSess.next();
					if (currTime - sess.getLastAccessTime() >= timeoutMs)
					{
						sessMap.remove(sess.getSessId());
					}
				}
			}
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
		Map<String, String> payload = new HashMap<String, String>();
		payload.put("svrId", String.valueOf(this.serverId));
		return jwt.generate(payload, param);
	}

	public JWTSession getSession(String token)
	{
		JWTParam param = new JWTParam();
		Map<String, String> payload = jwt.parse(token, param);
		if (payload == null)
		{
			return null;
		}
		Integer serverId = StringUtil.toInteger(payload.get("svrId"));
		Long sessId = StringUtil.toLong(param.getJWTId());
		if (sessId != null && serverId != null)
		{
			JWTSession sess;
			if (serverId.intValue() == this.serverId)
			{
				synchronized (this)
				{
					sess = this.sessMap.get(sessId);
					if (sess != null)
					{
						sess.setLastAccessTime(System.currentTimeMillis());
					}
					return sess;
				}
			}
			else
			{
				Map<Long, JWTSession> sessMap;
				sess = null;
				synchronized(this.remoteSessMap)
				{
					sessMap = this.remoteSessMap.get(serverId);
					if (sessMap != null)
					{
						sess = sessMap.get(sessId);
					}
					else
					{
						sessMap = new HashMap<Long, JWTSession>();
						this.remoteSessMap.put(serverId, sessMap);
					}
				}

				if (sess != null)
				{
					if (!this.sendKeepAlive(serverId, sessId))
					{
						synchronized(this.remoteSessMap)
						{
							sessMap.remove(sessId);
							sess = null;
						}
					}
					else
					{
						sess.setLastAccessTime(System.currentTimeMillis());
					}
				}
				else
				{
					sess = this.sendGetSession(serverId, sessId);
					if (sess != null)
					{
						synchronized(this.remoteSessMap)
						{
							sess.setLastAccessTime(System.currentTimeMillis());
							sessMap.put(sessId, sess);
						}
						if (this.sessInit != null)
						{
							this.sessInit.initSession(sess);
						}
					}
				}
				return sess;
			}
		}
		return null;
	}

	public synchronized void setSessionListener(JWTSessionListener listener)
	{
		this.listener = listener;
	}

	private boolean sendReq(Map<String, Object> reqMap)
	{
		return cli.publish(this.topicName, JSONMapper.object2Json(reqMap));
	}

	private int nextReqId()
	{
		int ret;
		synchronized(this.reqMap)
		{
			ret = this.reqNextId++;
			if (this.reqNextId >= 65536)
			{
				this.reqNextId -= 65536;
			}
		}
		return ret;
	}

	private boolean sendKeepAlive(int serverId, long sessId)
	{
		JWTRequest req = new JWTRequest();
		req.evt = new ThreadEvent();
		req.reqId = nextReqId();
		req.reqEnd = false;
		req.reqResult = false;
		synchronized(this.reqMap)
		{
			this.reqMap.put(req.reqId, req);
		}
		Map<String, Object> reqMap = new HashMap<String, Object>();
		reqMap.put("act", "kareq");
		reqMap.put("svrId", String.valueOf(serverId));
		reqMap.put("sessId", String.valueOf(sessId));
		reqMap.put("reqSvr", String.valueOf(this.serverId));
		reqMap.put("reqId", String.valueOf(req.reqId));
		if (!sendReq(reqMap))
		{
			synchronized(this.reqMap)
			{
				this.reqMap.remove(req.reqId);
			}
			return false;
		}
		req.evt.waitEvent(500);
		synchronized(this.reqMap)
		{
			this.reqMap.remove(req.reqId);
		}
		return req.reqResult;
	}

	private JWTSession sendGetSession(int serverId, long sessId)
	{
		JWTRequest req = new JWTRequest();
		req.evt = new ThreadEvent();
		req.reqId = nextReqId();
		req.reqEnd = false;
		req.reqUserName = null;
		req.reqRoles = new ArrayList<String>();
		synchronized(this.reqMap)
		{
			this.reqMap.put(req.reqId, req);
		}
		Map<String, Object> reqMap = new HashMap<String, Object>();
		reqMap.put("act", "gsreq");
		reqMap.put("svrId", String.valueOf(serverId));
		reqMap.put("sessId", String.valueOf(sessId));
		reqMap.put("reqSvr", String.valueOf(this.serverId));
		reqMap.put("reqId", String.valueOf(req.reqId));
		if (!sendReq(reqMap))
		{
			synchronized(this.reqMap)
			{
				this.reqMap.remove(req.reqId);
			}
			return null;
		}
		long startTime = System.currentTimeMillis();
		long t;
		while (!req.reqEnd)
		{
			t = System.currentTimeMillis() - startTime;
			if (t >= 2000)
			{
				break;
			}
			req.evt.waitEvent(2000 - (int)t);
		}
		synchronized(this.reqMap)
		{
			this.reqMap.remove(req.reqId);
		}
		if (req.reqEnd && req.reqUserName != null && req.reqCnt == req.reqRoles.size())
		{
			JWTSession sess;
			synchronized(req.reqRoles)
			{
				sess = new JWTSession(sessId, req.reqUserName, req.reqRoles);
			}
			synchronized(this.remoteSessMap)
			{
				sessMap = this.remoteSessMap.get(serverId);
				if (sessMap == null)
				{
					sessMap = new HashMap<Long, JWTSession>();
					this.remoteSessMap.put(serverId, sessMap);
				}
				sessMap.put(sessId, sess);
			}
			return sess;
		}
		else
		{
			return null;
		}
	}

	@Override
	public void onPublishMessage(String topic, byte[] buff, int buffOfst, int buffSize)
	{
		JWTSession sess;
		Map<String, Object> reqMap;
		JWTRequest req;
		Object retObj = JSONParser.parse(new String(buff, buffOfst, buffSize, StandardCharsets.UTF_8));
		if (retObj != null && retObj instanceof Map)
		{
			@SuppressWarnings("unchecked")
			Map<String, Object> retMap = (Map<String, Object>)retObj;
			String action = (String)retMap.get("act");
			Integer serverId = StringUtil.toInteger((String)retMap.get("svrId"));
			Long sessId = StringUtil.toLong((String)retMap.get("sessId"));
			Integer reqServerId = StringUtil.toInteger((String)retMap.get("reqSvr"));
			Integer reqId = StringUtil.toInteger((String)retMap.get("reqId"));
			if (action != null && serverId == this.serverId && sessId != null && reqServerId != null && reqId != null)
			{
				switch (action)
				{
				case "kareq":
					synchronized(this)
					{
						sess = this.sessMap.get(sessId);
						if (sess != null)
						{
							sess.setLastAccessTime(System.currentTimeMillis());
						}
					}
					reqMap = new HashMap<String, Object>();
					reqMap.put("act", "karesp");
					reqMap.put("svrId", String.valueOf(reqServerId));
					reqMap.put("sessId", String.valueOf(sessId));
					reqMap.put("reqSvr", String.valueOf(this.serverId));
					reqMap.put("reqId", String.valueOf(reqId));
					reqMap.put("res", (sess != null)?"ok":"err");
					this.sendReq(reqMap);
					break;
				case "karesp":
					synchronized(this.reqMap)
					{
						req = this.reqMap.get(reqId);
					}
					if (req != null)
					{
						req.reqResult = "ok".equals((String)retMap.get("res"));
						req.reqEnd = true;
						req.evt.set();
					}
					break;
				case "gsreq":
					synchronized(this)
					{
						sess = this.sessMap.get(sessId);
						if (sess != null)
						{
							sess.setLastAccessTime(System.currentTimeMillis());
						}
					}
					reqMap = new HashMap<String, Object>();
					reqMap.put("act", "gsresp");
					reqMap.put("svrId", String.valueOf(reqServerId));
					reqMap.put("sessId", String.valueOf(sessId));
					reqMap.put("reqSvr", String.valueOf(this.serverId));
					reqMap.put("reqId", String.valueOf(reqId));
					if (sess == null)
					{
						reqMap.put("cnt", "0");
						reqMap.put("end", "1");
						this.sendReq(reqMap);
					}
					else
					{
						List<String> roleList = sess.getRoleList();
						reqMap.put("cnt", String.valueOf(roleList.size()));
						reqMap.put("user", sess.getUserName());
						StringBuilder sb = new StringBuilder();
						int i = 0;
						int j = 16;
						while (j < roleList.size())
						{
							sb.setLength(0);
							while (i < j)
							{
								if (sb.length() > 0)
								{
									sb.append(",");
								}
								sb.append(roleList.get(i));
								i++;
							}
							reqMap.put("roles", sb.toString());
							this.sendReq(reqMap);
							j += 16;
						}
						j = roleList.size();
						sb.setLength(0);
						while (i < j)
						{
							if (sb.length() > 0)
							{
								sb.append(",");
							}
							sb.append(roleList.get(i));
							i++;
						}
						reqMap.put("roles", sb.toString());
						reqMap.put("end", "1");
						this.sendReq(reqMap);
					}
					break;
				case "gsresp":
					synchronized(this.reqMap)
					{
						req = this.reqMap.get(reqId);
					}
					if (req != null)
					{
						req.reqUserName = (String)retMap.get("user");
						if (req.reqCnt == 0)
						{
							Integer icnt = StringUtil.toInteger((String)retMap.get("cnt"));
							if (icnt != null)
							{
								req.reqCnt = icnt;
							}
						}
						String roles = (String)retMap.get("roles");
						if (roles != null)
						{
							String[] roleArr = StringUtil.split(roles, ",");
							int i = 0;
							int j = roleArr.length;
							synchronized(req.reqRoles)
							{
								while (i < j)
								{
									req.reqRoles.add(roleArr[i]);
									i++;
								}
							}
						}
						if (req.reqCnt == req.reqRoles.size() && retMap.containsKey("end"))
						{
							req.reqEnd = true;
							req.evt.set();
						}
					}
					break;
				}
			}
		}
	}

	@Override
	public void onDisconnect()
	{
	}
}
