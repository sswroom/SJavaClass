package org.sswr.util.data.cond;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

import org.sswr.util.data.ObjectGetter;
import org.sswr.util.data.VariItemUtil;
import org.sswr.util.db.DBColumnInfo;
import org.sswr.util.db.DBUtil;
import org.sswr.util.db.DBUtil.DBType;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class NumberField extends NumberObject
{
	private String fieldName;
	public NumberField(@Nonnull String fieldName)
	{
		this.fieldName = fieldName;
	}

	public @Nonnull ConditionObject clone()
	{
		return new NumberField(this.fieldName);
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

	public @Nonnull NumberType getNumberType(@Nonnull ObjectGetter getter) throws IllegalAccessException, InvocationTargetException
	{
		Object item = getter.getObjectByName(this.fieldName);
		return toNumberType(item);
	}

	public long evalInt(@Nonnull ObjectGetter getter) throws IllegalAccessException, InvocationTargetException
	{
		Object item = getter.getObjectByName(this.fieldName);
		if (item == null)
		{
			throw new IllegalAccessException("Returning null");
		}
		return VariItemUtil.asI64(item);
	}

	public double evalDouble(@Nonnull ObjectGetter getter) throws IllegalAccessException, InvocationTargetException
	{
		Object item = getter.getObjectByName(this.fieldName);
		if (item == null)
		{
			throw new IllegalAccessException("Returning null");
		}
		return VariItemUtil.asF64(item);
	}

	public static @Nonnull NumberType toNumberType(@Nullable Object item)
	{
		if ((item instanceof Double) || (item instanceof Float))
			return NumberType.F64;
		if ((item instanceof Byte) ||
			(item instanceof Integer) ||
			(item instanceof Boolean))
			return NumberType.I32;
		if (item instanceof Long)
			return NumberType.I64;
		return NumberType.Null;
	}
}
