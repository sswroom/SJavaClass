package org.sswr.util.data;

import org.sswr.util.data.textenc.URIEncoding;
import org.sswr.util.io.Path;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class URLString
{
	@Nullable
	public static String getURLFilePath(@Nonnull String url)
	{
		if (url.length() < 8 || !url.substring(0, 8).equalsIgnoreCase("FILE:///"))
			return null;
		if (Path.PATH_SEPERATOR == '\\')
		{
			return URIEncoding.uriDecode(url.substring(8)).replace('/', '\\');
		}
		else
		{
			return URIEncoding.uriDecode(url.substring(7));
		}
	}
	
	@Nonnull
	public static String getURLDomain(@Nonnull String url, @Nullable SharedInt port)
	{
		int i;
		int j;
		int k;
		i = url.indexOf("://");
		if (i != -1)
		{
			url = url.substring(i + 3);
		}
		k = url.indexOf('@');
		i = url.indexOf('/');
		if (k != -1 && i != -1 && k < i)
		{
			url = url.substring(k + 1);
			i -= k + 1;
		}
		j = url.indexOf(':');
		if (i != -1 && j != -1 && j < i)
		{
			if (port != null)
			{
				port.value = StringUtil.toIntegerS(url.substring(j + 1, i), 0);
			}
			if (i < j)
			{
				return url.substring(0, i);
			}
			else
			{
				return url.substring(0, j);
			}
		}
		else if (i != -1)
		{
			if (port != null)
			{
				port.value = 0;
			}
			return url.substring(0, i);
		}
		else if (j != -1)
		{
			if (port != null)
			{
				port.value = StringUtil.toIntegerS(url.substring(j + 1), 0);
			}
			return url.substring(0, j);
		}
		else
		{
			if (port != null)
			{
				port.value = 0;
			}
			return url;
		}
	}

	@Nullable
	public static String getURIScheme(@Nonnull String url)
	{
		int i = url.indexOf(':');
		if (i == -1)
		{
			return null;
		}
		return url.substring(0, i);
	}
	
	@Nonnull
	public static String getURLHost(@Nonnull String url)
	{
		int i;
		i = url.indexOf("://");
		if (i != -1)
		{
			url = url.substring(i + 3);
		}
		i = url.indexOf('/');
		if (i != -1)
		{
			return url.substring(0, i);
		}
		else
		{
			return url;
		}
	}
}
