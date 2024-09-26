package org.sswr.util.crypto;

import java.nio.charset.StandardCharsets;

import org.sswr.util.crypto.BlockCipher.ChainMode;
import org.sswr.util.data.ByteTool;
import org.sswr.util.data.RandomMT19937;
import org.sswr.util.data.textbinenc.Base64Enc;
import org.sswr.util.data.textbinenc.EncodingException;

import jakarta.annotation.Nonnull;

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
	private byte salt[];
	private int saltSize;
	private byte iv[];
	private int ivSize;
	private int iterCnt;
	private int dkLen;
	private RandomMT19937 random;

	public JasyptEncryptor(@Nonnull KeyAlgorithm keyAlg, @Nonnull CipherAlgorithm cipherAlg, @Nonnull String key)
	{
		this.keyAlgorithmn = keyAlg;
		this.cipherAlgorithm = cipherAlg;
		switch (this.cipherAlgorithm)
		{
		case AES256:
			this.saltSize = 16;
			this.ivSize = 16;
			this.iterCnt = 1000;
			this.dkLen = 32;
			break;
		default:
			this.saltSize = 16;
			this.ivSize = 16;
			this.iterCnt = 1000;
			this.dkLen = 32;
			break;
		}
		this.salt = null;
		this.iv = null;
		this.random = null;
		this.key = key;
	}

	@Nonnull
	private byte []decGetSalt(@Nonnull byte salt[], @Nonnull byte buff[])
	{
		if (this.salt != null)
		{
			ByteTool.copyArray(salt, 0, this.salt, 0, this.saltSize);
			return buff;
		}
		else
		{
			ByteTool.copyArray(salt, 0, buff, 0, this.saltSize);
			byte outBuff[] = new byte[buff.length - this.saltSize];
			ByteTool.copyArray(outBuff, 0, buff, this.saltSize, outBuff.length);
			return outBuff;
		}
	}

	@Nonnull
	private byte []decGetIV(@Nonnull byte iv[], @Nonnull byte buff[])
	{
		if (this.iv != null)
		{
			ByteTool.copyArray(iv, 0, this.iv, 0, this.ivSize);
			return buff;
		}
		else
		{
			ByteTool.copyArray(iv, 0, buff, 0, this.ivSize);
			byte outBuff[] = new byte[buff.length - this.ivSize];
			ByteTool.copyArray(outBuff, 0, buff, this.ivSize, outBuff.length);
			return outBuff;
		}
	}

	@Nonnull
	private byte []getEncKey(@Nonnull byte salt[])
	{
		switch (this.keyAlgorithmn)
		{
		case PBEWITHHMACSHA512:
			byte []keyBuff = key.getBytes(StandardCharsets.UTF_8);
			HMAC hmac = new HMAC(new SHA512(), keyBuff, 0, keyBuff.length);
			return PBKDF2.pbkdf2(salt, this.iterCnt, this.dkLen, hmac);
		}
		return salt;
	}

	@Nonnull
	private Encryption getEnc(@Nonnull byte iv[], @Nonnull byte keyBuff[])
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

	private void genRandomBytes(@Nonnull byte buff[], int ofst, int len)
	{
		if (this.random == null)
		{
			this.random = new RandomMT19937((int)System.currentTimeMillis());
		}
		byte tmpBuff[];
		while (len >= 4)
		{
			ByteTool.writeMInt32(buff, ofst, this.random.nextInt32());
			len -= 4;
			ofst += 4;
		}
		if (len > 0)
		{
			tmpBuff = new byte[4];
			ByteTool.writeMInt32(tmpBuff, 0, this.random.nextInt32());
			while (len-- > 0)
			{
				buff[ofst + len] = tmpBuff[len];
			}
		}
	}

	@Nonnull
	public byte []decrypt(@Nonnull String b64String) throws EncryptionException
	{
		return decrypt(new Base64Enc().decodeBin(b64String));
	}

	@Nonnull
	public String decryptToString(@Nonnull String b64String) throws EncryptionException
	{
		return getString(decrypt(b64String));
	}

	@Nonnull 
	public byte []decrypt(@Nonnull byte []buff) throws EncryptionException
	{
		byte salt[] = new byte[this.saltSize];
		byte iv[] = new byte[this.ivSize];
		buff = decGetSalt(salt, buff);
		buff = decGetIV(iv, buff);
		byte keyBuff[] = getEncKey(salt);
		Encryption enc = getEnc(iv, keyBuff);
		byte[] paddedResult = enc.decrypt(buff, 0, buff.length);
		if (paddedResult.length > 0)
		{
			byte tail = paddedResult[paddedResult.length - 1];
			if ((tail & 0xff) > 0 && (tail & 0xff) <= enc.getDecBlockSize())
			{
				int i = paddedResult.length - 1;
				while (i > 0)
				{
					if (paddedResult[i - 1] >= 32)
						break;
					i--;
				}
				byte[] unpaddedResult = new byte[i];
				ByteTool.copyArray(unpaddedResult, 0, paddedResult, 0, i);
				return unpaddedResult;
			}
		}
		return paddedResult;
	}

	@Nonnull
	public String getString(@Nonnull byte[] decBuff)
	{
		int i = decBuff.length;
		while (i-- > 0)
		{
			if (decBuff[i] >= 32)
			{
				break;
			}
		}
		return new String(decBuff, 0, i + 1, StandardCharsets.UTF_8);
	}

	@Nonnull
	public String encryptAsB64(@Nonnull byte srcBuff[]) throws EncryptionException, EncodingException
	{
		return encryptAsB64(srcBuff, 0, srcBuff.length);
	}

	@Nonnull
	public String encryptAsB64(@Nonnull byte srcBuff[], int srcOfst, int srcLen) throws EncryptionException, EncodingException
	{
		byte srcTmpBuff[];
		int nBlock = srcLen / this.ivSize;
		int destLen;
		if (nBlock * this.ivSize != srcLen)
		{
			destLen = (nBlock + 1) * this.ivSize;
			srcTmpBuff = new byte[destLen];
			ByteTool.copyArray(srcTmpBuff, 0, srcBuff, 0, srcLen);
			ByteTool.arrayFill(srcTmpBuff, srcLen, destLen - srcLen, (byte)(destLen - srcLen));
			srcBuff = srcTmpBuff;
		}
		else
		{
			destLen = nBlock * this.ivSize;		
		}
		if (this.salt == null)
		{
			destLen += this.saltSize;
		}
		if (this.iv == null)
		{
			destLen += this.ivSize;
		}
		int destOfst = 0;
		byte destBuff[] = new byte[destLen];
		byte salt[];
		byte iv[];
		if (this.salt != null)
		{
			salt = this.salt;
		}
		else
		{
			this.genRandomBytes(destBuff, destOfst, this.saltSize);
			salt = new byte[this.saltSize];
			ByteTool.copyArray(salt, 0, destBuff, destOfst, this.saltSize);
			destOfst += this.saltSize;
		}
		if (this.iv != null)
		{
			iv = this.iv;
		}
		else
		{
			this.genRandomBytes(destBuff, destOfst, this.ivSize);
			iv = new byte[this.ivSize];
			ByteTool.copyArray(iv, 0, destBuff, destOfst, this.ivSize);
			destOfst += this.ivSize;
		}
		byte key[] = this.getEncKey(salt);
		Encryption enc = this.getEnc(iv, key);
		byte encBuff[] = enc.encrypt(srcBuff, 0, destLen - destOfst);
		ByteTool.copyArray(destBuff, destOfst, encBuff, 0, encBuff.length);
	
		return new Base64Enc().encodeBin(destBuff);
	}
}
