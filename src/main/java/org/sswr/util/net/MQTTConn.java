package org.sswr.util.net;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.sswr.util.basic.MyThread;
import org.sswr.util.basic.ThreadEvent;
import org.sswr.util.data.ByteTool;
import org.sswr.util.io.IOStream;
import org.sswr.util.io.ProtocolDataListener;
import org.sswr.util.io.protohdlr.ProtoMQTTHandler;

public class MQTTConn implements Runnable, ProtocolDataListener
{
	private ProtoMQTTHandler protoHdlr;
	private TCPClient cli;
	private Object cliData;
	private boolean recvRunning;
	private boolean recvStarted;

	private List<MQTTEventHdlr> hdlrList;

	private List<MQTTPacketInfo> packetList;
	private ThreadEvent packetEvt;

	public void dataParsed(IOStream stm, Object stmObj, int cmdType, int seqId, byte[] cmd, int cmdOfst, int cmdSize)
	{
		if ((cmdType & 0xf0) == 0x30 && cmdSize >= 2)
		{
			byte qosLev = (byte)((cmdType & 6) >> 1);
			int i;
			int packetId = ByteTool.readMUInt16(cmd, cmdOfst + 0);
			String topic = null;
			if ((packetId + 2) <= cmdSize)
			{
				topic = new String(cmd, cmdOfst + 2, packetId, StandardCharsets.UTF_8);
			}
			i = packetId + 2;
			if (qosLev == 1 || qosLev == 2)
			{
				if (i + 2 <= cmdSize)
				{
					packetId = ByteTool.readMUInt16(cmd, cmdOfst + i);
				}
				else
				{
					packetId = 0;
				}
				i += 2;
			}
			if (topic != null && i <= cmdSize)
			{
				this.onPublishMessage(topic, cmd, cmdOfst + i, cmdSize - i);
			}
			if (qosLev == 1)
			{
				this.sendPubAck(packetId);
			}
			else if (qosLev == 2)
			{
				this.sendPubRec(packetId);
			}
			topic = null;
		}
		else
		{
			MQTTPacketInfo packet;
			byte[] buff = new byte[cmdSize];
			packet = new MQTTPacketInfo();;
			packet.setPacketType((byte)cmdType);
			ByteTool.copyArray(buff, 0, cmd, cmdOfst, cmdSize);
			packet.setContent(buff);
			synchronized (this.packetList)
			{
				this.packetList.add(packet);
			}
			this.packetEvt.set();
		}
	}

	public void dataSkipped(IOStream stm, Object stmObj, byte[] buff, int buffOfst, int buffSize)
	{

	}
	
	public void run()
	{
		byte buff[] = new byte[2048];
		int buffSize;
		int readSize;
		this.recvStarted = true;
		this.recvRunning = true;
		buffSize = 0;
		while (true)
		{
			readSize = this.cli.read(buff, buffSize, 2048 - buffSize);
			if (readSize <= 0)
			{
				break;
			}
			buffSize += readSize;
			readSize = this.protoHdlr.parseProtocol(this.cli, this, this.cliData, buff, 0, buffSize);
			if (readSize == 0)
			{
				buffSize = 0;
			}
			else if (readSize < buffSize)
			{
				ByteTool.copyArray(buff, 0, buff, buffSize - readSize, readSize);
				buffSize = readSize;
			}
		}
		this.recvRunning = false;
		this.packetEvt.set();
		this.onDisconnect();
	}

	private void onPublishMessage(String topic, byte[] message, int ofst, int msgSize)
	{
		int i = this.hdlrList.size();
		while (i-- > 0)
		{
			this.hdlrList.get(i).onPublishMessage(topic, message, ofst, msgSize);
		}
	}

	private void onDisconnect()
	{
		int i = this.hdlrList.size();
		while (i-- > 0)
		{
			this.hdlrList.get(i).onDisconnect();
		}
	}

