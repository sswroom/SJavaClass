package org.sswr.util.net;

import java.net.InetAddress;

public class MQTTClient implements Runnable
{
	public enum ConnError
	{
		SUCCESS,
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

	public MQTTClient(InetAddress brokerAddr, int port, TCPClientType cliType, int keepAliveS, String username, String password)
	{
		this.packetId = 1;
		this.keepAliveS = keepAliveS;
		this.conn = new MQTTConn(brokerAddr, port, cliType);
		if (this.conn.isError())
		{
			this.conn.close();
			this.connError = ConnError.CONNECT_ERROR;
			return;
		}
		String clientId = "sswrMQTT/" + System.currentTimeMillis();
		if (this.conn.sendConnect((byte)4, keepAliveS, clientId, username, password))
		{
			if (this.conn.waitConnAck(30000) == MQTTConnectStatus.ACCEPTED)
			{
				this.connError = ConnError.SUCCESS;
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
		this.conn.handlePublishMessage(hdlr);
	}

	private synchronized int nextPacketId()
	{
		return this.packetId++;
	}

	public boolean subscribe(String topic)
	{
		int packetId = this.nextPacketId();
		if (this.conn.sendSubscribe(packetId, topic))
		{
			if (this.conn.waitSubAck(packetId, 30000) <= 2)
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
}
