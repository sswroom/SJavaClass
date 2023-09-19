package org.sswr.util.net;

import java.util.Map;

public class AzureToken
{
	private Map<String, String> tokenData;

	AzureToken(Map<String, String> tokenData)
	{
		this.tokenData = tokenData;
	}	
	
	public String getEmail()
	{
		return this.tokenData.get("preferred_username");
	}

	public String getAuthenCode()
	{
		return this.tokenData.get("rh");
	}

	public String getJWTValue(String name)
	{
		return this.tokenData.get(name);
	}
}
