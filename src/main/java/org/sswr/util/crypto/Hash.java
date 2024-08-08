package org.sswr.util.crypto;

public abstract class Hash
{
	public abstract String getName();
	public abstract Hash clone();
	public abstract void clear();
	public abstract void calc(byte[] buff, int ofst, int buffSize);
	public void calc(byte[] buff)
	{
		calc(buff, 0, buff.length);
	}
	public abstract byte []getValue();
	public abstract int getBlockSize();
	public abstract int getResultSize();
}
