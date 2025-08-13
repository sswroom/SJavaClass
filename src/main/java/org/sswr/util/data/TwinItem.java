package org.sswr.util.data;

public class TwinItem<K,V>
{
	private K key;
	private V value;
	
	public TwinItem(K key, V value)
	{
		this.key = key;
		this.value = value;
	}
}
