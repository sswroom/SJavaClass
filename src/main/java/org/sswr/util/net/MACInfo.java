package org.sswr.util.net;

import java.util.List;

import org.sswr.util.data.ByteTool;
import org.sswr.util.io.ResourceLoader;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class MACInfo
{
	private static List<MACEntry> macList;
	private static MACEntry uncMac;
	private static boolean loadMACList()
	{
		uncMac = new MACEntry(0, 0, "Unknown");
		return (macList = ResourceLoader.loadObjects(MACEntry.class, "MACInfo.macList.txt", new String[] {"rangeStart", "rangeEnd", "name"})) != null;
	}

	@Nonnull
	public static MACEntry getMACInfo(long macAddr)
	{
		if (macList == null && !loadMACList())
		{
			return uncMac;
		}
		int i = 0;
		int j = macList.size() - 1;
		int k;
		while (i <= j)
		{
			k = (i + j) >> 1;
			if (macAddr >= macList.get(k).getRangeStart() && macAddr <= macList.get(k).getRangeEnd())
			{
				return macList.get(k);
			}
			else if (macAddr < macList.get(k).getRangeStart())
			{
				j = k - 1;
			}
			else
			{
				i = k + 1;
			}
		}
		return uncMac;		
	}

	@Nonnull
	public static MACEntry getMACInfoBuff(@Nonnull byte[] mac, int index)
	{
		byte buff[] = new byte[8];
		buff[0] = 0;
		buff[1] = 0;
		buff[2] = mac[index + 0];
		buff[3] = mac[index + 1];
		buff[4] = mac[index + 2];
		buff[5] = mac[index + 3];
		buff[6] = mac[index + 4];
		buff[7] = mac[index + 5];
		return getMACInfo(ByteTool.readMInt64(buff, 0));
	}

	@Nonnull
	public static MACEntry getMACInfoOUI(@Nonnull byte[] oui, int index)
	{
		byte buff[] = new byte[8];
		buff[0] = 0;
		buff[1] = 0;
		buff[2] = oui[index + 0];
		buff[3] = oui[index + 1];
		buff[4] = oui[index + 2];
		buff[5] = 0;
		buff[6] = 0;
		buff[7] = 0;
		return getMACInfo(ByteTool.readMInt64(buff, 0));
	}

	@Nullable
	public static List<MACEntry> getMACEntryList()
	{
		if (macList == null && !loadMACList())
		{
			return null;
		}
		return macList;
	}
}
