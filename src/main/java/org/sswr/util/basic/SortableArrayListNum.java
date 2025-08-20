package org.sswr.util.basic;

public abstract class SortableArrayListNum<T extends Number> extends SortableArrayList<T>
{
	public double mean()
	{
		double sum = 0;
		int i = this.size();
		while (i-- > 0)
		{
			sum += this.get(i).doubleValue();
		}
		return sum / (double)this.size();
	}
}
