package org.sswr.util.net;

import java.nio.charset.StandardCharsets;

import org.sswr.util.data.ByteTool;
import org.sswr.util.data.SharedInt;

public class ASN1PDUBuilder
{
	private int seqOffset[];
	private int currLev;
	private int buffSize;
	private byte[] buff;
	private int currOffset;

	public ASN1PDUBuilder()
	{
		this.seqOffset = new int[16];
		this.currLev = 0;
		this.buffSize = 128;
		this.buff = new byte[buffSize];
		this.currOffset = 0;
	}

	public void allocateSize(int size)
	{
		if (this.currOffset + size > this.buffSize)
		{
			while (this.currOffset + size > this.buffSize)
			{
				this.buffSize = this.buffSize << 1;
			}
			byte[] newBuff = new byte[this.buffSize];
			ByteTool.copyArray(newBuff, 0, this.buff, 0, this.currOffset);
			this.buff = newBuff;
		}
	}

	public void beginOther(byte type)
	{
		this.allocateSize(2);
		this.buff[this.currOffset] = type;
		this.currOffset += 2;
		this.seqOffset[this.currLev] = this.currOffset;
		this.currLev++;
	}

	public void beginSequence()
	{
		this.beginOther((byte)0x30);
	}

	public void beginSet()
	{
		this.beginOther((byte)0x31);
	}

	public void beginContentSpecific(int n)
	{
		this.beginOther((byte)(0xA0 + n));
	}

	public void endLevel()
	{
		if (this.currLev > 0)
		{
			int seqOffset;
			int seqLen;
			this.currLev--;
			seqOffset = this.seqOffset[this.currLev];
			seqLen = this.currOffset - seqOffset;
			if (seqLen < 128)
			{
				this.buff[seqOffset - 1] = (byte)seqLen;
			}
			else if (seqLen < 256)
			{
				this.allocateSize(1);
				ByteTool.copyArray(this.buff, seqOffset + 1, this.buff, seqOffset, seqLen);
				this.buff[seqOffset - 1] = (byte)0x81;
				this.buff[seqOffset] = (byte)seqLen;
				this.currOffset += 1;
			}
			else if (seqLen < 65536)
			{
				this.allocateSize(2);
				ByteTool.copyArray(this.buff, seqOffset + 2, this.buff, seqOffset, seqLen);
				this.buff[seqOffset - 1] = (byte)0x82;
				ByteTool.writeMInt16(this.buff, seqOffset, seqLen);
				this.currOffset += 2;
			}
			else
			{
				this.allocateSize(3);
				ByteTool.copyArray(this.buff, seqOffset + 3, this.buff, seqOffset, seqLen);
				this.buff[seqOffset - 1] = (byte)0x83;
				ByteTool.writeMInt24(this.buff, seqOffset, seqLen);
				this.currOffset += 3;
			}
		}
	}

	public void endAll()
	{
		while (this.currLev > 0)
		{
			this.endLevel();
		}
	}
	
	public void appendBool(boolean v)
	{
		this.allocateSize(3);
		this.buff[this.currOffset] = 1;
		this.buff[this.currOffset + 1] = 1;
		this.buff[this.currOffset + 2] = (byte)(v?1:0);
		this.currOffset += 3;
	}

	public void appendInt32(int v)
	{
		if (v < 128 && v >= -128)
		{
			this.allocateSize(3);
			this.buff[this.currOffset] = 2;
			this.buff[this.currOffset + 1] = 1;
			this.buff[this.currOffset + 2] = (byte)v;
			this.currOffset += 3;
		}
		else if (v < 32768 && v >= -32768)
		{
			this.allocateSize(4);
			this.buff[this.currOffset] = 2;
			this.buff[this.currOffset + 1] = 2;
			ByteTool.writeMInt16(this.buff, this.currOffset + 2, v);
			this.currOffset += 4;
		}
		else if (v < 8388608 && v >= -8388608)
		{
			this.allocateSize(5);
			this.buff[this.currOffset] = 2;
			this.buff[this.currOffset + 1] = 3;
			ByteTool.writeMInt24(this.buff, this.currOffset + 2, v);
			this.currOffset += 5;
		}
		else
		{
			this.allocateSize(6);
			this.buff[this.currOffset] = 2;
			this.buff[this.currOffset + 1] = 4;
			ByteTool.writeMInt32(this.buff, this.currOffset + 2, v);
			this.currOffset += 6;
		}
	}

	public void appendUInt32(int v)
	{
		if (v < 256)
		{
			this.allocateSize(3);
			this.buff[this.currOffset] = 2;
			this.buff[this.currOffset + 1] = 1;
			this.buff[this.currOffset + 2] = (byte)v;
			this.currOffset += 3;
		}
		else if (v < 65536)
		{
			this.allocateSize(4);
			this.buff[this.currOffset] = 2;
			this.buff[this.currOffset + 1] = 2;
			ByteTool.writeMInt16(this.buff, this.currOffset + 2, v);
			this.currOffset += 4;
		}
		else if (v < 0x1000000)
		{
			this.allocateSize(5);
			this.buff[this.currOffset] = 2;
			this.buff[this.currOffset + 1] = 3;
			ByteTool.writeMInt24(this.buff, this.currOffset + 2, v);
			this.currOffset += 5;
		}
		else
		{
			this.allocateSize(6);
			this.buff[this.currOffset] = 2;
			this.buff[this.currOffset + 1] = 4;
			ByteTool.writeMInt32(this.buff, this.currOffset + 2, v);
			this.currOffset += 6;
		}
	}

