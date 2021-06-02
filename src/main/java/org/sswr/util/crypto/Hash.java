package org.sswr.util.crypto;

public interface Hash
{
	public String getName();
	public Hash clone();
	public void clear();
	public void calc(byte buff[], int ofst, int buffSize);
	public byte []getValue();
	public int getBlockSize();
	public int getResultSize();
}
