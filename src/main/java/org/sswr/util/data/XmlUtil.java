package org.sswr.util.data;

import jakarta.annotation.Nonnull;

public class XmlUtil {
	@Nonnull
	public static String toAttr(@Nonnull String v)
	{
		v = v.replace("&", "&amp;");
		v = v.replace("<", "&lt;");
		v = v.replace(">", "&gt;");
		v = v.replace("'", "&apos;");
		v = v.replace("\"", "&quot;");
		v = v.replace("\n", "&#10;");
		return v;
	}
	
	@Nonnull
	public static String toXMLText(@Nonnull String v)
	{
		v = v.replace("&", "&amp;");
		v = v.replace("<", "&lt;");
		v = v.replace(">", "&gt;");
		v = v.replace("'", "&apos;");
		v = v.replace("\"", "&quot;");
		return v;
	}

	@Nonnull
	public static String toHTMLBodyText(@Nonnull String v)
	{
		v = v.replace("&", "&#38;");
		v = v.replace("<", "&lt;");
		v = v.replace(">", "&gt;");
		v = v.replace("'", "&#39;");
		v = v.replace("\"", "&quot;");
		v = v.replace("\r", "");
		v = v.replace("\n", "<br/>");
		v = v.replace("\t", "&nbsp;&nbsp;&nbsp;&nbsp;");
		return v;
	}

	@Nonnull
	public static String parseStr(@Nonnull String v)
	{
		return parseStr(v.toCharArray(), 0, v.length());
	}

	public static String parseStr(@Nonnull char[] v, int xmlStart, int xmlEnd)
	{
		StringBuilder sb = new StringBuilder();
		char c;
		int leng;
		while (xmlStart < xmlEnd)
		{
			c = v[xmlStart];
			leng = xmlEnd - xmlStart;
			if (c == '&')
			{
				if (leng >= 5 && v[xmlStart + 1] == 'a' && v[xmlStart + 2] == 'm' && v[xmlStart + 3] == 'p' && v[xmlStart + 4] == ';')
				{
					sb.append('&');
					xmlStart += 5;
				}
				else if (leng >= 4 && v[xmlStart + 1] == 'l' && v[xmlStart + 2] == 't' && v[xmlStart + 3] == ';')
				{
					sb.append('<');
					xmlStart += 4;
				}
				else if (leng >= 4 && v[xmlStart + 1] == 'g' && v[xmlStart + 2] == 't' && v[xmlStart + 3] == ';')
				{
					sb.append('>');
					xmlStart += 4;
				}
				else if (leng >= 6 && v[xmlStart + 1] == 'a' && v[xmlStart + 2] == 'p' && v[xmlStart + 3] == 'o' && v[xmlStart + 4] == 's' && v[xmlStart + 5] == ';')
				{
					sb.append('\'');
					xmlStart += 6;
				}
				else if (leng >= 6 && v[xmlStart + 1] == 'a' && v[xmlStart + 2] == 'p' && v[xmlStart + 3] == 'o' && v[xmlStart + 4] == 's' && v[xmlStart + 5] == ';')
				{
					sb.append('"');
					xmlStart += 6;
				}
				else if (leng >= 3 && v[xmlStart + 1] == '#' && v[xmlStart + 2] == 'x')
				{
					boolean valid = true;
					int val = 0;
					int tmp = xmlStart + 3;
					while (true)
					{
						if (tmp >= xmlEnd)
						{
							valid = false;
							break;
						}
						c = v[tmp++];
						if (c >= '0' && c <= '9')
						{
							val = (val << 4) + (int)(c - 48);
						}
						else if (c >= 'A' && c <= 'F')
						{
							val = (val << 4) + (int)(c - 0x37);
						}
						else if (c >= 'a' && c <= 'f')
						{
							val = (val << 4) + (int)(c - 0x57);
						}
						else if (c == ';')
						{
							sb.append((char)val);
							xmlStart = tmp;
							break;
						}
						else
						{
							valid = false;
							break;
						}
					}
					if (!valid)
					{
						sb.append('&');
						xmlStart++;
					}
				}
				else if (leng >= 2 && v[xmlStart + 1] == '#')
				{
					boolean valid = true;
					int val = 0;
					int tmp = xmlStart + 2;
					while (true)
					{
						if (tmp >= xmlEnd)
						{
							valid = false;
							break;
						}
						c = v[tmp++];
						if (c >= '0' && c <= '9')
						{
							val = (val * 10) + (int)(c - 48);
						}
						else if (c == ';')
						{
							sb.append((char)val);
							xmlStart = tmp;
							break;
						}
						else
						{
							valid = false;
							break;
						}
					}
					if (!valid)
					{
						sb.append('&');
						xmlStart++;
					}
				}
				else
				{
					sb.append('&');
					xmlStart++;
				}
			}
			else
			{
				sb.append(c);
				xmlStart++;
			}
		}
		return sb.toString();
	}
}
