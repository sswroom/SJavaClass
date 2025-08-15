package org.sswr.util.data.cond;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import org.sswr.util.data.ObjectGetter;
import org.sswr.util.db.DBColumnInfo;
import org.sswr.util.db.DBUtil;
import org.sswr.util.db.DBUtil.DBType;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class TimestampObject extends TimeObject
{
	private Timestamp val;
	public TimestampObject(@Nonnull Timestamp val)
	{
		this.val = val;
	}

	public @Nonnull ConditionObject clone()
	{
		return new TimestampObject(this.val);
	}

	public @Nonnull String toWhereClause(@Nullable Map<String, DBColumnInfo> colsMap, @Nonnull DBType dbType, byte tzQhr, int maxDBItem)
	{
		return DBUtil.dbTS(dbType, this.val);
	}

	public boolean canWhereClause(int maxDBItem)
	{
		return true;
	}

	public void getFieldList(@Nonnull List<String> fieldList)
	{
	}

	public @Nullable Timestamp eval(@Nonnull ObjectGetter getter)
	{
		return this.val;
	}
}