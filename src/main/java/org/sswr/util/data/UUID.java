package org.sswr.util.data;

import java.util.Arrays;

public class UUID
{
	private byte[] uuidBuff;

	public UUID(byte[] buff, int ofst)
	{
		this.uuidBuff = Arrays.copyOfRange(buff, ofst, ofst + 16);
	}

	public void toString(StringBuilder sb)
	{
		sb.append(StringUtil.toHex32(ByteTool.readInt32(this.uuidBuff, 0)));
		sb.append("-");
		sb.append(StringUtil.toHex16(ByteTool.readInt16(this.uuidBuff, 4)));
		sb.append("-");
		sb.append(StringUtil.toHex16(ByteTool.readInt16(this.uuidBuff, 6)));
		sb.append("-");
		sb.append(StringUtil.toHex16(ByteTool.readInt16(this.uuidBuff, 8)));
		sb.append("-");
		StringUtil.appendHex(sb, this.uuidBuff, 10, 6, (char)0, LineBreakType.NONE);
	}

	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		toString(sb);
		return sb.toString();
	}
}
