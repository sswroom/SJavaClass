package org.sswr.util.io;

import java.io.PrintStream;
import java.time.ZonedDateTime;

import jakarta.annotation.Nonnull;

public class PrintStreamLog implements LogHandler
{
	private PrintStream pstm;

	public PrintStreamLog(@Nonnull PrintStream pstm)
	{
		this.pstm = pstm;
	}

	@Override
	public void logAdded(@Nonnull ZonedDateTime logTime, @Nonnull String logMsg, @Nonnull LogLevel logLev) {
		this.pstm.println(logMsg);
	}

	@Override
	public void logClosed() {
		pstm.close();
	}
}
