package org.sswr.util.data;

import jakarta.annotation.Nullable;

public class Encoding {
	private int codePage;
//	private boolean lastHigh;

	public Encoding(int codePage)
	{
		this.codePage = codePage;
		if (codePage == 0)
		{
			this.codePage = 65001;
		}
//		this.lastHigh = false;
	}

	public Encoding()
	{
		this.codePage = 65001;
//		this.lastHigh = false;
	}

	public void setCodePage(int codePage)
	{
		this.codePage = codePage;
	}

	public int countUTF8Chars(byte[] bytes, int ofst, int byteSize)
	{
		if (this.codePage == 65001)
		{
			return byteSize;
		}
		else if (this.codePage == 1200)
		{
			int byteCnt = 0;
			int c;
			int c2;
			byteSize = byteSize >> 1;
			while (byteSize-- > 0)
			{
				c = ByteTool.readUInt16(bytes, ofst);
				ofst += 2;
				if (c < 0x80)
					byteCnt++;
				else if (c < 0x800)
					byteCnt += 2;
				else if (byteSize > 0)
				{
					c2 = ByteTool.readUInt16(bytes, ofst);
					if (c >= 0xd800 && c < 0xdc00 && c2 >= 0xdc00 && c2 < 0xe000)
					{
						int code = (0x10000 + ((c - 0xd800) << 10) + (c2 - 0xdc00));
						ofst += 2;
						byteSize--;
						if (code < 0x200000)
						{
							byteCnt += 4;
						}
						else if (code < 0x4000000)
						{
							byteCnt += 5;
						}
						else
						{
							byteCnt += 6;
						}
					}
					else
					{
						byteCnt += 3;
					}
				}
				else
				{
					byteCnt += 3;
				}
			}
			return byteCnt;
		}
		else if (this.codePage == 1201)
		{
			int byteCnt = 0;
			int c;
			int c2;
			byteSize = byteSize >> 1;
			while (byteSize-- > 0)
			{
				c = ByteTool.readMUInt16(bytes, ofst);
				ofst += 2;
				if (c < 0x80)
					byteCnt++;
				else if (c < 0x800)
					byteCnt += 2;
				else if (byteSize > 0)
				{
					c2 = ByteTool.readMUInt16(bytes, ofst);
					if (c >= 0xd800 && c < 0xdc00 && c2 >= 0xdc00 && c2 < 0xe000)
					{
						int code = 0x10000 + ((int)(c - 0xd800) << 10) + (int)(c2 - 0xdc00);
						ofst += 2;
						byteSize--;
						if (code < 0x200000)
						{
							byteCnt += 4;
						}
						else if (code < 0x4000000)
						{
							byteCnt += 5;
						}
						else
						{
							byteCnt += 6;
						}
					}
					else
					{
						byteCnt += 3;
					}
				}
				else
				{
					byteCnt += 3;
				}
			}
			return byteCnt;
		}
		else
		{
			return byteSize;
		}
	}

