package org.sswr.util.net;

import java.util.Map;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class AzureToken
{
	private Map<String, String> tokenData;

	AzureToken(@Nonnull Map<String, String> tokenData)
	{
		this.tokenData = tokenData;
	}	
	
	@Nullable
	public String getEmail()
	{
		return this.tokenData.get("preferred_username");
	}

	@Nullable
	public String getAuthenCode()
	{
		return this.tokenData.get("rh");
	}

	@Nullable
	public String getJWTValue(@Nonnull String name)
	{
		return this.tokenData.get(name);
	}
}
