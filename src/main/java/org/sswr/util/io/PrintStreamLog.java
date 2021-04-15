package org.sswr.util.io;

import java.io.PrintStream;

public class PrintStreamLog implements LogHandler
{
	private PrintStream pstm;

	public PrintStreamLog(PrintStream pstm)
	{
		this.pstm = pstm;
	}

	@Override
	public void logAdded(long logTime, String logMsg, LogLevel logLev) {
		this.pstm.println(logMsg);
	}

	@Override
	public void logClosed() {
		pstm.close();
	}
}
