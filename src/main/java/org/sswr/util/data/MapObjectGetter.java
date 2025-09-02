package org.sswr.util.data;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import jakarta.annotation.Nonnull;

public class MapObjectGetter implements ObjectGetter
{
	private @Nonnull Map<String, ?> map;

	public MapObjectGetter(@Nonnull Map<String, ?> map)
	{
		this.map = map;
	}

	@Override
	public Object getObjectByName(@Nonnull String name) throws IllegalAccessException, InvocationTargetException {
		return this.map.get(name);
	}
	
}
