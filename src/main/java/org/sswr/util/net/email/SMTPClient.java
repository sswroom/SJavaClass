package org.sswr.util.net.email;

import java.io.ByteArrayOutputStream;
import java.util.List;

import org.sswr.util.basic.MyThread;
import org.sswr.util.io.IOWriter;

public class SMTPClient
{
	private String host;
	private int port;
	private SMTPConnType connType;
	private IOWriter logWriter;
	private String authUser;
	private String authPassword;

	public SMTPClient(String host, int port, SMTPConnType connType, IOWriter logWriter)
	{
		this.host = host;
		this.port = port;
		this.connType = connType;
		this.logWriter = logWriter;
		this.authUser = null;
		this.authPassword = null;
	}

	public void setPlainAuth(String userName, String password)
	{
		this.authUser = userName;
		this.authPassword = password;
	}

	public boolean send(EmailMessage message)
	{
		if (!message.completedMessage())
		{
			return false;
		}
		ByteArrayOutputStream mstm = new ByteArrayOutputStream();
		if (!message.writeToStream(mstm))
		{
			return false;
		}
		SMTPConn conn = new SMTPConn(this.host, this.port, this.connType, this.logWriter);
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
		if (!conn.sendMailFrom(message.getFromAddr()))
		{
			conn.close();
			return false;
		}
		List<String> recpList = message.getRecpList();
		int i = 0;
		int j = recpList.size();
		while (i < j)
		{
			if (!conn.sendRcptTo(recpList.get(i)))
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

}
