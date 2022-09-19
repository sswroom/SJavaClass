package org.sswr.util.net;

import java.util.Arrays;

import org.sswr.util.data.ByteTool;
import org.sswr.util.data.LineBreakType;
import org.sswr.util.data.StringUtil;
import org.sswr.util.io.ParsedObject;

public abstract class ASN1Data extends ParsedObject
{
	protected byte[] buff;

	protected ASN1Data(String sourceName, byte[] buff, int ofst, int size)
	{
		super(sourceName);
		this.buff = Arrays.copyOfRange(buff, ofst, ofst + size);
	}

	public abstract ASN1Type getASN1Type();
	public abstract ASN1Data clone();
	public abstract String toString();

	public boolean toASN1String(StringBuilder sb)
	{
		return ASN1Util.pduToString(this.buff, 0, this.buff.length, sb, 0);
	}

	public byte[] getASN1Buff()
	{
		return this.buff;
	}

	public int getASN1BuffSize()
	{
		return this.buff.length;
	}

	public static void appendInteger(StringBuilder sb, byte[] pdu, int ofst, int len)
	{
		if (len == 1)
		{
			sb.append(pdu[ofst] & 255);
		}
		else if (len == 2)
		{
			sb.append(ByteTool.readMInt16(pdu, ofst));
		}
		else if (len == 3)
		{
			sb.append(ByteTool.readMInt24(pdu, ofst));
		}
		else if (len == 4)
		{
			sb.append(ByteTool.readMInt32(pdu, ofst));
		}
		else if (len == 8)
		{
			sb.append(ByteTool.readMInt64(pdu, ofst));
		}
		else
		{
			StringUtil.appendHex(sb, pdu, ofst, len, ' ', LineBreakType.NONE);
		}
	}
}
