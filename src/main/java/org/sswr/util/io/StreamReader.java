package org.sswr.util.io;

import org.sswr.util.data.ByteTool;
import org.sswr.util.data.Encoding;
import org.sswr.util.data.LineBreakType;
import org.sswr.util.data.SharedInt;
import org.sswr.util.data.StringBuilderUTF8;

import jakarta.annotation.Nonnull;

public class StreamReader implements IOReader
{
	private static int BUFFSIZE = 16384;

	private @Nonnull Encoding enc;
	private @Nonnull IOStream stm;
	private @Nonnull byte[] cbuff;
	private int cSize;
	private int cPos;
	private @Nonnull byte[] buff;
	private int buffSize;
	private @Nonnull LineBreakType lineBreak;
	private long lastPos;

	private void fillBuffer()
	{
		int i;
		int destOfst;

		if (stm instanceof SeekableStream)
		{
			long currPos = ((SeekableStream)stm).getPosition();
			if (this.lastPos != currPos)
			{
				buffSize = 0;
				cSize = 0;
				cPos = 0;
			}
		}

		if (cPos != 0)
		{
			if (cSize > cPos)
			{
				i = cSize - cPos;
				ByteTool.copyArray(cbuff, 0, cbuff, cPos, i);
				cSize = i;
				cPos = 0;
			}
			else
			{
				cPos = 0;
				cSize = 0;
			}
		}

		buffSize += stm.read(buff, buffSize, BUFFSIZE - buffSize);
		if (stm instanceof SeekableStream)
		{
			lastPos = ((SeekableStream)stm).getPosition();
		}
		if (buffSize <= 0)
		{
			return;
		}
		int convSize = BUFFSIZE - cSize;
		if (convSize > buffSize)
			convSize = buffSize;
		SharedInt si = new SharedInt();
		if (convSize != 0)
		{
			destOfst = this.enc.utf8FromBytes(cbuff, cSize, buff, 0, convSize, si);
			cSize = destOfst;
			ByteTool.copyArray(buff, 0, buff, si.value, buffSize - si.value);
			buffSize -= si.value;
		}
	}

	private void checkHeader()
	{
		if (buffSize != 0)
			return;
		buffSize += stm.read(buff, buffSize, 4 - buffSize);
		if (buffSize >= 3 && (buff[0] & 0xff) == 0xef && (buff[1] & 0xff) == 0xbb && (buff[2] & 0xff) == 0xbf)
		{
			this.enc.setCodePage(65001);
			buff[0] = buff[3];
			buffSize -= 3;
		}
		else if (buffSize >= 2 && (buff[0] & 0xff) == 0xff && (buff[1] & 0xff) == 0xfe)
		{
			this.enc.setCodePage(1200);
			buff[0] = buff[2];
			buff[1] = buff[3];
			buffSize -= 2;
		}
		else if (buffSize >= 2 && (buff[0] & 0xff) == 0xfe && (buff[1] & 0xff) == 0xff)
		{
			this.enc.setCodePage(1201);
			buff[0] = buff[2];
			buff[1] = buff[3];
			buffSize -= 2;
		}
		if (stm instanceof SeekableStream)
		{
			this.lastPos = ((SeekableStream)stm).getPosition();
		}
		else
		{
			this.lastPos = 0;
		}
	}

	public StreamReader(@Nonnull IOStream stm)
	{
		this.enc = new Encoding();
		this.stm = stm;
		this.buff = new byte[BUFFSIZE];
		this.buffSize = 0;
		this.cbuff = new byte[BUFFSIZE + 1];
		this.cSize = 0;
		this.cPos = 0;
		if (stm instanceof SeekableStream)
		{
			this.lastPos = ((SeekableStream)stm).getPosition();
		}
		else
		{
			this.lastPos = 0;
		}
		this.lineBreak = LineBreakType.NONE;
		checkHeader();
		fillBuffer();
	}

	public StreamReader(@Nonnull IOStream stm, int codePage)
	{
		this.enc = new Encoding(codePage);
		this.stm = stm;
		this.buff = new byte[BUFFSIZE];
		this.buffSize = 0;
		this.cbuff = new byte[BUFFSIZE + 1];
		this.cSize = 0;
		this.cPos = 0;
		if (stm instanceof SeekableStream)
		{
			this.lastPos = ((SeekableStream)stm).getPosition();
		}
		else
		{
			this.lastPos = 0;
		}
		this.lineBreak = LineBreakType.NONE;
		checkHeader();
		fillBuffer();
	}

