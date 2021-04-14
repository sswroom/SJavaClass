package org.sswr.util.io;

import java.io.IOException;
import java.io.Writer;

public class NullWriter extends Writer
{
	public NullWriter()
	{

	}
	
	@Override
	public void close() throws IOException {
	}

	@Override
	public void flush() throws IOException {
	}

	@Override
	public void write(char[] arg0, int arg1, int arg2) throws IOException {
	}
	
}
