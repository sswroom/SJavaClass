package org.sswr.util.io;

import java.nio.charset.Charset;

public class OSInfo
{
	public static OSType getOSType()
	{
		if (System.getProperty("java.vm.name").equalsIgnoreCase("Dalvik"))
		{
			return OSType.ANDROID;
		}
		else if (System.getProperty("os.name").equalsIgnoreCase("Linux"))
		{
			return OSType.LINUX;
		}
		else if (System.getProperty("os.name").toUpperCase().startsWith("WINDOWS"))
		{
			return OSType.WINDOWS;
		}
		else
		{
			return OSType.UNKNOWN;
		}
	}

	public static Charset getDefaultCharset()
	{
		return Charset.forName(System.getProperty("sun.jnu.encoding"));
	}
}