	public void close()
	{
		this.stm.close();
	}

	public int readLine(@Nonnull char[] buff, int ofst, int maxCharCnt)
	{
		return -1;
	}

	public int readLine(@Nonnull byte[] buff, int ofst, int maxCharCnt)
	{
		byte[] dest = buff;
		int destOfst = ofst;
		boolean tmp = false;
		if (stm instanceof SeekableStream)
		{
			long currPos = ((SeekableStream)stm).getPosition();
			if (this.lastPos != currPos)
			{
				this.fillBuffer();
			}
		}
		int currPos = cPos;
		int currSize = cSize;
		byte c;
		while (true)
		{
			currPos = cPos;
			currSize = cSize;
			int srcOfst = currPos;
			while (currPos < currSize)
			{
				c = cbuff[srcOfst];
				if (c == 13)
				{
					if (!tmp && srcOfst + 1 == currSize)
					{
						tmp = true;
						break;
					}
					if (cbuff[srcOfst + 1] == 10)
					{
						dest[destOfst] = 0;
						this.cPos = currPos + 2;
						this.lineBreak = LineBreakType.CRLF;
						return destOfst;
					}
					else
					{
						dest[destOfst] = 0;
						this.cPos = currPos + 1;
						this.lineBreak = LineBreakType.CR;
						return destOfst;
					}
				}
				else if (c == 10)
				{
					dest[destOfst] = 0;
					this.cPos = currPos + 1;
					this.lineBreak = LineBreakType.LF;
					return destOfst;
				}
				else if (maxCharCnt <= 0)
				{
					dest[destOfst] = 0;
					this.lineBreak = LineBreakType.NONE;
					this.cPos = currPos;
					return destOfst;
				}

				dest[destOfst++] = c;
				srcOfst++;
				currPos++;
				maxCharCnt--;
			}
			this.cPos = currPos;
			this.fillBuffer();
			if (cSize <= cPos)
			{
				if (buff == dest)
				{
					return -1;
				}
				else
				{
					dest[destOfst] = 0;
					this.lineBreak = LineBreakType.NONE;
					return destOfst;
				}
			}
		}
	}

	public boolean readLine(@Nonnull StringBuilderUTF8 sb, int maxCharCnt)
	{
		sb.allocLeng(maxCharCnt);
		int endPtr;
		if ((endPtr = this.readLine(sb.getBytes(), sb.getBytesLength(), maxCharCnt)) > 0)
		{
			sb.setEndOfst(endPtr);
			return true;
		}
		return false;
	}

	public int getLastLineBreak(@Nonnull char[] buff, int ofst)
	{
		if (this.lineBreak == LineBreakType.CR)
		{
			buff[ofst++] = 13;
		}
		else if (this.lineBreak == LineBreakType.LF)
		{
			buff[ofst++] = 10;
		}
		else if (this.lineBreak == LineBreakType.CRLF)
		{
			buff[ofst++] = 13;
			buff[ofst++] = 10;
		}
		buff[ofst] = 0;
		return ofst;
	}

	public int getLastLineBreak(@Nonnull byte[] buff, int ofst)
	{
		if (this.lineBreak == LineBreakType.CR)
		{
			buff[ofst++] = 13;
		}
		else if (this.lineBreak == LineBreakType.LF)
		{
			buff[ofst++] = 10;
		}
		else if (this.lineBreak == LineBreakType.CRLF)
		{
			buff[ofst++] = 13;
			buff[ofst++] = 10;
		}
		buff[ofst] = 0;
		return ofst;
	}

	public boolean getLastLineBreak(@Nonnull StringBuilderUTF8 sb)
	{
		sb.appendLB(this.lineBreak);
		return true;
	}

	public boolean isLineBreak()
	{
		return this.lineBreak != LineBreakType.NONE;
	}

	public boolean readToEnd(@Nonnull StringBuilderUTF8 sb)
	{
		boolean succ = false;
		while (this.readLine(sb, BUFFSIZE))
		{
			succ = this.getLastLineBreak(sb);
		}
		return succ;
	}
}