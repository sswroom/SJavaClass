package org.sswr.util.data.textbinenc;

import jakarta.annotation.Nonnull;

public class ANSITextBinEnc extends CharsetTextBinEnc
{
	public ANSITextBinEnc()
	{
		super(null);
	}

	public @Nonnull String getName()
	{
		return "ANSI Text";
	}
}
