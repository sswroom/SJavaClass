package org.sswr.util.io;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public interface ProtocolDataListener
{
	public void dataParsed(@Nonnull IOStream stm, @Nullable Object stmObj, int cmdType, int seqId, @Nonnull byte[] cmd, int cmdOfst, int cmdSize);
	public void dataSkipped(@Nonnull IOStream stm, @Nullable Object stmObj, @Nonnull byte[] buff, int buffOfst, int buffSize);
}
