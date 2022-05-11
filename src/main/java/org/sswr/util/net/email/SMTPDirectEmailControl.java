package org.sswr.util.net.email;

import java.time.ZonedDateTime;

import org.sswr.util.io.LogLevel;
import org.sswr.util.io.LogTool;
import org.sswr.util.io.LogToolWriter;
import org.sswr.util.net.SocketFactory;
import org.sswr.util.net.email.EmailValidator.Status;

public class SMTPDirectEmailControl implements EmailControl
{
	private SMTPClient smtp;
	private String smtpFrom;
	private EmailValidator validator;

	public SMTPDirectEmailControl(String smtpHost, Integer smtpPort, SMTPConnType connType, String username, String password, String smtpFrom, LogTool logger)
	{
		this.smtpFrom = smtpFrom;
		this.smtp = new SMTPClient(smtpHost, (smtpPort == null)?getDefaultPort():smtpPort.intValue(), connType, new LogToolWriter(logger, LogLevel.RAW));
		if (username != null && username.length() > 0 && password != null && password.length() > 0)
		{
			this.smtp.setPlainAuth(username, password);
		}
		this.validator = new EmailValidator(SocketFactory.create());
	}

	public boolean sendMail(EmailTemplate template, String toList)
	{
		EmailMessage message = new EmailMessage();
		message.setSubject(template.getSubject());
		message.setContent(template.getContent(), "text/html; charset=utf-8");
		message.setSentDate(ZonedDateTime.now());
		message.setFrom(null, this.smtpFrom);
		message.addToList(toList);
		return this.smtp.send(message);
	}

	public boolean isServerOnline()
	{
		return this.smtp.testServerOnline();
	}

	public boolean validateDestAddr(String addr)
	{
		return this.validator.validate(addr) == Status.S_VALID;
	}

	public String sendTestingEmail(String toAddress)
	{
		EmailMessage message = new EmailMessage();
		message.setSubject("Email Testing");
		message.setContent("This is a test email", "text/html; charset=utf-8");
		message.setSentDate(ZonedDateTime.now());
		message.setFrom(null, this.smtpFrom);
		message.addToList(toAddress);
		if (this.smtp.send(message))
		{
			return "Sent";
		}
		else
		{
			return "Error";
		}
	}

	public static int getDefaultPort()
	{
		return 25;
	}

}
