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
		case WINDOWS:
			return new WindowsSocketFactory();
		case ANDROID:
		case LINUX:
		case UNKNOWN:
		default:
			return new LinuxSocketFactory();
		}
	}
}
