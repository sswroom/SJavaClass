package org.sswr.util.basic;

public class ArrayListInt64 extends SortableArrayList<Long>
{
	@Override
	public int compareItem(Long obj1, Long obj2)
	{
		if (obj1 > obj2)
		{
			return 1;
		}
		else if (obj1 < obj2)
		{
			return -1;
		}
		else
		{
			return 0;
		}
	}
}
