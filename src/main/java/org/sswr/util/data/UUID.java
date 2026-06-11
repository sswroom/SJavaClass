package org.sswr.util.data;

import java.util.Arrays;

import jakarta.annotation.Nonnull;

public class UUID
{
	@Nonnull
	private byte[] uuidBuff;

	public UUID(@Nonnull byte[] buff, int ofst)
	{
		byte[] newBuff = Arrays.copyOfRange(buff, ofst, ofst + 16);
		if (newBuff == null) throw new IllegalArgumentException("Buff is null");
		this.uuidBuff = newBuff;
	}

	public void toString(@Nonnull StringBuilderUTF8 sb)
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

	public void getValue(@Nonnull byte[] buff, int ofst)
	{
		ByteTool.copyArray(buff, ofst, this.uuidBuff, 0, 16);
	}

	@Nonnull
	public String toString()
	{
		StringBuilderUTF8 sb = new StringBuilderUTF8();
		toString(sb);
		return sb.toString();
	}
}
