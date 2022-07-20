package org.sswr.util.net.email;

public interface EmailControl
{
	public boolean sendMail(EmailMessage message, String toList, String ccList);
	public boolean isServerOnline();
	public boolean validateDestAddr(String addr);
	public String sendTestingEmail(String toAddress);
}
