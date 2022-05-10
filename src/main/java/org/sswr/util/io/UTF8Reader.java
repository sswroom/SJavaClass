package org.sswr.util.io;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.sswr.util.data.ByteTool;
import org.sswr.util.data.LineBreakType;
import org.sswr.util.data.StringUtil;

public class UTF8Reader
{
	private static final int BUFFSIZE = 65536;

	private InputStream stm;
	private byte []buff;
	private int buffSize;
	private int currOfst;
	private LineBreakType lineBreak;

	private void fillBuffer()
	{
		if (this.currOfst == this.buffSize)
		{
			this.buffSize = 0;
			this.currOfst = 0;
		}
		else if (this.currOfst > 0)
		{
			ByteTool.copyArray(this.buff, 0, this.buff, this.currOfst, this.buffSize - this.currOfst);
			this.buffSize -= this.currOfst;
			this.currOfst = 0;
		}
		try
		{
			int readSize = this.stm.read(this.buff, this.buffSize, BUFFSIZE - this.buffSize);
			if (readSize >= 0)
			{
				this.buffSize += readSize;
			}
		}
		catch (IOException ex)
		{

		}
	}

	private void checkHeader()
	{
		if (this.buffSize != 0)
			return;
		try
		{
			this.buffSize += this.stm.read(this.buff, this.buffSize, 4 - this.buffSize);
		}
		catch (IOException ex)
		{
			
		}
		if (this.buffSize >= 3 && (this.buff[0] & 0xff) == 0xef && (this.buff[1] & 0xff) == 0xbb && (this.buff[2] & 0xff) == 0xbf)
		{
			this.buff[0] = this.buff[3];
			this.currOfst = 0;
			this.buffSize -= 3;
		}
	}

	public UTF8Reader(InputStream stm)
	{
		this.stm = stm;
		this.buff = new byte[BUFFSIZE];
		this.buffSize = 0;
		this.currOfst = 0;
		this.lineBreak = LineBreakType.NONE;
		this.checkHeader();
		this.fillBuffer();	
	}

	public void close() throws IOException
	{
		this.stm.close();
	}

	public char peek()
	{
		int ret;
		if (this.currOfst < this.buffSize)
		{
			byte c = this.buff[this.currOfst];
			if ((c & 0x80) == 0)
			{
				return (char)c;
			}
			else if ((c & 0xe0) == 0xc0)
			{
				if (this.buffSize - this.currOfst >= 2)
				{
					ret = ((int)(c & 0x1f) << 6) | (int)(this.buff[this.currOfst + 1] & 0x3f);
					return (char)ret;
				}
			}
			else if ((c & 0xf0) == 0xe0)
			{
				if (this.buffSize - this.currOfst >= 3)
				{
					ret = ((int)(c & 0x0f) << 12) | ((int)(this.buff[this.currOfst + 1] & 0x3f) << 6) | (int)(this.buff[this.currOfst + 2] & 0x3f);
					return (char)ret;
				}
			}
			else if ((c & 0xf8) == 0xf0)
			{
				if (this.buffSize - this.currOfst >= 4)
				{
					ret = ((int)(c & 0x7) << 18) | ((int)(this.buff[this.currOfst + 1] & 0x3f) << 12) | ((int)(this.buff[this.currOfst + 2] & 0x3f) << 6) | (int)(this.buff[this.currOfst + 3] & 0x3f);
					return (char)ret;
				}
			}
			else if ((c & 0xfc) == 0xf8)
			{
				if (this.buffSize - this.currOfst >= 5)
				{
					ret = ((int)(c & 0x3) << 24) | ((int)(this.buff[this.currOfst + 1] & 0x3f) << 18) | ((int)(this.buff[this.currOfst + 2] & 0x3f) << 12) | ((int)(this.buff[this.currOfst + 3] & 0x3f) << 6) | (int)(this.buff[this.currOfst + 4] & 0x3f);
					return (char)ret;
				}
			}
			else
			{
				if (this.buffSize - this.currOfst >= 6)
				{
					ret = ((int)(c & 0x1) << 30) | ((int)(this.buff[this.currOfst + 1] & 0x3f) << 24) | ((int)(this.buff[this.currOfst + 2] & 0x3f) << 18) | ((int)(this.buff[this.currOfst + 3] & 0x3f) << 12) | ((int)(this.buff[this.currOfst + 4] & 0x3f) << 6) | (int)(this.buff[this.currOfst + 5] & 0x3f);
					return (char)ret;
				}
			}
		}
		this.fillBuffer();
		if (this.currOfst < this.buffSize)
		{
			byte c = this.buff[this.currOfst];
			if ((c & 0x80) == 0)
			{
				return (char)c;
			}
			else if ((c & 0xe0) == 0xc0)
			{
				if (this.buffSize - this.currOfst >= 2)
				{
					ret = ((int)(c & 0x1f) << 6) | (int)(this.buff[this.currOfst + 1] & 0x3f);
					return (char)ret;
				}
			}
			else if ((c & 0xf0) == 0xe0)
			{
				if (this.buffSize - this.currOfst >= 3)
				{
					ret = ((int)(c & 0x0f) << 12) | ((int)(this.buff[this.currOfst + 1] & 0x3f) << 6) | (int)(this.buff[this.currOfst + 2] & 0x3f);
					return (char)ret;
				}
			}
			else if ((c & 0xf8) == 0xf0)
			{
				if (this.buffSize - this.currOfst >= 4)
				{
					ret = ((int)(c & 0x7) << 18) | ((int)(this.buff[this.currOfst + 1] & 0x3f) << 12) | ((int)(this.buff[this.currOfst + 2] & 0x3f) << 6) | (int)(this.buff[this.currOfst + 3] & 0x3f);
					return (char)ret;
				}
			}
			else if ((c & 0xfc) == 0xf8)
			{
				if (this.buffSize - this.currOfst >= 5)
				{
					ret = ((int)(c & 0x3) << 24) | ((int)(this.buff[this.currOfst + 1] & 0x3f) << 18) | ((int)(this.buff[this.currOfst + 2] & 0x3f) << 12) | ((int)(this.buff[this.currOfst + 3] & 0x3f) << 6) | (int)(this.buff[this.currOfst + 4] & 0x3f);
					return (char)ret;
				}
			}
			else
			{
				if (this.buffSize - this.currOfst >= 6)
				{
					ret = ((int)(c & 0x1) << 30) | ((int)(this.buff[this.currOfst + 1] & 0x3f) << 24) | ((int)(this.buff[this.currOfst + 2] & 0x3f) << 18) | ((int)(this.buff[this.currOfst + 3] & 0x3f) << 12) | ((int)(this.buff[this.currOfst + 4] & 0x3f) << 6) | (int)(this.buff[this.currOfst + 5] & 0x3f);
					return (char)ret;
				}
			}
		}
		return 0;
	}
	
