package org.sswr.util.data;

public class JasperUtil
{
	public static String styledCJKString(String dispStr, String cjkFont, String engFont)
	{
		return "<style fontName='"+(StringUtil.hasCJKChar(dispStr)?cjkFont:engFont)+"'>"+dispStr+"</style>";
	}
}
