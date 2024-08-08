package org.sswr.util.data;

import java.util.ArrayList;
import java.util.List;

public class JSONBuilder
{
	public enum ObjectType
	{
		OT_OBJECT,
		OT_ARRAY,
		OT_END
	}

	private StringBuilder sb;
	private List<ObjectType> objTypes;
	private ObjectType currType;
	private boolean isFirst;

	private void appendStr(String val)
	{
		char carr[] = val.toCharArray();
		char c;
		int i = 0;
		int j = carr.length;
		this.sb.append('\"');
		while (i < j)
		{
			c = carr[i];
			if (c == '\\')
			{
				this.sb.append('\\');
				this.sb.append('\\');
			}
			else if (c == '\"')
			{
				this.sb.append('\\');
				this.sb.append('\"');
			}
			else if (c == '\r')
			{
				this.sb.append('\\');
				this.sb.append('r');
			}
			else if (c == '\n')
			{
				this.sb.append('\\');
				this.sb.append('n');
			}
			else
			{
				this.sb.append(c);
			}
			i++;
		}
		this.sb.append('\"');
	}

	public JSONBuilder(ObjectType rootType)
	{
		this.objTypes = new ArrayList<ObjectType>();
		this.sb = new StringBuilder();
		this.currType = rootType;
		this.isFirst = true;
		if (rootType == ObjectType.OT_ARRAY)
		{
			this.sb.append('[');
		}
		else
		{
			this.sb.append('{');
		}
	}

	public boolean arrayAddFloat64(double val)
	{
		if (this.currType != ObjectType.OT_ARRAY)
			return false;
		if (this.isFirst)
			this.isFirst = false;
		else
		{
			this.sb.append(",");
		}
		this.sb.append(val);
		return true;
	}

	public boolean arrayAddInt32(int val)
	{
		if (this.currType != ObjectType.OT_ARRAY)
			return false;
		if (this.isFirst)
			this.isFirst = false;
		else
		{
			this.sb.append(",");
		}
		this.sb.append(val);
		return true;
	}

	public boolean arrayAddInt64(long val)
	{
		if (this.currType != ObjectType.OT_ARRAY)
			return false;
		if (this.isFirst)
			this.isFirst = false;
		else
		{
			this.sb.append(",");
		}
		this.sb.append(val);
		return true;
	}

	public boolean arrayAddStr(String val)
	{
		if (this.currType != ObjectType.OT_ARRAY)
			return false;
		if (this.isFirst)
			this.isFirst = false;
		else
		{
			this.sb.append(",");
		}
		if (val == null)
			this.sb.append("null");
		else
			this.appendStr(val);
		return true;
	}

	public boolean arrayBeginObject()
	{
		if (this.currType != ObjectType.OT_ARRAY)
			return false;
		if (this.isFirst)
			this.isFirst = false;
		else
		{
			this.sb.append(",");
		}
		this.objTypes.add(ObjectType.OT_ARRAY);
		this.currType = ObjectType.OT_OBJECT;
		this.isFirst = true;
		this.sb.append('{');
		return true;
	}

	public boolean arrayBeginArray()
	{
		if (this.currType != ObjectType.OT_ARRAY)
			return false;
		if (this.isFirst)
			this.isFirst = false;
		else
		{
			this.sb.append(",");
		}
		this.objTypes.add(ObjectType.OT_ARRAY);
		this.currType = ObjectType.OT_ARRAY;
		this.isFirst = true;
		this.sb.append('[');
		return true;		
	}

	public boolean arrayEnd()
	{
		if (this.currType != ObjectType.OT_ARRAY)
			return false;
		int i = this.objTypes.size();
		if (i <= 0)
		{
			this.currType = ObjectType.OT_END;
			return true;
		}
		this.currType = this.objTypes.remove(i - 1);
		this.isFirst = false;
		this.sb.append(']');
		return true;
	}