	public char read()
	{
		int ret;
		if (this.currOfst < this.buffSize)
		{
			byte c = this.buff[this.currOfst];
			if ((c & 0x80) == 0)
			{
				this.currOfst += 1;
				return (char)c;
			}
			else if ((c & 0xe0) == 0xc0)
			{
				if (this.buffSize - this.currOfst >= 2)
				{
					ret = ((int)(c & 0x1f) << 6) | (int)(this.buff[this.currOfst + 1] & 0x3f);
					this.currOfst += 2;
					return (char)ret;
				}
			}
			else if ((c & 0xf0) == 0xe0)
			{
				if (this.buffSize - this.currOfst >= 3)
				{
					ret = ((int)(c & 0x0f) << 12) | ((int)(this.buff[this.currOfst + 1] & 0x3f) << 6) | (int)(this.buff[this.currOfst + 2] & 0x3f);
					this.currOfst += 3;
					return (char)ret;
				}
			}
			else if ((c & 0xf8) == 0xf0)
			{
				if (this.buffSize - this.currOfst >= 4)
				{
					ret = ((int)(c & 0x7) << 18) | ((int)(this.buff[this.currOfst + 1] & 0x3f) << 12) | ((int)(this.buff[this.currOfst + 2] & 0x3f) << 6) | (int)(this.buff[this.currOfst + 3] & 0x3f);
					this.currOfst += 4;
					return (char)ret;
				}
			}
			else if ((c & 0xfc) == 0xf8)
			{
				if (this.buffSize - this.currOfst >= 5)
				{
					ret = ((int)(c & 0x3) << 24) | ((int)(this.buff[this.currOfst + 1] & 0x3f) << 18) | ((int)(this.buff[this.currOfst + 2] & 0x3f) << 12) | ((int)(this.buff[this.currOfst + 3] & 0x3f) << 6) | (int)(this.buff[this.currOfst + 4] & 0x3f);
					this.currOfst += 5;
					return (char)ret;
				}
			}
			else
			{
				if (this.buffSize - this.currOfst >= 6)
				{
					ret = ((int)(c & 0x1) << 30) | ((int)(this.buff[this.currOfst + 1] & 0x3f) << 24) | ((int)(this.buff[this.currOfst + 2] & 0x3f) << 18) | ((int)(this.buff[this.currOfst + 3] & 0x3f) << 12) | ((int)(this.buff[this.currOfst + 4] & 0x3f) << 6) | (int)(this.buff[this.currOfst + 5] & 0x3f);
					this.currOfst += 6;
					return (char)ret;
				}
			}
		}
		this.fillBuffer();
		if (this.currOfst < this.buffSize)
		{
			byte c = this.buff[this.currOfst];
			if ((c & 0x80) == 0)
			{
				this.currOfst += 1;
				return (char)c;
			}
			else if ((c & 0xe0) == 0xc0)
			{
				if (this.buffSize - this.currOfst >= 2)
				{
					ret = ((int)(c & 0x1f) << 6) | (int)(this.buff[this.currOfst + 1] & 0x3f);
					this.currOfst += 2;
					return (char)ret;
				}
			}
			else if ((c & 0xf0) == 0xe0)
			{
				if (this.buffSize - this.currOfst >= 3)
				{
					ret = ((int)(c & 0x0f) << 12) | ((int)(this.buff[this.currOfst + 1] & 0x3f) << 6) | (int)(this.buff[this.currOfst + 2] & 0x3f);
					this.currOfst += 3;
					return (char)ret;
				}
			}
			else if ((c & 0xf8) == 0xf0)
			{
				if (this.buffSize - this.currOfst >= 4)
				{
					ret = ((int)(c & 0x7) << 18) | ((int)(this.buff[this.currOfst + 1] & 0x3f) << 12) | ((int)(this.buff[this.currOfst + 2] & 0x3f) << 6) | (int)(this.buff[this.currOfst + 3] & 0x3f);
					this.currOfst += 4;
					return (char)ret;
				}
			}
			else if ((c & 0xfc) == 0xf8)
			{
				if (this.buffSize - this.currOfst >= 5)
				{
					ret = ((int)(c & 0x3) << 24) | ((int)(this.buff[this.currOfst + 1] & 0x3f) << 18) | ((int)(this.buff[this.currOfst + 2] & 0x3f) << 12) | ((int)(this.buff[this.currOfst + 3] & 0x3f) << 6) | (int)(this.buff[this.currOfst + 4] & 0x3f);
					this.currOfst += 5;
					return (char)ret;
				}
			}
			else
			{
				if (this.buffSize - this.currOfst >= 6)
				{
					ret = ((int)(c & 0x1) << 30) | ((int)(this.buff[this.currOfst + 1] & 0x3f) << 24) | ((int)(this.buff[this.currOfst + 2] & 0x3f) << 18) | ((int)(this.buff[this.currOfst + 3] & 0x3f) << 12) | ((int)(this.buff[this.currOfst + 4] & 0x3f) << 6) | (int)(this.buff[this.currOfst + 5] & 0x3f);
					this.currOfst += 6;
					return (char)ret;
				}
			}
		}
		return 0;
	}

