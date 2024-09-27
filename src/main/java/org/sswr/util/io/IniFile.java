package org.sswr.util.io;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class IniFile
{
	@Nonnull
	public static ConfigFile parse(@Nonnull InputStream stm, @Nonnull Charset charset) throws IOException
	{
		Reader reader = new InputStreamReader(stm, charset);
		ConfigFile cfg = parseReader(reader);
		reader.close();
		if (cfg == null)
			throw new IOException("Config content empty");
		return cfg;
	}

	@Nonnull
	public static ConfigFile parse(String fileName, Charset charset) throws IOException
	{
		Reader reader = new FileReader(FileUtil.getRealPath(fileName, false), charset);
		ConfigFile cfg = parseReader(reader);
		reader.close();
		if (cfg == null)
			throw new IOException("Config content empty");
		return cfg;
	}

	@Nullable
	private static ConfigFile parseReader(@Nonnull Reader reader) throws IOException
	{
		ConfigFile cfg = new ConfigFile();
		String cate = null;
		String s;
		int i;
		BufferedReader r = new BufferedReader(reader);
		while ((s = r.readLine()) != null)
		{
			s.trim();
			if (s.startsWith("[") && s.endsWith("]"))
			{
				cate = s.substring(1, s.length() - 1);
			}
			else
			{
				i = s.indexOf("=");
				if (i >= 0)
				{
					String key = s.substring(0, i);
					String val = s.substring(i + 1);
					i = 0;
					while ((i = val.indexOf("\\", i)) >= 0)
					{
						if (i + 1 < val.length())
						{
							char c = val.charAt(i + 1);
							if (c == 'r')
							{
								val = val.substring(0, i) + "\r" + val.substring(i + 2);
							}
							else if (c == 'n')
							{
								val = val.substring(0, i) + "\n" + val.substring(i + 2);
							}
							else if (c == 't')
							{
								val = val.substring(0, i) + "\t" + val.substring(i + 2);
							}
							else
							{
								val = val.substring(0, i) + c + val.substring(i + 2);
							}
							i++;
						}
					}
					cfg.setValue(cate, key, val);
				}
			}
		}
		r.close();
		if (cfg.getCateCount() == 0)
		{
			return null;
		}
		return cfg;
	}

	public static boolean saveConfig(@Nonnull OutputStream stm, @Nonnull Charset charset, @Nonnull ConfigFile cfg) throws IOException
	{
		return saveConfig(new OutputStreamWriter(stm, charset), cfg);
	}

	public static boolean saveConfig(@Nonnull Writer writer, @Nonnull ConfigFile cfg) throws IOException
	{
		Set<String> keys;
		String key;
		keys = cfg.getKeys(null);
		Iterator<String> itKey;
		if (keys != null)
		{
			itKey = keys.iterator();
			while (itKey.hasNext())
			{
				key = itKey.next();
				writer.write(key);
				writer.write("=");
				writer.write(cfg.getValue(key)+"\r\n");
			}
		}
		Iterator<String> itCate = cfg.getCateList().iterator();
		String cate;
		while (itCate.hasNext())
		{
			cate = itCate.next();
			writer.write("\r\n");
			writer.write("[");
			writer.write(cate);
			writer.write("]\r\n");
	
			keys = cfg.getKeys(cate);
			if (keys != null)
			{
				itKey = keys.iterator();
				while (itKey.hasNext())
				{
					key = itKey.next();
					writer.write(key);
					writer.write("=");
					writer.write(cfg.getValue(cate, key)+"\r\n");
				}
			}
		}
		return true;
	}
}
