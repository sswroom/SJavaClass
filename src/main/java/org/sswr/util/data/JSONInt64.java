package org.sswr.util.data;

import jakarta.annotation.Nonnull;

public class JSONInt64 extends JSONBase
{
	private long val;

	public JSONInt64(long val)
	{
		this.val = val;
	}

	@Nonnull
	public String toJSONString()
	{
		return String.valueOf(this.val);
	}

	public boolean equals(@Nonnull String s)
	{
		return false;
	}

	public boolean identical(@Nonnull JSONBase obj)
	{
		if (!(obj instanceof JSONInt64))
			return false;
		return ((JSONInt64)obj).getValue() == this.val;
	}
	
	@Nonnull
	public String toString()
	{
		return String.valueOf(this.val);
	}

	public long getValue()
	{
		return this.val;
	}
}