	public boolean readLine(StringBuilder sb, int maxByteCnt)
	{
		if (this.currOfst >= this.buffSize)
		{
			this.fillBuffer();
			if (this.currOfst >= this.buffSize)
				return false;
		}
	
		this.lineBreak = LineBreakType.NONE;
		int currSize = 0;
		int writeSize = 0;
		int charSize;
		while (true)
		{
			if (currSize >= maxByteCnt)
			{
				sb.append(new String(this.buff, this.currOfst, currSize, StandardCharsets.UTF_8));
				this.currOfst += currSize;
				return true;
			}
			if (this.currOfst + currSize >= this.buffSize)
			{
				if (currSize > 0)
				{
					sb.append(new String(this.buff, this.currOfst, currSize, StandardCharsets.UTF_8));
					this.currOfst += currSize;
					maxByteCnt -= currSize;
					writeSize += currSize;
					currSize = 0;
				}
				this.fillBuffer();
				if (this.currOfst >= this.buffSize)
				{
					return true;
				}
			}
	
			byte c = this.buff[this.currOfst + currSize];
			if ((c & 0x80) == 0)
			{
				if (c == 10)
				{
					sb.append(new String(this.buff, this.currOfst, currSize, StandardCharsets.UTF_8));
					this.currOfst += currSize + 1;
					this.lineBreak = LineBreakType.LF;
					return true;
				}
				else if (c == 13)
				{
					sb.append(new String(this.buff, this.currOfst, currSize, StandardCharsets.UTF_8));
					this.currOfst += currSize + 1;
					if (this.currOfst < this.buffSize && this.buff[this.currOfst] == 10)
					{
						this.lineBreak = LineBreakType.CRLF;
						this.currOfst += 1;
					}
					else
					{
						this.lineBreak = LineBreakType.CR;
					}
					return true;
				}
				currSize += 1;
			}
			else
			{
				if ((c & 0xe0) == 0xc0)
				{	
					charSize = 2;
				}
				else if ((c & 0xf0) == 0xe0)
				{
					charSize = 3;
				}
				else if ((c & 0xf8) == 0xf0)
				{
					charSize = 4;
				}
				else if ((c & 0xfc) == 0xf8)
				{
					charSize = 5;
				}
				else
				{
					charSize = 6;
				}
	
				if (maxByteCnt - currSize < charSize)
				{
					sb.append(new String(this.buff, this.currOfst, currSize, StandardCharsets.UTF_8));
					this.currOfst += currSize;
					return true;
				}
				else if (this.buffSize - this.currOfst < currSize + charSize)
				{
					if (currSize > 0)
					{
						sb.append(new String(this.buff, this.currOfst, currSize, StandardCharsets.UTF_8));
						this.currOfst += currSize;
						maxByteCnt -= currSize;
						writeSize += currSize;
						currSize = 0;
					}
					this.fillBuffer();
					if (this.buffSize - this.currOfst < currSize + charSize)
					{
						return writeSize > 0;
					}
				}
				currSize += charSize;
			}
		}
	}

