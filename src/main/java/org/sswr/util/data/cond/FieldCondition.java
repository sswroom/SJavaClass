package org.sswr.util.data.cond;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.sswr.util.data.ObjectGetter;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public abstract class FieldCondition extends BooleanObject
{
	protected String fieldName;
	public FieldCondition(@Nonnull String fieldName)
	{
		this.fieldName = fieldName;
	}

	public boolean eval(@Nonnull ObjectGetter getter) throws IllegalAccessException, InvocationTargetException
	{
		Object item = getter.getObjectByName(this.fieldName);
		return this.testValid(item);
	}

	public void getFieldList(@Nonnull List<String> fieldList)
	{
		fieldList.add(fieldName);
	}

	public abstract boolean testValid(@Nullable Object item);
}
