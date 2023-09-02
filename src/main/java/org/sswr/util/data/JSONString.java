package org.sswr.util.data;

public class JSONString extends JSONBase
{
	private String val;

	public JSONString(String val)
	{
		this.val = val;
	}

	public String toJSONString()
	{
		if (this.val == null)
			return "null";
		StringBuilder sb = new StringBuilder();
		JSText.toJSTextDQuote(sb, this.val);
		return sb.toString();
	}

	public boolean equals(String s)
	{
		if (this.val == null)
			return s == null;
		else if (s == null)
			return false;
		else
			return this.val.equals(s);
	}

	public boolean identical(JSONBase obj)
	{
		if (!(obj instanceof JSONString))
			return false;
		String cs = ((JSONString)obj).getValue();
		if (this.val == null)
			return cs == null;
		else if (cs == null)
			return false;
		else
			return this.val.equals(cs);
	}

	public String toString()
	{
		if (this.val == null)
			return "null";
		else
			return this.val;
	}

	public String getValue()
	{
		return this.val;
	}
}
