package org.sswr.util.data.cond;

import java.lang.reflect.InvocationTargetException;

import org.sswr.util.data.ObjectGetter;

import jakarta.annotation.Nonnull;

public abstract class NumberObject extends ConditionObject
{
	public @Nonnull DataType getReturnType() { return DataType.Number; }
	public abstract @Nonnull NumberType getNumberType(@Nonnull ObjectGetter getter) throws IllegalAccessException, InvocationTargetException;
	public abstract long evalInt(@Nonnull ObjectGetter getter) throws IllegalAccessException, InvocationTargetException;
	public abstract double evalDouble(@Nonnull ObjectGetter getter) throws IllegalAccessException, InvocationTargetException;
}
