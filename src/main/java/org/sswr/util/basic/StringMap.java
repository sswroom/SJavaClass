package org.sswr.util.basic;

public class StringMap<T> extends ArrayMap<String, T>
{
	public StringMap()
	{
		super(new ArrayListStr());
	}
}
