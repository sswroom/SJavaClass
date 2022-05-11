package org.sswr.util.net.email;

public interface EmailControl
{
	public boolean sendMail(EmailTemplate template, String toList);
	public boolean isServerOnline();
	public boolean validateDestAddr(String addr);
	public String sendTestingEmail(String toAddress);
}
