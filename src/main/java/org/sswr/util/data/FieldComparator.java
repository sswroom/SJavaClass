package org.sswr.util.data;

import java.lang.reflect.InvocationTargetException;
import java.util.Comparator;
import java.util.Map;

import org.sswr.util.db.DBColumnInfo;
import org.sswr.util.db.DBUtil;

public class FieldComparator<T> implements Comparator<T>
{
	private String fieldNames[];
	private Object getters[];
	private int dirs[];
	public FieldComparator(Class<?> cls, String compareConds) throws NoSuchFieldException
	{
		String conds[] = compareConds.split(",");
		String cond[];
		getters = new Object[conds.length];
		dirs = new int[conds.length];
		fieldNames = new String[conds.length];
		int i = 0;
		int j = conds.length;
		while (i < j)
		{
			cond = conds[i].trim().split(" ");
			dirs[i] = 1;
			if (cond.length > 2)
			{
				throw new IllegalArgumentException("\""+conds[i]+"\" is not supported");
			}
			else if (cond.length == 2)
			{
				cond[1] = cond[1].toUpperCase();
				if (cond[1].equals("ASC"))
				{
					dirs[i] = 1;
				}
				else if (cond[1].equals("DESC"))
				{
					dirs[i] = -1;
				}
				else
				{
					throw new IllegalArgumentException("\""+conds[i]+"\" is not supported");
				}
			}
			fieldNames[i] = cond[0];
			getters[i] = new FieldGetter<T>(cls, cond[0]);
			i++;
		}
	}

	@Override
	public int compare(T arg0, T arg1)
	{
		try
		{
			int i = 0;
			int j = this.getters.length;
			int k;
			while (i < j)
			{
				@SuppressWarnings("unchecked")
				FieldGetter<T> getter = (FieldGetter<T>)this.getters[i];
				k = DataTools.objectCompare(getter.get(arg0), getter.get(arg1)) * this.dirs[i];
				if (k != 0)
				{
					return k;
				}
				i++;
			}
			return 0;
		}
		catch (IllegalAccessException ex)
		{
			throw new ClassCastException(ex.getMessage());
		}
		catch (InvocationTargetException ex)
		{
			throw new ClassCastException(ex.getMessage());
		}
	}

	public String toOrderClause(Map<String, DBColumnInfo> colsMap, DBUtil.DBType dbType)
	{
		StringBuilder sb = new StringBuilder();
		int i = 0;
		int j  = this.getters.length;
		while (i < j)
		{
			if (i > 0)
			{
				sb.append(", ");
			}
			DBColumnInfo col = colsMap.get(this.fieldNames[i]);
			sb.append(DBUtil.dbCol(dbType, col.colName));
			if (this.dirs[i] == -1)
			{
				sb.append(" desc");
			}
			i++;
		}
		return sb.toString();
	}
}
