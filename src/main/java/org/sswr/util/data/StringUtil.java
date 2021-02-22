package org.sswr.util.data;

import java.sql.Timestamp;
import java.time.LocalDateTime;

public class StringUtil
{
	private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

	public static boolean isNumeric(String s)
	{
		if (s == null || s.length() == 0)
		{
			return false;
		}
		char carr[] = s.toCharArray();
		int i = 0;
		int j = carr.length;
		while (i < j)
		{
			if (!Character.isDigit(carr[i++]))
				return false;
		}
		return true;
	}

	public static Timestamp toTimestamp(String s)
	{
		if (s.length() == 8)
		{
			return Timestamp.valueOf(LocalDateTime.of(Integer.parseInt(s.substring(0, 4)), Integer.parseInt(s.substring(4, 6)), Integer.parseInt(s.substring(6, 8)), 0, 0));
		}
		else if (s.length() == 12)
		{
			return Timestamp.valueOf(LocalDateTime.of(Integer.parseInt(s.substring(0, 4)), Integer.parseInt(s.substring(4, 6)), Integer.parseInt(s.substring(6, 8)), Integer.parseInt(s.substring(8, 10)), Integer.parseInt(s.substring(10, 12))));
		}
		else if (s.length() == 14)
		{
			return Timestamp.valueOf(LocalDateTime.of(Integer.parseInt(s.substring(0, 4)), Integer.parseInt(s.substring(4, 6)), Integer.parseInt(s.substring(6, 8)), Integer.parseInt(s.substring(8, 10)), Integer.parseInt(s.substring(10, 12)), Integer.parseInt(s.substring(12, 14))));
		}
		else
		{
			throw new IllegalArgumentException();
		}
	}

	public static String toHex(byte b)
	{
		int v = b & 0xff;
		return new String(new char[]{HEX_ARRAY[v >> 4], HEX_ARRAY[v & 15]});
	}
}
