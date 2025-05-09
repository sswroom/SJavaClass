package org.sswr.util.data.textbinenc;

import java.nio.charset.StandardCharsets;

import org.sswr.util.data.ByteTool;
import org.sswr.util.data.LineBreakType;
import org.sswr.util.data.StringBuilderUTF8;

import jakarta.annotation.Nonnull;

public class Base64Enc extends TextBinEnc
{
	public enum B64Charset
	{
		NORMAL,
		URL
	}
	private B64Charset cs;
	private boolean noPadding;
	private static byte decArr[] = {
		(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff,
		(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff,
		(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0x3e, (byte)0xff, (byte)0x3e, (byte)0xff, (byte)0x3f,
		(byte)0x34, (byte)0x35, (byte)0x36, (byte)0x37, (byte)0x38, (byte)0x39, (byte)0x3a, (byte)0x3b, (byte)0x3c, (byte)0x3d, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff,
		(byte)0xff, (byte)0x00, (byte)0x01, (byte)0x02, (byte)0x03, (byte)0x04, (byte)0x05, (byte)0x06, (byte)0x07, (byte)0x08, (byte)0x09, (byte)0x0a, (byte)0x0b, (byte)0x0c, (byte)0x0d, (byte)0x0e,
		(byte)0x0f, (byte)0x10, (byte)0x11, (byte)0x12, (byte)0x13, (byte)0x14, (byte)0x15, (byte)0x16, (byte)0x17, (byte)0x18, (byte)0x19, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0x3f,
		(byte)0xff, (byte)0x1a, (byte)0x1b, (byte)0x1c, (byte)0x1d, (byte)0x1e, (byte)0x1f, (byte)0x20, (byte)0x21, (byte)0x22, (byte)0x23, (byte)0x24, (byte)0x25, (byte)0x26, (byte)0x27, (byte)0x28,
		(byte)0x29, (byte)0x2a, (byte)0x2b, (byte)0x2c, (byte)0x2d, (byte)0x2e, (byte)0x2f, (byte)0x30, (byte)0x31, (byte)0x32, (byte)0x33, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff,
		(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff,
		(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff,
		(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff,
		(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff,
		(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff,
		(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff,
		(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff,
		(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff
	};

	private static @Nonnull byte []getEncArr(@Nonnull B64Charset cs)
	{
		if (cs == B64Charset.URL)
		{
			return "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_".getBytes(StandardCharsets.UTF_8);
		}
		else
		{
			return "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".getBytes(StandardCharsets.UTF_8);
		}
	}

	public Base64Enc()
	{
		this.cs = B64Charset.NORMAL;
		this.noPadding = false;
	}

	public Base64Enc(@Nonnull B64Charset cs, boolean noPadding)
	{
		this.cs = cs;
		this.noPadding = noPadding;
	}

	@Nonnull
	public String encodeBin(@Nonnull byte []dataBuff, int dataOfst, int buffSize)
	{
		StringBuilderUTF8 sb = new StringBuilderUTF8();
		encodeBin(sb, dataBuff, dataOfst, buffSize);
		return sb.toString();
	}

	public void encodeBin(@Nonnull StringBuilderUTF8 sb, @Nonnull byte []dataBuff, int dataOfst, int buffSize)
	{
		encodeBin(sb, dataBuff, dataOfst, buffSize, LineBreakType.NONE, 0);
	}

	public void encodeBin(@Nonnull StringBuilderUTF8 sb, @Nonnull byte []dataBuff)
	{
		encodeBin(sb, dataBuff, 0, dataBuff.length, LineBreakType.NONE, 0);
	}

	public void encodeBin(@Nonnull StringBuilderUTF8 sb, @Nonnull byte[] dataBuff, int dataOfst, int buffSize, @Nonnull LineBreakType lbt, int charsPerLine)
	{
		byte []encArr = getEncArr(this.cs);
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
			return;
		int lbSize;
		switch (lbt)
		{
		case CRLF:
			lbSize = 2;
			break;
		case CR:
		case LF:
			lbSize = 1;
			break;
		case NONE:
		default:
			lbSize = 0;
			break;
		}
		if (lbt == LineBreakType.NONE || charsPerLine == 0)
		{
			sb.allocLeng(outSize);
			while (tmp2-- > 0)
			{
				sb.appendUTF8Char(encArr[ByteTool.shr8(dataBuff[dataOfst + 0], 2)]);
				sb.appendUTF8Char(encArr[((dataBuff[dataOfst + 0] << 4) | ByteTool.shr8(dataBuff[dataOfst + 1], 4)) & 0x3f]);
				sb.appendUTF8Char(encArr[((dataBuff[dataOfst + 1] << 2) | ByteTool.shr8(dataBuff[dataOfst + 2], 6)) & 0x3f]);
				sb.appendUTF8Char(encArr[dataBuff[dataOfst + 2] & 0x3f]);
				dataOfst += 3;
			}
			if (tmp1 == 1)
			{
				sb.appendUTF8Char(encArr[ByteTool.shr8(dataBuff[dataOfst + 0], 2)]);
				sb.appendUTF8Char(encArr[(dataBuff[dataOfst + 0] << 4) & 0x3f]);
				if (this.noPadding)
				{
				}
				else
				{
					sb.appendUTF8Char((byte)'=');
					sb.appendUTF8Char((byte)'=');
				}
			}
			else if (tmp1 == 2)
			{
				sb.appendUTF8Char(encArr[ByteTool.shr8(dataBuff[dataOfst + 0], 2)]);
				sb.appendUTF8Char(encArr[((dataBuff[dataOfst + 0] << 4) | ByteTool.shr8(dataBuff[dataOfst + 1], 4)) & 0x3f]);
				sb.appendUTF8Char(encArr[(dataBuff[dataOfst + 1] << 2) & 0x3f]);
				if (this.noPadding)
				{
				}
				else
				{
					sb.appendUTF8Char((byte)'=');
				}
			}
		}
		else
		{
			byte[] sptr = new byte[4];
			int lineCnt = outSize / charsPerLine;
			sb.allocLeng(outSize + lineCnt * lbSize);
			int lineLeft = charsPerLine;
			while (tmp2-- > 0)
			{
				sptr[0] = encArr[ByteTool.shr8(dataBuff[dataOfst + 0], 2)];
				sptr[1] = encArr[((dataBuff[dataOfst + 0] << 4) | ByteTool.shr8(dataBuff[dataOfst + 1], 4)) & 0x3f];
				sptr[2] = encArr[((dataBuff[dataOfst + 1] << 2) | ByteTool.shr8(dataBuff[dataOfst + 2], 6)) & 0x3f];
				sptr[3] = encArr[dataBuff[dataOfst + 2] & 0x3f];
				if (lineLeft > 4)
				{
					sb.appendC(sptr, 0, 4);
					lineLeft -= 4;
				}
				else if (lineLeft == 4)
				{
					sb.appendC(sptr, 0, 4);
					sb.appendLB(lbt);
					lineLeft = charsPerLine;
				}
				else
				{
					sb.appendC(sptr, 0, lineLeft);
					sb.appendLB(lbt);
					sb.appendC(sptr, lineLeft, 4 - lineLeft);
					lineLeft = charsPerLine + lineLeft - 4;
				}
				dataOfst += 3;
			}
			if (tmp1 == 1)
			{
				sptr[0] = encArr[ByteTool.shr8(dataBuff[dataOfst + 0], 2)];
				sptr[1] = encArr[(dataBuff[dataOfst + 0] << 4) & 0x3f];
				if (this.noPadding)
				{
					if (lineLeft >= 2)
					{
						sb.appendC(sptr, 0, 2);
					}
					else
					{
						sb.appendUTF8Char(sptr[0]);
						sb.appendLB(lbt);
						sb.appendUTF8Char(sptr[1]);
					}
				}
				else
				{
					sptr[2] = '=';
					sptr[3] = '=';
					if (lineLeft >= 4)
					{
						sb.appendC(sptr, 0, 4);
					}
					else
					{
						sb.appendC(sptr, 0, lineLeft);
						sb.appendLB(lbt);
						sb.appendC(sptr, lineLeft, 4 - lineLeft);
					}
				}
			}
			else if (tmp1 == 2)
			{
				sptr[0] = encArr[ByteTool.shr8(dataBuff[dataOfst + 0], 2)];
				sptr[1] = encArr[((dataBuff[dataOfst + 0] << 4) | ByteTool.shr8(dataBuff[dataOfst + 1], 4)) & 0x3f];
				sptr[2] = encArr[(dataBuff[dataOfst + 1] << 2) & 0x3f];
				if (this.noPadding)
				{
					if (lineLeft >= 3)
					{
						sb.appendC(sptr, 0, 3);
					}
					else
					{
						sb.appendC(sptr, 0, lineLeft);
						sb.appendLB(lbt);
						sb.appendC(sptr, lineLeft, 3 - lineLeft);
					}
				}
				else
				{
					sptr[3] = '=';
					if (lineLeft >= 4)
					{
						sb.appendC(sptr, 0, 4);
					}
					else
					{
						sb.appendC(sptr, 0, lineLeft);
						sb.appendLB(lbt);
						sb.appendC(sptr, lineLeft, 4 - lineLeft);
					}
				}
			}
		}
	}

	@Nonnull
	public byte []decodeBin(@Nonnull String s)
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
				if (code != (byte)0xff)
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

	@Nonnull
	public byte []decodeBin(@Nonnull byte []dataBuff, int dataOfst, int buffSize)
	{
		int i = 0;
		int j = buffSize;
		int len = j / 4 * 3 + (j & 3);
		int c;
		byte code;
		byte b = 0;
		byte b2 = 0;
		byte destBuff[] = new byte[len];
		int destOfst = 0;
		while (i < j)
		{
			c = dataBuff[dataOfst + (i++)] & 255;
			if (c < 0x80)
			{
				code = decArr[c];
				if (code != (byte)0xff)
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

	public @Nonnull String getName()
	{
		return "Base64";
	}
}
