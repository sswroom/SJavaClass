package org.sswr.util.basic;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import jakarta.annotation.Nullable;

public abstract class SortableArrayList<T> extends ArrayList<T> implements Comparator<T>
{
	private static final long serialVersionUID = -4816476556L;
	
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
			l = compare(this.get(k), val);
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

	public @Nullable T min()
	{
		int i = this.size();
		if (this.size() == 0)
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

	public @Nullable T max()
	{
		int i = this.size();
		if (this.size() == 0)
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

	public @Nullable T median()
	{
		int cnt = this.size();
		if (cnt == 0)
			return null;
		if (cnt == 1)
			return this.get(0);
		@SuppressWarnings("unchecked")
		T[] tmpArr = (T[])Array.newInstance(this.get(0).getClass(), cnt);
		int i = 0;
		while (i < cnt)
		{
			tmpArr[i] = this.get(i);
			i++;
		}
		Arrays.sort(tmpArr, this);
		return tmpArr[cnt >> 1];
	}	
}
