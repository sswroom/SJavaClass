package org.sswr.util.net;

import java.util.List;

import org.sswr.util.io.ResourceLoader;

public class ASN1OIDDB
{
	private static List<ASN1OIDInfo> oidList;

	private static boolean loadOIDList()
	{
		return (oidList = ResourceLoader.loadObjects(ASN1OIDInfo.class, "SNMPOIDDB.oidList.txt", new String[] {"name", "len", "oid"})) != null;
	}

	public static void oidToNameString(byte[] pdu, int pduOfst, int pduSize, StringBuilder sb)
	{
		if (oidList == null && !loadOIDList())
		{
			return;
		}
		ASN1OIDInfo oid;
		int v;
		int checkSize = pduSize;
		while (checkSize > 0)
		{
			oid = oidGetEntry(pdu, pduOfst, checkSize);
			if (oid != null)
			{
				sb.append(oid.getName());
				v = 0;
				while (checkSize < pduSize)
				{
					v = (v << 7) | (pdu[checkSize] & 0x7f);
					if ((pdu[checkSize] & 0x80) == 0)
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

	public static ASN1OIDInfo oidGetEntry(byte[] pdu, int pduOfst, int pduSize)
	{
		if (oidList == null && !loadOIDList())
		{
			return null;
		}
		ASN1OIDInfo oid;
		int i = 0;
		int j = oidList.size() - 1;
		int k;
		int l;
		while (i <= j)
		{
			k = (i + j) >> 1;
			oid = oidList.get(k);
			l = SNMPUtil.oidCompare(pdu, pduSize, oid.getOid(), oid.getLen());
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
		}
		return null;
	}
}
