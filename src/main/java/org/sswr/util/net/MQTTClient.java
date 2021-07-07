package org.sswr.util.net;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class MQTTClient implements Runnable, MQTTPublishMessageHdlr
{
	public enum ConnError
	{
		CONNECTED,
		CONNECT_ERROR,
		SEND_CONNECT_ERROR,
		NOT_ACCEPT,
		DISCONNECT
	}

	private MQTTConn conn;
	private ConnError connError;
	private int packetId;
	private int keepAliveS;
	private Thread thread;
	private List<MQTTPublishMessageHdlr> hdlrList;

	public MQTTClient(InetAddress brokerAddr, int port, TCPClientType cliType, int keepAliveS, String username, String password)
	{
		this.packetId = 1;
		this.keepAliveS = keepAliveS;
		this.hdlrList = new ArrayList<MQTTPublishMessageHdlr>();
		this.conn = new MQTTConn(brokerAddr, port, cliType);
		if (this.conn.isError())
		{
			this.conn.close();
			this.connError = ConnError.CONNECT_ERROR;
			return;
		}
		String clientId = "sswrMQTT/" + System.currentTimeMillis();
		this.conn.handlePublishMessage(this);
		if (this.conn.sendConnect((byte)4, keepAliveS, clientId, username, password))
		{
			if (this.conn.waitConnAck(30000) == MQTTConnectStatus.ACCEPTED)
			{
				this.connError = ConnError.CONNECTED;
				this.thread = new Thread(this);
				this.thread.start();
			}
			else
			{
				this.connError = ConnError.NOT_ACCEPT;
			}
		}
		else
		{
			this.connError = ConnError.SEND_CONNECT_ERROR;
		}
	}

	public void close()
	{
		this.conn.close();
		if (this.thread != null)
		{
			this.thread.interrupt();
			this.thread = null;
		}
		this.connError = ConnError.DISCONNECT;
	}

	public void handlePublishMessage(MQTTPublishMessageHdlr hdlr)
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

	public boolean subscribe(String topic, boolean waitReply)
	{
		int packetId = this.nextPacketId();
		if (this.conn.sendSubscribe(packetId, topic))
		{
			if (!waitReply || this.conn.waitSubAck(packetId, 30000) <= 2)
			{
				return true;
			}
		}
		return false;
	}

	public boolean publish(String topic, String message)
	{
		return this.conn.sendPublish(topic, message);
	}

	public ConnError getConnError()
	{
		return this.connError;
	}

	public void run()
	{
		while (true)
		{
			try
			{
				synchronized(this)
				{
					this.wait(this.keepAliveS * 500);
				}
				this.conn.sendPing();
			}
			catch (InterruptedException ex)
			{
				break;
			}
		}
	}

	@Override
	public void onPublishMessage(String topic, byte[] buff, int buffOfst, int buffSize)
	{
		synchronized (this.hdlrList)
		{
			int i = this.hdlrList.size();
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
}
