package org.sswr.util.data.textbinenc;

import org.sswr.util.data.ByteTool;

import jakarta.annotation.Nonnull;

public class Base32Enc extends TextBinEnc
{
	private static byte decArr[] = {
		(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff,
		(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff,
		(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0x3e, (byte)0xff, (byte)0x3e, (byte)0xff, (byte)0xff,
		(byte)0xff, (byte)0xff, (byte)0x1a, (byte)0x1b, (byte)0x1c, (byte)0x1d, (byte)0x1e, (byte)0x1f, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff,
		(byte)0xff, (byte)0x00, (byte)0x01, (byte)0x02, (byte)0x03, (byte)0x04, (byte)0x05, (byte)0x06, (byte)0x07, (byte)0x08, (byte)0x09, (byte)0x0a, (byte)0x0b, (byte)0x0c, (byte)0x0d, (byte)0x0e,
		(byte)0x0f, (byte)0x10, (byte)0x11, (byte)0x12, (byte)0x13, (byte)0x14, (byte)0x15, (byte)0x16, (byte)0x17, (byte)0x18, (byte)0x19, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff,
		(byte)0xff, (byte)0x00, (byte)0x01, (byte)0x02, (byte)0x03, (byte)0x04, (byte)0x05, (byte)0x06, (byte)0x07, (byte)0x08, (byte)0x09, (byte)0x0a, (byte)0x0b, (byte)0x0c, (byte)0x0d, (byte)0x0e,
		(byte)0x0f, (byte)0x10, (byte)0x11, (byte)0x12, (byte)0x13, (byte)0x14, (byte)0x15, (byte)0x16, (byte)0x17, (byte)0x18, (byte)0x19, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff,
		(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff,
		(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff,
		(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff,
		(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff,
		(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff,
		(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff,
		(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff,
		(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff
	};

	private static @Nonnull char []getEncArr()
	{
		return "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567".toCharArray();
	}

	public Base32Enc()
	{
	}

	public @Nonnull String encodeBin(@Nonnull byte []dataBuff, int dataOfst, int buffSize)
	{
		StringBuilder sb = new StringBuilder();
		char []encArr = getEncArr();
		int outSize;
		int tmp1 = buffSize * 8 / 5;
		int tmp2 = buffSize * 8 % 5;
		if (tmp2 != 0)
		{
			outSize = tmp1 + 1;
		}
		else
		{
			outSize = tmp1;
		}
		if (outSize == 0)
			return "";
		sb.ensureCapacity(outSize);
		tmp1 = buffSize / 5;
		tmp2 = buffSize - tmp1 * 5;
		while (tmp1-- > 0)
		{
			sb.append(encArr[ByteTool.shr8(dataBuff[dataOfst + 0], 3)]);
			sb.append(encArr[((dataBuff[dataOfst + 0] << 2) | ByteTool.shr8(dataBuff[dataOfst + 1], 6)) & 0x1f]);
			sb.append(encArr[(ByteTool.shr8(dataBuff[dataOfst + 1], 1)) & 0x1f]);
			sb.append(encArr[((dataBuff[dataOfst + 1] << 4) | ByteTool.shr8(dataBuff[dataOfst + 2], 4)) & 0x1f]);
			sb.append(encArr[((dataBuff[dataOfst + 2] << 1) | ByteTool.shr8(dataBuff[dataOfst + 3], 7)) & 0x1f]);
			sb.append(encArr[(ByteTool.shr8(dataBuff[dataOfst + 3], 2)) & 0x1f]);
			sb.append(encArr[((dataBuff[dataOfst + 3] << 3) | ByteTool.shr8(dataBuff[dataOfst + 4], 5)) & 0x1f]);
			sb.append(encArr[dataBuff[dataOfst + 4] & 0x1f]);
			dataOfst += 5;
		}
		if (tmp2 == 1)
		{
			sb.append(encArr[ByteTool.shr8(dataBuff[dataOfst + 0], 3)]);
			sb.append(encArr[(dataBuff[dataOfst + 0] << 2) & 0x1f]);
		}
		else if (tmp2 == 2)
		{
			sb.append(encArr[ByteTool.shr8(dataBuff[dataOfst + 0], 3)]);
			sb.append(encArr[((dataBuff[dataOfst + 0] << 2) | ByteTool.shr8(dataBuff[dataOfst + 1], 6)) & 0x1f]);
			sb.append(encArr[(ByteTool.shr8(dataBuff[dataOfst + 1], 1)) & 0x1f]);
			sb.append(encArr[(dataBuff[dataOfst + 1] << 4) & 0x1f]);
		}
		else if (tmp2 == 3)
		{
			sb.append(encArr[ByteTool.shr8(dataBuff[dataOfst + 0], 3)]);
			sb.append(encArr[((dataBuff[dataOfst + 0] << 2) | ByteTool.shr8(dataBuff[dataOfst + 1], 6)) & 0x1f]);
			sb.append(encArr[(ByteTool.shr8(dataBuff[dataOfst + 1], 1)) & 0x1f]);
			sb.append(encArr[((dataBuff[dataOfst + 1] << 4) | ByteTool.shr8(dataBuff[dataOfst + 2], 4)) & 0x1f]);
			sb.append(encArr[(dataBuff[dataOfst + 2] << 1) & 0x1f]);
		}
		else if (tmp2 == 4)
		{
			sb.append(encArr[ByteTool.shr8(dataBuff[dataOfst + 0], 3)]);
			sb.append(encArr[((dataBuff[dataOfst + 0] << 2) | ByteTool.shr8(dataBuff[dataOfst + 1], 6)) & 0x1f]);
			sb.append(encArr[(ByteTool.shr8(dataBuff[dataOfst + 1], 1)) & 0x1f]);
			sb.append(encArr[((dataBuff[dataOfst + 1] << 4) | ByteTool.shr8(dataBuff[dataOfst + 2], 4)) & 0x1f]);
			sb.append(encArr[((dataBuff[dataOfst + 2] << 1) | ByteTool.shr8(dataBuff[dataOfst + 3], 7)) & 0x1f]);
			sb.append(encArr[(ByteTool.shr8(dataBuff[dataOfst + 3], 2)) & 0x1f]);
			sb.append(encArr[(dataBuff[dataOfst + 3] << 3) & 0x1f]);
		}
		return sb.toString();
	}

	public @Nonnull byte []decodeBin(@Nonnull String s)
	{
		char b64Str[] = s.toCharArray();
		int i = 0;
		int j = b64Str.length;
		int len = j * 5 / 8 + 1;
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
						b2 = (byte)(code << 3);
						b = 1;
						break;
					case 1:
						destBuff[destOfst] = (byte)(b2 | (code >> 2));
						b2 = (byte)(code << 6);
						destOfst++;
						b = 2;
						break;
					case 2:
						b2 = (byte)(b2 | (code << 1));
						b = 3;
						break;
					case 3:
						destBuff[destOfst] = (byte)(b2 | (code >> 4));
						b2 = (byte)(code << 4);
						destOfst++;
						b = 4;
						break;
					case 4:
						destBuff[destOfst] = (byte)(b2 | (code >> 1));
						b2 = (byte)(code << 7);
						destOfst++;
						b = 5;
						break;
					case 5:
						b2 = (byte)(b2 | (code << 2));
						b = 6;
						break;
					case 6:
						destBuff[destOfst] = (byte)(b2 | (code >> 3));
						b2 = (byte)(code << 5);
						destOfst++;
						b = 7;
						break;
					case 7:
						destBuff[destOfst] = (byte)(b2 | code);
						b2 = 0;
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

	public @Nonnull String getName()
	{
		return "Base32";
	}	
}
