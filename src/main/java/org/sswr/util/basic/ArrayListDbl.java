package org.sswr.util.basic;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class ArrayListDbl extends SortableArrayListNum<Double>
{
	private static final long serialVersionUID = 379656752L;
	
	@Override
	public int compare(@Nullable Double obj1, @Nullable Double obj2)
	{
		double i1;
		double i2;
		if (obj1 == null)
			i1 = 0;
		else
			i1 = obj1.doubleValue();
		if (obj2 == null)
			i2 = 0;
		else
			i2 = obj2.doubleValue();
		if (i1 > i2)
		{
			return 1;
		}
		else if (i1 < i2)
		{
			return -1;
		}
		else
		{
			return 0;
		}
	}
	
	public double frobeniusNorm()
	{
		double sum = 0;
		double v;
		int i = this.size();
		while (i-- > 0)
		{
			v = this.get(i);
			sum += v * v;
		}
		return Math.sqrt(sum);
	}

	public double average()
	{
		double sum = 0;
		int i = this.size();
		while (i-- > 0)
		{
			sum += this.get(i);
		}
		return sum / (double)this.size();
	}

	public double stdDev()
	{
		double avg = this.average();
		double sum = 0;
		double d;
		int i = this.size();
		while (i-- > 0)
		{
			d = this.get(i) - avg;
			sum += d * d;
		}
		return Math.sqrt(sum / (double)this.size());
	}

	public @Nonnull ArrayListDbl subset(int firstIndex, int endIndex)
	{
		int objCnt = this.size();
		if (firstIndex < 0)
			firstIndex = 0;
		if (firstIndex > objCnt)
			firstIndex = objCnt;
		if (endIndex > objCnt)
			endIndex = objCnt;
		ArrayListDbl outList = new ArrayListDbl();
		while (firstIndex < endIndex)
		{
			outList.add(this.get(firstIndex));
			firstIndex++;
		}
		return outList;
	}

	public @Nonnull ArrayListDbl subset(int firstIndex)
	{
		return subset(firstIndex, this.size());
	}
}
