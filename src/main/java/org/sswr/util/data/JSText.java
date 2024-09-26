package org.sswr.util.data;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class JSText
{
	@Nonnull
	public static String quoteString(@Nullable String v)
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

	@Nonnull
	public static String dquoteString(@Nullable String v)
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
		sb.append('\"');
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
			case '\"':
				sb.append('\\');
				sb.append('\"');
				break;
			default:
				sb.append(c);
				break;
			}
			i++;
		}
		sb.append('\"');
		return sb.toString();
	}

	public static void toJSTextDQuote(@Nonnull StringBuilder sb, @Nullable String v)
	{
		if (v == null)
		{
			sb.append("null");
			return;
		}
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
