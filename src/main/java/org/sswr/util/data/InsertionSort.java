package org.sswr.util.data;

import java.util.Comparator;
import java.util.List;

import jakarta.annotation.Nonnull;

public class InsertionSort
{
	static <T> void sortB(@Nonnull List<T> arr, @Nonnull Comparator<T> func, int left, int right)
	{
		int i;
		int j;
		int k;
		int l;
		T temp;
		T temp1;
		T temp2;
		temp1 = arr.get(left);
		i = left + 1;
		while (i <= right)
		{
			temp2 = arr.get(i);
			if ( func.compare(temp1, temp2) > 0)
			{
				j = left;
				k = i - 1;
				while (j <= k)
				{
					l = (j + k) >> 1;
					temp = arr.get(l);
					if (func.compare(temp, temp2) > 0)
					{
						k = l - 1;
					}
					else
					{
						j = l + 1;
					}
				}
				DataTools.copyList(arr, j + 1, j, i - j);
				arr.set(j, temp2);
			}
			else
			{
				temp1 = temp2;
			}
			i++;
		}
	}
	
	static <T extends Comparable<T>> void sortB(@Nonnull List<T> arr, int left, int right)
	{
		int i;
		int j;
		int k;
		int l;
		T temp;
		T temp1;
		T temp2;
		temp1 = arr.get(left);
		i = left + 1;
		while (i <= right)
		{
			temp2 = arr.get(i);
			if ( temp1.compareTo(temp2) > 0)
			{
				j = left;
				k = i - 1;
				while (j <= k)
				{
					l = (j + k) >> 1;
					temp = arr.get(l);
					if (temp.compareTo(temp2) > 0)
					{
						k = l - 1;
					}
					else
					{
						j = l + 1;
					}
				}
				DataTools.copyList(arr, j + 1, j, (i - j));
				arr.set(j, temp2);
			}
			else
			{
				temp1 = temp2;
			}
			i++;
		}
	}
}
