package org.sswr.util.crypto;

public class ASN1OIDDB
{
	public static void oidToNameString(byte[] pdu, int ofst, int size, StringBuilder sb)
	{
		OIDInfo oid;
		int v;
		int checkSize = size;
		while (checkSize > 0)
		{
			oid = oidGetEntry(pdu, ofst, checkSize);
			if (oid != null)
			{
				sb.append(oid.name);
				v = 0;
				while (checkSize < size)
				{
					v = (v << 7) | (pdu[ofst + checkSize] & 0x7f);
					if ((pdu[ofst + checkSize] & 0x80) == 0)
					{
						sb.append('.');
						sb.append(v);
						v = 0;
					}
					checkSize++;
				}
				return;
			}
			checkSize--;
		}
	}

	public static OIDInfo oidGetEntry(byte[] pdu, int ofst, int size)
	{
/* 		OIDInfo oid;
		int i = 0;
		int j = oidList.length - 1;
		int k;
		int l;
		while (i <= j)
		{
			k = (i + j) >> 1;
			oid = oidList[k];
			l = ASN1Util.oidCompare(pdu, ofst, size, oid.oid, 0, oid.oid.length);
			if (l > 0)
			{
				i = k + 1;
			}
			else if (l < 0)
			{
				j = k - 1;
			}
			else
			{
				return oid;
			}
		}*/
		return null;
	}
}
