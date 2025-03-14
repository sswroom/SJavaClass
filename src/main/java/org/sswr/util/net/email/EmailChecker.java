package org.sswr.util.net.email;

import java.io.File;
import java.time.ZonedDateTime;
import java.util.Iterator;

import org.sswr.util.data.DataTools;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class EmailChecker<T extends TemplateEmailStatus> implements Runnable
{
	private EmailMessageCreator msgCreator;
	private EmailCheckHandler<T> handler;
	private EmailControl emailCtrl;
	private boolean splitDestAddr;
	private String basePath;

	public EmailChecker(@Nonnull EmailMessageCreator msgCreator, @Nonnull EmailCheckHandler<T> handler, @Nonnull EmailControl emailCtrl, boolean splitDestAddr, @Nonnull String basePath)
	{
		this.msgCreator = msgCreator;
		this.handler = handler;
		this.emailCtrl = emailCtrl;
		this.splitDestAddr = splitDestAddr;
		this.basePath = basePath;
	}

	private void sendEmails(@Nonnull EmailMessage message, @Nullable String toAddrs, @Nullable String ccAddrs, @Nonnull StringBuilder sbSucc, @Nonnull StringBuilder sbFail)
	{
		if (this.splitDestAddr)
		{
			if (toAddrs != null && toAddrs.length() > 0)
			{
				String emailAddrs[] = toAddrs.split(",");
				String emailAddr;
				int i = 0;
				int j = emailAddrs.length;
				while (i < j)
				{
					emailAddr = emailAddrs[i].trim();
					if (emailCtrl.sendMail(message, emailAddr, null, null))
					{
						if (sbSucc.length() > 0)
						{
							sbSucc.append(",");
						}
						sbSucc.append(emailAddr);
					}
					else
					{
						if (sbFail.length() > 0)
						{
							sbFail.append(",");
						}
						sbFail.append(emailAddr);
					}
					i++;
				}
			}
			if (ccAddrs != null && ccAddrs.length() > 0)
			{
				sendEmails(message, ccAddrs, null, sbSucc, sbFail);
			}
		}
		else
		{
			if (emailCtrl.sendMail(message, toAddrs, ccAddrs, null))
			{
				if (sbSucc.length() > 0)
				{
					sbSucc.append(",");
				}
				sbSucc.append(toAddrs);
				if (ccAddrs != null && ccAddrs.length() > 0)
				{
					sbSucc.append(",");
					sbSucc.append(ccAddrs);
				}
			}
			else
			{
				if (sbFail.length() > 0)
				{
					sbFail.append(",");
				}
				sbFail.append(toAddrs);
				if (ccAddrs != null && ccAddrs.length() > 0)
				{
					sbFail.append(",");
					sbFail.append(ccAddrs);
				}
			}
		}
	}

	private void doQueueEmail(@Nonnull EmailControl emailCtrl, @Nonnull TemplateEmailStatus email)
	{
		try
		{
			EmailMessage message = this.msgCreator.createMessage(email.getTplname(), email.getParamObj(), email.getItemParamsObj());
			boolean attErr = false;
			int i = 0;
			int j = email.getAttachmentCount();
			while (i < j)
			{
				String attPath = email.getAttachmentPath(i, this.basePath);
				File attFile = new File(attPath);
				if (attFile.exists() && attFile.isFile())
				{
					message.addAttachmentFile(attPath);
				}
				else
				{
					attErr = true;
					break;
				}
				i++;
			}
			if (attErr)
			{
				email.setStatus(EmailStatus.ATTACHMENT_ERROR);
			}
			else if (email.getToEmails() == null || email.getToEmails().length() == 0)
			{
				email.setStatus(EmailStatus.NO_ADDRESS);
			}
			else
			{
				StringBuilder sbSucc = new StringBuilder();
				StringBuilder sbFail = new StringBuilder();
				sendEmails(message, email.getToEmails(), email.getCcEmails(), sbSucc, sbFail);
				if (sbFail.length() == 0)
				{
					email.setStatus(EmailStatus.SENT);
				}
				else if (sbSucc.length() == 0)
				{
					email.setStatus(EmailStatus.FAIL);
				}
				else
				{
					email.setStatus(EmailStatus.PARTIAL_SUCCESS);
				}
				email.setEmailsSuccess(sbSucc.toString());
				email.setEmailsFail(sbFail.toString());
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			email.setStatus(EmailStatus.FAIL);
		}
	}

	@Override
	public void run()
	{
		ZonedDateTime t = ZonedDateTime.now();
		T email;
		T newEmail;
		Iterator<T> itEmails = this.handler.getPendingEmails().iterator();
		while (itEmails.hasNext())
		{
			email = itEmails.next();
			try
			{
				newEmail = DataTools.cloneEntity(email);
				doQueueEmail(this.emailCtrl, newEmail);
				this.handler.updateEmailStatus(email, newEmail);
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		this.handler.endEmailChecking(t);
	}
	
	public boolean isServerOnline()
	{
		return this.emailCtrl.isServerOnline();
	}

	@Nonnull
	public String sendTestingEmail(@Nonnull String toAddress)
	{
		return this.emailCtrl.sendTestingEmail(toAddress);
	}

	@Nonnull
	public EmailMessageCreator getEmailMessageCreator()
	{
		return this.msgCreator;
	}
}