	public int utf8FromBytes(byte[] buff, int buffOfst, byte[] bytes, int byteOfst, int byteSize, @Nullable SharedInt byteConv)
	{
		if (this.codePage == 65001)
		{
			if (byteConv != null) byteConv.value = byteSize;
			ByteTool.copyArray(buff, buffOfst, bytes, byteOfst, byteSize);
			return buffOfst + byteSize;
		}
		else if (this.codePage == 1200)
		{
			int dest = buffOfst;
			int c;
			int c2;
			byteSize = byteSize >> 1;
			int retSize = byteSize << 1;
			while (byteSize-- > 0)
			{
				c = ByteTool.readUInt16(bytes, byteOfst);
				byteOfst += 2;
				if (c < 0x80)
				{
					buff[dest++] = (byte)c;
				}
				else if (c < 0x800)
				{
					buff[dest++] = (byte)(0xc0 | (c >> 6));
					buff[dest++] = (byte)(0x80 | (c & 0x3f));
				}
				else if (byteSize > 0)
				{
					c2 = ByteTool.readUInt16(bytes, byteOfst);
					if (c >= 0xd800 && c < 0xdc00 && c2 >= 0xdc00 && c2 < 0xe000)
					{
						int code = 0x10000 + ((int)(c - 0xd800) << 10) + (int)(c2 - 0xdc00);
						byteOfst += 2;
						byteSize--;
						if (code < 0x200000)
						{
							buff[dest++] = (byte)(0xf0 | (code >> 18));
							buff[dest++] = (byte)(0x80 | ((code >> 12) & 0x3f));
							buff[dest++] = (byte)(0x80 | ((code >> 6) & 0x3f));
							buff[dest++] = (byte)(0x80 | (code & 0x3f));
						}
						else if (code < 0x4000000)
						{
							buff[dest++] = (byte)(0xf8 | (code >> 24));
							buff[dest++] = (byte)(0x80 | ((code >> 18) & 0x3f));
							buff[dest++] = (byte)(0x80 | ((code >> 12) & 0x3f));
							buff[dest++] = (byte)(0x80 | ((code >> 6) & 0x3f));
							buff[dest++] = (byte)(0x80 | (code & 0x3f));
						}
						else
						{
							buff[dest++] = (byte)(0xfc | (code >> 30));
							buff[dest++] = (byte)(0x80 | ((code >> 24) & 0x3f));
							buff[dest++] = (byte)(0x80 | ((code >> 18) & 0x3f));
							buff[dest++] = (byte)(0x80 | ((code >> 12) & 0x3f));
							buff[dest++] = (byte)(0x80 | ((code >> 6) & 0x3f));
							buff[dest++] = (byte)(0x80 | (code & 0x3f));
						}
					}
					else
					{
						buff[dest++] = (byte)(0xe0 | (c >> 12));
						buff[dest++] = (byte)(0x80 | ((c >> 6) & 0x3f));
						buff[dest++] = (byte)(0x80 | (c & 0x3f));
					}
				}
				else
				{
					buff[dest++] = (byte)(0xe0 | (c >> 12));
					buff[dest++] = (byte)(0x80 | ((c >> 6) & 0x3f));
					buff[dest++] = (byte)(0x80 | (c & 0x3f));
				}
			}
			buff[dest] = 0;
			if (byteConv != null) byteConv.value = retSize;
			return dest;
		}
		else if (this.codePage == 1201)
		{
			int dest = buffOfst;
			int c;
			int c2;
			byteSize = byteSize >> 1;
			int retSize = byteSize << 1;
			while (byteSize-- > 0)
			{
				c = ByteTool.readMUInt16(bytes, byteOfst);
				byteOfst += 2;
				if (c < 0x80)
				{
					buff[dest++] = (byte)c;
				}
				else if (c < 0x800)
				{
					buff[dest++] = (byte)(0xc0 | (c >> 6));
					buff[dest++] = (byte)(0x80 | (c & 0x3f));
				}
				else if (byteSize > 0)
				{
					c2 = ByteTool.readMUInt16(bytes, byteOfst);
					if (c >= 0xd800 && c < 0xdc00 && c2 >= 0xdc00 && c2 < 0xe000)
					{
						int code = 0x10000 + ((int)(c - 0xd800) << 10) + (int)(c2 - 0xdc00);
						byteOfst += 2;
						byteSize--;
						if (code < 0x200000)
						{
							buff[dest++] = (byte)(0xf0 | (code >> 18));
							buff[dest++] = (byte)(0x80 | ((code >> 12) & 0x3f));
							buff[dest++] = (byte)(0x80 | ((code >> 6) & 0x3f));
							buff[dest++] = (byte)(0x80 | (code & 0x3f));
						}
						else if (code < 0x4000000)
						{
							buff[dest++] = (byte)(0xf8 | (code >> 24));
							buff[dest++] = (byte)(0x80 | ((code >> 18) & 0x3f));
							buff[dest++] = (byte)(0x80 | ((code >> 12) & 0x3f));
							buff[dest++] = (byte)(0x80 | ((code >> 6) & 0x3f));
							buff[dest++] = (byte)(0x80 | (code & 0x3f));
						}
						else
						{
							buff[dest++] = (byte)(0xfc | (code >> 30));
							buff[dest++] = (byte)(0x80 | ((code >> 24) & 0x3f));
							buff[dest++] = (byte)(0x80 | ((code >> 18) & 0x3f));
							buff[dest++] = (byte)(0x80 | ((code >> 12) & 0x3f));
							buff[dest++] = (byte)(0x80 | ((code >> 6) & 0x3f));
							buff[dest++] = (byte)(0x80 | (code & 0x3f));
						}
					}
					else
					{
						buff[dest++] = (byte)(0xe0 | (c >> 12));
						buff[dest++] = (byte)(0x80 | ((c >> 6) & 0x3f));
						buff[dest++] = (byte)(0x80 | (c & 0x3f));
					}
				}
				else
				{
					buff[dest++] = (byte)(0xe0 | (c >> 12));
					buff[dest++] = (byte)(0x80 | ((c >> 6) & 0x3f));
					buff[dest++] = (byte)(0x80 | (c & 0x3f));
				}
			}
			buff[dest] = 0;
			if (byteConv != null) byteConv.value = retSize;
			return dest;
		}
		else
		{
			if (byteConv != null) byteConv.value = byteSize;
			ByteTool.copyArray(buff, buffOfst, bytes, byteOfst, byteSize);
			return buffOfst + byteSize;
		}
	}
}
