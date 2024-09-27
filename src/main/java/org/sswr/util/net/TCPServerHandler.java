package org.sswr.util.net;

import java.net.Socket;

import jakarta.annotation.Nonnull;

public interface TCPServerHandler
{
	public void onTCPConnection(@Nonnull Socket s);
}
