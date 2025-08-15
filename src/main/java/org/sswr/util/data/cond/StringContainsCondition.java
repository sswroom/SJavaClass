package org.sswr.util.data.cond;

import java.util.Map;

import org.sswr.util.data.StringBuilderUTF8;
import org.sswr.util.data.VariItemUtil;
import org.sswr.util.db.DBColumnInfo;
import org.sswr.util.db.DBUtil;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class StringContainsCondition extends FieldCondition
{
	private String val;

	public StringContainsCondition(@Nonnull String fieldName, @Nonnull String val)
	{
		super(fieldName);
		this.val = val;
	}

	public @Nonnull ConditionObject clone()
	{
		return new StringContainsCondition(this.fieldName, this.val);
	}

	@Nonnull
	public String toWhereClause(@Nullable Map<String, DBColumnInfo> colsMap, @Nonnull DBUtil.DBType dbType, byte tzQhr, int maxDbItem)
	{
		StringBuilderUTF8 sb = new StringBuilderUTF8();
		sb.append(DBUtil.dbCol(dbType, toFieldName(colsMap, this.fieldName)));
		sb.append(" like ");
		sb.append(DBUtil.dbStr(dbType, "%"+this.val+"%"));
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
		return sVal.indexOf(this.val) >= 0;
	}


}