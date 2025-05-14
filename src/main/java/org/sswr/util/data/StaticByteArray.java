package org.sswr.util.data;

import jakarta.annotation.Nonnull;

public class StaticByteArray implements ByteArray {
	@Nonnull
	private byte[] buff;
	private int ofst;
	private int len;

	public StaticByteArray(@Nonnull byte[] buff)
	{
		this.buff = buff;
		this.ofst = 0;
		this.len = buff.length;
	}

	public StaticByteArray(@Nonnull byte[] buff, int ofst, int len)
	{
		this.buff = buff;
		this.ofst = ofst;
		this.len = len;
	}

	@Override
	@Nonnull
	public byte[] getBytes() {
		return this.buff;
	}

	@Override
	public int getBytesOffset() {
		return this.ofst;
	}

	@Override
	public int getBytesLength() {
		return this.len;
	}
}
