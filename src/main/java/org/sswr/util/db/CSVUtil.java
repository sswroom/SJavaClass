package org.sswr.util.db;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.sswr.util.data.FieldGetter;

public class CSVUtil {
	public static String quote(String s)
	{
		if (s == null)
		{
			return "\"\"";
		}
		return "\""+s.replace("\"", "\"\"")+"\"";
	}
	public static String join(String s[])
	{
		StringBuilder sb = new StringBuilder();
		int i = 0;
		int j = s.length;
		while (i < j)
		{
			if (i > 0) sb.append(",");
			sb.append(quote(s[i]));
			i++;
		}
		return sb.toString();
	}

	public static String[] readLine(BufferedReader reader) throws IOException
	{
		ArrayList<String> cols;
		boolean isQuoted = false;
		StringBuilder sb = new StringBuilder();
		cols = new ArrayList<String>();
		while (true)
		{
			String line = reader.readLine();
			if (line == null)
			{
				return null;
			}

			int i = 0;
			int j = line.length();
			char c;
			while (i < j)
			{
				c = line.charAt(i);
				if (c == '"')
				{
					if (!isQuoted)
					{
						isQuoted = true;
					}
					else if (i + 1 < j && line.charAt(i + 1) == '"')
					{
						i++;
						sb.append(c);
					}
					else
					{
						isQuoted = false;
					}
				}
				else if (isQuoted)
				{
					sb.append(c);
				}
				else if (c == ',')
				{
					cols.add(sb.toString());
					sb.setLength(0);
				}
				else
				{
					sb.append(c);
				}
				i++;
			}
			if (!isQuoted)
			{
				cols.add(sb.toString());
				break;
			}
			sb.append("\r\n");
		}
		return cols.toArray(new String[cols.size()]);
	}


	private static <T> void appendRow(StringBuilder sb, T data, List<FieldGetter<T>> getters) throws IllegalAccessException, InvocationTargetException
	{
		int i = 0;
		int j = getters.size();
		while (i < j)
		{
			if (i > 0)
			{
				sb.append(",");
			}
			Object o = getters.get(i).get(data);
			if (o == null)
			{
				sb.append("\"\"");
			}
			else
			{
				sb.append(quote(o.toString()));
			}
			i++;
		}
	}

	public static <T> boolean createFile(File file, Charset cs, Iterable<T> datas, String cols[])
	{
		try
		{
			FileWriter writer = new FileWriter(file, cs);
			StringBuilder sb;
			int i = 0;
			int j = cols.length;
			sb = new StringBuilder();
			while (i < j)
			{
				if (i > 0)
				{
					sb.append(",");
				}
				sb.append(quote(cols[i]));
				i++;
			}
			sb.append("\r\n");
			writer.write(sb.toString());
			
			boolean succ = true;

			try
			{
				Iterator<T> it = datas.iterator();
				if (it.hasNext())
				{
					T data = it.next();
					ArrayList<FieldGetter<T>> getters = new ArrayList<FieldGetter<T>>(j);
					i = 0;
					while (i < j)
					{
						getters.add(new FieldGetter<T>(data.getClass(), cols[i]));
						i++;
					}

					sb.setLength(0);
					appendRow(sb, data, getters);
					sb.append("\r\n");
					writer.write(sb.toString());
					while (it.hasNext())
					{
						data = it.next();
						sb.setLength(0);
						appendRow(sb, data, getters);
						sb.append("\r\n");
						writer.write(sb.toString());
					}
				}
			}
			catch (NoSuchFieldException ex)
			{
				ex.printStackTrace();
				succ = false;
			}
			catch (IllegalAccessException ex)
			{
				ex.printStackTrace();
				succ = false;
			}
			catch (InvocationTargetException ex)
			{
				ex.printStackTrace();
				succ = false;
			}

			writer.close();
			return succ;
		}
		catch (IOException ex)
		{
			return false;
		}

	}
}