	private MQTTPacketInfo getNextPacket(byte packetType, int timeoutMS)
	{
		long initT = System.currentTimeMillis();
		MQTTPacketInfo packet;
		long t;
		while (true)
		{
			while (this.packetList.size() > 0)
			{
				synchronized(this.packetList)
				{
					packet = this.packetList.remove(0);
				}
				if ((packet.getPacketType() & 0xf0) == packetType)
				{
					return packet;
				}
			}
			t = System.currentTimeMillis() - initT;
			if (!this.recvRunning || t >= timeoutMS)
				return null;
			this.packetEvt.waitEvent(timeoutMS - (int)t);
		}	
	}

	public MQTTConn(String host, int port, TCPClientType cliType)
	{
		this.recvRunning = false;
		this.recvStarted = false;
		this.hdlrList = new ArrayList<MQTTEventHdlr>();
	
		this.packetList = new ArrayList<MQTTPacketInfo>();
		this.packetEvt = new ThreadEvent(true);
		this.protoHdlr = new ProtoMQTTHandler(this);
		this.cli = new TCPClient(host, port, cliType);
		if (this.cli.isConnectError())
		{
			this.cli.close();
			this.cli = null;
		}
		else
		{
			this.cli.setNoDelay(true);
			this.cliData = this.protoHdlr.createStreamData(this.cli);
			new Thread(this).start();
			while (!this.recvStarted)
			{
				MyThread.sleep(1);
			}
		}
	}

	public void close()
	{
		if (this.cli != null)
		{
			if (this.recvRunning)
			{
				this.cli.close();
			}
			while (this.recvRunning)
			{
				MyThread.sleep(1);
			}
			this.protoHdlr.deleteStreamData(this.cli, this.cliData);
			this.cli.close();
			this.cli = null;
		}
	}

	public void handleEvents(MQTTEventHdlr hdlr)
	{
		this.hdlrList.add(hdlr);
	}

	public boolean isError()
	{
		return this.cli == null || !this.recvRunning;
	}

	public boolean sendConnect(byte protoVer, int keepAliveS, String clientId, String userName, String password)
	{
		byte[] packet1 = new byte[512];
		byte[] packet2 = new byte[512];
		byte[] sbuff;
	
		int i;
		int j;
		packet1[0] = 0;
		packet1[1] = 4;
		packet1[2] = 'M';
		packet1[3] = 'Q';
		packet1[4] = 'T';
		packet1[5] = 'T';
		packet1[6] = protoVer;
		packet1[7] = 2; //Flags;
		ByteTool.writeMInt16(packet1, 8, keepAliveS);
		sbuff = clientId.getBytes(StandardCharsets.UTF_8);
		i = 10;
		j = sbuff.length;
		ByteTool.writeMInt16(packet1, i, j);
		ByteTool.copyArray(packet1, i + 2, sbuff, 0, j);
		i += j + 2;
		if (userName != null)
		{
			packet1[7] |= 0x80;
			sbuff = userName.getBytes(StandardCharsets.UTF_8);
			j = sbuff.length;
			ByteTool.writeMInt16(packet1, i, j);
			ByteTool.copyArray(packet1, i + 2, sbuff, 0, j);
			i += j + 2;
			}
		if (password != null)
		{
			packet1[7] |= 0x40;
			sbuff = password.getBytes(StandardCharsets.UTF_8);
			j = sbuff.length;
			ByteTool.writeMInt16(packet1, i, j);
			ByteTool.copyArray(packet1, i + 2, sbuff, 0, j);
			i += j + 2;
		}
		j = this.protoHdlr.buildPacket(packet2, 0, 0x10, 0, packet1, 0, i, this.cliData);
		return this.cli.write(packet2, 0, j) == j;
	}

	public boolean sendPublish(String topic, String message)
	{
		byte[] packet1 = new byte[512];
		byte[] packet2 = new byte[512];
		byte[] sbuff;
	
		int i;
		int j;
		i = 0;
	
		sbuff = topic.getBytes(StandardCharsets.UTF_8);
		j = sbuff.length;
		ByteTool.writeMInt16(packet1, i, j);
		ByteTool.copyArray(packet1, i + 2, sbuff, 0, j);
		i += j + 2;
		sbuff = message.getBytes(StandardCharsets.UTF_8);
		j = sbuff.length;
		ByteTool.copyArray(packet1, i, sbuff, 0, j);
		i += j;
	
		j = this.protoHdlr.buildPacket(packet2, 0, 0x30, 0, packet1, 0, i, this.cliData);
		return this.cli.write(packet2, 0, j) == j;
	}

