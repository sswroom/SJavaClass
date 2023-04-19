package org.sswr.util.net.email;

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
		if (name == null)
		{
			return this.address;
		}
		else
		{
			return "\""+this.name+"\" <"+this.address+">";
		}
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
}
