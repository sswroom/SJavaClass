package org.sswr.util.io;

public abstract class SeekableStream extends IOStream
{
	public SeekableStream(String sourceName)
	{
		super(sourceName);
	}

	public abstract long seekFromBeginning(long position);
	public abstract long seekFromCurrent(long position);
	public abstract long seekFromEnd(long position);
	public abstract long getPosition();
	public abstract long getLength();
}
