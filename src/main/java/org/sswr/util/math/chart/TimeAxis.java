package org.sswr.util.math.chart;

import java.time.Instant;

import org.sswr.util.data.DateTimeUtil;
import org.sswr.util.math.Coord2DDbl;

import jakarta.annotation.Nonnull;

public class TimeAxis extends Axis
{
	private @Nonnull Instant min;
	private @Nonnull Instant max;
	public TimeAxis(@Nonnull TimeData data)
	{
		Instant[] dataArr = data.getData();
		if (data.getCount() > 0)
		{
			this.min = this.max = dataArr[0];
			this.extendRange(data);
		}
		else
		{
			this.min = Instant.now();
			this.max = this.min;
		}
	}

	public TimeAxis(@Nonnull Instant val)
	{
		this.min = val;
		this.max = val;
	}

	public @Nonnull DataType getType()
	{
		return DataType.Time;
	}

	public void calcX(@Nonnull ChartData data, @Nonnull Coord2DDbl[] pos, double minX, double maxX)
	{
		if (data instanceof TimeData)
		{
			TimeData tdata = (TimeData)data;
			double leng = (maxX - minX);
			double ratio = leng / DateTimeUtil.timeDiffSec(max, min);
			Instant[] tArr = tdata.getData();
			int i = 0;
			int j = tArr.length;
			while (i < j)
			{
				pos[i].x = minX + DateTimeUtil.timeDiffSec(tArr[i], min) * ratio;
				i++;
			}
		}
	}
	
	public void calcY(@Nonnull ChartData data, @Nonnull Coord2DDbl[] pos, double minY, double maxY)
	{
		if (data instanceof TimeData)
		{
			double leng = (maxY - minY);
			double ratio = leng / DateTimeUtil.timeDiffSec(max, min);
			Instant[] tArr = ((TimeData)data).getData();
			int i = 0;
			int j = tArr.length;
			while (i < j)
			{
				pos[i].y = minY + DateTimeUtil.timeDiffSec(tArr[i], min) * ratio;
				i++;
			}
		}
	}

	public void extendRange(@Nonnull TimeData data)
	{
		Instant[] dataArr = data.getData();
		Instant v;
		int i = 0;
		int j = dataArr.length;
		while (i < j)
		{
			v = dataArr[i];
			if (v.isBefore(min)) min = v;
			if (v.isAfter(max)) max = v;
			i++;
		}
	}

	public void extendRange(Instant inst)
	{
		if (inst.isBefore(min)) min = inst;
		if (inst.isAfter(max)) max = inst;
	}

	public @Nonnull Instant getMax()
	{
		return this.max;
	}

	public @Nonnull Instant getMin()
	{
		return this.min;
	}
}
