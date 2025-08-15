package org.sswr.util.data.cond;

import java.util.Iterator;
import java.util.Map;

import org.sswr.util.data.DataTools;
import org.sswr.util.db.DBColumnInfo;
import org.sswr.util.db.DBUtil;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class Int32InCondition extends FieldCondition
{
	private Iterable<Integer> vals;

	public Int32InCondition(@Nonnull String fieldName, @Nonnull Iterable<Integer> vals)
	{
		super(fieldName);
		this.vals = vals;
	}

	public @Nonnull ConditionObject clone()
	{
		return new Int32InCondition(this.fieldName, this.vals);
	}

	
	public @Nonnull String toWhereClause(@Nullable Map<String, DBColumnInfo> colsMap, @Nonnull DBUtil.DBType dbType, byte tzQhr, int maxDbItem) throws IllegalAccessException
	{
		if (DataTools.getSize(vals) > maxDbItem)
		{
			throw new IllegalAccessException("Too many fields");
		}
		else
		{
			StringBuilder sb = new StringBuilder();
			sb.append(DBUtil.dbCol(dbType, toFieldName(colsMap, fieldName)));
			sb.append(" in (");
			sb.append(DataTools.intJoin(vals, ", "));
			sb.append(")");
			return sb.toString();
		}
	}

	public boolean canWhereClause(int maxDBItem)
	{
		return DataTools.getSize(this.vals) <= maxDBItem;
	}

	public boolean testValid(@Nullable Object item)
	{
		long iVal;
		if (item == null)
		{
			return false;
		}

		if (item instanceof Float)
		{
			iVal = (long)((Float)item).floatValue();
		}
		else if (item instanceof Double)
		{
			iVal = (long)((Double)item).floatValue();
		}
		else if (item instanceof Byte)
		{
			iVal = ((Byte)item).byteValue();
		}
		else if (item instanceof Integer)
		{
			iVal = ((Integer)item).intValue();
		}
		else if (item instanceof Long)
		{
			iVal = ((Long)item).intValue();
		}
		else
		{
			System.out.println("Int32InCondition: Unsupported type: "+item.getClass().toString());
			return false;
		}
		Iterator<Integer> it = this.vals.iterator();
		while (it.hasNext())
		{
			if (iVal == it.next().intValue())
			{
				return true;
			}
		}
		return false;
	}
}
