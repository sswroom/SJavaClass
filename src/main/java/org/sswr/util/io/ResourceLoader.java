package org.sswr.util.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.sswr.util.data.CPPObjectParser;

public class ResourceLoader
{
	public static InputStream load(Class<?> cls, String resourceName)
	{
		Module module = cls.getModule();
		try
		{
			return module.getResourceAsStream(resourceName);
		}
		catch (IOException ex)
		{
			return null;
		}
	}

	public static <T> List<T> loadObjects(Class<T> cls, String resourceName)
	{
		InputStream stm = load(cls, resourceName);
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
			parser = new CPPObjectParser<T>(cls);
		}
		catch (NoSuchMethodException ex)
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
