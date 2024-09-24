package org.sswr.util.basic;

import java.util.HashMap;
import java.util.Map;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class ThreadVar
{
    private static final ThreadLocal<Map<String, Object>> varHolder = new ThreadLocal<>();

	private static @Nonnull Map<String, Object> getVarMap()
	{
		Map<String, Object> varMap = varHolder.get();
		if (varMap == null)
		{
			varMap = new HashMap<String, Object>();
			varHolder.set(varMap);
		}
		return varMap;
	}
	public static void set(@Nullable String name, @Nullable Object val)
	{
		Map<String, Object> varMap = getVarMap();
        varMap.put(name, val);
    }

    public static @Nullable Object get(String name)
	{
		Map<String, Object> varMap = getVarMap();
        return varMap.get(name);
    }
}
