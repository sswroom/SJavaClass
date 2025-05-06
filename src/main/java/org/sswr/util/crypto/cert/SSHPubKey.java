package org.sswr.util.crypto.cert;

import java.nio.charset.StandardCharsets;

import org.sswr.util.data.ByteTool;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class SSHPubKey {
	private String sourceNameObj;
	private byte[] buff;

	public SSHPubKey(@Nonnull String sourceName, @Nonnull byte[] buff, int buffOfst, int buffSize)
	{
		this.sourceNameObj = sourceName;
		this.buff = ByteTool.subArray(buff, buffOfst, buffSize);
	}

	@Nonnull
	public byte[] getArr()
	{
		return this.buff;
	}
	
	public int getSize()
	{
		return this.buff.length;
	}

	@Nullable
	public byte[] getRSAModulus()
	{
		if (this.buff.length < 15)
			return null;
		int i;
		int buffSize;
		if (ByteTool.readMInt32(this.buff, 0) != 7 || !new String(this.buff, 4, 7, StandardCharsets.UTF_8).equals("ssh-rsa"))
			return null;
		buffSize = ByteTool.readMInt32(this.buff, 11);
		i = 15;
		if (this.buff.length < i + buffSize + 4)
			return null;
		i += buffSize + 4;
		buffSize = ByteTool.readMInt32(this.buff, i - 4);
		if (this.buff.length < i + buffSize)
			return null;
		return ByteTool.subArray(this.buff, i, buffSize);
	}

	@Nullable
	public byte[] getRSAPublicExponent()
	{
		if (this.buff.length < 15)
			return null;
		int i;
		int buffSize;
		if (ByteTool.readMInt32(this.buff, 0) != 7 || !new String(this.buff, 4, 7, StandardCharsets.UTF_8).equals("ssh-rsa"))
			return null;
		buffSize = ByteTool.readMInt32(this.buff, 11);
		i = 15;
		if (this.buff.length < i + buffSize + 4)
			return null;
		return ByteTool.subArray(this.buff, i, buffSize);
	}

	@Nullable
	public MyX509Key createKey()
	{
		byte[] modulus;
		byte[] publicExponent;
		if ((modulus = getRSAModulus()) != null && (publicExponent = getRSAPublicExponent()) != null)
		{
			return MyX509Key.createRSAPublicKey(this.sourceNameObj, modulus, 0, modulus.length, publicExponent, 0, publicExponent.length);
		}
		return null;
	}

	@Nonnull
	public static SSHPubKey createRSAPublicKey(@Nonnull String name, @Nonnull byte[] modulus, int modulusOfst, int modulusSize, @Nonnull byte[] publicExponent, int publicExponentOfst, int publicExponentSize)
	{
		byte[] header = {0x00, 0x00, 0x00, 0x07, 0x73, 0x73, 0x68, 0x2D, 0x72, 0x73, 0x61};
		byte[] buff = new byte[modulusSize + publicExponentSize + 19];
		int i;
		ByteTool.copyArray(buff, 0, header, 0, header.length);
		i = header.length;
		ByteTool.writeMInt32(buff, i, publicExponentSize);
		ByteTool.copyArray(buff, i + 4, publicExponent, publicExponentOfst, publicExponentSize);
		i += 4 + publicExponentSize;
		ByteTool.writeMInt32(buff, i, modulusSize);
		ByteTool.copyArray(buff, i + 4, modulus, modulusOfst, modulusSize);
		i += 4 + modulusSize;
		SSHPubKey key = new SSHPubKey(name, buff, 0, i);
		return key;
	}

	public static boolean isValid(@Nonnull byte[] buff, int buffOfst, int buffSize)
	{
		byte[] header1 = {0x00, 0x00, 0x00, 0x07, 0x73, 0x73, 0x68, 0x2D, 0x72, 0x73, 0x61, 0x00, 0x00, 0x00};
		if (buffSize >= header1.length && ByteTool.byteEquals(buff, buffOfst, header1, 0, header1.length)) return true;
		return false;
	}
}
