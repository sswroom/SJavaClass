package org.sswr.util.net.email;

import org.sswr.util.data.StringUtil;

public class NullEmailControl implements EmailControl
{
	@Override
	public boolean sendMail(EmailMessage message, String toList, String ccList)
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
