package org.sswr.util.net;

import java.net.InetAddress;

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
/*		Net::SocketUtil::AddressInfo addr;
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
		if (i != Status.INVALID_INDEX)
		{
			return Status.S_INVALID_FORMAT;
		}
	
		Text::String *emailSvr = 0;
		Data::ArrayList<Net::DNSClient::RequestAnswer*> ansList;
		Net::DNSClient::RequestAnswer *ans;
		this->dnsClient->GetByEmailDomainName(&ansList, emailDomain);
		i = 0;
		j = ansList.GetCount();
		while (i < j)
		{
			ans = ansList.GetItem(i);
			if (ans->recType == 15)
			{
				emailSvr = ans->rd->Clone();
				break;
			}
			j++;
		}
		Net::DNSClient::FreeAnswers(&ansList);
		if (emailSvr == 0)
		{
			return S_DOMAIN_NOT_RESOLVED;
		}
	
		if (!this->sockf->DNSResolveIP(emailSvr->ToCString(), &addr))
		{
			emailSvr->Release();
			return S_DOMAIN_NOT_RESOLVED;
		}
		NEW_CLASS(conn, Net::Email::SMTPConn(this->sockf, 0, emailSvr->ToCString(), 25, Net::Email::SMTPConn::CT_PLAIN, 0));
		emailSvr->Release();
		if (conn->IsError())
		{
			DEL_CLASS(conn);
			return S_CONN_ERROR;
		}
		if (!conn->SendHelo(CSTR("[127.0.0.1]")))
		{
			conn->SendQuit();
			DEL_CLASS(conn);
			return S_COMM_ERROR;
		}
		if (!conn->SendMailFrom(CSTR("sswroom@yahoo.com")))
		{
			conn->SendQuit();
			DEL_CLASS(conn);
			return S_FROM_NOT_ACCEPT;
		}
		if (!conn->SendRcptTo(emailAddr))
		{
			conn->SendQuit();
			DEL_CLASS(conn);
			return S_NO_SUCH_ADDR;
		}
		conn->SendQuit();
		DEL_CLASS(conn);*/
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
