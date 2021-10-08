package org.sswr.util.data;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.sswr.util.io.CharReader;

public class CPPObjectParser<T>
{
	public Constructor<T> constructor;
	public List<FieldSetter> setters;

	public CPPObjectParser(Class<T> cls, String[] fieldNames) throws NoSuchMethodException, NoSuchFieldException
	{
		this.constructor = cls.getConstructor(new Class[0]);
		this.setters = new ArrayList<FieldSetter>();
		if (fieldNames == null)
		{
			Field[] fields = cls.getDeclaredFields();
			int i = 0;
			int j = fields.length;
			while (i < j)
			{
				setters.add(new FieldSetter(fields[i]));
				i++;
			}
		}
		else
		{
			int i = 0;
			int j = fieldNames.length;
			while (i < j)
			{
				setters.add(new FieldSetter(cls.getDeclaredField(fieldNames[i])));
				i++;
			}
		}
	}

	public T parseObject(CharReader reader)
	{
		int i;
		int j;
		reader.skipWS();
		while (reader.startsWith("//"))
		{
			if (!reader.nextLine())
			{
				return null;
			}
			reader.skipWS();
		}
		if (reader.nextChar() != '{')
		{
			return null;
		}
		try
		{
			T o;
			try
			{
				o = this.constructor.newInstance(new Object[0]);
			}
			catch (InstantiationException ex)
			{
				ex.printStackTrace();
				return null;
			}
	
			char c;
			int objInd = 0;
			Field f = this.setters.get(0).getField();
			while (true)
			{
				reader.skipWS();
				if (f.getType().equals(String.class))
				{
					c = reader.nextChar();
					if (c != '\"')
					{
						return null;
					}
					StringBuilder sb = new StringBuilder();
					while (true)
					{
						c = reader.nextChar();
						if (c == 0)
						{
							return null;
						}
						else if (c == '\"')
						{
							break;
						}
						else if (c == '\\')
						{
							switch (reader.nextChar())
							{
								case 'r':
									sb.append('\r');
									break;
								case 'n':
									sb.append('\n');
									break;
								case 't':
									sb.append('\t');
									break;
								case 0:
									return null;
								case '\\':
									sb.append('\\');
									break;
								default:
									sb.append(c);
									break;

							}
						}
						else
						{
							sb.append(c);
						}
					}
					this.setters.get(objInd).set(o, sb.toString());
				}
				else if (f.getType().equals(int.class))
				{
					StringBuilder sb = new StringBuilder();
					boolean isHex = false;
					while (true)
					{
						c = reader.currChar();
						if (c >= '0' && c <= '9')
						{
							sb.append(c);
						}
						else if (c == 'x' && sb.toString().equals("0"))
						{
							sb.append(c);
							isHex = true;
						}
						else if (isHex && c >= 'A' && c <= 'F')
						{
							sb.append(c);
						}
						else if (isHex && c >= 'a' && c <= 'f')
						{
							sb.append(c);
						}
						else
						{
							break;
						}
						reader.nextChar();
					}
					Integer iVal = StringUtil.toInteger(sb.toString());
					if (iVal == null)
					{
						return null;
					}
					setters.get(objInd).set(o, iVal);
				}
				else if (f.getType().equals(long.class))
				{
					StringBuilder sb = new StringBuilder();
					boolean isHex = false;
					while (true)
					{
						c = reader.currChar();
						if (c >= '0' && c <= '9')
						{
							sb.append(c);
						}
						else if (c == 'x' && sb.toString().equals("0"))
						{
							sb.append(c);
							isHex = true;
						}
						else if (isHex && c >= 'A' && c <= 'F')
						{
							sb.append(c);
						}
						else if (isHex && c >= 'a' && c <= 'f')
						{
							sb.append(c);
						}
						else if (c == 'L')
						{
							if (reader.startsWith("LL"))
							{
								reader.nextChar();
								reader.nextChar();
							}
							break;
						}
						else
						{
							break;
						}
						reader.nextChar();
					}
					Long lVal = StringUtil.toLong(sb.toString());
					if (lVal == null)
					{
						return null;
					}
					setters.get(objInd).set(o, lVal);
				}
				else if (f.getType().equals(byte[].class))
				{
					List<Integer> intList = new ArrayList<Integer>();
					StringBuilder sb = new StringBuilder();
					boolean isHex = false;
					c = reader.nextChar();
					if (c != '{')
					{
						return null;
					}
					while (true)
					{
						while (true)
						{
							c = reader.currChar();
							if (c >= '0' && c <= '9')
							{
								sb.append(c);
							}
							else if (c == 'x' && sb.toString().equals("0"))
							{
								sb.append(c);
								isHex = true;
							}
							else if (isHex && c >= 'A' && c <= 'F')
							{
								sb.append(c);
							}
							else if (isHex && c >= 'a' && c <= 'f')
							{
								sb.append(c);
							}
							else
							{
								break;
							}
							reader.nextChar();
						}
						Integer iVal = StringUtil.toInteger(sb.toString());
						if (iVal == null)
						{
							return null;
						}
						intList.add(iVal);
						sb.setLength(0);
						isHex = false;

						reader.skipWS();
						while (reader.startsWith("//"))
						{
							if (!reader.nextLine())
							{
								return null;
							}
							reader.skipWS();
						}
						c = reader.nextChar();
						if (c == '}')
						{
							break;
						}
						else if (c != ',')
						{
							return null;
						}
						reader.skipWS();

					}
					byte[] byteBuff = new byte[intList.size()];
					i = 0;
					j = intList.size();
					while (i < j)
					{
						byteBuff[i] = (byte)(intList.get(i) & 0xff);
						i++;
					}
					setters.get(objInd).set(o, byteBuff);
				}
				else if (f.getType().equals(boolean.class))
				{
					c = reader.currChar();
					if (c == 't')
					{
						if (reader.nextChar() == 'r' && reader.nextChar() == 'u' && reader.nextChar() == 'e')
						{
							setters.get(objInd).set(o, true);
							reader.nextChar();
						}
						else
						{
							return null;
						}
					}
					else if (c == 'f')
					{
						if (reader.nextChar() == 'a' && reader.nextChar() == 'l' && reader.nextChar() == 's' && reader.nextChar() == 'e')
						{
							setters.get(objInd).set(o, true);
							reader.nextChar();
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
				else
				{
					return null;
				}
				reader.skipWS();
				while (reader.startsWith("//"))
				{
					if (!reader.nextLine())
					{
						return null;
					}
					reader.skipWS();
				}
				c = reader.nextChar();
				if (c == ',')
				{
					objInd++;
					if (objInd >= this.setters.size())
					{
						return null;
					}
					f = this.setters.get(objInd).getField();
				}
				else if (c == '}')
				{
					objInd++;
					if (objInd >= this.setters.size())
					{
						break;
					}
					return null;
				}
				else
				{
					return null;
				}
			}
			reader.skipWS();
			while (reader.startsWith("//"))
			{
				if (!reader.nextLine())
				{
					return null;
				}
				reader.skipWS();
			}
			c = reader.currChar();
			if (c == ',' || c == '}')
			{
				return o;
			}
			else
			{
				return null;
			}
		}
		catch (IllegalAccessException ex)
		{
			ex.printStackTrace();
			return null;
		}
		catch (InvocationTargetException ex)
		{
			ex.printStackTrace();
			return null;
		}
	}
}
