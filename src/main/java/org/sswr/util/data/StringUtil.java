package org.sswr.util.data;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class StringUtil
{
	private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

	/**
	* Check whether the string is numeric
	*
	* @param  s  the string to check
	* @return    true if the string is not null/empty and contains digits only
	*/
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

	/**
	* Check whether the string is null or empty
	*
	* @param  s  the string to check
	* @return    true if the string is null or empty
	*/
	public static boolean isNullOrEmpty(String s)
	{
		return s == null || s.length() == 0;
	}

	/**
	* Check whether the string has any characters
	*
	* @param  s  the string to check
	* @return    true if the string has at least 1 characters
	*/
	public static boolean hasChars(String s)
	{
		return s != null && s.length() > 0;
	}

	/**
	* Check whether the string array can convert to int array
	*
	* @param  sarr  array of string to check
	* @return    true if the string can convert to int array with at least 1 element
	*/
	public static boolean canParseIntArr(String sarr[])
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
	public static String leftPad(String s, int minLeng, char padChar)
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
	* @param  s  original string
	* @param  minDigits minimum number of digits of the string
	* @return      padded string
	*/
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
	* @param  objs  list of string
	* @param  seperator seperator of strings
	* @return      joined string or null if objs is null
	*/
	public static String join(Iterable<String> strs, String seperator)
	{
		if (strs == null)
			return null;
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
	public static String join(String strs[], String seperator)
	{
		if (strs == null)
			return null;
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
	public static <T extends Object> String joinObjs(Iterable<T> objs, String seperator)
	{
		if (objs == null)
			return null;
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
	* Convert byte to Hexadecimal String
	*
	* @param  b  byte to convert
	* @return    Upper case Hexadecimal String, must be 2 char long
	*/
	public static String toHex(byte b)
	{
		int v = b & 0xff;
		return new String(new char[]{HEX_ARRAY[v >> 4], HEX_ARRAY[v & 15]});
	}

	/**
	* Convert byte array to hexadecimal String
	*
	* @param  buff  byte array to convert
	* @return    Upper case Hexadecimal String, must be 2 * buff.length characters long
	*/
	public static String toHex(byte buff[])
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
	public static String toHex(byte buff[], int index, int len)
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
	* @param  ch  Char to seperate for every byte
	* @param  lbt  LineBreakType to seperate for every 16 bytes
	* @param  sb  StringBuilder to build the output string
	*/
	public static void toHex(byte buff[], char ch, LineBreakType lbt, StringBuilder sb)
	{
		toHex(buff, 0, buff.length, ch, lbt, sb);
	}


	/**
	* Convert byte array to hexadecimal String
	*
	* @param  buff  byte array to convert
	* @param  ofst  offset of byte array to convert
	* @param  count  number of bytes to convert
	* @param  ch  Char to seperate for every byte
	* @param  lbt  LineBreakType to seperate for every 16 bytes
	* @param  sb  StringBuilder to build the output string
	*/
	public static void toHex(byte buff[], int ofst, int count, char ch, LineBreakType lbt, StringBuilder sb)
	{
		int i = ofst;
		int j = ofst + count;
		int v;
		if (ch != 0)
		{
			while (i < j)
			{
				v = buff[i] & 0xff;
				sb.append(HEX_ARRAY[v >> 4]);
				sb.append(HEX_ARRAY[v & 15]);
				sb.append(ch);
				i++;
				if ((i & 15) == 0)
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
			}
		}
		else
		{
			while (i < j)
			{
				v = buff[i] & 0xff;
				sb.append(HEX_ARRAY[v >> 4]);
				sb.append(HEX_ARRAY[v & 15]);
				i++;
				if ((i & 15) == 0)
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
			}
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

	/**
	* Parse String to Integer
	*
	* @param  s  String to parse
	* @return     null if it is not valid Integer
	*/
	public static Integer toInteger(String s)
	{
		try
		{
			if (isNullOrEmpty(s))
			{
				return null;
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
	public static int toIntegerS(String s, int failVal)
	{
		try
		{
			if (isNullOrEmpty(s))
			{
				return failVal;
			}
			return Integer.parseInt(s);
		}
		catch (NumberFormatException ex)
		{
			return failVal;
		}
	}

	/**
	* Parse String to Long
	*
	* @param  s  String to parse
	* @return     null if it is not valid Long
	*/
	public static Long toLong(String s)
	{
		try
		{
			if (isNullOrEmpty(s))
			{
				return null;
			}
			return Long.parseLong(s);
		}
		catch (NumberFormatException ex)
		{
			return null;
		}
	}

	/**
	* Parse String to Double
	*
	* @param  s  String to parse
	* @return     null if it is not valid Double
	*/
	public static Double toDouble(String s)
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
	* Parse String into Set of Integer
	*
	* @param  s  String to parse
	* @param  seperator Seperator of the string
	* @return     null if it is input is not valid
	*/
	public static Set<Integer> toIntSet(String s, String seperator)
	{
		if (s == null) return null;
		return toIntSet(s.split(seperator));
	}

	/**
	* Parse String array into Set of Integer
	*
	* @param  sarr  Array of String to parse
	* @return     null if it is input is not valid
	*/
	public static Set<Integer> toIntSet(String sarr[])
	{
		if (sarr == null) return null;
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
	public static <T extends Enum<T>> String getEnumName(T e)
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
			if (v > 1.0e-14)
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
	public static int concat(char []buff, int ofst, String s)
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
	* Trim L and R of StringBuilder for white space 
	*
	* @param  sb  StringBuilder to trim
	*/
	public static void trim(StringBuilder sb)
	{
		trimRight(sb);
		trimLeft(sb);
	}

	/**
	* Trim Left of StringBuilder for white space 
	*
	* @param  sb  StringBuilder to trim
	*/
	public static void trimLeft(StringBuilder sb)
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
	public static void trimRight(StringBuilder sb)
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
	 * @return  >= 0 for exact match
	 *          < 0 for not matching, ~return = position for sorted value to insert
	 */
	public static int sortedIndexOf(List<String> list, String value)
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
	public static int sortedInsert(List<String> list, String value)
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
	public static boolean startsWith(StringBuilder sb, String value)
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
	public static boolean endsWith(StringBuilder sb, String value)
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
	public static boolean endsWith(StringBuilder sb, char value)
	{
		if (sb.length() <= 0)
		{
			return false;
		}
		return sb.charAt(sb.length() - 1) == value;
	}
}
