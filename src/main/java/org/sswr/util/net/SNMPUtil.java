package org.sswr.util.net;

import java.util.List;

import org.sswr.util.data.ByteTool;
import org.sswr.util.data.SharedInt;
import org.sswr.util.data.StringUtil;

public class SNMPUtil
{
	public static SNMPErrorStatus pduParseMessage(byte[] pdu, int pduOfst, int size, SharedInt reqId, List<SNMPBindingItem> itemList)
	{
		int i;
		if (pdu[pduOfst + 0] != 0x30)
		{
			reqId.value = 0;
			return SNMPErrorStatus.UNKRESP;
		}
		int pduEnd = pduOfst + size;
		SNMPBindingItem item;
		SharedInt bindingLen = new SharedInt();
		int bindingEnd;
		SharedInt pduLen = new SharedInt();
		int err;
		i = ASN1Util.pduParseLen(pdu, pduOfst + 1, pduEnd, pduLen);
		if (i > pduEnd)
		{
			reqId.value = 0;
			return SNMPErrorStatus.UNKRESP;
		}
		if (i + pduLen.value != pduEnd)
		{
			reqId.value = 0;
			return SNMPErrorStatus.UNKRESP;
		}
		if (pdu[i] != 2 || pdu[i + 1] != 1)
		{
			reqId.value = 0;
			return SNMPErrorStatus.UNKRESP;
		}
		if (pdu[i + 2] == 0 || pdu[i + 2] == 1) //v1 message / v2c message
		{
			i += 3;
			if (pdu[i] != 4)
			{
				reqId.value = 0;
				return SNMPErrorStatus.UNKRESP;
			}
			i = ASN1Util.pduParseLen(pdu, i + 1, pduEnd, pduLen);
			if (i > pduEnd)
			{
				reqId.value = 0;
				return SNMPErrorStatus.UNKRESP;
			}
			if (i + pduLen.value >= pduEnd)
			{
				reqId.value = 0;
				return SNMPErrorStatus.UNKRESP;
			}
			i += pduLen.value;
			if ((pdu[i] & 0xf0) != 0xa0)
			{
				reqId.value = 0;
				return SNMPErrorStatus.UNKRESP;
			}
			i = ASN1Util.pduParseLen(pdu, i + 1, pduEnd, pduLen);
			if (i > pduEnd)
			{
				reqId.value = 0;
				return SNMPErrorStatus.UNKRESP;
			}
			if (i + pduLen.value != pduEnd)
			{
				reqId.value = 0;
				return SNMPErrorStatus.UNKRESP;
			}
			if (pdu[i] != 2)
			{
				reqId.value = 0;
				return SNMPErrorStatus.UNKRESP;
			}
			if (pdu[i + 1] == 1)
			{
				reqId.value = pdu[i + 2] & 0xff;
				i += 3;
			}
			else if (pdu[i + 1] == 2)
			{
				reqId.value = ByteTool.readMUInt16(pdu, i + 2);
				i += 4;
			}
			else if (pdu[i + 1] == 3)
			{
				reqId.value = ByteTool.readMUInt24(pdu, i + 2);
				i += 5;
			}
			else if (pdu[i + 1] == 4)
			{
				reqId.value = ByteTool.readMInt32(pdu, i + 2);
				i += 6;
			}
			else
			{
				reqId.value = 0;
				return SNMPErrorStatus.UNKRESP;
			}
			if (pdu[i] != 2) //error-status
			{
				return SNMPErrorStatus.UNKRESP;
			}
			if (pdu[i + 1] != 1)
			{
				return SNMPErrorStatus.UNKRESP;
			}
			err = pdu[i + 2];
			i += 3;
			if (pdu[i] != 2) //error-status
			{
				return SNMPErrorStatus.UNKRESP;
			}
			i = ASN1Util.pduParseLen(pdu, i + 1, pduEnd, pduLen);
			if (i > pduEnd)
			{
				return SNMPErrorStatus.UNKRESP;
			}
			i += pduLen.value;
			if (pdu[i] != 0x30)
			{
				return SNMPErrorStatus.UNKRESP;
			}
			i = ASN1Util.pduParseLen(pdu, i + 1, pduEnd, pduLen);
			if (i > pduEnd)
			{
				return SNMPErrorStatus.UNKRESP;
			}
			if (i + pduLen.value != pduEnd)
			{
				return SNMPErrorStatus.UNKRESP;
			}
			while (i < pduEnd)
			{
				if (pdu[i] != 0x30)
				{
					return SNMPErrorStatus.UNKRESP;
				}
				i = ASN1Util.pduParseLen(pdu, i + 1, pduEnd, bindingLen);
				if (i > pduEnd)
				{
					return SNMPErrorStatus.UNKRESP;
				}
				bindingEnd = i + bindingLen.value;
				if (pdu[i] != 6)
				{
					return SNMPErrorStatus.UNKRESP;
				}
				i = ASN1Util.pduParseLen(pdu, i + 1, pduEnd, pduLen);
				if (i > pduEnd || pduLen.value > 64)
				{
					return SNMPErrorStatus.UNKRESP;
				}
				if (i + pduLen.value > bindingEnd)
				{
					return SNMPErrorStatus.UNKRESP;
				}
				item = new SNMPBindingItem();
				item.setOid(pdu, i, pduLen.value);
				i += pduLen.value;
				if (i + 2 > bindingEnd)
				{
					return SNMPErrorStatus.UNKRESP;
				}
				item.setValType(pdu[i]);
				i = ASN1Util.pduParseLen(pdu, i + 1, pduEnd, pduLen);
				if (i + pduLen.value != bindingEnd)
				{
					return SNMPErrorStatus.UNKRESP;
				}
				item.setValLen(pduLen.value);
				if (pduLen.value == 0)
				{
					item.setValBuff(null);
				}
				else
				{
					byte[] valBuff = new byte[pduLen.value];
					ByteTool.copyArray(valBuff, 0, pdu, i, pduLen.value);
					item.setValBuff(valBuff);
				}
				itemList.add(item);
				i += pduLen.value;
			}
			return SNMPErrorStatus.getStatus(err);
		}
		else
		{
			reqId.value = 0;
			return SNMPErrorStatus.UNKRESP;
		}
	}
	
