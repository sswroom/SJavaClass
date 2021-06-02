package org.sswr.util.data;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class JSONObject extends JSONBase
{
	private Map<String, JSONBase> objVals;
	public JSONObject()
	{
		this.objVals = new HashMap<String, JSONBase>();
	}

	public JSType getJSType()
	{
		return JSType.OBJECT;
	}
	
	public void toJSONString(StringBuilder sb)
	{
		Iterator<String> itKeys = this.objVals.keySet().iterator();
		String key;
		JSONBase obj;
		boolean hasLast = false;
		while (itKeys.hasNext())
		{
			if (hasLast)
			{
				sb.append(", ");
			}
			key = itKeys.next();
			JSText.toJSTextDQuote(sb, key);
			sb.append(":");
			obj = this.objVals.get(key);;
			if (obj != null)
			{
				obj.toJSONString(sb);
			}
			else
			{
				sb.append("null");
			}
			hasLast = true;
		}
		sb.append("}");
	}

	public boolean equals(String s)
	{
		return false;
	}

	public boolean identical(JSONBase obj)
	{
		return this == obj;
	}

	public void setObjectValue(String name, JSONBase val)
	{
		this.objVals.put(name, val);
	}
	
	public JSONBase getObjectValue(String name)
	{
		return this.objVals.get(name);
	}

	public Set<String> getObjectNames()
	{
		return this.objVals.keySet();
	}
}
