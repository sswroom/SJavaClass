package org.sswr.util.crypto;

import jakarta.annotation.Nonnull;

public abstract class Encryption
{
	@Nonnull 
	public abstract byte []encrypt(@Nonnull byte inBuff[], int inOfst, int inSize) throws EncryptionException;
	@Nonnull 
	public abstract byte []decrypt(@Nonnull byte inBuff[], int inOfst, int inSize) throws EncryptionException;
	@Nonnull
	public byte[] encrypt(@Nonnull byte[] inBuff) throws EncryptionException
	{
		return encrypt(inBuff, 0, inBuff.length);
	}

	@Nonnull
	public byte[] decrypt(@Nonnull byte[] inBuff) throws EncryptionException
	{
		return decrypt(inBuff, 0, inBuff.length);
	}

	public abstract int getEncBlockSize();
	public abstract int getDecBlockSize();
}
