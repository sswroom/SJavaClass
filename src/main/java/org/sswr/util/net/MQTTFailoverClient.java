package org.sswr.util.net;

import java.util.ArrayList;
import java.util.List;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class MQTTFailoverClient implements MQTTClient
{
	class TopicInfo
	{
		public String topic;
		public MQTTPublishMessageHdlr hdlr;
	}

	class ChannelHandler implements MQTTEventHdlr
	{
		private MQTTStaticClient client;

		public ChannelHandler(@Nonnull MQTTStaticClient client)
		{
			this.client = client;
		}

		@Override
		public void onPublishMessage(@Nonnull String topic, @Nonnull byte[] buff, int buffOfst, int buffSize)
		{
			onChannelPublishMessage(client, topic, buff, buffOfst, buffSize);
		}

		@Override
		public void onDisconnect()
		{
			MQTTFailoverClient.this.onDisconnect();
		}
	}

	private FailoverHandler<MQTTStaticClient> foHdlr;
	private List<TopicInfo> topicList;
	private List<MQTTEventHdlr> hdlrList;

	public MQTTFailoverClient(@Nonnull FailoverType foType)
	{
		this.foHdlr = new FailoverHandler<MQTTStaticClient>(foType);
		this.topicList = new ArrayList<TopicInfo>();
		this.hdlrList = new ArrayList<MQTTEventHdlr>();
	}

	public void addClient(@Nonnull MQTTStaticClient client)
	{
		client.handleEvents(new ChannelHandler(client));
		this.foHdlr.addChannel(client);
	}

	public void subscribe(@Nonnull String topic, @Nullable MQTTPublishMessageHdlr hdlr)
	{
		TopicInfo info = new TopicInfo();
		info.topic = topic;
		info.hdlr = hdlr;
		synchronized(this)
		{
			this.topicList.add(info);
		}

		List<MQTTStaticClient> cliList = this.foHdlr.getAllChannels();
		int i = cliList.size();
		while (i-- > 0)
		{
			cliList.get(i).subscribe(topic, null);
		}
	}

	public boolean publish(@Nonnull String topic, @Nonnull String message)
	{
		MQTTStaticClient client = this.foHdlr.getCurrChannel();
		if (client == null)
		{
			return false;
		}
		if (client.publish(topic, message))
		{
			return true;
		}
		List<MQTTStaticClient> clients = this.foHdlr.getOtherChannels();
		int i = 0;
		int j = clients.size();
		while (i < j)
		{
			client = clients.get(i);
			if (client.publish(topic, message))
			{
				this.foHdlr.setCurrChannel(client);
				return true;
			}
			i++;
		}
		return false;
	}

	public void handleEvents(@Nonnull MQTTEventHdlr hdlr)
	{
		synchronized(this.hdlrList)
		{
			this.hdlrList.add(hdlr);
		}
	}

	public void onChannelPublishMessage(@Nonnull MQTTStaticClient client, @Nonnull String topic, @Nonnull byte[] buff, int buffOfst, int buffSize)
	{
		if (client != this.foHdlr.getCurrChannel())
		{
			return;
		}

		List<MQTTPublishMessageHdlr> hdlrList = new ArrayList<MQTTPublishMessageHdlr>();
		TopicInfo info;
		int i;
		synchronized (this)
		{
			i = this.topicList.size();
			while (i-- > 0)
			{
				info = this.topicList.get(i);
				if (info.hdlr != null && MQTTUtil.topicMatch(topic, info.topic))
				{
					hdlrList.add(info.hdlr);
				}
			}
		}

		if (hdlrList.size() > 0)
		{
			i = hdlrList.size();
			while (i-- > 0)
			{
				try
				{
					hdlrList.get(i).onPublishMessage(topic, buff, buffOfst, buffSize);
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
				}
			}
		}
		synchronized (this.hdlrList)
		{
			i = this.hdlrList.size();
			while (i-- > 0)
			{
				try
				{
					this.hdlrList.get(i).onPublishMessage(topic, buff, buffOfst, buffSize);
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
				}
			}
		}
	}

	public void onDisconnect()
	{
		synchronized (this.hdlrList)
		{
			int i = this.hdlrList.size();
			while (i-- > 0)
			{
				try
				{
					this.hdlrList.get(i).onDisconnect();
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
				}
			}
		}
	}

	public void close()
	{
		List<MQTTStaticClient> clients = this.foHdlr.getAllChannels();
		int i = clients.size();
		while (i-- > 0)
		{
			clients.get(i).close();
		}
	}
}
