package org.sswr.util.data;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.Map;

public class JSONMapper
{
	private static <T> void object2Json(StringBuilder sb, T obj)
	{
		if (obj == null)
		{
			sb.append("null");
		}
		else if (obj instanceof Map)
		{
			@SuppressWarnings("unchecked")
			Map<String, Object> map = (Map<String, Object>)obj;
			Iterator<String> itKeys = map.keySet().iterator();
			boolean found = false;
			String key;
			sb.append("{");
			while (itKeys.hasNext())
			{
				key = itKeys.next();
				if (found)
				{
					sb.append(",");
				}
				object2Json(sb, key);
				sb.append(":");
				object2Json(sb, map.get(key));
				found = true;
			}
			sb.append("}");
		}
		else if (obj instanceof Iterable)
		{
			@SuppressWarnings("unchecked")
			Iterable<Object> coll = (Iterable<Object>)obj;
			Iterator<Object> itObj = coll.iterator();
			boolean found = false;
			sb.append("[");
			while (itObj.hasNext())
			{
				if (found)
				{
					sb.append(",");
				}
				object2Json(sb, itObj.next());
				found = true;
			}
			sb.append("]");
		}
		else if (obj instanceof String)
		{
			JSText.toJSTextDQuote(sb, (String)obj);
		}
		else if (obj instanceof Float)
		{
			float dVal = (Float)obj;
			if (Float.isNaN(dVal))
			{
				sb.append("\"NaN\"");
			}
			else if (Float.isInfinite(dVal))
			{
				if (dVal < 0)
				{
					sb.append("\"-Infinity\"");
				}
				else
				{
					sb.append("\"Infinity\"");
				}
			}
			else
			{
				sb.append((Float)obj);
			}
		}
		else if (obj instanceof Double)
		{
			double dVal = (Double)obj;
			if (Double.isNaN(dVal))
			{
				sb.append("\"NaN\"");
			}
			else if (Double.isInfinite(dVal))
			{
				if (dVal < 0)
				{
					sb.append("\"-Infinity\"");
				}
				else
				{
					sb.append("\"Infinity\"");
				}
			}
			else
			{
				sb.append((Double)obj);
			}
		}
		else if (obj instanceof Byte)
		{
			sb.append((Byte)obj);
		}
		else if (obj instanceof Short)
		{
			sb.append((Short)obj);
		}
		else if (obj instanceof Integer)
		{
			sb.append((Integer)obj);
		}
		else if (obj instanceof Long)
		{
			sb.append((Long)obj);
		}
		else if (obj instanceof Boolean)
		{
			sb.append((Boolean)obj);
		}
		else if (obj instanceof Timestamp)
		{
			JSText.toJSTextDQuote(sb, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format((Timestamp)obj));
		}
		else
		{
			sb.append("{");
			boolean found = false;
			Class<?> cls = obj.getClass();
			Field[] fields = cls.getDeclaredFields();
			FieldGetter<T> getter;
			int i = 0;
			int j = fields.length;
			while (i < j)
			{
				try
				{
					getter = new FieldGetter<T>(fields[i]);
					Object o = getter.get(obj);
					if (found)
					{
						sb.append(",");
					}
					found = true;
					JSText.toJSTextDQuote(sb, fields[i].getName());
					sb.append(":");
					object2Json(sb, o);
				}
				catch (IllegalAccessException ex)
				{

				}
				catch (InvocationTargetException ex)
				{

				}
				catch (IllegalArgumentException ex)
				{
					throw new IllegalArgumentException(cls.getName()+"."+fields[i].getName()+" "+ex.getMessage());
				}
				i++;
			}
			sb.append("}");
		}
	}

	public static String object2Json(Object obj)
	{
		StringBuilder sb = new StringBuilder();
		object2Json(sb, obj);
		return sb.toString();
	}
}
