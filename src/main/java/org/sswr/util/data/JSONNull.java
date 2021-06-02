package org.sswr.util.data;

public class JSONNull extends JSONBase
{
	public JSONNull()
	{

	}

	public JSType getJSType()
	{
		return JSType.NULL;
	}

	public void toJSONString(StringBuilder sb)
	{
		sb.append("null");
	}

	public boolean equals(String s)
	{
		return s.equals("null");
	}

	public boolean identical(JSONBase obj)
	{
		return obj.getJSType() == JSType.NULL;
	}
}
