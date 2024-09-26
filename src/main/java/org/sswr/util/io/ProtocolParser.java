package org.sswr.util.io;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public interface ProtocolParser
{
	@Nullable
	public Object createStreamData(@Nonnull IOStream stm);
	public void deleteStreamData(@Nonnull IOStream stm, @Nullable Object stmData);
	public int parseProtocol(@Nonnull IOStream stm, @Nullable Object stmObj, @Nullable Object stmData, @Nonnull byte[] buff, int buffOfst, int buffSize); // return unprocessed size	
}
