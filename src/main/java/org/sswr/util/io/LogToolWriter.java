package org.sswr.util.io;

import org.sswr.util.data.StringUtil;

import jakarta.annotation.Nonnull;

public class LogToolWriter implements IOWriter
{
	private StringBuilder sb;
	private LogTool log;
	private LogLevel level;

	public LogToolWriter(@Nonnull LogTool log, @Nonnull LogLevel level)
	{
		this.log = log;
		this.level = level;
		this.sb = new StringBuilder();
	}

	@Override
	public boolean writeStr(@Nonnull String str)
	{
		this.sb.append(str);
		this.flush();
		return true;
	}

	@Override
	public boolean writeLine(@Nonnull String str)
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
