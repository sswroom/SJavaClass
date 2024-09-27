package org.sswr.util.net.email;

import java.io.ByteArrayOutputStream;
import java.util.List;

import org.sswr.util.basic.MyThread;
import org.sswr.util.io.IOWriter;
import org.sswr.util.net.SSLEngine;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class SMTPClient
{
	private String host;
	private int port;
	private SSLEngine ssl;
	private SMTPConnType connType;
	private IOWriter logWriter;
	private String authUser;
	private String authPassword;

	public SMTPClient(@Nonnull String host, int port, @Nullable SSLEngine ssl, @Nonnull SMTPConnType connType, @Nullable IOWriter logWriter)
	{
		this.host = host;
		this.port = port;
		this.ssl = ssl;
		this.connType = connType;
		this.logWriter = logWriter;
		this.authUser = null;
		this.authPassword = null;
	}

	public void setPlainAuth(@Nullable String userName, @Nullable String password)
	{
		this.authUser = userName;
		this.authPassword = password;
	}

	public boolean send(@Nonnull SMTPMessage message)
	{
		if (!message.canSend())
		{
			return false;
		}
		String fromAddr = message.getFromAddr();
		if (fromAddr == null)
			return false;
		ByteArrayOutputStream mstm = new ByteArrayOutputStream();
		if (!message.writeMessage(mstm))
		{
			return false;
		}
		SMTPConn conn = new SMTPConn(this.host, this.port, this.ssl, this.connType, this.logWriter);
		if (conn.isError())
		{
			conn.close();
			return false;
		}
		if (!conn.sendEHlo("[127.0.0.1]"))
		{
			if (!conn.sendHelo("[127.0.0.1]"))
			{
				conn.close();
				return false;
			}
		}
		MyThread.sleep(10);
		if (this.authUser != null && this.authPassword != null)
		{
			if (!conn.sendAuth(this.authUser, this.authPassword))
			{
				conn.close();
				return false;
			}
		}
		if (!conn.sendMailFrom(fromAddr))
		{
			conn.close();
			return false;
		}
		List<EmailAddress> recpList = message.getRecpList();
		int i = 0;
		int j = recpList.size();
		while (i < j)
		{
			if (!conn.sendRcptTo(recpList.get(i).getAddress()))
			{
				conn.close();
				return false;
			}
			i++;
		}
		byte []content = mstm.toByteArray();
		boolean succ = conn.sendData(content, 0, content.length);
		conn.sendQuit();
		conn.close();
		return succ;
	}

	public boolean testServerOnline()
	{
		SMTPConn conn = new SMTPConn(this.host, this.port, this.ssl, this.connType, this.logWriter);
		if (conn.isError())
		{
			conn.close();
			return false;
		}
		conn.close();
		return true;
	}
}
