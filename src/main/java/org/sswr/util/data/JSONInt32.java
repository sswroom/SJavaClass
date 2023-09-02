package org.sswr.util.data;

public class JSONInt32 extends JSONBase
{
	private int val;

	public JSONInt32(int val)
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
		if (!(obj instanceof JSONInt32))
			return false;
		return ((JSONInt32)obj).getValue() == this.val;
	}
	public String toString()
	{
		return String.valueOf(this.val);
	}

	public int getValue()
	{
		return this.val;
	}
}
