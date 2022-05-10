package org.sswr.util.net.email;

public interface EmailControl
{
	public boolean sendMail(EmailTemplate template, String toList);
	public boolean isServerValid();
	public String testEmail(String toAddress);
}
