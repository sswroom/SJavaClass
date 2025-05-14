package org.sswr.util.data;

import java.nio.charset.StandardCharsets;

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
	public static String toAttrText(@Nonnull String v)
	{
		return "\""+toAttr(v)+"\"";
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
	public static String toHTMLTextXMLColor(@Nonnull String text)
	{
		StringBuilderUTF8 sb = new StringBuilderUTF8();
		boolean elementStarted = false;
		boolean beginAttr = false;
		boolean fontStarted = false;
		byte quoteChar = 0;
		byte c;
		byte[] textBuff = text.getBytes(StandardCharsets.UTF_8);
		int len = text.length();
		int ofst = 0;
		while (ofst < len && (c = textBuff[ofst]) != 0)
		{
			ofst++;
			if (quoteChar != 0 || !elementStarted)
			{
				if (c == '&')
				{
					sb.append("&#38;");
				}
				else if (c == '<')
				{
					if (quoteChar == 0)
					{
						elementStarted = true;
						fontStarted = true;
					}
					sb.append("<font color=\"red\">&lt;");
				}
				else if (c == '>')
				{
					sb.append("&gt;");
				}
				else if (c == '\'')
				{
					sb.append("&#39;");
				}
				else if (c == '"')
				{
					sb.append("&quot;");
				}
				else if (c == '\r')
				{
				}
				else if (c == '\n')
				{
					sb.append("<br/>");
				}
				else if (c == '\t')
				{
					sb.append("&nbsp;&nbsp;&nbsp;&nbsp;");
				}
				else
				{
					sb.appendUTF8Char(c);
				}
				if (c == quoteChar)
				{
					quoteChar = 0;
				}
			}
			else
			{
				if (c == '=' && beginAttr)
				{
					if (!fontStarted)
					{
						sb.append("<font color=\"blue\">");
						fontStarted = true;
					}
					sb.append("=</font><font color=\"green\">");
				}
				else if (c == ' ')
				{
					if (fontStarted)
					{
						sb.append(" </font>");
						fontStarted = false;
					}
					else
					{
						sb.appendUTF8Char((byte)' ');
					}
					beginAttr = true;
				}
				else if (c == '>')
				{
					if (fontStarted)
					{
						sb.append("</font>");
					}
					sb.append("<font color=\"red\">&gt;</font>");
					beginAttr = false;
					fontStarted = false;
					elementStarted = false;
				}
				else if (c == '\'')
				{
					sb.append("&#39;");
					quoteChar = '\'';
				}
				else if (c == '"')
				{
					sb.append("&quot;");
					quoteChar = '\"';
				}
				else if (c == '/')
				{
					if (ofst < len && textBuff[ofst] == '>')
					{
						if (fontStarted)
						{
							sb.append("</font>");
							fontStarted = false;
						}
						sb.append("<font color=\"red\">/&gt;</font>");
						ofst++;
						elementStarted = false;
					}
					else
					{
						sb.appendUTF8Char(c);
					}
				}
				else if (c == '\r')
				{
				}
				else if (c == '\n')
				{
					sb.append("<br/>");
				}
				else if (c == '\t')
				{
					sb.append("&nbsp;&nbsp;&nbsp;&nbsp;");
				}
				else if (c == '&')
				{
					sb.append("&#38;");
				}
				else
				{
					if (!fontStarted)
					{
						fontStarted = true;
						sb.append("<font color=\"blue\">");
					}
					sb.appendUTF8Char(c);
				}
			}
		}
		if (fontStarted)
		{
			sb.append("</font>");
		}
		return sb.toString();
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

	public static boolean htmlAppendCharRef(@Nonnull byte[] chrRef, int chrOfst, int refSize, @Nonnull StringBuilderUTF8 sb)
	{
		byte[] sbuff = new byte[6];
		int wcs;
		if (chrRef[chrOfst + 0] != '&')
		{
			return false;
		}
		if (refSize == 4)
		{
			if (chrRef[chrOfst + 1] == '#')
			{
				sbuff[0] = chrRef[chrOfst + 2];
				sbuff[1] = 0;
				sb.appendUTF8Char((byte)StringUtil.toUInt32(chrRef, chrOfst + 2));
				return true;
			}
			else if (StringUtil.startsWithC(chrRef, chrOfst, refSize, "&lt;"))
			{
				sb.appendUTF8Char((byte)'<');
				return true;
			}
			else if (StringUtil.startsWithC(chrRef, chrOfst, refSize, "&gt;"))
			{
				sb.appendUTF8Char((byte)'>');
				return true;
			}
		}
		else if (refSize == 5)
		{
			if (chrRef[chrOfst + 1] == '#')
			{
				if (chrRef[chrOfst + 2] == 'x')
				{
					sb.appendUTF8Char(StringUtil.hex2UInt8C(chrRef, chrOfst + 3));
					return true;
				}
				else
				{
					sbuff[0] = chrRef[chrOfst + 2];
					sbuff[1] = chrRef[chrOfst + 3];
					sbuff[2] = 0;
					wcs = StringUtil.toUInt32(sbuff, 0);
					sb.appendChar(wcs, 1);
					return true;
				}
			}
			else if (StringUtil.startsWithC(chrRef, chrOfst, refSize, "&amp;"))
			{
				sb.appendUTF8Char((byte)'&');
				return true;
			}
		}
		else if (refSize == 6)
		{
			if (chrRef[chrOfst + 1] == '#')
			{
				if (chrRef[chrOfst + 2] == 'x')
				{
					wcs = StringUtil.hex2UInt8C(chrRef, chrOfst + 3);
				}
				else
				{
					sbuff[0] = chrRef[chrOfst + 2];
					sbuff[1] = chrRef[chrOfst + 3];
					sbuff[2] = chrRef[chrOfst + 4];
					sbuff[3] = 0;
					wcs = StringUtil.toUInt32(sbuff, 0);
				}
				sb.appendChar(wcs, 1);
				return true;
			}
			else if (StringUtil.startsWithC(chrRef, chrOfst, refSize, "&apos;"))
			{
				sb.appendUTF8Char((byte)'\'');
				return true;
			}
			else if (StringUtil.startsWithC(chrRef, chrOfst, refSize, "&bull;"))
			{
				sb.appendChar(0x2022, 1);
				return true;
			}
			else if (StringUtil.startsWithC(chrRef, chrOfst, refSize, "&quot;"))
			{
				sb.appendUTF8Char((byte)'\"');
				return true;
			}
		}
		else if (refSize == 7)
		{
			if (chrRef[chrOfst + 1] == '#')
			{
				if (chrRef[chrOfst + 2] == 'x')
				{
					wcs = StringUtil.hex2UInt8C(chrRef, chrOfst + 4);
					if (chrRef[chrOfst + 3] <= '9')
					{
						wcs += (int)(chrRef[chrOfst + 3] - '0') << 8;
					}
					else if (chrRef[chrOfst + 3] <= 'F')
					{
						wcs += (int)(chrRef[chrOfst + 3] - 0x37) << 8;
					}
					else if (chrRef[chrOfst + 3] <= 'f')
					{
						wcs += (int)(chrRef[chrOfst + 3] - 0x57) << 8;
					}
				}
				else
				{
					sbuff[0] = chrRef[chrOfst + 2];
					sbuff[1] = chrRef[chrOfst + 3];
					sbuff[2] = chrRef[chrOfst + 4];
					sbuff[3] = chrRef[chrOfst + 5];
					sbuff[4] = 0;
					wcs = (int)StringUtil.toUInt32(sbuff, 0);
				}
				sb.appendChar(wcs, 1);
				return true;
			}
			else if (StringUtil.startsWithC(chrRef, chrOfst, refSize, "&raquo;"))
			{
				sb.appendChar(0xbb, 1);
				return true;
			}
		}
		return false;
	}
}
