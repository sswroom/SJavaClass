package org.sswr.util.web;

public class NullEmailControl implements EmailControl
{
	@Override
	public boolean sendMail(EmailTemplate template, String toList) {
		return false;
	}

	@Override
	public boolean isServerValid() {
		return true;
	}

	@Override
	public String testEmail(String toAddress) {
		return "Sent";
	}
}