	public boolean objectAddFloat64(String name, double val)
	{
		if (this.currType != ObjectType.OT_OBJECT)
			return false;
		if (this.isFirst)
			this.isFirst = false;
		else
		{
			this.sb.append(",");
		}
		this.appendStr(name);
		this.sb.append(":");
		this.sb.append(val);
		return true;
	}

	public boolean objectAddInt32(String name, int val)
	{
		if (this.currType != ObjectType.OT_OBJECT)
			return false;
		if (this.isFirst)
			this.isFirst = false;
		else
		{
			this.sb.append(",");
		}
		this.appendStr(name);
		this.sb.append(":");
		this.sb.append(val);
		return true;
	}

	public boolean objectAddInt64(String name, long val)
	{
		if (this.currType != ObjectType.OT_OBJECT)
			return false;
		if (this.isFirst)
			this.isFirst = false;
		else
		{
			this.sb.append(",");
		}
		this.appendStr(name);
		this.sb.append(":");
		this.sb.append(val);
		return true;
	}

	public boolean objectAddStr(String name, String val)
	{
		if (this.currType != ObjectType.OT_OBJECT)
			return false;
		if (this.isFirst)
			this.isFirst = false;
		else
		{
			this.sb.append(",");
		}
		this.appendStr(name);
		this.sb.append(":");
		if (val == null)
		{
			this.sb.append("null");
		}
		else
		{
			this.appendStr(val);
		}
		return true;
	}

	public boolean objectAddArrayStr(String name, String value, String splitChar)
	{
		if (this.currType != ObjectType.OT_OBJECT)
			return false;
		if (this.isFirst)
			this.isFirst = false;
		else
		{
			this.sb.append(",");
		}
		this.appendStr(name);
		this.sb.append(':');
		this.sb.append('[');
		if (value.length() > 0)
		{
			String[] strs = StringUtil.split(value, splitChar);
			int i = 0;
			int j = strs.length;
			while (i < j)
			{
				if (i > 0)
					this.sb.append(",");
				this.appendStr(strs[i]);
				i++;
			}
		}
		this.sb.append(']');
		return true;
	}

	public boolean objectBeginArray(String name)
	{
		if (this.currType != ObjectType.OT_OBJECT)
			return false;
		if (this.isFirst)
			this.isFirst = false;
		else
		{
			this.sb.append(",");
		}
		this.appendStr(name);
		this.sb.append(":[");
		this.objTypes.add(ObjectType.OT_OBJECT);
		this.currType = ObjectType.OT_ARRAY;
		this.isFirst = true;
		return true;
	}

	public boolean objectBeginObject(String name)
	{
		if (this.currType != ObjectType.OT_OBJECT)
			return false;
		if (this.isFirst)
			this.isFirst = false;
		else
		{
			this.sb.append(",");
		}
		this.appendStr(name);
		this.sb.append(":{");
		this.objTypes.add(ObjectType.OT_OBJECT);
		this.currType = ObjectType.OT_OBJECT;
		this.isFirst = true;
		return true;
	}

	public boolean objectEnd()
	{
		if (this.currType != ObjectType.OT_OBJECT)
			return false;
		int i = this.objTypes.size();
		if (i <= 0)
		{
			this.currType = ObjectType.OT_END;
			return true;
		}
		this.currType = this.objTypes.remove(i - 1);
		this.isFirst = false;
		this.sb.append('}');
		return true;

	}

	public void endBuild()
	{
		int i;
		if (this.currType == ObjectType.OT_ARRAY)
		{
			this.sb.append(']');
		}
		else if (this.currType == ObjectType.OT_OBJECT)
		{
			this.sb.append('}');
		}
		i = this.objTypes.size();
		while (i-- > 0)
		{
			if (this.objTypes.get(i) == ObjectType.OT_OBJECT)
			{
				this.sb.append('}');
			}
			else
			{
				this.sb.append(']');
			}
		}
		this.currType = ObjectType.OT_END;		
	}

	public String toString()
	{
		this.endBuild();
		return this.sb.toString();
	}
}
