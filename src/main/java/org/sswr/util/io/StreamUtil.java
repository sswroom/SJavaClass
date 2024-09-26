package org.sswr.util.io;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import jakarta.annotation.Nonnull;

public class StreamUtil
{
	public static boolean seekFromBeginning(@Nonnull InputStream stm, long offset)
	{
		try
		{
			if (stm instanceof FileInputStream)
			{
				FileInputStream fis = (FileInputStream)stm;
				fis.getChannel().position(offset);
				return true;
			}
			return false;
		}
		catch (IOException ex)
		{
			return false;
		}
	}

	public static long getLength(@Nonnull InputStream stm)
	{
		try
		{
			if (stm instanceof FileInputStream)
			{
				FileInputStream fis = (FileInputStream)stm;
				return fis.getChannel().size();
			}
			else
			{
				return stm.available();
			}
		}
		catch (IOException ex)
		{
			return 0;
		}
	}
}
