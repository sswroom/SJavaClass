package org.sswr.util.net.email;

import java.time.ZonedDateTime;

import jakarta.annotation.Nonnull;

public interface EmailCheckHandler<T extends TemplateEmailStatus>
{
	@Nonnull
	public Iterable<T> getPendingEmails();
	public void updateEmailStatus(@Nonnull T current, @Nonnull T newStatus);
	public void endEmailChecking(@Nonnull ZonedDateTime startTime);
}
