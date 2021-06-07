package org.sswr.util.crypto;

public interface Encryption
{
	public byte []encrypt(byte inBuff[], int inOfst, int inSize, Object encParam);
	public byte []decrypt(byte inBuff[], int inOfst, int inSize, Object decParam);

	public int getEncBlockSize();
	public int getDecBlockSize();
}
