package org.sswr.util.web;

import java.util.Iterator;

import org.sswr.util.data.DataTools;
import org.sswr.util.io.ResourceLoader;

public class EmailChecker<T extends TemplateEmailStatus> implements Runnable
{
	private Class<T> cls;
	private EmailCheckHandler<T> handler;
	private EmailControl emailCtrl;

	public EmailChecker(Class<T> cls, EmailCheckHandler<T> handler, EmailControl emailCtrl)
	{
		this.cls = cls;
		this.handler = handler;
		this.emailCtrl = emailCtrl;
	}

	private void doQueueEmail(EmailControl emailCtrl, TemplateEmailStatus email)
	{
		try
		{
			EmailTemplate template = new EmailTemplate(ResourceLoader.load(this.cls, "email/"+email.getTplname()+".txt", null), email.getParamObj());
			if (email.getItemParams() != null)
			{
				template.addItems(email.getItemParamsObj());
			}
			if (email.getEmails() == null || email.getEmails().length() == 0)
			{
				email.setStatus(EmailStatus.NO_ADDRESS);
			}
			else
			{
				String emailAddrs[] = email.getEmails().split(",");
				String emailAddr;
				StringBuilder sbSucc = new StringBuilder();
				StringBuilder sbFail = new StringBuilder();
				int i = 0;
				int j = emailAddrs.length;
				while (i < j)
				{
					emailAddr = emailAddrs[i].trim();
					if (emailCtrl.sendMail(template, emailAddr))
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
	
	public boolean isServerValid()
	{
		return this.emailCtrl.isServerValid();
	}

	public String testEmail(String toAddress)
	{
		return this.emailCtrl.testEmail(toAddress);
	}
}
