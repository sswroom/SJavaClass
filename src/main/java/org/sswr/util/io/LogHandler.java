package org.sswr.util.io;

import java.time.ZonedDateTime;

public interface LogHandler
{
	public void logAdded(ZonedDateTime logTime, String logMsg, LogLevel logLev);
	public void logClosed();
}
