package org.sswr.util.net;

import org.sswr.util.data.URLString;
import org.sswr.util.io.FileStream;
import org.sswr.util.io.IOStream;
import org.sswr.util.io.LogTool;
import org.sswr.util.io.Path;
import org.sswr.util.io.PathType;
import org.sswr.util.io.FileStream.BufferType;
import org.sswr.util.io.FileStream.FileMode;
import org.sswr.util.io.FileStream.FileShare;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class URL {

	public static IOStream openStream(@Nonnull String url, @Nullable String userAgent, @Nonnull TCPClientFactory clif, @Nullable SSLEngine ssl, int timeout, @Nonnull LogTool log)
	{
		IOStream stm;
		if (Path.getPathType(url) == PathType.File)
		{
			return new FileStream(url, FileMode.ReadOnly, FileShare.DenyNone, BufferType.Normal);
		}
		if (url.toLowerCase().startsWith("http://"))
		{
			HTTPClient cli = HTTPClient.createClient(clif, ssl, userAgent, true, false);
			cli.setReadTimeout(timeout);
			cli.connect(url, RequestMethod.HTTP_GET, null, null, true);
			if (cli.getRespStatus() == StatusCode.MOVED_TEMPORARILY || cli.getRespStatus() == StatusCode.MOVED_PERMANENTLY)
			{
				String newUrl = cli.getRespHeader("Location");
				if (newUrl != null && newUrl.length() > 0 && !newUrl.equals(url) && (newUrl.startsWith("http://") || newUrl.startsWith("https://")))
				{
					stm = openStream(newUrl, userAgent, clif, ssl, timeout, log);
					cli.close();
					return stm;
				}
			}
			return cli;
		}
		else if (url.toLowerCase().startsWith("https://"))
		{
			HTTPClient cli = HTTPClient.createClient(clif, ssl, userAgent, true, true);
			cli.setReadTimeout(timeout);
			cli.connect(url, RequestMethod.HTTP_GET, null, null, true);
			if (cli.getRespStatus() == StatusCode.MOVED_TEMPORARILY || cli.getRespStatus() == StatusCode.MOVED_PERMANENTLY)
			{
				String newUrl = cli.getRespHeader("Location");
				if (newUrl != null && newUrl.length() > 0 && !newUrl.equals(url) && (newUrl.startsWith("http://") || newUrl.startsWith("https://")))
				{
					stm = openStream(newUrl, userAgent, clif, ssl, timeout, log);
					cli.close();
					return stm;
				}
			}
			return cli;
		}
		else if (url.toLowerCase().startsWith("file:///"))
		{
			String newPath = URLString.getURLFilePath(url);
			if (newPath != null)
			{
				return new FileStream(newPath, FileMode.ReadOnly, FileShare.DenyNone, BufferType.Normal);
			}
		}
		return null;
	}
}
