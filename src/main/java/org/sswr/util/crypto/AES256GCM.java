package org.sswr.util.crypto;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.sswr.util.data.ByteTool;

public class AES256GCM extends Encryption
{
	private byte[] key;
	private byte[] iv;
	public AES256GCM(byte[] key, int keyOfst, int keyLeng, byte[] iv, int ivOfst)
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
	public byte[] encrypt(byte[] inBuff, int inOfst, int inSize) {
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
			ex.printStackTrace();
			return null;
		}
	}

	@Override
	public byte[] decrypt(byte[] inBuff, int inOfst, int inSize) {
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
			ex.printStackTrace();
			return null;
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

	public void setIV(byte[] iv, int ivOfst)
	{
		ByteTool.copyArray(this.iv, 0, iv, ivOfst, 12);
	}
}
