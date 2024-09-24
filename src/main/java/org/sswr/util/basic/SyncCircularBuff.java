package org.sswr.util.basic;

import org.sswr.util.data.ByteTool;

import jakarta.annotation.Nullable;

public class SyncCircularBuff<T>
{
	private Object[] buff;
	private int getIndex;
	private int putIndex;

	public SyncCircularBuff()
	{
		this.buff = new Object[32];
		this.getIndex = 0;
		this.putIndex = 0;
	}


	public synchronized boolean hasItems()
	{
		return this.getIndex != this.putIndex;
	}
	
	public synchronized void put(T item)
	{
		if (((this.putIndex + 1) & (this.buff.length - 1)) == this.getIndex)
		{
			int oldCapacity = this.buff.length;
			Object []newBuff = new Object[oldCapacity << 1];
			if (this.getIndex < this.putIndex)
			{
				ByteTool.copyArray(newBuff, 0, this.buff, 0, oldCapacity);
			}
			else
			{
				ByteTool.copyArray(newBuff, 0, this.buff, this.getIndex, (oldCapacity - this.getIndex));
				ByteTool.copyArray(newBuff, oldCapacity - this.getIndex, this.buff, 0, this.putIndex);
				this.getIndex = 0;
				this.putIndex = oldCapacity - 1;
			}
			this.buff = newBuff;
		}
		this.buff[this.putIndex] = item;
		this.putIndex = (this.putIndex + 1) & (this.buff.length - 1);
	}
	
	public synchronized @Nullable T get()
	{
		if (this.getIndex == this.putIndex)
		{
			return null;
		}
		@SuppressWarnings("unchecked")
		T ret = (T)this.buff[this.getIndex];
		this.getIndex = (this.getIndex + 1) & (this.buff.length - 1);
		return ret;
	}
	
	public synchronized @Nullable T getNoRemove()
	{
		if (this.getIndex == this.putIndex)
		{
			return null;
		}
		@SuppressWarnings("unchecked")
		T ret = (T)this.buff[this.getIndex];
		return ret;
	}
	
	public synchronized @Nullable T getLastNoRemove()
	{
		if (this.getIndex == this.putIndex)
		{
			return null;
		}
		int lastIndex = (this.putIndex - 1) & (this.buff.length - 1);
		@SuppressWarnings("unchecked")
		T ret = (T)this.buff[lastIndex];
		return ret;
	}
	
	public synchronized int getCount()
	{
		if (this.getIndex <= this.putIndex)
		{
			return this.putIndex - this.getIndex;
		}
		else
		{
			return this.putIndex + this.buff.length - this.getIndex;
		}
	}
	
	public synchronized int indexOf(T item)
	{
		int andVal = this.buff.length - 1;
		int i = 0;
		int j;
		while (true)
		{
			j = (this.getIndex + i) & andVal;
			if (j == this.putIndex)
			{
				return -1;
			}
			if (this.buff[j] == item)
			{
				return i;
			}
			i++;
		}
	}
}
