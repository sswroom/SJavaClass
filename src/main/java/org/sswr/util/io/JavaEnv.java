package org.sswr.util.io;

import jakarta.annotation.Nonnull;

public class JavaEnv
{
	@Nonnull
	public static String getProgName()
	{
		String cmd = System.getProperty("sun.java.command");
		if (cmd == null)
			return "<Unknown>";
		if (cmd.endsWith(".jar"))
		{
			return cmd.substring(0, cmd.length() - 4);
		}
		int i = cmd.indexOf(".");
		return cmd.substring(i + 1);
	}
}
