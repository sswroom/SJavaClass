package org.sswr.util.data;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

//NN
public class DateTimeUtil
{
	private static @Nonnull ZonedDateTime setDate(@Nonnull ZonedDateTime dt, int year, int month, int day)
	{
		while (month <= 0)
		{
			month += 12;
			year--;
		}
		while (month > 12)
		{
			month -= 12;
			year++;
		}
		while (day <= 0)
		{
			month--;
			if (month <= 0)
			{
				month += 12;
				year--;
			}
			YearMonth ym = YearMonth.of(year, month);
			day += ym.lengthOfMonth();
		}
		while (true)
		{
			YearMonth ym = YearMonth.of(year, month);
			if (day <= ym.lengthOfMonth())
			{
				break;
			}
			day -= ym.lengthOfMonth();
			month++;
			if (month > 12)
			{
				year++;
				month -= 12;
			}
		}
		return dt.withDayOfMonth(1).withYear(year).withMonth(month).withDayOfMonth(day);
	}

	private static @Nonnull ZonedDateTime setDate(@Nonnull ZonedDateTime dt, String strs[])
	{
		int vals[] = new int[3];
		vals[0] = Integer.parseInt(strs[0]);
		vals[1] = Integer.parseInt(strs[1]);
		vals[2] = Integer.parseInt(strs[2]);
		if (vals[0] > 100)
		{
			return setDate(dt, vals[0], vals[1], vals[2]);
		}
		else if (vals[2] > 100)
		{
			if (vals[0] > 12)
			{
				return setDate(dt, vals[2], vals[1], vals[0]);
			}
			else
			{
				return setDate(dt, vals[2], vals[0], vals[1]);
			}
		}
		else
		{
			if (vals[1] > 12)
			{
				return setDate(dt, ((dt.getYear() / 100) * 100) + vals[2], vals[0], vals[1]);
			}
			else
			{
				return setDate(dt, ((dt.getYear() / 100) * 100) + vals[0], vals[1], vals[2]);
			}
		}
	}

	private static @Nonnull ZonedDateTime setTime(@Nonnull ZonedDateTime dt, String strs[])
	{
		int h;
		int m;
		int s;
		int i;
		if (strs.length == 3)
		{
			h = Integer.parseInt(strs[0]);
			m = Integer.parseInt(strs[1]);
			i = strs[2].indexOf(".");
			if (i >= 0)
			{
				s = Integer.parseInt(strs[2].substring(0, i));
				int ns = Integer.parseInt(strs[2].substring(i + 1));
				i = 9 - (strs[2].length() - i - 1);
				while (i-- > 0)
				{
					ns = ns * 10;
				}
				return dt.withHour(h).withMinute(m).withSecond(s).withNano(ns);
			}
			else
			{
				s = Integer.parseInt(strs[2]);
				return dt.withHour(h).withMinute(m).withSecond(s).truncatedTo(ChronoUnit.SECONDS);
			}
		}
		else if (strs.length == 2)
		{
			h = Integer.parseInt(strs[0]);
			m = Integer.parseInt(strs[1]);
			return dt.withHour(h).withMinute(m).truncatedTo(ChronoUnit.MINUTES);
		}
		else
		{
			throw new IllegalArgumentException();
		}
	}

	private static @Nonnull ZonedDateTime setTZ(@Nonnull ZonedDateTime dt, @Nonnull String tzStr)
	{
		if (tzStr.length() == 6)
		{
			int min = Integer.parseInt(tzStr.substring(4));
			if (tzStr.charAt(3) == ':')
			{
				min = min + Integer.parseInt(tzStr.substring(1, 3)) * 60;
			}
			else
			{
				throw new IllegalArgumentException();
			}
			char c = tzStr.charAt(0);
			if (c == '+')
			{

			}
			else if (c == '-')
			{
				min = -min;
			}
			else
			{
				throw new IllegalArgumentException();
			}
			return dt.withZoneSameLocal(ZoneOffset.ofTotalSeconds(min * 60));
		}
		throw new IllegalArgumentException("Unknown tz: "+tzStr);
	}

