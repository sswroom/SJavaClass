package org.sswr.util.basic;

public class StringMap<T> extends ArrayMap<String, T>
{
	public StringMap()
	{
		this.keys = new ArrayListStr();
	}
}
