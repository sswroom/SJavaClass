package org.sswr.util.data;

import java.lang.reflect.InvocationTargetException;

import jakarta.annotation.Nonnull;

public interface ObjectGetter
{
	public Object getObjectByName(@Nonnull String name) throws IllegalAccessException, InvocationTargetException;
}
