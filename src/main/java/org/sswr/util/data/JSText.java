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
		StringBuilderUTF8 sb = new StringBuilderUTF8();
		sb.appendUTF8Char((byte)'\'');
		while (i < j)
		{
			c = carr[i];
			switch (c)
			{
			case '\\':
				sb.appendUTF8Char((byte)'\\');
				sb.appendUTF8Char((byte)'\\');
				break;
			case '\r':
				sb.appendUTF8Char((byte)'\\');
				sb.appendUTF8Char((byte)'r');
				break;
			case '\n':
				sb.appendUTF8Char((byte)'\\');
				sb.appendUTF8Char((byte)'n');
				break;
			case '\t':
				sb.appendUTF8Char((byte)'\\');
				sb.appendUTF8Char((byte)'t');
				break;
			case '\'':
				sb.appendUTF8Char((byte)'\\');
				sb.appendUTF8Char((byte)'\'');
				break;
			default:
				sb.appendChar(c, 1);
				break;
			}
			i++;
		}
		sb.appendUTF8Char((byte)'\'');
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
		StringBuilderUTF8 sb = new StringBuilderUTF8();
		sb.appendUTF8Char((byte)'\"');
		while (i < j)
		{
			c = carr[i];
			switch (c)
			{
			case '\\':
				sb.appendUTF8Char((byte)'\\');
				sb.appendUTF8Char((byte)'\\');
				break;
			case '\r':
				sb.appendUTF8Char((byte)'\\');
				sb.appendUTF8Char((byte)'r');
				break;
			case '\n':
				sb.appendUTF8Char((byte)'\\');
				sb.appendUTF8Char((byte)'n');
				break;
			case '\t':
				sb.appendUTF8Char((byte)'\\');
				sb.appendUTF8Char((byte)'t');
				break;
			case '\"':
				sb.appendUTF8Char((byte)'\\');
				sb.appendUTF8Char((byte)'\"');
				break;
			default:
				sb.appendChar(c, 1);
				break;
			}
			i++;
		}
		sb.appendUTF8Char((byte)'\"');
		return sb.toString();
	}

	public static @Nonnull String toJSText(@Nullable String v)
	{
		StringBuilderUTF8 sb = new StringBuilderUTF8();
		char c;
		if (v == null)
			return "null";
		char[] sarr = v.toCharArray();
		int i = 0;
		int j = sarr.length;
		sb.appendUTF8Char((byte)'\'');
		while (i < j)
		{
			c = sarr[i];
			switch (c)
			{
			case '\\':
				sb.append("\\\\");
				break;
			case '\'':
				sb.append("\\\'");
				break;
			case '\n':
				sb.append("\\n");
				break;
			case '\r':
				sb.append("\\n");
				break;
			case '\0':
				sb.append("\\0");
				break;
			default:
				if (c < 32)
				{
					sb.append("\\u" + StringUtil.toHex16(c));
				}
				else
				{
					sb.appendChar((int)c, 1);
				}
				break;
			}
		}
		sb.appendUTF8Char((byte)'\'');
		return sb.toString();		
	}

	public static void toJSTextDQuote(@Nonnull StringBuilderUTF8 sb, @Nullable String v)
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
		sb.appendUTF8Char((byte)'\"');
		while (i < j)
		{
			c = carr[i];
			switch (c)
			{
			case '\"':
				sb.appendUTF8Char((byte)'\\');
				sb.appendUTF8Char((byte)'\"');
				break;
			case '\n':
				sb.appendUTF8Char((byte)'\\');
				sb.appendUTF8Char((byte)'n');
				break;
			case '\r':
				sb.appendUTF8Char((byte)'\\');
				sb.appendUTF8Char((byte)'n');
				break;
			default:
				sb.appendChar(c, 1);
				break;
			}
			i++;
		}
		sb.appendUTF8Char((byte)'\"');
	}
}
