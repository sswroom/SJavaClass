package org.sswr.util.data;

import java.util.Comparator;
import java.util.List;

import jakarta.annotation.Nonnull;

public class ArtificialQuickSort
{
	private static <T> void presort(@Nonnull List<T> arr, @Nonnull Comparator<T> func, int left, int right)
	{
		T temp = null;
	
		while (left < right)
		{
			if (func.compare(arr.get(left), arr.get(right)) > 0)
			{
				temp = arr.get(left);
				arr.set(left, arr.get(right));
				arr.set(right, temp);
			}
			left++;
			right--;
		}
	}
	
	private static <T extends Comparable<T>> void presort(@Nonnull List<T> arr, int left, int right)
	{
		T temp = null;
	
		while (left < right)
		{
			if (arr.get(left).compareTo(arr.get(right)) > 0)
			{
				temp = arr.get(left);
				arr.set(left, arr.get(right));
				arr.set(right, temp);
			}
			left++;
			right--;
		}
	}	

	private static <T> void sort(@Nonnull List<T> arr, @Nonnull Comparator<T> func, int firstIndex, int lastIndex)
	{
		int levi[] = new int[32768];
		int desni[] = new int[32768];
		int index;
		int i;
		int left;
		int right;
		T meja;
		int left1;
		int right1;
		T temp;
	
		presort(arr, func, firstIndex, lastIndex);
	
		index = 0;
		levi[index] = firstIndex;
		desni[index] = lastIndex;
	
		while ( index >= 0 )
		{
			left = levi[index];
			right = desni[index];
			i = right - left;
			if (i <= 0)
			{
				index--;
			}
			else if (i <= 64)
			{
				InsertionSort.sortB(arr, func, left, right);
				index--;
			}
			else
			{
				meja = arr.get( (left + right) >> 1 );
				left1 = left;
				right1 = right;
				while (true)
				{
					while ( func.compare(arr.get(right1), meja) >= 0 )
					{
						if (--right1 < left1)
							break;
					}
					while ( func.compare(arr.get(left1), meja) < 0 )
					{
						if (++left1 > right1)
							break;
					}
					if (left1 > right1)
						break;
	
					temp = arr.get(right1);
					arr.set(right1--, arr.get(left1));
					arr.set(left1++, temp);
				}
				if (left1 == left)
				{
					arr.set((left + right) >> 1, arr.get(left));
					arr.set(left, meja);
					levi[index] = left + 1;
					desni[index] = right;
				}
				else
				{
					desni[index] = --left1;
					right1++;
					index++;
					levi[index] = right1;
					desni[index] = right;
				}
			}
		}
	}
	
	private static <T extends Comparable<T>> void sort(@Nonnull List<T> arr, int firstIndex, int lastIndex)
	{
		int levi[] = new int[32768];
		int desni[] = new int[32768];
		int index;
		int i;
		int left;
		int right;
		T meja;
		int left1;
		int right1;
		T temp;

		presort(arr, firstIndex, lastIndex);
	
		index = 0;
		levi[index] = firstIndex;
		desni[index] = lastIndex;
	
		while ( index >= 0 )
		{
			left = levi[index];
			right = desni[index];
			i = right - left;
			if (i <= 0)
			{
				index--;
			}
			else if (i <= 64)
			{
				InsertionSort.sortB(arr, left, right);
				index--;
			}
			else
			{
				meja = arr.get( (left + right) >> 1 );
				left1 = left;
				right1 = right;
				while (true)
				{
					while ( arr.get(right1).compareTo(meja) >= 0 )
					{
						if (--right1 < left1)
							break;
					}
					while ( arr.get(left1).compareTo(meja) < 0 )
					{
						if (++left1 > right1)
							break;
					}
					if (left1 > right1)
						break;
	
					temp = arr.get(right1);
					arr.set(right1--, arr.get(left1));
					arr.set(left1++, temp);
				}
				if (left1 == left)
				{
					arr.set((left + right) >> 1, arr.get(left));
					arr.set(left, meja);
					levi[index] = left + 1;
					desni[index] = right;
				}
				else
				{
					desni[index] = --left1;
					right1++;
					index++;
					levi[index] = right1;
					desni[index] = right;
				}
			}
		}
	}
	
	public static <T> void sort(@Nonnull List<T> arr, @Nonnull Comparator<T> func)
	{
		sort(arr, func, 0, arr.size() - 1);
	}

	public static <T extends Comparable<T>> void sort(@Nonnull List<T> arr)
	{
		sort(arr, 0, arr.size() - 1);
	}
}
