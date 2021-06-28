package org.sswr.util.basic;

public class MyThread
{
	public static void sleep(long ms)
	{
		try
		{
			Thread.sleep(ms);
		}
		catch (InterruptedException ex)
		{
		}
	}

	public static void setPriority(ThreadPriority priority)
	{
		int ipriority;
		switch (priority)
		{
			case IDLE:
				ipriority = Thread.MIN_PRIORITY;
				break;
			case LOWEST:
				ipriority = 2;
				break;
			case LOW:
				ipriority = 3;
				break;
			case NORMAL:
				ipriority = Thread.NORM_PRIORITY;
				break;
			case HIGH:
				ipriority = 7;
				break;
			case HIGHEST:
				ipriority = 9;
				break;
			case REALTIME:
				ipriority = Thread.MAX_PRIORITY;
				break;
			default:
				return;
		}
		Thread.currentThread().setPriority(ipriority);
	}
}
