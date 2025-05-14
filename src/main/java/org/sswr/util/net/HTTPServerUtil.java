package org.sswr.util.net;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;

import org.sswr.util.data.ByteArray;
import org.sswr.util.data.DateTimeUtil;
import org.sswr.util.data.StringUtil;
import org.sswr.util.data.textenc.URIEncoding;
import org.sswr.util.net.BrowserInfo.BrowserType;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class HTTPServerUtil {

	public static void addDefHeaders(@Nonnull HttpServletResponse resp, @Nonnull HttpServletRequest req)
	{
	}

	public static void addCacheControl(@Nonnull HttpServletResponse resp, int cacheAge)
	{
		if (cacheAge < 0)
		{
			resp.addHeader("Cache-Control", "public, max-age, immutable");
		}
		else if (cacheAge == 0)
		{
			resp.addHeader("Cache-Control", "no-cache, no-store, must-revalidate");
		}
		else
		{
			resp.addHeader("Cache-Control", "private; max-age="+cacheAge);
		}
	}

	public static void addTimeHeader(@Nonnull HttpServletResponse resp, @Nonnull String name, @Nonnull ZonedDateTime dt)
	{
		resp.addHeader(name, WebUtil.date2Str(dt));
	}

	public static void addTimeHeader(@Nonnull HttpServletResponse resp, @Nonnull String name, @Nonnull Timestamp ts)
	{
		addTimeHeader(resp, name, DateTimeUtil.newZonedDateTime(ts));
	}

	public static void addContentDisposition(@Nonnull HttpServletResponse resp, boolean isAttachment, @Nonnull String attFileName, @Nonnull BrowserType browser)
	{
		String s;
		if (isAttachment)
		{
			s = "attachment";
		}
		else
		{
			s = "inline";
		}
		if (attFileName != null)
		{
			s = s + "; filename=\"";
			if (browser == BrowserType.IE)
			{
				s = s + URIEncoding.uriEncode(attFileName);
			}
			else
			{
				s = s + URIEncoding.uriEncode(attFileName);
			}
			s = s + "\"";
		}
		resp.addHeader("Content-Disposition", s);
	}

	public static void addContentLength(@Nonnull HttpServletResponse resp, long contentLeng)
	{
		resp.addHeader("Content-Length", String.valueOf(contentLeng));
	}

	public static void addContentType(@Nonnull HttpServletResponse resp, @Nonnull String contentType)
	{
		resp.addHeader("Content-Type", contentType);
	}

	public static void addDate(@Nonnull HttpServletResponse resp, @Nonnull ZonedDateTime dt)
	{
		addTimeHeader(resp, "Date", dt);
	}

	public static void addExpireTime(@Nonnull HttpServletResponse resp, @Nullable ZonedDateTime dt)
	{
		if (dt == null)
		{
			resp.addHeader("Expires", "0");
		}
		else
		{
			addTimeHeader(resp, "Expires", dt);
		}
	}

	public static void addLastModified(@Nonnull HttpServletResponse resp, @Nonnull ZonedDateTime dt)
	{
		addTimeHeader(resp, "Last-Modified", dt);
	}

	public static void addLastModified(@Nonnull HttpServletResponse resp, @Nonnull Timestamp ts)
	{
		addTimeHeader(resp, "Last-Modified", ts);
	}

	public static void addServer(@Nonnull HttpServletResponse resp, @Nonnull String server)
	{
		resp.addHeader("Server", server);
	}

	public static void addAccessControlAllowOrigin(@Nonnull HttpServletResponse resp, @Nonnull String origin)
	{
		resp.addHeader("Access-Control-Allow-Origin", origin);
	}

	public static void responseText(@Nonnull HttpServletRequest req, @Nonnull HttpServletResponse resp, @Nonnull String content)
	{
		byte[] buff = content.getBytes(StandardCharsets.UTF_8);
		addDefHeaders(resp, req);
		addContentType(resp, "text/plain");
		addContentLength(resp, buff.length);
		sendContent(req, resp, "text/plain", buff);
	}

	public static boolean responseFile(@Nonnull HttpServletRequest req, @Nonnull HttpServletResponse resp, @Nonnull String fileName, int cacheAge)
	{
		String mime;
		long sizeLeft;
		File file = new File(fileName);
		if (!file.exists() || !file.isFile())
		{
			return false;
		}
		long lastMod = file.lastModified();
		String ifModSince;
		if ((ifModSince = req.getHeader("If-Modified-Since")) != null)
		{
			try
			{
				ZonedDateTime t2 = DateTimeUtil.parse(ifModSince);
				long time = t2.toEpochSecond();
				if (time / 1000 == lastMod / 1000)
				{
					resp.setStatus(StatusCode.NOT_MODIFIED);
					addDefHeaders(resp, req);
					addCacheControl(resp, cacheAge);
					addContentLength(resp, 0);
					return true;
				}
			}
			catch (DateTimeParseException ex)
			{
				ex.printStackTrace();
			}
		}

		mime = MIME.getMIMEFromFileName(fileName);

		sizeLeft = file.length();
		String range;
		FileInputStream fs = null;
		try
		{
			if ((range = req.getHeader("Range")) != null)
			{
				long fileSize = sizeLeft;
				if (!range.startsWith("bytes="))
				{
					resp.setStatus(StatusCode.REQUESTED_RANGE_NOT_SATISFIABLE);
					addDefHeaders(resp, req);
					addCacheControl(resp, cacheAge);
					addContentLength(resp, 0);
					return true;
				}
				if (range.indexOf(',') != -1)
				{
					resp.setStatus(StatusCode.REQUESTED_RANGE_NOT_SATISFIABLE);
					addDefHeaders(resp, req);
					addCacheControl(resp, cacheAge);
					addContentLength(resp, 0);
					return true;
				}
				long start = 0;
				long end = -1;
				int i = range.indexOf('-');
				if (i == -1)
				{
					resp.setStatus(StatusCode.REQUESTED_RANGE_NOT_SATISFIABLE);
					addDefHeaders(resp, req);
					addCacheControl(resp, cacheAge);
					addContentLength(resp, 0);
					return true;
				}
				String rangeStart = range.substring(6, i);
				if ((start = StringUtil.toLongS(rangeStart, -1)) < 0)
				{
					resp.setStatus(StatusCode.REQUESTED_RANGE_NOT_SATISFIABLE);
					addDefHeaders(resp, req);
					addCacheControl(resp, cacheAge);
					addContentLength(resp, 0);
					return true;
				}
				String rangeEnd = range.substring(i + 1);
				if (rangeEnd.length() > 0)
				{
					if ((end = StringUtil.toLongS(rangeEnd, -1)) < 0)
					{
						resp.setStatus(StatusCode.REQUESTED_RANGE_NOT_SATISFIABLE);
						addDefHeaders(resp, req);
						addCacheControl(resp, cacheAge);
						addContentLength(resp, 0);
						return true;
					}
					if (end <= start || end > sizeLeft)
					{
						resp.setStatus(StatusCode.REQUESTED_RANGE_NOT_SATISFIABLE);
						addDefHeaders(resp, req);
						addCacheControl(resp, cacheAge);
						addContentLength(resp, 0);
						return true;
					}
					sizeLeft = end - start;
				}
				else
				{
					sizeLeft = sizeLeft - start;
				}
				fs = new FileInputStream(file);
				fs.getChannel().position(start);
				resp.setStatus(StatusCode.PARTIAL_CONTENT);
				resp.addHeader("Content-Range", "bytes "+start+"-"+(start + sizeLeft - 1)+"/"+fileSize);
			}
			else
			{
				fs = new FileInputStream(file);
			}
			addDefHeaders(resp, req);
			addCacheControl(resp, cacheAge);
			addLastModified(resp, DateTimeUtil.newZonedDateTime(lastMod));
			addContentType(resp, mime);
			resp.addHeader("Accept-Ranges", "bytes");
			if (sizeLeft <= 0)
			{
				sendContent(req, resp, mime, fs.readAllBytes());
			}
			else
			{
				sendContent(req, resp, mime, fs.readNBytes((int)sizeLeft));
			}
			fs.close();
			return true;
		}
		catch (IOException ex)
		{
			if (fs != null)
			{
				try
				{
					fs.close();
				}
				catch (IOException ex2)
				{
				}
			}
			ex.printStackTrace();
			return false;
		}
	}

	public static boolean sendContent(@Nonnull HttpServletRequest req, @Nonnull HttpServletResponse resp, @Nonnull String mime, @Nonnull byte[] content)
	{
		addContentLength(resp, content.length);
		try
		{
			resp.getOutputStream().write(content);
			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}

	public static boolean sendContent(@Nonnull HttpServletRequest req, @Nonnull HttpServletResponse resp, @Nonnull String mime, @Nonnull ByteArray content)
	{
		addContentLength(resp, content.getBytesLength());
		try
		{
			resp.getOutputStream().write(content.getBytes(), content.getBytesOffset(), content.getBytesLength());
			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}

	public static boolean isForwardedSSL(@Nonnull HttpServletRequest req)
	{
		String s;
		if ((s = req.getHeader("Forwarded")) != null)
		{
			return s.indexOf("proto=https") >= 0;
		}
		if ((s = req.getHeader("X-Forwarded-Proto")) != null)
		{
			return s.equals("https");
		}
		if ((s = req.getHeader("X-Forwarded-Ssl")) != null)
		{
			return s.equals("on");
		}
		if ((s = req.getHeader("Front-End-Https")) != null)
		{
			return s.equals("on");
		}
		if ((s = req.getHeader("X-Url-Scheme")) != null)
		{
			return s.equals("https");
		}
		return false;
	}

	@Nullable
	public static String getForwardedAddr(@Nonnull HttpServletRequest req)
	{
		String s;
		if ((s = req.getHeader("Forwarded")) != null)
		{
			String[] sarr = StringUtil.split(s, ";");
			int i = 0;
			int j = sarr.length;
			while (i < j)
			{
				if (sarr[i].startsWith("for="))
				{
					String[] sarr2 = StringUtil.split(sarr[i].substring(4), ",");
					return sarr2[0].trim();
				}
				i++;
			}
		}
		if ((s = req.getHeader("X-Forwarded-For")) != null)
		{
			String[] sarr = StringUtil.split(s, ",");
			return sarr[0].trim();
		}
		return req.getRemoteAddr();
	}

	public static boolean redirectURL(@Nonnull HttpServletResponse resp, @Nonnull HttpServletRequest req, @Nonnull String url, int cacheAge)
	{
		addDefHeaders(resp, req);
		resp.setStatus(StatusCode.MOVED_TEMPORARILY);
		addCacheControl(resp, cacheAge);
		resp.addHeader("Location", url);
		addContentLength(resp, 0);
		return true;
	}
}
