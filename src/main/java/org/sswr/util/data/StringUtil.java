package org.sswr.util.data;

import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.sswr.util.media.Size2DInt;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class StringUtil
{
	private static final boolean VERBOSE = false;
	public static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
	public static final char[] hex_array = "0123456789abcdef".toCharArray();

	/**
	* Check whether the string is numeric
	*
	* @param  s  the string to check
	* @return    true if the string is not null/empty and contains digits only
	*/
	public static boolean isNumeric(@Nullable String s)
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

	/**
	* Check whether the string contains non-ASCII characters
	*
	* @param  s  the string to check
	* @return    true if the string is not null/empty and contains non-ASCII characters
	*/
	public static boolean isNonASCII(@Nullable String s)
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
			if (carr[i++] >= 0x80)
				return true;
		}
		return false;
	}

	/**
	* Check whether the byte array are all ascii text
	*
	* @param  buff  the byte array to check
	* @param  ofst  the offset of byte array
	* @param  len  the length of byte array
	* @return    true if the byte array are all ascii characters
	*/
	public static boolean isASCIIText(@Nonnull byte[] buff, int ofst, int len)
	{
		byte b;
		while (len-- > 0)
		{
			b = buff[ofst++];
			if ((b >= 0x20 && b < 0x7F) || b == 13 || b == 10)
			{
	
			}
			else
			{
				return false;
			}
		}
		return true;
	}

	/**
	* Check whether the string is in email address form
	*
	* @param  s  the string to check
	* @return    true if the string is in email address form
	*/
	public static boolean isEmailAddress(@Nonnull String s)
	{
		int atPos = -1;
		boolean dotFound = false;
		char[] carr = s.toCharArray();
		int startPtr = 0;
		int endPtr = carr.length;
		char c;
		while (startPtr < endPtr)
		{
			c = carr[startPtr++];
			if (Character.isLetterOrDigit(c) || "!#$%&'*+-/=?^_`{|}~".indexOf(c) >= 0)
			{
	
			}
			else if (c == '.')
			{
				if (atPos != -1)
				{
					dotFound = true;
				}
			}
			else if (c == '@')
			{
				if (atPos != -1)
				{
					return false;
				}
				atPos = startPtr - 1;
				dotFound = false;
	
			}
			else
			{
				return false;
			}
		}
		if (atPos == -1 || atPos == 0 || !dotFound)
		{
			return false;
		}
		return true;
	}

	/**
	* Check whether the string is null or empty
	*
	* @param  s  the string to check
	* @return    true if the string is null or empty
	*/
	public static boolean isNullOrEmpty(@Nullable String s)
	{
		return s == null || s.length() == 0;
	}

	/**
	* Check whether the string has any characters
	*
	* @param  s  the string to check
	* @return    true if the string has at least 1 characters
	*/
	public static boolean hasChars(@Nullable String s)
	{
		return s != null && s.length() > 0;
	}

	/**
	* Check whether the string array can convert to int array
	*
	* @param  sarr  array of string to check
	* @return    true if the string can convert to int array with at least 1 element
	*/
	public static boolean canParseIntArr(@Nullable String sarr[])
	{
		if (sarr == null)
			return false;
		int i = sarr.length;
		if (i <= 0)
			return false;
		while (i-- > 0)
		{
			if (toInteger(sarr[i]) == null)
				return false;
		}
		return true;
	}

	/**
	* Pad string with padChar if it is shorter than minLeng
	*
	* @param  s  original string
	* @param  minLeng minimum length of the string
	* @param  padChar the char to pad
	* @return      padded string
	*/
	@Nonnull
	public static String leftPad(@Nonnull String s, int minLeng, char padChar)
	{
		int l = s.length();
		if (l >= minLeng)
		{
			return s;
		}
		StringBuilder sb = new StringBuilder(minLeng);
		l = minLeng - l;
		while (l-- > 0)
		{
			sb.append(padChar);
		}
		sb.append(s);
		return sb.toString();
	}

	/**
	* Pad integer to string with '0'
	*
	* @param  val  integer value
	* @param  minDigits minimum number of digits of the string
	* @return      padded string
	*/
	@Nonnull
	public static String intZPad(int val, int minDigits)
	{
		if (val < 0)
		{
			return "-"+leftPad((-val)+"", minDigits, '0');
		}
		else
		{
			return leftPad(val+"", minDigits, '0');
		}
	}

	/**
	* Join strings into string
	*
	* @param  strs  list of string
	* @param  seperator seperator of strings
	* @return      joined string or null if objs is null
	*/
	@Nonnull
	public static String join(@Nonnull Iterable<String> strs, @Nonnull String seperator)
	{
		StringBuilder sb = new StringBuilder();
		Iterator<String> it = strs.iterator();
		if (it.hasNext())
		{
			sb.append(it.next());
		}
		while (it.hasNext())
		{
			sb.append(seperator);
			sb.append(it.next());
		}
		return sb.toString();
	}

	/**
	* Join string array into string
	*
	* @param  strs  array of string
	* @param  seperator seperator of strings
	* @return      joined string or null if strs is null
	*/
	@Nonnull
	public static String join(@Nonnull String strs[], @Nonnull String seperator)
	{
		StringBuilder sb = new StringBuilder();
		int i = 1;
		int j = strs.length;
		if (j > 0)
		{
			sb.append(strs[0]);
			while (i < j)
			{
				sb.append(seperator);
				sb.append(strs[i]);
				i++;
			}
		}
		return sb.toString();
	}

	/**
	* Join objects into string
	*
	* @param  objs  list of object
	* @param  seperator seperator of strings
	* @return      joined string or null if objs is null
	*/
	@Nonnull
	public static <T extends Object> String joinObjs(@Nonnull Iterable<T> objs, @Nonnull String seperator)
	{
		StringBuilder sb = new StringBuilder();
		Iterator<T> it = objs.iterator();
		if (it.hasNext())
		{
			sb.append(it.next().toString());
		}
		while (it.hasNext())
		{
			sb.append(seperator);
			sb.append(it.next().toString());
		}
		return sb.toString();
	}

	/**
	* Convert Hexadecimal String to Integer
	*
	* @param  s  Hexadecimal String to convert
	* @return    Integer value, null if error
	*/
	@Nullable
	public static Integer hex2Int(@Nonnull String s)
	{
		if (s.length() > 8)
		{
			return null;
		}
		char carr[] = s.toCharArray();
		int i = 0;
		int j = carr.length;
		int v = 0;
		while (i < j)
		{
			if (carr[i] >= '0' && carr[i] <= '9')
			{
				v = (v << 4) | (carr[i] - 0x30);
			}
			else if (carr[i] >= 'A' && carr[i] <= 'Z')
			{
				v = (v << 4) | (carr[i] - 0x37);
			}
			else if (carr[i] >= 'a' && carr[i] <= 'z')
			{
				v = (v << 4) | (carr[i] - 0x57);
			}
			i++;
		}
		return v;
	}

	/**
	* Convert Hexadecimal String to Byte array
	*
	* @param  s  Hexadecimal String to convert
	* @return    byte array value
	*/
	@Nonnull
	public static byte[] hex2Bytes(@Nonnull String s)
	{
		char carr[] = s.toCharArray();
		byte retArr[] = new byte[carr.length >> 1];
		int i = 0;
		int j = carr.length;
		int v = 0;
		int k = 0;
		boolean found = false;
		while (i < j)
		{
			if (carr[i] >= '0' && carr[i] <= '9')
			{
				if (found)
				{
					v = (v << 4) | (carr[i] - 0x30);
					retArr[k++] = (byte)v;
					found = false;
				}
				else
				{
					v = carr[i] - 0x30;
					found = true;
				}
			}
			else if (carr[i] >= 'A' && carr[i] <= 'Z')
			{
				if (found)
				{
					v = (v << 4) | (carr[i] - 0x37);
					retArr[k++] = (byte)v;
					found = false;
				}
				else
				{
					v = carr[i] - 0x37;
					found = true;
				}
			}
			else if (carr[i] >= 'a' && carr[i] <= 'z')
			{
				if (found)
				{
					v = (v << 4) | (carr[i] - 0x57);
					retArr[k++] = (byte)v;
					found = false;
				}
				else
				{
					v = carr[i] - 0x57;
					found = true;
				}
			}
			i++;
		}
		if (k == retArr.length)
		{
			return retArr;
		}
		else
		{
			return Arrays.copyOf(retArr, k);
		}
	}

	/**
	* Convert Hexadecimal String to Long
	*
	* @param  s  Hexadecimal String to convert
	* @return    Long value, null if error
	*/
	@Nullable
	public static Long hex2Long(@Nonnull String s)
	{
		if (s.length() > 16)
		{
			return null;
		}
		char carr[] = s.toCharArray();
		int i = 0;
		int j = carr.length;
		long v = 0;
		while (i < j)
		{
			if (carr[i] >= '0' && carr[i] <= '9')
			{
				v = (v << 4) | (carr[i] - 0x30);
			}
			else if (carr[i] >= 'A' && carr[i] <= 'Z')
			{
				v = (v << 4) | (carr[i] - 0x37);
			}
			else if (carr[i] >= 'a' && carr[i] <= 'z')
			{
				v = (v << 4) | (carr[i] - 0x57);
			}
			i++;
		}
		return v;
	}

	/**
	* Convert byte to Hexadecimal String
	*
	* @param  b  byte to convert
	* @return    Upper case Hexadecimal String, must be 2 char long
	*/
	@Nonnull
	public static String toHex(byte b)
	{
		int v = b & 0xff;
		return new String(new char[]{HEX_ARRAY[v >> 4], HEX_ARRAY[v & 15]});
	}

	/**
	* Convert Int16 to Hexadecimal String
	*
	* @param  v  int16 to convert
	* @return    Upper case Hexadecimal String, must be 4 char long
	*/
	@Nonnull
	public static String toHex16(int v)
	{
		int v1 = (v >> 8) & 0xff;
		int v2 = v & 0xff;
		return new String(new char[]{HEX_ARRAY[v1 >> 4], HEX_ARRAY[v1 & 15], HEX_ARRAY[v2 >> 4], HEX_ARRAY[v2 & 15]});
	}

	/**
	* Convert Int32 to Hexadecimal String
	*
	* @param  v  int32 to convert
	* @return    Upper case Hexadecimal String, must be 8 char long
	*/
	@Nonnull
	public static String toHex32(int v)
	{
		int v1 = (v >> 24) & 0xff;
		int v2 = (v >> 16) & 0xff;
		int v3 = (v >> 8) & 0xff;
		int v4 = v & 0xff;
		return new String(new char[]{
			HEX_ARRAY[v1 >> 4], HEX_ARRAY[v1 & 15],
			HEX_ARRAY[v2 >> 4], HEX_ARRAY[v2 & 15],
			HEX_ARRAY[v3 >> 4], HEX_ARRAY[v3 & 15],
			HEX_ARRAY[v4 >> 4], HEX_ARRAY[v4 & 15]
		});
	}

	/**
	* Convert Int64 to Hexadecimal String
	*
	* @param  v  int64 to convert
	* @return    Upper case Hexadecimal String, must be 16 char long
	*/
	@Nonnull
	public static String toHex64(long v)
	{
		int v1 = (int)(v >> 56) & 0xff;
		int v2 = (int)(v >> 48) & 0xff;
		int v3 = (int)(v >> 40) & 0xff;
		int v4 = (int)(v >> 32) & 0xff;
		int v5 = (int)(v >> 24) & 0xff;
		int v6 = (int)(v >> 16) & 0xff;
		int v7 = (int)(v >> 8) & 0xff;
		int v8 = (int)(v) & 0xff;
		return new String(new char[]{
			HEX_ARRAY[v1 >> 4], HEX_ARRAY[v1 & 15],
			HEX_ARRAY[v2 >> 4], HEX_ARRAY[v2 & 15],
			HEX_ARRAY[v3 >> 4], HEX_ARRAY[v3 & 15],
			HEX_ARRAY[v4 >> 4], HEX_ARRAY[v4 & 15],
			HEX_ARRAY[v5 >> 4], HEX_ARRAY[v5 & 15],
			HEX_ARRAY[v6 >> 4], HEX_ARRAY[v6 & 15],
			HEX_ARRAY[v7 >> 4], HEX_ARRAY[v7 & 15],
			HEX_ARRAY[v8 >> 4], HEX_ARRAY[v8 & 15]
		});
	}

	/**
	* Convert byte array to hexadecimal String
	*
	* @param  buff  byte array to convert
	* @return    Upper case Hexadecimal String, must be 2 * buff.length characters long
	*/
	@Nonnull
	public static String toHex(@Nonnull byte buff[])
	{
		int i = 0;
		int j = buff.length;
		int v;
		char carr[] = new char[j << 1];
		while (i < j)
		{
			v = buff[i] & 0xff;
			carr[(i << 1)] = HEX_ARRAY[v >> 4];
			carr[(i << 1) + 1] = HEX_ARRAY[v & 15];
			i++;
		}
		return new String(carr);
	}

	/**
	* Convert byte array to hexadecimal String
	*
	* @param  buff  byte array to convert
	* @param  index  start index of the data
	* @param  len  length of the data
	* @return    Upper case Hexadecimal String, must be 2 * len characters long
	*/
	@Nonnull
	public static String toHex(@Nonnull byte buff[], int index, int len)
	{
		int i = index;
		int j = index + len;
		int k = 0;
		int v;
		char carr[] = new char[len << 1];
		while (i < j)
		{
			v = buff[i] & 0xff;
			carr[k] = HEX_ARRAY[v >> 4];
			carr[k + 1] = HEX_ARRAY[v & 15];
			i++;
			k += 2;
		}
		return new String(carr);
	}

	/**
	* Convert byte array to hexadecimal String
	*
	* @param  buff  byte array to convert
	* @param  index  start index of the data
	* @param  len  length of the data
	* @param  seperator  char for seperating bytes
	* @return    Upper case Hexadecimal String, must be 3 * len - 1 characters long
	*/
	@Nonnull
	public static String toHex(@Nonnull byte buff[], int index, int len, char seperator)
	{
		int i = index;
		int j = index + len;
		int k = 0;
		int v;
		char carr[] = new char[len * 3 - 1];
		while (i < j)
		{
			if (i > 0)
			{
				carr[k - 1] = seperator;
			}
			v = buff[i] & 0xff;
			carr[k] = HEX_ARRAY[v >> 4];
			carr[k + 1] = HEX_ARRAY[v & 15];
			i++;
			k += 3;
		}
		return new String(carr);
	}

	/**
	* Convert byte array to hexadecimal String
	*
	* @param  sb  StringBuilder to build the output string
	* @param  buff  byte array to convert
	* @param  ch  Char to seperate for every byte
	* @param  lbt  LineBreakType to seperate for every 16 bytes
	*/
	public static void appendHex(@Nonnull StringBuilder sb, @Nonnull byte buff[], char ch, @Nonnull LineBreakType lbt)
	{
		appendHex(sb, buff, 0, buff.length, ch, lbt);
	}

	/**
	* Convert byte array to hexadecimal String
	*
	* @param  sb  StringBuilder to build the output string
	* @param  buff  byte array to convert
	* @param  ofst  offset of byte array to convert
	* @param  count  number of bytes to convert
	* @param  ch  Char to seperate for every byte
	* @param  lbt  LineBreakType to seperate for every 16 bytes
	*/
	public static void appendHex(@Nonnull StringBuilder sb, @Nonnull byte buff[], int ofst, int count, char ch, @Nonnull LineBreakType lbt)
	{
		int i = 0;
		int j = count;
		int v;
		if (ch != 0)
		{
			while (i < j)
			{
				v = buff[i + ofst] & 0xff;
				sb.append(HEX_ARRAY[v >> 4]);
				sb.append(HEX_ARRAY[v & 15]);
				sb.append(ch);
				i++;
				if ((i & 15) == 0)
				{
					appendLineBreak(sb, lbt);
				}
			}
		}
		else
		{
			while (i < j)
			{
				v = buff[i + ofst] & 0xff;
				sb.append(HEX_ARRAY[v >> 4]);
				sb.append(HEX_ARRAY[v & 15]);
				i++;
				if ((i & 15) == 0)
				{
					appendLineBreak(sb, lbt);
				}
			}
		}
	}

	/**
	* Append Line Break to StringBuilder
	*
	* @param  sb  StringBuilder to append
	* @param  lbt  LineBreakType to append
	*/
	public static void appendLineBreak(@Nonnull StringBuilder sb, @Nonnull LineBreakType lbt)
	{
		switch (lbt)
		{
		case NONE:
			break;
		case CR:
			sb.append('\r');
			break;
		case LF:
			sb.append('\n');
			break;
		case CRLF:
			sb.append("\r\n");
			break;
		}
	}

	/**
	* Parse String to Timestamp
	*
	* @param  s  String to parse, format must be either:
	*            yyyyMMdd, yyyyMMddHHmm, yyyyMMddHHmmss
	* @return    null if it is not valid Integer
	* @exception IllegalArgumentException if s is not in valid format
	*/
	@Nonnull
	public static Timestamp toTimestamp(@Nonnull String s)
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

	/**
	* Parse String to Integer
	*
	* @param  s  String to parse
	* @return     null if it is not valid Integer
	*/
	@Nullable
	public static Integer toInteger(@Nullable String s)
	{
		try
		{
			if (s == null || s.length() == 0)
			{
				return null;
			}
			if (s.startsWith("0x"))
			{
				return hex2Int(s.substring(2));
			}
			return Integer.parseInt(s);
		}
		catch (NumberFormatException ex)
		{
			return null;
		}
	}

	/**
	* Parse String to Integer with fail value
	*
	* @param  s  String to parse
	* @param  failVal  value to assign on fail
	* @return     failVal if it is not valid Integer
	*/
	public static int toIntegerS(@Nonnull String s, int failVal)
	{
		Integer iVal = toInteger(s);
		if (iVal == null)
			return failVal;
		return iVal;
	}

	/**
	* Parse String to Long
	*
	* @param  s  String to parse
	* @return     null if it is not valid Long
	*/
	@Nullable
	public static Long toLong(@Nullable String s)
	{
		try
		{
			if (s == null || s.length() == 0)
			{
				return null;
			}
			if (s.startsWith("0x"))
			{
				return hex2Long(s.substring(2));
			}
			return Long.parseLong(s);
		}
		catch (NumberFormatException ex)
		{
			return null;
		}
	}

	/**
	* Parse String to Long with fail value
	*
	* @param  s  String to parse
	* @param  failVal  value to assign on fail
	* @return     null if it is not valid Long
	*/
	public static long toLongS(@Nonnull String s, long failVal)
	{
		Long lVal = toLong(s);
		if (lVal == null)
			return failVal;
		return lVal;
	}

	/**
	* Parse String to Double
	*
	* @param  s  String to parse
	* @return     null if it is not valid Double
	*/
	@Nullable
	public static Double toDouble(@Nonnull String s)
	{
		try
		{
			if (isNullOrEmpty(s))
			{
				return null;
			}
			return Double.parseDouble(s);
		}
		catch (NumberFormatException ex)
		{
			return null;
		}
	}

	/**
	* Parse String to Double with fail value
	*
	* @param  s  String to parse
	* @param failVal value to assign on fail
	* @return     null if it is not valid Double
	*/
	public static double toDoubleS(@Nonnull String s, double failVal)
	{
		Double dVal = toDouble(s);
		if (dVal == null)
			return failVal;
		else
			return dVal;
	}

	/**
	* Parse String into Set of Integer
	*
	* @param  s  String to parse
	* @param  seperator Seperator of the string
	* @return     null if it is input is not valid
	*/
	@Nullable
	public static Set<Integer> toIntSet(@Nonnull String s, @Nonnull String seperator)
	{
		return toIntSet(StringUtil.split(s, seperator));
	}

	/**
	* Parse String array into Set of Integer
	*
	* @param  sarr  Array of String to parse
	* @return     null if it is input is not valid
	*/
	@Nullable
	public static Set<Integer> toIntSet(@Nonnull String sarr[])
	{
		Set<Integer> retSet = new HashSet<Integer>();
		int i = sarr.length;
		Integer val;
		while (i-- > 0)
		{
			val = toInteger(sarr[i]);
			if (val == null)
				return null;
			retSet.add(val);
		}
		return retSet;
	}

	/**
	* Convert enum into name string
	*
	* @param  e  Enum to convert
	* @return     null if it is input null
	*/
	@Nullable
	public static <T extends Enum<T>> String getEnumName(@Nullable T e)
	{
		if (e == null)
		{
			return null;
		}
		return e.name();
	}

	/**
	* Convert double into string
	*
	* @param  v  double value to convert
	* @return     string presentation of the value
	*/
	@Nonnull
	public static String fromDouble(double v)
	{
		if (v == 0)
		{
			return "0";
		}
		else if (Double.isNaN(v))
		{
			return "1.#QNAN0";
		}
		StringBuilder sb = new StringBuilder();
		if (v < 0)
		{
			sb.append('-');
			v = -v;
		}
		if (Double.isInfinite(v))
		{
			sb.append("1.#INF00");
			return sb.toString();
		}
		int i = 14;
		int ex = -10000 + (int)(Math.log10(v) + 10000);
		int iVal;
		v = v * Math.pow(10.0, -ex - 1) + 5.0e-15;
		if (ex >= 16 || ex <= -4)
		{
			v = v * 10.0;
			iVal = (int)v;
			v = v - iVal;
			sb.append((char)(iVal + 48));
			sb.append('.');
			v = v * 10.0;
			iVal = (int)v;
			v = v - iVal;
			sb.append((char)(iVal + 48));
			i--;
	
			if (v > 1.0e-10)
			{
				while (v > 1.0e-10)
				{
					v = v * 10.0;
					iVal = (int)v;
					v = v - iVal;
					sb.append((char)(iVal + 48));
					if (--i <= 0)
						break;
				}
				if (sb.charAt(sb.length() - 1) == '0')
				{
					sb.deleteCharAt(sb.length() - 1);
				}
			}
			sb.append('e');
			if (ex < 0)
			{
				sb.append('-');
				ex = -ex;
			}
			else
			{
				sb.append('+');
			}
			sb.append(""+ex);
		}
		else if (ex < 0)
		{
			sb.append('0');
			sb.append('.');
			while (++ex < 0)
			{
				sb.append('0');
			}
			while (v > 1.0e-10)
			{
				v = v * 10.0;
				iVal = (int)v;
				v = v - iVal;
				sb.append((char)(iVal + 48));
				if (--i <= 0)
					break;
			}
			while (sb.charAt(sb.length() - 1) == '0')
			{
				sb.deleteCharAt(sb.length() - 1);
			}
		}
		else
		{
			while (ex >= 1)
			{
				v = v * 10.0;
				iVal = (int)v;
				v = v - iVal;
				sb.append((char)(iVal + 48));
				i--;
				ex -= 1;
			}
			v = v * 10.0;
			iVal = (int)v;
			v = v - iVal;
			sb.append((char)(iVal + 48));
			if (v > 1.0e-10)
			{
				sb.append('.');
				v = v * 10.0;
				iVal = (int)v;
				v = v - iVal;
				sb.append((char)(iVal + 48));
			}
			i--;
			if (v > 1.0e-10)
			{
				while (v > 1.0e-10)
				{
					v = v * 100.0;
					iVal = (int)v;
					v = v - iVal;
					sb.append((char)(iVal + 48));
					if (--i <= 0)
						break;
				}
				while (sb.charAt(sb.length() - 1) == '0')
				{
					sb.deleteCharAt(sb.length() - 1);
				}
				if (sb.charAt(sb.length() - 1) == '.')
				{
					sb.deleteCharAt(sb.length() - 1);
				}
			}
		}
		return sb.toString();
	}

	/**
	* Round double value to 8 sig fig
	*
	* @param  v  double value to round
	* @return     rounded value
	*/
	public static double fixDouble(double v)
	{
		if (v == 0)
		{
			return v;
		}
		else if (Double.isNaN(v))
		{
			return v;
		}
		else if (Double.isInfinite(v))
		{
			return v;
		}
		boolean neg;
		if (v < 0)
		{
			neg = true;
			v = -v;
		}
		else
		{
			neg = false;
		}
		int ex = -10000 + (int)(Math.log10(v) + 10000);
		double mulVal = Math.pow(10.0, -ex + 8);
		v = Math.round(v * mulVal + 5.0e-15);
		v = v / mulVal;
		if (neg)
		{
			v = -v;
		}
		return v;
	}

	/**
	* Concat char array with a string
	*
	* @param  buff  char array to concat
	* @param  ofst  offset of the array to concat
	* @param  s     string to be concated
	* @return     end offset of the string in buff
	*/
	public static int concat(@Nonnull char []buff, int ofst, @Nonnull String s)
	{
		char[] carr = s.toCharArray();
		int i = 0;
		int j = carr.length;
		while (i < j)
		{
			buff[ofst + i] = carr[i];
			i++;
		}
		return ofst + j;
	}

	/**
	* Concat utf8 array with a string
	*
	* @param  buff  utf8 array to concat
	* @param  ofst  offset of the array to concat
	* @param  s     string to be concated
	* @return     end offset of the string in buff
	*/
	public static int concat(@Nonnull byte []buff, int ofst, @Nonnull String s)
	{
		byte[] carr = s.getBytes(StandardCharsets.UTF_8);
		int i = 0;
		int j = carr.length;
		while (i < j)
		{
			buff[ofst + i] = carr[i];
			i++;
		}
		return ofst + j;
	}

	/**
	* Concat utf8 array with a string buffer
	*
	* @param  buff  utf8 array to concat
	* @param  ofst  offset of the array to concat
	* @param  sbuff   string buffer to be concated
	* @param  sbuffOfst  string buffer offset
	* @param  sbuffLen   string buffer length
	* @return     end offset of the string in buff
	*/
	public static int concat(@Nonnull byte []buff, int ofst, @Nonnull byte[] sbuff, int sbuffOfst, int sbuffLen)
	{
		int i = 0;
		int j = sbuffLen;
		while (i < j)
		{
			buff[ofst + i] = sbuff[i + sbuffOfst];
			i++;
		}
		return ofst + j;
	}

	/**
	* Concat utf8 array with a hex of buffer
	*
	* @param  buff  utf8 array to concat
	* @param  ofst  offset of the array to concat
	* @param  bytes   string buffer to be concated
	* @param  bytesOfst  string buffer offset
	* @param  bytesLen   string buffer length
	* @param  seperator  seperator between bytes
	* @return     end offset of the string in buff
	*/
	public static int concatHexBytes(@Nonnull byte []buff, int ofst, @Nonnull byte[] bytes, int bytesOfst, int bytesLen, byte seperator)
	{
		byte val;
		if (seperator == 0)
		{
			int i = 0;
			while (i < bytesLen)
			{
				val = bytes[bytesOfst + i];
				buff[ofst] = (byte)HEX_ARRAY[val >> 4];
				buff[ofst + 1] = (byte)HEX_ARRAY[val & 15];
				ofst += 2;
				i++;
			}
			buff[ofst] = 0;
		}
		else
		{
			int i = 0;
			while (i < bytesLen)
			{
				val = bytes[bytesOfst + i];
				buff[ofst] = (byte)HEX_ARRAY[val >> 4];
				buff[ofst + 1] = (byte)HEX_ARRAY[val & 15];
				buff[ofst + 2] = seperator;
				ofst += 3;
				i++;
			}
			buff[--ofst] = 0;
		}
		return ofst;
	}

	/**
	* Trim L and R of StringBuilder for white space 
	*
	* @param  sb  StringBuilder to trim
	*/
	public static void trim(@Nonnull StringBuilder sb)
	{
		trimRight(sb);
		trimLeft(sb);
	}

	/**
	* Trim Left of StringBuilder for white space 
	*
	* @param  sb  StringBuilder to trim
	*/
	public static void trimLeft(@Nonnull StringBuilder sb)
	{
		int len = sb.length();
		if (len <= 0)
		{
			return;
		}
		char c;
		c = sb.charAt(0);
		if (c == ' ' || c == '\t')
		{
			int i = 1;
			while (i < len)
			{
				c = sb.charAt(i);
				if (c != ' ' && c != '\t')
				{
					break;
				}

				i++;
			}
			sb.delete(0, i);
			len -= i;
		}
	}

	/**
	* Trim Right of StringBuilder for white space 
	*
	* @param  sb  StringBuilder to trim
	*/
	public static void trimRight(@Nonnull StringBuilder sb)
	{
		int len = sb.length();
		char c;
		while (len-- > 0)
		{
			c = sb.charAt(len);
			if (c != ' ' && c != '\t')
			{
				len++;
				break;
			}
		}
		if (len < 0)
		{
			len = 0;
		}
		sb.setLength(len);
	}

	/**
	 * Binary search of value from list
	 * 
	 * @param  list List of string
	 * @param  value Value to search
	 * @return  {@literal >}= 0 for exact match
	 *          {@literal <} 0 for not matching, ~return = position for sorted value to insert
	 */
	public static int sortedIndexOf(@Nonnull List<String> list, @Nonnull String value)
	{
		int i;
		int j;
		int k;
		int l;
		i = 0;
		j = list.size() - 1;
		while (i <= j)
		{
			k = (i + j) >> 1;
			l = list.get(k).compareTo(value);
			if (l > 0)
			{
				j = k - 1;
			}
			else if (l < 0)
			{
				i = k + 1;
			}
			else
			{
				return k;
			}
		}
		return -i - 1;
	}

	/**
	 * Insert a value into sorted list
	 * 
	 * @param  list List of string
	 * @param  value Value to insert
	 * @return  index of list has been inserted
	 */
	public static int sortedInsert(@Nonnull List<String> list, @Nonnull String value)
	{
		int i = sortedIndexOf(list, value);
		if (i >= 0)
		{
			list.add(i, value);
			return i;
		}
		else
		{
			list.add(~i, value);
			return ~i;
		}
	}

	/**
	 * Check whether the StringBuilder is starts with a string
	 * 
	 * @param  sb StringBuilder to check
	 * @param  value Value to check
	 * @return  whether it is starts with a string
	 */
	public static boolean startsWith(@Nonnull StringBuilder sb, @Nonnull String value)
	{
		return sb.length() >= value.length() && sb.substring(0, value.length()).equals(value);
	}

	/**
	 * Check whether the StringBuilder is ends with a string
	 * 
	 * @param  sb StringBuilder to check
	 * @param  value Value to check
	 * @return  whether it is ends with a string
	 */
	public static boolean endsWith(@Nonnull StringBuilder sb, @Nonnull String value)
	{
		if (sb.length() < value.length())
		{
			return false;
		}
		return sb.substring(sb.length() - value.length()).equals(value);
	}

	/**
	 * Check whether the StringBuilder is ends with a char
	 * 
	 * @param  sb StringBuilder to check
	 * @param  value Value to check
	 * @return  whether it is ends with a char
	 */
	public static boolean endsWith(@Nonnull StringBuilder sb, char value)
	{
		if (sb.length() <= 0)
		{
			return false;
		}
		return sb.charAt(sb.length() - 1) == value;
	}

	/**
	 * Check whether the String is ends with string (Case insensitive)
	 * 
	 * @param  str String to check
	 * @param  value Value to check
	 * @return  whether str is ends with value
	 */
	public static boolean endsWithICase(@Nonnull String str, @Nonnull String value)
	{
		int strLen = str.length();
		int valueLen = value.length();
		if (strLen < valueLen)
		{
			return false;
		}
		return str.substring(strLen - valueLen).equalsIgnoreCase(value);
	}

	/**
	 * Append chars to StringBuilder
	 * 
	 * @param  sb StringBuilder to append
	 * @param  c  Char to append
	 * @param  cnt Number of char to append
	 */
	public static void appendChar(@Nonnull StringBuilder sb, char c, int cnt)
	{
		while (cnt-- > 0)
		{
			sb.append(c);
		}
	}

	/**
	 * Convert byte array of UTF8 to String with zero ended
	 * 
	 * @param  buff     byte array of UTF8
	 * @param  buffOfst start offset of the UTF8 string
	 * @return  result string
	 */
	@Nonnull
	public static String fromUTF8Z(@Nonnull byte[] buff, int buffOfst)
	{
		int len = buff.length;
		int endOfst = buffOfst;
		while (endOfst < len)
		{
			if (buff[endOfst] == 0)
			{
				break;
			}
			endOfst++;
		}
		return new String(buff, buffOfst, endOfst - buffOfst, StandardCharsets.UTF_8);
	}

	/**
	 * Convert UTF-8 byte buffer to string
	 * 
	 * @param  buff byte buffer to convert
	 * @param  ofst offset of byte buffer start
	 * @return  converted string
	 */
	@Nonnull
	public static String byte2String(@Nonnull byte[] buff, int ofst)
	{
		int endOfst = ofst;
		int j = buff.length;
		while (endOfst < j)
		{
			if (buff[endOfst] == 0)
			{
				break;
			}
			endOfst++;
		}
		return new String(buff, ofst, endOfst - ofst, StandardCharsets.UTF_8);
	}

	/**
	 * Check whether the byte buffer is starts with a string
	 * 
	 * @param  buff byte buffer to check
	 * @param  ofst offset of byte buffer start
	 * @param  value Value to check
	 * @return  whether it is starts with a string
	 */
	public static boolean startsWith(@Nonnull byte[] buff, int ofst, @Nonnull String value)
	{
		byte[] valueBuff = value.getBytes(StandardCharsets.UTF_8);
		if (ofst + valueBuff.length > buff.length)
		{
			return false;
		}
		int i = 0;
		int j = valueBuff.length;
		while (i < j)
		{
			if (buff[ofst + i] != valueBuff[i])
			{
				return false;
			}
			i++;
		}
		return true;
	}

	/**
	 * Check whether the char buffer is starts with a string
	 * 
	 * @param  buff char buffer to check
	 * @param  ofst offset of char buffer start
	 * @param  len length of the buffer from ofst
	 * @param  value Value to check
	 * @return  whether it is starts with a string
	 */
	public static boolean startsWith(@Nonnull char[] buff, int ofst, int len, @Nonnull String value)
	{
		if (len < value.length() || ofst + len > buff.length)
			return false;
		return new String(buff, ofst, value.length()).equals(value);
	}

	/**
	 * Check whether the byte buffer is starts with a string
	 * 
	 * @param  buff byte buffer to check
	 * @param  ofst offset of byte buffer start
	 * @param  len length of byte buffer
	 * @param  value Value to check
	 * @return  whether it is starts with a string
	 */
	public static boolean startsWithC(@Nonnull byte[] buff, int ofst, int len, @Nonnull String value)
	{
		byte[] valueBuff = value.getBytes(StandardCharsets.UTF_8);
		if (valueBuff.length > len)
		{
			return false;
		}
		int i = 0;
		int j = valueBuff.length;
		while (i < j)
		{
			if (buff[ofst + i] != valueBuff[i])
			{
				return false;
			}
			i++;
		}
		return true;
	}

	/**
	 * Check whether the byte buffer is starts with a string ignoring case
	 * 
	 * @param  buff byte buffer to check
	 * @param  ofst offset of byte buffer start
	 * @param  len length of byte buffer
	 * @param  value Value to check
	 * @return  whether it is starts with a string
	 */
	public static boolean startsWithICaseC(@Nonnull byte[] buff, int ofst, int len, @Nonnull String value)
	{
		byte[] valueBuff = value.getBytes(StandardCharsets.UTF_8);
		if (valueBuff.length > len)
		{
			return false;
		}
		int i = 0;
		int j = valueBuff.length;
		byte b1;
		byte b2;
		while (i < j)
		{
			b1 = valueBuff[i];
			b2 = buff[ofst + i]; 
			if (b1 >= 'a' && b1 <= 'z')
			{
				b1 -= 0x20;
			}
			if (b2 >= 'a' && b2 <= 'z')
			{
				b2 -= 0x20;
			}
			if (b1 != b2)
			{
				return false;
			}
			i++;
		}
		return true;
	}

	/**
	 * Check whether the byte buffer is equals to a string ignore case
	 * 
	 * @param  buff byte buffer to check
	 * @param  ofst offset of byte buffer start
	 * @param  value Value to check
	 * @return  whether it is equals to a string
	 */
	public static boolean equalsICase(@Nonnull byte[] buff, int ofst, @Nonnull String value)
	{
		byte[] valueBuff = value.getBytes(StandardCharsets.UTF_8);
		if (ofst + valueBuff.length > buff.length)
		{
			return false;
		}
		if (ofst + valueBuff.length != buff.length && buff[ofst + valueBuff.length] != 0)
		{
			return false;
		}
		int i = 0;
		int j = valueBuff.length;
		while (i < j)
		{
			if (toUpper(buff[ofst + i]) != toUpper(valueBuff[i]))
			{
				return false;
			}
			i++;
		}
		return true;
	}

	/**
	 * Convert byte of utf-8 char to upper case
	 * 
	 * @param  c the char to convert
	 * @return  converted char
	 */
	public static byte toUpper(byte c)
	{
		if (c >= 'a' && c <= 'z')
		{
			return (byte)(c - 32);
		}
		else
		{
			return c;
		}
	}

	/**
	 * Convert bytes of utf-8 char to double
	 * 
	 * @param  buff utf-8 byte buffer of string to convert
	 * @param  ofst offset of the byte buffer to be start of the string
	 * @return  result double value or 0 if error occurs
	 */
	public static double toDouble(@Nonnull byte[] buff, int ofst)
	{
		Double d = toDouble(byte2String(buff, ofst));
		if (d == null)
		{
			return 0;
		}
		return d.doubleValue();
	}

	/**
	 * Split string into array
	 * 
	 * @param s the string to split
	 * @param seperator the seperator to split
	 * @return  splitted string array
	 */
	@Nonnull
	public static String[] split(@Nonnull String s, @Nonnull String seperator)
	{
		ArrayList<String> sarr = new ArrayList<String>();
		int k = 0;
		int i = 0;
		int j = s.length() - seperator.length() + 1;
		while (i < j)
		{
			if (s.regionMatches(i, seperator, 0, seperator.length()))
			{
				sarr.add(s.substring(k, i));
				i += seperator.length();
				k = i;
			}
			else
			{
				i++;
			}
		}
		sarr.add(s.substring(k, s.length()));
		String[] ret = new String[i = sarr.size()];
		while (i-- > 0)
		{
			ret[i] = sarr.get(i);
		}
		return ret;
	}

	/**
	 * Split string by line into array
	 * 
	 * @param s the string to split
	 * @return  splitted string array
	 */
	@Nonnull
	public static String[] splitLine(@Nonnull String s)
	{
		ArrayList<String> sarr = new ArrayList<String>();
		int k = 0;
		int i = 0;
		int j = s.length();
		char c;
		while (i < j)
		{
			c = s.charAt(i);
			if (c == '\r')
			{
				sarr.add(s.substring(k, i));
				i++;
				if (i < j && s.charAt(i) == '\n')
				{
					i++;
				}
				k = i;
			}
			else if (c == '\n')
			{
				sarr.add(s.substring(k, i));
				i++;
				k = i;
			}
			else
			{
				i++;
			}
		}
		sarr.add(s.substring(k, s.length()));
		String[] ret = new String[i = sarr.size()];
		while (i-- > 0)
		{
			ret[i] = sarr.get(i);
		}
		return ret;
	}

	/**
	 * Split string by line into array
	 * 
	 * @param s the string to split
	 * @param maxLines max Lines to split
	 * @return  splitted string array
	 */
	@Nonnull
	public static String[] splitLine(@Nonnull String s, int maxLines)
	{
		ArrayList<String> sarr = new ArrayList<String>();
		int k = 0;
		int i = 0;
		int j = s.length();
		char c;
		while (i < j)
		{
			c = s.charAt(i);
			if (c == '\r')
			{
				sarr.add(s.substring(k, i));
				i++;
				if (i < j && s.charAt(i) == '\n')
				{
					i++;
				}
				k = i;
				if (sarr.size() >= maxLines - 1)
					break;
			}
			else if (c == '\n')
			{
				sarr.add(s.substring(k, i));
				i++;
				k = i;
				if (sarr.size() >= maxLines - 1)
					break;
			}
			else
			{
				i++;
			}
		}
		sarr.add(s.substring(k, s.length()));
		String[] ret = new String[i = sarr.size()];
		while (i-- > 0)
		{
			ret[i] = sarr.get(i);
		}
		return ret;
	}

	/**
	 * Check whether string contains CJK characters
	 * 
	 * @param s the string to check
	 * @return  true if s contain CJK characters
	 */
	public static boolean hasCJKChar(@Nonnull String s)
	{
		int i = 0;
		int j = s.length();
		while (i < j)
		{
			if (CharUtil.isCJK(s.charAt(i)))
				return true;
			i++;
		}
		return false;
	}

	/**
	 * Check index of character inside a char array
	 * 
	 * @param carr the char array to check
	 * @param startIndex the first index to start searching
	 * @param c the character to search
	 * @return  -1 if character not found
	 * 			other = index of the first appear
	 */
	public static int indexOfChar(@Nonnull char[] carr, int startIndex, char c)
	{
		int j = carr.length;
		while (startIndex < j)
		{
			if (carr[startIndex] == c)
				return startIndex;
			startIndex++;
		}
		return -1;
	}

	/**
	 * Count number of char in UTF-16
	 * 
	 * @param s string to count
	 * @return 	number of UTF-16 char
	 * 			0 if s is null
	 */
	public static int utf16CharCnt(@Nullable String s)
	{
		if (s == null)
			return 0;
		return s.length();
	}


	/**
	 * Count number of char in UTF-32
	 * 
	 * @param s string to count
	 * @return 	number of UTF-16 char
	 * 			0 if s is null
	 */
	public static int utf32CharCnt(@Nullable String s)
	{
		if (s == null)
			return 0;
		int len = 0;
		int ofst = 0;
		int slen = s.length();
		char c;
		char c2;
		while (ofst < slen)
		{
			c = s.charAt(ofst);
			if (ofst + 1 < slen && c >= 0xd800 && c < 0xdc00 && (c2 = s.charAt(ofst + 1)) >= 0xdc00 && c2 < 0xe000)
			{
				ofst += 2;
			}
			else
			{
				ofst++;
			}
			len++;
		}
		return len;
	}

	/**
	 * Check whether the string is unsigned integer
	 * 
	 * @param s string to check
	 * @return true if the string is unsigned integer
	 */
	public static boolean isUInteger(@Nonnull String s)
	{
		int i = 0;
		int j = s.length();
		while (i < j)
		{
			if (!CharUtil.isDigit(s.charAt(i)))
				return false;
			i++;
		}
		return true;

	}

	/**
	 * Check whether the string is valid hkid
	 * 
	 * @param hkid the string to check
	 * @return true if the string is valid hkid
	 */
	public static boolean isHKID(@Nonnull String hkid)
	{
		String sbuff;
		char chk;
		int ichk;
		if (hkid.endsWith(")"))
		{
			if (hkid.length() == 10)
			{
				if (hkid.charAt(7) == '(')
				{
					sbuff = hkid.substring(0, 7);
					chk = hkid.charAt(8);
				}
				else
				{
					return false;
				}
			}
			else if (hkid.length() == 11)
			{
				if (hkid.charAt(8) == '(')
				{
					sbuff = hkid.substring(0, 8);
					chk = hkid.charAt(9);
				}
				else
				{
					return false;
				}
			}
			else
			{
				return false;
			}
		}
		else
		{
			if (hkid.length() == 8)
			{
				sbuff = hkid.substring(0, 7);
				chk = hkid.charAt(7);
			}
			else if (hkid.length() == 9)
			{
				sbuff = hkid.substring(0, 8);
				chk = hkid.charAt(8);
			}
			else
			{
				return false;
			}
		}

		if (CharUtil.isDigit(chk))
			ichk = (int)chk - 0x30;
		else if (chk == 'A')
			ichk = 10;
		else
			return false;

		int thisChk;
		if (sbuff.length() == 8)
		{
			if (!CharUtil.isUpperCase(sbuff.charAt(0)) ||
				!CharUtil.isUpperCase(sbuff.charAt(1)) ||
				!CharUtil.isDigit(sbuff.charAt(2)) ||
				!CharUtil.isDigit(sbuff.charAt(3)) ||
				!CharUtil.isDigit(sbuff.charAt(4)) ||
				!CharUtil.isDigit(sbuff.charAt(5)) ||
				!CharUtil.isDigit(sbuff.charAt(6)) ||
				!CharUtil.isDigit(sbuff.charAt(7)))
					return false;
			
			thisChk = 0;
			thisChk += (sbuff.charAt(0) - 'A' + 10) * 9;
			thisChk += (sbuff.charAt(1) - 'A' + 10) * 8;
			thisChk += (sbuff.charAt(2) - '0') * 7;
			thisChk += (sbuff.charAt(3) - '0') * 6;
			thisChk += (sbuff.charAt(4) - '0') * 5;
			thisChk += (sbuff.charAt(5) - '0') * 4;
			thisChk += (sbuff.charAt(6) - '0') * 3;
			thisChk += (sbuff.charAt(7) - '0') * 2;
			thisChk += ichk;
			if ((thisChk % 11) != 0)
				return false;
			return true;
		}
		else
		{
			if (!CharUtil.isUpperCase(sbuff.charAt(0)) ||
				!CharUtil.isDigit(sbuff.charAt(1)) ||
				!CharUtil.isDigit(sbuff.charAt(2)) ||
				!CharUtil.isDigit(sbuff.charAt(3)) ||
				!CharUtil.isDigit(sbuff.charAt(4)) ||
				!CharUtil.isDigit(sbuff.charAt(5)) ||
				!CharUtil.isDigit(sbuff.charAt(6)))
					return false;

			thisChk = 36 * 9;
			thisChk += (sbuff.charAt(0) - 'A' + 10) * 8;
			thisChk += (sbuff.charAt(1) - '0') * 7;
			thisChk += (sbuff.charAt(2) - '0') * 6;
			thisChk += (sbuff.charAt(3) - '0') * 5;
			thisChk += (sbuff.charAt(4) - '0') * 4;
			thisChk += (sbuff.charAt(5) - '0') * 3;
			thisChk += (sbuff.charAt(6) - '0') * 2;
			if (VERBOSE)
			{
				System.out.println(" : 36 * 9 = "+(36 * 9));
				System.out.println(sbuff.charAt(0)+": "+((int)sbuff.charAt(0) - 'A' + 10)+" * 8 = "+(((int)sbuff.charAt(0) - 'A' + 10) * 8));
				System.out.println(sbuff.charAt(1)+": "+((int)sbuff.charAt(1) - '0')+" * 7 = "+(((int)sbuff.charAt(1) - '0') * 7));
				System.out.println(sbuff.charAt(2)+": "+((int)sbuff.charAt(2) - '0')+" * 6 = "+(((int)sbuff.charAt(2) - '0') * 6));
				System.out.println(sbuff.charAt(3)+": "+((int)sbuff.charAt(3) - '0')+" * 5 = "+(((int)sbuff.charAt(3) - '0') * 5));
				System.out.println(sbuff.charAt(4)+": "+((int)sbuff.charAt(4) - '0')+" * 4 = "+(((int)sbuff.charAt(4) - '0') * 4));
				System.out.println(sbuff.charAt(5)+": "+((int)sbuff.charAt(5) - '0')+" * 3 = "+(((int)sbuff.charAt(5) - '0') * 3));
				System.out.println(sbuff.charAt(6)+": "+((int)sbuff.charAt(6) - '0')+" * 2 = "+(((int)sbuff.charAt(6) - '0') * 2));
				System.out.println("Total = "+thisChk+", Mod = "+(thisChk % 11)+", Check = "+ichk);
			}
			thisChk += ichk;
			if ((thisChk % 11) != 0)
				return false;
			return true;
		}
	}

	/**
	 * Output UTF32 char to StringBuilder
	 * 
	 * @param sb StringBuilder to output
	 * @param c UTF-32 char code
	 */
	public static void appendUTF32Char(@Nonnull StringBuilder sb, int c)
	{
		if (c < 0x10000)
		{
			sb.append((char)c);
		}
		else
		{
			char carr[] = new char[2];
			carr[0] = (char)(0xd800 + ((c - 0x10000) >> 10));
			carr[1] = (char)((c & 0x3ff) + 0xdc00);
			String s = new String(carr);
			sb.append(s);
			//sb.append("0x"+StringUtil.toHex16(0xd800 + (c >> 10)));
			//sb.append("0x"+StringUtil.toHex16((c & 0x3ff) + 0xdc00));
		}
	}

	/**
	 * Output non-null string
	 * 
	 * @param s string or null string
	 * @return non-null string
	 */
	@Nonnull
	public static String orEmpty(@Nullable String s)
	{
		return (s == null)?"":s;
	}

	/**
	 * Split string with fixed char length
	 * @param s string to split
	 * @param charLength length in char to split
	 * @return List of splitted string
	 */
	@Nonnull
	public static List<String> fixedSplit(@Nonnull String s, int charLength)
	{
		int i;
		int l;
		List<String> ret = new ArrayList<String>();
		i = 0;
		l = s.length();
		while (l - i > charLength)
		{
			ret.add(s.substring(i, i + charLength));
			i += charLength;
		}
		ret.add(s.substring(i));
		return ret;
	}

	/**
	 * Write a UTF-32 char into UTF-8 byte array
	 * @param buff byte array to write
	 * @param ofst offset of byte array to write
	 * @param c the UTF-32 char to write
	 * @return size written
	 */
	public static int writeChar(@Nonnull byte[] buff, int ofst, int c)
	{
		if (c < 0)
		{
			buff[ofst + 0] = (byte)(0xfc | ((c >> 30) & 3));
			c = c & 0x3fffffff;
			buff[ofst + 1] = (byte)(0x80 | ((c >> 24) & 0x3f));
			buff[ofst + 2] = (byte)(0x80 | ((c >> 18) & 0x3f));
			buff[ofst + 3] = (byte)(0x80 | ((c >> 12) & 0x3f));
			buff[ofst + 4] = (byte)(0x80 | ((c >> 6) & 0x3f));
			buff[ofst + 5] = (byte)(0x80 | (c & 0x3f));
			return 6;
		}
		else if (c < 0x80)
		{
			buff[ofst + 0] = (byte)c;
			return 1;
		}
		else if (c < 0x800)
		{
			buff[ofst + 0] = (byte)(0xc0 | (c >> 6));
			buff[ofst + 1] = (byte)(0x80 | (c & 0x3f));
			return 2;
		}
		else if (c < 0x10000)
		{
			buff[ofst + 0] = (byte)(0xe0 | (c >> 12));
			buff[ofst + 1] = (byte)(0x80 | ((c >> 6) & 0x3f));
			buff[ofst + 2] = (byte)(0x80 | (c & 0x3f));
			return 3;
		}
		else if (c < 0x200000)
		{
			buff[ofst + 0] = (byte)(0xf0 | (c >> 18));
			buff[ofst + 1] = (byte)(0x80 | ((c >> 12) & 0x3f));
			buff[ofst + 2] = (byte)(0x80 | ((c >> 6) & 0x3f));
			buff[ofst + 3] = (byte)(0x80 | (c & 0x3f));
			return 4;
		}
		else if (c < 0x4000000)
		{
			buff[ofst + 0] = (byte)(0xf8 | (c >> 24));
			buff[ofst + 1] = (byte)(0x80 | ((c >> 18) & 0x3f));
			buff[ofst + 2] = (byte)(0x80 | ((c >> 12) & 0x3f));
			buff[ofst + 3] = (byte)(0x80 | ((c >> 6) & 0x3f));
			buff[ofst + 4] = (byte)(0x80 | (c & 0x3f));
			return 5;
		}
		else
		{
			buff[ofst + 0] = (byte)(0xfc | (c >> 30));
			buff[ofst + 1] = (byte)(0x80 | ((c >> 24) & 0x3f));
			buff[ofst + 2] = (byte)(0x80 | ((c >> 18) & 0x3f));
			buff[ofst + 3] = (byte)(0x80 | ((c >> 12) & 0x3f));
			buff[ofst + 4] = (byte)(0x80 | ((c >> 6) & 0x3f));
			buff[ofst + 5] = (byte)(0x80 | (c & 0x3f));
			return 6;
		}
	}

	/**
	 * Index of using UTF8 bytes
	 * @param buff byte array to search
	 * @param ofst offset of byte array to search
	 * @param buffSize size of byte array to size
	 * @param searchStr string to search
	 * @return index of first match counting from ofst or -1 if not found
	 */
	public static int indexOfUTF8(@Nonnull byte[] buff, int ofst, int buffSize, @Nonnull String searchStr)
	{
		byte[] searchBuff = searchStr.getBytes(StandardCharsets.UTF_8);
		int i = ofst;
		int j = ofst + buffSize - searchBuff.length;
		int k;
		int l = searchBuff.length;
		boolean match;
		while (i < j)
		{
			match = true;
			k = 0;
			while (k < l)
			{
				if (buff[i + k] != searchBuff[k])
				{
					match = false;
					break;
				}
				k++;
			}
			if (match)
			{
				return i - ofst;
			}
			i++;
		}
		return -1;
	}

	/**
	 * Convert 2 bytes hex to byte
	 * @param buff byte array to convert
	 * @param ofst offset of hex value
	 * @return hex or 0 on error
	 */
	public static byte hex2UInt8C(@Nonnull byte[] buff, int ofst)
	{
		if (ofst < 0 || ofst + 2 > buff.length)
		{
			return 0;
		}
		int outVal = 0;
		byte c;
		c = buff[ofst];
		if (c == 0)
			return (byte)outVal;
		if (c >= '0' && c <= '9')
		{
			outVal = (byte)(c - 48);
		}
		else if (c >= 'A' && c <= 'F')
		{
			outVal = (byte)(c - 0x37);
		}
		else if (c >= 'a' && c <= 'f')
		{
			outVal = (byte)(c - 0x57);
		}
		else
		{
			return 0;
		}
		c = buff[ofst + 1];
		if (c == 0)
			return (byte)outVal;
		if (c >= '0' && c <= '9')
		{
			outVal = (byte)((outVal << 4) | (c - 48));
		}
		else if (c >= 'A' && c <= 'F')
		{
			outVal = (byte)((outVal << 4) | (c - 0x37));
		}
		else if (c >= 'a' && c <= 'f')
		{
			outVal = (byte)((outVal << 4) | (c - 0x57));
		}
		else
		{
			return 0;
		}
		return (byte)outVal;
	}

	/**
	 * Convert 8 bytes hex to int
	 * @param buff byte array to convert
	 * @param ofst offset of hex value
	 * @return hex or 0 on error
	 */
	public static int hex2Int32C(@Nonnull byte[] buff, int ofst)
	{
		if (ofst < 0 || ofst + 8 > buff.length)
		{
			return 0;
		}
		int i = 8;
		int outVal = 0;
		byte c;
		while (i-- > 0)
		{
			c = buff[ofst];
			if (c == 0)
				return outVal;
			if (c >= '0' && c <= '9')
			{
				outVal = (outVal << 4) | (c - 48);
			}
			else if (c >= 'A' && c <= 'F')
			{
				outVal = (outVal << 4) | (c - 0x37);
			}
			else if (c >= 'a' && c <= 'f')
			{
				outVal = (outVal << 4) | (c - 0x57);
			}
			else
			{
				return 0;
			}
			ofst++;
		}
		return outVal;
	}

	/**
	 * Convert utf8 bytes to uint
	 * @param buff byte array to convert
	 * @param ofst offset of value
	 * @return value or 0 on error
	 */
	public static int toUInt32(byte[] buff, int ofst)
	{
		int retVal = 0;
		int endOfst = buff.length;
		byte b;
		if (endOfst - ofst >= 10 && buff[ofst + 0] == '0' && buff[ofst + 1] == 'x')
		{
			retVal = hex2Int32C(buff, ofst + 2);
		}
		else
		{
			while (ofst < endOfst)
			{
				b = buff[ofst];
				if (b == 0)
					return retVal;
				if (b < '0' || b > '9')
					return 0;
				retVal = retVal * 10 + (int)b - 48;
				ofst++;
			}
		}
		return retVal;
	}

	/**
	 * Split string using CSV syntax
	 * @param csvArr array to store splitted array
	 * @param maxCount maximum split count
	 * @param csv String to split
	 * @return number of array elements filled
	 */
	public static int csvSplit(@Nonnull String[] csvArr, int maxCount, @Nonnull String csv)
	{
		boolean quoted = false;
		boolean first = true;
		int i = 1;
		char[] strToSplit = csv.toCharArray();
		int strStart = 0;
		int strWrite = 0;
		int strCurr = 0;
		char c;
		while (i < maxCount && strCurr < strToSplit.length)
		{
			c = strToSplit[strCurr++];
			if (c == 0)
			{
				strCurr--;
				break;
			}
			if (c == '"')
			{
				if (!quoted)
				{
					quoted = true;
					first = false;
				}
				else if (strCurr < strToSplit.length && strToSplit[strCurr] == '"')
				{
					strCurr++;
					strToSplit[strWrite++] = '"';
					first = false;
				}
				else
				{
					quoted = false;
				}
			}
			else if (c == ',' && !quoted)
			{
				csvArr[i - 1] = new String(strToSplit, strStart, strWrite - strStart);
				strStart = strWrite;
				i++;
				first = true;
			}
			else
			{
				if (c == ' ' && first)
				{
				}
				else
				{
					strToSplit[strWrite++] = c;
					first = false;
				}
			}
		}
		csvArr[i - 1] = new String(strToSplit, strStart, strWrite - strStart);
		return i;
	}

	/**
	 * Get string size in monospace environment
	 * @param s string to calculate
	 * @return size in monospace character
	 */
	public static @Nonnull Size2DInt getMonospaceSize(@Nonnull String s)
	{
		UTF32Reader reader = new UTF32Reader(s);
		int h = 1;
		int w = 0;
		int maxW = 0;
		int c;
		while (true)
		{
			c = reader.nextChar();
			if (c == 0)
			{
				if (w > maxW)
				{
					maxW = w;
				}
				return new Size2DInt(maxW, h);
			}
			if (c == '\r')
			{
				if (reader.isNextChar('\n'))
				{
					reader.nextChar();
				}
				if (w > maxW)
				{
					maxW = w;
				}
				w = 0;
				h++;
			}
			else if (c == '\n')
			{
				if (w > maxW)
				{
					maxW = w;
				}
				w = 0;
				h++;
			}
			else if (CharUtil.isDoubleSize(c))
				w += 2;
			else
				w += 1;
		}
	}	
}
