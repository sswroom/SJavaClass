package org.sswr.util.net;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class SNMPTrapMonitor implements UDPPacketListener
{
	private UDPServer svr;
	private SNMPTrapHandler hdlr;

	public SNMPTrapMonitor(InetAddress bindaddr, SNMPTrapHandler hdlr)
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
	public void udpPacketReceived(InetAddress addr, int port, byte[] buff, int ofst, int length)
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
