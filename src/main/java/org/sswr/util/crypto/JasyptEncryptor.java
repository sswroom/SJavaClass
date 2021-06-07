package org.sswr.util.crypto;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.sswr.util.crypto.BlockCipher.ChainMode;
import org.sswr.util.data.ByteTool;

public class JasyptEncryptor
{
	public enum KeyAlgorithm
	{
		PBEWITHHMACSHA512
	}

	public enum CipherAlgorithm
	{
		AES256
	}

	private KeyAlgorithm keyAlgorithmn;
	private CipherAlgorithm cipherAlgorithm;
	private String key;
	private int saltSize;
	private int ivSize;

	public JasyptEncryptor(KeyAlgorithm keyAlg, CipherAlgorithm cipherAlg, String key)
	{
		this.keyAlgorithmn = keyAlg;
		this.cipherAlgorithm = cipherAlg;
		switch (this.cipherAlgorithm)
		{
		case AES256:
			this.saltSize = 16;
			this.ivSize = 16;
			break;
		default:
			this.saltSize = 16;
			this.ivSize = 16;
			break;
		}
		this.key = key;
	}

	private byte []decGetSalt(byte salt[], byte buff[])
	{
		ByteTool.copyArray(salt, 0, buff, 0, this.saltSize);
		byte outBuff[] = new byte[buff.length - this.saltSize];
		ByteTool.copyArray(outBuff, 0, buff, this.saltSize, outBuff.length);
		return outBuff;
	}

	private byte []decGetIV(byte salt[], byte buff[])
	{
		ByteTool.copyArray(salt, 0, buff, 0, this.saltSize);
		byte outBuff[] = new byte[buff.length - this.saltSize];
		ByteTool.copyArray(outBuff, 0, buff, this.saltSize, outBuff.length);
		return outBuff;
	}

	private byte []getEncKey(byte salt[])
	{
		switch (this.keyAlgorithmn)
		{
		case PBEWITHHMACSHA512:
			byte []keyBuff = key.getBytes(StandardCharsets.UTF_8);
			HMAC hmac = new HMAC(new SHA512(), keyBuff, 0, keyBuff.length);
			return PBKDF2.pbkdf2(salt, 1000, 32, hmac);
		}
		return salt;
	}

	private Encryption getEnc(byte iv[], byte keyBuff[])
	{
		switch (this.cipherAlgorithm)
		{
		case AES256:
		default:
			AES256 aes256 = new AES256(keyBuff);
			aes256.setChainMode(ChainMode.CBC);
			aes256.setIV(iv);
			return aes256;
		}
	}

	public byte []decrypt(String b64String)
	{
		return decrypt(Base64.getDecoder().decode(b64String));
	}

	public String decryptToString(String b64String)
	{
		return getString(decrypt(b64String));
	}

	public byte []decrypt(byte []buff)
	{
		byte salt[] = new byte[this.saltSize];
		byte iv[] = new byte[this.ivSize];
		buff = decGetSalt(salt, buff);
		buff = decGetIV(iv, buff);
		byte keyBuff[] = getEncKey(salt);
		Encryption enc = getEnc(iv, keyBuff);
		return enc.decrypt(buff, 0, buff.length, null);
	}

	public String getString(byte[] decBuff)
	{
		int i = decBuff.length;
		while (i-- > 0)
		{
			if (decBuff[i] != 8)
			{
				break;
			}
		}
		return new String(decBuff, 0, i + 1, StandardCharsets.UTF_8);
	}
}
