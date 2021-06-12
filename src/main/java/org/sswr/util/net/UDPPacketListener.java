package org.sswr.util.net;

import java.net.InetAddress;

public interface UDPPacketListener
{
	public void udpPacketReceived(InetAddress addr, int port, byte[] buff, int ofst, int length);
}
