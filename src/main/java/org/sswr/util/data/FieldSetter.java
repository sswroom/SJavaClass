package org.sswr.util.data;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class FieldSetter
{
	private Field field;
	private Method setter;
	public FieldSetter(@Nonnull Field field)
	{
		this.field = field;
		this.setter = ReflectTools.findSetter(field);
		if (this.setter == null && !ReflectTools.isPublic(field.getModifiers()))
		{
			throw new IllegalArgumentException("Setter method of "+field.getName()+" ("+field.getDeclaringClass().getName()+") not found");
		}
	}

	public void set(@Nonnull Object o, @Nullable Object v) throws IllegalAccessException, InvocationTargetException
	{
		if (this.setter != null)
		{
			this.setter.invoke(o, v);
		}
		else
		{
			this.field.set(o, v);
		}
	}

	@Nonnull
	public Field getField()
	{
		return this.field;
	}
}
