package org.sswr.util.basic;

public class ArrayListInt32 extends SortableArrayList<Integer>
{
	private static final long serialVersionUID = 6357929868L;
	
	@Override
	public int compareItem(Integer obj1, Integer obj2)
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
