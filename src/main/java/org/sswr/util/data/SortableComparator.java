package org.sswr.util.data;

import java.util.Comparator;

import jakarta.annotation.Nonnull;

public class SortableComparator<T> implements Comparator<T> {
	private SortableArrayList<T> list;
	SortableComparator(@Nonnull SortableArrayList<T> list)
	{
		this.list = list;
	}

	public int compare(T a, T b)
	{
		return this.list.compare(a, b);
	}
}
