package org.sswr.util.basic;

import java.util.ArrayList;
import java.util.List;

import jakarta.annotation.Nonnull;

public class TimedRunner implements Runnable
{
	class RunnableInfo
	{
		public Runnable runnable;
		public int runIntervalMs;
		public long lastRunTime;
	}

	private boolean running;
	private Thread t;
	private List<RunnableInfo> runnables;

	public TimedRunner()
	{
		this.runnables = new ArrayList<RunnableInfo>();
		this.running = false;
		this.t = new Thread(this);
		this.t.start();
	}

	public void addRunnable(@Nonnull Runnable runnable, int runIntervalMs)
	{
		synchronized(this.runnables)
		{
			RunnableInfo info = new RunnableInfo();
			info.runnable = runnable;
			info.lastRunTime = 0;
			info.runIntervalMs = runIntervalMs;
			this.runnables.add(info);
		}
	}

	public void stop()
	{
		if (this.running)
		{
			this.t.interrupt();
		}
	}

	public boolean isRunning()
	{
		return this.running;
	}

	public void run()
	{
		this.running = true;
		while (true)
		{
			Object [] infos;
			synchronized(this.runnables)
			{
				infos = this.runnables.toArray();
			}
			int i = infos.length;
			RunnableInfo info;
			long currTime;
			while (i-- > 0)
			{
				try
				{
					info = (RunnableInfo)infos[i];
					currTime = System.currentTimeMillis();
					if (currTime - info.lastRunTime >= info.runIntervalMs)
					{
						info.lastRunTime = currTime;
						info.runnable.run();
					}
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
				}
			}

			try
			{
				Thread.sleep(1000);
			}
			catch (InterruptedException ex)
			{
				break;
			}
		}
		System.out.println("TimedRunner stopped");
		this.running = false;
	}
}
