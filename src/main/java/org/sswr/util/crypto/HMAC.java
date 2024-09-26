package org.sswr.util.crypto;

import org.sswr.util.data.ByteTool;

import jakarta.annotation.Nonnull;

public class HMAC extends Hash
{
	private Hash hashInner;
	private Hash hashOuter;
	private byte key[];
	private int keySize;
	private int padSize;
	private byte iPad[];
	private byte oPad[];

	public HMAC(@Nonnull Hash hash, @Nonnull byte key[], int index, int keySize)
	{
		this.hashInner = hash.clone();
		this.hashOuter = hash.clone();
		this.key = new byte[keySize];
		this.keySize = keySize;
		ByteTool.copyArray(this.key, 0, key, index, keySize);
	
		this.padSize = hash.getBlockSize();
		if (this.padSize < keySize)
			this.padSize = keySize;
		this.iPad = new byte[this.padSize];
		this.oPad = new byte[this.padSize];
		int i = this.padSize;
		while (i-- > 0)
		{
			this.iPad[i] = 0x36;
			this.oPad[i] = 0x5c;
		}
		
		i = keySize;
		while (i-- > 0)
		{
			byte b = key[i];
			this.iPad[i] ^= b;
			this.oPad[i] ^= b;
		}
	
		this.clear();
	}

	@Nonnull
	public String getName()
	{
		return "HMAC-" + this.hashInner.getName();
	}

	@Nonnull
	public Hash clone()
	{
		return new HMAC(this.hashInner, this.key, 0, this.keySize);
	}

	public void clear()
	{
		this.hashInner.clear();
		this.hashInner.calc(this.iPad, 0, this.padSize);
	}

	public void calc(@Nonnull byte buff[], int index, int buffSize)
	{
		this.hashInner.calc(buff, index, buffSize);
	}

	@Nonnull
	public byte []getValue()
	{
		byte buff[] = this.hashInner.getValue();
		this.hashOuter.clear();
		this.hashOuter.calc(this.oPad, 0, this.padSize);
		this.hashOuter.calc(buff, 0, buff.length);
		return this.hashOuter.getValue();
	}
	
	public int getBlockSize()
	{
		return this.hashInner.getBlockSize();
	}
	
	public int getResultSize()
	{
		return this.hashInner.getResultSize();
	}
}
