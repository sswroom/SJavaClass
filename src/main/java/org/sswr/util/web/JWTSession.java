package org.sswr.util.web;

import java.util.List;

public class JWTSession
{
	private long sessId;
	private List<String> roleList;
	private long lastAccessTime;

	public JWTSession(long sessId, List<String> roleList)
	{
		this.sessId = sessId;
		this.roleList = roleList;
	}

	public long getSessId()
	{
		return this.sessId;
	}
	
	public List<String> getRoleList()
	{
		return this.roleList;
	}

	public void setLastAccessTime(long lastAccessTime)
	{
		this.lastAccessTime = lastAccessTime;
	}

	public long getLastAccessTime()
	{
		return this.lastAccessTime;
	}
}
