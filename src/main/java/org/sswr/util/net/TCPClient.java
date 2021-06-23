package org.sswr.util.net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.sswr.util.io.IOStream;

public class TCPClient extends IOStream
{
	private Socket s;
	private long totalRecvSize;
	private long totalSendSize;
	long cliId;
	int flags; //1 = shutdown send, 2 = shutdown recv, 4 = closed, 8 = connect error

	public TCPClient(String hostName, int port)
	{
		super(hostName);
		this.totalRecvSize = 0;
		this.totalSendSize = 0;
		this.flags = 0;
		this.s = null;
	
		InetAddress addr;
		try
		{
			addr = InetAddress.getByName(hostName);
		}
		catch (UnknownHostException ex)
		{
			ex.printStackTrace();
			this.flags = 12;
			return;
		}
		try
		{
			this.s = new Socket(addr, port);
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

	public TCPClient(InetAddress addr, int port)
	{
		super("");
		this.totalRecvSize = 0;
		this.totalSendSize = 0;
		this.s = null;
		this.flags = 0;
		try
		{
			this.s = new Socket(addr, port);
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

	public TCPClient(Socket s)
	{
		super("");
		this.s = s;
		this.flags = 0;
		this.totalRecvSize = 0;
		this.totalSendSize = 0;
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
	public int read(byte[] buff, int ofst, int size)
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
	public int write(byte[] buff, int ofst, int size) {
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

	public String getLocalName()
	{
		return this.s.getLocalAddress().getHostAddress();
	}

	public long getCliId()
	{
		return this.cliId;
	}

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
	}

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
}
