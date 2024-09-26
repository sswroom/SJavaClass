package org.sswr.util.io;

import jakarta.annotation.Nonnull;

public class DomainUser
{
	public static String removeUserDomain(@Nonnull String username)
	{
		int i;
		if ((i = username.lastIndexOf("\\")) >= 0)
		{
			return username.substring(i + 1);
		}
		else if ((i = username.indexOf("@")) >= 0)
		{
			return username.substring(0, i);
		}
		else
		{
			return username;
		}
	}
}
