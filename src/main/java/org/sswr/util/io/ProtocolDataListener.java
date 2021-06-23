package org.sswr.util.io;

public interface ProtocolDataListener
{
	public void dataParsed(IOStream stm, Object stmObj, int cmdType, int seqId, byte[] cmd, int cmdOfst, int cmdSize);
	public void dataSkipped(IOStream stm, Object stmObj, byte[] buff, int buffOfst, int buffSize);
}
