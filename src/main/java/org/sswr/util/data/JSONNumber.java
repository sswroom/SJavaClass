package org.sswr.util.data;

public class JSONNumber extends JSONBase
{
	private double val;

	public JSONNumber(double val)
	{
		this.val = val;
	}

	public JSType getJSType()
	{
		return JSType.NUMBER;
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
		if (obj.getJSType() != JSType.NUMBER)
			return false;
		return ((JSONNumber)obj).getValue() == this.val;
	}
	
	public double getValue()
	{
		return this.val;
	}
}
