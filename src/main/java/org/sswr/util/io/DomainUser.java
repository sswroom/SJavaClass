package org.sswr.util.io;

public class DomainUser
{
	public static String removeUserDomain(String username)
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
