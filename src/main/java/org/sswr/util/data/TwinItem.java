package org.sswr.util.data;

public class TwinItem<K,V>
{
	public K key;
	public V value;
	
	public TwinItem(K key, V value)
	{
		this.key = key;
		this.value = value;
	}
}
