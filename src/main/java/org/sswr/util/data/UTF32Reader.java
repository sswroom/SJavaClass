package org.sswr.util.data;

import jakarta.annotation.Nonnull;

public class UTF32Reader
{
	private @Nonnull String s;
	private int ofst;
	public UTF32Reader(@Nonnull String s)
	{
		this.s = s;
		this.ofst = 0;
	}

	public int nextChar()
	{
		int slen = this.s.length();
		if (this.ofst >= slen)
			return 0;
		char c = this.s.charAt(ofst);
		char c2;
		if (this.ofst + 1 < slen && c >= 0xd800 && c < 0xdc00 && (c2 = this.s.charAt(this.ofst + 1)) >= 0xdc00 && c2 < 0xe000)
		{
			this.ofst += 2;
			return 0x10000 + ((c - 0xd800) << 10) + (c2 - 0xdc00);
		}
		else
		{
			this.ofst++;
			return c;
		}
	}
}
