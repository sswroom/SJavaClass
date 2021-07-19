package org.sswr.util.crypto;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.sswr.util.data.textbinenc.Base32Enc;

public class TOTP extends OTP
{
	private byte[] key;
	private int intervalMS;

	public TOTP(byte[] key)
	{
		super(6);
		this.key = key.clone();
		this.intervalMS = 30000;
	}

	public TOTP(String key)
	{
		super(6);
		this.key = new Base32Enc().decodeBin(key);
		this.intervalMS = 30000;
	}

	public long getCounter()
	{
		return (System.currentTimeMillis() / this.intervalMS);
	}
	
	public int nextCode()
	{
		return HOTP.calcCode(this.key, this.getCounter(), this.nDigits);
	}
	
	public boolean isValid(int code)
	{
		int i = 5;
		long counter = this.getCounter() - (i >> 1);
		while (i-- > 0)
		{
			if (HOTP.calcCode(this.key, counter, this.nDigits) == code)
			{
				return true;
			}
			counter++;
		}
		return false;
	}

	public String genURI(String name)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("otpauth://totp/");
		sb.append(URLEncoder.encode(name, StandardCharsets.UTF_8));
		sb.append("?secret=");
		Base32Enc b32 = new Base32Enc();
		sb.append(b32.encodeBin(this.key, 0, this.key.length));
		return sb.toString();
	}
}
