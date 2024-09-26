package org.sswr.util.data;

import jakarta.annotation.Nonnull;

public class JSONNumber extends JSONBase
{
	private double val;

	public JSONNumber(double val)
	{
		this.val = val;
	}

	@Nonnull
	public String toJSONString()
	{
		return StringUtil.fromDouble(this.val);
	}

	public boolean equals(@Nonnull String s)
	{
		return false;
	}

	public boolean identical(@Nonnull JSONBase obj)
	{
		if (!(obj instanceof JSONNumber))
			return false;
		return ((JSONNumber)obj).getValue() == this.val;
	}
	
	@Nonnull
	public String toString()
	{
		return StringUtil.fromDouble(this.val);
	}

	public double getValue()
	{
		return this.val;
	}
}
