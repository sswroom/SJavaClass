package org.sswr.util.net;

public interface TCPClientHandler
{
	public void onTCPClientEvent(TCPClient cli, Object cliData, TCPEventType evtType);
	public void onTCPClientData(TCPClient cli, Object cliData, byte[] buff, int ofst, int size);
	public void onTCPClientTimeout(TCPClient cli, Object cliData);
}
