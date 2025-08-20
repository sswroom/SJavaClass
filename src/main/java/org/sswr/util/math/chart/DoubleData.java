package org.sswr.util.math.chart;

import java.util.List;

import jakarta.annotation.Nonnull;

public class DoubleData implements ChartData
{
	private @Nonnull double[] dblArr;
	public DoubleData(@Nonnull double[] dblArr, int ofst, int count)
	{
		this.dblArr = new double[count];
		int i = count;
		while (i-- > 0)
		{
			this.dblArr[i] = dblArr[i + ofst];
		}
	}

	public DoubleData(@Nonnull List<Double> dblArr, int ofst, int count)
	{
		this.dblArr = new double[count];
		int i = count;
		while (i-- > 0)
		{
			this.dblArr[i] = dblArr.get(i + ofst).doubleValue();
		}
	}

	public @Nonnull DataType getType()
	{
		return DataType.DOUBLE;
	}

	public @Nonnull ChartData clone()
	{
		return new DoubleData(this.dblArr, 0, this.dblArr.length);
	}

	public int getCount()
	{
		return this.dblArr.length;
	}

	public @Nonnull double[] getData()
	{
		return this.dblArr;
	}
}