	public static @Nonnull LocalDateTime clearDayOfMonth(@Nonnull LocalDateTime dt)
	{
		return LocalDateTime.of(dt.getYear(), dt.getMonthValue(), 1, 0, 0);
	}

	public static @Nonnull Timestamp clearDayOfMonth(@Nonnull Timestamp ts)
	{
		return Timestamp.valueOf(clearDayOfMonth(ts.toLocalDateTime()));
	}

	public static @Nonnull ZonedDateTime clearTime(@Nonnull ZonedDateTime dt)
	{
		return dt.truncatedTo(ChronoUnit.DAYS);
	}

	public static @Nonnull LocalDateTime clearTime(@Nonnull LocalDateTime dt)
	{
		return dt.truncatedTo(ChronoUnit.DAYS);
	}

	public static @Nonnull Timestamp clearTime(@Nonnull Timestamp ts)
	{
		return Timestamp.valueOf(clearTime(ts.toLocalDateTime()));
	}

	public static @Nonnull LocalDateTime clearMs(@Nonnull LocalDateTime dt)
	{
		return dt.truncatedTo(ChronoUnit.SECONDS);
	}

	public static @Nonnull Timestamp clearMs(@Nonnull Timestamp ts)
	{
		return Timestamp.valueOf(clearMs(ts.toLocalDateTime()));
	}

	public static @Nonnull Timestamp toDayStart(@Nonnull Timestamp ts)
	{
		return clearTime(ts);
	}

	public static @Nonnull LocalDateTime toDayStart(@Nonnull LocalDateTime dt)
	{
		return clearTime(dt);
	}

	public static @Nonnull ZonedDateTime toDayStart(@Nonnull ZonedDateTime dt)
	{
		return clearTime(dt);
	}

	public static @Nonnull Timestamp toDayEnd(@Nonnull Timestamp ts)
	{
		return Timestamp.valueOf(toDayEnd(ts.toLocalDateTime()));
	}

	public static @Nonnull LocalDateTime toDayEnd(@Nonnull LocalDateTime dt)
	{
		return clearTime(dt).plusDays(1).minusNanos(1);
	}

	public static @Nonnull ZonedDateTime toDayEnd(@Nonnull ZonedDateTime dt)
	{
		return clearTime(dt).plusDays(1).minusNanos(1);
	}

	public static @Nonnull ZonedDateTime toMonthStart(@Nonnull ZonedDateTime dt)
	{
		return clearTime(dt).withDayOfMonth(1);
	}

	public static @Nonnull ZonedDateTime toYearStart(@Nonnull ZonedDateTime dt)
	{
		return clearTime(dt).withDayOfYear(1);
	}

	public static @Nullable Timestamp toTimestamp(@Nullable ZonedDateTime dt)
	{
		if (dt == null) return null;
		return Timestamp.from(dt.toInstant());
	}

	public static boolean isDayStart(@Nonnull Timestamp ts)
	{
		return ts.equals(clearTime(ts));
	}

	public static int parseMonthStr(@Nonnull String monStr)
	{
		String umonStr = monStr.toUpperCase();
		if (umonStr.startsWith("JAN"))
		{
			return 1;
		}
		else if (umonStr.startsWith("FEB"))
		{
			return 2;
		}
		else if (umonStr.startsWith("MAR"))
		{
			return 3;
		}
		else if (umonStr.startsWith("APR"))
		{
			return 4;
		}
		else if (umonStr.startsWith("MAY"))
		{
			return 5;
		}
		else if (umonStr.startsWith("JUN"))
		{
			return 6;
		}
		else if (umonStr.startsWith("JUL"))
		{
			return 7;
		}
		else if (umonStr.startsWith("AUG"))
		{
			return 8;
		}
		else if (umonStr.startsWith("SEP"))
		{
			return 9;
		}
		else if (umonStr.startsWith("OCT"))
		{
			return 10;
		}
		else if (umonStr.startsWith("NOV"))
		{
			return 11;
		}
		else if (umonStr.startsWith("DEC"))
		{
			return 12;
		}
		throw new IllegalArgumentException();
	}

