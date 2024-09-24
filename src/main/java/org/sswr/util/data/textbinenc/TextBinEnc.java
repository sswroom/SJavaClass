package org.sswr.util.data.textbinenc;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public abstract class TextBinEnc
{
	public @Nullable String encodeBin(@Nonnull byte[] dataBuff)
	{
		return encodeBin(dataBuff, 0, dataBuff.length);
	}

	public abstract @Nullable String encodeBin(@Nonnull byte []dataBuff, int dataOfst, int buffSize);
	public abstract @Nullable byte []decodeBin(@Nonnull String s);
	public abstract @Nonnull String getName();

	public @Nonnull String toString()
	{
		return getName();
	}
}
