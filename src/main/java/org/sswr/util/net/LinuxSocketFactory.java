package org.sswr.util.net;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.sswr.util.data.DataTools;
import org.sswr.util.io.UTF8Reader;

public class LinuxSocketFactory extends SocketFactory
{
	@Override
	public InetAddress[] getDefDNS() {
		try
		{
			List<InetAddress> addrList = new ArrayList<InetAddress>();
			StringBuilder sb = new StringBuilder();
			UTF8Reader reader = new UTF8Reader(new FileInputStream("/etc/resolv.conf"));
			while (true)
			{
				sb.setLength(0);
				if (!reader.readLine(sb, 1024))
				{
					break;
				}
				String line = sb.toString().trim();
				if (line.startsWith("#"))
				{

				}
				else
				{
					String[] sarr = line.split(" ");
					if (sarr.length == 2)
					{
						if (sarr[0].equals("nameserver"))
						{
							try
							{
								addrList.add(InetAddress.getByName(sarr[1]));
							}
							catch (UnknownHostException ex)
							{
								ex.printStackTrace();
							}
						}
					}
				}
			}
			reader.close();
			return DataTools.toArray(InetAddress.class, addrList);
		}
		catch (IOException ex)
		{
			return new InetAddress[0];
		}
	}
	
}
