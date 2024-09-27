package org.sswr.util.web;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Iterator;

import org.sswr.util.basic.ArrayListInt64;
import org.sswr.util.basic.MyThread;
import org.sswr.util.basic.ThreadEvent;
import org.sswr.util.data.ByteTool;
import org.sswr.util.io.OSType;
import org.sswr.util.net.BrowserInfo;
import org.sswr.util.net.BrowserInfo.BrowserType;
import org.sswr.util.net.BrowserInfo.UserAgentInfo;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class MemoryWebSessionManager extends WebSessionManager implements Runnable
{
	private String path;
	private String cookieName;
	private int chkInterval;
	private boolean chkToStop;
	private boolean chkRunning;
	private Thread chkThread;
	private ArrayList<MemoryWebSession> sesses;
	private ArrayListInt64 sessIds;
	private ThreadEvent chkEvt;

	@Override
	public void run()
	{
		MemoryWebSession sess;
		long sessId;
		int i;
		int j;
	
		{
			long lastChkTime;
			long currTime;
			lastChkTime = System.currentTimeMillis();
	
			this.chkRunning = true;
			while (!this.chkToStop)
			{
				currTime = System.currentTimeMillis();
				if ((currTime - lastChkTime) >= this.chkInterval)
				{
					lastChkTime = System.currentTimeMillis();
	
					i = this.sesses.size();
					while (i-- > 0)
					{
						synchronized(this)
						{
							sess = this.sesses.get(i);
							sessId = this.sessIds.get(i);
						}
						if (sess != null)
						{
							boolean toDel;
							synchronized(sess)
							{
								toDel = this.hdlr.onSessionCheck(sess);
							}
							if (toDel)
							{
								synchronized(this)
								{
									j = this.sessIds.sortedIndexOf(sessId);
									this.sessIds.remove(j);
									sess = this.sesses.remove(j);
								}
	
								synchronized(sess)
								{
									this.hdlr.onSessionDeleted(sess);
								}
							}
						}
					}
				}
				this.chkEvt.waitEvent(1000);
			}
		}
		this.chkRunning = false;
	}

	private long getSessId(@Nonnull HttpServletRequest req)
	{
		try
		{
			Cookie[] cookies = req.getCookies();
			int i = cookies.length;
			while (i-- > 0)
			{
				if (cookies[i].getName().equals(this.cookieName))
				{
					return Long.parseLong(cookies[i].getValue());
				}
			}
		}
		catch (Exception ex)
		{
		}
		return 0;
	}

	public MemoryWebSessionManager(@Nonnull String path, @Nonnull SessionHandler hdlr, int chkInterval, @Nonnull String cookieName)
	{
		super(hdlr);
		this.sessIds = new ArrayListInt64();
		this.sesses = new ArrayList<MemoryWebSession>();
		this.chkEvt = new ThreadEvent(true);
		this.path = path;
		if (cookieName != null && cookieName.length() > 0)
			this.cookieName = cookieName;
		else
			this.cookieName = "WebSessId";
		this.chkInterval = chkInterval;
		this.chkToStop = false;
		this.chkRunning = false;
		this.chkThread = new Thread(this, "WebSessCheck");
		this.chkThread.start();
		while (!this.chkRunning)
		{
			MyThread.sleep(10);
		}
	}

	public void close()
	{
		this.chkToStop = true;
		this.chkEvt.set();
		while (this.chkRunning)
		{
			MyThread.sleep(10);
		}

		MemoryWebSession sess;
		Iterator<MemoryWebSession> it = this.sesses.iterator();
		while (it.hasNext())
		{
			sess = it.next();
			this.hdlr.onSessionDeleted(sess);
		}
	}

	@Override
	@Nullable
	public WebSession getSession(@Nonnull HttpServletRequest req, @Nonnull HttpServletResponse resp)
	{
		long sessId = this.getSessId(req);
		if (sessId == 0)
			return null;
		MemoryWebSession sess = (MemoryWebSession)this.getSession(sessId);
		if (sess != null)
		{
			UserAgentInfo ua = BrowserInfo.parseReq(req);
			if (!sess.requestValid(ua.browser, ua.os))
			{
				return null;
			}
			return sess;
		}
		return null;
	}

	@Override
	@Nonnull
	public WebSession createSession(@Nonnull HttpServletRequest req, @Nonnull HttpServletResponse resp)
	{
		WebSession sess = getSession(req, resp);
		if (sess != null)
			return sess;
		long sessId = this.genSessId(req);
		Cookie cookie = new Cookie(this.cookieName, String.valueOf(sessId));
		cookie.setPath(this.path);
		cookie.setHttpOnly(true);
		cookie.setSecure(req.isSecure());
		cookie.setAttribute("SameSite", "Strict");
		resp.addCookie(cookie);
		int i;
		UserAgentInfo ua = BrowserInfo.parseReq(req);
		sess = new MemoryWebSession(sessId, ua.browser, ua.os);
		synchronized(this)
		{
			i = this.sessIds.sortedInsert(sessId);
			this.sesses.add(i, (MemoryWebSession)sess);
		}
		return sess;
	}

	@Override
	public void deleteSession(@Nonnull HttpServletRequest req, @Nonnull HttpServletResponse resp) {
		long sessId = getSessId(req);
		int i;
		MemoryWebSession sess;
		if (sessId != 0)
		{
			sess = null;
			synchronized(this)
			{
				i = this.sessIds.sortedIndexOf(sessId);
				if (i >= 0)
				{
					sess = this.sesses.remove(i);
					this.sessIds.remove(i);
				}
			}
			if (sess != null)
			{
				synchronized(sess)
				{
					this.hdlr.onSessionDeleted(sess);
				}
			}
			Cookie cookie = new Cookie(this.cookieName, String.valueOf(sessId));
			cookie.setPath(this.path);
			cookie.setHttpOnly(true);
			cookie.setSecure(req.isSecure());
			cookie.setAttribute("SameSite", "Strict");
			cookie.setMaxAge(0);
			resp.addCookie(cookie);
		}
	}

	public long genSessId(@Nonnull HttpServletRequest req)
	{
		byte[] buff = new byte[8];
		buff[0] = 0;
		buff[1] = 0;
		ByteTool.writeInt16(buff, 2, req.getRemotePort());
		try
		{
			InetAddress addr = InetAddress.getByName(req.getRemoteAddr());
			if (addr != null)
			{
				ByteTool.writeInt32(buff, 4, addr.hashCode());
			}
			else
			{
				ByteTool.writeInt32(buff, 4, 0);
			}
		}
		catch (Exception ex)
		{
			ByteTool.writeInt32(buff, 4, 0);
		}
		return System.currentTimeMillis() + ByteTool.readInt64(buff, 0);
	}

	@Nonnull
	public WebSession createSession(long sessId)
	{
		int si;
		MemoryWebSession sess;
		synchronized (this)
		{
			si = this.sessIds.sortedIndexOf(sessId);
			if (si >= 0 && (sess = this.sesses.get(si)) != null)
			{

			}
			else
			{
				sess = new MemoryWebSession(sessId, BrowserType.Unknown, OSType.Unknown);
				int i = this.sessIds.sortedInsert(sessId);
				this.sesses.add(i, (MemoryWebSession)sess);
			}
		}
		return sess;
	}

	@Nullable
	public WebSession getSession(long sessId)
	{
		WebSession sess;
		int i;
		sess = null;
		synchronized (this)
		{
			i = this.sessIds.sortedIndexOf(sessId);
			if (i >= 0)
			{
				sess = this.sesses.get(i);
			}
		}
		return sess;
	}

	public void deleteSession(long sessId)
	{
		int i;
		MemoryWebSession sess;
		sess = null;
		synchronized (this)
		{
			i = this.sessIds.sortedIndexOf(sessId);
			if (i >= 0)
			{
				sess = this.sesses.remove(i);
				this.sessIds.remove(i);
			}
		}
		if (sess != null)
		{
			synchronized(sess)
			{
				this.hdlr.onSessionDeleted(sess);
			}
		}
	}

	public void getSessionIds(@Nonnull ArrayListInt64 sessIds)
	{
		synchronized(this)
		{
			sessIds.addAll(this.sessIds);
		}
	}

}
