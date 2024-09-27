package org.sswr.util.net;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public interface TCPClientHandler
{
	public void onTCPClientEvent(@Nonnull TCPClient cli, @Nullable Object cliData, @Nonnull TCPEventType evtType);
	public void onTCPClientData(@Nonnull TCPClient cli, @Nullable Object cliData, @Nonnull byte[] buff, int ofst, int size);
	public void onTCPClientTimeout(@Nonnull TCPClient cli, @Nullable Object cliData);
}
