package org.sswr.util.data;

import org.sswr.util.math.MathUtil;

public abstract class JSONBase
{
	protected JSONBase()
	{
	}

	public abstract String toJSONString();
	public abstract boolean equals(String s);
	public abstract boolean identical(JSONBase obj);
	public abstract String toString();

	public JSONBase getValue(String path)
	{
		if (path.equals("this"))
		{
			return this;
		}
		char carr[] = path.toCharArray();
		int currIndex = 0;
		int endIndex = carr.length;
		int dotIndex;
		int brkIndex;
		Integer index;
		JSONBase json = this;
		while (json != null)
		{
			dotIndex = StringUtil.indexOfChar(carr, currIndex, '.');
			brkIndex = StringUtil.indexOfChar(carr, currIndex, '[');
			if (dotIndex == -1 && brkIndex == -1)
			{
				if (json instanceof JSONObject)
				{
					return ((JSONObject)json).getObjectValue(new String(carr, currIndex, endIndex - currIndex));
				}
				else if (json instanceof JSONArray)
				{
					index = StringUtil.toInteger(new String(carr, currIndex, endIndex - currIndex));
					if (index != null)
					{
						return ((JSONArray)json).getArrayValue(dotIndex);
					}
				}
				return null;
			}
			else
			{
				boolean isDot = false;
				if (brkIndex == -1)
				{
					isDot = true;
				}
				else if (dotIndex == -1)
				{
					dotIndex = brkIndex;
					isDot = false;
				}
				else if (dotIndex < brkIndex)
				{
					isDot = true;
				}
				else
				{
					dotIndex = brkIndex;
					isDot = false;
				}
				if (json instanceof JSONObject)
				{
					json = ((JSONObject)json).getObjectValue(new String(carr, currIndex, dotIndex - currIndex));
				}
				else if (json instanceof JSONArray)
				{
					index = StringUtil.toInteger(new String(carr, currIndex, dotIndex - currIndex));
					if (index != null)
					{
						json = ((JSONArray)json).getArrayValue(index);
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
				currIndex = dotIndex + 1;
				if (!isDot)
				{
					if (json == null)
					{
						return null;
					}
					dotIndex = StringUtil.indexOfChar(carr, currIndex, ']');
					if (dotIndex == -1)
					{
						return null;
					}
					if (!(json instanceof JSONArray))
					{
						return null;
					}
					index = StringUtil.toInteger(new String(carr, currIndex, dotIndex - currIndex));
					if (index == null)
					{
						return null;
					}
					json = ((JSONArray)json).getArrayValue(index);
					currIndex = dotIndex + 1;
					if (currIndex >= endIndex)
					{
						return json;
					}
					else if (carr[currIndex] == '.')
					{
						currIndex++;
					}
					else if (carr[currIndex] == '[')
					{
	
					}
					else
					{
						return null;
					}
				}
			}
		}
		return null;
	}

	public String getValueString(String path)
	{
		JSONBase json = this.getValue(path);
		if (json != null && json instanceof JSONString)
		{
			return ((JSONString)json).getValue();
		}
		return null;
	}

	public int getValueAsInt32(String path)
	{
		JSONBase json = this.getValue(path);
		if (json != null)
		{
			return json.getAsInt32();
		}
		return 0;
	}

	public long getValueAsInt64(String path)
	{
		JSONBase json = this.getValue(path);
		if (json != null)
		{
			return json.getAsInt64();
		}
		return 0;
	}

	public double getValueAsDouble(String path)
	{
		JSONBase json = this.getValue(path);
		if (json != null)
		{
			return json.getAsDouble();
		}
		return 0;		
	}

	public boolean getValueAsDouble(String path, SharedDouble val)
	{
		JSONBase json = this.getValue(path);
		if (json != null)
		{
			return json.getAsDouble(val);
		}
		return false;
	}

	public boolean getValueAsBool(String path)
	{
		JSONBase json = this.getValue(path);
		if (json != null)
		{
			return json.getAsBool();
		}
		return false;
	}

	public JSONArray getValueArray(String path)
	{
		JSONBase json = this.getValue(path);
		if (json != null && json instanceof JSONArray)
		{
			return (JSONArray)json;
		}
		return null;
	}

	public JSONObject getValueObject(String path)
	{
		JSONBase json = this.getValue(path);
		if (json != null && json instanceof JSONObject)
		{
			return (JSONObject)json;
		}
		return null;
	}

	public int getAsInt32()
	{
		if (this instanceof JSONBool)
			return ((JSONBool)this).getValue()?1:0;
		else if (this instanceof JSONInt32)
			return ((JSONInt32)this).getValue();
		else if (this instanceof JSONInt64)
			return (int)(((JSONInt64)this).getValue());
		else if (this instanceof JSONNumber)
			return MathUtil.double2Int32(((JSONNumber)this).getValue());
		else if (this instanceof JSONString)
			return StringUtil.toIntegerS(((JSONString)this).getValue(), 0);
		else
			return 0;
	}

	public long getAsInt64()
	{
		if (this instanceof JSONBool)
			return ((JSONBool)this).getValue()?1:0;
		else if (this instanceof JSONInt32)
			return ((JSONInt32)this).getValue();
		else if (this instanceof JSONInt64)
			return (((JSONInt64)this).getValue());
		else if (this instanceof JSONNumber)
			return MathUtil.double2Int64(((JSONNumber)this).getValue());
		else if (this instanceof JSONString)
			return StringUtil.toLongS(((JSONString)this).getValue(), 0);
		else
			return 0;
	}

	public double getAsDouble()
	{
		if (this instanceof JSONBool)
			return ((JSONBool)this).getValue()?1:0;
		else if (this instanceof JSONInt32)
			return ((JSONInt32)this).getValue();
		else if (this instanceof JSONInt64)
			return (((JSONInt64)this).getValue());
		else if (this instanceof JSONNumber)
			return ((JSONNumber)this).getValue();
		else if (this instanceof JSONString)
			return StringUtil.toDoubleS(((JSONString)this).getValue(), 0);
		else
			return 0;
	}

	public boolean getAsDouble(SharedDouble val)
	{
		if (this instanceof JSONBool)
		{
			val.value = ((JSONBool)this).getValue()?1:0;
			return true;
		}
		else if (this instanceof JSONInt32)
		{
			val.value = ((JSONInt32)this).getValue();
			return true;
		}
		else if (this instanceof JSONInt64)
		{
			val.value = (((JSONInt64)this).getValue());
			return true;
		}
		else if (this instanceof JSONNumber)
		{
			val.value = ((JSONNumber)this).getValue();
			return true;
		}
		else if (this instanceof JSONString)
		{
			Double dVal = StringUtil.toDouble(((JSONString)this).getValue());
			if (dVal == null)
				return false;
			val.value = dVal;
			return true;
		}
		else
			return false;
	}

	public boolean getAsBool()
	{
		if (this instanceof JSONBool)
			return ((JSONBool)this).getValue();
		else if (this instanceof JSONInt32)
			return ((JSONInt32)this).getValue() != 0;
		else if (this instanceof JSONInt64)
			return ((JSONInt64)this).getValue() != 0;
		else if (this instanceof JSONNumber)
			return ((JSONNumber)this).getValue() != 0;
		else if (this instanceof JSONString)
			return ((JSONString)this).getValue() != null;
		else
			return false;
	}

	public static JSONBase parseJSONStr(String jsonStr)
	{
		StringBuilder sb = new StringBuilder();
		char[] carr = jsonStr.toCharArray();
		SharedInt endIndex = new SharedInt();
		return parseJSONStr2(carr, 0, carr.length, endIndex, sb);
	}

	private static int clearWS(char[] carr, int index, int endIndex)
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

	private static int parseJSString(char[] jsonStr, int startIndex, int endIndex, StringBuilder sb)
	{
		char[] sbuff = new char[128];
		int sindex;
		char c;
		if (startIndex >= endIndex || jsonStr[startIndex] != '\"')
			return -1;
		startIndex++;
		sindex = 0;
		while (true)
		{
			if (startIndex >= endIndex)
				return -1;
			c = jsonStr[startIndex++];
			if (c == '\"')
			{
				if (sindex != 0)
				{
					sb.append(new String(sbuff, 0, sindex));
				}
				return startIndex;
			}
			else if (c == '\\')
			{
				if (startIndex >= endIndex)
					return -1;
				c = jsonStr[startIndex++];
				if (c == '\"')
				{
					sbuff[sindex++] = '\"';
				}
				else if (c == '\\')
				{
					sbuff[sindex++] = '\\';
				}
				else if (c == '/')
				{
					sbuff[sindex++] = '/';
				}
				else if (c == 'b')
				{
					sbuff[sindex++] = '\b';
				}
				else if (c == 'r')
				{
					sbuff[sindex++] = '\r';
				}
				else if (c == 'n')
				{
					sbuff[sindex++] = '\n';
				}
				else if (c == 'f')
				{
					sbuff[sindex++] = '\f';
				}
				else if (c == 't')
				{
					sbuff[sindex++] = '\t';
				}
				else if (c == 'u')
				{
					int v = 0;
					if (startIndex + 4 > endIndex)
						return -1;
					c = jsonStr[startIndex++];
					if (c >= '0' && c <= '9')
						v = (v << 4) + (int)(c - '0');
					else if (c >= 'A' && c <= 'F')
						v = (v << 4) + (int)(c - 0x37);
					else if (c >= 'a' && c <= 'f')
						v = (v << 4) + (int)(c - 0x57);
					else
						return -1;
					c = jsonStr[startIndex++];
					if (c >= '0' && c <= '9')
						v = (v << 4) + (int)(c - '0');
					else if (c >= 'A' && c <= 'F')
						v = (v << 4) + (int)(c - 0x37);
					else if (c >= 'a' && c <= 'f')
						v = (v << 4) + (int)(c - 0x57);
					else
						return -1;
					c = jsonStr[startIndex++];
					if (c >= '0' && c <= '9')
						v = (v << 4) + (int)(c - '0');
					else if (c >= 'A' && c <= 'F')
						v = (v << 4) + (int)(c - 0x37);
					else if (c >= 'a' && c <= 'f')
						v = (v << 4) + (int)(c - 0x57);
					else
						return -1;
					c = jsonStr[startIndex++];
					if (c >= '0' && c <= '9')
						v = (v << 4) + (int)(c - '0');
					else if (c >= 'A' && c <= 'F')
						v = (v << 4) + (int)(c - 0x37);
					else if (c >= 'a' && c <= 'f')
						v = (v << 4) + (int)(c - 0x57);
					else
						return -1;
	
					sbuff[sindex++] = (char)v;
				}
				else
				{
					return 0;
				}
			}
			else
			{
				sbuff[sindex++] = c;
			}
			if (sindex >= 126)
			{
				sb.append(sbuff, 0, sindex);
				sindex = 0;
			}
		}
	}

	private static int parseJSNumber(char[] jsonStr, int startIndex, int endIndex, SharedDouble val)
	{
		char[] sbuff = new char[256];
		int dindex = 0;
		char c;
		boolean hasDot = false;
		boolean hasE = false;
		boolean numStart = true;
		while (true)
		{
			if (startIndex >= endIndex)
			{
				Double dVal = StringUtil.toDouble(new String(sbuff, 0, dindex));
				if (dVal == null)
					return -1;
				val.value = dVal;
				return startIndex;
			}
			c = jsonStr[startIndex];
			if (c >= '0' && c <= '9')
			{
				sbuff[dindex++] = c;
				numStart = false;
			}
			else if (c == '-')
			{
				if (!numStart)
					return -1;
				sbuff[dindex++] = c;
				numStart = false;
	
			}
			else if (c == '.')
			{
				if (hasDot || hasE)
					return -1;
				hasDot = true;
				numStart = false;
				sbuff[dindex++] = c;
			}
			else if (c == 'e' || c == 'E')
			{
				if (hasE || numStart)
					return -1;
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
				Double dVal = StringUtil.toDouble(new String(sbuff, 0, dindex));
				if (dVal == null)
					return -1;
				val.value = dVal;
				return startIndex;
			}
			startIndex++;
		}
	}

	private static JSONBase parseJSONStr2(char[] jsonStr, int startIndex, int endIndex, SharedInt endIndexOut, StringBuilder sbEnv)
	{
		char c;
		startIndex = clearWS(jsonStr, startIndex, endIndex);
		if (startIndex >= endIndex)
		{
			endIndexOut.value = 0;
			return null;
		}
		SharedInt tmpOut = new SharedInt();
		c = jsonStr[startIndex];
		if (c == '{')
		{
			JSONObject jobj;

			startIndex++;
			jobj = new JSONObject();

			startIndex = clearWS(jsonStr, startIndex, endIndex);
			if (startIndex >= endIndex)
			{
				endIndexOut.value = 0;
				return null;
			}
			c = jsonStr[startIndex];
			if (c == '}')
			{
				startIndex++;
				endIndexOut.value = startIndex;
				return jobj;
			}
			StringBuilder sb = new StringBuilder();
			while (true)
			{
				startIndex = clearWS(jsonStr, startIndex, endIndex);
				if (startIndex >= endIndex)
				{
					endIndexOut.value = 0;
					return null;
				}
				c = jsonStr[startIndex];
				if (c == '\"')
				{
					JSONBase obj;
					sb.setLength(0);
					startIndex = parseJSString(jsonStr, startIndex, endIndex, sb);
					if (startIndex == -1)
					{
						endIndexOut.value = 0;
						return null;
					}
					startIndex = clearWS(jsonStr, startIndex, endIndex);
					if (startIndex >= endIndex || jsonStr[startIndex] != ':')
					{
						endIndexOut.value = 0;
						return null;
					}
					startIndex++;
					startIndex = clearWS(jsonStr, startIndex, endIndex);

					obj = parseJSONStr2(jsonStr, startIndex, endIndex, tmpOut, sbEnv);
					if (tmpOut.value == 0)
					{
						endIndexOut.value = 0;
						return null;
					}
					jobj.setObjectValue(sb.toString(), obj);
					startIndex = clearWS(jsonStr, tmpOut.value, endIndex);
					if (startIndex >= endIndex)
					{
						endIndexOut.value = 0;
						return null;
					}
					c = jsonStr[startIndex];
					if (c == '}')
					{
						startIndex++;
						endIndexOut.value = startIndex;
						return jobj;
					}
					else if (c == ',')
					{
						startIndex++;
					}
					else
					{
						endIndexOut.value = 0;
						return null;
					}
				}
				else
				{
					endIndexOut.value = 0;
					return null;
				}
			}
		}
		else if (c == '[')
		{
			JSONArray arr;
			JSONBase obj;

			startIndex++;
			arr = new JSONArray();
			startIndex = clearWS(jsonStr, startIndex, endIndex);
			if (startIndex >= endIndex)
			{
				endIndexOut.value = 0;
				return null;
			}
			else if (jsonStr[startIndex] == ']')
			{
				startIndex++;
				endIndexOut.value = startIndex;
				return arr;
			}
			while (true)
			{
				if (startIndex >= endIndex)
				{
					endIndexOut.value = 0;
					return null;
				}
				obj = parseJSONStr2(jsonStr, startIndex, endIndex, tmpOut, sbEnv);
				if (tmpOut.value == 0)
				{
					endIndexOut.value = 0;
					return null;
				}
				arr.addArrayValue(obj);
				startIndex = clearWS(jsonStr, tmpOut.value, endIndex);
				if (startIndex >= endIndex)
				{
					endIndexOut.value = 0;
					return null;
				}
				c = jsonStr[startIndex];
				if (c == ']')
				{
					startIndex++;
					endIndexOut.value = startIndex;
					return arr;
				}
				else if (c == ',')
				{
					startIndex++;
				}
				else
				{
					endIndexOut.value = 0;
					return null;
				}
			}
		}
		else if (c == '\"')
		{
			int i;
			sbEnv.setLength(0);
			i = parseJSString(jsonStr, startIndex, endIndex, sbEnv);
			if (i == -1)
			{
				endIndexOut.value = 0;
				return null;
			}
			endIndexOut.value = i;
			return new JSONString(sbEnv.toString());
		}
		else if (c == '-' || (c >= '0' && c <= '9'))
		{
			SharedDouble val = new SharedDouble();
			startIndex = parseJSNumber(jsonStr, startIndex, endIndex, val);
			if (startIndex == -1)
			{
				endIndexOut.value = 0;
				return null;
			}
			else 
			{
				endIndexOut.value = startIndex;
				return new JSONNumber(val.value);
			}
		}
		else if (c == 't')
		{
			if (StringUtil.startsWith(jsonStr, startIndex, endIndex - startIndex, "true"))
			{
				endIndexOut.value = startIndex + 4;
				return new JSONBool(true);
			}
			else
			{
				endIndexOut.value = 0;
				return null;
			}
		}
		else if (c == 'f')
		{
			if (StringUtil.startsWith(jsonStr, startIndex, endIndex - startIndex, "false"))
			{
				endIndexOut.value = startIndex + 5;
				return new JSONBool(false);
			}
			else
			{
				endIndexOut.value = 0;
				return null;
			}
		}
		else if (c == 'n')
		{
			if (StringUtil.startsWith(jsonStr, startIndex, endIndex - startIndex, "null"))
			{
				endIndexOut.value = startIndex + 4;
				return new JSONNull();
			}
			else
			{
				endIndexOut.value = 0;
				return null;
			}
		}
		else
		{
			endIndexOut.value = 0;
			return null;
		}
	}
}
