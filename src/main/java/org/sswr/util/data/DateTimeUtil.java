package org.sswr.util.data;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.DateTimeException;
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

public class DateTimeUtil
{
	public static class DateValue
	{
		public int year;
		public byte month;
		public byte day;
	}

	@Nonnull
	private static ZonedDateTime setDate(@Nonnull ZonedDateTime dt, int year, int month, int day)
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

	@Nonnull
	private static ZonedDateTime setDate(@Nonnull ZonedDateTime dt, String strs[])
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

	@Nonnull
	private static ZonedDateTime setTime(@Nonnull ZonedDateTime dt, String strs[])
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

	@Nonnull
	private static ZonedDateTime setTZ(@Nonnull ZonedDateTime dt, @Nonnull String tzStr)
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

	@Nonnull
	public static LocalDateTime clearDayOfMonth(@Nonnull LocalDateTime dt)
	{
		return LocalDateTime.of(dt.getYear(), dt.getMonthValue(), 1, 0, 0);
	}

	@Nonnull
	public static Timestamp clearDayOfMonth(@Nonnull Timestamp ts)
	{
		return Timestamp.valueOf(clearDayOfMonth(ts.toLocalDateTime()));
	}

	@Nonnull
	public static ZonedDateTime clearTime(@Nonnull ZonedDateTime dt)
	{
		return dt.truncatedTo(ChronoUnit.DAYS);
	}

	@Nonnull
	public static LocalDateTime clearTime(@Nonnull LocalDateTime dt)
	{
		return dt.truncatedTo(ChronoUnit.DAYS);
	}

	@Nonnull
	public static Timestamp clearTime(@Nonnull Timestamp ts)
	{
		return Timestamp.valueOf(clearTime(ts.toLocalDateTime()));
	}

	@Nonnull
	public static LocalDateTime clearMs(@Nonnull LocalDateTime dt)
	{
		return dt.truncatedTo(ChronoUnit.SECONDS);
	}

	@Nonnull
	public static Timestamp clearMs(@Nonnull Timestamp ts)
	{
		return Timestamp.valueOf(clearMs(ts.toLocalDateTime()));
	}

	@Nonnull
	public static Timestamp toDayStart(@Nonnull Timestamp ts)
	{
		return clearTime(ts);
	}

	@Nonnull
	public static LocalDateTime toDayStart(@Nonnull LocalDateTime dt)
	{
		return clearTime(dt);
	}

	@Nonnull
	public static ZonedDateTime toDayStart(@Nonnull ZonedDateTime dt)
	{
		return clearTime(dt);
	}

	@Nonnull
	public static Timestamp toDayEnd(@Nonnull Timestamp ts)
	{
		return Timestamp.valueOf(toDayEnd(ts.toLocalDateTime()));
	}

	@Nonnull
	public static LocalDateTime toDayEnd(@Nonnull LocalDateTime dt)
	{
		return clearTime(dt).plusDays(1).minusNanos(1);
	}

	@Nonnull
	public static ZonedDateTime toDayEnd(@Nonnull ZonedDateTime dt)
	{
		return clearTime(dt).plusDays(1).minusNanos(1);
	}

	@Nonnull
	public static ZonedDateTime toMonthStart(@Nonnull ZonedDateTime dt)
	{
		return clearTime(dt).withDayOfMonth(1);
	}

	@Nonnull
	public static ZonedDateTime toYearStart(@Nonnull ZonedDateTime dt)
	{
		return clearTime(dt).withDayOfYear(1);
	}

	@Nullable
	public static Timestamp toTimestamp(@Nullable ZonedDateTime dt)
	{
		if (dt == null) return null;
		return Timestamp.from(dt.toInstant());
	}

