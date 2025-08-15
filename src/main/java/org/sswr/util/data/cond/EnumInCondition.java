package org.sswr.util.data.cond;

import java.util.Iterator;
import java.util.Map;

import org.sswr.util.data.DataTools;
import org.sswr.util.db.DBColumnInfo;
import org.sswr.util.db.DBUtil;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.persistence.EnumType;

public class EnumInCondition extends FieldCondition
{
	private Iterable<Enum<?>> vals;
	private EnumType enumType;

	public EnumInCondition(@Nonnull String fieldName, @Nonnull Iterable<Enum<?>> vals, @Nonnull EnumType enumType)
	{
		super(fieldName);
		this.vals = vals;
		this.enumType = enumType;
	}

	public @Nonnull ConditionObject clone()
	{
		return new EnumInCondition(fieldName, vals, enumType);
	}

	public @Nonnull String toWhereClause(@Nullable Map<String, DBColumnInfo> colsMap, @Nonnull DBUtil.DBType dbType, byte tzQhr, int maxDbItem)
	{
		if (DataTools.getSize(this.vals) > maxDbItem)
		{
			return "";
		}
		else
		{
			StringBuilder sb = new StringBuilder();
			sb.append(DBUtil.dbCol(dbType, toFieldName(colsMap, fieldName)));
			Iterator<Enum<?>> it = vals.iterator();
			if (!it.hasNext())
			{
				sb.append(" is null");
			}
			else
			{
				sb.append(" in (");
				if (this.enumType == EnumType.STRING)
				{
					sb.append(DBUtil.dbStr(dbType, it.next().name()));
					while (it.hasNext())
					{
						sb.append(", ");
						sb.append(DBUtil.dbStr(dbType, it.next().name()));
					}
				}
				else
				{
					sb.append(it.next().ordinal());
					while (it.hasNext())
					{
						sb.append(", ");
						sb.append(it.next().ordinal());
					}
				}
				sb.append(")");
			}

			return sb.toString();
		}
	}

	public boolean canWhereClause(int maxDBItem)
	{
		return DataTools.getSize(this.vals) <= maxDBItem;
	}

	public boolean testValid(@Nullable Object v)
	{
		Iterator<Enum<?>> it = vals.iterator();
		if (v == null)
		{
			return !it.hasNext();
		}
		else
		{
			while (it.hasNext())
			{
				if (it.next() == v)
				{
					return true;
				}
			}
			return false;
		}
	}
}