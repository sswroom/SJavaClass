package org.sswr.util.math.chart;

import org.sswr.util.math.Coord2DDbl;

import jakarta.annotation.Nonnull;

public class DoubleAxis extends Axis
{
	private double min;
	private double max;
	public DoubleAxis(@Nonnull DoubleData data)
	{
		double[] dataArr = data.getData();
		if (data.getCount() > 0)
		{
			this.min = this.max = dataArr[0];
			this.extendRange(data);
		}
		else
		{
			this.min = 0;
			this.max = this.min;
		}
	}

	public @Nonnull DataType getType()
	{
		return DataType.DOUBLE;
	}

	public void calcX(@Nonnull ChartData data, @Nonnull Coord2DDbl[] pos, double minX, double maxX)
	{
		if (data instanceof DoubleData)
		{
			double leng = (maxX - minX);
			double ratio = leng / (double)(max - min);
			double[] dArr = ((DoubleData)data).getData();
			int i = 0;
			int j = dArr.length;
			while (i < j)
			{
				pos[i].x = minX + (dArr[i] - min) * ratio;
				i++;
			}
		}
	}

	public void calcY(@Nonnull ChartData data, @Nonnull Coord2DDbl[] pos, double minY, double maxY)
	{
		if (data instanceof DoubleData)
		{
			double leng = (maxY - minY);
			double ratio = leng / (double)(max - min);
			double[] dArr = ((DoubleData)data).getData();
			int i = 0;
			int j = dArr.length;
			while (i < j)
			{
				pos[i].y = minY + (dArr[i] - min) * ratio;
				i++;
			}
		}
	}

	public void extendRange(@Nonnull DoubleData data)
	{
		double[] dataArr = data.getData();
		double v;
		int i = 0;
		int j = dataArr.length;
		while (i < j)
		{
			v = dataArr[i];
			if (v < min) min = v;
			if (v > max) max = v;
			i++;
		}
	}

	public void extendRange(double v)
	{
		if (v < min) min = v;
		if (v > max) max = v;
	}

	public double getMax()
	{
		return this.max;
	}

	public double getMin()
	{
		return this.min;
	}
}
