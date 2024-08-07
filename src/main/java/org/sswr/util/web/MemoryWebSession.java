package org.sswr.util.web;

import java.util.HashMap;

import org.sswr.util.basic.MyThread;
import org.sswr.util.io.OSType;
import org.sswr.util.net.BrowserInfo.BrowserType;

public class MemoryWebSession implements WebSession
{
	private BrowserType browser;
	private OSType os;
	private HashMap<String, Object> items;
	private long sessId;
	private long threadId;
	private long threadUseCnt;

	public MemoryWebSession(long sessId, BrowserType browser, OSType os)
	{
		this.sessId = sessId;
		this.browser = browser;
		this.os = os;
		this.items = new HashMap<String, Object>();
		this.threadId = 0;
		this.threadUseCnt = 0;
	}
	
	public boolean requestValid(BrowserType browser, OSType os)
	{
		if (this.browser != BrowserType.Unknown && this.browser != browser)
			return false;
		if (this.os != OSType.Unknown && this.os != os)
			return false;
		return true;
	}

	public void beginUse()
	{
		long threadId = Thread.currentThread().getId();
		while (true)
		{
			synchronized(this)
			{
				if (this.threadId == 0)
				{
					this.threadId = threadId;
					this.threadUseCnt = 1;
					break;
				}
				else if (this.threadId == threadId)
				{
					this.threadUseCnt++;
					break;
				}
			}
			MyThread.sleep(10);
		}
	}

	public void endUse()
	{
		synchronized(this)
		{
			if ((--this.threadUseCnt) == 0)
			{
				this.threadId = 0;
			}
		}
	}

	public long getSessId()
	{
		return this.sessId;
	}

	public void setValuePtr(String name, Object val)
	{
		this.items.put(name, val);
	}

	public void setValueDbl(String name, double val)
	{
		this.items.put(name, val);
	}

	public void setValueInt64(String name, long val)
	{
		this.items.put(name, val);
	}

	public void setValueInt32(String name, int val)
	{
		this.items.put(name, val);
	}

	public Object getValuePtr(String name)
	{
		return this.items.get(name);
	}

	public Double getValueDbl(String name)
	{
		Object obj = this.items.get(name);
		if (obj instanceof Double)
			return (Double)obj;
		return null;
	}

	public Long getValueInt64(String name)
	{
		Object obj = this.items.get(name);
		if (obj instanceof Long)
			return (Long)obj;
		return null;
	}

	public Integer getValueInt32(String name)
	{
		Object obj = this.items.get(name);
		if (obj instanceof Integer)
			return (Integer)obj;
		return null;
	}
}
