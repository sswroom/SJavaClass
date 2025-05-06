package org.sswr.util.crypto.encrypt;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.sswr.util.data.ByteTool;

import jakarta.annotation.Nonnull;

public class AES256GCM extends Encryption
{
	private byte[] key;
	private byte[] iv;
	public AES256GCM(@Nonnull byte[] key, int keyOfst, int keyLeng, @Nonnull byte[] iv, int ivOfst)
	{
		if (keyLeng != 32)
		{
			throw new IllegalArgumentException("keyLeng must be 32");
		}
		this.key = new byte[keyLeng];
		ByteTool.copyArray(this.key, 0, key, keyOfst, keyLeng);
		this.iv = new byte[12];
		ByteTool.copyArray(this.iv, 0, iv, ivOfst, 12);
	}

	@Override
	@Nonnull 
	public byte[] encrypt(@Nonnull byte[] inBuff, int inOfst, int inSize) throws EncryptionException {
		SecretKeySpec skeySpec = new SecretKeySpec(this.key, "AES");
		try
		{
			Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
			GCMParameterSpec parameterSpec = new GCMParameterSpec(128, this.iv); 
			cipher.init(Cipher.ENCRYPT_MODE, skeySpec, parameterSpec);
			return cipher.doFinal(inBuff, inOfst, inSize);
		}
		catch (Exception ex)
		{
			throw new EncryptionException(ex);
		}
	}

	@Override
	@Nonnull
	public byte[] decrypt(@Nonnull byte[] inBuff, int inOfst, int inSize) throws EncryptionException {
		SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
		try
		{
			Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
			GCMParameterSpec parameterSpec = new GCMParameterSpec(128, iv); 
			cipher.init(Cipher.DECRYPT_MODE, skeySpec, parameterSpec);
			return cipher.doFinal(inBuff, inOfst, inSize);
		}
		catch (Exception ex)
		{
			throw new EncryptionException(ex);
		}
	}

	@Override
	public int getEncBlockSize() {
		return 16;
	}

	@Override
	public int getDecBlockSize() {
		return 16;
	}

	public void setIV(@Nonnull byte[] iv, int ivOfst)
	{
		ByteTool.copyArray(this.iv, 0, iv, ivOfst, 12);
	}
}
