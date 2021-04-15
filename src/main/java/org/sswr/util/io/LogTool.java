package org.sswr.util.io;

import java.io.PrintStream;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

public class LogTool
{
	private List<LogHandler> hdlrArr;
	private List<LogLevel> levArr;
	private List<LogHandler> fileLogArr;
	private boolean closed;

	public LogTool()
	{
		this.hdlrArr = new ArrayList<LogHandler>();
		this.levArr = new ArrayList<LogLevel>();
		this.fileLogArr = new ArrayList<LogHandler>();
		this.closed = false;
	}

	public void close()
	{
		if (this.closed)
			return;
		this.closed = true;
		synchronized(this.hdlrArr)
		{
			int i = hdlrArr.size();
			long t = System.currentTimeMillis();
			while (i-- > 0)
			{
				hdlrArr.get(i).logAdded(t, "End logging normally", LogLevel.FORCE);
				hdlrArr.get(i).logClosed();
			}
		}
	}
	
	public void addFileLog(String fileName, LogType style, LogGroup groupStyle, LogLevel logLev, String dateFormat, boolean directWrite)
	{
		if (closed)
			return;
		if (directWrite)
		{
			FileLog logs = new FileLog(fileName, style, groupStyle, dateFormat, ZoneOffset.systemDefault());
			this.addLogHandler(logs, logLev);
			fileLogArr.add(logs);
		}
		else
		{
			MTFileLog logs = new MTFileLog(fileName, style, groupStyle, dateFormat, ZoneOffset.systemDefault());
			this.addLogHandler(logs, logLev);
			fileLogArr.add(logs);
		}
	}
	
	public LogTool addPrintLog(PrintStream pstm, LogLevel logLev)
	{
		if (closed)
			return this;
		this.addLogHandler(new PrintStreamLog(pstm), logLev);
		return this;
	}
	
	public void addLogHandler(LogHandler hdlr, LogLevel logLev)
	{
		if (this.closed)
			return;
		synchronized(this.hdlrArr)
		{
			this.hdlrArr.add(hdlr);
			this.levArr.add(logLev);
		}
	
		long t = System.currentTimeMillis();
		StringBuilder sb = new StringBuilder();
		sb.append("Program ");
		sb.append(JavaEnv.getProgName());
		sb.append(" started");
		hdlr.logAdded(t, sb.toString(), LogLevel.FORCE);
	}
	
	public void removeLogHandler(LogHandler hdlr)
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
	
	public void logMessage(String logMsg, LogLevel level)
	{
		int iLevel = level.ordinal();
		long t = System.currentTimeMillis();
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

	public void logException(Exception ex)
	{
		this.logMessage(ex.toString(), LogLevel.ERR_DETAIL);
	}
}
