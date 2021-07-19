package org.sswr.util.crypto;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.sswr.util.data.ByteTool;
import org.sswr.util.data.textbinenc.Base32Enc;

public class HOTP extends OTP
{
	private byte[] key;
	private long counter;

	public HOTP(byte[] key, long counter)
	{
		super(6);
		this.key = key.clone();
		this.counter = counter;
	}

	public HOTP(String key, long counter)
	{
		super(6);
		this.key = new Base32Enc().decodeBin(key);
		this.counter = counter;
	}

	public long getCounter()
	{
		return this.counter;
	}

	public int nextCode()
	{
		return calcCode(this.key, this.counter++, this.nDigits);
	}

	public boolean isValid(int code)
	{
		int cnt = 0;
		int calCode;
		while (cnt < 10)
		{
			calCode = calcCode(this.key, this.counter + cnt, this.nDigits);
			if (calCode == code)
			{
				this.counter += cnt + 1;
				return true;
			}
			cnt++;
		}
		return false;
	}

	public static int calcCode(byte[] key, long counter, int nDigits)
	{
		byte[] buff = new byte[20];
		HMAC hmac;
		SHA1 hash;
		hash = new SHA1();
		hmac = new HMAC(hash, key, 0, key.length);
		ByteTool.writeMInt64(buff, 0, counter);
		hmac.calc(buff, 0, 8);
		buff = hmac.getValue();
		int v = ByteTool.readMInt32(buff, buff[buff.length - 1] & 15) & 0x7fffffff;
		if (nDigits == 6)
		{
			return v % 1000000;
		}
		else if (nDigits == 8)
		{
			return v % 100000000;
		}
		else
		{
			return v;
		}
	}

	public String genURI(String name)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("otpauth://hotp/");
		sb.append(URLEncoder.encode(name, StandardCharsets.UTF_8));
		sb.append("?secret=");
		Base32Enc b32 = new Base32Enc();
		sb.append(b32.encodeBin(this.key, 0, this.key.length));
		return sb.toString();
	}
}
