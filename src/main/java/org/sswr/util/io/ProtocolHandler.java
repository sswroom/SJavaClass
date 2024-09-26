package org.sswr.util.io;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public interface ProtocolHandler extends ProtocolParser
{
	public int buildPacket(@Nonnull byte[] buff, int buffOfst, int cmdType, int seqId, @Nonnull byte[] cmd, int cmdOfst, int cmdSize, @Nullable Object stmData);
}