	public static @Nonnull ZonedDateTime parse(@Nonnull String dateStr)
	{
		if (dateStr.length() < 5)
			throw new IllegalArgumentException("Cannot parse dateStr: "+dateStr);
		if (dateStr.charAt(3) == ',' && dateStr.indexOf(",", 4) == -1)
		{
			dateStr = dateStr.substring(4).trim();
		}
		ZonedDateTime dt = ZonedDateTime.now();
		String strs2[] = StringUtil.split(dateStr, " ");
		String strs[];
		if (strs2.length == 1)
		{
			strs2 = StringUtil.split(dateStr, "T");
		}
		if (strs2.length == 2)
		{
			if ((strs = StringUtil.split(strs2[0], "-")).length == 3)
			{
				dt = setDate(dt, strs);
			}
			else if ((strs = StringUtil.split(strs2[0], "/")).length == 3)
			{
				dt = setDate(dt, strs);
			}
			else if ((strs = StringUtil.split(strs2[0], ":")).length == 3)
			{
				dt = setDate(dt, strs);
			}
			else
			{
				throw new IllegalArgumentException("Cannot parse dateStr: "+dateStr);
			}
			String tz = null;
			int i = strs2[1].indexOf('+');
			if (i < 0)
			{
				i = strs2[1].indexOf('-');
			}
			if (i >= 0)
			{
				tz = strs2[1].substring(i);
				strs2[1] = strs2[1].substring(0, i);
			}
			strs = StringUtil.split(strs2[1], ":");
			if (strs.length == 3)
			{
				if (strs[2].endsWith("Z"))
				{
					strs[2] = strs[2].substring(0, strs[2].length() - 1);
					dt = dt.withZoneSameLocal(ZoneOffset.UTC);
				}
				else if (tz != null)
				{
					dt = setTime(dt, strs);
					dt = setTZ(dt, tz);
				}
				else
				{
					dt = setTime(dt, strs);
				}
				return dt;
			}
			else if (strs.length == 2)
			{
				dt = setTime(dt, strs);
				if (tz != null)
				{
					dt = setTZ(dt, tz);
				}
				return dt;
			}
			else
			{
				throw new IllegalArgumentException("Cannot parse dateStr: "+dateStr);
			}
		}
		else if (strs2.length == 1)
		{
			if ((strs = StringUtil.split(strs2[0], "-")).length == 3)
			{
				dt = setDate(dt, strs);
				dt = clearTime(dt);
				return dt;
			}
			else if ((strs = StringUtil.split(strs2[0], "/")).length == 3)
			{
				dt = setDate(dt, strs);
				dt = clearTime(dt);
				return dt;
			}
			else if ((strs = StringUtil.split(strs2[0], ":")).length == 3)
			{
				dt = setTime(dt, strs);
				return dt;
			}
			else
			{
				throw new IllegalArgumentException("Cannot parse dateStr: "+dateStr);
			}
		}
		else if (strs2.length == 4 || (strs2.length == 5 && (strs2[4].startsWith("-") || strs2[4].startsWith("+") || strs2[4].equals("GMT"))))
		{
			int len1 = strs2[0].length();
			int len2 = strs2[1].length();
			int len3 = strs2[2].length();
			if (len1 == 3 && len2 <= 2 && len3 == 4)
			{
				dt = dt.withDayOfMonth(1).withYear(Integer.parseInt(strs2[2])).withMonth(parseMonthStr(strs2[0])).withDayOfMonth(Integer.parseInt(strs2[1]));
			}
			else if (len1 <= 2 && len2 == 3 && len3 == 4)
			{
				dt = dt.withDayOfMonth(1).withYear(Integer.parseInt(strs2[2])).withMonth(parseMonthStr(strs2[1])).withDayOfMonth(Integer.parseInt(strs2[0]));
			}
			else
			{
				throw new IllegalArgumentException("Cannot parse dateStr: "+dateStr);
			}
			strs = StringUtil.split(strs2[3], ":");
			if (strs.length == 3)
			{
				dt = setTime(dt, strs);
			}
			else if (strs.length == 2)
			{
				dt = setTime(dt, strs);
			}
			else
			{
				throw new IllegalArgumentException("Cannot parse dateStr: "+dateStr);
			}
			
			if (strs2.length == 5)
			{
				if (strs2[4].equals("GMT"))
				{
					dt = dt.withZoneSameLocal(ZoneOffset.UTC);
				}
				else if (strs2[4].length() == 5)
				{
					int min = Integer.parseInt(strs2[4].substring(3));
					if (strs2[4].charAt(2) == ':')
					{
						min = min + Integer.parseInt(strs2[4].substring(1, 3)) * 60;
					}
					else
					{
						throw new IllegalArgumentException("Cannot parse dateStr: "+dateStr);
					}
					if (strs2[4].startsWith("-"))
					{
						dt = dt.withZoneSameLocal(ZoneOffset.ofTotalSeconds(-min * 60));
					}
					else if (strs2[4].startsWith("+"))
					{
						dt = dt.withZoneSameLocal(ZoneOffset.ofTotalSeconds(min * 60));
					}
				}
				else
				{
					throw new IllegalArgumentException("Cannot parse dateStr: "+dateStr);
				}
			}
			return dt;
		}
		else
		{
			throw new IllegalArgumentException("Cannot parse dateStr: "+dateStr);
		}
	}

