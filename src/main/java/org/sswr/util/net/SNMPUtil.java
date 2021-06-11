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

	public static boolean oidStartsWith(byte []oid1, int oid1Len, byte []oid2, int oid2Len)
	{
		if (oid1Len < oid2Len)
			return false;
		int i = 0;
		while (i < oid2Len)
		{
			if (oid1[i] != oid2[i])
				return false;
			i++;
		}
		return true;
	}


	public static void oidToString(byte []pdu, int pduSize, StringBuilder sb)
	{
		int v = 0;
		int i = 1;
		sb.append((pdu[0] & 0xff) / 40);
		sb.append('.');
		sb.append((pdu[0] & 0xff) % 40);
		while (i < pduSize)
		{
			v = (v << 7) | (pdu[i] & 0x7f);
			if ((pdu[i] & 0x80) == 0)
			{
				sb.append('.');
				sb.append(v);
				v = 0;
			}
			i++;
		}
	}
}
