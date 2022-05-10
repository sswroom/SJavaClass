package org.sswr.util.io;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class UTF8Writer implements IOWriter
{
	private IOStream ioStm;
	private OutputStream stm;

	public UTF8Writer(IOStream stm)
	{
		this.ioStm = stm;
	}

	public UTF8Writer(OutputStream stm)
	{
		this.stm = stm;
	}

	public void close()
	{
	}

	public boolean writeStr(String str)
	{
		byte[] buff = str.getBytes(StandardCharsets.UTF_8);
		if (this.ioStm != null)
		{
			return this.ioStm.write(buff, 0, buff.length) == buff.length;
		}
		try
		{
			this.stm.write(buff);
			return true;
		}
		catch (IOException ex)
		{
			return false;
		}
	}

	public boolean writeLine(String str)
	{
		byte[] buff = str.getBytes(StandardCharsets.UTF_8);
		if (this.ioStm != null)
		{
			if (this.ioStm.write(buff, 0, buff.length) != buff.length)
				return false;
			buff = new byte[2];
			buff[0] = 13;
			buff[1] = 10;
			return (this.ioStm.write(buff, 0, buff.length) == buff.length);
		}
		try
		{
			this.stm.write(buff);
			buff = new byte[2];
			buff[0] = 13;
			buff[1] = 10;
			this.stm.write(buff);
			return true;
		}
		catch (IOException ex)
		{
			return false;
		}
	}

	public boolean writeLine()
	{
		byte[] buff = new byte[2];
		buff[0] = 13;
		buff[1] = 10;
		if (this.ioStm != null)
		{
			return this.ioStm.write(buff, 0, buff.length) == buff.length;
		}
		try
		{
			this.stm.write(buff);
			return true;
		}
		catch (IOException ex)
		{
			return false;
		}
	}

	public void writeSignature()
	{
		if (this.ioStm == null)
		{
			return;
		}
		if (this.ioStm instanceof SeekableStream)
		{
			if (((SeekableStream)this.ioStm).getPosition() == 0)
			{
				byte[] buff = new byte[3];
				buff[0] = (byte)0xEF;
				buff[1] = (byte)0xBB;
				buff[2] = (byte)0xBF;
				this.ioStm.write(buff, 0, 3);
			}
		}
	}
}