	@Nullable
	public static Date toDate(@Nullable LocalDate dat)
	{
		if (dat == null) return null;
		return Date.valueOf(dat);
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

	@Nonnull
	public static ZonedDateTime parse(@Nonnull String dateStr)
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

	@Nonnull
	public static Timestamp addSecond(@Nonnull Timestamp t, int secondDiff)
	{
		return Timestamp.valueOf(t.toLocalDateTime().plusSeconds(secondDiff));
	}

	@Nonnull
	public static Timestamp addDay(@Nonnull Timestamp t, int dayDiff)
	{
		return Timestamp.valueOf(t.toLocalDateTime().plusDays(dayDiff));
	}

	@Nonnull
	public static Timestamp addMonth(@Nonnull Timestamp t, int monthDiff)
	{
		return Timestamp.valueOf(t.toLocalDateTime().plusMonths(monthDiff));
	}

	@Nonnull
	public static Timestamp toWeekdayBefore(@Nonnull Timestamp t, @Nonnull DayOfWeek weekday)
	{
		return Timestamp.valueOf(toWeekdayBefore(t.toLocalDateTime(), weekday));
	}

	@Nonnull
	public static LocalDateTime toWeekdayBefore(@Nonnull LocalDateTime t, @Nonnull DayOfWeek weekday)
	{
		t = toDayStart(t);
		while (t.getDayOfWeek() != weekday)
		{
			t = t.minusDays(1);
		}
		return t;
	}

	@Nonnull
	public static ZonedDateTime toWeekdayBefore(@Nonnull ZonedDateTime t, @Nonnull DayOfWeek weekday)
	{
		t = toDayStart(t);
		while (t.getDayOfWeek() != weekday)
		{
			t = t.minusDays(1);
		}
		return t;
	}

	@Nonnull
	public static ZonedDateTime newZonedDateTime(@Nonnull Timestamp ts)
	{
		return newZonedDateTime(ts.toInstant());
	}

	@Nonnull
	public static ZonedDateTime newZonedDateTime(long t)
	{
		return ZonedDateTime.ofInstant(Instant.ofEpochMilli(t), ZoneId.systemDefault());
	}

	@Nonnull
	public static ZonedDateTime newZonedDateTime(@Nonnull Instant inst)
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

	@Nonnull
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

	@Nonnull
	public static String toString(@Nonnull Timestamp ts, @Nonnull String format)
	{
		return toString(ZonedDateTime.ofInstant(ts.toInstant(), ZoneId.systemDefault()), format);
	}

	@Nonnull
	public static String toString(@Nonnull Date dat, @Nonnull String format)
	{
		return toString(ZonedDateTime.of(dat.toLocalDate().atStartOfDay(), ZoneId.systemDefault()), format);
	}

	@Nonnull
	public static String toString(@Nonnull LocalDate dat, @Nonnull String format)
	{
		return toString(dat.atStartOfDay(ZoneId.systemDefault()), format);
	}

	@Nonnull
	public static String toString(@Nonnull Time tim, @Nonnull String format)
	{
		return toString(ZonedDateTime.of(tim.toLocalTime().atDate(LocalDate.now()), ZoneId.systemDefault()), format);
	}

	@Nonnull
	public static String toString(@Nonnull ZonedDateTime t)
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

	@Nonnull
	public static String toString(@Nonnull Timestamp ts)
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

	@Nonnull
	public static String toString(@Nonnull Date dat)
	{
		return toString(dat, "yyyy-MM-dd");
	}

	@Nonnull
	public static String toStringNoZone(@Nonnull ZonedDateTime t)
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

	@Nonnull
	public static String toStringNoZone(@Nonnull Timestamp ts)
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

	@Nonnull
	public static String toStringISO8601(@Nonnull ZonedDateTime ts)
	{
		return ts.toInstant().toString();
	}

	public static int toYMD(@Nonnull ZonedDateTime dt)
	{
		return dt.getYear() * 10000 + dt.getMonthValue() * 100 + dt.getDayOfMonth();
	}

	public static int toYMD(@Nonnull Timestamp ts)
	{
		return toYMD(newZonedDateTime(ts));
	}

	@Nonnull
	public static Timestamp timestampNow()
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

	public static byte getLocalTZQhr()
	{
		ZonedDateTime now = ZonedDateTime.now();
		return getTZQhr(now.getOffset());
	}

	public static byte getTZQhr(@Nonnull ZoneOffset z)
	{
		int secs = z.getTotalSeconds();
		return (byte)(secs / 900);
	}

	@Nonnull
	public static ZoneOffset fromTZQhr(byte tzQhr)
	{
		return ZoneOffset.ofHoursMinutes(tzQhr / 4, (tzQhr % 4) * 15);
	}

	@Nonnull
	public static Instant newInstant(long secs, int nanosec)
	{
		try
		{
			return Instant.ofEpochSecond(secs, nanosec);
		}
		catch (DateTimeException ex)
		{
			throw new IllegalArgumentException("Unsupported values: secs = "+secs+", nanosec = "+nanosec, ex);
		}
	}

	@Nonnull
	public static ZonedDateTime newZonedDateTime(Instant inst, byte tzQhr)
	{
		return ZonedDateTime.ofInstant(inst, fromTZQhr(tzQhr));
	}
	
	public static long getTotalDays(@Nullable Date dat)
	{
		if (dat == null)
		{
			return getTotalDays((LocalDate)null);
		}
		else
		{
			return getTotalDays(dat.toLocalDate());
		}
	}

	public static long getTotalDays(@Nullable LocalDate dat)
	{
		if (dat == null)
		{
			return -1234567;
		}
		else
		{
			return date2TotalDays(dat.getYear(), dat.getMonthValue(), dat.getDayOfMonth());
		}
	}

	@Nullable
	public static LocalDate newLocalDate(long totalDays)
	{
		if (totalDays == -1234567)
		{
			return null;
		}
		else
		{
			DateValue d = new DateValue();
			totalDays2DateValue(totalDays, d);
			return LocalDate.of(d.year, d.month, d.day);
		}
	}

	public static boolean isYearLeap(int year)
	{
		return ((year & 3) == 0) && ((year % 100) != 0 || (year % 400) == 0);
	}
	
	public static long date2TotalDays(int year, int month, int day)
	{
		int totalDays;
		int leapDays;
		int yearDiff;
		int yearDiff100;
		int yearDiff400;
	
		int currYear = year;
		int currMonth = month;
		int currDay = day;
	
		if (currYear <= 2000)
		{
			yearDiff = 2000 - currYear;
		}
		else
		{
			yearDiff = currYear - 2000 - 1;
		}
		yearDiff100 = yearDiff / 100;
		yearDiff400 = yearDiff100 >> 2;
		yearDiff >>= 2;
		leapDays = yearDiff - yearDiff100 + yearDiff400;
	
		if (currYear <= 2000)
		{
			totalDays = 10957 - (2000 - currYear) * 365 - leapDays;
		}
		else
		{
			totalDays = 10958 + (currYear - 2000) * 365 + leapDays;
		}
	
		switch (currMonth)
		{
		case 12:
			totalDays += 30;
		case 11:
			totalDays += 31;
		case 10:
			totalDays += 30;
		case 9:
			totalDays += 31;
		case 8:
			totalDays += 31;
		case 7:
			totalDays += 30;
		case 6:
			totalDays += 31;
		case 5:
			totalDays += 30;
		case 4:
			totalDays += 31;
		case 3:
			if (isYearLeap(year))
				totalDays += 29;
			else
				totalDays += 28;
		case 2:
			totalDays += 31;
		case 1:
			break;
		default:
			break;
		}
		totalDays += currDay - 1;
		return totalDays;
	}

	public static void totalDays2DateValue(long totalDays, @Nonnull DateValue d)
	{
		if (totalDays < 0)
		{
			d.year = 1970;
			while (totalDays < 0)
			{
				d.year--;
				if (isYearLeap(d.year))
				{
					totalDays += 366;
				}
				else
				{
					totalDays += 365;
				}
			}
		}
		else
		{
			if (totalDays < 10957)
			{
				d.year = 1970;
				while (true)
				{
					if (isYearLeap(d.year))
					{
						if (totalDays < 366)
						{
							break;
						}
						else
						{
							d.year++;
							totalDays -= 366;
						}
					}
					else
					{
						if (totalDays < 365)
						{
							break;
						}
						else
						{
							d.year++;
							totalDays -= 365;
						}
					}
				}
			}
			else
			{
				totalDays -= 10957;
				d.year = (int)(2000 + ((totalDays / 1461) << 2));
				totalDays = totalDays % 1461;
				if (totalDays >= 366)
				{
					totalDays--;
					d.year = (int)(d.year + totalDays / 365);
					totalDays = totalDays % 365;
				}
			}
		}
	
		if (isYearLeap(d.year))
		{
			if (totalDays < 121)
			{
				if (totalDays < 60)
				{
					if (totalDays < 31)
					{
						d.month = 1;
						d.day = (byte)(totalDays + 1);
					}
					else
					{
						d.month = 2;
						d.day = (byte)(totalDays - 31 + 1);
					}
				}
				else
				{
					if (totalDays < 91)
					{
						d.month = 3;
						d.day = (byte)(totalDays - 60 + 1);
					}
					else
					{
						d.month = 4;
						d.day = (byte)(totalDays - 91 + 1);
					}
				}
			}
			else
			{
				if (totalDays < 244)
				{
					if (totalDays < 182)
					{
						if (totalDays < 152)
						{
							d.month = 5;
							d.day = (byte)(totalDays - 121 + 1);
						}
						else
						{
							d.month = 6;
							d.day = (byte)(totalDays - 152 + 1);
						}
					}
					else
					{
						if (totalDays < 213)
						{
							d.month = 7;
							d.day = (byte)(totalDays - 182 + 1);
						}
						else
						{
							d.month = 8;
							d.day = (byte)(totalDays - 213 + 1);
						}
					}
				}
				else
				{
					if (totalDays < 305)
					{
						if (totalDays < 274)
						{
							d.month = 9;
							d.day = (byte)(totalDays - 244 + 1);
						}
						else
						{
							d.month = 10;
							d.day = (byte)(totalDays - 274 + 1);
						}
					}
					else
					{
						if (totalDays < 335)
						{
							d.month = 11;
							d.day = (byte)(totalDays - 305 + 1);
						}
						else
						{
							d.month = 12;
							d.day = (byte)(totalDays - 335 + 1);
						}
					}
				}
			}
		}
		else
		{
			if (totalDays < 120)
			{
				if (totalDays < 59)
				{
					if (totalDays < 31)
					{
						d.month = 1;
						d.day = (byte)(totalDays + 1);
					}
					else
					{
						d.month = 2;
						d.day = (byte)(totalDays - 31 + 1);
					}
				}
				else
				{
					if (totalDays < 90)
					{
						d.month = 3;
						d.day = (byte)(totalDays - 59 + 1);
					}
					else
					{
						d.month = 4;
						d.day = (byte)(totalDays - 90 + 1);
					}
				}
			}
			else
			{
				if (totalDays < 243)
				{
					if (totalDays < 181)
					{
						if (totalDays < 151)
						{
							d.month = 5;
							d.day = (byte)(totalDays - 120 + 1);
						}
						else
						{
							d.month = 6;
							d.day = (byte)(totalDays - 151 + 1);
						}
					}
					else
					{
						if (totalDays < 212)
						{
							d.month = 7;
							d.day = (byte)(totalDays - 181 + 1);
						}
						else
						{
							d.month = 8;
							d.day = (byte)(totalDays - 212 + 1);
						}
					}
				}
				else
				{
					if (totalDays < 304)
					{
						if (totalDays < 273)
						{
							d.month = 9;
							d.day = (byte)(totalDays - 243 + 1);
						}
						else
						{
							d.month = 10;
							d.day = (byte)(totalDays - 273 + 1);
						}
					}
					else
					{
						if (totalDays < 334)
						{
							d.month = 11;
							d.day = (byte)(totalDays - 304 + 1);
						}
						else
						{
							d.month = 12;
							d.day = (byte)(totalDays - 334 + 1);
						}
					}
				}
			}
		}
	}
}
