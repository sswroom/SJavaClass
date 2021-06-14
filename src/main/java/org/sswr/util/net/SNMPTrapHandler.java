package org.sswr.util.net;

import java.net.InetAddress;
import java.util.List;

public interface SNMPTrapHandler
{
	public void onSNMPMessage(InetAddress addr, int port, SNMPTrapInfo trap, List<SNMPBindingItem> itemList);
}
