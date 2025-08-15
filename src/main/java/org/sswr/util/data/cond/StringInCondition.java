package org.sswr.util.data.cond;

import java.util.Iterator;
import java.util.Map;

import org.sswr.util.data.DataTools;
import org.sswr.util.data.StringBuilderUTF8;
import org.sswr.util.data.VariItemUtil;
import org.sswr.util.db.DBColumnInfo;
import org.sswr.util.db.DBUtil;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class StringInCondition extends FieldCondition
{
	private Iterable<String> vals;

	public StringInCondition(@Nonnull String fieldName, @Nonnull Iterable<String> vals)
	{
		super(fieldName);
		this.vals = vals;
	}

	public @Nonnull ConditionObject clone()
	{
		return new StringInCondition(this.fieldName, this.vals);
	}

	public @Nonnull String toWhereClause(@Nullable Map<String, DBColumnInfo> colsMap, @Nonnull DBUtil.DBType dbType, byte tzQhr, int maxDbItem)
	{
		if (DataTools.getSize(this.vals) > maxDbItem)
		{
			throw new IllegalAccessError("Too many items");
		}
		else
		{
			StringBuilderUTF8 sb = new StringBuilderUTF8();
			sb.append(DBUtil.dbCol(dbType, toFieldName(colsMap, fieldName)));
			Iterator<String> it = this.vals.iterator();
			if (it.hasNext())
			{
				sb.append(" in (");
				sb.append(DBUtil.dbStr(dbType, it.next()));
				while (it.hasNext())
				{
					sb.append(", ");
					sb.append(DBUtil.dbStr(dbType, it.next()));
				}
				sb.append(")");
			}
			return sb.toString();
		}
	}

	public boolean canWhereClause(int maxDBItem)
	{
		int cnt = DataTools.getSize(this.vals);
		return cnt > 0 && cnt <= maxDBItem;
	}

	public boolean testValid(@Nullable Object item)
	{
		if (item == null)
		{
			return false;
		}
		String sVal = VariItemUtil.asStr(item);
		Iterator<String> it = this.vals.iterator();
		while (it.hasNext())
		{
			if (sVal.equals(it.next()))
			{
				return true;
			}
		}
		return false;
	}
}
