package org.sswr.util.net.email;

import java.util.List;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public interface EmailControl
{
	public boolean sendMail(@Nonnull EmailMessage message, @Nullable String toList, @Nullable String ccList, @Nullable String bccList);
	public boolean sendBatchMail(@Nonnull EmailMessage message, @Nonnull List<String> toList);
	public boolean isServerOnline();
	public boolean validateDestAddr(@Nonnull String addr);
	@Nonnull
	public String sendTestingEmail(@Nonnull String toAddress);
}
