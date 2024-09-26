package org.sswr.util.data.textbinenc;

import org.sswr.util.data.ByteTool;

import jakarta.annotation.Nonnull;

public class CPPTextBinEnc extends TextBinEnc
{
	public CPPTextBinEnc()
	{

	}

	@Nonnull
	public String encodeBin(@Nonnull byte []dataBuff, int dataOfst, int buffSize)
	{
		boolean lineStart = true;
		byte b;
		int endOfst = dataOfst + buffSize;
		StringBuilder sb = new StringBuilder();
		while (dataOfst < endOfst)
		{
			b = dataBuff[dataOfst++];
			if (lineStart)
			{
				sb.append("\"");
				lineStart = false;
			}
			if (b == 0)
			{
				sb.append("\\0");
			}
			else if (b == '\r')
			{
				sb.append("\\r");
			}
			else if (b == '\n')
			{
				sb.append("\\n\"\r\n");
				lineStart = true;
			}
			else if (b == '\\')
			{
				sb.append("\\\\");
			}
			else
			{
				dataOfst--;
				dataOfst = ByteTool.readUTF8(sb, dataBuff, dataOfst);
			}
		}
		if (!lineStart)
		{
			sb.append('\"');
		}
		return sb.toString();
	}

	@Nonnull
	public byte []decodeBin(@Nonnull String s) throws EncodingException
	{
		char carr[] = s.toCharArray();
		int i = 0;
		int j = carr.length;
		boolean isQuote = false;
		char c;
		byte dataBuff[] = new byte[j * 6];
		int dataOfst = 0;
		while (i < j)
		{
			c = carr[i++];
			if (!isQuote)
			{
				if (c == '"')
				{
					isQuote = true;
				}
				else if (c == 0x20 || c == '\t' || c == 13 || c == 10)
				{
				}
				else
				{
					throw new EncodingException("Non white space character out of quote");
				}
			}
			else if (c == '\\')
			{
				if (i >= j)
				{
					throw new EncodingException("Escape when end of string");
				}
				c = carr[i++];
				if (c == 'r')
				{
					dataBuff[dataOfst++] = '\r';
				}
				else if (c == 'n')
				{
					dataBuff[dataOfst++] = '\n';
				}
				else if (c == 't')
				{
					dataBuff[dataOfst++] = '\t';
				}
				else if (c == '\\')
				{
					dataBuff[dataOfst++] = '\\';
				}
				else if (c == '0')
				{
					dataBuff[dataOfst++] = '\0';
				}
				else
				{
					throw new EncodingException("Unsupported escape character: '"+c+"'");
				}
			}
			else if (c == '"')
			{
				isQuote = false;
			}
			else
			{
				dataOfst = ByteTool.writeUTF8(dataBuff, dataOfst, c);
			}
		}
		if (isQuote)
		{
			throw new EncodingException("End of quote not found");
		}
		byte buff[] = new byte[dataOfst];
		ByteTool.copyArray(buff, 0, dataBuff, 0, dataOfst);
		return buff;
	}

	@Nonnull
	public String getName()
	{
		return "CPP String";		
	}
}
