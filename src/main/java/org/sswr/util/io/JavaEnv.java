package org.sswr.util.io;

public class JavaEnv
{
	public static Class<?> getMainClass()
	{
		try
		{
			return Class.forName(System.getProperty("sun.java.command"));
		}
		catch (ClassNotFoundException ex)
		{
			return null;
		}
	}
}
