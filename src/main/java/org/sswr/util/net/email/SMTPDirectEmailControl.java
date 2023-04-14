package org.sswr.util.net.email;

import java.time.ZonedDateTime;
import java.util.List;

import org.sswr.util.data.StringUtil;
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

	public boolean sendMail(EmailMessage msg, String toList, String ccList)
	{
		SMTPMessage message = new SMTPMessage();
		message.setSubject(msg.getSubject());
		message.setContent(msg.getContent(), "text/html; charset=utf-8");
		message.setSentDate(ZonedDateTime.now());
		message.setFrom(null, this.smtpFrom);
		message.addToList(toList);
		if (ccList != null && ccList.length() > 0)
		{
			message.addCcList(ccList);
		}
		int i = 0;
		int j = msg.getAttachmentCount();
		while (i < j)
		{
			if (message.addAttachment(msg.getAttachment(i)) == null)
			{
				return false;
			}
			i++;
		}
		return this.smtp.send(message);
	}

	public boolean sendBatchMail(EmailMessage msg, List<String> toList)
	{
		return sendMail(msg, StringUtil.join(toList, ","), null);
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
		SMTPMessage message = new SMTPMessage();
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
