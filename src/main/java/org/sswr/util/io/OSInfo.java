package org.sswr.util.io;

import java.nio.charset.Charset;

public class OSInfo
{
	public static OSType getOSType()
	{
		if (System.getProperty("java.vm.name").equalsIgnoreCase("Dalvik"))
		{
			return OSType.Android;
		}
		else
		{
			String osName = System.getProperty("os.name").toUpperCase();
			if (osName.equals("LINUX"))
			{

				return OSType.Linux_X86_64;
			}
			else if (osName.startsWith("WINDOWS"))
			{
				return OSType.WindowsNT64;
			}
			else
			{
				return OSType.Unknown;
			}
		}
	}

	public static Charset getDefaultCharset()
	{
		return Charset.forName(System.getProperty("sun.jnu.encoding"));
	}
}
