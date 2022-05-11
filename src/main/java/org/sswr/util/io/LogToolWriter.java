package org.sswr.util.io;

import org.sswr.util.data.StringUtil;

public class LogToolWriter implements IOWriter
{
	private StringBuilder sb;
	private LogTool log;
	private LogLevel level;

	public LogToolWriter(LogTool log, LogLevel level)
	{
		this.log = log;
		this.level = level;
		this.sb = new StringBuilder();
	}

	@Override
	public boolean writeStr(String str)
	{
		this.sb.append(str);
		this.flush();
		return true;
	}

	@Override
	public boolean writeLine(String str)
	{
		this.sb.append(str);
		this.flush();
		this.log.logMessage(this.sb.toString(), this.level);
		this.sb.setLength(0);
		return true;
	}

	@Override
	public boolean writeLine()
	{
		this.log.logMessage(this.sb.toString(), this.level);
		this.sb.setLength(0);
		return true;
	}
	
	private void flush()
	{
		String lines[] = StringUtil.splitLine(this.sb.toString());
		if (lines.length == 1)
		{
			return;
		}
		int i = 0;
		int j = lines.length - 1;
		while (i < j)
		{
			this.log.logMessage(lines[i], this.level);
			i++;
		}
		this.sb.setLength(0);
		this.sb.append(lines[j]);
	}
}
