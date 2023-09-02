package org.sswr.util.data;

public class JSONInt64 extends JSONBase
{
	private long val;

	public JSONInt64(long val)
	{
		this.val = val;
	}

	public String toJSONString()
	{
		return String.valueOf(this.val);
	}

	public boolean equals(String s)
	{
		return false;
	}

	public boolean identical(JSONBase obj)
	{
		if (!(obj instanceof JSONInt64))
			return false;
		return ((JSONInt64)obj).getValue() == this.val;
	}
	public String toString()
	{
		return String.valueOf(this.val);
	}

	public long getValue()
	{
		return this.val;
	}
}
