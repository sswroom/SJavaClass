package org.sswr.util.net;

public class TCPClientStatus
{
	public static final int TCP_BUFF_SIZE = 16384;
	public TCPClient cli;
	public Object cliData;
	public long lastDataTimeTicks;
	public boolean reading;
	public boolean processing;
	public boolean timeAlerted;
	public long timeStart;
	public boolean recvDataExist;
	public byte buff[] = new byte[TCP_BUFF_SIZE];
	public int buffSize;
}
