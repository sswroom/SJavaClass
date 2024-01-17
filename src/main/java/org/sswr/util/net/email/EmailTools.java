package org.sswr.util.net.email;

import java.nio.charset.StandardCharsets;

import org.sswr.util.data.textbinenc.Base64Enc;

public class EmailTools
{
	public static String toUTF8Header(String headerValue)
	{
		Base64Enc b64 = new Base64Enc();
		return "=?UTF-8?B?" + b64.encodeBin(headerValue.getBytes(StandardCharsets.UTF_8)) + "?=";
	}
}
