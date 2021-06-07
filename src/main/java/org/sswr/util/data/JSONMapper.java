package org.sswr.util.data;

import java.util.Iterator;
import java.util.Map;

public class JSONMapper
{
	private static void object2Json(StringBuilder sb, Object obj)
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
		else if (obj instanceof Double)
		{
			sb.append((Double)obj);
		}
		else if (obj instanceof Integer)
		{
			sb.append((Integer)obj);
		}
		else if (obj instanceof Long)
		{
			sb.append((Long)obj);
		}
		else
		{
			JSText.toJSTextDQuote(sb, obj.toString());
		}
	}

	public static String object2Json(Object obj)
	{
		StringBuilder sb = new StringBuilder();
		object2Json(sb, obj);
		return sb.toString();
	}
}
