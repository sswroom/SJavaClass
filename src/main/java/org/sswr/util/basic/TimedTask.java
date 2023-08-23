package org.sswr.util.basic;

import org.sswr.util.io.LogTool;

public abstract class TimedTask implements Runnable
{
	private int intervalMS;
	private Thread thread;
	private LogTool logger;

	protected TimedTask(String threadName, int intervalMS)
	{
		this.intervalMS = intervalMS;
		this.thread = new Thread(this, threadName);
		this.thread.start();
		this.logger = null;
	}

	public void setLogger(LogTool logger)
	{
		this.logger = logger;
	}

	public abstract void action() throws Exception;

	public void run()
	{
		long nextTriggerTime = System.currentTimeMillis();
		long currTime;
		while (true)
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
}
