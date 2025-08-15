package org.sswr.util.data.cond;

import java.lang.reflect.InvocationTargetException;

import org.sswr.util.data.ObjectGetter;

import jakarta.annotation.Nonnull;

public abstract class BooleanObject extends ConditionObject
{
	public @Nonnull DataType getReturnType() { return DataType.Boolean; }
	public abstract boolean eval(@Nonnull ObjectGetter getter) throws IllegalAccessException, InvocationTargetException;
}
