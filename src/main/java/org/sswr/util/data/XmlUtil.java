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
}
