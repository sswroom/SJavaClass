package org.sswr.util.data.cond;

import java.lang.reflect.InvocationTargetException;
import java.sql.Timestamp;

import org.sswr.util.data.ObjectGetter;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public abstract class TimeObject extends ConditionObject
{
	public @Nonnull DataType getReturnType() { return DataType.Time; }
	public abstract @Nullable Timestamp eval(@Nonnull ObjectGetter getter) throws IllegalAccessException, InvocationTargetException;
}
