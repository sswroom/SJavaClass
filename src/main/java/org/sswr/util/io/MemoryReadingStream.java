package org.sswr.util.io;

import org.sswr.util.data.ByteArray;
import org.sswr.util.data.ByteTool;

import jakarta.annotation.Nonnull;

public class MemoryReadingStream extends SeekableStream {
	private byte[] buff;
	private int buffOfst;
	private int buffEndOfst;
	private int currOfst;

	public MemoryReadingStream(@Nonnull byte[] buff, int buffOfst, int buffSize)
	{
		super("MemoryReadingStream");
		this.buff = buff;
		this.buffOfst = buffOfst;
		this.buffEndOfst = buffOfst + buffSize;
		this.currOfst = 0;
	}

	public MemoryReadingStream(@Nonnull ByteArray barr)
	{
		super("MemoryReadingStream");
		this.buff = barr.getBytes();
		this.buffOfst = barr.getBytesOffset();
		this.buffEndOfst = barr.getBytesOffset() + barr.getBytesLength();
		this.currOfst = 0;
	}

	public boolean isDown()
	{
		return false;
	}


	public int read(@Nonnull byte[] buff, int ofst, int size)
	{
		int readSize = size;
		if (this.buffEndOfst - this.currOfst < readSize)
		{
			readSize = this.buffEndOfst - this.currOfst;
		}
		ByteTool.copyArray(buff, ofst, this.buff, this.currOfst, readSize);
		this.currOfst += readSize;
		return readSize;
	}

	public int write(@Nonnull byte[] buff, int ofst, int size)
	{
		return 0;
	}

	public int flush()
	{
		return 0;
	}

	public void close()
	{
	}

	public boolean recover()
	{
		return true;
	}

	public long seekFromBeginning(long position)
	{
		long outPos = position + this.buffOfst;

		if (outPos > this.buffEndOfst)
			return this.currOfst - this.buffOfst;
		this.currOfst = (int)outPos;
		return this.currOfst - this.buffOfst;
	}

	public long seekFromCurrent(long position)
	{
		long outPos;
		if (position < 0 && this.currOfst + position < this.buffOfst)
			outPos = 0;
		else
			outPos = this.currOfst - (long)this.buffOfst + position;
		return seekFromBeginning(outPos);
	}

	public long seekFromEnd(long position)
	{
		long outPos;
		if (position < 0 && this.buffEndOfst - this.buffOfst < -position)
			outPos = 0;
		else
			outPos = ((long)this.buffEndOfst - this.buffOfst + position);
		return seekFromBeginning(outPos);
	}

	public long getPosition()
	{
		return this.currOfst - this.buffOfst;
	}

	public long getLength()
	{
		return this.buffEndOfst - this.buffOfst;
	}
}
