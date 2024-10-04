package org.sswr.util.data;

import java.io.ByteArrayOutputStream;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneOffset;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class BinaryBuilder
{
	private ByteArrayOutputStream baos;

	public BinaryBuilder()
	{
		this.baos = new ByteArrayOutputStream();
	}
	
	public BinaryBuilder(int initSize)
	{
		this.baos = new ByteArrayOutputStream(initSize);
	}
	
	public void appendI32(int val)
	{
		byte[] buff = new byte[4];
		ByteTool.writeInt32(buff, 0, val);
		this.baos.write(buff, 0, buff.length);
	}
	
	public void appendU32(int val)
	{
		byte[] buff = new byte[4];
		ByteTool.writeInt32(buff, 0, val);
		this.baos.write(buff, 0, buff.length);
	}
	
	public void appendNI32(@Nullable Integer val)
	{
		byte[] buff = new byte[4];
		if (val == null)
			ByteTool.writeInt32(buff, 0, 0x80000000);
		else
			ByteTool.writeInt32(buff, 0, val.intValue());
		this.baos.write(buff, 0, buff.length);
	}
	
	public void appendI64(long val)
	{
		byte[] buff = new byte[8];
		ByteTool.writeInt64(buff, 0, val);
		this.baos.write(buff, 0, buff.length);
	}
	
	public void appendF64(double val)
	{
		byte[] buff = new byte[8];
		ByteTool.writeDouble(buff, 0, val);
		this.baos.write(buff, 0, buff.length);
	}
	
	public void appendStr(@Nullable String s)
	{
		byte[] buff = new byte[6];
		if (s == null)
		{
			ByteTool.writeInt32(buff, 0, -1);
			this.baos.write(buff, 0, 2);
			return;
		}
		byte[] sarr = s.getBytes(StandardCharsets.UTF_8);
		if (sarr.length < 65534)
		{
			ByteTool.writeInt16(buff, 0, sarr.length);
			this.baos.write(buff, 0, 2);
			if (sarr.length > 0)
			{
				this.baos.write(sarr, 0, sarr.length);
			}
		}
		else
		{
			ByteTool.writeInt16(buff, 0, -2);
			ByteTool.writeInt32(buff, 2, sarr.length - 65534);
			this.baos.write(buff, 0, 6);
			this.baos.write(sarr, 0, sarr.length);
		}
	}

	public void appendChar(char c)
	{
		byte[] buff = new byte[1];
		buff[0] = (byte)c;
		this.baos.write(buff, 0, 1);
	}
	
	public void appendBool(boolean b)
	{
		byte[] buff = new byte[1];
		buff[0] = (byte)(b?0xff:0);
		this.baos.write(buff, 0, 1);
	}
	
	public void appendTS(@Nullable Timestamp ts)
	{
		byte[] buff = new byte[13];
		if (ts == null)
		{
			ByteTool.writeInt64(buff, 0, 0);
			ByteTool.writeInt32(buff, 8, 0);
			buff[12] = 127;
		}
		else
		{
			Instant inst = ts.toInstant();
			ByteTool.writeInt64(buff, 0, ts.getTime() / 1000);
			ByteTool.writeInt32(buff, 8, ts.getNanos());
			buff[12] = DateTimeUtil.getTZQhr(ZoneOffset.systemDefault().getRules().getOffset(inst));
		}
		this.baos.write(buff, 0, 13);
	}
	
	public void appendDate(@Nullable Date dat)
	{
		byte[] buff = new byte[8];
		ByteTool.writeInt64(buff, 0, DateTimeUtil.getTotalDays(dat));
		this.baos.write(buff, 0, 8);
	}
	
	public void appendIPAddr(@Nonnull InetAddress addr)
	{
		byte[] buff = new byte[1];
		if (addr instanceof Inet4Address)
		{
			Inet4Address iaddr = (Inet4Address)addr;
			byte[] addrbuff = iaddr.getAddress();
			buff[0] = 1; //Net::AddrType::IPv4
			this.baos.write(buff, 0, 1);
			this.baos.write(addrbuff, 0, addrbuff.length);
		}
		else if (addr instanceof Inet6Address)
		{
			Inet6Address iaddr = (Inet6Address)addr;
			byte[] addrbuff = iaddr.getAddress();
			buff[0] = 2; //Net::AddrType::IPv6;
			this.baos.write(buff, 0, 1);
			this.baos.write(addrbuff, 0, addrbuff.length);
			buff = new byte[20 - addrbuff.length];
			this.baos.write(buff, 0, buff.length);
		}
		else
		{
			buff[0] = 0; //Net::AddrType::Unknown;
			this.baos.write(buff, 0, 1);
		}
	}
	
	public void appendBArr(@Nonnull byte[] barr)
	{
		byte[] buff = new byte[6];
		int size = StringUtil.writeChar(buff, 0, (char)barr.length);
		this.baos.write(buff, 0, size);
		this.baos.write(barr, 0, barr.length);
	}
	
	public byte[] build()
	{
		return this.baos.toByteArray();
	}
	
}
