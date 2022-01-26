package org.sswr.util.basic;

public class HiResClock
{
	private long stTime;

	public HiResClock()
	{
		this.stTime = System.currentTimeMillis();
	}

	public void start()
	{
		this.stTime = System.currentTimeMillis();
	}

	public double getTimeDiff()
	{
		return (System.currentTimeMillis() - this.stTime) * 0.001;
	}
	public long getTimeDiffus()
	{
		return (System.currentTimeMillis() - this.stTime) * 1000;
	}

	public double getAndRestart()
	{
		long currTime = System.currentTimeMillis();
		double ret = (currTime - this.stTime) * 0.001;
		this.stTime = currTime;
		return ret;
	}

	static long getRelTime_us()
	{
		return System.currentTimeMillis() * 1000;
	}
}
