package org.sswr.util.data;

public class JSONNumber extends JSONBase
{
	private double val;

	public JSONNumber(double val)
	{
		this.val = val;
	}

	public String toJSONString()
	{
		return StringUtil.fromDouble(this.val);
	}

	public boolean equals(String s)
	{
		return false;
	}

	public boolean identical(JSONBase obj)
	{
		if (!(obj instanceof JSONNumber))
			return false;
		return ((JSONNumber)obj).getValue() == this.val;
	}
	public String toString()
	{
		return StringUtil.fromDouble(this.val);
	}

	public double getValue()
	{
		return this.val;
	}
}
