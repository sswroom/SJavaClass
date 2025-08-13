package org.sswr.util.data;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public abstract class SortableArrayList<T> extends ArrayList<T> implements Comparator<T>
{
	public SortableArrayList()
	{
		super();
	}

	public SortableArrayList(int capacity)
	{
		super(capacity);
	}

	public int sortedInsert(T val)
	{
		int i;
		int j;
		int k;
		int l;
		i = 0;
		j = this.size() - 1;
		while (i <= j)
		{
			k = (i + j) >> 1;
			l = this.compare(this.get(k), val);
			if (l > 0)
			{
				j = k - 1;
			}
			else if (l < 0)
			{
				i = k + 1;
			}
			else
			{
				i = k + 1;
				break;
			}
		}
		this.add(i, val);
		return i;
	}
	
	public int sortedIndexOf(T val)
	{
		int i;
		int j;
		int k;
		int l;
		i = 0;
		j = this.size() - 1;
		while (i <= j)
		{
			k = (i + j) >> 1;
			l = this.compare(this.get(k), val);
			if (l > 0)
			{
				j = k - 1;
			}
			else if (l < 0)
			{
				i = k + 1;
			}
			else
			{
				return k;
			}
		}
		return -i - 1;
	}

	public abstract int compare(T a, T b);
	public abstract double toValue(T v);

	public T min()
	{
		int i = this.size();
		if (i == 0)
			return null;
		T v = this.get(0);
		while (i-- > 1)
		{
			if (this.compare(this.get(i), v) < 0)
			{
				v = this.get(i);
			}
		}
		return v;
	}

	public T max()
	{
		int i = this.size();
		if (i == 0)
			return null;
		T v = this.get(0);
		while (i-- > 1)
		{
			if (this.compare(this.get(i), v) > 0)
			{
				v = this.get(i);
			}
		}
		return v;
	}

	public Double Mean()
	{
		double sum = 0;
		int i = this.size();
		while (i-- > 0)
		{
			sum += this.toValue(this.get(i));
		}
		return sum / (double)this.size();
	}

	public T Median()
	{
		int cnt = this.size();
		if (cnt == 0)
			return null;
		if (cnt == 1)
			return this.get(0);
		List<T> tmpArr = new ArrayList<T>(cnt);
		int i = 0;
		while (i < cnt)
		{
			tmpArr.add(this.get(i));
		}
		ArtificialQuickSort.sort(tmpArr, this);
		return tmpArr.get(cnt >> 1);
	}
}
