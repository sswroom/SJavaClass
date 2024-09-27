package org.sswr.util.net;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;

import org.sswr.util.io.OSInfo;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public abstract class SocketFactory
{
	private String proxyHost;
	private int proxyPort;

	public SocketFactory()
	{
		this.proxyHost = null;
		this.proxyPort = 0;
	}

	public abstract InetAddress[] getDefDNS();

	@Nonnull
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

	@Nullable
	public Proxy getProxy()
	{
		if (this.proxyHost == null || this.proxyPort == 0)
		{
			return null;
		}
		Proxy proxy = new Proxy(Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
		return proxy;
	}

	public void setProxy(@Nullable String host, int port)
	{
		this.proxyHost = host;
		this.proxyPort = port;
	}
}
