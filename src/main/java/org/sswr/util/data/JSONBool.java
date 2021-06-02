package org.sswr.util.data;

public class JSONBool extends JSONBase
{
	private boolean val;

	public JSONBool(boolean val)
	{
		this.val = val;
	}

	public JSType getJSType()
	{
		return JSType.BOOL;
	}

	public void toJSONString(StringBuilder sb)
	{
		sb.append(this.val);
	}

	public boolean equals(String s)
	{
		return false;
	}
	
	public boolean identical(JSONBase obj)
	{
		if (obj.getJSType() != JSType.BOOL)
			return false;
		return ((JSONBool)obj).getValue() == this.val;
	}
	
	public boolean getValue()
	{
		return this.val;
	}	
}
