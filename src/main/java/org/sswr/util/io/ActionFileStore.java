package org.sswr.util.io;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import org.sswr.util.basic.MyThread;
import org.sswr.util.basic.ThreadVar;
import org.sswr.util.data.DateTimeUtil;
import org.sswr.util.db.CSVUtil;
import org.sswr.util.db.DBUpdateHandler;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class ActionFileStore implements Runnable, DBUpdateHandler {
	public enum ActionType {
		CREATE,
		UPDATE,
		DELETE
	}

	public class LogEntry
	{
		public long logTime;
		public String logLine;
	}

	private String logPath;
	private ArrayList<LogEntry> entries;

	private boolean threadRunning;
	private boolean threadToStop;
	private LocalDateTime lastEntryTime;
	private FileWriter fs;
	private LogTool logger;
	private String packageName;

	public ActionFileStore(@Nullable String logPath, @Nullable LogTool logger, @Nullable String packageName)
	{
		this.entries = new ArrayList<LogEntry>();
		this.threadRunning = false;
		this.threadToStop = false;
		this.logPath = logPath;
		this.logger = logger;
		this.packageName = packageName;
		if (this.logPath != null)
		{
			this.logPath = FileUtil.getRealPath(this.logPath);
			File file = new File(this.logPath);
			file.mkdirs();
			Thread t = new Thread(this);
			t.start();
		}
	}


	public void logAction(int stackSkip, @Nullable String userName, @Nonnull ActionType actType, @Nullable String fromData, @Nullable String toData)
	{
		StackTraceElement[] stacks = Thread.currentThread().getStackTrace();
		String className = null;
		String funcName = null;
		String utilPackage = this.getClass().getPackage().getName();
		int i = utilPackage.lastIndexOf(".");
		if (i >= 0)
		{
			utilPackage = utilPackage.substring(0, i);
		}

		i = stackSkip;
		int j = stacks.length;
		while (i < j)
		{
			if (stacks[i].getClassName().startsWith(utilPackage))
			{

			}
			else if (packageName == null || stacks[i].getClassName().startsWith(packageName))
			{
				className = stacks[i].getClassName();
				funcName = stacks[i].getMethodName();
				break;
			}
			i++;
		}

		Timestamp t = DateTimeUtil.timestampNow();
		String line = CSVUtil.join(new String[]{t.toString(), userName, className+"."+funcName, actType.name(), fromData, toData});
		synchronized(this.entries)
		{
			LogEntry ent = new LogEntry();
			ent.logTime = t.getTime();
			ent.logLine = line;
			this.entries.add(ent);
		}
		if (this.logger != null)
		{
			this.logger.logMessage(line, LogLevel.RAW);
		}
		synchronized(this)
		{
			this.notify();
		}
	}

	public void close()
	{
		if (this.threadRunning)
		{
			this.threadToStop = true;
			synchronized(this)
			{
				this.notify();
			}
			while (this.threadRunning)
			{
				MyThread.sleep(10);
			}
		}
	}

	private synchronized void saveEntries()
	{
		LocalDateTime t;
		Object arr[];
		while (true)
		{
			synchronized (this.entries)
			{
				if (this.entries.size() <= 0)
				{
					if (this.fs != null)
					{
						t = LocalDateTime.now();
						if (t.getYear() != this.lastEntryTime.getYear() || t.getMonth() != this.lastEntryTime.getMonth() || t.getDayOfMonth() != this.lastEntryTime.getDayOfMonth())
						{
							try
							{
								this.fs.close();
							}
							catch (Exception ex)
							{
	
							}
							this.fs = null;
							this.lastEntryTime = null;
						}
					}
					return;
				}
				int sz = this.entries.size();
				if (sz > 200)
				{
					List<LogEntry> sublist = this.entries.subList(0, 200);
					arr = sublist.toArray();
					this.entries.removeAll(sublist);
				}
				else
				{
					arr = this.entries.toArray();
					this.entries.clear();
				}
			}
			DateTimeFormatter monthFmt = DateTimeFormatter.ofPattern("yyyyMM");
			DateTimeFormatter dayFmt = DateTimeFormatter.ofPattern("yyyyMMdd");
			LogEntry ent;
			boolean hasSuccess = false;
			int i = 0;
			int j = arr.length;
			while (i < j)
			{
				ent = (LogEntry)arr[i];
				t = LocalDateTime.ofInstant(Instant.ofEpochMilli(ent.logTime), TimeZone.getDefault().toZoneId());
				if (this.fs == null || this.lastEntryTime == null || t.getYear() != this.lastEntryTime.getYear() || t.getMonth() != this.lastEntryTime.getMonth() || t.getDayOfMonth() != this.lastEntryTime.getDayOfMonth())
				{
					this.lastEntryTime = t;
					if (this.fs != null)
					{
						try
						{
							this.fs.close();
						}
						catch (Exception ex)
						{

						}
						this.fs = null;
					}

					String filePath = this.logPath + this.lastEntryTime.format(monthFmt);
					String fileName = filePath + File.separator + this.lastEntryTime.format(dayFmt)+".csv";
					new File(filePath).mkdirs();

					try
					{
						this.fs = new FileWriter(fileName, StandardCharsets.UTF_8, true);
					}
					catch (IOException ex)
					{
						this.fs = null;
						ex.printStackTrace();
					}
				}
				if (this.fs != null)
				{
					try
					{
						this.fs.write(ent.logLine+"\r\n");
						hasSuccess = true;
					}
					catch (IOException ex)
					{
						synchronized (this.entries)
						{
							this.entries.add(ent);
						}
						ex.printStackTrace();
					}
				}
				else
				{
					synchronized (this.entries)
					{
						this.entries.add(ent);
					}
				}
				i++;
			}
			try
			{
				if (this.fs != null)
				{
					fs.flush();
				}
			}
			catch (IOException ex)
			{
				ex.printStackTrace();
			}
			if (!hasSuccess)
			{
				return;
			}
		}
	}

	@Override
	public void run()
	{
		if (this.logger != null)
		{
			this.logger.logMessage("ActionFileStore.thread started", LogLevel.ACTION);
		}
		this.threadRunning = true;
		while (!this.threadToStop)
		{
			synchronized (this)
			{
				try
				{
					this.wait(10000);
				}
				catch (InterruptedException ex)
				{
	
				}
				this.saveEntries();
			}
		}

		if (this.fs != null)
		{
			try
			{
				this.fs.close();
			}
			catch (Exception ex)
			{

			}
			this.fs = null;
		}
		this.threadRunning = false;
		if (this.logger != null)
		{
			this.logger.logMessage("ActionFileStore.thread end", LogLevel.ACTION);
		}
	}


	@Override
	public void dbUpdated(@Nullable Object oldObj, @Nullable Object newObj) {
		if (oldObj == null)
		{
			if (newObj == null)
			{

			}
			else
			{
				this.logAction(2, getUser(), ActionFileStore.ActionType.CREATE, null, newObj.toString());
			}
		}
		else
		{
			if (newObj == null)
			{
				this.logAction(2, getUser(), ActionFileStore.ActionType.DELETE, oldObj.toString(), null);
			}
			else
			{
				this.logAction(2, getUser(), ActionFileStore.ActionType.UPDATE, oldObj.toString(), newObj.toString());
			}
		}
	}

	@Nullable
	public String getUser()
	{
		return (String)ThreadVar.get("User");
	}
}
