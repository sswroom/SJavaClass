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
		return this.append(Short.toString(iVal));
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
	public StringBuilderUTF8 appendHexBuff(@Nonnull byte[] buff, int ofst, int buffSize, byte seperator, LineBreakType lineBreak)
	{
		if (buffSize == 0)
			return this;
		int lbCnt;
		int lineCnt;
		int i;
		if (lineBreak == LineBreakType.NONE)
		{
			lbCnt = 0;
			lineCnt = 0;
		}
		else
		{
			lineCnt = (buffSize >> 4);
			if ((buffSize & 15) == 0)
				lineCnt -= 1;
			if (lineBreak == LineBreakType.CRLF)
				lbCnt = lineCnt << 1;
			else
				lbCnt = lineCnt;
		}
		i = 0;
		if (seperator == 0)
		{
			this.allocLeng((buffSize << 1) + lbCnt);
			int buffEnd = this.leng;
			this.leng += (buffSize << 1) + lbCnt;
			while (buffSize-- > 0)
			{
				this.v[buffEnd + 0] = (byte)StringUtil.HEX_ARRAY[buff[ofst] >> 4];
				this.v[buffEnd + 1] = (byte)StringUtil.HEX_ARRAY[buff[ofst] & 15];
				buffEnd += 2;
				ofst++;
				i++;
				if ((i & 15) == 0 && buffSize > 0)
				{
					if (lineBreak == LineBreakType.CRLF)
					{
						this.v[buffEnd + 0] = 13;
						this.v[buffEnd + 1] = 10;
						buffEnd += 2;
					}
					else if (lineBreak == LineBreakType.CR)
					{
						this.v[buffEnd++] = '\r';
					}
					else if (lineBreak == LineBreakType.LF)
					{
						this.v[buffEnd++] = '\n';
					}
				}
			}
			this.v[buffEnd + 0] = 0;
		}
		else
		{
			this.allocLeng(buffSize * 3 + lbCnt - 1 - lineCnt);
			int buffEnd = this.leng;
			this.leng += buffSize * 3 + lbCnt - 1 - lineCnt;
			while (buffSize-- > 0)
			{
				i++;
				this.v[buffEnd + 0] = (byte)StringUtil.HEX_ARRAY[buff[ofst] >> 4];
				this.v[buffEnd + 1] = (byte)StringUtil.HEX_ARRAY[buff[ofst] & 15];
				ofst++;
				if (buffSize > 0)
				{
					if ((i & 15) == 0)
					{
						switch (lineBreak)
						{
						case CRLF:
							this.v[buffEnd + 2] = 13;
							this.v[buffEnd + 3] = 10;
							buffEnd += 4;
							break;
						case CR:
							this.v[buffEnd + 2] = '\r';
							buffEnd += 3;
							break;
						case LF:
							this.v[buffEnd + 2] = '\n';
							buffEnd += 3;
							break;
						case NONE:
						default:
							this.v[buffEnd + 2] = seperator;
							buffEnd += 3;
							break;
						}
					}
					else
					{
						this.v[buffEnd + 2] = seperator;
						buffEnd += 3;
					}
				}
				else
				{
					buffEnd += 2;
					this.v[buffEnd] = 0;
					break;
				}
			}
		}
		return this;
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
