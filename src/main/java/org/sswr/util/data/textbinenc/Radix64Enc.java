package org.sswr.util.data.textbinenc;

import org.sswr.util.data.ByteTool;

public class Radix64Enc
{
	private byte[] encArr;
	private byte[] decArr;
	
	public Radix64Enc(String encArr)
	{
		this.encArr = new byte[64];
		this.decArr = new byte[256];
		int i = 256;
		int j;
		while (i-- > 0)
		{
			this.decArr[i] = -1;
		}
		char c;
		char[] encBuff = encArr.toCharArray();
		i = 0;
		j = 64;
		while (i < j)
		{
			c = encBuff[i];
			this.encArr[i] = (byte)c;
			this.decArr[(byte)c] = (byte)i;
			i++;
		}
	}

	public String encodeBin(byte []dataBuff, int dataOfst, int buffSize)
	{
		StringBuilder sb = new StringBuilder();
		int outSize;
		int tmp1 = buffSize % 3;
		int tmp2 = buffSize / 3;
		if (tmp1 != 0)
		{
			outSize = tmp2 * 4 + 4;
		}
		else
		{
			outSize = tmp2 * 4;
		}
		if (outSize == 0)
			return "";
		sb.ensureCapacity(outSize);
		while (tmp2-- > 0)
		{
			sb.append((char)encArr[ByteTool.shr8(dataBuff[dataOfst + 0], 2)]);
			sb.append((char)encArr[((dataBuff[dataOfst + 0] << 4) | ByteTool.shr8(dataBuff[dataOfst + 1], 4)) & 0x3f]);
			sb.append((char)encArr[((dataBuff[dataOfst + 1] << 2) | ByteTool.shr8(dataBuff[dataOfst + 2], 6)) & 0x3f]);
			sb.append((char)encArr[dataBuff[dataOfst + 2] & 0x3f]);
			dataOfst += 3;
		}
		if (tmp1 == 1)
		{
			sb.append((char)encArr[ByteTool.shr8(dataBuff[dataOfst + 0], 2)]);
			sb.append((char)encArr[(dataBuff[dataOfst + 0] << 4) & 0x3f]);
		}
		else if (tmp1 == 2)
		{
			sb.append((char)encArr[ByteTool.shr8(dataBuff[dataOfst + 0], 2)]);
			sb.append((char)encArr[((dataBuff[dataOfst + 0] << 4) | ByteTool.shr8(dataBuff[dataOfst + 1], 4)) & 0x3f]);
			sb.append((char)encArr[(dataBuff[dataOfst + 1] << 2) & 0x3f]);
		}
		return sb.toString();
	}

	public byte []decodeBin(String s)
	{
		char b64Str[] = s.toCharArray();
		int i = 0;
		int j = b64Str.length;
		int len = j / 4 * 3 + (j & 3);
		char c;
		byte code;
		byte b = 0;
		byte b2 = 0;
		byte destBuff[] = new byte[len];
		int destOfst = 0;
		while (i < j)
		{
			c = b64Str[i++];
			if (c < 0x80)
			{
				code = decArr[c];
				if (code != 0xff)
				{
					switch (b)
					{
					case 0:
						b2 = (byte)(code << 2);
						b = 1;
						break;
					case 1:
						destBuff[destOfst] = (byte)(b2 | (code >> 4));
						b2 = (byte)(code << 4);
						destOfst++;
						b = 2;
						break;
					case 2:
						destBuff[destOfst] = (byte)(b2 | (code >> 2));
						b2 = (byte)(code << 6);
						destOfst++;
						b = 3;
						break;
					case 3:
						destBuff[destOfst] = (byte)(b2 | code);
						destOfst++;
						b = 0;
						break;
					}
				}
			}
		}
		if (destOfst == len)
		{
			return destBuff;
		}
		else
		{
			byte outBuff[] = new byte[destOfst];
			ByteTool.copyArray(outBuff, 0, destBuff, 0, destOfst);
			return outBuff;
		}
	}

	public String getName()
	{
		return "Radix64";
	}
}
