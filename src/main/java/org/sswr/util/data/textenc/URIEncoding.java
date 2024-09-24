package org.sswr.util.data.textenc;

import java.nio.charset.StandardCharsets;

import org.sswr.util.data.StringUtil;

import jakarta.annotation.Nonnull;

public class URIEncoding
{
	private static final byte uriAllow[] = {
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		0, 1, 1, 0, 1, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 0, 1,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 0, 1,
		0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 1, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
	
	public static @Nonnull String uriEncode(@Nonnull String uri)
	{
		byte[] carr = uri.getBytes(StandardCharsets.UTF_8);
		StringBuilder sb = new StringBuilder();
		int i = 0;
		int j = carr.length;
		int v;
		while (i < j)
		{
			v = carr[i] & 0xff;
			if (uriAllow[v] != 0)
			{
				sb.append((char)carr[i]);
			}
			else
			{
				sb.append('%');
				sb.append(StringUtil.HEX_ARRAY[v >> 4]);
				sb.append(StringUtil.HEX_ARRAY[v & 15]);
			}
			i++;
		}
		return sb.toString();
	}

	public static @Nonnull String uriDecode(@Nonnull String uri)
	{
		byte[] carr = uri.getBytes(StandardCharsets.UTF_8);
		byte[] destBuff = new byte[carr.length];
		int i = 0;
		int j = carr.length;
		int k = 0;
		byte b;
		int c;
		int v;
		while (i < j)
		{
			b = carr[i];
			if (b == '%')
			{
				if (i + 2 >= j)
				{
					destBuff[k++] = b;
				}
				else
				{
					v = 0;
					c = carr[i + 1] & 0xff;
					if (c >= 0x30 && c <= 0x39)
					{
						v = (c - 0x30);
					}
					else if (c >= 0x41 && c <= 0x46)
					{
						v = (c - 0x37);
					}
					else if (c >= 0x61 && c <= 0x66)
					{
						v = (c - 0x57);
					}
					else
					{
						destBuff[k++] = '%';
						destBuff[k++] = (byte)c;
						c = 0;
						i++;
					}
					if (c != 0)
					{
						c = carr[i + 2] & 0xff;
						if (c >= 0x30 && c <= 0x39)
						{
							destBuff[k++] = (byte)((v << 4) + (c - 0x30));
						}
						else if (c >= 0x41 && c <= 0x46)
						{
							destBuff[k++] = (byte)((v << 4) + (c - 0x37));
						}
						else if (c >= 0x61 && c <= 0x66)
						{
							destBuff[k++] = (byte)((v << 4) + (c - 0x57));
						}
						else
						{
							destBuff[k++] = '%';
							destBuff[k++] = carr[i + 1];
							destBuff[k++] = (byte)c;
							i += 2;
						}
					}
				}
			}
			else
			{
				destBuff[k++] = b;
			}
			i++;
		}
		return new String(destBuff, 0, k, StandardCharsets.UTF_8);
	}
}
