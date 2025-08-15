package org.sswr.util.data.cond;

import java.sql.Timestamp;
import java.util.Map;

import org.sswr.util.db.DBColumnInfo;
import org.sswr.util.db.DBUtil;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class TimeBetweenCondition extends FieldCondition
{
	private Timestamp t1;
	private Timestamp t2;

	public TimeBetweenCondition(@Nonnull String fieldName, @Nonnull Timestamp t1, @Nonnull Timestamp t2)
	{
		super(fieldName);
		this.t1 = t1;
		this.t2 = t2;
	}

	public @Nonnull ConditionObject clone()
	{
		return new TimeBetweenCondition(this.fieldName, this.t1, this.t2);
	}

	public @Nonnull String toWhereClause(@Nullable Map<String, DBColumnInfo> colsMap, @Nonnull DBUtil.DBType dbType, byte tzQhr, int maxDbItem)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(DBUtil.dbCol(dbType, toFieldName(colsMap, this.fieldName)));
		sb.append(" between ");
		sb.append(DBUtil.dbTS(dbType, t1));
		sb.append(" and ");
		sb.append(DBUtil.dbTS(dbType, t2));
		return sb.toString();
	}

	public boolean canWhereClause(int maxDBItem)
	{
		return true;
	}

	public boolean testValid(@Nullable Object item)
	{
		if (item == null)
			return false;
		if (item instanceof Timestamp)
		{
			Timestamp t = (Timestamp)item;
			return t.compareTo(t1) >= 0 && t.compareTo(t2) <= 0;
		}
		return false;
	}
}
