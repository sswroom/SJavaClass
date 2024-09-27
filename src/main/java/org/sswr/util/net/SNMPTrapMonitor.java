package org.sswr.util.net;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class SNMPTrapMonitor implements UDPPacketListener
{
	private UDPServer svr;
	private SNMPTrapHandler hdlr;

	public SNMPTrapMonitor(@Nullable InetAddress bindaddr, @Nonnull SNMPTrapHandler hdlr)
	{
		this.hdlr = hdlr;
		this.svr = new UDPServer(bindaddr, 162, null, this, null, null, 2, false);
	}

	public void close()
	{
		this.svr.close();
	}

	public boolean isError()
	{
		return this.svr.isError();
	}
	
	@Override
	public void udpPacketReceived(@Nonnull InetAddress addr, int port, @Nonnull byte[] buff, int ofst, int length)
	{
		SNMPTrapInfo trap = new SNMPTrapInfo();
		List<SNMPBindingItem> itemList = new ArrayList<SNMPBindingItem>();
		SNMPErrorStatus err;
		err = SNMPUtil.pduParseTrapMessage(buff, ofst, length, trap, itemList);
		if (err == SNMPErrorStatus.NOERROR)
		{
			this.hdlr.onSNMPMessage(addr, port, trap, itemList);
		}
	}
}
