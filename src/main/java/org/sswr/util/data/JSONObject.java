package org.sswr.util.data;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class JSONObject extends JSONBase
{
	private Map<String, JSONBase> objVals;

	public JSONObject()
	{
		this.objVals = new HashMap<String, JSONBase>();
	}

	@Nonnull
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

	public boolean equals(@Nonnull String s)
	{
		///////////////////////////////
		return false;
	}

	public boolean identical(@Nonnull JSONBase obj)
	{
		return this == obj;
	}

	@Nonnull
	public String toString()
	{
		return this.toJSONString();
	}
		
	public void setObjectValue(@Nonnull String name, @Nullable JSONBase val)
	{
		this.objVals.put(name, val);
	}

	public void setObjectInt32(@Nonnull String name, int val)
	{
		this.objVals.put(name, new JSONInt32(val));
	}

	public void setObjectInt64(@Nonnull String name, long val)
	{
		this.objVals.put(name, new JSONInt64(val));
	}

	public void setObjectDouble(@Nonnull String name, double val)
	{
		this.objVals.put(name, new JSONNumber(val));
	}

	public void setObjectString(@Nonnull String name, @Nonnull String val)
	{
		this.objVals.put(name, new JSONString(val));
	}

	public void setObjectBool(@Nonnull String name, boolean val)
	{
		this.objVals.put(name, new JSONBool(val));
	}

	@Nullable
	public JSONBase getObjectValue(@Nonnull String name)
	{
		return this.objVals.get(name);
	}

	@Nullable
	public JSONArray getObjectArray(@Nonnull String name)
	{
		JSONBase o = this.getObjectValue(name);
		if (o != null && o instanceof JSONArray)
			return (JSONArray)o;
		return null;
	}

	@Nullable
	public JSONObject getObjectObject(@Nonnull String name)
	{
		JSONBase o = this.getObjectValue(name);
		if (o != null && o instanceof JSONObject)
			return (JSONObject)o;
		return null;
	}

	@Nonnull
	public Set<String> getObjectNames()
	{
		return this.objVals.keySet();
	}

	@Nullable
	public String getObjectString(@Nonnull String name)
	{
		JSONBase baseObj = this.objVals.get(name);
		if (baseObj == null || !(baseObj instanceof JSONString))
		{
			return null;
		}
		return ((JSONString)baseObj).getValue();
	}

	public double getObjectDouble(@Nonnull String name)
	{
		JSONBase baseObj = this.objVals.get(name);
		if (baseObj == null)
		{
			return 0;
		}
		return baseObj.getAsDouble();
	}

	public int getObjectInt32(@Nonnull String name)
	{
		JSONBase baseObj = this.objVals.get(name);
		if (baseObj == null)
		{
			return 0;
		}
		return baseObj.getAsInt32();
	}

	public long getObjectInt64(@Nonnull String name)
	{
		JSONBase baseObj = this.objVals.get(name);
		if (baseObj == null)
		{
			return 0;
		}
		return baseObj.getAsInt64();
	}

	public boolean getObjectBool(@Nonnull String name)
	{
		JSONBase baseObj = this.objVals.get(name);
		if (baseObj == null)
		{
			return false;
		}
		return baseObj.getAsBool();
	}

	public void removeObject(@Nonnull String name)
	{
		this.objVals.remove(name);
	}

}