	public static double calcMonthDiff(@Nonnull LocalDateTime t1, @Nonnull LocalDateTime t2)
	{
		int yDiff = t1.getYear() - t2.getYear();
		int mDiff;
		mDiff = yDiff * 12 + t1.getMonth().getValue() - t2.getMonth().getValue();
		YearMonth ym = YearMonth.of(t1.getYear(), t1.getMonth());
		double dDiff = t1.getNano() - t2.getNano();
		dDiff = (t1.getSecond() - t2.getSecond()) + dDiff / 1000000000.0;
		dDiff = (t1.getMinute() - t2.getMinute()) + dDiff / 60.0;
		dDiff = (t1.getHour() - t2.getHour()) + dDiff / 60.0;
		dDiff = (t1.getDayOfMonth() - t2.getDayOfMonth()) + dDiff / 24.0;
		return mDiff + dDiff / ym.lengthOfMonth();
	}

	public static double calcMonthDiff(@Nonnull Timestamp t1, @Nonnull Timestamp t2)
	{
		return calcMonthDiff(t1.toLocalDateTime(), t2.toLocalDateTime());
	}

	public static double calcDayDiff(@Nonnull Timestamp t1, @Nonnull Timestamp t2)
	{
		return (t1.getTime() - t2.getTime()) / 86400000.0;
	}

	public static double calcDayDiff(@Nonnull ZonedDateTime t1, @Nonnull ZonedDateTime t2)
	{
		return (getTimeMillis(t1) - getTimeMillis(t2)) / 86400000.0;
	}

	public static @Nonnull Timestamp addSecond(@Nonnull Timestamp t, int secondDiff)
	{
		return Timestamp.valueOf(t.toLocalDateTime().plusSeconds(secondDiff));
	}

	public static @Nonnull Timestamp addDay(@Nonnull Timestamp t, int dayDiff)
	{
		return Timestamp.valueOf(t.toLocalDateTime().plusDays(dayDiff));
	}

	public static @Nonnull Timestamp addMonth(@Nonnull Timestamp t, int monthDiff)
	{
		return Timestamp.valueOf(t.toLocalDateTime().plusMonths(monthDiff));
	}

	public static @Nonnull Timestamp toWeekdayBefore(@Nonnull Timestamp t, @Nonnull DayOfWeek weekday)
	{
		return Timestamp.valueOf(toWeekdayBefore(t.toLocalDateTime(), weekday));
	}

	public static @Nonnull LocalDateTime toWeekdayBefore(@Nonnull LocalDateTime t, @Nonnull DayOfWeek weekday)
	{
		t = toDayStart(t);
		while (t.getDayOfWeek() != weekday)
		{
			t = t.minusDays(1);
		}
		return t;
	}

	public static @Nonnull ZonedDateTime toWeekdayBefore(@Nonnull ZonedDateTime t, @Nonnull DayOfWeek weekday)
	{
		t = toDayStart(t);
		while (t.getDayOfWeek() != weekday)
		{
			t = t.minusDays(1);
		}
		return t;
	}

