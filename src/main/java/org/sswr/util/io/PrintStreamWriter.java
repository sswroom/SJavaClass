package org.sswr.util.io;

import java.io.PrintStream;

import jakarta.annotation.Nonnull;

public class PrintStreamWriter implements IOWriter
{
	private PrintStream stm;
	
	public PrintStreamWriter(@Nonnull PrintStream stm)
	{
		this.stm = stm;
	}

	@Override
	public boolean writeStr(@Nonnull String str)
	{
		this.stm.print(str);
		return true;
	}

	@Override
	public boolean writeLine(@Nonnull String str) {
		this.stm.println(str);
		return true;
	}

	@Override
	public boolean writeLine() {
		this.stm.println();
		return true;
	}
	
}
