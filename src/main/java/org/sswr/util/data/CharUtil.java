package org.sswr.util.data;

import java.util.ArrayList;
import java.util.List;

import org.sswr.util.io.ResourceLoader;

public class CharUtil
{
	private static List<UnicodeBlock> ublk;

	public static UnicodeBlock getUnicodeBlock(char c)
	{
		if (ublk == null)
		{
			ublk = ResourceLoader.loadObjects(UnicodeBlock.class, "CharUtil.ublk.txt", new String[]{"firstCode", "lastCode", "dblWidth", "name"});
			if (ublk == null) ublk = new ArrayList<UnicodeBlock>();
		}
		UnicodeBlock blk;
		int i = 0;
		int j = ublk.size() - 1;
		int k;
		while (i <= j)
		{
			k = (i + j) >> 1;
			blk = ublk.get(k);
			if (c < blk.getFirstCode())
			{
				j = k - 1;
			}
			else if (c > blk.getLastCode())
			{
				i = k + 1;
			}
			else
			{
				return blk;
			}
		}
		return null;
	}

	public static boolean isDoubleSize(char c)
	{
		if (c == 0x3000)
		{
			return true;
		}
		if (c >= 0xff01 && c <= 0xff5e)
		{
			return true;
		}
		UnicodeBlock blk = getUnicodeBlock(c);
		if (blk == null)
		{
			return false;
		}
		return blk.getDblWidth();
	}
}
