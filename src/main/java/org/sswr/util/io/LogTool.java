package org.sswr.util.io;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class LogTool
{
	private List<LogHandler> hdlrArr;
	private List<LogLevel> levArr;
	private List<LogHandler> fileLogArr;
	private boolean closed;
	private boolean skipStarted;

	public LogTool()
	{
		this.hdlrArr = new ArrayList<LogHandler>();
		this.levArr = new ArrayList<LogLevel>();
		this.fileLogArr = new ArrayList<LogHandler>();
		this.closed = false;
		this.skipStarted = false;
	}

	public void close()
	{
		if (this.closed)
			return;
		this.closed = true;
		synchronized(this.hdlrArr)
		{
			int i = hdlrArr.size();
			ZonedDateTime t = ZonedDateTime.now();
			while (i-- > 0)
			{
				hdlrArr.get(i).logAdded(t, "End logging normally", LogLevel.FORCE);
				hdlrArr.get(i).logClosed();
			}
		}
	}
	
	public void addFileLog(@Nonnull String fileName, @Nonnull LogType style, @Nonnull LogGroup groupStyle, @Nonnull LogLevel logLev, @Nullable String dateFormat, boolean directWrite)
	{
		if (closed)
			return;
		if (dateFormat == null)
		{
			dateFormat = "yyyy-MM-dd HH:mm:ss.SSS";
		}
		if (directWrite)
		{
			FileLog logs = new FileLog(fileName, style, groupStyle, dateFormat);
			this.addLogHandler(logs, logLev);
			fileLogArr.add(logs);
		}
		else
		{
			MTFileLog logs = new MTFileLog(fileName, style, groupStyle, dateFormat);
			this.addLogHandler(logs, logLev);
			fileLogArr.add(logs);
		}
	}

	@Nonnull
	public LogTool addPrintLog(@Nonnull PrintStream pstm, @Nonnull LogLevel logLev)
	{
		if (closed)
			return this;
		this.addLogHandler(new PrintStreamLog(pstm), logLev);
		return this;
	}
	
	public void addLogHandler(@Nonnull LogHandler hdlr, @Nonnull LogLevel logLev)
	{
		if (this.closed)
			return;
		synchronized(this.hdlrArr)
		{
			this.hdlrArr.add(hdlr);
			this.levArr.add(logLev);
		}
	
		if (!this.skipStarted)
		{
			ZonedDateTime t = ZonedDateTime.now();
			StringBuilder sb = new StringBuilder();
			sb.append("Program ");
			sb.append(JavaEnv.getProgName());
			sb.append(" started");
			hdlr.logAdded(t, sb.toString(), LogLevel.FORCE);
		}
	}
	
	public void removeLogHandler(@Nonnull LogHandler hdlr)
	{
		if (this.closed)
			return;
		synchronized(this.hdlrArr)
		{
			int i = this.hdlrArr.size();
			while (i-- > 0)
			{
				if (this.hdlrArr.get(i) == hdlr)
				{
					this.hdlrArr.remove(i);
					this.levArr.remove(i);
					break;
				}
			}
		}
	}
	
	public void logMessage(@Nonnull String logMsg, @Nonnull LogLevel level)
	{
		int iLevel = level.ordinal();
		ZonedDateTime t = ZonedDateTime.now();
		synchronized(this.hdlrArr)
		{
			int i = this.hdlrArr.size();
			while (i-- > 0)
			{
				if (levArr.get(i).ordinal() >= iLevel)
					this.hdlrArr.get(i).logAdded(t, logMsg, level);
			}
		}
	}

	public void logException(@Nonnull Exception ex)
	{
		StringWriter writer = new StringWriter();
		ex.printStackTrace(new PrintWriter(writer));
		this.logMessage(writer.toString(), LogLevel.ERR_DETAIL);
	}
	
	public void skipStarted()
	{
		this.skipStarted = true;
	}
}
