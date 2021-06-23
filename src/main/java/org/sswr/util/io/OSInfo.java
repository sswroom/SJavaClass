package org.sswr.util.io;

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
		else if (System.getProperty("os.name").equalsIgnoreCase("Windows"))
		{
			return OSType.WINDOWS;
		}
		else
		{
			return OSType.UNKNOWN;
		}
	}
}
