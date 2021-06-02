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

	public JSType getJSType()
	{
		return JSType.ARRAY;
	}

	public void toJSONString(StringBuilder sb)
	{
		JSONBase obj;
		int i = 0;
		int j = this.arrVals.size();
		sb.append("[");
		while (i < j)
		{
			if (i > 0)
			{
				sb.append(",");
			}
			obj = this.arrVals.get(i);
			if (obj != null)
			{
				obj.toJSONString(sb);
			}
			else
			{
				sb.append("null");
			}
			i++;
		}
		sb.append("]");
	}

	public boolean equals(String s)
	{
		return false;
	}

	public boolean identical(JSONBase obj)
	{
		return this == obj;
	}

	public void setArrayValue(int index, JSONBase val)
	{
		this.arrVals.set(index, val);
	}

	public void addArrayValue(JSONBase val)
	{
		this.arrVals.add(val);
	}

	public JSONBase getArrayValue(int index)
	{
		return this.arrVals.get(index);
	}
	
	public int getArrayLength()
	{
		return this.arrVals.size();
	}
}
