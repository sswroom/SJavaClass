package org.sswr.util.data;

public abstract class JSONBase
{
	public enum JSType
	{
		OBJECT,
		ARRAY,
		NUMBER,
		STRING,
		BOOL,
		NULL,
		INT32,
		INT64
	}

	protected JSONBase()
	{

	}

	public abstract JSType getJSType();
	public abstract void toJSONString(StringBuilder sb);
	public abstract boolean equals(String s);
	public abstract boolean identical(JSONBase obj);

	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		toJSONString(sb);
		return sb.toString();
	}

	public static JSONBase parseJSONStr(String jsonStr)
	{
		char carr[] = jsonStr.toCharArray();
		return parseJSONStr2(carr, 0, carr.length, new SharedInt());
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

	private static int parseJSNumber(char carr[], int index, int endIndex, SharedDouble val)
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
				Double v = StringUtil.toDouble(new String(sbuff, 0, dindex));
				if (v != null)
				{
					val.value = v.doubleValue();
					return index;
				}
				else
				{
					val.value = 0;
					return index;
				}
			}
			index++;
		}
	}

	private static JSONBase parseJSONStr2(char carr[], int index, int endIndex, SharedInt parseEndIndex)
	{
		char c;
		index = clearWS(carr, index, endIndex);
		if (index >= endIndex)
		{
			return null;
		}
		c = carr[index];
		if (c == '{')
		{
			JSONObject jobj;
	
			index++;
			jobj = new JSONObject();
	
			index = clearWS(carr, index, endIndex);
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
				c = carr[index];
				if (c == '\"')
				{
					StringBuilder sb = new StringBuilder();
					JSONBase obj;
					index = parseJSString(carr, index, endIndex, sb);
					if (index > endIndex)
					{
						return null;
					}
					index = clearWS(carr, index, endIndex);
					c = carr[index];
					if (c != ':')
					{
						return null;
					}
					index++;
					index = clearWS(carr, index, endIndex);
	
					SharedInt newEndIndex = new SharedInt();
					obj = parseJSONStr2(carr, index, endIndex, newEndIndex);
					if (obj == null)
					{
						return null;
					}
					index = newEndIndex.value;
					jobj.setObjectValue(sb.toString(), obj);
					index = clearWS(carr, index, endIndex);
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
						return null;
					}
				}
				else
				{
					return null;
				}
			}
		}
		else if (c == '[')
		{
			JSONArray arr;
			JSONBase obj;
	
			index++;
			arr = new JSONArray();
			index  = clearWS(carr, index, endIndex);
			if (carr[index] == ']')
			{
				index++;
				parseEndIndex.value = index;
				return arr;
			}
			while (true)
			{
				c = carr[index];
				if (c == 0)
				{
					return null;
				}
				else
				{
					SharedInt newEndIndex = new SharedInt();
					obj = parseJSONStr2(carr, index, endIndex, newEndIndex);
					if (obj == null)
					{
						return null;
					}
					index = newEndIndex.value;
					arr.addArrayValue(obj);
					index = clearWS(carr, index, endIndex);
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
						return null;
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
				return null;
			}
			parseEndIndex.value = index;
			return new JSONString(sb2.toString());
		}
		else if (c == '-' || (c >= '0' && c <= '9'))
		{
			SharedDouble val = new SharedDouble();
			index = parseJSNumber(carr, index, endIndex, val);
			if (index > endIndex)
			{
				return null;
			}
			else 
			{
				JSONNumber num = new JSONNumber(val.value);
				parseEndIndex.value = index;
				return num;
			}
		}
		else if (c == 't')
		{
			if (carr[index] == 't' && carr[index + 1] == 'r' && carr[index + 2] == 'u' && carr[index + 3] == 'e')
			{
				parseEndIndex.value = index + 4;
				return new JSONBool(true);
			}
			else
			{
				return null;
			}
		}
		else if (c == 'f')
		{
			if (carr[index] == 'f' && carr[index + 1] == 'a' && carr[index + 2] == 'l' && carr[index + 3] == 's' && carr[index + 4] == 'e')
			{
				parseEndIndex.value = index + 5;
				return new JSONBool(false);
			}
			else
			{
				return null;
			}
		}
		else if (c == 'n')
		{
			if (carr[index] == 'n' && carr[index + 1] == 'u' && carr[index + 2] == 'l' && carr[index + 3] == 'l')
			{
				parseEndIndex.value = index + 4;;
				return new JSONNull();
			}
			else
			{
				return null;
			}
		}
		else
		{
			return null;
		}
	}
}
