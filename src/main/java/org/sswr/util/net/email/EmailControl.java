package org.sswr.util.net.email;

import java.util.List;

public interface EmailControl
{
	public boolean sendMail(EmailMessage message, String toList, String ccList);
	public boolean sendBatchMail(EmailMessage message, List<String> toList);
	public boolean isServerOnline();
	public boolean validateDestAddr(String addr);
	public String sendTestingEmail(String toAddress);
}
