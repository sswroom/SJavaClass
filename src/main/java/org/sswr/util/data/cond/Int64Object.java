package org.sswr.util.data.cond;

import java.util.List;
import java.util.Map;

import org.sswr.util.data.ObjectGetter;
import org.sswr.util.db.DBColumnInfo;
import org.sswr.util.db.DBUtil.DBType;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class Int64Object extends NumberObject
{
	private long val;
	public Int64Object(long val)
	{
		this.val = val;
	}

	public @Nonnull ConditionObject clone()
	{
		return new Int64Object(this.val);
	}

	public @Nonnull String toWhereClause(@Nullable Map<String, DBColumnInfo> colsMap, @Nonnull DBType dbType, byte tzQhr, int maxDBItem)
	{
		return String.valueOf(this.val);
	}

	public boolean canWhereClause(int maxDBItem)
	{
		return true;
	}

	public void getFieldList(@Nonnull List<String> fieldList)
	{
	}

	public @Nonnull NumberType getNumberType(@Nonnull ObjectGetter getter)
	{
		return NumberType.I32;
	}

	public long evalInt(@Nonnull ObjectGetter getter)
	{
		return this.val;
	}

	public double evalDouble(@Nonnull ObjectGetter getter)
	{
		return (double)this.val;
	}
}