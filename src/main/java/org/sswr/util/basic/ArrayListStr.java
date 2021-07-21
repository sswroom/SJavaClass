package org.sswr.util.basic;

public class ArrayListStr extends SortableArrayList<String>
{
	@Override
	public int compareItem(String obj1, String obj2)
	{
		return obj1.compareTo(obj2);
	}
}