	public void appendString(String s)
	{
		if (s == null)
		{
			this.allocateSize(2);
			this.buff[this.currOffset] = 4;
			this.buff[this.currOffset + 1] = 0;
			this.currOffset += 2;
			return;
		}
		byte[] sbytes = s.getBytes(StandardCharsets.UTF_8);
		int len = sbytes.length;
		if (len < 128)
		{
			this.allocateSize(len + 2);
			this.buff[this.currOffset] = 4;
			this.buff[this.currOffset + 1] = (byte)len;
			ByteTool.copyArray(this.buff, this.currOffset + 2, sbytes, 0, len);
			this.currOffset += len + 2;
		}
		else if (len < 256)
		{
			this.allocateSize(len + 3);
			this.buff[this.currOffset] = 4;
			this.buff[this.currOffset + 1] = (byte)0x81;
			this.buff[this.currOffset + 2] = (byte)len;
			ByteTool.copyArray(this.buff, this.currOffset + 3, sbytes, 0, len);
			this.currOffset += len + 3;
		}
		else if (len < 65536)
		{
			this.allocateSize(len + 4);
			this.buff[this.currOffset] = 4;
			this.buff[this.currOffset + 1] = (byte)0x82;
			ByteTool.writeMInt16(this.buff, this.currOffset + 2, len);
			ByteTool.copyArray(this.buff, this.currOffset + 4, sbytes, 0, len);
			this.currOffset += len + 4;
		}
		else
		{
			this.allocateSize(len + 5);
			this.buff[this.currOffset] = 4;
			this.buff[this.currOffset + 1] = (byte)0x83;
			ByteTool.writeMInt24(this.buff, this.currOffset + 2, len);
			ByteTool.copyArray(this.buff, this.currOffset + 5, sbytes, 0, len);
			this.currOffset += len + 5;
		}
	}

	public void appendNull()
	{
		this.allocateSize(2);
		this.buff[this.currOffset] = 5;
		this.buff[this.currOffset + 1] = 0;
		this.currOffset += 2;
	}

	public void appendOID(byte[] oid, int len)
	{
		this.allocateSize(len + 2);
		this.buff[this.currOffset] = 6;
		this.buff[this.currOffset + 1] = (byte)len;
		ByteTool.copyArray(this.buff, this.currOffset + 2, oid, 0, len);
		this.currOffset += len + 2;
	}

	public void appendChoice(int v)
	{
		if (v < 256)
		{
			this.allocateSize(3);
			this.buff[this.currOffset] = 10;
			this.buff[this.currOffset + 1] = 1;
			this.buff[this.currOffset + 2] = (byte)v;
			this.currOffset += 3;
		}
		else if (v < 65536)
		{
			this.allocateSize(4);
			this.buff[this.currOffset] = 10;
			this.buff[this.currOffset + 1] = 2;
			ByteTool.writeMInt16(this.buff, this.currOffset + 2, v);
			this.currOffset += 4;
		}
		else if (v < 0x1000000)
		{
			this.allocateSize(5);
			this.buff[this.currOffset] = 10;
			this.buff[this.currOffset + 1] = 3;
			ByteTool.writeMInt24(this.buff, this.currOffset + 2, v);
			this.currOffset += 5;
		}
		else
		{
			this.allocateSize(6);
			this.buff[this.currOffset] = 10;
			this.buff[this.currOffset + 1] = 4;
			ByteTool.writeMInt32(this.buff, this.currOffset + 2, v);
			this.currOffset += 6;
		}
	}

	public void appendOther(byte type, byte[] buff, int buffOfst, int buffSize)
	{
		if (buffSize == 0)
		{
			this.allocateSize(2);
			this.buff[this.currOffset] = type;
			this.buff[this.currOffset + 1] = 0;
			this.currOffset += 2;
		}
		else if (buffSize < 128)
		{
			this.allocateSize(buffSize + 2);
			this.buff[this.currOffset] = type;
			this.buff[this.currOffset + 1] = (byte)buffSize;
			ByteTool.copyArray(this.buff, this.currOffset + 2, buff, 0, buffSize);
			this.currOffset += buffSize + 2;
		}
		else if (buffSize < 256)
		{
			this.allocateSize(buffSize + 3);
			this.buff[this.currOffset] = type;
			this.buff[this.currOffset + 1] = (byte)0x81;
			this.buff[this.currOffset + 2] = (byte)buffSize;
			ByteTool.copyArray(this.buff, this.currOffset + 3, buff, 0, buffSize);
			this.currOffset += buffSize + 3;
		}
		else if (buffSize < 65536)
		{
			this.allocateSize(buffSize + 4);
			this.buff[this.currOffset] = type;
			this.buff[this.currOffset + 1] = (byte)0x82;
			ByteTool.writeMInt16(this.buff, this.currOffset + 2, buffSize);
			ByteTool.copyArray(this.buff, this.currOffset + 4, buff, 0, buffSize);
			this.currOffset += buffSize + 4;
		}
		else
		{
			this.allocateSize(buffSize + 5);
			this.buff[this.currOffset] = type;
			this.buff[this.currOffset + 1] = (byte)0x83;
			ByteTool.writeMInt24(this.buff, this.currOffset + 2, buffSize);
			ByteTool.copyArray(this.buff, this.currOffset + 5, buff, 0, buffSize);
			this.currOffset += buffSize + 5;
		}
	}

	public byte[] getBuff(SharedInt buffSize)
	{
		buffSize.value = this.currOffset;
		return this.buff;
	}	
}
