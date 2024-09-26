package org.sswr.util.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import jakarta.annotation.Nonnull;

public class MyProcess
{
	public static int run(@Nonnull String cmd, @Nonnull String []args, @Nonnull StringBuilder output)
	{
		Runtime rt = Runtime.getRuntime();
		String[] commands = new String[args.length + 1];
		commands[0] = cmd;
		int i = 0;
		int j = args.length;
		while (i < j)
		{
			commands[i + 1] = args[i];
			i++;
		}
		try
		{
			Process proc = rt.exec(commands);
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			
			String s = null;
			while ((s = stdInput.readLine()) != null)
			{
				output.append(s+"\r\n");
			}

			stdInput = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
			while ((s = stdInput.readLine()) != null)
			{
				output.append(s+"\r\n");
			}
			return proc.waitFor();
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
			return -1;
		}
		catch (InterruptedException ex)
		{
			ex.printStackTrace();
			return -1;
		}
	}
}
