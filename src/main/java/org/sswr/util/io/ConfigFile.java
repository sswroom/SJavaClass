package org.sswr.util.io;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
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

	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		Set<String> cates = this.cfgVals.keySet();
		Map<String, String> vals;
		Iterator<String> itCate = cates.iterator();
		String key;
		Iterator<String> itKey;
		String cate;
		while (itCate.hasNext())
		{
			cate = itCate.next();
			vals = this.cfgVals.get(cate);
			itKey = vals.keySet().iterator();
			while (itKey.hasNext())
			{
				key = itKey.next();
				if (cate.length() > 0)
				{
					sb.append(cate+".");
				}
				sb.append(key);
				sb.append("=");
				sb.append(vals.get(key));
				sb.append("\r\n");
			}
		}
		return sb.toString();
	}

	public static ConfigFile fromSystemProperties()
	{
		return fromProperties(System.getProperties());
	}

	public static ConfigFile fromProperties(Properties properties)
	{
		ConfigFile cfg = new ConfigFile();
		Iterator<Object> itKeys = properties.keySet().iterator();
		Object key;
		while (itKeys.hasNext())
		{
			key = itKeys.next();
			cfg.setValue(null, key.toString(), properties.get(key).toString());
		}
		return cfg;
	}

	public ConfigFile merge(ConfigFile cfg)
	{
		Iterator<String> itCates = cfg.cfgVals.keySet().iterator();
		while (itCates.hasNext())
		{
			String cate = itCates.next();
			Map<String, String> cfgCate = cfg.cfgVals.get(cate);
			Iterator<String> itKeys = cfgCate.keySet().iterator();
			String key;
			while (itKeys.hasNext())
			{
				key = itKeys.next();
				this.setValue(cate, key, cfgCate.get(key));
			}
		}
		return this;
	}
}
