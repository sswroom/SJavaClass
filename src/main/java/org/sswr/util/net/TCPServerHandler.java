package org.sswr.util.net;

import java.net.Socket;

public interface TCPServerHandler
{
	public void onTCPConnection(Socket s);
}
