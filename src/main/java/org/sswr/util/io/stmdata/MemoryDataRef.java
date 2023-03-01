package org.sswr.util.io.stmdata;

import org.sswr.util.data.ByteTool;
import org.sswr.util.io.StreamData;

public class MemoryDataRef implements StreamData
{
	private byte[] data;
	private int dataOfst;
	private int dataLength;

	public MemoryDataRef(byte[] data, int dataOfst, int dataLength)
	{
		this.data = data;
		this.dataOfst = dataOfst;
		this.dataLength = dataLength;
	}

	public void close()
	{
	}

	public int getRealData(long dataOffset, int length, byte[] buffer, int buffOfst)
	{
		if (dataOffset >= this.dataLength)
		{
			return 0;
		}
		if (dataOffset + length > this.dataLength)
		{
			length = (int)(this.dataLength - dataOffset);
		}
		if (length > 0)
		{
			ByteTool.copyArray(buffer, buffOfst, this.data, (int)(dataOffset + this.dataOfst), length);
		}
		return length;
	}

	public String getFullName()
	{
		return "";
	}

	public String getShortName()
	{
		return "Memory";
	}

	public void setFullName(String fullName)
	{
	}

	public long getDataSize()
	{
		return this.dataLength;
	}

	public StreamData getPartialData(long offset, long length)
	{
		if (offset >= this.dataLength)
		{
			return new MemoryDataRef(this.data, 0, 0);
		}
		if (offset + length > this.dataLength)
		{
			length = this.dataLength - offset;
		}
		return new MemoryDataRef(this.data, (int)(this.dataOfst + offset), (int)length);
	}

	public boolean isFullFile()
	{
		return false;
	}

	public String getFullFileName()
	{
		return null;
	}

	public boolean isLoading()
	{
		return false;
	}

	public int getSeekCount()
	{
		return 0;
	}
}
