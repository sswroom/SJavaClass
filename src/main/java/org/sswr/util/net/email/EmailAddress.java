package org.sswr.util.net.email;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.sswr.util.data.StringUtil;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class EmailAddress
{
	private @Nullable String name;
	private @Nonnull String address;

	public EmailAddress(@Nullable String name, @Nonnull String address) {
		this.name = name;
		this.address = address;
	}

	@Nullable
	public String getName() {
		return this.name;
	}

	public void setName(@Nullable String name) {
		this.name = name;
	}

	@Nonnull
	public String getAddress() {
		return this.address;
	}

	public void setAddress(@Nonnull String address) {
		this.address = address;
	}

	@Nonnull
	public EmailAddress name(@Nullable String name) {
		setName(name);
		return this;
	}

	@Nonnull
	public EmailAddress address(@Nonnull String address) {
		setAddress(address);
		return this;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof EmailAddress)) {
			return false;
		}
		EmailAddress emailAddress = (EmailAddress) o;
		return Objects.equals(name, emailAddress.name) && Objects.equals(address, emailAddress.address);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, address);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		String name = this.name;
		if (name != null && name.length() > 0)
		{
			if (StringUtil.isNonASCII(name))
			{
				sb.append(EmailTools.toUTF8Header(name));
			}
			else
			{
				sb.append('"');
				sb.append(name);
				sb.append('"');
			}
			sb.append(' ');
		}
		sb.append('<');
		sb.append(this.address);
		sb.append('>');
		return sb.toString();
	}

	@Nullable
	public static EmailAddress parse(@Nonnull String val)
	{
		int i;
		if (val.endsWith(">") && (i = val.indexOf("<")) >= 0)
		{
			EmailAddress addr = new EmailAddress(null, val.substring(i + 1, val.length() - 1));
			val = val.substring(0, i).trim();
			if (val.startsWith("\"") && val.endsWith("\""))
			{
				addr.setName(val.substring(1, val.length() - 1));
			}
			else if (val.length() > 0)
			{
				addr.setName(val);
			}
			return addr;
		}
		else if (StringUtil.isEmailAddress(val))
		{
			return new EmailAddress(null, val);
		}
		else
		{
			return null;
		}
	}

	@Nullable
	public static List<EmailAddress> parseList(@Nonnull String val)
	{
		if (val == null || val.length() == 0)
			return null;
		List<EmailAddress> ret = new ArrayList<EmailAddress>();
		String[] addrs = StringUtil.split(val, ",");
		int i = 0;
		int j = addrs.length;
		while (i < j)
		{
			addrs[i] = addrs[i].trim();
			EmailAddress addr = EmailAddress.parse(addrs[i]);
			if (addr == null)
			{
				return null;
			}
			else
			{
				ret.add(addr);
			}
			i++;
		}
		return ret;
	}
}
