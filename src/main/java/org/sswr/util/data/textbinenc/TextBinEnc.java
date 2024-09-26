package org.sswr.util.data.textbinenc;

import jakarta.annotation.Nonnull;

public abstract class TextBinEnc
{
	public @Nonnull String encodeBin(@Nonnull byte[] dataBuff) throws EncodingException
	{
		return encodeBin(dataBuff, 0, dataBuff.length);
	}

	@Nonnull
	public abstract String encodeBin(@Nonnull byte []dataBuff, int dataOfst, int buffSize) throws EncodingException;
	@Nonnull
	public abstract byte []decodeBin(@Nonnull String s) throws EncodingException;
	@Nonnull
	public abstract String getName();

	@Nonnull
	public String toString()
	{
		return getName();
	}
}
