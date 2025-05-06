package org.sswr.util.data;

import java.nio.charset.StandardCharsets;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

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

	public StringBuilderUTF8 append(@Nonnull String s)
	{
		byte[] sbuff = s.getBytes(StandardCharsets.UTF_8);
		this.allocLeng(sbuff.length);
		ByteTool.copyArray(this.v, this.leng, sbuff, 0, sbuff.length);
		this.leng += sbuff.length;
		this.v[this.leng] = 0;
		return this;
	}

	public StringBuilderUTF8 appendOpt(@Nullable String s)
	{
		if (s == null)
			return this;
		return append(s);
	}

	public StringBuilderUTF8 appendUTF8Char(byte c)
	{
		this.allocLeng(1);
		this.v[this.leng] = c;
		this.v[this.leng + 1] = 0;
		this.leng++;
		return this;
	}

	public StringBuilderUTF8 appendChar(int c, int repCnt)
	{
		byte[] oc = new byte[6];
		int buffEnd;
		if (c < 0x80)
		{
			this.allocLeng(repCnt);
			buffEnd = this.leng;
			byte b = (byte)c;
			while (repCnt-- > 0)
			{
				this.v[buffEnd + 0] = b;
				buffEnd += 1;
			}
		}
		else if (c < 0x800)
		{
			this.allocLeng(2 * repCnt);
			buffEnd = this.leng;
			oc[0] = (byte)(0xc0 | (c >> 6));
			oc[1] = (byte)(0x80 | (c & 0x3f));
			int b = ByteTool.readUInt16(oc, 0);
			while (repCnt-- > 0)
			{
				ByteTool.writeInt16(this.v, buffEnd, b);
				buffEnd += 2;
			}
		}
		else if (c < 0x10000)
		{
			this.allocLeng(3 * repCnt);
			buffEnd = this.leng;
			oc[0] = (byte)(0xe0 | (c >> 12));
			oc[1] = (byte)(0x80 | ((c >> 6) & 0x3f));
			oc[2] = (byte)(0x80 | (c & 0x3f));
			int b = ByteTool.readUInt16(oc, 0);
			while (repCnt-- > 0)
			{
				ByteTool.writeInt16(this.v, buffEnd, b);
				this.v[buffEnd + 2] = oc[2];
				buffEnd += 3;
			}
		}
		else if (c < 0x200000)
		{
			this.allocLeng(4 * repCnt);
			buffEnd = this.leng;
			oc[0] = (byte)(0xf0 | (c >> 18));
			oc[1] = (byte)(0x80 | ((c >> 12) & 0x3f));
			oc[2] = (byte)(0x80 | ((c >> 6) & 0x3f));
			oc[3] = (byte)(0x80 | (c & 0x3f));
			int b = ByteTool.readInt32(oc, 0);
			while (repCnt-- > 0)
			{
				ByteTool.writeInt32(this.v, buffEnd, b);
				buffEnd += 4;
			}
		}
		else if (c < 0x4000000)
		{
			this.allocLeng(5 * repCnt);
			buffEnd = this.leng;
			oc[0] = (byte)(0xf8 | (c >> 24));
			oc[1] = (byte)(0x80 | ((c >> 18) & 0x3f));
			oc[2] = (byte)(0x80 | ((c >> 12) & 0x3f));
			oc[3] = (byte)(0x80 | ((c >> 6) & 0x3f));
			oc[4] = (byte)(0x80 | (c & 0x3f));
			int b = ByteTool.readInt32(oc, 0);
			while (repCnt-- > 0)
			{
				ByteTool.writeInt32(this.v, buffEnd, b);
				this.v[buffEnd + 4] = oc[4];
				buffEnd += 5;
			}
		}
		else
		{
			this.allocLeng(6 * repCnt);
			buffEnd = this.leng;
			oc[0] = (byte)(0xfc | (c >> 30));
			oc[1] = (byte)(0x80 | ((c >> 24) & 0x3f));
			oc[2] = (byte)(0x80 | ((c >> 18) & 0x3f));
			oc[3] = (byte)(0x80 | ((c >> 12) & 0x3f));
			oc[4] = (byte)(0x80 | ((c >> 6) & 0x3f));
			oc[5] = (byte)(0x80 | (c & 0x3f));
			int b1 = ByteTool.readInt32(oc, 0);
			int b2 = ByteTool.readUInt16(oc, 4);
			while (repCnt-- > 0)
			{
				ByteTool.writeInt32(this.v, buffEnd, b1);
				ByteTool.writeInt16(this.v, buffEnd + 4, b2);
				buffEnd += 6;
			}
		}
		this.v[buffEnd + 0] = 0;
		this.leng = buffEnd;
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
