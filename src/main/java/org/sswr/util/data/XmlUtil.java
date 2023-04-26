package org.sswr.util.data;

public class XmlUtil {
	public static String toAttr(String v)
	{
		v = v.replace("&", "&amp;");
		v = v.replace("<", "&lt;");
		v = v.replace(">", "&gt;");
		v = v.replace("'", "&apos;");
		v = v.replace("\"", "&quot;");
		v = v.replace("\n", "&#10;");
		return v;
	}
	
	public static String toXMLText(String v)
	{
		v = v.replace("&", "&amp;");
		v = v.replace("<", "&lt;");
		v = v.replace(">", "&gt;");
		v = v.replace("'", "&apos;");
		v = v.replace("\"", "&quot;");
		return v;
	}

	public static String toHTMLText(String v)
	{
		v = v.replace("&", "&amp;");
		v = v.replace("<", "&lt;");
		v = v.replace(">", "&gt;");
		v = v.replace("'", "&apos;");
		v = v.replace("\"", "&quot;");
		v = v.replace("\r", "");
		v = v.replace("\n", "<br/>");
		return v;
	}
}
