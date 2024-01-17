package org.sswr.util.net.email;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.sswr.util.data.StringUtil;

public class EmailAddress
{
	private String name;
	private String address;

	public EmailAddress() {
	}

	public EmailAddress(String name, String address) {
		this.name = name;
		this.address = address;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAddress() {
		return this.address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public EmailAddress name(String name) {
		setName(name);
		return this;
	}

	public EmailAddress address(String address) {
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

	public static EmailAddress parse(String val)
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

	public static List<EmailAddress> parseList(String val)
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
