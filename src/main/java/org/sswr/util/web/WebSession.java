package org.sswr.util.web;

import org.sswr.util.io.OSType;
import org.sswr.util.net.BrowserInfo.BrowserType;

public interface WebSession
{
	public boolean requestValid(BrowserType browser, OSType os);
	public void beginUse();
	public void endUse();
	public long getSessId();

	public void setValuePtr(String name, Object val);
	public void setValueDbl(String name, double val);
	public void setValueInt64(String name, long val);
	public void setValueInt32(String name, int val);

	public Object getValuePtr(String name);
	public Double getValueDbl(String name);
	public Long getValueInt64(String name);
	public Integer getValueInt32(String name);
}
