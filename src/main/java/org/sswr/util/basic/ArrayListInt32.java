package org.sswr.util.basic;

import jakarta.annotation.Nullable;

public class ArrayListInt32 extends SortableArrayListNum<Integer>
{
	private static final long serialVersionUID = 6357929868L;
	
	@Override
	public int compare(@Nullable Integer obj1, @Nullable Integer obj2)
	{
		int i1;
		int i2;
		if (obj1 == null)
			i1 = 0;
		else
			i1 = obj1.intValue();
		if (obj2 == null)
			i2 = 0;
		else
			i2 = obj2.intValue();
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
