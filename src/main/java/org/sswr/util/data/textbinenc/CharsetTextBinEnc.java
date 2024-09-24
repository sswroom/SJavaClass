package org.sswr.util.data.textbinenc;

import java.nio.charset.Charset;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class CharsetTextBinEnc extends TextBinEnc
{
	private Charset cs;

	public CharsetTextBinEnc(@Nullable Charset cs)
	{
		if (cs == null)
		{
			this.cs = Charset.defaultCharset();
		}
		else
		{
			this.cs = cs;
		}
	}

	public @Nullable String encodeBin(@Nonnull byte []dataBuff, int dataOfst, int buffSize)
	{
		return new String(dataBuff, dataOfst, buffSize, this.cs);
	}

	public @Nullable byte []decodeBin(@Nonnull String s)
	{
		return s.getBytes(this.cs);
	}

	public @Nonnull String getName()
	{
		return "Charset Encoding";
	}	
}
