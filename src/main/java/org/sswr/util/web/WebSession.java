package org.sswr.util.web;

import org.sswr.util.io.OSType;
import org.sswr.util.net.BrowserInfo.BrowserType;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public interface WebSession
{
	public boolean requestValid(@Nonnull BrowserType browser, @Nonnull OSType os);
	public long getSessId();

	public void setValuePtr(@Nonnull String name, @Nullable Object val);
	public void setValueDbl(@Nonnull String name, double val);
	public void setValueInt64(@Nonnull String name, long val);
	public void setValueInt32(@Nonnull String name, int val);

	@Nullable
	public Object getValuePtr(@Nonnull String name);
	@Nullable
	public Double getValueDbl(@Nonnull String name);
	@Nullable
	public Long getValueInt64(@Nonnull String name);
	@Nullable
	public Integer getValueInt32(@Nonnull String name);
}
