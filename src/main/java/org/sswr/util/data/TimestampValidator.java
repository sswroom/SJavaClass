package org.sswr.util.data;

import java.sql.Timestamp;

public class TimestampValidator
{
	private String fmt;

	public TimestampValidator(String format)
	{
		this.fmt = format;
	}

	public boolean isValid(Timestamp ts, String s)
	{
		if (ts == null)
		{
			return StringUtil.isNullOrEmpty(s);
		}
		return DateTimeUtil.toString(ts, fmt).equals(s);
	}
}
