package org.sswr.util.net.email;

public interface EmailCheckHandler<T extends TemplateEmailStatus>
{
	public Iterable<T> getPendingEmails();
	public void updateEmailStatus(T current, T newStatus);
	public void endEmailChecking(long startTimeTicks);
}
