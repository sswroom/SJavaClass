package org.sswr.util.data;

import java.nio.charset.StandardCharsets;

import jakarta.annotation.Nonnull;

public class StringBuilderUTF8 {
	private @Nonnull byte[] v;
	private int leng;
	private int buffSize;

	public StringBuilderUTF8()
	{
		this.buffSize = 1024;
		this.v = new byte[this.buffSize + 1];
		this.leng = 0;
		this.v[0] = 0;
	}

	public void allocLeng(int leng)
	{
		if (leng + this.leng > this.buffSize)
		{
			this.buffSize <<= 1;
			while (leng + this.leng > this.buffSize)
			{
				this.buffSize <<= 1;
			}
			byte[] newStr = new byte[this.buffSize + 1];
			ByteTool.copyArray(newStr, 0, this.v, 0, this.leng + 1);
			this.v = newStr;
		}
	}

	public StringBuilderUTF8 appendUTF8Char(byte c)
	{
		this.allocLeng(1);
		this.v[this.leng] = c;
		this.v[this.leng + 1] = 0;
		this.leng++;
		return this;
	}

	public StringBuilderUTF8 appendC(byte[] v, int ofst, int len)
	{
		this.allocLeng(1);
		ByteTool.copyArray(this.v, this.leng, v, ofst, len);
		this.leng += len;
		this.v[this.leng] = 0;
		return this;
	}

	public StringBuilderUTF8 clearStr()
	{
		this.leng = 0;
		this.v[0] = 0;
		return this;
	}

	public byte[] getBytes()
	{
		return this.v;
	}

	public String toString()
	{
		return new String(this.v, 0, this.leng, StandardCharsets.UTF_8);
	}

	public int getLength()
	{
		return this.leng;
	}

	public int getCharCnt()
	{
		return this.leng;
	}
}
