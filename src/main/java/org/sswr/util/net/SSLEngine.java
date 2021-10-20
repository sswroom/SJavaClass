package org.sswr.util.net;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;

public class SSLEngine
{
	SSLSocketFactory factory;
	public SSLEngine()
	{
		this.factory = (SSLSocketFactory)SSLSocketFactory.getDefault();
	}

	public SocketFactory getSocketFactory()
	{
		return this.factory;
	}
}
