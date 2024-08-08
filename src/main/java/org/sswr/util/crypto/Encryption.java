package org.sswr.util.crypto;

public abstract class Encryption
{
	public abstract byte []encrypt(byte inBuff[], int inOfst, int inSize);
	public abstract byte []decrypt(byte inBuff[], int inOfst, int inSize);
	public byte[] encrypt(byte[] inBuff)
	{
		return encrypt(inBuff, 0, inBuff.length);
	}

	public byte[] decrypt(byte[] inBuff)
	{
		return decrypt(inBuff, 0, inBuff.length);
	}

	public abstract int getEncBlockSize();
	public abstract int getDecBlockSize();
}
