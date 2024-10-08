package org.sswr.util.io;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import org.sswr.util.basic.ThreadEvent;
import org.sswr.util.data.DateTimeUtil;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class MTFileLog implements Runnable, LogHandler
{
	private LogType logStyle;
	private LogGroup groupStyle;
	private int lastVal;
	private Writer log;
	private String dateFormat;
	private String fileName;
	private String extName;
	private boolean closed;
	private boolean running;
	private ThreadEvent evt;
	private List<ZonedDateTime> dateList;
	private List<String> msgList;

	@Nonnull
	private String getNewName(@Nonnull ZonedDateTime logTime)
	{
		StringBuilder sb = new StringBuilder();
		ZonedDateTime time = logTime;

		switch (this.groupStyle)
		{
		case NO_GROUP:
			sb.append(this.fileName);
			break;
		case PER_YEAR:
			sb.append(this.fileName);
			sb.append(DateTimeUtil.toString(time, "yyyy"));
			new File(sb.toString()).mkdirs();
			sb.append(File.separator);
			sb.append(this.extName);
			break;
		case PER_MONTH:
			sb.append(this.fileName);
			sb.append(DateTimeUtil.toString(time, "yyyyMM"));
			new File(sb.toString()).mkdirs();
			sb.append(File.separator);
			sb.append(this.extName);
			break;
		case PER_DAY:
			sb.append(this.fileName);
			sb.append(DateTimeUtil.toString(time, "yyyyMMdd"));
			new File(sb.toString()).mkdirs();
			sb.append(File.separator);
			sb.append(this.extName);
			break;
		}

		switch (this.logStyle)
		{
		case SINGLE_FILE:
			break;
		case PER_HOUR:
			this.lastVal = time.getDayOfMonth() * 24 + time.getHour();
			sb.append(DateTimeUtil.toString(time, "yyyyMMddHH"));
			sb.append(".log");
			break;
		case PER_DAY:
			this.lastVal = time.getDayOfMonth();
			sb.append(DateTimeUtil.toString(time, "yyyyMMdd"));
			sb.append(".log");
			break;
		case PER_MONTH:
			this.lastVal = time.getMonthValue();
			sb.append(DateTimeUtil.toString(time, "yyyyMM"));
			sb.append(".log");
			break;
		case PER_YEAR:
			this.lastVal = time.getYear();
			sb.append(DateTimeUtil.toString(time, "yyyy"));
			sb.append(".log");
			break;
		}

		return sb.toString();
	}

	private void writeArr(@Nonnull List<String> msgArr, @Nonnull List<ZonedDateTime> dateArr)
	{
		int i = 0;
		int arrCnt;
		synchronized(msgArr)
		{
			arrCnt = msgArr.size();
		}
		while (i < arrCnt)
		{
			String newFile = null;
			ZonedDateTime logTime;
			String logMsg;
			synchronized (msgArr)
			{
				logTime = dateArr.get(i);				
				logMsg = msgArr.get(i);
			}
			ZonedDateTime time = logTime;
		
			if (this.logStyle == LogType.PER_DAY)
			{
				if (time.getDayOfMonth() != this.lastVal)
				{
					newFile = this.getNewName(logTime);
				}
			}
			else if (this.logStyle == LogType.PER_MONTH)
			{
				if (time.getMonthValue() != this.lastVal)
				{
					newFile = this.getNewName(logTime);
				}
			}
			else if (this.logStyle == LogType.PER_YEAR)
			{
				if (time.getYear() != this.lastVal)
				{
					newFile = this.getNewName(logTime);
				}
			}
			else if (this.logStyle == LogType.PER_HOUR)
			{
				if (this.lastVal != (time.getDayOfMonth() * 24 + time.getHour()))
				{
					newFile = this.getNewName(logTime);
				}
			}
		
			if (newFile != null)
			{
				try
				{
					this.log.close();
				}
				catch (IOException ex)
				{
					ex.printStackTrace();
				}
		
				try
				{
					this.log = new FileWriter(newFile, StandardCharsets.UTF_8, true);
					this.log.write(DateTimeUtil.toString(time, this.dateFormat)+"Program running\r\n");
					this.log.flush();
				}
				catch (IOException ex)
				{
					ex.printStackTrace();
					this.log = new NullWriter();
				}
			}
		
			StringBuilder sb = new StringBuilder();
			sb.append(DateTimeUtil.toString(time, this.dateFormat));
			sb.append(logMsg);
			sb.append("\r\n");
			try
			{
				log.write(sb.toString());
			}
			catch (IOException ex)
			{
				ex.printStackTrace();
			}

			i++;
		}
		if (this.log != null)
		{
			try
			{
				this.log.flush();
			}
			catch (IOException ex)
			{

			}
		}
	}

	public MTFileLog(@Nonnull String fileName, @Nonnull LogType style, @Nonnull LogGroup groupStyle, @Nullable String dateFormat)
	{
		this.evt = new ThreadEvent(true);
		this.dateList = new ArrayList<ZonedDateTime>();
		this.msgList = new ArrayList<String>();
		if (dateFormat != null)
		{
			this.dateFormat = dateFormat + "\t";
		}
		else
		{
			this.dateFormat = "yyyy-MM-dd HH:mm:ss\t";
		}
		this.logStyle = style;
		this.groupStyle = groupStyle;
		this.closed = false;
		this.running = false;
	
		int i;
	
		this.fileName = FileUtil.getRealPath(fileName, false);
		if (this.groupStyle != LogGroup.NO_GROUP)
		{
			i = this.fileName.lastIndexOf(File.separatorChar);
			this.extName = this.fileName.substring(i + 1);
		}
		else
		{
			this.extName = null;
			i = this.fileName.lastIndexOf(File.separatorChar);
			if (i >= 0)
			{
				File logDir = new File(this.fileName.substring(0, i));
				if (!logDir.exists())
				{
					logDir.mkdirs();
				}
			}
		}
	
		String newFileName = getNewName(ZonedDateTime.now());
	
		try
		{
			this.log = new FileWriter(newFileName, StandardCharsets.UTF_8, true);
			//this.log.write((char)0xfeff);
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
			this.log = new NullWriter();
		}
		new Thread(this).start();
	}

	public void logClosed()
	{
		if (!this.closed)
		{
			this.closed = true;
			this.evt.set();
		}
	}

	public synchronized void logAdded(@Nonnull ZonedDateTime logTime, @Nonnull String logMsg, @Nonnull LogLevel logLev)
	{
		if (this.closed)
			return;

		synchronized(this)
		{
			this.msgList.add(logMsg);
			this.dateList.add(logTime);
		}
		this.evt.set();
	}

	@Override
	public void run() {
		int arrCnt;
		List<String> msgArr = new ArrayList<String>();
		List<ZonedDateTime> dateArr = new ArrayList<ZonedDateTime>();
		this.running = true;
		while (!this.closed)
		{
			synchronized(this)
			{
				if ((arrCnt = this.msgList.size()) > 0)
				{
					msgArr = new ArrayList<String>();
					dateArr = new ArrayList<ZonedDateTime>();
					msgArr.addAll(this.msgList.subList(0, arrCnt));
					dateArr.addAll(this.dateList.subList(0, arrCnt));
					this.msgList.removeAll(msgArr);
					this.dateList.removeAll(dateArr);
				}
			}
			
			if (arrCnt > 0)
			{
				this.writeArr(msgArr, dateArr);
			}
			this.evt.waitEvent(1000);
		}
	
		if ((arrCnt = this.msgList.size()) > 0)
		{
			synchronized(this)
			{
				msgArr = new ArrayList<String>();
				dateArr = new ArrayList<ZonedDateTime>();
				msgArr.addAll(this.msgList.subList(0, arrCnt));
				dateArr.addAll(this.dateList.subList(0, arrCnt));
				this.msgList.removeAll(msgArr);
				this.dateList.removeAll(dateArr);
			}
			this.writeArr(msgArr, dateArr);
		}
	
		this.running = false;
	}

	public boolean isRunning()
	{
		return this.running;
	}
}
