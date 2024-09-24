package org.sswr.util.basic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

//NN
public class ArrayMap<T, V> implements Map<T, V>
{
	protected SortableArrayList<T> keys;
	protected ArrayList<V> vals;

	public ArrayMap()
	{
		this.vals = new ArrayList<V>();
	}

	@Override
	public void clear()
	{
		this.keys.clear();
		this.vals.clear();
	}

	@Override
	public boolean containsKey(Object key)
	{
		@SuppressWarnings("unchecked")
		T t = (T)key;
		return this.keys.sortedIndexOf(t) >= 0;
	}
	@Override
	public boolean containsValue(Object value) {
		return this.vals.indexOf(value) >= 0;
	}

	@Override
	public Set<Entry<T, V>> entrySet()
	{
		return null;
	}

	@Override
	public V get(Object key) {
		int i;
		@SuppressWarnings("unchecked")
		T t = (T)key;
		i = this.keys.sortedIndexOf(t);
		if (i >= 0)
		{
			return this.vals.get(i);
		}
		else
		{
			return null;
		}
	}

	@Override
	public boolean isEmpty()
	{
		return this.keys.size() == 0;
	}

	@Override
	public Set<T> keySet()
	{
		return Set.copyOf(this.keys);
	}

	@Override
	public V put(T key, V val)
	{
		int i;
		i = this.keys.sortedIndexOf(key);
		if (i >= 0)
		{
			V oldVal = this.vals.get(i);
            this.vals.set(i, val);
			return oldVal;
		}
		else
		{
			this.keys.add(~i, key);
			this.vals.add(~i, val);
			return null;
		}
	}

	@Override
	public void putAll(Map<? extends T, ? extends V> m)
	{
		Iterator<? extends T> tList = m.keySet().iterator();
		while (tList.hasNext())
		{
			T t = tList.next();
			this.put(t, m.get(t));
		}
	}

	@Override
	public V remove(Object key)
	{
		int i;
		@SuppressWarnings("unchecked")
		T t = (T)key;
		i = this.keys.sortedIndexOf(t);
		if (i >= 0)
		{
			this.keys.remove(i);
			return this.vals.remove(i);
		}
		else
		{
			return null;
		}
	}

	@Override
	public int size()
	{
		return this.keys.size();
	}

	@Override
	public Collection<V> values() {
		return this.vals;
	}

	public @Nullable T getKey(int index)
	{
		return this.keys.get(index);
	}

	public int getIndex(@Nullable T key)
	{
		return this.keys.sortedIndexOf(key);
	}

	public @Nonnull ArrayList<V> getValueList()
	{
		return this.vals;
	}

	public @Nonnull SortableArrayList<T> getKeys()
	{
		return this.keys;
	}
}
