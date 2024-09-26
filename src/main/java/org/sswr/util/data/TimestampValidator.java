package org.sswr.util.data;

import java.sql.Timestamp;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class TimestampValidator
{
	private String fmt;

	public TimestampValidator(@Nonnull String format)
	{
		this.fmt = format;
	}

	public boolean isValid(@Nullable Timestamp ts, @Nullable String s)
	{
		if (ts == null)
		{
			return StringUtil.isNullOrEmpty(s);
		}
		return DateTimeUtil.toString(ts, fmt).equals(s);
	}
}
