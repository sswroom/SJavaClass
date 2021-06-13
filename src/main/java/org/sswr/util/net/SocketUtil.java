package org.sswr.util.net;

import java.net.Inet4Address;
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
			return Inet4Address.getByAddress(addr).toString();
		}
		catch (UnknownHostException ex)
		{
			ex.printStackTrace();
			return "0.0.0.0";
		}
	}
}
