package org.sswr.util.data;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

public class TimestampValidator
{
	private SimpleDateFormat fmt;

	public TimestampValidator(String format)
	{
		this.fmt = new SimpleDateFormat(format);
	}

	public boolean isValid(Timestamp ts, String s)
	{
		if (ts == null)
		{
			return StringUtil.isNullOrEmpty(s);
		}
		return fmt.format(ts).equals(s);
	}
}
