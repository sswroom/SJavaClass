package org.sswr.util.basic;

import java.sql.Timestamp;

import org.sswr.util.data.DateTimeUtil;

public class HiResClock
{
	private long stTime;

	public HiResClock()
	{
		this.stTime = getRelTime_us();
	}

	public void start()
	{
		this.stTime = getRelTime_us();
	}

	public double getTimeDiff()
	{
		return (getRelTime_us() - this.stTime) * 0.000001;
	}
	public long getTimeDiffus()
	{
		return (getRelTime_us() - this.stTime);
	}

	public double getAndRestart()
	{
		long currTime = getRelTime_us();
		double ret = (currTime - this.stTime) * 0.000001;
		this.stTime = currTime;
		return ret;
	}

	static long getRelTime_us()
	{
		Timestamp ts = DateTimeUtil.timestampNow();
		return ts.getTime() * 1000 + (ts.getNanos() / 1000) % 1000;
	}
}
