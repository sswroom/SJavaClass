package org.sswr.util.io;

import org.sswr.util.data.ByteTool;

public class MemoryStream extends SeekableStream
{
	public static final int MAX_CAPACITY = 1048576000;
	public static final int DEF_CAPACITY = 1024;

	private int capacity;
	private int currPtr;
	private int currSize;
	private byte[] memPtr;

	public MemoryStream(String dbg)
	{
		super("MemoryStream");

		this.capacity = DEF_CAPACITY;
		this.currSize = 0;
		this.currPtr = 0;
		this.memPtr = new byte[this.capacity];
	}

	public byte[] getBuff()
	{
		return this.memPtr;
	}

	@Override
	public boolean isDown()
	{
		return false;
	}

	@Override
	public int read(byte[] buff, int ofst, int size)
	{
		int readSize = size;
		if (this.currSize - this.currPtr < readSize)
		{
			readSize = this.currSize - this.currPtr;
		}
		ByteTool.copyArray(buff, ofst, this.memPtr, this.currPtr, readSize);
		this.currPtr += readSize;
		return readSize;
	}

	@Override
	public int write(byte[] buff, int ofst, int size)
	{
		int endPos = this.currPtr + size;

		if (endPos <= this.capacity)
		{
			ByteTool.copyArray(this.memPtr, this.currPtr, buff, ofst, size);
			if (endPos > this.currSize)
				this.currSize = endPos;
			this.currPtr = endPos;
			return size;
		}
		if (endPos > MAX_CAPACITY)
		{
			size = MAX_CAPACITY - this.currPtr;
			endPos = MAX_CAPACITY;
		}
		while (endPos > this.capacity)
		{
			int newCapacity = this.capacity << 1;
			byte[] newPtr;
			if (newCapacity > MAX_CAPACITY)
			{
				newCapacity = MAX_CAPACITY;
			}
			newPtr = new byte[newCapacity];
			ByteTool.copyArray(newPtr, 0, this.memPtr, 0, this.currSize);
			this.memPtr = newPtr;
			this.capacity = newCapacity;
		}
	
		ByteTool.copyArray(this.memPtr, this.currPtr, buff, ofst, size);
		if (endPos > this.currSize)
			this.currSize = endPos;
		this.currPtr = endPos;
		return size;
	}

	@Override
	public int flush()
	{
		return 0;
	}

	@Override
	public void close()
	{
	}

	@Override
	public boolean recover()
	{
		return true;
	}	

	@Override
	public long seekFromBeginning(long position)
	{
		long outPos = position;

		if (this.capacity == 0)
		{
			if (outPos > currSize)
				return this.currPtr;
			this.currPtr = (int)outPos;
			return this.currPtr;
		}
	
		if (outPos > MAX_CAPACITY)
		{
			return this.currPtr;
		}
		else if (outPos > currSize)
		{
			return this.currPtr;
		}
	
		while (outPos > this.capacity)
		{
			byte[] outPtr = new byte[this.capacity << 1];
			ByteTool.copyArray(outPtr, 0, this.memPtr, 0, this.currSize);
			this.capacity = this.capacity << 1;
			this.memPtr = outPtr;
		}
	
		if (this.currSize < outPos)
		{
			this.currSize = (int)outPos;
		}
	
		this.currPtr = (int)outPos;
		return outPos;
	}

	@Override
	public long seekFromCurrent(long position)
	{
		long outPos;
		if (position < 0 && this.currPtr < -position)
			outPos = 0;
		else
			outPos = this.currPtr + position;
		return seekFromBeginning(outPos);
	}

	@Override
	public long seekFromEnd(long position)
	{
		long outPos;
		if (position < 0 && this.currSize < -position)
			outPos = 0;
		else
			outPos = this.currSize + position;
		return seekFromBeginning(outPos);
	}

	@Override
	public long getPosition()
	{
		return this.currPtr;
	}

	@Override
	public long getLength()
	{
		return this.currSize;
	}

	public void clear()
	{
		this.currPtr = 0;
		this.currSize = 0;
	}
}
