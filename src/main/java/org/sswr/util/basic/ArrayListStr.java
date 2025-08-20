package org.sswr.util.basic;

import jakarta.annotation.Nullable;

public class ArrayListStr extends SortableArrayList<String>
{
	private static final long serialVersionUID = 2229777143L;
	
	@Override
	public int compare(@Nullable String obj1, @Nullable String obj2)
	{
		if (obj1 == obj2)
			return 0;
		if (obj1 == null)
			return -1;
		if (obj2 == null)
			return 1;
		return obj1.compareTo(obj2);
	}
}
