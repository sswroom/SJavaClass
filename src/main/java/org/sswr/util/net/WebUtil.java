package org.sswr.util.net;

import java.time.DayOfWeek;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import org.sswr.util.data.DateTimeUtil;

public class WebUtil
{
	public static String date2Str(ZonedDateTime dt)
	{
		String wds[] = {"Mon, ", "Tue, ", "Wed, ", "Thu, ", "Fri, ", "Sat, ", "Sun, "};
		ZonedDateTime t = dt.withZoneSameInstant(ZoneOffset.UTC);
		DayOfWeek wd = t.getDayOfWeek();
		return wds[wd.ordinal()] + DateTimeUtil.toString(t, "dd MMM yyyy HH:mm:ss") + " GMT";
	}
}
