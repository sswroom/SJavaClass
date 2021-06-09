package org.sswr.util.data.textbinenc;

import java.nio.charset.StandardCharsets;

public class UCS2TextBinEnc extends CharsetTextBinEnc
{
	public UCS2TextBinEnc()
	{
		super(StandardCharsets.UTF_16LE);
	}

	public String getName()
	{
		return "Unicode Text";
	}
}
