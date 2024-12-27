package org.sswr.util.net.email;

import java.util.List;

import org.sswr.util.data.StringUtil;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class NullEmailControl implements EmailControl
{
	@Override
	public boolean sendMail(@Nonnull EmailMessage msg, @Nullable String toList, @Nullable String ccList, @Nullable String bccList)
	{
		return false;
	}

	@Override
	public boolean sendBatchMail(@Nonnull EmailMessage msg, @Nonnull List<String> toList)
	{
		return false;
	}

	@Override
	public boolean isServerOnline()
	{
		return true;
	}

	@Override
	public boolean validateDestAddr(@Nonnull String addr)
	{
		return StringUtil.isEmailAddress(addr);
	}

	@Override
	@Nonnull
	public String sendTestingEmail(@Nonnull String toAddress) {
		return "Sent";
	}
}
