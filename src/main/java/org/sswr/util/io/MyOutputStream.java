package org.sswr.util.io;

import java.io.IOException;
import java.io.OutputStream;

import jakarta.annotation.Nonnull;

public class MyOutputStream extends OutputStream
{
	private IOStream stm;

	MyOutputStream(@Nonnull IOStream stm)
	{
		this.stm = stm;
	}

	@Override
	public void close()
	{
		this.stm.close();
	}

	@Override
	public void write(int b) throws IOException
	{
		byte buff[] = new byte[1];
		buff[0] = (byte)b;
		if (this.stm.write(buff, 0, 1) != 1)
		{
			throw new IOException();
		}
	}
	
	@Override
	public void write(byte[] b) throws IOException
	{
		if (this.stm.write(b, 0, b.length) != b.length)
		{
			throw new IOException();
		}
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException
	{
		if (this.stm.write(b, off, len) != len)
		{
			throw new IOException();
		}
	}
}
