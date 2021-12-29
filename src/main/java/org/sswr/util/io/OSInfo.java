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
		else
		{
			String osName = System.getProperty("os.name").toUpperCase();
			if (osName.equals("LINUX"))
			{
				return OSType.LINUX;
			}
			else if (osName.startsWith("WINDOWS"))
			{
				return OSType.WINDOWS;
			}
			else
			{
				return OSType.UNKNOWN;
			}
		}
	}

	public static Charset getDefaultCharset()
	{
		return Charset.forName(System.getProperty("sun.jnu.encoding"));
	}
}
