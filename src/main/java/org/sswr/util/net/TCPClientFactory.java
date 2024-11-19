package org.sswr.util.net;

import java.net.InetAddress;
import java.time.Duration;

import org.sswr.util.net.HTTPProxyTCPClient.PasswordType;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class TCPClientFactory {
	private @Nullable SocketFactory sockf;
	private @Nullable String proxyHost;
	private int proxyPort;
	private @Nullable String proxyUser;
	private @Nullable String proxyPwd;

	public TCPClientFactory(@Nullable SocketFactory sockf)
	{
		this.sockf = sockf;
		this.proxyHost = null;
		this.proxyPort = 0;
		this.proxyUser = null;
		this.proxyPwd = null;
	}

	public void setProxy(@Nonnull String proxyHost, int proxyPort, @Nullable String proxyUser, @Nullable String proxyPwd)
	{
		this.proxyHost = proxyHost;
		this.proxyPort = proxyPort;
		this.proxyUser = proxyUser;
		this.proxyPwd = proxyPwd;
	}

	@Nullable
	public SocketFactory getSocketFactory()
	{
		return this.sockf;
	}

	@Nonnull
	public TCPClient create(@Nonnull String name, int port, @Nonnull Duration timeout)
	{
		SocketFactory sockf;
		TCPClient cli;
		String proxyHost;
		if ((sockf = this.sockf) != null && (proxyHost = this.proxyHost) != null && proxyPort != 0)
		{
			cli = new HTTPProxyTCPClient(sockf, proxyHost, proxyPort, (this.proxyUser == null || this.proxyPwd == null)?PasswordType.None:PasswordType.Basic, this.proxyUser, this.proxyPwd, name, port);
		}
		else
		{
			cli = new TCPClient(name, port, null, TCPClientType.PLAIN, (int)timeout.toMillis());
		}
		return cli;
	}

	@Nonnull
	public TCPClient create(int ip, int port, @Nonnull Duration timeout)
	{
		SocketFactory sockf;
		TCPClient cli;
		String proxyHost;
		if ((sockf = this.sockf) != null && (proxyHost = this.proxyHost) != null && proxyPort != 0)
		{
			String ipName = SocketUtil.getIPv4Name(ip);
			cli = new HTTPProxyTCPClient(sockf, proxyHost, proxyPort, (this.proxyUser == null || this.proxyPwd == null)?PasswordType.None:PasswordType.Basic, this.proxyUser, this.proxyPwd, ipName, port);
		}
		else
		{
			InetAddress addr = SocketUtil.getAddr(ip);
			cli = new TCPClient(addr, port, null, TCPClientType.PLAIN, (int)timeout.toMillis());
		}
		return cli;
	}

	@Nonnull
	public TCPClient create(@Nonnull InetAddress addr, int port, @Nonnull Duration timeout)
	{
		SocketFactory sockf;
		TCPClient cli;
		String proxyHost;
		if ((sockf = this.sockf) != null && (proxyHost = this.proxyHost) != null && proxyPort != 0)
		{
			String ipName = addr.toString();
			cli = new HTTPProxyTCPClient(sockf, proxyHost, proxyPort, (this.proxyUser == null || this.proxyPwd == null)?PasswordType.None:PasswordType.Basic, this.proxyUser, this.proxyPwd, ipName, port);
		}
		else
		{
			cli = new TCPClient(addr, port, null, TCPClientType.PLAIN, (int)timeout.toMillis());
		}
		return cli;
	}

}
