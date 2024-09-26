package org.sswr.util.data.textbinenc;

import java.nio.charset.StandardCharsets;

import jakarta.annotation.Nonnull;

public class UTF8TextBinEnc extends CharsetTextBinEnc
{
	public UTF8TextBinEnc()
	{
		super(StandardCharsets.UTF_8);
	}

	@Nonnull
	public String getName()
	{
		return "UTF-8 Text";
	}
}
