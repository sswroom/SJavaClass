package org.sswr.util.data;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.Timestamp;
import java.util.Iterator;
import java.util.Map;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class JSONMapper
{
	private static <T> void object2Json(@Nonnull StringBuilderUTF8 sb, @Nullable T obj)
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
				sb.appendF64(dVal);
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
				sb.appendF64(dVal);
			}
		}
		else if (obj instanceof Byte)
		{
			sb.appendI16(((Byte)obj).shortValue());
		}
		else if (obj instanceof Short)
		{
			sb.appendI16(((Short)obj).shortValue());
		}
		else if (obj instanceof Integer)
		{
			sb.appendI32(((Integer)obj).intValue());
		}
		else if (obj instanceof Long)
		{
			sb.appendI64(((Long)obj).longValue());
		}
		else if (obj instanceof Boolean)
		{
			sb.appendOpt(((Boolean)obj).toString());
		}
		else if (obj instanceof Timestamp)
		{
			JSText.toJSTextDQuote(sb, DateTimeUtil.toStringNoZone((Timestamp)obj));
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

	@Nonnull
	public static String object2Json(@Nullable Object obj)
	{
		StringBuilderUTF8 sb = new StringBuilderUTF8();
		object2Json(sb, obj);
		return sb.toString();
	}
}
