package org.sswr.util.io;

import jakarta.annotation.Nonnull;

public abstract class SeekableStream extends IOStream
{
	public SeekableStream(@Nonnull String sourceName)
	{
		super(sourceName);
	}

	public abstract long seekFromBeginning(long position);
	public abstract long seekFromCurrent(long position);
	public abstract long seekFromEnd(long position);
	public abstract long getPosition();
	public abstract long getLength();
}
