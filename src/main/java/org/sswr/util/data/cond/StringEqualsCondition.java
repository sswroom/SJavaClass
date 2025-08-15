package org.sswr.util.data.cond;

import java.util.Map;

import org.sswr.util.data.VariItemUtil;
import org.sswr.util.db.DBColumnInfo;
import org.sswr.util.db.DBUtil;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class StringEqualsCondition extends FieldCondition
{
	private String val;

	public StringEqualsCondition(@Nonnull String fieldName, @Nonnull String val)
	{
		super(fieldName);
		this.val = val;
	}

	public @Nonnull ConditionObject clone()
	{
		return new StringEqualsCondition(this.fieldName, this.val);
	}

	public @Nonnull String toWhereClause(@Nullable Map<String, DBColumnInfo> colsMap, @Nonnull DBUtil.DBType dbType, byte tzQhr, int maxDbItem)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(DBUtil.dbCol(dbType, toFieldName(colsMap, this.fieldName)));
		sb.append(" = ");
		sb.append(DBUtil.dbStr(dbType, this.val));
		return sb.toString();
	}

	public boolean canWhereClause(int maxDBItem)
	{
		return true;
	}

	public boolean testValid(@Nullable Object item)
	{
		if (item == null)
		{
			return false;
		}
		String sVal = VariItemUtil.asStr(item);
		return sVal.equals(this.val);
	}
}