package org.sswr.util.data;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Arrays;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class BinaryParser
{
	private byte[] buff;
	private int currOfst;
	private boolean error;

	public BinaryParser(byte[] buff)
	{
		this.buff = buff;
		this.currOfst = 0;
		this.error = false;
	}

	public int nextI32()
	{
		if (this.error)
			return 0;
		if (this.currOfst + 4 > this.buff.length)
		{
			this.error = true;
			return 0;
		}
		int ret = ByteTool.readInt32(this.buff, this.currOfst);
		this.currOfst += 4;
		return ret;
	}

	public int nextU32()
	{
		if (this.error)
			return 0;
		if (this.currOfst + 4 > this.buff.length)
		{
			this.error = true;
			return 0;
		}
		int ret = ByteTool.readInt32(this.buff, this.currOfst);
		this.currOfst += 4;
		return ret;
	}

	@Nullable
	public Integer nextNI32()
	{
		if (this.error)
			return null;
		if (this.currOfst + 4 > this.buff.length)
		{
			this.error = true;
			return null;
		}
		int ret = ByteTool.readInt32(this.buff, this.currOfst);
		this.currOfst += 4;
		if (ret == 0x80000000)
			return null;
		return ret;
	}

	public long nextI64()
	{
		if (this.error)
			return 0;
		if (this.currOfst + 8 > this.buff.length)
		{
			this.error = true;
			return 0;
		}
		long ret = ByteTool.readInt64(this.buff, this.currOfst);
		this.currOfst += 8;
		return ret;
	}

	public double nextF64()
	{
		if (this.error)
			return 0;
		if (this.currOfst + 8 > this.buff.length)
		{
			this.error = true;
			return 0;
		}
		double ret = ByteTool.readDouble(this.buff, this.currOfst);
		this.currOfst += 8;
		return ret;
	}

	@Nonnull
	public String nextStrNN()
	{
		String nns;
		if ((nns = this.nextStr()) != null)
		{
			return nns;
		}
		return "";
	}

	@Nullable
	public String nextStr()
	{
		if (this.error)
			return null;
		if (this.currOfst + 2 > this.buff.length)
		{
			this.error = true;
			return null;
		}
		int b1 = ByteTool.readUInt16(this.buff, this.currOfst);
		if (b1 == 65535)
		{
			this.currOfst += 2;
			return null;
		}
		if (b1 != 65534)
		{
			if (this.currOfst + 2 + b1 > this.buff.length)
			{
				this.error = true;
				return null;
			}
			String s = new String(this.buff, this.currOfst + 2, b1, StandardCharsets.UTF_8);
			this.currOfst += b1 + 2;
			return s;
		}
		if (this.currOfst + 6 > this.buff.length)
		{
			this.error = true;
			return null;
		}
		int len = ByteTool.readInt32(this.buff, this.currOfst + 2) + 65534;
		if (this.currOfst + 6 + len > this.buff.length)
		{
			this.error = true;
			return null;
		}
		String s = new String(this.buff, this.currOfst + 6, len, StandardCharsets.UTF_8);
		this.currOfst += len + 6;
		return s;
	}

	public char nextChar()
	{
		if (this.error)
			return 0;
		if (this.currOfst + 1 > this.buff.length)
		{
			this.error = true;
			return 0;
		}
		char c = (char)this.buff[this.currOfst];
		this.currOfst += 1;
		return c;
	}

	public boolean nextBool()
	{
		if (this.error)
			return false;
		if (this.currOfst + 1 > this.buff.length)
		{
			this.error = true;
			return false;
		}
		boolean b;
		if (this.buff[this.currOfst] == 0)
			b = false;
		else if ((this.buff[this.currOfst] & 0xff) == 0xff)
			b = true;
		else
		{
			this.error = true;
			return false;
		}
		this.currOfst += 1;
		return b;
	}

	@Nullable
	public ZonedDateTime nextTS()
	{
		if (this.error)
			return null;
		if (this.currOfst + 13 > this.buff.length)
		{
			this.error = true;
			return null;
		}
		long seconds = ByteTool.readInt64(this.buff, this.currOfst);
		int nanosec = ByteTool.readInt32(this.buff, this.currOfst + 8);
		byte tzQhr = this.buff[this.currOfst + 12];
		ZonedDateTime ret;
		if (seconds == 0 && nanosec == 0 && tzQhr == 127)
		{
			ret = null;			
		}
		else
		{
			Instant inst = DateTimeUtil.newInstant(seconds, nanosec);
			try
			{
				ret = DateTimeUtil.newZonedDateTime(inst, tzQhr);
			}
			catch (Exception ex)
			{
				this.error = true;
				ex.printStackTrace();
				return null;
			}
		}
		this.currOfst += 13;
		return ret;
	}

	@Nullable
	public LocalDate nextDate()
	{
		if (this.error)
			return null;
		if (this.currOfst + 8 > this.buff.length)
		{
			this.error = true;
			return null;
		}
		LocalDate ret = DateTimeUtil.newLocalDate(ByteTool.readInt64(this.buff, this.currOfst));
		this.currOfst += 8;
		return ret;
	}

	@Nullable
	public InetAddress nextIPAddr()
	{
		if (this.error)
			return null;
		if (this.currOfst + 1 > this.buff.length)
		{
			this.error = true;
			return null;
		}
		try
		{
			if (this.buff[this.currOfst] == 1) //Net::AddrType::IPv4)
			{
				if (this.currOfst + 5 > this.buff.length)
				{
					this.error = true;
					return null;
				}
				InetAddress addr = Inet4Address.getByAddress(Arrays.copyOfRange(this.buff, this.currOfst + 1, this.currOfst + 5));
				this.currOfst += 5;
				return addr;
			}
			else if (this.buff[this.currOfst] == 2) //Net::AddrType::IPv6)
			{
				if (this.currOfst + 21 > this.buff.length)
				{
					this.error = true;
					return null;
				}
				InetAddress addr = Inet4Address.getByAddress(Arrays.copyOfRange(this.buff, this.currOfst + 1, this.currOfst + 17));
				this.currOfst += 21;
				return addr;
			}
			else if (this.buff[this.currOfst] == 0)//Net::AddrType::Unknown)
			{
				this.currOfst += 1;
				return null;
			}
		}
		catch (UnknownHostException ex)
		{
			ex.printStackTrace();
		}
		this.error = true;
		return null;
	}

	public byte[] nextBArr()
	{
		if (this.error)
			return null;
		if (this.currOfst + 1 > this.buff.length)
		{
			this.error = true;
			return null;
		}
		int b = (this.buff[this.currOfst] & 0xff);
		int size;
		if (b < 0x80)
		{
			size = b;
			this.currOfst += 1;
		}
		else if ((b & 0xe0) == 0xc0)
		{
			if (this.currOfst + 2 > this.buff.length)
			{
				this.error = true;
				return null;
			}
			size = (((b & 0x1f) << 6) | (this.buff[this.currOfst + 1] & 0x3f));
			this.currOfst += 2;
		}
		else if ((b & 0xf0) == 0xe0)
		{
			if (this.currOfst + 3 > this.buff.length)
			{
				this.error = true;
				return null;
			}
			size = (((b & 0x0f) << 12) | ((this.buff[this.currOfst + 1] & 0x3f) << 6) | (this.buff[this.currOfst + 2] & 0x3f));
			this.currOfst += 3;
		}
		else if ((b & 0xf8) == 0xf0)
		{
			if (this.currOfst + 4 > this.buff.length)
			{
				this.error = true;
				return null;
			}
			size = (((b & 0x7) << 18) | ((this.buff[this.currOfst + 1] & 0x3f) << 12) | ((this.buff[this.currOfst + 2] & 0x3f) << 6) | (this.buff[this.currOfst + 3] & 0x3f));
			this.currOfst += 4;
		}
		else if ((b & 0xfc) == 0xf8)
		{
			if (this.currOfst + 5 > this.buff.length)
			{
				this.error = true;
				return null;
			}
			size = (((b & 0x3) << 24) | ((this.buff[this.currOfst + 1] & 0x3f) << 18) | ((this.buff[this.currOfst + 2] & 0x3f) << 12) | ((this.buff[this.currOfst + 3] & 0x3f) << 6) | (this.buff[this.currOfst + 4] & 0x3f));
			this.currOfst += 5;
		}
		else if ((b & 0xfe) == 0xfc)
		{
			if (this.currOfst + 6 > this.buff.length)
			{
				this.error = true;
				return null;
			}
			size = (((b & 0x1) << 30) | ((this.buff[this.currOfst + 1] & 0x3f) << 24) | ((this.buff[this.currOfst + 2] & 0x3f) << 18) | ((this.buff[this.currOfst + 3] & 0x3f) << 12) | ((this.buff[this.currOfst + 4] & 0x3f) << 6) | (this.buff[this.currOfst + 5] & 0x3f));
			this.currOfst += 6;
		}
		else
		{
			this.error = true;
			return null;
		}
		if (this.currOfst + size > this.buff.length)
		{
			this.error = true;
			return null;
		}
		byte[] arr = Arrays.copyOfRange(this.buff, this.currOfst, this.currOfst + size);
		this.currOfst += size;
		return arr;
	}

	public boolean hasError()
	{
		return this.error;
	}
}
