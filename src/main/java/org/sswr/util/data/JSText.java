package org.sswr.util.data;

public class JSText
{
	public static String quoteString(String v)
	{
		if (v == null)
		{
			return "null";
		}
		char carr[] = v.toCharArray();
		int i = 0;
		int j = carr.length;
		char c;
		StringBuilder sb = new StringBuilder();
		sb.append('\'');
		while (i < j)
		{
			c = carr[i];
			switch (c)
			{
			case '\\':
				sb.append('\\');
				sb.append('\\');
				break;
			case '\r':
				sb.append('\\');
				sb.append('r');
				break;
			case '\n':
				sb.append('\\');
				sb.append('n');
				break;
			case '\t':
				sb.append('\\');
				sb.append('t');
				break;
			case '\'':
				sb.append('\\');
				sb.append('\'');
				break;
			default:
				sb.append(c);
				break;
			}
			i++;
		}
		sb.append('\'');
		return sb.toString();
	}

	public static void toJSTextDQuote(StringBuilder sb, String v)
	{
		char carr[] = v.toCharArray();
		char c;
		int i = 0;
		int j = carr.length;
		sb.append('\"');
		while (i < j)
		{
			c = carr[i];
			switch (c)
			{
			case '\"':
				sb.append('\\');
				sb.append('\"');
				break;
			case '\n':
				sb.append('\\');
				sb.append('n');
				break;
			case '\r':
				sb.append('\\');
				sb.append('n');
				break;
			default:
				sb.append(c);
				break;
			}
			i++;
		}
		sb.append('\"');
	}
}
