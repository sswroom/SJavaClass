package org.sswr.util.data.textbinenc;

import org.sswr.util.data.ByteTool;
import org.sswr.util.data.LineBreakType;
import org.sswr.util.data.StringUtil;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class HexTextBinEnc extends TextBinEnc
{
	public HexTextBinEnc()
	{
	}

	@Override
	public @Nullable String encodeBin(@Nonnull byte []dataBuff, int dataOfst, int buffSize)
	{
		StringBuilder sb = new StringBuilder();
		StringUtil.appendHex(sb, dataBuff, dataOfst, buffSize, ' ', LineBreakType.CRLF);
		return sb.toString();
	}

	@Override
	public @Nullable byte []decodeBin(@Nonnull String s)
	{
		char carr[] = s.toCharArray();
		char c;
		byte b = 0;
		boolean exist = false;
		int i = 0;
		int j = carr.length;
		byte dataBuff[] = new byte[j / 2];
		int dataOfst = 0;
		while (i < j)
		{
			c = carr[i++];
			if (c >= '0' && c <= '9')
			{
				b = (byte)(b | (c - 0x30));
			}
			else if (c >= 'A' && c <= 'F')
			{
				b = (byte)(b | (c - 0x37));
			}
			else if (c >= 'a' && c <= 'f')
			{
				b = (byte)(b | (c - 0x57));
			}
			else
			{
				continue;
			}
			if (exist)
			{
				dataBuff[dataOfst++] = b;
				exist = false;
				b = 0;
			}
			else
			{
				b = (byte)(b << 4);
				exist = true;
			}
		}
		byte retBuff[] = new byte[dataOfst];
		ByteTool.copyArray(retBuff, 0, dataBuff, 0, dataOfst);
		return retBuff;
	}

	public @Nonnull String getName()
	{
		return "Hex";
	}
}
