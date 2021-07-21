package org.sswr.util.basic;

public class Int32Map<T> extends ArrayMap<Integer, T>
{
	public Int32Map()
	{
		this.keys = new ArrayListInt32();
	}
}
