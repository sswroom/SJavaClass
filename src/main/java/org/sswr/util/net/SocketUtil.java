package org.sswr.util.net;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;

import org.sswr.util.crypto.hash.CRC32R;
import org.sswr.util.data.ByteTool;

import jakarta.annotation.Nonnull;

public class SocketUtil
{
	@Nonnull
	public static String getIPv4Name(@Nonnull byte[] buff, int ofst)
	{
		byte[] addr = new byte[4];
		addr[0] = buff[ofst];
		addr[1] = buff[ofst + 1];
		addr[2] = buff[ofst + 2];
		addr[3] = buff[ofst + 3];
		try
		{
			return Inet4Address.getByAddress(addr).getHostAddress();
		}
		catch (UnknownHostException ex)
		{
			ex.printStackTrace();
			return "0.0.0.0";
		}
	}

	@Nonnull
	public static String getIPv4Name(int ip)
	{
		byte[] addr = new byte[4];
		ByteTool.writeMInt32(addr, 0, ip);
		try
		{
			return Inet4Address.getByAddress(addr).getHostAddress();
		}
		catch (UnknownHostException ex)
		{
			ex.printStackTrace();
			return "0.0.0.0";
		}
	}

	@Nonnull
	public static InetAddress getAddr(int ipv4)
	{
		byte[] addr = new byte[4];
		ByteTool.writeMInt32(addr, 0, ipv4);
		try
		{
			return Inet4Address.getByAddress(addr);
		}
		catch (UnknownHostException ex)
		{
			ex.printStackTrace();
			return Inet4Address.getLoopbackAddress();
		}
	}

	@Nonnull
	public static IPType getIPType(@Nonnull InetAddress addr)
	{
		if (addr instanceof Inet4Address)
		{
			byte []addrBuff = ((Inet4Address)addr).getAddress();
			if (addrBuff[0] == 0 && addrBuff[1] == 0 && addrBuff[2] == 0 && addrBuff[3] == 0)
			{
				return IPType.NETWORK;
			}
			else if ((addrBuff[0] & 0xff) == 0xff && (addrBuff[1] & 0xff) == 0xff && (addrBuff[2] & 0xff) == 0xff && (addrBuff[3] & 0xff) == 0xff)
			{
				return IPType.BROADCAST;
			}
			else if (addrBuff[0] == 127)
			{
				return IPType.LOCAL;
			}
			else if ((addrBuff[0] & 0xff) >= 224 && (addrBuff[0] & 0xff) <= 239)
			{
				return IPType.MULTICAST;
			}
		
			if ((addrBuff[0] & 0xff) <= 127)
			{
				if ((addrBuff[1] & 0xff) == 255 && (addrBuff[2] & 0xff) == 255 && (addrBuff[3] & 0xff) == 255)
				{
					return IPType.BROADCAST;
				}
				else if ((addrBuff[1] & 0xff) == 0 && (addrBuff[2] & 0xff) == 0 && (addrBuff[3] & 0xff) == 0)
				{
					return IPType.NETWORK;
				}
			}
			else if ((addrBuff[0] & 0xff) <= 191)
			{
				if ((addrBuff[2] & 0xff) == 255 && (addrBuff[3] & 0xff) == 255)
				{
					return IPType.BROADCAST;
				}
				else if ((addrBuff[2] & 0xff) == 0 && (addrBuff[3] & 0xff) == 0)
				{
					return IPType.NETWORK;
				}
			}
			else if ((addrBuff[0] & 0xff) <= 223)
			{
				if ((addrBuff[3] & 0xff) == 255)
				{
					return IPType.BROADCAST;
				}
				else if ((addrBuff[3] & 0xff) == 0)
				{
					return IPType.NETWORK;
				}
			}
			
			if ((addrBuff[0] & 0xff) == 10)
			{
				return IPType.PRIVATE;
			}
			else if ((addrBuff[0] & 0xff) == 172 && ((addrBuff[1] & 0xff) & 0xf0) == 16)
			{
				return IPType.PRIVATE;
			}
			else if ((addrBuff[0] & 0xff) == 192 && (addrBuff[1] & 0xff) == 168)
			{
				return IPType.PRIVATE;
			}
			else
			{
				return IPType.PUBLIC;
			}
		}
		else
		{
			return IPType.PRIVATE;
		}
	}

	public static long genSocketId(@Nonnull Socket s)
	{
		int lPort = s.getLocalPort();
		int rPort = 0;
		InetAddress rAddr = null;
		SocketAddress remAddr = s.getRemoteSocketAddress();
		if (remAddr != null && (remAddr instanceof InetSocketAddress))
		{
			InetSocketAddress netAddr = (InetSocketAddress)remAddr;
			rAddr = netAddr.getAddress();
			rPort = netAddr.getPort();
			return (calcCliId(rAddr) & (long)0xffffffff) | (((long)rPort) << 32) | (((long)lPort) << 48);
		}
		else
		{
			return 0;
		}
	}

	public static long calcCliId(@Nonnull InetAddress addr)
	{
		if (addr instanceof Inet4Address)
		{
			return ByteTool.readMInt32(((Inet4Address)addr).getAddress(), 0);
		}
		else if (addr instanceof Inet6Address)
		{
			CRC32R crc = new CRC32R();
			byte[] ret;
			byte[] addrBuff = addr.getAddress();
			crc.calc(addrBuff, 0, addrBuff.length);
			ret = crc.getValue();
			return ByteTool.readMInt32(ret, 0);
		}
		return 0;
	}
}
