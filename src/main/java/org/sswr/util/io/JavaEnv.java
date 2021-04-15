package org.sswr.util.io;

public class JavaEnv
{
	public static String getProgName()
	{
		String cmd = System.getProperty("sun.java.command");
		if (cmd.endsWith(".jar"))
		{
			return cmd.substring(0, cmd.length() - 4);
		}
		int i = cmd.indexOf(".");
		return cmd.substring(i + 1);
	}
}
