package org.sswr.util.basic;

import jakarta.annotation.Nullable;

public class ArrayListInt64 extends SortableArrayListNum<Long>
{
	private static final long serialVersionUID = 5314653313L;

	@Override
	public int compare(@Nullable Long obj1, @Nullable Long obj2)
	{
		long i1;
		long i2;
		if (obj1 == null)
			i1 = 0;
		else
			i1 = obj1.longValue();
		if (obj2 == null)
			i2 = 0;
		else
			i2 = obj2.longValue();
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
}
