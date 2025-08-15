package org.sswr.util.data.cond;

import java.lang.reflect.InvocationTargetException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import org.sswr.util.data.ObjectGetter;
import org.sswr.util.data.VariItemUtil;
import org.sswr.util.db.DBColumnInfo;
import org.sswr.util.db.DBUtil;
import org.sswr.util.db.DBUtil.DBType;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class TimeField extends TimeObject
{
	private String fieldName;

	public TimeField(@Nonnull String fieldName)
	{
		this.fieldName = fieldName;
	}

	public @Nonnull ConditionObject clone()
	{
		return new TimeField(this.fieldName);
	}

	public @Nonnull String toWhereClause(@Nullable Map<String, DBColumnInfo> colsMap, @Nonnull DBType dbType, byte tzQhr, int maxDBItem)
	{
		return DBUtil.dbCol(dbType, toFieldName(colsMap, this.fieldName));
	}

	public boolean canWhereClause(int maxDBItem)
	{
		return true;
	}

	public void getFieldList(@Nonnull List<String> fieldList)
	{
		fieldList.add(this.fieldName);
	}

	public @Nullable Timestamp eval(@Nonnull ObjectGetter getter) throws IllegalAccessException, InvocationTargetException
	{
		Object item = getter.getObjectByName(this.fieldName);
		if (item == null)
		{
			throw new IllegalAccessException("Returning null");
		}
		return VariItemUtil.asTimestamp(item);
	}
}
