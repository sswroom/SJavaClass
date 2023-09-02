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

	public String toJSONString()
	{
		StringBuilder sb = new StringBuilder();
		JSONBase obj;
		Iterator<String> it = this.objVals.keySet().iterator();
		String key;
		boolean first = true;
		sb.append('{');
		while (it.hasNext())
		{
			if (!first)
			{
				sb.append(", ");
			}
			first = false;
			key = it.next();
			JSText.toJSTextDQuote(sb, key);
			sb.append(" : ");
			obj = this.objVals.get(key);
			if (obj != null)
			{
				sb.append(obj.toJSONString());
			}
			else
			{
				sb.append("null");
			}
		}
		sb.append('}');
		return sb.toString();
	}

	public boolean equals(String s)
	{
		///////////////////////////////
		return false;
	}

	public boolean identical(JSONBase obj)
	{
		return this == obj;
	}

	public String toString()
	{
		return this.toJSONString();
	}
		
	public void setObjectValue(String name, JSONBase val)
	{
		this.objVals.put(name, val);
	}

	public void setObjectInt32(String name, int val)
	{
		this.objVals.put(name, new JSONInt32(val));
	}

	public void setObjectInt64(String name, long val)
	{
		this.objVals.put(name, new JSONInt64(val));
	}

	public void setObjectDouble(String name, double val)
	{
		this.objVals.put(name, new JSONNumber(val));
	}

	public void setObjectString(String name, String val)
	{
		this.objVals.put(name, new JSONString(val));
	}

	public void setObjectBool(String name, boolean val)
	{
		this.objVals.put(name, new JSONBool(val));
	}

	public JSONBase getObjectValue(String name)
	{
		return this.objVals.get(name);
	}

	public JSONArray getObjectArray(String name)
	{
		JSONBase o = this.getObjectValue(name);
		if (o != null && o instanceof JSONArray)
			return (JSONArray)o;
		return null;
	}

	public JSONObject getObjectObject(String name)
	{
		JSONBase o = this.getObjectValue(name);
		if (o != null && o instanceof JSONObject)
			return (JSONObject)o;
		return null;
	}

	public Set<String> getObjectNames()
	{
		return this.objVals.keySet();
	}

	public String getObjectString(String name)
	{
		JSONBase baseObj = this.objVals.get(name);
		if (baseObj == null || !(baseObj instanceof JSONString))
		{
			return null;
		}
		return ((JSONString)baseObj).getValue();
	}

	public double getObjectDouble(String name)
	{
		JSONBase baseObj = this.objVals.get(name);
		if (baseObj == null)
		{
			return 0;
		}
		return baseObj.getAsDouble();
	}

	public int getObjectInt32(String name)
	{
		JSONBase baseObj = this.objVals.get(name);
		if (baseObj == null)
		{
			return 0;
		}
		return baseObj.getAsInt32();
	}

	public long getObjectInt64(String name)
	{
		JSONBase baseObj = this.objVals.get(name);
		if (baseObj == null)
		{
			return 0;
		}
		return baseObj.getAsInt64();
	}

	public boolean getObjectBool(String name)
	{
		JSONBase baseObj = this.objVals.get(name);
		if (baseObj == null)
		{
			return false;
		}
		return baseObj.getAsBool();
	}

	public void removeObject(String name)
	{
		this.objVals.remove(name);
	}

}
