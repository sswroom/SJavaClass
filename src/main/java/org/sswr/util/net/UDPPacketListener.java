package org.sswr.util.net;

import java.net.InetAddress;

import jakarta.annotation.Nonnull;

public interface UDPPacketListener
{
	public void udpPacketReceived(@Nonnull InetAddress addr, int port, @Nonnull byte[] buff, int ofst, int length);
}
