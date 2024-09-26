package org.sswr.util.data;

import jakarta.annotation.Nonnull;

public class JSONString extends JSONBase
{
	private String val;

	public JSONString(@Nonnull String val)
	{
		this.val = val;
	}

	@Nonnull
	public String toJSONString()
	{
		if (this.val == null)
			return "null";
		StringBuilder sb = new StringBuilder();
		JSText.toJSTextDQuote(sb, this.val);
		return sb.toString();
	}

	public boolean equals(@Nonnull String s)
	{
		if (this.val == null)
			return s == null;
		else
			return this.val.equals(s);
	}

	public boolean identical(@Nonnull JSONBase obj)
	{
		if (!(obj instanceof JSONString))
			return false;
		String cs = ((JSONString)obj).getValue();
		if (this.val == null)
			return cs == null;
		else
			return this.val.equals(cs);
	}

	@Nonnull
	public String toString()
	{
		if (this.val == null)
			return "null";
		else
			return this.val;
	}

	@Nonnull
	public String getValue()
	{
		return this.val;
	}
}
