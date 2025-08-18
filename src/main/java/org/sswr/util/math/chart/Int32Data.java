package org.sswr.util.math.chart;

import java.util.List;

import jakarta.annotation.Nonnull;

public class Int32Data implements ChartData {
	private @Nonnull int[] intArr;
	public Int32Data(@Nonnull int[] intArr, int ofst, int count)
	{
		this.intArr = new int[count];
		int i = count;
		while (i-- > 0)
		{
			this.intArr[i] = intArr[i + ofst];
		}
	}

	public Int32Data(@Nonnull List<Integer> intArr)
	{
		this.intArr = new int[intArr.size()];
		int i = intArr.size();
		while (i-- > 0)
		{
			this.intArr[i] = intArr.get(i).intValue();
		}
	}

	public @Nonnull DataType getType()
	{
		return DataType.Integer;
	}

	public @Nonnull ChartData clone()
	{
		return new Int32Data(this.intArr, 0, this.intArr.length);
	}

	public int getCount()
	{
		return this.intArr.length;
	}

	public @Nonnull int[] getData()
	{
		return this.intArr;
	}
}
