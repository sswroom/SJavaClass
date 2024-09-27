package org.sswr.util.net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.sswr.util.io.IOStream;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class TCPClient extends IOStream
{
	private static boolean debug = false;
	private Socket s;
	private SSLEngine ssl;
	private long totalRecvSize;
	private long totalSendSize;
	private long cliId;
	private int flags; //1 = shutdown send, 2 = shutdown recv, 4 = closed, 8 = connect error
	private int timeoutMS;

	public TCPClient(@Nonnull String hostName, int port, @Nullable SSLEngine ssl, @Nonnull TCPClientType cliType)
	{
		super(hostName);
		this.ssl = ssl;
		this.totalRecvSize = 0;
		this.totalSendSize = 0;
		this.flags = 0;
		this.s = null;
		this.timeoutMS = 0;
	
		InetAddress addr;
		try
		{
			addr = InetAddress.getByName(hostName);
		}
		catch (UnknownHostException ex)
		{
			//ex.printStackTrace();
			this.flags = 12;
			return;
		}
		try
		{
			if (cliType == TCPClientType.SSL)
			{
				javax.net.SocketFactory factory;
				if (ssl != null)
					factory = ssl.getSocketFactory();
				else
					factory = SSLSocketFactory.getDefault();
				this.s = factory.createSocket(addr, port);
				((SSLSocket)this.s).startHandshake();
			}
			else
			{
				this.s = new Socket();
				this.s.connect(new InetSocketAddress(addr, port));
			}
		}
		catch (IOException ex)
		{
			if (debug)
			{
				ex.printStackTrace();
			}
			this.flags = 12;
			return;
		}
		this.setSourceName(this.getRemoteName());
		this.cliId = SocketUtil.genSocketId(s);
	}

	public TCPClient(@Nonnull String hostName, int port, @Nullable SSLEngine ssl, @Nonnull TCPClientType cliType, int connTimeoutMS)
	{
		super(hostName);
		this.ssl = ssl;
		this.totalRecvSize = 0;
		this.totalSendSize = 0;
		this.flags = 0;
		this.s = null;
		this.timeoutMS = 0;
	
		InetAddress addr;
		try
		{
			addr = InetAddress.getByName(hostName);
		}
		catch (UnknownHostException ex)
		{
			//ex.printStackTrace();
			this.flags = 12;
			return;
		}
		try
		{
			if (cliType == TCPClientType.SSL)
			{
				javax.net.SocketFactory factory;
				if (ssl != null)
					factory = ssl.getSocketFactory();
				else
					factory = SSLSocketFactory.getDefault();
				this.s = factory.createSocket(hostName, port);
				((SSLSocket)this.s).startHandshake();
			}
			else
			{
				this.s = new Socket();
				this.s.connect(new InetSocketAddress(addr, port), connTimeoutMS);
			}
		}
		catch (IOException ex)
		{
			if (debug)
			{
				ex.printStackTrace();
			}
			this.flags = 12;
			return;
		}
		this.setSourceName(this.getRemoteName());
		this.cliId = SocketUtil.genSocketId(s);
	}

	public TCPClient(@Nonnull InetAddress addr, int port, @Nullable SSLEngine ssl, @Nonnull TCPClientType cliType)
	{
		super("");
		this.ssl = ssl;
		this.totalRecvSize = 0;
		this.totalSendSize = 0;
		this.s = null;
		this.flags = 0;
		this.timeoutMS = 0;
		try
		{
			if (cliType == TCPClientType.SSL)
			{
				javax.net.SocketFactory factory;
				if (ssl != null)
					factory = ssl.getSocketFactory();
				else
					factory = SSLSocketFactory.getDefault();
				this.s = factory.createSocket(addr, port);
				((SSLSocket)this.s).startHandshake();
			}
			else
			{
				this.s = new Socket(addr, port);
			}
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
			this.flags = 12;
			return;
		}
		this.setSourceName(this.getRemoteName());
		this.cliId = SocketUtil.genSocketId(s);
	}

	public TCPClient(@Nullable Socket s, @Nonnull SSLEngine ssl)
	{
		super("");
		this.ssl = ssl;
		this.s = s;
		this.flags = 0;
		this.totalRecvSize = 0;
		this.totalSendSize = 0;
		this.timeoutMS = 0;
		if (s != null)
		{
			this.cliId = SocketUtil.genSocketId(s);
			this.setSourceName(this.getRemoteName());
		}
		else
		{
			this.cliId = 0;
			this.flags = 4;
		}
	}

	@Override
	public boolean isDown()
	{
		if (this.s == null || (this.flags & 6) != 0)
		{
			return true;
		}
		return false;
	}

	@Override
	public int read(@Nonnull byte[] buff, int ofst, int size)
	{
		if (s != null && (this.flags & 6) == 0)
		{
			try
			{
				int recvSize = this.s.getInputStream().read(buff, ofst, size);
				if (recvSize > 0)
				{
					this.totalRecvSize += recvSize;
					return recvSize;	
				}
				return 0;
			}
			catch (IOException ex)
			{
				this.flags |= 2;
				return 0;
			}
		}
		else
		{
			return 0;
		}
	}

	@Override
	public int write(@Nonnull byte[] buff, int ofst, int size) {
		if (this.s != null && (this.flags & 5) == 0)
		{
			try
			{
				this.s.getOutputStream().write(buff, ofst, size);
				this.totalSendSize += size;
				return size;
			}
			catch (IOException ex)
			{
				this.flags |= 1;
				return 0;
			}
		}
		else
		{
			return 0;
		}
	}

	@Override
	public int flush()
	{
		return 0;
	}

	@Override
	public void close()
	{
		if ((this.flags & 4) == 0)
		{
			this.flags |= 4;
			try
			{
				this.s.close();
			}
			catch (IOException ex)
			{

			}
		}
	}

	@Override
	public boolean recover() {
		return false;
	}

	public boolean isClosed()
	{
		return (this.flags & 4) != 0;
	}

	public boolean isSendDown()
	{
		return (this.flags & 1) != 0;
	}

	public boolean isRecvDown()
	{
		return (this.flags & 2) != 0;
	}

	public boolean isConnectError()
	{
		return (this.flags & 8) != 0;
	}

	public int getRecvBuffSize()
	{
		try
		{
			return this.s.getReceiveBufferSize();
		}
		catch (SocketException ex)
		{
			ex.printStackTrace();
			return 0;
		}
	}

	@Nonnull
	public String getRemoteName()
	{
		SocketAddress addr = this.s.getRemoteSocketAddress();
		if (addr instanceof InetSocketAddress)
		{
			InetSocketAddress inetAddr = (InetSocketAddress)addr;
			return inetAddr.getAddress().getHostAddress();
		}
		return addr.toString();
	}

	@Nonnull
	public String getLocalName()
	{
		return this.s.getLocalAddress().getHostAddress();
	}

	public long getCliId()
	{
		return this.cliId;
	}

	@Nullable
	public InetAddress getRemoteAddr()
	{
		SocketAddress addr = this.s.getRemoteSocketAddress();
		if (addr instanceof InetSocketAddress)
		{
			InetSocketAddress inetAddr = (InetSocketAddress)addr;
			return inetAddr.getAddress();
		}
		return null;
	}

	public int getRemotePort()
	{
		SocketAddress addr = this.s.getRemoteSocketAddress();
		if (addr instanceof InetSocketAddress)
		{
			InetSocketAddress inetAddr = (InetSocketAddress)addr;
			return inetAddr.getPort();
		}
		return 0;
	}

	public int getLocalPort()
	{
		return this.s.getLocalPort();
	}

	public void setNoDelay(boolean val)
	{
		try
		{
			this.s.setTcpNoDelay(val);
		}
		catch (SocketException ex)
		{
			ex.printStackTrace();
		}
	}

	public void shutdownSend()
	{
		this.flags |= 1;
		try
		{
			this.s.shutdownOutput();
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
		}
	}

	public void setTimeout(int ms)
	{
		try
		{
			this.s.setSoTimeout(ms);
		}
		catch (SocketException ex)
		{
			ex.printStackTrace();
		}
		this.timeoutMS = ms;
	}
	
	public int getTimeoutMS()
	{
		return this.timeoutMS;
	}

	@Nullable
	public Socket getSocket()
	{
		return this.s;
	}

	public long getTotalRecvSize()
	{
		return this.totalRecvSize;
	}

	public long getTotalSendSize()
	{
		return this.totalSendSize;
	}

	public boolean sslHandshake()
	{
		if (this.s instanceof SSLSocket)
		{
		}
		else
		{
			try
			{
				SSLSocketFactory factory;
				if (this.ssl != null)
					factory = (SSLSocketFactory)this.ssl.getSocketFactory();
				else
					factory = (SSLSocketFactory)SSLSocketFactory.getDefault();
				SSLSocket soc = (SSLSocket)factory.createSocket(this.s, null, false);
				this.s = soc;
				soc.setSoTimeout(5000);
				soc.setUseClientMode(true);
				soc.startHandshake();
				return true;
			}
			catch (IOException ex)
			{
				ex.printStackTrace();
				return false;
			}
		}
		return false;
	}
}
