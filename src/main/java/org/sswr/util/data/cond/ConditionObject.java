package org.sswr.util.data.cond;

import java.util.List;
import java.util.Map;

import org.sswr.util.db.DBColumnInfo;
import org.sswr.util.db.DBUtil;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public abstract class ConditionObject
{
	public static String toFieldName(@Nullable Map<String, DBColumnInfo> colsMap, @Nonnull String fieldName)
	{
		DBColumnInfo col;
		if (colsMap != null && (col = colsMap.get(fieldName)) != null)
		{
			return col.colName;
		}
		else
		{
			return fieldName;
		}
	}

	public abstract @Nonnull DataType getReturnType();
	public abstract @Nonnull ConditionObject clone();
	public abstract @Nonnull String toWhereClause(@Nullable Map<String, DBColumnInfo> colsMap, @Nonnull DBUtil.DBType dbType, byte tzQhr, int maxDbItem) throws IllegalAccessException;
	public abstract boolean canWhereClause(int maxDBItem);
	public abstract void getFieldList(@Nonnull List<String> fieldList);
}