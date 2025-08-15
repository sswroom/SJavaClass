package org.sswr.util.data;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import jakarta.annotation.Nonnull;

public class ObjectFieldGetter<T> implements ObjectGetter {
	private T o;
	private HashMap<String, FieldGetter<T>> fieldMap;
	public ObjectFieldGetter(@Nonnull T o)
	{
		this.o = o;
		this.fieldMap = new HashMap<String, FieldGetter<T>>();
	}
	@Override
	public Object getObjectByName(@Nonnull String name) throws IllegalAccessException, InvocationTargetException{
		FieldGetter<T> getter;
		getter = this.fieldMap.get(name);
		if (getter == null)
		{
			try
			{
				getter = new FieldGetter<T>(this.o.getClass(), name);
				this.fieldMap.put(name, getter);
			}
			catch (NoSuchFieldException ex)
			{
				return new IllegalAccessException("No such field: "+ex.getMessage());
			}
		}
		return getter.get(this.o);
	}
}