	public static @Nonnull ZonedDateTime newZonedDateTime(@Nonnull Timestamp ts)
	{
		return newZonedDateTime(ts.toInstant());
	}

	public static @Nonnull ZonedDateTime newZonedDateTime(long t)
	{
		return ZonedDateTime.ofInstant(Instant.ofEpochMilli(t), ZoneId.systemDefault());
	}

	public static @Nonnull ZonedDateTime newZonedDateTime(@Nonnull Instant inst)
	{
		return ZonedDateTime.ofInstant(inst, ZoneId.systemDefault());
	}

	public static long getTimeMillis(@Nonnull ZonedDateTime t)
	{
		return t.toInstant().toEpochMilli();
	}

	public static boolean isSameMonth(@Nonnull ZonedDateTime t1, @Nonnull ZonedDateTime t2)
	{
		if (!t2.getZone().equals(t1.getZone()))
		{
			t2 = t2.withZoneSameInstant(t1.getZone());
		}
		return t1.getYear() == t2.getYear() && t1.getMonthValue() == t2.getMonthValue();
	}

	public static boolean isSameWeek(@Nonnull ZonedDateTime t1, @Nonnull ZonedDateTime t2, @Nonnull DayOfWeek weekday)
	{
		if (!t2.getZone().equals(t1.getZone()))
		{
			t2 = t2.withZoneSameInstant(t1.getZone());
		}
		t1 = toWeekdayBefore(t1, weekday);
		double diff = calcDayDiff(t2, t1);
		return diff >= 0 && diff < 7;
	}

	public static boolean isSameDay(@Nullable ZonedDateTime t1, @Nullable ZonedDateTime t2)
	{
		if (t1 == t2)
			return true;
		if (t1 == null || t2 == null)
			return false;
		if (!t2.getZone().equals(t1.getZone()))
		{
			t2 = t2.withZoneSameInstant(t1.getZone());
			if (t2 == null)
				return false;
		}
		return isSameMonth(t1, t2) && t1.getDayOfMonth() == t2.getDayOfMonth();
	}

	public static boolean isSameDay(@Nonnull Timestamp t1, @Nonnull Timestamp t2)
	{
		return isSameDay(newZonedDateTime(t1), newZonedDateTime(t2));
	}

	public static boolean isSameHour(@Nonnull ZonedDateTime t1, @Nonnull ZonedDateTime t2)
	{
		if (t1 == t2)
		{
			return true;
		}
		if (t1 == null || t2 == null)
		{
			return false;
		}
		if (!t2.getZone().equals((t1.getZone())))
		{
			t2 = t2.withZoneSameInstant(t1.getZone());
		}
		return t1.getYear() == t2.getYear() && t1.getMonthValue() == t2.getMonthValue() && t1.getDayOfMonth() == t2.getDayOfMonth() && t1.getHour() == t2.getHour();
	}

	public static boolean isSameHour(@Nonnull Timestamp t1, @Nonnull Timestamp t2)
	{
		return isSameHour(newZonedDateTime(t1), newZonedDateTime(t2));
	}

	public static String toString(@Nonnull ZonedDateTime t, @Nonnull String format)
	{
		int i = 0;
		int j;
		while ((j = format.indexOf('z', i)) >= 0)
		{
			if (j + 1 >= format.length())
				break;
			if (format.charAt(j + 1) == 'z')
			{
				format = format.substring(0, j) + format.substring(j + 1);
				i = j;
				while (i < format.length() && format.charAt(i) == 'z')
				{
					i++;
				}
			}
			else
			{
				i = j + 1;
			}
		}
		return DateTimeFormatter.ofPattern(format.replace('f', 'n').replace('z', 'x')).format(t);
	}

	public static @Nonnull String toString(@Nonnull Timestamp ts, @Nonnull String format)
	{
		return toString(ZonedDateTime.ofInstant(ts.toInstant(), ZoneId.systemDefault()), format);
	}

