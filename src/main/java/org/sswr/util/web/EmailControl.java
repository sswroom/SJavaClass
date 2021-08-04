package org.sswr.util.web;

public interface EmailControl
{
	public boolean sendMail(EmailTemplate template, String toList);
	public boolean isServerValid();
	public String testEmail(String toAddress);
}
