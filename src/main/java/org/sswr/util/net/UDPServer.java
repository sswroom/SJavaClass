package org.sswr.util.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.sswr.util.basic.ThreadEvent;
import org.sswr.util.data.ByteTool;
import org.sswr.util.data.DateTimeUtil;
import org.sswr.util.io.FileStream;
import org.sswr.util.io.LogLevel;
import org.sswr.util.io.LogTool;
import org.sswr.util.io.FileStream.BufferType;
import org.sswr.util.io.FileStream.FileMode;
import org.sswr.util.io.FileStream.FileShare;

public class UDPServer
{
	private MulticastSocket socV4;
	private UDPPacketListener hdlr;
	private String logPrefix;
	private int recvCnt;

	private UDPServerThread threadStats[];
	private int threadCnt;
	private int port;
	private ThreadEvent ctrlEvt;

	private LogTool msgLog;
	private String msgPrefix;

	private Object logFileMut;
	private FileStream logFileR;
	private FileStream logFileS;
	private ZonedDateTime logDateR;
	private ZonedDateTime logDateS;

	class UDPServerThread implements Runnable
	{
		private ThreadEvent evt;
		private boolean threadRunning;
		private boolean toStop;

		public UDPServerThread()
		{
			this.toStop = false;
			this.threadRunning = false;
			this.evt = new ThreadEvent(true);
		}

		public void run()
		{
			this.threadRunning = true;
			UDPServer.this.ctrlEvt.set();
		
			DateTimeFormatter dateTimeFmt = DateTimeFormatter.ofPattern("yyyyMMdd");
			byte []buff = new byte[2048];
			DatagramPacket packet = new DatagramPacket(buff, buff.length);
			while (!this.toStop)
			{
				try
				{
					UDPServer.this.socV4.receive(packet);
					ZonedDateTime logTime = ZonedDateTime.now();
					synchronized(UDPServer.this)
					{
						UDPServer.this.recvCnt++;
					}
					if (UDPServer.this.msgLog != null)
					{
						StringBuilder sb = new StringBuilder();
						if (UDPServer.this.msgPrefix != null)
						{
							sb.append(UDPServer.this.msgPrefix);
						}
						sb.append("Received ");
						sb.append(packet.getLength());
						sb.append(" bytes from ");
						sb.append(packet.getAddress().toString());
						sb.append(":");
						sb.append(packet.getPort());
						UDPServer.this.msgLog.logMessage(sb.toString(), LogLevel.RAW);
					}
		
					if (UDPServer.this.logPrefix != null)
					{
						synchronized(UDPServer.this.logFileMut)
						{
							if ((logTime.getDayOfMonth() != UDPServer.this.logDateR.getDayOfMonth()) || (UDPServer.this.logFileR == null))
							{
								if (UDPServer.this.logFileR != null)
								{
									UDPServer.this.logFileR.close();
									UDPServer.this.logFileR = null;
								}
								UDPServer.this.logFileR = new FileStream(UDPServer.this.logPrefix + logTime.format(dateTimeFmt) + "r.udp", FileMode.Append, FileShare.DenyWrite, BufferType.Normal);
							}
			
							if (UDPServer.this.logFileR != null)
							{
								int v = (int)((DateTimeUtil.getTimeMillis(logTime) / 1000) & 0xffffffffL);
								byte hbuff[] = new byte[8];
								hbuff[0] = (byte)0xaa;
								hbuff[1] = (byte)0xbb;
								ByteTool.writeInt16(hbuff, 2, packet.getLength());
								ByteTool.writeInt32(hbuff, 4, v);
								UDPServer.this.logFileR.write(hbuff, 0, 8);
								UDPServer.this.logFileR.write(packet.getData(), 0, packet.getLength());
							}
						}
					}
					UDPServer.this.hdlr.udpPacketReceived(packet.getAddress(), packet.getPort(), packet.getData(), packet.getOffset(), packet.getLength());
				}
				catch (IOException ex)
				{
					break;
				}
			}
			this.threadRunning = false;
			UDPServer.this.ctrlEvt.set();
		}

		public void setEvent()
		{
			this.evt.set();
		}

		public boolean isThreadRunning()
		{
			return this.threadRunning;
		}

		public void toStop()
		{
			this.toStop = true;
		}
	}

