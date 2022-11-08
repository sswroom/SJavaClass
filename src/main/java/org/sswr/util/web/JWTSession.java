package org.sswr.util.web;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JWTSession
{
	private long sessId;
	private List<String> roleList;
	private Timestamp lastAccessTime;
	private String userName;
	private Map<String, Object> objectMap;

	public JWTSession(long sessId, String userName, List<String> roleList)
	{
		this.sessId = sessId;
		this.userName = userName;
		this.roleList = roleList;
		this.objectMap = new HashMap<String, Object>();
	}

	public long getSessId()
	{
		return this.sessId;
	}
	
	public List<String> getRoleList()
	{
		return this.roleList;
	}

	public String getUserName()
	{
		return this.userName;
	}

	public void setLastAccessTime(Timestamp lastAccessTime)
	{
		this.lastAccessTime = lastAccessTime;
	}

	public Timestamp getLastAccessTime()
	{
		return this.lastAccessTime;
	}

	public void setValue(String name, Object val)
	{
		this.objectMap.put(name, val);
	}

	public Object getValue(String name)
	{
		return this.objectMap.get(name);
	}
}
