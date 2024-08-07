package org.sswr.util.net;

import java.net.InetAddress;

import org.sswr.util.io.OSInfo;

public interface SocketFactory
{
	public InetAddress[] getDefDNS();

	public static SocketFactory create()
	{
		switch (OSInfo.getOSType())
		{
		case WindowsNT:
		case WindowsNT64:
		case WindowsSvr:
			return new WindowsSocketFactory();
		case Android:
		case Linux_X86_64:
		case Linux_i686:
		case Unknown:
		default:
			return new LinuxSocketFactory();
		}
	}
}
