package org.sswr.util.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JSONParser
{
	public static Object parse(String jsonStr)
	{
		char carr[] = jsonStr.toCharArray();
		int leng = carr.length;
		int index = 0;
		if (leng > 0 && carr[0] == 0xFEFF)
		{
			index++;
		}
		return parseJSONStr2(carr, index, leng, new SharedInt());
	}

	private static int clearWS(char carr[], int index, int endIndex)
	{
		char c;
		while (index < endIndex)
		{
			c = carr[index];
			if (c == ' ' || c == '\r' || c == '\n' || c == '\t')
			{
				index++;
			}
			else
			{
				break;
			}
		}
		return index;
	}

	private static int parseJSString(char carr[], int index, int endIndex, StringBuilder sb)
	{
		char c;
		c = carr[index++];
		if (c != '\"')
			return 0;
		while (index < endIndex)
		{
			c = carr[index++];
			if (c == '\"')
			{
				return index;
			}
			else if (c == '\\')
			{
				if (index >= endIndex)
				{
					return endIndex + 1;
				}
				c = carr[index++];
				if (c == '\"')
				{
					sb.append('\"');
				}
				else if (c == '\\')
				{
					sb.append('\\');
				}
				else if (c == '/')
				{
					sb.append('/');
				}
				else if (c == 'b')
				{
					sb.append('\b');
				}
				else if (c == 'r')
				{
					sb.append('\r');
				}
				else if (c == 'n')
				{
					sb.append('\n');
				}
				else if (c == 'f')
				{
					sb.append('\f');
				}
				else if (c == 't')
				{
					sb.append('\t');
				}
				else if (c == 'u')
				{
					if (index + 4 > endIndex)
					{
						return endIndex + 1;
					}
					int v = 0;
					c = carr[index++];
					if (c >= '0' && c <= '9')
						v = (v << 4) + (c - '0');
					else if (c >= 'A' && c <= 'F')
						v = (v << 4) + (c - 0x37);
					else if (c >= 'a' && c <= 'f')
						v = (v << 4) + (c - 0x57);
					else
						return endIndex + 1;
					c = carr[index++];
					if (c >= '0' && c <= '9')
						v = (v << 4) + (c - '0');
					else if (c >= 'A' && c <= 'F')
						v = (v << 4) + (c - 0x37);
					else if (c >= 'a' && c <= 'f')
						v = (v << 4) + (c - 0x57);
					else
						return endIndex + 1;
					c = carr[index++];
					if (c >= '0' && c <= '9')
						v = (v << 4) + (c - '0');
					else if (c >= 'A' && c <= 'F')
						v = (v << 4) + (c - 0x37);
					else if (c >= 'a' && c <= 'f')
						v = (v << 4) + (c - 0x57);
					else
						return endIndex + 1;
					c = carr[index++];
					if (c >= '0' && c <= '9')
						v = (v << 4) + (c - '0');
					else if (c >= 'A' && c <= 'F')
						v = (v << 4) + (c - 0x37);
					else if (c >= 'a' && c <= 'f')
						v = (v << 4) + (c - 0x57);
					else
						return endIndex + 1;
	
					if (v >= 0xd800 && v < 0xdc00)
					{
						if (index + 6 <= endIndex && carr[index] == '\\' && carr[index + 1] == 'u')
						{
							int v2 = 0;
							index += 2;
							c = carr[index++];
							if (c >= '0' && c <= '9')
								v2 = (v2 << 4) + (c - '0');
							else if (c >= 'A' && c <= 'F')
								v2 = (v2 << 4) + (c - 0x37);
							else if (c >= 'a' && c <= 'f')
								v2 = (v2 << 4) + (c - 0x57);
							else
								return endIndex + 1;
							c = carr[index++];
							if (c >= '0' && c <= '9')
								v2 = (v2 << 4) + (c - '0');
							else if (c >= 'A' && c <= 'F')
								v2 = (v2 << 4) + (c - 0x37);
							else if (c >= 'a' && c <= 'f')
								v2 = (v2 << 4) + (c - 0x57);
							else
								return endIndex + 1;
							c = carr[index++];
							if (c >= '0' && c <= '9')
								v2 = (v2 << 4) + (c - '0');
							else if (c >= 'A' && c <= 'F')
								v2 = (v2 << 4) + (c - 0x37);
							else if (c >= 'a' && c <= 'f')
								v2 = (v2 << 4) + (c - 0x57);
							else
								return endIndex + 1;
							c = carr[index++];
							if (c >= '0' && c <= '9')
								v2 = (v2 << 4) + (c - '0');
							else if (c >= 'A' && c <= 'F')
								v2 = (v2 << 4) + (c - 0x37);
							else if (c >= 'a' && c <= 'f')
								v2 = (v2 << 4) + (c - 0x57);
							else
								return endIndex + 1;
	
							if (v2 >= 0xdc00 && v2 < 0xe000)
							{
								v = 0x10000 + ((v - 0xd800) << 10) + (v2 - 0xdc00);
								sb.append((char)v);
							}
							else
							{
								sb.append((char)v);
								sb.append((char)v2);
							}
						}
						else
						{
							sb.append((char)v);
						}
					}
					else
					{
						sb.append((char)v);
					}
				}
				else
				{
					return endIndex + 1;
				}
			}
			else
			{
				sb.append(c);
			}
		}
		return endIndex + 1;
	}

	private static int parseJSNumber(char carr[], int index, int endIndex, SharedObject<Object> val)
	{
		char sbuff[] = new char[256];
		int dindex = 0;
		char c;
		boolean hasDot = false;
		boolean hasE = false;
		boolean numStart = true;
		while (true)
		{
			c = carr[index];
			if (c >= '0' && c <= '9')
			{
				sbuff[dindex++] = c;
				numStart = false;
			}
			else if (c == '-')
			{
				if (!numStart)
					return endIndex + 1;
				sbuff[dindex++] = c;
				numStart = false;
	
			}
			else if (c == '.')
			{
				if (hasDot || hasE)
					return endIndex + 1;
				hasDot = true;
				numStart = false;
				sbuff[dindex++] = c;
			}
			else if (c == 'e' || c == 'E')
			{
				if (hasE || numStart)
					return endIndex + 1;
				hasE = true;
				sbuff[dindex++] = c;
				numStart = true;
			}
			else if (c == '+' && hasE && numStart)
			{
				numStart = false;
				sbuff[dindex++] = c;
			}
			else
			{
				sbuff[dindex] = 0;
				String s = new String(sbuff, 0, dindex);
				Integer i32 = StringUtil.toInteger(s);
				if (i32 != null)
				{
					val.value = i32;
					return index;
				}
				Long i64 = StringUtil.toLong(s);
				if (i64 != null)
				{
					val.value = i64;
					return index;
				}
				Double v = StringUtil.toDouble(s);
				if (v != null)
				{
					val.value = v;
					return index;
				}
				throw new IllegalArgumentException("Illegal numeric value");
			}
			index++;
		}
	}

	private static Object parseJSONStr2(char carr[], int index, int endIndex, SharedInt parseEndIndex)
	{
		char c;
		index = clearWS(carr, index, endIndex);
		if (index >= endIndex)
		{
			throw new IllegalArgumentException("index("+index+") >= endIndex("+endIndex+")");
		}
		c = carr[index];
		if (c == '{')
		{
			Map<String, Object> jobj;
	
			index++;
			jobj = new HashMap<String, Object>();
	
			index = clearWS(carr, index, endIndex);
			if (index >= endIndex)
			{
				throw new IllegalArgumentException("Unexpected end of string after {");
			}
			c = carr[index];
			if (c == '}')
			{
				index++;
				parseEndIndex.value = index;
				return jobj;
			}
			while (true)
			{
				index = clearWS(carr, index, endIndex);
				if (index >= endIndex)
				{
					throw new IllegalArgumentException("Unexpected end of string before object name");
				}
				c = carr[index];
				if (c == '\"')
				{
					StringBuilder sb = new StringBuilder();
					Object obj;
					index = parseJSString(carr, index, endIndex, sb);
					if (index > endIndex)
					{
						throw new IllegalArgumentException("Error in parsing object name");
					}
					index = clearWS(carr, index, endIndex);
					if (index >= endIndex)
					{
						throw new IllegalArgumentException("Unexpected end of string after object name");
					}
					c = carr[index];
					if (c != ':')
					{
						throw new IllegalArgumentException("':' not found after object name");
					}
					index++;
					index = clearWS(carr, index, endIndex);
	
					SharedInt newEndIndex = new SharedInt();
					obj = parseJSONStr2(carr, index, endIndex, newEndIndex);
					index = newEndIndex.value;
					jobj.put(sb.toString(), obj);
					index = clearWS(carr, index, endIndex);
					if (index >= endIndex)
					{
						throw new IllegalArgumentException("Unexpected end of string after object value");
					}
					c = carr[index];
					if (c == '}')
					{
						index++;
						parseEndIndex.value = index;
						return jobj;
					}
					else if (c == ',')
					{
						index++;
					}
					else
					{
						throw new IllegalArgumentException("Invalid character after object");
					}
				}
				else
				{
					throw new IllegalArgumentException("Invalid object name");
				}
			}
		}
		else if (c == '[')
		{
			List<Object> arr;
			Object obj;
	
			index++;
			arr = new ArrayList<Object>();
			index  = clearWS(carr, index, endIndex);
			if (index >= endIndex)
			{
				throw new IllegalArgumentException("Unexpected end of string after [");
			}
			if (carr[index] == ']')
			{
				index++;
				parseEndIndex.value = index;
				return arr;
			}
			while (true)
			{
				index = clearWS(carr, index, endIndex);
				if (index >= endIndex)
				{
					throw new IllegalArgumentException("Unexpected end of string after [");
				}
				c = carr[index];
				if (c == 0)
				{
					return null;
				}
				else
				{
					SharedInt newEndIndex = new SharedInt();
					newEndIndex.value = 0;
					obj = parseJSONStr2(carr, index, endIndex, newEndIndex);
					if (obj == null && newEndIndex.value == 0)
					{
						return null;
					}
					index = newEndIndex.value;
					arr.add(obj);
					index = clearWS(carr, index, endIndex);
					if (index >= endIndex)
					{
						throw new IllegalArgumentException("Unexpected end of string after array value");
					}
					c = carr[index];
					if (c == ']')
					{
						index++;
						parseEndIndex.value = index;
						return arr;
					}
					else if (c == ',')
					{
						index++;
					}
					else
					{
						throw new IllegalArgumentException("Unexpected character after array value ("+c+")");
					}
				}
			}
		}
		else if (c == '\"')
		{
			StringBuilder sb2 = new StringBuilder();
			index = parseJSString(carr, index, endIndex, sb2);
			if (index > endIndex)
			{
				throw new IllegalArgumentException("Error in parsing string value");
			}
			parseEndIndex.value = index;
			return sb2.toString();
		}
		else if (c == '-' || (c >= '0' && c <= '9'))
		{
			SharedObject<Object> val = new SharedObject<Object>();
			index = parseJSNumber(carr, index, endIndex, val);
			if (index > endIndex)
			{
				throw new IllegalArgumentException("Error in parsing numeric value");
			}
			else 
			{
				parseEndIndex.value = index;
				return val.value;
			}
		}
		else if (c == 't')
		{
			if (carr[index] == 't' && carr[index + 1] == 'r' && carr[index + 2] == 'u' && carr[index + 3] == 'e')
			{
				parseEndIndex.value = index + 4;
				return true;
			}
			else
			{
				throw new IllegalArgumentException("Error in parsing t value");
			}
		}
		else if (c == 'f')
		{
			if (carr[index] == 'f' && carr[index + 1] == 'a' && carr[index + 2] == 'l' && carr[index + 3] == 's' && carr[index + 4] == 'e')
			{
				parseEndIndex.value = index + 5;
				return false;
			}
			else
			{
				throw new IllegalArgumentException("Error in parsing f value");
			}
		}
		else if (c == 'n')
		{
			if (carr[index] == 'n' && carr[index + 1] == 'u' && carr[index + 2] == 'l' && carr[index + 3] == 'l')
			{
				parseEndIndex.value = index + 4;
				return null;
			}
			else
			{
				throw new IllegalArgumentException("Error in parsing n value");
			}
		}
		else
		{
			throw new IllegalArgumentException("Unexpected character found ("+c+")");
		}
	}
}
