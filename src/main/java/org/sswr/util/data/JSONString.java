package org.sswr.util.data;

public class JSONString extends JSONBase
{
	private String val;

	public JSONString(String val)
	{
		this.val = val;
	}

	public JSType getJSType()
	{
		return JSType.STRING;
	}
	
	public void toJSONString(StringBuilder sb)
	{
		JSText.toJSTextDQuote(sb, this.val);
	}

	public boolean equals(String s)
	{
		if (this.val == null)
		{
			return s == null;
		}
		else
		{
			return this.val.equals(s);
		}
	}

	public boolean identical(JSONBase obj)
	{
		if (obj.getJSType() != JSType.STRING)
			return false;
		String cs = ((JSONString)obj).getValue();
		if (this.val == null)
		{
			return cs == null;
		}
		else if (cs == null)
		{
			return false;
		}
		else
		{
			return cs.equals(this.val);
		}
	}

	public String getValue()
	{
		return this.val;
	}
}
