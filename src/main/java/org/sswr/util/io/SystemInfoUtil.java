package org.sswr.util.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ProcessHandle.Info;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SystemInfoUtil
{
	public static class FreeSpaceEntry
	{
	public String path;
	public long freeSize;
	}

	public static class MemoryStatus
	{
	public long javaFreeMemory;
	public long javaTotalMemory;
	public long javaMaxMemory;
	public long totalPhysicalMemory;
	public long usedPhysicalMemory;
	public long freePhysicalMemory;
	public long totalSwapMemory;
	public long usedSwapMemory;
	public long freeSwapMemory;
	}

	public static class ProcessState
	{
	public long pid;
	public long ppid;
	public String name;
	public String cmdLine;
	public ProcessHandle process;
	}

	public static class ProcessStatus
	{
	public long pid;
	public long ppid;
	public String name;
	public String cmdLine;
	public long usedMemory;
	public ZonedDateTime startTime;
	public long threadCnt;
	public long handleCnt;
	}

/*	private List<String> processNames;
	private Map<Long, ProcessState> processStatus;

	public SystemInfoUtil(List<String> processNames)
	{
		this.processNames = new ArrayList<String>();
		this.processNames.addAll(processNames);
		this.processStatus = new HashMap<Long, ProcessState>();
		this.reload();
	}

	public void reload()
	{
		ProcessState state;
		Iterator<ProcessState> itStatus = this.processStatus.values().iterator();
		List<ProcessState> removeList = new ArrayList<ProcessState>();
		while (itStatus.hasNext())
		{
			state = itStatus.next();
			if (!state.process.isAlive())
			{
				removeList.add(state);
			}
		}
		int i = removeList.size();
		while (i-- > 0)
		{
			this.processStatus.remove(removeList.get(i).pid);
		}

		Iterator<ProcessHandle> processes = ProcessHandle.allProcesses().iterator();
		ProcessHandle process;
		while (processes.hasNext())
		{
			process = processes.next();
			if (!this.processStatus.containsKey(process.pid()))
			{
				Info info = process.info();
				String cmdLine = info.commandLine().orElse(null);
				if (cmdLine != null)
				{
					i = this.processNames.size();
					while (i-- > 0)
					{
						if (cmdLine.indexOf(this.processNames.get(i)) >= 0)
						{
							System.out.println("pid "+process.pid()+", "+cmdLine);
							state = new ProcessState();
							state.pid = process.pid();
							state.name = this.processNames.get(i);
							state.process = process;
							this.processStatus.put(state.pid, state);
							break;
						}
					}
				}
			}
		}
	}

	public List<ProcessStatus> getProcessStatus()
	{
		List<ProcessStatus> ret = new ArrayList<ProcessStatus>();
		ProcessState state;
		ProcessStatus status;
		Iterator<ProcessState> itState = this.processStatus.values().iterator();
		while (itState.hasNext())
		{
			state = itState.next();
			status = new ProcessStatus();
			status.pid = state.pid;
			status.name = state.name;
			status.usedMemory = getProcessMemoryUsed(state.pid);
			ret.add(status);
		}
		return ret;
	}*/

	private static ZonedDateTime parseWindowDate(String s)
	{
		if (s.length() > 22)
		{
			try
			{
				if (s.charAt(21) == '+')
				{
					return ZonedDateTime.of(Integer.parseInt(s.substring(0, 4)),
						Integer.parseInt(s.substring(4, 6)),
						Integer.parseInt(s.substring(6, 8)),
						Integer.parseInt(s.substring(8, 10)),
						Integer.parseInt(s.substring(10, 12)),
						Integer.parseInt(s.substring(12, 14)),
						Integer.parseInt(s.substring(15, 21)) * 1000,
						ZoneOffset.ofTotalSeconds(Integer.parseInt(s.substring(22)) * 60));
				}
				else if (s.charAt(21) == '-')
				{
					return ZonedDateTime.of(Integer.parseInt(s.substring(0, 4)),
						Integer.parseInt(s.substring(4, 6)),
						Integer.parseInt(s.substring(6, 8)),
						Integer.parseInt(s.substring(8, 10)),
						Integer.parseInt(s.substring(10, 12)),
						Integer.parseInt(s.substring(12, 14)),
						Integer.parseInt(s.substring(15, 21)) * 1000,
						ZoneOffset.ofTotalSeconds(Integer.parseInt(s.substring(22)) * -60));
				}
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
			return null;
		}
		else
		{
			return null;
		}
	}

	public static List<ProcessStatus> getProcessStatus(List<String> processNames)
	{
		List<ProcessStatus> ret = new ArrayList<ProcessStatus>();
		if (OSInfo.getOSType() == OSType.WINDOWS)
		{
			ProcessBuilder pb = new ProcessBuilder("wmic", "process", "get", "CommandLine,CreationDate,HandleCount,ParentProcessId,ProcessId,ThreadCount,WorkingSetSize", "/format:csv");
			try
			{
				Process proc = pb.start();
				BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
				String s;
				String[] sarr;
				int i;
				ProcessStatus status;
				while ((s = reader.readLine()) != null && s.length() == 0)
				{
				}
				while ((s = reader.readLine()) != null)
				{
					if (s.length() > 0)
					{
						sarr = s.split(",");
						if (sarr.length == 8 && sarr[1].length() > 0)
						{
							i = processNames.size();
							while (i-- > 0)
							{
								if (sarr[1].indexOf(processNames.get(i)) >= 0)
								{
									status = new ProcessStatus();
									status.pid = Long.parseLong(sarr[5]);
									status.ppid = Long.parseLong(sarr[4]);
									status.name = processNames.get(i);
									status.cmdLine = sarr[1];
									status.usedMemory = Long.parseLong(sarr[7]);
									status.handleCnt = Long.parseLong(sarr[3]);
									status.threadCnt = Long.parseLong(sarr[6]);
									status.startTime = parseWindowDate(sarr[2]);
									ret.add(status);
									break;
								}
							}
		
						}
					}
				}
				reader.close();
				proc.waitFor();
			}
			catch (IOException ex)
			{

			}
			catch (InterruptedException ex)
			{

			}
			return ret;
		}
		else
		{
			int i;
			Iterator<ProcessHandle> processes = ProcessHandle.allProcesses().iterator();
			ProcessHandle process;
			ProcessStatus status;
			while (processes.hasNext())
			{
				process = processes.next();
				Info info = process.info();
				String cmdLine = info.commandLine().orElse(null);
				if (cmdLine != null)
				{
					i = processNames.size();
					while (i-- > 0)
					{
						if (cmdLine.indexOf(processNames.get(i)) >= 0)
						{
							status = new ProcessStatus();
							status.pid = process.pid();
							ProcessHandle parProc = process.parent().orElse(null);
							status.ppid = 0;
							if (parProc != null)
								status.ppid = parProc.pid();
							status.name = processNames.get(i);
							status.cmdLine = cmdLine;
							status.usedMemory = getProcessMemoryUsed(status.pid);
							status.handleCnt = 0;
							status.threadCnt = 0;
							status.startTime = null;
							ret.add(status);
							break;
						}
					}
				}	
			}
		}
		return ret;
	}

	public static List<FreeSpaceEntry> getFreeSpaces()
	{
		List<FreeSpaceEntry> ret = new ArrayList<FreeSpaceEntry>();
		FreeSpaceEntry ent;
		Iterator<Path> paths = FileSystems.getDefault().getRootDirectories().iterator();
		Path path;
		while (paths.hasNext())
		{
			path = paths.next();
			ent = new FreeSpaceEntry();
			ent.path = path.toString();
			try
			{
				ent.freeSize = Files.getFileStore(path).getUsableSpace();
			}
			catch (IOException ex)
			{
				ent.freeSize = -1;
			}
			ret.add(ent);
		}
		return ret;
	}

	private static long toByte(String s)
	{
		try
		{
			if (s.endsWith(" kB"))
			{
				return Long.parseLong(s.substring(0, s.length() - 3)) * 1024;
			}
			else
			{
				return Long.parseLong(s);
			}
		}
		catch (Exception ex)
		{
			return 0;
		}
	}

	public static long executeWmic(String cmdLine)
	{
		long ret = 0;
		try
		{
			ProcessBuilder pb = new ProcessBuilder(cmdLine.split(" "));
			Process proc = pb.start();
			BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			String s;
			while ((s = reader.readLine()) != null)
			{
				if (s.length() > 0)
					break;
			}
			String value;
			while ((value = reader.readLine()) != null)
			{
				if (value.length() > 0)
					break;
			}
			if (value != null)
			{
				ret = Long.parseLong(value.trim());	
			}
			reader.close();
			proc.waitFor();
		}
		catch (IOException ex)
		{

		}
		catch (InterruptedException ex)
		{

		}
		return ret;
	}

	public static MemoryStatus getMemoryStatus()
	{
		MemoryStatus status = new MemoryStatus();
		status.javaFreeMemory = Runtime.getRuntime().freeMemory();
		status.javaTotalMemory = Runtime.getRuntime().totalMemory();
		status.javaMaxMemory = Runtime.getRuntime().maxMemory();
		status.freePhysicalMemory = 0;
		status.usedPhysicalMemory = 0;
		status.totalPhysicalMemory = 0;
		status.totalSwapMemory = 0;
		status.usedSwapMemory = 0;
		status.freeSwapMemory = 0;
		switch (OSInfo.getOSType())
		{
		case LINUX:
			try
			{
				BufferedReader reader = new BufferedReader(new FileReader(new File("/proc/meminfo")));
				String s;
				while ((s = reader.readLine()) != null)
				{
					if (s.startsWith("MemTotal:"))
					{
						status.totalPhysicalMemory = toByte(s.substring(9).trim());
					}
					else if (s.startsWith("MemAvailable:"))
					{
						status.freePhysicalMemory = toByte(s.substring(13).trim());
					}
					else if (s.startsWith("SwapTotal:"))
					{
						status.totalSwapMemory = toByte(s.substring(10).trim());
					}
					else if (s.startsWith("SwapFree:"))
					{
						status.freeSwapMemory = toByte(s.substring(9).trim());
					}
				}
				reader.close();
				status.usedPhysicalMemory = status.totalPhysicalMemory - status.freePhysicalMemory;
				status.usedSwapMemory = status.totalSwapMemory - status.freeSwapMemory;
			}
			catch (IOException ex)
			{
			}
			break;
		case WINDOWS:
			status.totalPhysicalMemory = executeWmic("wmic ComputerSystem get TotalPhysicalMemory");
			status.freePhysicalMemory = executeWmic("wmic OS get FreePhysicalMemory") * 1024;
			status.totalSwapMemory = executeWmic("wmic OS get TotalVirtualMemorySize") * 1024;
			status.freeSwapMemory = executeWmic("wmic OS get FreeVirtualMemory") * 1024;
			status.usedPhysicalMemory = status.totalPhysicalMemory - status.freePhysicalMemory;
			status.usedSwapMemory = status.totalSwapMemory - status.freeSwapMemory;
			break;
		default:
			break;
		}
		return status;
	}

	public static long getProcessMemoryUsed(long pid)
	{
		switch (OSInfo.getOSType())
		{
		case LINUX:
			try
			{
				long ret = 0;
				BufferedReader reader = new BufferedReader(new FileReader(new File("/proc/"+pid+"/status")));
				String s;
				while ((s = reader.readLine()) != null)
				{
					if (s.startsWith("VmRSS:"))
					{
						ret = toByte(s.substring(6).trim());
					}
				}
				reader.close();
				return ret;
			}
			catch (IOException ex)
			{
				return 0;
			}
		case WINDOWS:
			try
			{
				long ret = 0;
				ProcessBuilder pb = new ProcessBuilder("tasklist", "/FI", "PID eq "+pid, "/FO", "LIST");
				Process proc = pb.start();
				BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
				String s;
				while ((s = reader.readLine()) != null)
				{
					if (s.startsWith("Mem Usage:"))
					{
						s = s.substring(10).trim();
						s = s.replace(",", "");
						if (s.endsWith(" K"))
						{
							ret = Long.parseLong(s.substring(0, s.length() - 2)) * 1024;
						}
					}
				}
				reader.close();
				proc.waitFor();
				return ret;
			}
			catch (IOException ex)
			{
				return 0;
			}
			catch (InterruptedException ex)
			{
				return 0;
			}
		default:
			return 0;
		}
	}
}
