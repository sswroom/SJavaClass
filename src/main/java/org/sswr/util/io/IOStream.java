package org.sswr.util.io;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import jakarta.annotation.Nonnull;

public abstract class IOStream extends ParsedObject
{
	public IOStream(@Nonnull String sourceName)
	{
		super(sourceName);
	}

	public void dispose()
	{
		this.close();
	}

	public abstract boolean isDown();
	public abstract int read(@Nonnull byte []buff, int ofst, int size);
	public abstract int write(@Nonnull byte []buff, int ofst, int size);
	public abstract int flush();
	public abstract void close();
	public abstract boolean recover();

	public int write(@Nonnull byte[] buff)
	{
		return write(buff, 0, buff.length);
	}

	@Nonnull
	public InputStream createInputStream()
	{
		return new MyInputStream(this);
	}

	@Nonnull
	public OutputStream createOutputStream()
	{
		return new MyOutputStream(this);
	}

	@Nonnull
	public ParserType getParserType()
	{
		return ParserType.Stream;
	}

	@Nonnull
	public byte[] readToEnd()
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buff = new byte[2048];
		int readSize;
		while (true)
		{
			readSize = this.read(buff, 0, 2048);
			if (readSize <= 0)
			{
				break;
			}
			baos.write(buff, 0, readSize);
		}
		return baos.toByteArray();
	}
}
