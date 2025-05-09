package org.sswr.util.net;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.sswr.util.basic.MyThread;
import org.sswr.util.data.StringUtil;
import org.sswr.util.io.LogLevel;
import org.sswr.util.io.LogTool;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class TaskMQTTManager implements MQTTEventHdlr
{
	private Map<String, Long> taskMap;
	private String controlTopic;
	private MQTTClient client;
	private int machineId;

	private Object taskMut;
	private String checkTaskId;
	private long checkTaskTime;
	private boolean taskExists;
	private LogTool log;

	public static enum TaskStatus
	{
		TaskBegin,
		TaskAlreadyRunnng,
		TaskStartedByOther,
		TaskPublishFailed
	}

	public TaskMQTTManager(@Nonnull MQTTClient client, @Nonnull String controlTopic, int machineId)
	{
		this.taskMap = new HashMap<String, Long>();
		this.client = client;
		this.controlTopic = controlTopic;
		this.machineId = machineId;
		this.log = null;
		this.taskMut = new Object();
		this.client.handleEvents(this);
	}

	public void setLog(@Nullable LogTool log)
	{
		this.log = log;
	}

	@Override
	public void onPublishMessage(@Nonnull String topic, @Nonnull byte[] buff, int buffOfst, int buffSize) {
		if (topic.equals(this.controlTopic))
		{
			String content = new String(buff, buffOfst, buffSize, StandardCharsets.UTF_8);
			String[] contents = StringUtil.split(content, "|");
			Integer machine = StringUtil.toInteger(contents[0]);
			if (this.log != null) this.log.logMessage("Received "+contents.length+" cols: "+content, LogLevel.COMMAND);
			if (machine == null || machine == this.machineId)
				return;
			if (contents.length <= 1)
				return;
			if (contents[1].equals("0") && contents.length == 4)
			{
				if (this.log != null) this.log.logMessage("Received BeginTask: "+contents[2]+", "+contents[3], LogLevel.COMMAND);
				long time;
				try
				{
					time = Long.parseLong(contents[2]);
				}
				catch (Exception ex)
				{
					if (this.log != null) this.log.logException(ex);
					return;
				}
				synchronized(this.taskMap)
				{
					if (this.log != null) this.log.logMessage("Checking \""+contents[2]+"\"", LogLevel.COMMAND);
					if (taskMap.get(contents[3]) != null)
					{
						sendTaskRunning(contents[3]);
						return;
					}
				}
				synchronized(this.taskMut)
				{
					if (this.checkTaskId != null && contents[3].equals(this.checkTaskId))
					{
						if (this.checkTaskTime < time)
						{
							sendTaskRunning(contents[3]);
							return;
						}
						else if (this.checkTaskTime == time && this.machineId < machine)
						{
							sendTaskRunning(contents[3]);
							return;
						}
					}
				}
			}
			else if (contents[1].equals("1") && contents.length == 3)
			{
				synchronized(this.taskMut)
				{
					if (this.checkTaskId != null && contents[2].equals(this.checkTaskId))
					{
						this.taskExists = true;
					}
				}
			}
		}
	}

	@Override
	public void onDisconnect() {
	}

	private boolean sendBeginTask(@Nonnull String taskId, long time)
	{
		return this.client.publish(this.controlTopic, this.machineId+"|0|"+time+"|"+taskId);
	}

	private boolean sendTaskRunning(@Nonnull String taskId)
	{
		return this.client.publish(this.controlTopic, this.machineId+"|1|"+taskId);
	}

	@Nonnull
	public synchronized TaskStatus beginTask(@Nonnull String taskId)
	{
		synchronized(this.taskMap)
		{
			if (this.taskMap.get(taskId) != null)
				return TaskStatus.TaskAlreadyRunnng;
		}
		long time = System.currentTimeMillis();
		synchronized(this.taskMut)
		{
			this.taskExists = false;
			this.checkTaskId = taskId;
			this.checkTaskTime = time;
		}
		if (!sendBeginTask(taskId, time))
		{
			synchronized(this.taskMut)
			{
				this.checkTaskId = null;
			}
			return TaskStatus.TaskPublishFailed;
		}
		MyThread.sleep(1000);
		synchronized(this.taskMut)
		{
			this.checkTaskId = null;
		}
		if (this.taskExists)
		{
			return TaskStatus.TaskStartedByOther;
		}

		synchronized(this.taskMap)
		{
			if (this.log != null) this.log.logMessage("Begin task: \""+taskId+"\", "+time, LogLevel.COMMAND);
			this.taskMap.put(taskId, time);
		}
		return TaskStatus.TaskBegin;
	}

	public void endTask(@Nonnull String taskId)
	{
		synchronized(this.taskMap)
		{
			if (this.log != null) this.log.logMessage("End task: \""+taskId+"\"", LogLevel.COMMAND);
			this.taskMap.remove(taskId);
		}
	}
}
