package org.sswr.util.net;

import java.net.InetAddress;

public class WindowsSocketFactory implements SocketFactory
{
	@Override
	public InetAddress[] getDefDNS()
	{
		String servers = System.getProperty("sun.net.spi.nameservice.nameservers");
		System.out.println(servers);
		return new InetAddress[0];
	}
	
}
