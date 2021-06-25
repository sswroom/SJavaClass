package org.sswr.util.io;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class StreamUtil
{
	public static boolean seekFromBeginning(InputStream stm, long offset)
	{
		try
		{
			if (stm instanceof FileInputStream)
			{
				FileInputStream fis = (FileInputStream)stm;
				fis.getChannel().position(0);
				return true;
			}
			return false;
		}
		catch (IOException ex)
		{
			return false;
		}
	}
}
