package org.sswr.util.data.cond;

import java.lang.reflect.InvocationTargetException;

import org.sswr.util.data.ObjectGetter;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public abstract class StringObject extends ConditionObject
{
	public @Nonnull DataType getReturnType() { return DataType.String; }
	public abstract @Nullable String eval(@Nonnull ObjectGetter getter) throws IllegalAccessException, InvocationTargetException;
}