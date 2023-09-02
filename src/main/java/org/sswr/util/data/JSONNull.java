package org.sswr.util.data;

public class JSONNull extends JSONBase
{
	public JSONNull()
	{
	}	

	public String toJSONString()
	{
		return "null";
	}

	public boolean equals(String s)
	{
		return s.equals("null");
	}

	public boolean identical(JSONBase obj)
	{
		return obj instanceof JSONNull;
	}

	public String toString()
	{
		return "null";
	}

}
