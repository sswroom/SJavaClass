package org.sswr.util.data.cond;

import java.util.Map;

import org.sswr.util.data.StringBuilderUTF8;
import org.sswr.util.data.VariItemUtil;
import org.sswr.util.db.DBColumnInfo;
import org.sswr.util.db.DBUtil;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class BooleanCondition extends FieldCondition
{
	private boolean val;

	public BooleanCondition(@Nonnull String fieldName, boolean val)
	{
		super(fieldName);
		this.val = val;
	}

	public @Nonnull ConditionObject clone()
	{
		return new BooleanCondition(fieldName, val);
	}

	public @Nonnull String toWhereClause(@Nullable Map<String, DBColumnInfo> colsMap, @Nonnull DBUtil.DBType dbType, byte tzQhr, int maxDbItem)
	{
		StringBuilderUTF8 sb = new StringBuilderUTF8();
		if (!val)
		{
			sb.append("NOT ");
		}
		sb.append(DBUtil.dbCol(dbType, toFieldName(colsMap, fieldName)));
		return sb.toString();
	}

	public boolean canWhereClause(int maxDbItem)
	{
		return true;
	}

	public boolean testValid(@Nullable Object v)
	{
		return VariItemUtil.asBool(v) == this.val;
	}
}
