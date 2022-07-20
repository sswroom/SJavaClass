package org.sswr.util.net.email;

import java.util.Iterator;

import org.sswr.util.data.DataTools;

public class EmailChecker<T extends TemplateEmailStatus> implements Runnable
{
	private EmailTemplateCreator tplCreator;
	private EmailCheckHandler<T> handler;
	private EmailControl emailCtrl;
	private boolean splitDestAddr;

	public EmailChecker(EmailTemplateCreator tplCreator, EmailCheckHandler<T> handler, EmailControl emailCtrl, boolean splitDestAddr)
	{
		this.tplCreator = tplCreator;
		this.handler = handler;
		this.emailCtrl = emailCtrl;
		this.splitDestAddr = splitDestAddr;
	}

	private void sendEmails(EmailTemplate template, String toAddrs, String ccAddrs, StringBuilder sbSucc, StringBuilder sbFail)
	{
		if (this.splitDestAddr)
		{
			String emailAddrs[] = toAddrs.split(",");
			String emailAddr;
			int i = 0;
			int j = emailAddrs.length;
			while (i < j)
			{
				emailAddr = emailAddrs[i].trim();
				if (emailCtrl.sendMail(template, emailAddr, null))
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
			if (ccAddrs != null && ccAddrs.length() > 0)
			{
				sendEmails(template, ccAddrs, null, sbSucc, sbFail);
			}
		}
		else
		{
			if (emailCtrl.sendMail(template, toAddrs, ccAddrs))
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

	private void doQueueEmail(EmailControl emailCtrl, TemplateEmailStatus email)
	{
		try
		{
			EmailTemplate template = this.tplCreator.createTemplate(email.getTplname(), email.getParamObj());
			if (email.getItemParams() != null)
			{
				template.addItems(email.getItemParamsObj());
			}
			if (email.getToEmails() == null || email.getToEmails().length() == 0)
			{
				email.setStatus(EmailStatus.NO_ADDRESS);
			}
			else
			{
				StringBuilder sbSucc = new StringBuilder();
				StringBuilder sbFail = new StringBuilder();
				sendEmails(template, email.getToEmails(), email.getCcEmails(), sbSucc, sbFail);
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
		long t = System.currentTimeMillis();
		T email;
		T newEmail;
		Iterator<T> itEmails = this.handler.getPendingEmails().iterator();
		while (itEmails.hasNext())
		{
			email = itEmails.next();
			newEmail = DataTools.cloneEntity(email);
			doQueueEmail(this.emailCtrl, newEmail);
			this.handler.updateEmailStatus(email, newEmail);
		}
		this.handler.endEmailChecking(t);
	}
	
	public boolean isServerOnline()
	{
		return this.emailCtrl.isServerOnline();
	}

	public String sendTestingEmail(String toAddress)
	{
		return this.emailCtrl.sendTestingEmail(toAddress);
	}
}
