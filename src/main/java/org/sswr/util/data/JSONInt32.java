package org.sswr.util.data;

import jakarta.annotation.Nonnull;

public class JSONInt32 extends JSONBase
{
	private int val;

	public JSONInt32(int val)
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
		if (!(obj instanceof JSONInt32))
			return false;
		return ((JSONInt32)obj).getValue() == this.val;
	}
	
	@Nonnull
	public String toString()
	{
		return String.valueOf(this.val);
	}

	public int getValue()
	{
		return this.val;
	}
}