	public UDPServer(InetAddress bindAddr, int port, String logPrefix, UDPPacketListener hdlr, LogTool msgLog, String msgPrefix, int workerCnt, boolean reuseAddr)
	{
		this.threadCnt = workerCnt;
		this.recvCnt = 0;
		int i;
	
		this.logPrefix = logPrefix;
		this.hdlr = hdlr;
		this.logFileR = null;
		this.logFileS = null;
		this.logFileMut = new Object();
		this.msgLog = msgLog;
		if (this.msgLog == null)
		{
			this.msgLog = new LogTool();
		}
		this.msgPrefix = msgPrefix;
		this.port = port;
		boolean succ = false;
		try
		{
			this.socV4 = new MulticastSocket(null);
			if (reuseAddr)
			{
				this.socV4.setReuseAddress(true);
			}
			this.socV4.bind(new InetSocketAddress(port));
			succ = true;
		}
		catch (SocketException ex)
		{
			ex.printStackTrace();
			if (this.socV4 != null)
			{
				this.socV4.close();
				this.socV4 = null;
			}
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
			this.socV4 = null;
		}
		if (succ)
		{
			if (port == 0)
			{
				this.port = this.socV4.getLocalPort();
			}
			if (this.logPrefix != null)
			{
				this.logDateR = ZonedDateTime.now().minusDays(1);
				this.logDateS = ZonedDateTime.now().minusDays(1);
			}
			else
			{
				this.logDateR = null;
				this.logDateS = null;
			}
			this.ctrlEvt = new ThreadEvent(true);

			this.threadStats = new UDPServerThread[this.threadCnt];
			i = this.threadCnt;
			while (i-- > 0)
			{
				this.threadStats[i] = new UDPServerThread();
				new Thread(this.threadStats[i]).start();
			}
			boolean running;
			while (true)
			{
				running = true;
				i = this.threadCnt;
				while (i-- > 0)
				{
					if (!this.threadStats[i].isThreadRunning())
					{
						running = false;
						break;
					}
				}
				if (running)
					break;
				this.ctrlEvt.waitEvent(10);
			}
		}
		else
		{
			this.logDateR = null;
			this.logDateS = null;
		}
	}

	public void close()
	{
		int i;
		if (this.threadStats != null)
		{
			i = this.threadCnt;
			while (i-- > 0)
			{
				this.threadStats[i].toStop();
			}
		}
		if (this.socV4 != null)
		{
			this.socV4.close();
		}
		if (this.threadStats != null)
		{
			i = this.threadCnt;
			while (i-- > 0)
			{
				this.threadStats[i].setEvent();
			}
	
			boolean threadRunning = true;
			while (threadRunning)
			{
				threadRunning = false;
				i = this.threadCnt;
				while (i-- > 0)
				{
					if (this.threadStats[i].isThreadRunning())
					{
						threadRunning = true;
						break;
					}
				}
				if (!threadRunning)
					break;
				this.ctrlEvt.waitEvent(10);
			}
		}
		if (this.socV4 != null)
		{
			this.socV4.close();
			this.socV4 = null;
		}
	
		if (this.logFileS != null) this.logFileS.close();
		if (this.logFileR != null) this.logFileR.close();
	}

	public int getPort()
	{
		return this.port;
	}

	public boolean isError()
	{
		return this.socV4 == null;
	}

	public boolean sendTo(InetAddress addr, int port, byte []buff, int ofst, int dataSize)
	{
		boolean succ;
		if (this.logPrefix != null)
		{
			ZonedDateTime logTime = ZonedDateTime.now();
	
			synchronized(this.logFileMut)
			{
				if ((logTime.getDayOfMonth() != this.logDateS.getDayOfMonth()) || (logFileS == null))
				{
					if (logFileS != null)
					{
						this.logFileS.close();
					}
					this.logFileS = new FileStream(this.logPrefix + logTime.format(DateTimeFormatter.ofPattern("yyyyMMdd"))+"s.udp", FileMode.Append, FileShare.DenyWrite, BufferType.Normal);
				}
		
				if (this.logFileS != null)
				{
					int v = (int)(DateTimeUtil.getTimeMillis(logTime) & 0xffffffffL);
					byte hbuff[] = new byte[8];
					hbuff[0] = (byte)0xaa;
					hbuff[1] = (byte)0xbb;
					ByteTool.writeInt16(hbuff, 2, dataSize);
					ByteTool.writeInt32(hbuff, 4, v);
					this.logFileS.write(hbuff, 0, 8);
					this.logFileS.write(buff, ofst, dataSize);
				}
			}
		}
	
		if (this.msgLog != null)
		{
			StringBuilder sb = new StringBuilder();
			if (msgPrefix != null)
			{
				sb.append(msgPrefix);
			}
			sb.append("Sending UDP ");
			sb.append(dataSize);
			sb.append(" bytes to ");
			sb.append(addr.toString());
			sb.append(":");
			sb.append(port);
			this.msgLog.logMessage(sb.toString(), LogLevel.RAW);
		}
	
		succ = false;
		try
		{
			this.socV4.send(new DatagramPacket(buff, ofst, dataSize, addr, port));
			succ = true;
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
			if (this.msgLog != null)
			{
				this.msgLog.logMessage(msgPrefix + "Send UDP data failed", LogLevel.ERROR);
			}
		}
		return succ;
	}

	public int getRecvCnt()
	{
		return this.recvCnt;
	}

	public void addMulticastIP(InetAddress ip)
	{
		try
		{
			InetSocketAddress addr = new InetSocketAddress(ip, this.port);
			this.socV4.joinGroup(addr, null);
		}
		catch (IOException ex)
		{
			this.msgLog.logException(ex);
		}
	}

	public void setBuffSize(int buffSize)
	{
		try
		{
			this.socV4.setReceiveBufferSize(buffSize);
		}
		catch (SocketException ex)
		{
			this.msgLog.logException(ex);
		}
	}

	public void setBroadcast(boolean val)
	{
		try
		{
			this.socV4.setBroadcast(val);
		}
		catch (SocketException ex)
		{
			this.msgLog.logException(ex);
		}
	}
}
