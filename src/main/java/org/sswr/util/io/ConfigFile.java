package org.sswr.util.io;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class ConfigFile
{
	private Map<String, Map<String, String>> cfgVals;

	public ConfigFile()
	{
		this.cfgVals = new HashMap<String, Map<String, String>>();
	}

	public String getValue(String name)
	{
		return getValue("", name);
	}

	public String getValue(String category, String name)
	{
		if (category == null)
		{
			category = "";
		}
		Map<String, String> cate = this.cfgVals.get(category);
		if (cate == null)
			return null;
		return cate.get(name);
	}

	public boolean setValue(String category, String name, String value)
	{
		Map<String, String> cate;
		if (name == null)
			return false;
		if (category == null)
		{
			category = "";
		}
		cate = this.cfgVals.get(category);
		if (cate == null)
		{
			cate = new HashMap<String, String>();
			this.cfgVals.put(category, cate);
		}
		cate.put(name, value);
		return true;
	}

	public int getCateCount()
	{
		return this.cfgVals.size();
	}

	public Set<String> getCateList()
	{
		return this.cfgVals.keySet();
	}

	public Set<String> getKeys(String category)
	{
		Map<String, String> cate;
		if (category == null)
		{
			category = "";
		}
		cate = this.cfgVals.get(category);
		if (cate == null)
		{
			return null;
		}
		return cate.keySet();
	}

	public boolean hasCategory(String category)
	{
		if (category == null)
		{
			category = "";
		}
		return this.cfgVals.containsKey(category);
	}

	public ConfigFile cloneCate(String category)
	{
		ConfigFile cfg = new ConfigFile();
		if (category == null)
		{
			category = "";
		}
		Map<String, String> cate = this.cfgVals.get(category);
		Set<String> cateVal = cate.keySet();
		Iterator<String> itCate = cateVal.iterator();
		while (itCate.hasNext())
		{
			String key = itCate.next();
			cfg.setValue("", key, cate.get(key));
		}
		return cfg;
	}
}
