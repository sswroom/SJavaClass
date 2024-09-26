package org.sswr.util.data;

import jakarta.annotation.Nonnull;

public class ASCIIValidator implements StringValidator
{
	private boolean validChars[];

	public ASCIIValidator()
	{
		validChars = new boolean[128];
	}

	public void allowCapitalLetters()
	{
		char i = 'A';
		char j = 'Z';
		while (i <= j)
		{
			this.validChars[i] = true;
			i++;
		}
	}

	public void allowSmallLetters()
	{
		char i = 'a';
		char j = 'z';
		while (i <= j)
		{
			this.validChars[i] = true;
			i++;
		}
	}

	public void allowDigits()
	{
		char i = '0';
		char j = '9';
		while (i <= j)
		{
			this.validChars[i] = true;
			i++;
		}
	}

	public void allowLetters()
	{
		this.allowCapitalLetters();
		this.allowSmallLetters();
	}

	public void allowAlphanumeric()
	{
		this.allowLetters();
		this.allowDigits();
	}

	public void allowChars(@Nonnull String chars)
	{
		char[] carr = chars.toCharArray();
		int i = 0;
		int j = carr.length;
		while (i < j)
		{
			if (carr[i] < 128)
				this.validChars[carr[i]] = true;
			i++;
		}
	}

	public boolean isValid(@Nonnull String s)
	{
		char[] carr = s.toCharArray();
		int i = 0;
		int j = carr.length;
		while (i < j)
		{
			if (carr[i] >= 128 || !this.validChars[carr[i]])
				return false;
			i++;
		}
		return true;
	}
}
