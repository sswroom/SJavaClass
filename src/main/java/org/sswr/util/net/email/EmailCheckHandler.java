package org.sswr.util.net.email;

import java.time.ZonedDateTime;

public interface EmailCheckHandler<T extends TemplateEmailStatus>
{
	public Iterable<T> getPendingEmails();
	public void updateEmailStatus(T current, T newStatus);
	public void endEmailChecking(ZonedDateTime startTime);
}
