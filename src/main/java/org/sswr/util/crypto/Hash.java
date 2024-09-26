package org.sswr.util.crypto;

import jakarta.annotation.Nonnull;

public abstract class Hash
{
	@Nonnull
	public abstract String getName();
	@Nonnull
	public abstract Hash clone();
	public abstract void clear();
	public abstract void calc(@Nonnull byte[] buff, int ofst, int buffSize);
	public void calc(@Nonnull byte[] buff)
	{
		calc(buff, 0, buff.length);
	}
	@Nonnull
	public abstract byte []getValue();
	public abstract int getBlockSize();
	public abstract int getResultSize();
}
