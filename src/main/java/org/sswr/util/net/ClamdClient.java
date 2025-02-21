package org.sswr.util.net;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

import org.sswr.util.data.ByteTool;

import jakarta.annotation.Nonnull;

public class ClamdClient
{
	private int port;
	private TCPClientFactory clif;

	public ClamdClient(@Nonnull TCPClientFactory clif, int port)
	{
		this.port = port;
		this.clif = clif;
	}

	public boolean noVirus(@Nonnull byte[] fileData)
	{
		TCPClient cli = clif.create("localhost", this.port, Duration.ofSeconds(8));
		if (cli.isConnectError())
		{
			cli.close();
			return false;
		}
		cli.setTimeout(5000);
		cli.write("zINSTREAM\0".getBytes(StandardCharsets.UTF_8));
		boolean succ = false;
		byte[] sendData = new byte[fileData.length + 8];
		ByteTool.writeMInt32(sendData, 0, fileData.length);
		ByteTool.copyArray(sendData, 4, fileData, 0, fileData.length);
		ByteTool.writeMInt32(sendData, fileData.length + 4, 0);
		if (cli.write(sendData) == sendData.length)
		{
			byte[] recv = new byte[2048];
			int readSize = cli.read(recv, 0, recv.length);
			String s = new String(recv, 0, readSize, StandardCharsets.UTF_8);
			succ = s.startsWith("stream: OK");
		}
		cli.close();
		return succ;
	}
}
