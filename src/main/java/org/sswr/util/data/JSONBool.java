package org.sswr.util.data;

import jakarta.annotation.Nonnull;

public class JSONBool extends JSONBase
{
	private boolean val;

	public JSONBool(boolean val)
	{
		this.val = val;
	}

	@Nonnull
	public String toJSONString()
	{
		return this.val?"true":"false";
	}

	public boolean equals(@Nonnull String s)
	{
		if (this.val)
			return s.equals("true");
		else
			return s.equals("false");
	}

	public boolean identical(@Nonnull JSONBase obj)
	{
		if (!(obj instanceof JSONBool))
			return false;
		return ((JSONBool)obj).getValue() == this.val;
	}
	
	@Nonnull
	public String toString()
	{
		return toJSONString();
	}

	public boolean getValue()
	{
		return this.val;
	}
}
