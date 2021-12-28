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
	public String name;
	public ProcessHandle process;
	}

	public static class ProcessStatus
	{
	public long pid;
	public String name;
	public long usedMemory;
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

	public static List<ProcessStatus> getProcessStatus(List<String> processNames)
	{
		List<ProcessStatus> ret = new ArrayList<ProcessStatus>();
		int i;
		Iterator<ProcessHandle> processes = ProcessHandle.allProcesses().iterator();
		ProcessHandle process;
		ProcessStatus status;
		while (processes.hasNext())
		{
			process = processes.next();
			Info info = process.info();
			if (OSInfo.getOSType() == OSType.WINDOWS)
			{
				String cmdLine = info.command().orElse(null);
				String []args = info.arguments().orElse(null);
				if (cmdLine != null || args != null)
				{
					int j;
					List<String> cmds;
					if (args == null)
					{
						cmds = List.of(cmdLine);
					}
					else
					{
						cmds = new ArrayList<String>();
						j = args.length;
						while (j-- > 0)
						{
							cmds.add(args[j]);
						}
						if (cmdLine != null)
						{
							cmds.add(cmdLine);
						}
					}
					j = cmds.size();
					while (j-- > 0)
					{
						cmdLine = cmds.get(j);
						i = processNames.size();
						while (i-- > 0)
						{
							if (cmdLine.indexOf(processNames.get(i)) >= 0)
							{
								status = new ProcessStatus();
								status.pid = process.pid();
								status.name = processNames.get(i);
								status.usedMemory = getProcessMemoryUsed(status.pid);
								ret.add(status);
								j = 0;
								break;
							}
						}
					}
				}
			}
			else
			{
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
							status.name = processNames.get(i);
							status.usedMemory = getProcessMemoryUsed(status.pid);
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
