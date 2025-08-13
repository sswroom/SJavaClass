package org.sswr.util.data;

import java.nio.charset.StandardCharsets;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class StringBuilderUTF8 implements ByteArray {
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

	@Nonnull
	public StringBuilderUTF8 append(@Nonnull String s)
	{
		byte[] sbuff = s.getBytes(StandardCharsets.UTF_8);
		this.allocLeng(sbuff.length);
		ByteTool.copyArray(this.v, this.leng, sbuff, 0, sbuff.length);
		this.leng += sbuff.length;
		this.v[this.leng] = 0;
		return this;
	}

	@Nonnull
	public StringBuilderUTF8 appendOpt(@Nullable String s)
	{
		if (s == null)
			return this;
		return append(s);
	}

	@Nonnull
	public StringBuilderUTF8 appendUTF8Char(byte c)
	{
		this.allocLeng(1);
		this.v[this.leng] = c;
		this.v[this.leng + 1] = 0;
		this.leng++;
		return this;
	}

	@Nonnull
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

	@Nonnull
	public StringBuilderUTF8 appendC(byte[] v, int ofst, int len)
	{
		this.allocLeng(len);
		ByteTool.copyArray(this.v, this.leng, v, ofst, len);
		this.leng += len;
		this.v[this.leng] = 0;
		return this;
	}

	@Nonnull
	public StringBuilderUTF8 appendI16(short iVal)
	{
		return this.append(String.valueOf(iVal));
	}

	@Nonnull
	public StringBuilderUTF8 appendU16(short iVal)
	{
		return this.append(String.valueOf(iVal & 65535));
	}

	@Nonnull
	public StringBuilderUTF8 appendI32(int iVal)
	{
		return this.append(String.valueOf(iVal));
	}

	@Nonnull
	public StringBuilderUTF8 appendU32(int iVal)
	{
		if (iVal < 0)
		{
			return this.append(String.valueOf(0xffffffffL & (long)iVal));
		}
		else
		{
			return this.append(String.valueOf(iVal));
		}
	}

	@Nonnull
	public StringBuilderUTF8 appendI64(long iVal)
	{
		return this.append(String.valueOf(iVal));
	}

	@Nonnull
	public StringBuilderUTF8 appendLB(LineBreakType lbt)
	{
		switch (lbt)
		{
		case CRLF:
			return appendC("\r\n".getBytes(), 0, 2);
		case CR:
			return appendUTF8Char((byte)'\r');
		case LF:
			return appendUTF8Char((byte)'\n');
		case NONE:
		default:
			return this;
		}
	}

	@Nonnull
	public StringBuilderUTF8 clearStr()
	{
		this.leng = 0;
		this.v[0] = 0;
		return this;
	}

	@Nonnull
	public byte[] getBytes()
	{
		return this.v;
	}

	@Nonnull
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

	@Override
	public int getBytesOffset() {
		return 0;
	}

	@Override
	public int getBytesLength() {
		return this.leng;
	}

	@Nonnull
	public String substring(int startIndex)
	{
		if (startIndex < 0)
		{
			startIndex = 0;
		}
		if (startIndex >= this.leng)
		{
			return "";
		}
		return new String(this.v, startIndex, this.leng - startIndex, StandardCharsets.UTF_8);
	}

	@Nonnull
	public String substring(int startIndex, int endIndex)
	{
		if (startIndex < 0)
		{
			startIndex = 0;
		}
		if (endIndex > this.leng)
		{
			endIndex = this.leng;
		}
		if (startIndex >= endIndex)
		{
			return "";
		}
		return new String(this.v, startIndex, endIndex - startIndex, StandardCharsets.UTF_8);
	}

	public void removeRange(int startIndex, int endIndex)
	{
		if (startIndex < 0)
			startIndex = 0;
		if (endIndex > this.leng)
			endIndex = this.leng;
		if (startIndex >= endIndex)
			return;
		ByteTool.copyArray(this.v, endIndex, this.v, startIndex, this.leng - endIndex);
		this.leng -= endIndex - startIndex;
	}

	public void setEndOfst(int endOfst)
	{
		this.leng = endOfst;
		this.v[endOfst] = 0;
	}

	public boolean startsWith(@Nonnull String s)
	{
		byte[] sarr = s.getBytes(StandardCharsets.UTF_8);
		if (sarr.length > this.leng)
			return false;
		int i = 0;
		int j = sarr.length;
		while (i < j)
		{
			if (this.v[i] != sarr[i])
				return false;
			i++;
		}
		return true;
	}

	public boolean endsWith(@Nonnull String s)
	{
		byte[] sarr = s.getBytes(StandardCharsets.UTF_8);
		if (sarr.length > this.leng)
			return false;
		int k = sarr.length;
		int i = this.leng - k;
		int j = 0;
		while (j < k)
		{
			if (this.v[i + j] != sarr[j])
				return false;
			j++;
		}
		return true;
	}

	public void trimToLength(int newLength)
	{
		if (newLength >= this.leng || newLength < 0)
			return;
		this.leng = newLength;
		this.v[newLength] = 0;
	}

	public int indexOf(@Nonnull String s)
	{
		return indexOf(s, 0);
	}

	public int indexOf(@Nonnull String s, int startIndex)
	{
		byte[] sarr = s.getBytes(StandardCharsets.UTF_8);
		int j = sarr.length;
		if (j > this.leng)
			return -1;
		if (j == 0)
			return 0;
		int i;
		int k = startIndex;
		int l = this.leng - j;
		boolean diff;
		while (k <= l)
		{
			diff = false;
			i = 0;
			while (i < j)
			{
				if (sarr[i] != this.v[k + i])
				{
					diff = true;
					break;
				}
				i++;
			}
			if (!diff)
			{
				return k;
			}

			k++;
		}
		return -1;
	}

	public void rTrim()
	{
		int len = this.leng;
		while (len > 0)
		{
			byte c = this.v[len - 1];
			if (c == ' ' || c == '\t' || c == 0)
			{
				len--;
			}
			else
			{
				break;
			}
		}
		this.v[len] = 0;
		this.leng = len;
	}

	public void trim()
	{
		this.rTrim();
		int sptr;
		byte c;
		byte[] str1 = this.v;
		if (this.leng > 0 && (str1[0] == ' ' || str1[0] == '\t'))
		{
			sptr = 1;
			while ((c = str1[sptr]) == ' ' || c == '\t')
				sptr++;
			this.leng -= sptr;
			ByteTool.copyArray(str1, 0, str1, sptr, this.leng + 1);
		}
	}

	public byte charAt(int index)
	{
		if (index < 0 || index >= this.leng)
			return 0;
		return this.v[index];
	}
}
