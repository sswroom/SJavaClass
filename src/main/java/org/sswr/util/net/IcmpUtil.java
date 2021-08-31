package org.sswr.util.net;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;

import org.sswr.util.data.SharedInt;
import org.sswr.util.io.OSInfo;
import org.sswr.util.io.UTF8Reader;

public class IcmpUtil
{
	public static boolean sendEcho(InetAddress addr, SharedInt respTime_us, SharedInt ttl) throws IOException
	{
		switch (OSInfo.getOSType())
		{
		case ANDROID:
		case LINUX:
			{
				String cmd;
				if (addr instanceof Inet6Address)
				{
					cmd = "ping6 -c 1 " + addr.getHostAddress();
				}
				else if (addr instanceof Inet4Address)
				{
					cmd = "ping -c 1 " + addr.getHostAddress();
				}
				else
				{
					return false;
				}
				ProcessBuilder pb = new ProcessBuilder(cmd.split(" "));
				Process proc = pb.start();
				UTF8Reader reader = new UTF8Reader(proc.getInputStream());
				StringBuilder sb = new StringBuilder();
				reader.readLine(sb, 512);
				sb.setLength(0);
				reader.readLine(sb, 512);
				String line = sb.toString();
				int i = line.indexOf(": ");
				if (i != -1)
				{
					line = line.substring(i + 2);
				}
				if (line.startsWith("icmp_seq="))
				{
					String[] sarr = line.split(" ");
					if (sarr.length == 4)
					{
						if (ttl != null)
						{
							ttl.value = Integer.parseInt(sarr[1].substring(4));
						}
						if (respTime_us != null)
						{
							respTime_us.value = (int)Math.round(Double.parseDouble(sarr[2].substring(5)) * 1000.0);
						}
						return true;
					}
				}
			}
			return false;
		case WINDOWS:
		case UNKNOWN:
		default:
			return false;
		}
	}
}
