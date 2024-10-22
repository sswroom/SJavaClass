package org.sswr.util.basic;

import org.sswr.util.io.LogTool;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public abstract class TimedTask implements Runnable
{
	private int intervalMS;
	private @Nonnull Thread thread;
	private @Nullable LogTool logger;
	private boolean toStop;

	protected TimedTask(@Nonnull String threadName, int intervalMS)
	{
		this.intervalMS = intervalMS;
		this.toStop = false;
		this.thread = new Thread(this, threadName);
		this.thread.start();
		this.logger = null;
	}

	public void setLogger(@Nullable LogTool logger)
	{
		this.logger = logger;
	}

	public abstract void action() throws Exception;

	public void run()
	{
		long nextTriggerTime = System.currentTimeMillis();
		long currTime;
		while (!this.toStop)
		{
			currTime = System.currentTimeMillis();
			if (currTime >= nextTriggerTime || nextTriggerTime > currTime + this.intervalMS)
			{
				try
				{
					this.action();
				}
				catch (Exception ex)
				{
					if (this.logger != null)
						this.logger.logException(ex);
					else
						ex.printStackTrace();
				}
				nextTriggerTime = System.currentTimeMillis() + this.intervalMS;
			}
			else
			{
				try
				{
					synchronized(this)
					{
						this.wait(nextTriggerTime - currTime);
						nextTriggerTime = currTime;
					}
				}
				catch (InterruptedException ex)
				{

				}
			}
		}
	}

	public void triggerTask()
	{
		synchronized(this)
		{
			this.notify();
		}
	}

	public void close()
	{
		if (!toStop)
		{
			this.toStop = true;
			try
			{
				this.thread.interrupt();
				while (this.thread.isAlive())
				{
					MyThread.sleep(10);
				}
			}
			catch (SecurityException ex)
			{
				ex.printStackTrace();
			}
		}
	}
}
