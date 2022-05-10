package org.sswr.util.net.email;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.sswr.util.net.DNSClient;
import org.sswr.util.net.DNSRequestAnswer;
import org.sswr.util.net.SocketFactory;

public class EmailValidator
{
	public enum Status
	{
		S_VALID,
		S_INVALID_FORMAT,
		S_NO_SUCH_ADDR,
		S_FROM_NOT_ACCEPT,
		S_CONN_ERROR,
		S_COMM_ERROR,
		S_DOMAIN_NOT_RESOLVED
	};

	private DNSClient dnsClient;

	public EmailValidator(SocketFactory sockf)
	{
		InetAddress[] dnsList = sockf.getDefDNS();
		if (dnsList != null && dnsList.length > 0)
		{
			this.dnsClient = new DNSClient(dnsList[0]);
		}
	}

	public void close()
	{
		if (this.dnsClient != null)
		{
			this.dnsClient.close();
			this.dnsClient = null;
		}
	}

	public Status validate(String emailAddr)
	{
		SMTPConn conn;
		String emailDomain;
		int i = emailAddr.indexOf('@');
		int j;
		if (i < 0 || i == 0)
		{
			return Status.S_INVALID_FORMAT;
		}
		emailDomain = emailAddr.substring(i + 1);
		i = emailDomain.indexOf('@');
		if (i != -1)
		{
			return Status.S_INVALID_FORMAT;
		}
	
		String emailSvr = null;
		List<DNSRequestAnswer> ansList = new ArrayList<DNSRequestAnswer>();
		DNSRequestAnswer ans;
		this.dnsClient.getByEmailDomainName(ansList, emailDomain);
		i = 0;
		j = ansList.size();
		while (i < j)
		{
			ans = ansList.get(i);
			if (ans.recType == 15)
			{
				emailSvr = ans.rd;
				break;
			}
			j++;
		}
		if (emailSvr == null)
		{
			return Status.S_DOMAIN_NOT_RESOLVED;
		}
	
		try
		{
			InetAddress.getByName(emailSvr);
		}
		catch (UnknownHostException ex)
		{
			return Status.S_DOMAIN_NOT_RESOLVED;
		}
		conn = new SMTPConn(emailSvr, 25, SMTPConnType.PLAIN, null);
		if (conn.isError())
		{
			conn.close();
			return Status.S_CONN_ERROR;
		}
		if (!conn.sendHelo("[127.0.0.1]"))
		{
			conn.sendQuit();
			conn.close();
			return Status.S_COMM_ERROR;
		}
		if (!conn.sendMailFrom("sswroom@yahoo.com"))
		{
			conn.sendQuit();
			conn.close();
			return Status.S_FROM_NOT_ACCEPT;
		}
		if (!conn.sendRcptTo(emailAddr))
		{
			conn.sendQuit();
			conn.close();
			return Status.S_NO_SUCH_ADDR;
		}
		conn.sendQuit();
		conn.close();
		return Status.S_VALID;
	}

	public static String StatusGetName(Status status)
	{
		switch (status)
		{
		case S_VALID:
			return "Email Address Valid";
		case S_INVALID_FORMAT:
			return "Invalid Format";
		case S_NO_SUCH_ADDR:
			return "No Such Address";
		case S_FROM_NOT_ACCEPT:
			return "From Address Not Accepted";
		case S_CONN_ERROR:
			return "Connect Error";
		case S_COMM_ERROR:
			return "Communication Error";
		case S_DOMAIN_NOT_RESOLVED:
			return "Domain not resolved";
		default:
			return "Unknown";
		}
	}
}
