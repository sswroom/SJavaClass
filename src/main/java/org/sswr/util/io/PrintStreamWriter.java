package org.sswr.util.io;

import java.io.PrintStream;

public class PrintStreamWriter implements IOWriter
{
	private PrintStream stm;
	
	public PrintStreamWriter(PrintStream stm)
	{
		this.stm = stm;
	}

	@Override
	public boolean writeStr(String str)
	{
		this.stm.print(str);
		return true;
	}

	@Override
	public boolean writeLine(String str) {
		this.stm.println(str);
		return true;
	}

	@Override
	public boolean writeLine() {
		this.stm.println();
		return true;
	}
	
}
