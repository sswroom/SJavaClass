package org.sswr.util.io;

import java.io.PrintStream;
import java.time.ZonedDateTime;

public class PrintStreamLog implements LogHandler
{
	private PrintStream pstm;

	public PrintStreamLog(PrintStream pstm)
	{
		this.pstm = pstm;
	}

	@Override
	public void logAdded(ZonedDateTime logTime, String logMsg, LogLevel logLev) {
		this.pstm.println(logMsg);
	}

	@Override
	public void logClosed() {
		pstm.close();
	}
}
