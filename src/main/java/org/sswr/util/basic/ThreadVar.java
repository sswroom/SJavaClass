package org.sswr.util.basic;

import java.util.HashMap;
import java.util.Map;

public class ThreadVar
{
    private static final ThreadLocal<Map<String, Object>> varHolder = new ThreadLocal<>();

	private static Map<String, Object> getVarMap()
	{
		Map<String, Object> varMap = varHolder.get();
		if (varMap == null)
		{
			varMap = new HashMap<String, Object>();
			varHolder.set(varMap);
		}
		return varMap;
	}
	public static void set(String name, Object val)
	{
		Map<String, Object> varMap = getVarMap();
        varMap.put(name, val);
    }

    public static Object get(String name)
	{
		Map<String, Object> varMap = getVarMap();
        return varMap.get(name);
    }
}
