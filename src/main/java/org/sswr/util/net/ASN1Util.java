package org.sswr.util.net;

import java.nio.charset.StandardCharsets;

import org.sswr.util.data.ByteTool;
import org.sswr.util.data.SharedByte;
import org.sswr.util.data.SharedInt;

public class ASN1Util
{
	public static int pduParseLen(byte[] pdu, int ofst, int pduSize, SharedInt len)
	{
		if (ofst >= pduSize)
		{
			len.value = 0;
			return pduSize + 1;
		}
		if ((pdu[ofst] & 0x80) != 0)
		{
			if ((pdu[ofst] & 0xff) == 0x81)
			{
				if (ofst + 2 > pduSize)
				{
					len.value = 0;
					return pduSize + 1;
				}
				len.value = pdu[ofst + 1] & 0xff;
				return ofst + 2;
			}
			else if ((pdu[ofst] & 0xff) == 0x82)
			{
				if (ofst + 3 > pduSize)
				{
					len.value = 0;
					return pduSize + 1;
				}
				len.value = ByteTool.readMUInt16(pdu, ofst + 1);
				return ofst + 3;
			}
			else if ((pdu[ofst] & 0xff) == 0x83)
			{
				if (ofst + 4 > pduSize)
				{
					len.value = 0;
					return pduSize + 1;
				}
				len.value = ByteTool.readMUInt24(pdu, ofst + 1);
				return ofst + 4;
			}
			else if (pdu[ofst] == 0x84)
			{
				if (ofst + 5 > pduSize)
				{
					len.value = 0;
					return pduSize + 1;
				}
				len.value = ByteTool.readMInt32(pdu, ofst + 1);
				return ofst + 5;
			}
			len.value = 0;
			return pduSize + 1;
		}
		else
		{
			len.value = pdu[ofst] & 0xff;
			return ofst + 1;
		}
	}
	
	public static int pudParseSeq(byte[] pdu, int pduOfst, int pduEndOfst, SharedByte type, SharedInt seqEndOfst)
	{
		int len;
		if (pduEndOfst - pduOfst < 2)
			return 0;
		type.value = pdu[pduOfst + 0];
		if ((pdu[pduOfst + 1] & 0xff) < 0x80)
		{
			len = pdu[pduOfst + 1];
			pduOfst += 2;
		}
		else if ((pdu[pduOfst + 1] & 0xff) == 0x81 && pduEndOfst - pduOfst >= 3)
		{
			len = pdu[pduOfst + 2] & 0xff;
			pduOfst += 3;
		}
		else if ((pdu[pduOfst + 1] & 0xff) == 0x82 && pduEndOfst - pduOfst >= 4)
		{
			len = ByteTool.readMUInt16(pdu, pduOfst + 2);
			pduOfst += 4;
		}
		else if ((pdu[pduOfst + 1] & 0xff) == 0x83 && pduEndOfst - pduOfst >= 5)
		{
			len = ByteTool.readMUInt24(pdu, pduOfst + 2);
			pduOfst += 5;
		}
		else if ((pdu[pduOfst + 1] & 0xff) == 0x84 && pduEndOfst - pduOfst >= 6)
		{
			len = ByteTool.readMInt32(pdu, pduOfst + 2);
			pduOfst += 6;
		}
		else
		{
			return 0;
		}
		if (pduOfst + len > pduEndOfst)
		{
			return 0;
		}
		seqEndOfst.value = pduOfst + len;
		return pduOfst;
	}
	
	public static int pduParseUInt32(byte[] pdu, int pduOfst, int pduEndOfst, SharedInt val)
	{
		if (pduEndOfst - pduOfst < 3)
			return 0;
		if (pdu[pduOfst + 0] != 2)
			return 0;
		if (pdu[pduOfst + 1] == 1)
		{
			val.value = pdu[pduOfst + 2] & 0xff;
			return pduOfst + 3;
		}
		else if (pdu[pduOfst + 1] == 2 && pduEndOfst - pduOfst >= 4)
		{
			val.value = ByteTool.readMUInt16(pdu, pduOfst + 2);
			return pduOfst + 4;
		}
		else if (pdu[pduOfst + 1] == 3 && pduEndOfst - pduOfst >= 5)
		{
			val.value = ByteTool.readMUInt24(pdu, pduOfst + 2);
			return pduOfst + 5;
		}
		else if (pdu[pduOfst + 1] == 4 && pduEndOfst - pduOfst >= 6)
		{
			val.value = ByteTool.readMInt32(pdu, pduOfst + 2);
			return pduOfst + 6;
		}
		else
		{
			return 0;
		}
	}
	
	public static int pduParseString(byte[] pdu, int pduOfst, int pduEndOfst, StringBuilder sb)
	{
		int len;
		if (pduEndOfst - pduOfst < 2)
			return 0;
		if (pdu[pduOfst + 0] != 4)
			return 0;
		if ((pdu[pduOfst + 1] & 0xff) < 0x80)
		{
			len = pdu[pduOfst + 1];
			pduOfst += 2;
		}
		else if ((pdu[pduOfst + 1] & 0xff) == 0x81 && pduEndOfst - pduOfst >= 3)
		{
			len = pdu[pduOfst + 2] & 0xff;
			pduOfst += 3;
		}
		else if ((pdu[pduOfst + 1] & 0xff) == 0x82 && pduEndOfst - pduOfst >= 4)
		{
			len = ByteTool.readMUInt16(pdu, pduOfst + 2);
			pduOfst += 4;
		}
		else if ((pdu[pduOfst + 1] & 0xff) == 0x83 && pduEndOfst - pduOfst >= 5)
		{
			len = ByteTool.readMUInt24(pdu, pduOfst + 2);
			pduOfst += 5;
		}
		else if ((pdu[pduOfst + 1] & 0xff) == 0x84 && pduEndOfst - pduOfst >= 6)
		{
			len = ByteTool.readMInt32(pdu, pduOfst + 2);
			pduOfst += 6;
		}
		else
		{
			return 0;
		}
		if (pduOfst + len > pduEndOfst)
		{
			return 0;
		}
		sb.append(new String(pdu, pduOfst, len, StandardCharsets.UTF_8));
		return pduOfst + len;
	}
	
	public static int pduParseChoice(byte[] pdu, int pduOfst, int pduEndOfst, SharedInt val)
	{
		if (pduEndOfst - pduOfst < 3)
			return 0;
		if (pdu[pduOfst + 0] != 10)
			return 0;
		if (pdu[pduOfst + 1] == 1)
		{
			val.value = pdu[pduOfst + 2] & 0xff;
			return pduOfst + 3;
		}
		else if (pdu[pduOfst + 1] == 2 && pduEndOfst - pduOfst >= 4)
		{
			val.value = ByteTool.readMUInt16(pdu, pduOfst + 2);
			return pduOfst + 4;
		}
		else if (pdu[pduOfst + 1] == 3 && pduEndOfst - pduOfst >= 5)
		{
			val.value = ByteTool.readMUInt24(pdu, pduOfst + 2);
			return pduOfst + 5;
		}
		else if (pdu[pduOfst + 1] == 4 && pduEndOfst - pduOfst >= 6)
		{
			val.value = ByteTool.readMInt32(pdu, pduOfst + 2);
			return pduOfst + 6;
		}
		else
		{
			return 0;
		}
	}
}
