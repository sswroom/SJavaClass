package org.sswr.util.data.cond;

import java.util.Map;

import org.sswr.util.basic.CompareCondition;
import org.sswr.util.db.DBColumnInfo;
import org.sswr.util.db.DBUtil;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.persistence.EnumType;

public class EnumCondition extends FieldCondition
{
	private Enum<?> val;
	private EnumType enumType;
	private CompareCondition cond;

	public EnumCondition(@Nonnull String fieldName, @Nonnull Enum<?> val, @Nonnull EnumType enumType, @Nonnull CompareCondition cond)
	{
		super(fieldName);
		this.val = val;
		this.enumType = enumType;
		this.cond = cond;
		if (cond != CompareCondition.Equal && cond != CompareCondition.NotEqual)
		{
			throw new IllegalArgumentException(cond.name()+" is not supported");
		}
	}

	public @Nonnull ConditionObject clone()
	{
		return new EnumCondition(fieldName, val, enumType, cond);
	}

	@Nonnull
	public String toWhereClause(@Nullable Map<String, DBColumnInfo> colsMap, @Nonnull DBUtil.DBType dbType, byte tzQhr, int maxDbItem)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(DBUtil.dbCol(dbType, toFieldName(colsMap, fieldName)));
		if (this.cond == CompareCondition.Equal)
		{
			if (val == null)
			{
				sb.append(" is null");
			}
			else
			{
				sb.append(" = ");
				if (this.enumType == EnumType.STRING)
				{
					sb.append(DBUtil.dbStr(dbType, this.val.name()));
				}
				else
				{
					sb.append(this.val.ordinal());
				}
			}
		}
		else if (this.cond == CompareCondition.NotEqual)
		{
			if (val == null)
			{
				sb.append(" is not null");
			}
			else
			{
				sb.append(" <> ");
				if (this.enumType == EnumType.STRING)
				{
					sb.append(DBUtil.dbStr(dbType, this.val.name()));
				}
				else
				{
					sb.append(this.val.ordinal());
				}
			}
		}
		return sb.toString();
	}

	public boolean canWhereClause(int maxDBItem)
	{
		return true;
	}

	public boolean testValid(@Nullable Object v)
	{
		if (this.val == null)
		{
			if (this.cond == CompareCondition.Equal)
			{
				return v == null;
			}
			else
			{
				return v != null;
			}
		}
		else if (this.cond == CompareCondition.Equal)
		{
			return v == this.val;
		}
		else
		{
			return v != this.val;
		}
	}
}
