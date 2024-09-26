package org.sswr.util.crypto;

import jakarta.annotation.Nonnull;

public abstract class OTP
{
	protected int nDigits;

	public OTP(int nDigits)
	{
		this.nDigits = nDigits;
	}

	public abstract long getCounter();
	public abstract int nextCode();
	public abstract boolean isValid(int code);
	@Nonnull
	public abstract String genURI(@Nonnull String name);

	@Nonnull
	public String codeString(int code)
	{
		if (code < 0)
		{
			return String.valueOf(code);
		}
		if (this.nDigits == 6)
		{
			if (code < 10)
			{
				return "00000" + code;
			}
			else if (code < 100)
			{
				return "0000" + code;
			}
			else if (code < 1000)
			{
				return "000" + code;
			}
			else if (code < 10000)
			{
				return "00" + code;
			}
			else if (code < 100000)
			{
				return "0" + code;
			}
			else
			{
				return String.valueOf(code);
			}
		}
		else
		{
			return String.valueOf(code);
		}
	}
}
