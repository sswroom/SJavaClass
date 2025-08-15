package org.sswr.util.data.cond;

import java.util.Map;

import org.sswr.util.data.StringBuilderUTF8;
import org.sswr.util.db.DBColumnInfo;
import org.sswr.util.db.DBUtil;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class NullCondition extends FieldCondition
{
	public NullCondition(@Nonnull String fieldName)
	{
		super(fieldName);
	}

	public @Nonnull ConditionObject clone()
	{
		return new NullCondition(fieldName);
	}

	public @Nonnull String toWhereClause(@Nullable Map<String, DBColumnInfo> colsMap, @Nonnull DBUtil.DBType dbType, byte tzQhr, int maxDbItem)
	{
		StringBuilderUTF8 sb = new StringBuilderUTF8();
		sb.append(DBUtil.dbCol(dbType, toFieldName(colsMap, fieldName)));
		sb.append(" is null");
		return sb.toString();
	}

	public boolean canWhereClause(int maxDbItem)
	{
		return true;
	}

	public boolean testValid(@Nullable Object v)
	{
		return v == null;
	}
}