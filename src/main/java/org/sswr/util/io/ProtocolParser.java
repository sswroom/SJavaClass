package org.sswr.util.io;

public interface ProtocolParser
{
	public Object createStreamData(IOStream stm);
	public void deleteStreamData(IOStream stm, Object stmData);
	public int parseProtocol(IOStream stm, Object stmObj, Object stmData, byte[] buff, int buffOfst, int buffSize); // return unprocessed size	
}
