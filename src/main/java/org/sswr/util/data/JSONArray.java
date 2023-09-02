package org.sswr.util.data;

import java.util.ArrayList;
import java.util.List;

public class JSONArray extends JSONBase
{
	private List<JSONBase> arrVals;
	
	public JSONArray()
	{
		this.arrVals = new ArrayList<JSONBase>();
	}

	public String toJSONString()
	{
		StringBuilder sb = new StringBuilder();
		JSONBase obj;
		int i = 0;
		int j = this.arrVals.size();
		sb.append('[');
		while (i < j)
		{
			if (i > 0)
			{
				sb.append(", ");
			}
			obj = this.arrVals.get(i);
			if (obj != null)
			{
				sb.append(obj.toJSONString());
			}
			else
			{
				sb.append("null");
			}
			i++;
		}
		sb.append(']');
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

	public void setArrayValue(int index, JSONBase val)
	{
		this.arrVals.set(index, val);
		////////////////////////////////////
	}

	public void addArrayValue(JSONBase val)
	{
		this.arrVals.add(val);
	}

	public JSONBase getArrayValue(int index)
	{
		if (index < 0 || index >= this.arrVals.size())
			return null;
		return this.arrVals.get(index);
	}

	public JSONObject getArrayObject(int index)
	{
		JSONBase o = this.getArrayValue(index);
		if (o != null && o instanceof JSONObject)
			return (JSONObject)o;
		return null;
	}

	public double getArrayDouble(int index)
	{
		JSONBase baseObj = this.getArrayValue(index);
		if (baseObj == null)
		{
			return 0;
		}
		return baseObj.getAsDouble();
	}

	public String getArrayString(int index)
	{
		JSONBase baseObj = this.getArrayValue(index);
		if (baseObj == null || !(baseObj instanceof JSONString))
		{
			return null;
		}
		return ((JSONString)baseObj).getValue();
	}

	public int getArrayLength()
	{
		return this.arrVals.size();
	}

	public void removeArrayItem(int index)
	{
		if (index < 0 || index >= this.arrVals.size())
			return;
		this.arrVals.remove(index);
	}
}
