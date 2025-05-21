package org.sswr.util.web;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sswr.util.data.DateTimeUtil;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class JWTSession
{
	private long sessId;
	private @Nonnull List<String> roleList;
	private @Nonnull Timestamp lastAccessTime;
	private @Nonnull String userName;
	private @Nonnull Map<String, Serializable> objectMap;

	public JWTSession(long sessId, @Nonnull String userName, @Nonnull List<String> roleList)
	{
		this.sessId = sessId;
		this.userName = userName;
		this.lastAccessTime = DateTimeUtil.timestampNow();
		this.roleList = roleList;
		this.objectMap = new HashMap<String, Serializable>();
	}

	public long getSessId()
	{
		return this.sessId;
	}
	
	@Nonnull
	public List<String> getRoleList()
	{
		return this.roleList;
	}

	@Nonnull
	public String getUserName()
	{
		return this.userName;
	}

	public void setLastAccessTime(@Nonnull Timestamp lastAccessTime)
	{
		this.lastAccessTime = lastAccessTime;
	}

	@Nonnull
	public Timestamp getLastAccessTime()
	{
		return this.lastAccessTime;
	}

	public void setValue(@Nonnull String name, @Nullable Serializable val)
	{
		this.objectMap.put(name, val);
	}

	@Nullable
	public Serializable getValue(@Nonnull String name)
	{
		return this.objectMap.get(name);
	}

	@Nonnull
	public Map<String, Serializable> getValues()
	{
		return this.objectMap;
	}
}
