package org.sswr.util.net.email;

import java.util.List;

import org.sswr.util.data.StringUtil;

public class NullEmailControl implements EmailControl
{
	@Override
	public boolean sendMail(EmailMessage msg, String toList, String ccList)
	{
		return false;
	}

	@Override
	public boolean sendBatchMail(EmailMessage msg, List<String> toList)
	{
		return false;
	}

	@Override
	public boolean isServerOnline()
	{
		return true;
	}

	@Override
	public boolean validateDestAddr(String addr)
	{
		return StringUtil.isEmailAddress(addr);
	}

	@Override
	public String sendTestingEmail(String toAddress) {
		return "Sent";
	}
}
