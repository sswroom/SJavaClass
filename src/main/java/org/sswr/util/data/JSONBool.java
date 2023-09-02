package org.sswr.util.data;

public class JSONBool extends JSONBase
{
	private boolean val;

	public JSONBool(boolean val)
	{
		this.val = val;
	}

	public String toJSONString()
	{
		return this.val?"true":"false";
	}

	public boolean equals(String s)
	{
		if (s == null)
			return this.val == false;
		if (this.val)
			return s.equals("true");
		else
			return s.equals("false");
	}

	public boolean identical(JSONBase obj)
	{
		if (!(obj instanceof JSONBool))
			return false;
		return ((JSONBool)obj).getValue() == this.val;
	}
	public String toString()
	{
		return toJSONString();
	}

	public boolean getValue()
	{
		return this.val;
	}
}
