package org.sswr.util.io;

import java.time.ZonedDateTime;

import jakarta.annotation.Nonnull;

public interface LogHandler
{
	public void logAdded(@Nonnull ZonedDateTime logTime, @Nonnull String logMsg, @Nonnull LogLevel logLev);
	public void logClosed();
}
