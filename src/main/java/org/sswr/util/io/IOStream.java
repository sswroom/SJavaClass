package org.sswr.util.io;

import java.io.InputStream;
import java.io.OutputStream;

public abstract class IOStream extends ParsedObject
{
	public IOStream(String sourceName)
	{
		super(sourceName);
	}

	public abstract int read(byte []buff, int ofst, int size);
	public abstract int write(byte []buff, int ofst, int size);
	public abstract int flush();
	public abstract void close();
	public abstract boolean recover();

	public InputStream createInputStream()
	{
		return new MyInputStream(this);
	}

	public OutputStream createOutputStream()
	{
		return new MyOutputStream(this);
	}
}
