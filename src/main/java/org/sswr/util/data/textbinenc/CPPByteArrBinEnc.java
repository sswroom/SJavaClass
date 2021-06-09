package org.sswr.util.data.textbinenc;

import org.sswr.util.data.ByteTool;
import org.sswr.util.data.StringUtil;

public class CPPByteArrBinEnc extends TextBinEnc
{
	public CPPByteArrBinEnc()
	{
	}

	public String encodeBin(byte []dataBuff, int dataOfst, int buffSize)
	{
		StringBuilder sb = new StringBuilder();
		int i = 0;
		while (i < buffSize)
		{
			if (i > 0)
			{
				sb.append(", ");
			}
			sb.append("0x");
			sb.append(StringUtil.toHex(dataBuff[dataOfst + i]));
			i++;
		}
		return sb.toString();
	}

	public byte []decodeBin(String s)
	{
		char carr[] = s.toCharArray();
		byte destBuff[] = new byte[carr.length];
		int destOfst = 0;
		char c;
		byte b = 0;
		int i = 0;
		int j = carr.length;
		while (i < j)
		{
			c = carr[i++];
			if (c == '0' && carr[i + 1] == 'x')
			{
				i++;
				c = carr[i++];
				if (c >= '0' && c <= '9')
				{
					b = (byte)(c - 0x30);
				}
				else if (c >= 'A' && c <= 'F')
				{
					b = (byte)(c - 0x37);
				}
				else if (c >= 'a' && c <= 'f')
				{
					b = (byte)(c - 0x57);
				}
				else
				{
					destBuff[destOfst++] = b;;
					i--;
					continue;
				}
				c = carr[i++];
				if (c >= '0' && c <= '9')
				{
					b = (byte)((b << 4) | (c - 0x30));
				}
				else if (c >= 'A' && c <= 'F')
				{
					b = (byte)((b << 4) | (c - 0x37));
				}
				else if (c >= 'a' && c <= 'f')
				{
					b = (byte)((b << 4) | (c - 0x57));
				}
				else
				{
					destBuff[destOfst++] = b;;
					i--;
					continue;
				}
				destBuff[destOfst++] = b;
				continue;
			}
			else if (c >= '0' && c <= '9')
			{
				b = (byte)(c - 0x30);
				while (true)
				{
					c = carr[i++];
					if (c >= '0' && c <= '9')
					{
						b = (byte)(b * 10 + (c - 0x30));
					}
					else
					{
						destBuff[destOfst++] = b;
						i--;
						break;
					}
				}
			}
		}
		if (destOfst == j)
		{
			return destBuff;
		}
		else
		{
			byte retBuff[] = new byte[destOfst];
			ByteTool.copyArray(retBuff, 0, destBuff, 0 , destOfst);
			return retBuff;
		}
	}

	public String getName()
	{
		return "CPP Byte Arr";
	}
}
