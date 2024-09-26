package org.sswr.util.crypto;

import org.sswr.util.data.ByteTool;
import org.sswr.util.data.RandomBytesGenerator;
import org.sswr.util.data.textbinenc.Base32Enc;
import org.sswr.util.data.textbinenc.Base64Enc;
import org.sswr.util.data.textbinenc.EncodingException;
import org.sswr.util.data.textbinenc.Base64Enc.B64Charset;

import jakarta.annotation.Nonnull;

public class IntKeyHandler
{
	@Nonnull
	public static String generate(int id, int leng, boolean caseSensitive) throws EncodingException
	{
		if (leng < 4)
		{
			leng = 4;
		}
		RandomBytesGenerator random = new RandomBytesGenerator();
		byte[] buff = new byte[leng + 4];
		ByteTool.copyArray(buff, 4, random.nextBytes(leng), 0, leng);
		ByteTool.writeInt32(buff, 0, id);
		buff[0] = (byte)(buff[0] ^ buff[leng]);
		buff[1] = (byte)(buff[1] ^ buff[leng + 1]);
		buff[2] = (byte)(buff[2] ^ buff[leng + 2]);
		buff[3] = (byte)(buff[3] ^ buff[leng + 3]);
		if (caseSensitive)
		{
			return new Base64Enc(B64Charset.URL, true).encodeBin(buff);
		}
		else
		{
			return new Base32Enc().encodeBin(buff);
		}
	}

	public static int parseKey(@Nonnull String key, boolean caseSensitive)
	{
		byte[] buff;
		if (caseSensitive)
		{
			buff = new Base64Enc(B64Charset.URL, true).decodeBin(key);
		}
		else
		{
			buff = new Base32Enc().decodeBin(key.toUpperCase());
		}
		if (buff == null || buff.length < 8)
		{
			throw new IllegalArgumentException("key is too short");
		}
		int leng = buff.length - 4;
		buff[0] = (byte)(buff[0] ^ buff[leng]);
		buff[1] = (byte)(buff[1] ^ buff[leng + 1]);
		buff[2] = (byte)(buff[2] ^ buff[leng + 2]);
		buff[3] = (byte)(buff[3] ^ buff[leng + 3]);
		return ByteTool.readInt32(buff, 0);
	}
}
