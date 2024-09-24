package org.sswr.util.basic;

import java.util.ArrayList;

import jakarta.annotation.Nullable;

public abstract class SortableArrayList<T> extends ArrayList<T>
{
	private static final long serialVersionUID = -4816476556L;
	
	public abstract int compareItem(@Nullable T obj1, @Nullable T obj2);

	public int sortedInsert(@Nullable T val)
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
			l = compareItem(this.get(k), val);
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

	public int sortedIndexOf(@Nullable T val)
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
			l = this.compareItem(this.get(k), val);
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
}
