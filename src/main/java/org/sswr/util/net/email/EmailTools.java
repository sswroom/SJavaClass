package org.sswr.util.net.email;

import java.nio.charset.StandardCharsets;

import org.sswr.util.data.textbinenc.Base64Enc;
import org.sswr.util.data.textbinenc.EncodingException;

import jakarta.annotation.Nonnull;

public class EmailTools
{
	@Nonnull
	public static String toUTF8Header(@Nonnull String headerValue)
	{
		Base64Enc b64 = new Base64Enc();
		try
		{
			return "=?UTF-8?B?" + b64.encodeBin(headerValue.getBytes(StandardCharsets.UTF_8)) + "?=";
		}
		catch (EncodingException ex)
		{
			ex.printStackTrace();
			return headerValue;
		}
	}
}
