package org.sswr.util.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.nio.charset.Charset;

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
			{
				String cmd;
				if (addr instanceof Inet6Address)
				{
					cmd = "ping -n 1 " + addr.getHostAddress();
				}
				else if (addr instanceof Inet4Address)
				{
					cmd = "ping -n 1 " + addr.getHostAddress();
				}
				else
				{
					return false;
				}
				ProcessBuilder pb = new ProcessBuilder(cmd.split(" "));
				Process proc = pb.start();
				Charset cs = Charset.forName(System.getProperty("sun.jnu.encoding"));
				BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream(), cs));
				String line = null;
				int i = 2;
				while (true)
				{
					line = reader.readLine();
					if (line == null)
					{
						return false;
					}
					if (line.length() > 0)
					{
						if (--i <= 0)
						{
							line = line.trim();
							break;
						}
					}
				}
				if (addr instanceof Inet4Address)
				{
					i = line.indexOf("ms TTL=");
					if (ttl != null)
					{
						ttl.value = Integer.parseInt(line.substring(i + 7));
					}
					if (respTime_us != null)
					{
						line = line.substring(0, i);
						i = line.lastIndexOf(' ');
						if (line.charAt(i + 1) == '=')
						{
							respTime_us.value = Integer.parseInt(line.substring(i + 2)) * 1000;
						}
						else
						{
							respTime_us.value = 0;
						}
					}
					return true;
				}
				else if (addr instanceof Inet6Address)
				{
					if (line.endsWith("ms"))
					{
						if (ttl != null)
						{
							ttl.value = 0;
						}
						if (respTime_us != null)
						{
							line = line.substring(0, line.length() - 2);
							i = line.lastIndexOf(' ');
							if (line.charAt(i + 1) == '=')
							{
								respTime_us.value = Integer.parseInt(line.substring(i + 2)) * 1000;
							}
							else
							{
								respTime_us.value = 0;
							}
						}
						return true;
					}
				}
				return false;
			}
		case UNKNOWN:
		default:
			return false;
		}
	}
}
