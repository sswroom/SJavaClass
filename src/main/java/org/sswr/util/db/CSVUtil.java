package org.sswr.util.db;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;

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
}
