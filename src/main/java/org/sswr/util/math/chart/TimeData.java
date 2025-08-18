package org.sswr.util.math.chart;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

import jakarta.annotation.Nonnull;

public class TimeData implements ChartData
{
	private Instant[] timeArr;

	public TimeData(@Nonnull Timestamp[] timeArr, int ofst, int count)
	{
		this.timeArr = new Instant[count];
		int i = count;
		while (i-- > 0)
		{
			this.timeArr[i] = timeArr[i + ofst].toInstant();
		}
	}

	public TimeData(@Nonnull Instant[] timeArr)
	{
		this.timeArr = new Instant[timeArr.length];
		int i = timeArr.length;
		while (i-- > 0)
		{
			this.timeArr[i] = timeArr[i];
		}
	}

	public TimeData(@Nonnull long[] ticksArr)
	{
		this.timeArr = new Instant[ticksArr.length];
		int i = ticksArr.length;
		while (i-- > 0)
		{
			this.timeArr[i] = Instant.ofEpochMilli(ticksArr[i]);
		}
	}

	public TimeData(@Nonnull List<Timestamp> timeArr)
	{
		this.timeArr = new Instant[timeArr.size()];
		int i = timeArr.size();
		while (i-- > 0)
		{
			this.timeArr[i] = timeArr.get(i).toInstant();
		}
	}
	
	public @Nonnull DataType getType()
	{
		return DataType.Time;
	}

	public @Nonnull ChartData clone()
	{
		return new TimeData(this.timeArr);
	}

	public int getCount()
	{
		return this.timeArr.length;
	}

	public @Nonnull Instant[] getData()
	{
		return this.timeArr;
	}
}
