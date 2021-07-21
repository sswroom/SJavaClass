package org.sswr.util.basic;

public class Int64Map<T> extends ArrayMap<Long, T>
{
	public Int64Map()
	{
		this.keys = new ArrayListInt64();
	}	
}
