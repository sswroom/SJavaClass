package org.sswr.util.web;

import java.util.HashMap;

import org.sswr.util.io.OSType;
import org.sswr.util.net.BrowserInfo.BrowserType;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class MemoryWebSession implements WebSession
{
	private BrowserType browser;
	private OSType os;
	private HashMap<String, Object> items;
	private long sessId;

	public MemoryWebSession(long sessId, @Nonnull BrowserType browser, @Nonnull OSType os)
	{
		this.sessId = sessId;
		this.browser = browser;
		this.os = os;
		this.items = new HashMap<String, Object>();
	}
	
	public boolean requestValid(@Nonnull BrowserType browser, @Nonnull OSType os)
	{
		if (this.browser != BrowserType.Unknown && this.browser != browser)
			return false;
		if (this.os != OSType.Unknown && this.os != os)
			return false;
		return true;
	}

	public long getSessId()
	{
		return this.sessId;
	}

	public void setValuePtr(@Nonnull String name, @Nullable Object val)
	{
		this.items.put(name, val);
	}

	public void setValueDbl(@Nonnull String name, double val)
	{
		this.items.put(name, val);
	}

	public void setValueInt64(@Nonnull String name, long val)
	{
		this.items.put(name, val);
	}

	public void setValueInt32(@Nonnull String name, int val)
	{
		this.items.put(name, val);
	}

	@Nullable
	public Object getValuePtr(@Nonnull String name)
	{
		return this.items.get(name);
	}

	@Nullable
	public Double getValueDbl(@Nonnull String name)
	{
		Object obj = this.items.get(name);
		if (obj instanceof Double)
			return (Double)obj;
		return null;
	}

	@Nullable
	public Long getValueInt64(@Nonnull String name)
	{
		Object obj = this.items.get(name);
		if (obj instanceof Long)
			return (Long)obj;
		return null;
	}

	@Nullable
	public Integer getValueInt32(@Nonnull String name)
	{
		Object obj = this.items.get(name);
		if (obj instanceof Integer)
			return (Integer)obj;
		return null;
	}
}
