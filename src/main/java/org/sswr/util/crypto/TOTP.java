package org.sswr.util.crypto;

import org.sswr.util.data.textbinenc.Base32Enc;
import org.sswr.util.data.textenc.URIEncoding;

import jakarta.annotation.Nonnull;

public class TOTP extends OTP
{
	private byte[] key;
	private int intervalMS;

	public TOTP(@Nonnull byte[] key)
	{
		super(6);
		this.key = key.clone();
		this.intervalMS = 30000;
	}

	public TOTP(@Nonnull String key)
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

	@Nonnull
	public String genURI(@Nonnull String name)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("otpauth://totp/");
		sb.append(URIEncoding.uriEncode(name));
		sb.append("?secret=");
		Base32Enc b32 = new Base32Enc();
		sb.append(b32.encodeBin(this.key, 0, this.key.length));
		return sb.toString();
	}
}
