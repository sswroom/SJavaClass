package org.sswr.util.math.chart;

import org.sswr.util.math.Coord2DDbl;

import jakarta.annotation.Nonnull;

public class Int32Axis extends Axis
{
	private int min;
	private int max;

	public Int32Axis(@Nonnull Int32Data data)
	{
		int[] dataArr = data.getData();
		if (dataArr.length > 0)
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
		return DataType.Integer;
	}

	public void calcX(@Nonnull ChartData data, @Nonnull Coord2DDbl[] pos, double minX, double maxX)
	{
		if (data instanceof Int32Data)
		{
			double leng = (maxX - minX);
			double ratio = leng / (double)(max - min);
			int[] iArr = ((Int32Data)data).getData();
			int i = 0;
			int j = iArr.length;
			while (i < j)
			{
				pos[i].x = minX + (iArr[i] - min) * ratio;
				i++;
			}
		}
	}
	
	public void calcY(@Nonnull ChartData data, @Nonnull Coord2DDbl[] pos, double minY, double maxY)
	{
		if (data instanceof Int32Data)
		{
			double leng = (maxY - minY);
			double ratio = leng / (double)(max - min);
			int[] iArr = ((Int32Data)data).getData();
			int i = 0;
			int j = iArr.length;
			while (i < j)
			{
				pos[i].y = minY + (iArr[i] - min) * ratio;
				i++;
			}
		}
	}

	public void extendRange(@Nonnull Int32Data data)
	{
		int[] dataArr = data.getData();
		int v;
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
	
	public void extendRange(int v)
	{
		if (v < min) min = v;
		if (v > max) max = v;
	}

	public int getMax()
	{
		return this.max;
	}

	public int getMin()
	{
		return this.min;
	}
}
