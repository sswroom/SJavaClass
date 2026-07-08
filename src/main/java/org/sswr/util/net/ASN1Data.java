package org.sswr.util.net;

import java.util.Arrays;

import org.sswr.util.data.ByteTool;
import org.sswr.util.data.LineBreakType;
import org.sswr.util.data.StringBuilderUTF8;
import org.sswr.util.data.StringUtil;
import org.sswr.util.io.ParsedObject;
import org.sswr.util.io.ParserType;

import jakarta.annotation.Nonnull;

public abstract class ASN1Data extends ParsedObject
{
	@Nonnull
	protected byte[] buff;

	protected ASN1Data(@Nonnull String sourceName, @Nonnull byte[] buff, int ofst, int size)
	{
		super(sourceName);
		byte[] newBuff = Arrays.copyOfRange(buff, ofst, ofst + size);
		if (newBuff == null) throw new IllegalArgumentException("Buff is null");
		this.buff = newBuff;
	}

	@Nonnull
	public ParserType getParserType()
	{
		return ParserType.ASN1Data;
	}
	
	@Nonnull
	public abstract ASN1Type getASN1Type();
	@Nonnull
	public abstract ASN1Data clone();
	@Nonnull
	public abstract String toString();

	public boolean toASN1String(@Nonnull StringBuilderUTF8 sb)
	{
		return ASN1Util.pduToString(this.buff, 0, this.buff.length, sb, 0);
	}

	@Nonnull
	public byte[] getASN1Buff()
	{
		return this.buff;
	}

	public int getASN1BuffSize()
	{
		return this.buff.length;
	}

	public static void appendInteger(@Nonnull StringBuilderUTF8 sb, @Nonnull byte[] pdu, int ofst, int len)
	{
		if (len == 1)
		{
			sb.appendI32(pdu[ofst] & 255);
		}
		else if (len == 2)
		{
			sb.appendI32(ByteTool.readMInt16(pdu, ofst));
		}
		else if (len == 3)
		{
			sb.appendI32(ByteTool.readMInt24(pdu, ofst));
		}
		else if (len == 4)
		{
			sb.appendI32(ByteTool.readMInt32(pdu, ofst));
		}
		else if (len == 8)
		{
			sb.appendI64(ByteTool.readMInt64(pdu, ofst));
		}
		else
		{
			StringUtil.appendHex(sb, pdu, ofst, len, ' ', LineBreakType.NONE);
		}
	}
}
