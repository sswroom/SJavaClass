package org.sswr.util.net.email;

import java.time.ZonedDateTime;
import java.util.List;

import org.sswr.util.data.StringUtil;
import org.sswr.util.io.LogLevel;
import org.sswr.util.io.LogTool;
import org.sswr.util.io.LogToolWriter;
import org.sswr.util.net.SSLEngine;
import org.sswr.util.net.SocketFactory;
import org.sswr.util.net.email.EmailValidator.Status;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class SMTPDirectEmailControl implements EmailControl
{
	private SMTPClient smtp;
	private String smtpFrom;
	private EmailValidator validator;

	public SMTPDirectEmailControl(@Nonnull String smtpHost, @Nullable Integer smtpPort, @Nullable SSLEngine ssl, @Nonnull SMTPConnType connType, @Nullable String username, @Nullable String password, @Nonnull String smtpFrom, @Nonnull LogTool logger)
	{
		this.smtpFrom = smtpFrom;
		this.smtp = new SMTPClient(smtpHost, (smtpPort == null)?getDefaultPort():smtpPort.intValue(), ssl, connType, new LogToolWriter(logger, LogLevel.RAW));
		if (username != null && username.length() > 0 && password != null && password.length() > 0)
		{
			this.smtp.setPlainAuth(username, password);
		}
		this.validator = new EmailValidator(SocketFactory.create());
	}

	public boolean sendMail(@Nonnull EmailMessage msg, @Nullable String toList, @Nullable String ccList, @Nullable String bccList)
	{
		SMTPMessage message = new SMTPMessage();
		String subject;
		String content;
		if ((subject = msg.getSubject()) == null)
			return false;
		if ((content = msg.getContent()) == null)
			return false;
		message.setSubject(subject);
		message.setContent(content, "text/html; charset=utf-8");
		message.setSentDate(ZonedDateTime.now());
		message.setFrom(new EmailAddress(null, this.smtpFrom));
		if (toList != null && toList.length() > 0)
		{
			message.addToList(toList);
		}
		if (ccList != null && ccList.length() > 0)
		{
			message.addCcList(ccList);
		}
		if (bccList != null && bccList.length() > 0)
		{
			message.addBccList(bccList);
		}
		int i = 0;
		int j = msg.getAttachmentCount();
		while (i < j)
		{
			EmailAttachment att = msg.getAttachment(i);
			if (att == null)
			{
				return false;
			}
			message.addAttachment(att);
			i++;
		}
		return this.smtp.send(message);
	}

	public boolean sendBatchMail(@Nonnull EmailMessage msg, @Nonnull List<String> toList)
	{
		return sendMail(msg, StringUtil.join(toList, ","), null, null);
	}

	public boolean isServerOnline()
	{
		return this.smtp.testServerOnline();
	}

	public boolean validateDestAddr(@Nonnull String addr)
	{
		return this.validator.validate(addr) == Status.S_VALID;
	}

	@Nonnull
	public String sendTestingEmail(@Nonnull String toAddress)
	{
		SMTPMessage message = new SMTPMessage();
		message.setSubject("Email Testing");
		message.setContent("This is a test email", "text/html; charset=utf-8");
		message.setSentDate(ZonedDateTime.now());
		message.setFrom(new EmailAddress(null, this.smtpFrom));
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
