package org.sswr.util.io;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.sswr.util.data.CPPObjectParser;
import org.sswr.util.data.SharedLong;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class ResourceLoader
{
	@Nullable
	public static String getResourcePath(@Nonnull Class<?> cls, @Nonnull String resourceName)
	{
		if (OSInfo.getOSType() == OSType.Android)
		{
			return cls.getClassLoader().getResource(resourceName).getPath();
		}
		Module module = cls.getModule();
		URL url = module.getClassLoader().getResource(resourceName);
		if (url == null)
		{
			return null;
		}
		return url.getPath();
	}

	@Nullable
	public static InputStream load(@Nonnull Class<?> cls, @Nonnull String resourceName, @Nullable SharedLong lastModified)
	{
		if (OSInfo.getOSType() == OSType.Android)
		{
			if (lastModified != null)
			{
				lastModified.value = 0;
			}
			return cls.getClassLoader().getResourceAsStream(resourceName);
		}
		Module module = cls.getModule();
		try
		{
			URL url = module.getClassLoader().getResource(resourceName);
			if (url == null)
			{
				return null;
			}
			URLConnection conn = url.openConnection();
			if (lastModified != null)
			{
				lastModified.value = conn.getLastModified();
			}
			return conn.getInputStream();
			//return module.getResourceAsStream(resourceName);
		}
		catch (IOException ex)
		{
			return null;
		}
	}

	@Nullable
	public static <T> List<T> loadObjects(@Nonnull Class<T> cls, @Nonnull String resourceName, @Nullable String[] fieldNames)
	{
		InputStream stm = load(cls, resourceName, null);
		if (stm == null)
		{
			return null;
		}
		CharReader reader = new CharReader(new UTF8Reader(stm));
		reader.skipWS();
		if (reader.nextChar() != '{')
		{
			try
			{
				reader.close();
			}
			catch (IOException ex)
			{
			}
			return null;
		}
		List<T> objList = new ArrayList<T>();
		CPPObjectParser<T> parser;
		try
		{
			parser = new CPPObjectParser<T>(cls, fieldNames);
		}
		catch (NoSuchMethodException ex)
		{
			ex.printStackTrace();
			return null;
		}
		catch (NoSuchFieldException ex)
		{
			ex.printStackTrace();
			return null;
		}
		while (true)
		{
			T o = parser.parseObject(reader);
			char c;
			if (o != null)
			{
				objList.add(o);
				c = reader.nextChar();
				if (c == '}')
				{
					break;
				}
				else if (c != ',')
				{
					System.out.println("ResourceLoader: not comma");
					return null;
				}
			}
			else
			{
				break;
			}
		}
		try
		{
			reader.close();
		}
		catch (IOException ex)
		{

		}
		return objList;
	}
}
