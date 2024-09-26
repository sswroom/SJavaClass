package org.sswr.util.io;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class ConfigFile
{
	private Map<String, Map<String, String>> cfgVals;

	public ConfigFile()
	{
		this.cfgVals = new HashMap<String, Map<String, String>>();
	}

	@Nullable
	public String getValue(@Nonnull String name)
	{
		return getValue("", name);
	}

	@Nullable
	public String getValue(@Nullable String category, @Nonnull String name)
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

	public boolean setValue(@Nullable String category, @Nonnull String name, @Nullable String value)
	{
		Map<String, String> cate;
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

	@Nonnull
	public Set<String> getCateList()
	{
		return this.cfgVals.keySet();
	}

	@Nullable
	public Set<String> getKeys(@Nullable String category)
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

	public boolean hasCategory(@Nullable String category)
	{
		if (category == null)
		{
			category = "";
		}
		return this.cfgVals.containsKey(category);
	}

	@Nullable
	public ConfigFile cloneCate(@Nullable String category)
	{
		ConfigFile cfg = new ConfigFile();
		if (category == null)
		{
			category = "";
		}
		Set<String> cateVal = this.getKeys(category);
		if (cateVal == null)
			return null;
		Iterator<String> itCate = cateVal.iterator();
		while (itCate.hasNext())
		{
			String key = itCate.next();
			cfg.setValue("", key, this.getValue(category, key));
		}
		return cfg;
	}

	@Nonnull
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		Set<String> cates = this.getCateList();
		Iterator<String> itCate = cates.iterator();
		String key;
		Iterator<String> itKey;
		Set<String> keys;
		String cate;
		while (itCate.hasNext())
		{
			cate = itCate.next();
			keys = this.getKeys(cate);
			if (keys != null)
			{
				itKey = keys.iterator();
				while (itKey.hasNext())
				{
					key = itKey.next();
					if (cate.length() > 0)
					{
						sb.append(cate+".");
					}
					sb.append(key);
					sb.append("=");
					sb.append(this.getValue(cate, key));
					sb.append("\r\n");
				}
			}
		}
		return sb.toString();
	}

	@Nonnull
	public static ConfigFile fromSystemProperties()
	{
		return fromProperties(System.getProperties());
	}

	@Nonnull
	public static ConfigFile fromProperties(@Nonnull Properties properties)
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

	@Nonnull
	public ConfigFile merge(@Nonnull ConfigFile cfg)
	{
		Iterator<String> itCates = cfg.getCateList().iterator();
		while (itCates.hasNext())
		{
			String cate = itCates.next();
			Set<String> keys = cfg.getKeys(cate);
			if (keys != null)
			{
				Iterator<String> itKeys = keys.iterator();
				String key;
				while (itKeys.hasNext())
				{
					key = itKeys.next();
					this.setValue(cate, key, cfg.getValue(cate, key));
				}
			}
		}
		return this;
	}
}
