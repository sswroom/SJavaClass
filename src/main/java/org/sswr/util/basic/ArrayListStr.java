package org.sswr.util.basic;

public class ArrayListStr extends SortableArrayList<String>
{
	private static final long serialVersionUID = 2229777143L;
	
	@Override
	public int compareItem(String obj1, String obj2)
	{
		return obj1.compareTo(obj2);
	}
}
