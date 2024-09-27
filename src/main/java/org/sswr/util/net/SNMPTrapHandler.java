package org.sswr.util.net;

import java.net.InetAddress;
import java.util.List;

import jakarta.annotation.Nonnull;

public interface SNMPTrapHandler
{
	public void onSNMPMessage(@Nonnull InetAddress addr, int port, @Nonnull SNMPTrapInfo trap, @Nonnull List<SNMPBindingItem> itemList);
}
