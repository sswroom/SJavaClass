package org.sswr.util.data;

import jakarta.annotation.Nonnull;

public class JSONNull extends JSONBase
{
	public JSONNull()
	{
	}	

	@Nonnull
	public String toJSONString()
	{
		return "null";
	}

	public boolean equals(@Nonnull String s)
	{
		return s.equals("null");
	}

	public boolean identical(@Nonnull JSONBase obj)
	{
		return obj instanceof JSONNull;
	}

	@Nonnull
	public String toString()
	{
		return "null";
	}

}
