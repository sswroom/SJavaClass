package org.sswr.util.net;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class SocketUtil
{
	public static String getIPv4Name(byte[] buff, int ofst)
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

	public static IPType getIPType(InetAddress addr)
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
}