	public static @Nonnull String toString(@Nonnull Date dat, @Nonnull String format)
	{
		return toString(ZonedDateTime.of(dat.toLocalDate().atStartOfDay(), ZoneId.systemDefault()), format);
	}

	public static @Nonnull String toString(@Nonnull Time tim, @Nonnull String format)
	{
		return toString(ZonedDateTime.of(tim.toLocalTime().atDate(LocalDate.now()), ZoneId.systemDefault()), format);
	}

	public static @Nonnull String toString(@Nonnull ZonedDateTime t)
	{
		int nano = t.getNano();
		if (nano == 0)
		{
			return toString(t, "yyyy-MM-dd HH:mm:ss zzzz");
		}
		else if ((nano % 1000000) == 0)
		{
			return toString(t, "yyyy-MM-dd HH:mm:ss.fff zzzz");
		}
		else if ((nano % 1000) == 0)
		{
			return toString(t, "yyyy-MM-dd HH:mm:ss.ffffff zzzz");
		}
		else
		{
			return toString(t, "yyyy-MM-dd HH:mm:ss.fffffffff zzzz");
		}
	}

	public static @Nonnull String toString(@Nonnull Timestamp ts)
	{
		int nano = ts.getNanos();
		if (nano == 0)
		{
			return toString(ts, "yyyy-MM-dd HH:mm:ss zzzz");
		}
		else if ((nano % 1000000) == 0)
		{
			return toString(ts, "yyyy-MM-dd HH:mm:ss.fff zzzz");
		}
		else if ((nano % 1000) == 0)
		{
			return toString(ts, "yyyy-MM-dd HH:mm:ss.ffffff zzzz");
		}
		else
		{
			return toString(ts, "yyyy-MM-dd HH:mm:ss.fffffffff zzzz");
		}
	}

	public static @Nonnull String toString(@Nonnull Date dat)
	{
		return toString(dat, "yyyy-MM-dd");
	}

	public static @Nonnull String toStringNoZone(@Nonnull ZonedDateTime t)
	{
		int nano = t.getNano();
		if (nano == 0)
		{
			return toString(t, "yyyy-MM-dd HH:mm:ss");
		}
		else if ((nano % 1000000) == 0)
		{
			return toString(t, "yyyy-MM-dd HH:mm:ss.fff");
		}
		else if ((nano % 1000) == 0)
		{
			return toString(t, "yyyy-MM-dd HH:mm:ss.ffffff");
		}
		else
		{
			return toString(t, "yyyy-MM-dd HH:mm:ss.fffffffff");
		}
	}

	public static @Nonnull String toStringNoZone(@Nonnull Timestamp ts)
	{
		int nano = ts.getNanos();
		if (nano == 0)
		{
			return toString(ts, "yyyy-MM-dd HH:mm:ss");
		}
		else if ((nano % 1000000) == 0)
		{
			return toString(ts, "yyyy-MM-dd HH:mm:ss.fff");
		}
		else if ((nano % 1000) == 0)
		{
			return toString(ts, "yyyy-MM-dd HH:mm:ss.ffffff");
		}
		else
		{
			return toString(ts, "yyyy-MM-dd HH:mm:ss.fffffffff");
		}
	}

	public static int toYMD(@Nonnull ZonedDateTime dt)
	{
		return dt.getYear() * 10000 + dt.getMonthValue() * 100 + dt.getDayOfMonth();
	}

	public static int toYMD(@Nonnull Timestamp ts)
	{
		return toYMD(newZonedDateTime(ts));
	}

	public static @Nonnull Timestamp timestampNow()
	{
		return Timestamp.from(Instant.now());
	}

	public static double timeDiffSec(@Nonnull Instant t1, @Nonnull Instant t2)
	{
		double v = (double)(t1.getEpochSecond() - t2.getEpochSecond());
		v += (t1.getNano() - t2.getNano()) * 0.000000001;
		return v;
	}
	
	public static boolean equals(@Nonnull Timestamp t1, @Nonnull Timestamp t2)
	{
		if (t1 == t2)
			return true;
		if (t1 == null || t2 == null)
			return false;
		return t1.equals(t2);
	}
}
