package org.sswr.util.io;

public interface ProtocolHandler extends ProtocolParser
{
	public int buildPacket(byte[] buff, int buffOfst, int cmdType, int seqId, byte[] cmd, int cmdOfst, int cmdSize, Object stmData);
}
