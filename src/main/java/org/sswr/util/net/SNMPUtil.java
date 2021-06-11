package org.sswr.util.net;

public class SNMPUtil
{
	public static int oidCompare(byte []oid1, byte[] oid2)
	{
		int oid1Len = 0;
		int oid2Len = 0;
		if (oid1 != null)
		{
			oid1Len = oid1.length;
		}
		if (oid2 != null)
		{
			oid2Len = oid2.length;
		}

		int i = 0;
		while (true)
		{
			if (i == oid1Len && i == oid2Len)
			{
				return 0;
			}
			else if (i >= oid1Len)
			{
				return -1;
			}
			else if (i >= oid2Len)
			{
				return 1;
			}
			else if ((oid1[i] & 0xff) > (oid2[i] & 0xff))
			{
				return 1;
			}
			else if ((oid1[i] & 0xff) < (oid2[i] & 0xff))
			{
				return -1;
			}
			i++;
		}
	}
}