	public static int oidCompare(byte []oid1, int oid1Len, byte[] oid2, int oid2Len)
	{
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


	public static void oidToString(byte []pdu, int pduOfst, int pduSize, StringBuilder sb)
	{
		int v = 0;
		int i = 1;
		sb.append((pdu[pduOfst + 0] & 0xff) / 40);
		sb.append('.');
		sb.append((pdu[pduOfst + 0] & 0xff) % 40);
		while (i < pduSize)
		{
			v = (v << 7) | (pdu[pduOfst + i] & 0x7f);
			if ((pdu[pduOfst + i] & 0x80) == 0)
			{
				sb.append('.');
				sb.append(v);
				v = 0;
			}
			i++;
		}
	}

	public static int oidCalcPDUSize(String oid)
	{
		Integer v;
		int retSize = 1;
		String sarr[] = oid.split("\\.");
		int i;
		int j;
		j = sarr.length;
		if (j == 1 || j == 2)
		{
			return 1;
		}
		i = 2;
		while (i < j)
		{
			v = StringUtil.toInteger(sarr[i++]);
			if (v == null)
			{
				return retSize;
			}
			while (v >= 128)
			{
				retSize++;
				v = v >> 7;
			}
			retSize++;
		}
		return retSize;
	}
	
	public static int oidText2PDU(String oid, byte[] pduBuff, int pduOfst)
	{
		Integer v;
		int retSize = 1;
		String sarr[] = oid.split("\\.");
		int i;
		int j;
		i = 0;
		j = sarr.length;
		if (j == 1)
		{
			pduBuff[pduOfst] = (byte)(StringUtil.toIntegerS(sarr[0], 0) * 40);
			return 1;
		}
		pduBuff[pduOfst] = (byte)(StringUtil.toIntegerS(sarr[0], 0) * 40 + StringUtil.toIntegerS(sarr[1], 0));
		if (j == 2)
		{
			return 1;
		}
		i = 2;
		while (i < j)
		{
			v = StringUtil.toInteger(sarr[i++]);
			if (v == null)
			{
				return retSize;
			}
			if (v < 128)
			{
				pduBuff[pduOfst + retSize] = (byte)v.intValue();
				retSize++;
			}
			else if (v < 0x4000)
			{
				pduBuff[pduOfst + retSize] = (byte)(0x80 | (v >> 7));
				pduBuff[pduOfst + retSize + 1] = (byte)(v & 0x7f);
				retSize += 2;
			}
			else if (v < 0x200000)
			{
				pduBuff[pduOfst + retSize] = (byte)(0x80 | (v >> 14));
				pduBuff[pduOfst + retSize + 1] = (byte)(0x80 | ((v >> 7) & 0x7f));
				pduBuff[pduOfst + retSize + 2] = (byte)(v & 0x7f);
				retSize += 3;
			}
			else if (v < 0x10000000)
			{
				pduBuff[pduOfst + retSize] = (byte)(0x80 | (v >> 21));
				pduBuff[pduOfst + retSize + 1] = (byte)(0x80 | ((v >> 14) & 0x7f));
				pduBuff[pduOfst + retSize + 2] = (byte)(0x80 | ((v >> 7) & 0x7f));
				pduBuff[pduOfst + retSize + 3] = (byte)(v & 0x7f);
				retSize += 4;
			}
			else
			{
				pduBuff[pduOfst + retSize] = (byte)(0x80 | (v >> 28));
				pduBuff[pduOfst + retSize + 1] = (byte)(0x80 | ((v >> 21) & 0x7f));
				pduBuff[pduOfst + retSize + 2] = (byte)(0x80 | ((v >> 14) & 0x7f));
				pduBuff[pduOfst + retSize + 3] = (byte)(0x80 | ((v >> 7) & 0x7f));
				pduBuff[pduOfst + retSize + 4] = (byte)(v & 0x7f);
				retSize += 5;
			}
		}
		return retSize;
	}

	public static String typeGetName(byte type)
	{
		switch (type & 0xff)
		{
		case 2:
			return "INTEGER";
		case 4:
			return "OCTET STRING";
		case 5:
			return "NULL";
		case 6:
			return "OBJECT IDENTIFIER";
		case 0x30:
			return "SEQUENCE";
		case 0x40:
			return "IpAddress";
		case 0x41:
			return "Counter32";
		case 0x42:
			return "Gauge32";
		case 0x43:
			return "Timeticks";
		case 0x44:
			return "Opaque";
		case 0x46:
			return "Counter64";
		case 0xA0:
			return "GetRequest-PDU";
		case 0xA1:
			return "GetNextRequest-PDU";
		case 0xA2:
			return "GetResponse-PDU";
		case 0xA3:
			return "SetRequest-PDU";
		case 0xA4:
			return "Trap-PDU";
		default:
			return "UNKNOWN";
		}
	}
}
