package org.sswr.util.net;

import java.util.ArrayList;
import java.util.List;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public final class MQTTStaticClient implements Runnable, MQTTEventHdlr, MQTTClient, FailoverChannel
{
	public enum ConnError
	{
		CONNECTED,
		CONNECT_ERROR,
		SEND_CONNECT_ERROR,
		NOT_ACCEPT,
		DISCONNECT
	}

	class TopicInfo
	{
		public String topic;
		public MQTTPublishMessageHdlr hdlr;
	}

	private MQTTConn conn;
	private ConnError connError;
	private int packetId;
	private int keepAliveS;
	private Thread thread;
	private boolean threadToStop;
	private boolean autoReconnect;
	private List<MQTTEventHdlr> hdlrList;
	private String brokerHost;
	private int port;
	private SSLEngine ssl;
	private TCPClientType cliType;
	private String clientId;
	private String username;
	private String password;
	private List<TopicInfo> topicList;

	public MQTTStaticClient(@Nonnull String brokerHost, int port, @Nullable SSLEngine ssl, @Nonnull TCPClientType cliType, int keepAliveS, @Nullable String username, @Nullable String password, boolean autoReconnect)
	{
		this.packetId = 1;
		this.keepAliveS = keepAliveS;
		this.hdlrList = new ArrayList<MQTTEventHdlr>();
		this.topicList = new ArrayList<TopicInfo>();
		this.autoReconnect = autoReconnect;

		this.brokerHost = brokerHost;
		this.port = port;
		this.ssl = ssl;
		this.cliType = cliType;
		this.clientId = "sswrMQTT/" + System.currentTimeMillis();
		this.username = username;
		this.password = password;

		this.conn = null;
		this.threadToStop = false;
		this.thread = new Thread(this);
		this.connError = this.connect();
		this.thread.start();
	}

	@Nonnull
	private ConnError connect()
	{
		this.packetId = 1;
		MQTTConn conn = new MQTTConn(this.brokerHost, this.port, this.ssl, this.cliType);
		if (conn.isError())
		{
			conn.close();
			return ConnError.CONNECT_ERROR;
		}
		synchronized(this)
		{
			conn.handleEvents(this);
			if (conn.sendConnect((byte)4, keepAliveS, this.clientId, this.username, this.password))
			{
				if (conn.waitConnAck(30000) == MQTTConnectStatus.ACCEPTED)
				{
					this.conn = conn;
					return ConnError.CONNECTED;
				}
				else
				{
					conn.close();
					return ConnError.NOT_ACCEPT;
				}
			}
			else
			{
				conn.close();
				return ConnError.SEND_CONNECT_ERROR;
			}
		}
	}

	public void close()
	{
		this.autoReconnect = false;
		this.threadToStop = true;
		if (this.thread != null)
		{
			this.thread.interrupt();
			this.thread = null;
		}
		synchronized (this)
		{
			if (this.conn != null)
			{
				this.conn.close();
				this.conn = null;
			}
		}
		this.connError = ConnError.DISCONNECT;
	}

	public void handleEvents(@Nonnull MQTTEventHdlr hdlr)
	{
		synchronized(this.hdlrList)
		{
			this.hdlrList.add(hdlr);
		}
	}

	private synchronized int nextPacketId()
	{
		return this.packetId++;
	}

	public void subscribe(@Nonnull String topic, @Nullable MQTTPublishMessageHdlr hdlr)
	{
		TopicInfo info = new TopicInfo();
		info.topic = topic;
		info.hdlr = hdlr;
		synchronized(this)
		{
			this.topicList.add(info);
			if (this.conn != null)
			{
				int packetId = this.nextPacketId();
				this.conn.sendSubscribe(packetId, topic);
			}
		}
	}

	public boolean publish(@Nonnull String topic, @Nonnull String message)
	{
		synchronized (this)
		{
			if (this.conn == null)
			{
				return false;
			}
			return this.conn.sendPublish(topic, message);
		}
	}

	@Nonnull
	public synchronized ConnError getConnError()
	{
		return this.connError;
	}

	public void run()
	{
		while (!this.threadToStop)
		{
			try
			{
				synchronized(this)
				{
					this.wait(this.keepAliveS * 500);
				}

				synchronized(this)
				{
					if (this.conn == null)
					{
						if (this.autoReconnect)
						{
							this.connError = this.connect();
							if (this.conn != null && this.connError == ConnError.CONNECTED)
							{
								int i = this.topicList.size();
								while (i-- > 0)
								{
									this.conn.sendSubscribe(this.nextPacketId(), this.topicList.get(i).topic);
								}
							}
						}
					}
					else
					{
						this.conn.clearPackets();
						this.conn.sendPing();
					}
				}
			}
			catch (InterruptedException ex)
			{
				break;
			}
		}
	}

	@Override
	public void onPublishMessage(@Nonnull String topic, @Nonnull byte[] buff, int buffOfst, int buffSize)
	{
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

	@Override
	public void onDisconnect()
	{
		this.connError = ConnError.DISCONNECT;
		synchronized(this)
		{
			if (this.conn != null)
			{
				this.conn.close();
				this.conn = null;
			}
		}
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

	@Override
	public boolean channelFail()
	{
		return this.conn == null;
	}
}