	public boolean sendPubAck(int packetId)
	{
		byte[] packet1 = new byte[16];
		byte[] packet2 = new byte[16];
		int j;
	
		ByteTool.writeMInt16(packet1, 0, packetId);
		j = this.protoHdlr.buildPacket(packet2, 0, 0x40, 0, packet1, 0, 2, this.cliData);
		return this.cli.write(packet2, 0, j) == j;
	}

	public boolean sendPubRec(int packetId)
	{
		byte[] packet1 = new byte[16];
		byte[] packet2 = new byte[16];
		int j;
	
		ByteTool.writeMInt16(packet1, 0, packetId);
		j = this.protoHdlr.buildPacket(packet2, 0, 0x50, 0, packet1, 0, 2, this.cliData);
		return this.cli.write(packet2, 0, j) == j;
	}

	public boolean sendSubscribe(int packetId, String topic)
	{
		byte[] packet1 = new byte[512];
		byte[] packet2 = new byte[512];
		byte[] sbuff;
	
		int i;
		int j;
	
		ByteTool.writeMInt16(packet1, 0, packetId);
		i = 2;
		sbuff = topic.getBytes(StandardCharsets.UTF_8);
		j = sbuff.length;
		ByteTool.writeMInt16(packet1, i, j);
		ByteTool.copyArray(packet1, i + 2, sbuff, 0, j);
		i += j + 2;
		packet1[i] = 0;
		i++;
	
		j = this.protoHdlr.buildPacket(packet2, 0, 0x82, 0, packet1, 0, i, this.cliData);
		return this.cli.write(packet2, 0, j) == j;
	}

	public boolean sendPing()
	{
		byte packet2[] = new byte[16];
		int j;
		j = this.protoHdlr.buildPacket(packet2, 0, 0xc0, 0, packet2, 0, 0, this.cliData);
		return this.cli.write(packet2, 0, j) == j;
	}

	public boolean sendDisconnect()
	{
		byte packet2[] = new byte[16];
		int j;
		j = this.protoHdlr.buildPacket(packet2, 0, 0xe0, 0, packet2, 0, 0, this.cliData);
		return this.cli.write(packet2, 0, j) == j;
	}

	public MQTTConnectStatus waitConnAck(int timeoutMS)
	{
		MQTTPacketInfo packet = this.getNextPacket((byte)0x20, timeoutMS);
		if (packet == null)
			return MQTTConnectStatus.TIMEDOUT;
	
		MQTTConnectStatus ret = MQTTConnectStatus.fromByte(packet.getContent()[1]);
		return ret;
	}

	public byte waitSubAck(int packetId, int timeoutMS)
	{
		MQTTPacketInfo packet = this.getNextPacket((byte)0x90, timeoutMS);
		if (packet == null)
			return (byte)0x80;
	
		byte ret;
		if (packet.getContent().length < 3 || packetId != ByteTool.readMUInt16(packet.getContent(), 0))
		{
			ret = (byte)0x80;
		}
		else
		{
			ret = packet.getContent()[2];
		}
		return ret;
	}

	public void clearPackets()
	{
		synchronized(this.packetList)
		{
			this.packetList.clear();
		}	
	}

	public static boolean publishMessage(String host, int port, TCPClientType cliType, String username, String password, String topic, String message)
	{
		MQTTConn cli = new MQTTConn(host, port, cliType);
		if (cli.isError())
		{
			cli.close();
			return false;
		}
	
		boolean succ = false;
		String clientId = "sswrMQTT/" + System.currentTimeMillis();
		if (cli.sendConnect((byte)4, 30, clientId, username, password))
		{
			succ = (cli.waitConnAck(30000) == MQTTConnectStatus.ACCEPTED);
		}
		if (succ)
		{
			succ = cli.sendPublish(topic, message);
			cli.sendDisconnect();
		}
		cli.close();
		return succ;
	}

	public static int getDefaultPort()
	{
		return 1883;
	}
}
