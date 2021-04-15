package org.sswr.util.io;

public interface LogHandler
{
	public void logAdded(long logTime, String logMsg, LogLevel logLev);
	public void logClosed();
}
