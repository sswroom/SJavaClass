package org.sswr.util.io;

import java.io.IOException;
import java.io.InputStream;

public class MyInputStream extends InputStream
{
	private IOStream stm;

	MyInputStream(IOStream stm)
	{
		this.stm = stm;
	}

	@Override
	public void close()
	{
		this.stm.close();
	}

	@Override
	public int read() throws IOException
	{
		byte buff[] = new byte[1];
		this.stm.read(buff, 0, 1);
		return buff[0] & 0xff;
	}
	
	@Override
	public int read(byte[] b)
	{
		return this.stm.read(b, 0, b.length);
	}

	@Override
	public int read(byte[] b, int off, int len)
	{
		return this.stm.read(b, off, len);
	}

	@Override
	public long skip(long n)
	{
		if (stm instanceof SeekableStream)
		{
			SeekableStream sstm = (SeekableStream)stm;
			long pos = sstm.getPosition();
			return sstm.seekFromCurrent(n) - pos;
		}
		return 0;
	}
}