	public int readLine(char []u8buff, int ofst, int maxByteCnt)
	{
		if (this.currOfst >= this.buffSize)
		{
			this.fillBuffer();
			if (this.currOfst >= this.buffSize)
				return 0;
		}
	
		this.lineBreak = LineBreakType.NONE;
		int currSize = 0;
		int writeSize = 0;
		int charSize;
		while (true)
		{
			if (currSize >= maxByteCnt)
			{
				ofst = StringUtil.concat(u8buff, ofst, new String(this.buff, this.currOfst, currSize, StandardCharsets.UTF_8));
				this.currOfst += currSize;
				return ofst;
			}
			if (this.currOfst + currSize >= this.buffSize)
			{
				if (currSize > 0)
				{
					ofst = StringUtil.concat(u8buff, ofst, new String(this.buff, this.currOfst, currSize, StandardCharsets.UTF_8));
					this.currOfst += currSize;
					maxByteCnt -= currSize;
					writeSize += currSize;
					currSize = 0;
				}
				this.fillBuffer();
				if (this.currOfst >= this.buffSize)
					return ofst;
			}
	
			byte c = this.buff[this.currOfst + currSize];
			if ((c & 0x80) == 0)
			{
				if (c == 10)
				{
					ofst = StringUtil.concat(u8buff, ofst, new String(this.buff, this.currOfst, currSize, StandardCharsets.UTF_8));
					this.currOfst += currSize + 1;
					this.lineBreak = LineBreakType.LF;
					return ofst;
				}
				else if (c == 13)
				{
					ofst = StringUtil.concat(u8buff, ofst, new String(this.buff, this.currOfst, currSize, StandardCharsets.UTF_8));
					this.currOfst += currSize + 1;
					if (this.currOfst < this.buffSize && this.buff[this.currOfst] == 10)
					{
						this.lineBreak = LineBreakType.CRLF;
						this.currOfst += 1;
					}
					else
					{
						this.lineBreak = LineBreakType.CR;
					}
					return ofst;
				}
				currSize += 1;
			}
			else
			{
				if ((c & 0xe0) == 0xc0)
				{	
					charSize = 2;
				}
				else if ((c & 0xf0) == 0xe0)
				{
					charSize = 3;
				}
				else if ((c & 0xf8) == 0xf0)
				{
					charSize = 4;
				}
				else if ((c & 0xfc) == 0xf8)
				{
					charSize = 5;
				}
				else
				{
					charSize = 6;
				}
	
				if (maxByteCnt - currSize < charSize)
				{
					ofst = StringUtil.concat(u8buff, ofst, new String(this.buff, this.currOfst, currSize, StandardCharsets.UTF_8));
					this.currOfst += currSize;
					return ofst;
				}
				else if (this.buffSize - this.currOfst < currSize + charSize)
				{
					if (currSize > 0)
					{
						ofst = StringUtil.concat(u8buff, ofst, new String(this.buff, this.currOfst, currSize, StandardCharsets.UTF_8));
						this.currOfst += currSize;
						maxByteCnt -= currSize;
						writeSize += currSize;
						currSize = 0;
					}
					this.fillBuffer();
					if (this.buffSize - this.currOfst < currSize + charSize)
					{
						if (writeSize <= 0)
							return 0;
						return ofst;
					}
				}
				currSize += charSize;
			}
		}
	}

	public int getLastLineBreak(char []buff, int ofst)
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
		return ofst;
	}

	public int getLastLineBreak(byte []buff, int ofst)
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
		return ofst;
	}

	public boolean getLastLineBreak(StringBuilder sb)
	{
		if (this.lineBreak == LineBreakType.CR)
		{
			sb.append('\r');
		}
		else if (this.lineBreak == LineBreakType.LF)
		{
			sb.append('\n');
		}
		else if (this.lineBreak == LineBreakType.CRLF)
		{
			sb.append("\r\n");
		}
		return true;
	}

	public boolean readToEnd(StringBuilder sb)
	{
		boolean succ = false;
		while (this.readLine(sb, BUFFSIZE))
		{
			succ = this.getLastLineBreak(sb);
		}
		return succ;
	}
}
