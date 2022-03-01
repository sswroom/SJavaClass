package org.sswr.util.net;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Iterator;
import java.util.Map;

import org.sswr.util.data.DateTimeUtil;
import org.sswr.util.data.SharedInt;
import org.sswr.util.data.textenc.URIEncoding;
import org.sswr.util.io.IOStream;

public class HTTPMyClient extends IOStream
{
	private static final boolean debug = false;
	private HttpURLConnection conn;
	private String method;
	private String url;
	private StringBuilder sbForm;
	private boolean canWrite;
	private InetAddress svrAddr;
	private int respCode;

	public HTTPMyClient(String url, String method) throws IOException
	{
		super(url);
		if (!url.startsWith("http://") && !url.startsWith("https://"))
		{
			throw new IOException("Not http/https request");
		}
		this.url = url;
		this.method = method;
		URL targetURL = new URL(url);
		HttpURLConnection.setFollowRedirects(false);
		this.svrAddr = InetAddress.getByName(targetURL.getHost());
		this.conn = (HttpURLConnection)targetURL.openConnection();
		this.conn.setRequestMethod(method);
		this.conn.setDoOutput(true);
		this.respCode = 0;
		switch (this.method)
		{
		case "POST":
		case "PUT":
		case "PATCH":
			this.canWrite = true;
			break;
		default:
			this.canWrite = false;
		}
	}

	public void addHeader(String name, String value)
	{
		this.conn.addRequestProperty(name, value);
	}

	public void addHeaders(Map<String, String> headers)
	{
		if (headers != null)
		{
			Iterator<String> hdrNames = headers.keySet().iterator();
			while (hdrNames.hasNext())
			{
				String name = hdrNames.next();
				this.addHeader(name, headers.get(name));
			}
		}
	}

	public boolean formBegin()
	{
		if (this.canWrite && this.sbForm == null)
		{
			this.addContentType("application/x-www-form-urlencoded");
			this.sbForm = new StringBuilder();
			return true;
		}
		return false;
	}

	public boolean formAdd(String name, String value)
	{
		if (this.sbForm == null)
		{
			return false;
		}
		if (this.sbForm.length() > 0)
		{
			this.sbForm.append('&');
		}
		this.sbForm.append(URIEncoding.uriEncode(name));
		this.sbForm.append('=');
		this.sbForm.append(URIEncoding.uriEncode(value));
		return true;
	}

	public void addTimeHeader(String name, ZonedDateTime dt)
	{
		this.addHeader(name, date2Str(dt));
	}

	public void addContentType(String contType)
	{
		this.addHeader("Content-Type", contType);
	}

	public void addContentLength(long leng)
	{
		this.addHeader("Content-Length", String.valueOf(leng));
	}

	public int getRespHeaderCnt()
	{
		return this.conn.getHeaderFields().size() - 1;
	}

	public String getRespHeader(int index)
	{
		String name = this.conn.getHeaderFieldKey(index + 1);
		if (name == null)
		{
			return null;
		}
		else
		{
			return name+": "+this.conn.getHeaderField(index + 1);
		}
	}

	public String getRespHeader(String name)
	{
		String val = this.conn.getHeaderField(name);
		if (val == null)
		{
			return null;
		}
		return name+": "+this.conn.getHeaderField(name);
	}

	public long getContentLength()
	{
		return this.conn.getContentLengthLong();
	}

	public String getContentEncoding()
	{
		return this.conn.getContentEncoding();
	}
	
	public ZonedDateTime getLastModified()
	{
		long mod = this.conn.getLastModified();
		if (mod == 0)
		{
			return null;
		}
		return DateTimeUtil.newZonedDateTime(mod);
	}

	public String getURL()
	{
		return this.url;
	}

	public void endRequest()
	{
		if (this.canWrite && this.sbForm != null)
		{
			this.canWrite = false;
			byte []buff = sbForm.toString().getBytes(StandardCharsets.UTF_8);
			this.addContentLength(buff.length);
			try
			{
				this.conn.getOutputStream().write(buff);
			}
			catch (IOException ex)
			{
				if (debug)
				{
					ex.printStackTrace();
				}
			}
		}
		if (this.respCode == 0)
		{
			try
			{
				this.respCode = this.conn.getResponseCode();
			}
			catch (IOException ex)
			{
				if (debug)
				{
					ex.printStackTrace();
				}
			}
		}
	}

	public int GetRespStatus() throws IOException
	{
		this.endRequest();
		return this.respCode;
	}

	public InetAddress getSvrAddr() throws IOException
	{
		return this.svrAddr;
	}

	public static String date2Str(ZonedDateTime dt)
	{
		String wds[] = {"Mon, ", "Tue, ", "Wed, ", "Thu, ", "Fri, ", "Sat, ", "Sun, "};
		ZonedDateTime t = dt.withZoneSameInstant(ZoneOffset.UTC);
		DayOfWeek wd = t.getDayOfWeek();
		return wds[wd.ordinal()] + DateTimeUtil.toString(t, "dd MMM yyyy HH:mm:ss") + " GMT";
	}

	@Override
	public int read(byte[] buff, int ofst, int size)
	{
		this.endRequest();
		try
		{
			int ret;
			if (this.respCode >= 400 && this.respCode < 600)
			{
				ret = this.conn.getErrorStream().read(buff, ofst, size);
			}
			else
			{
				ret = this.conn.getInputStream().read(buff, ofst, size);
			}
			if (debug)
			{
				System.out.println("read = "+ret);
			}
			if (ret < 0)
			{
				return 0;
			}
			return ret;
		}
		catch (Exception ex)
		{
			if (debug)
			{
				ex.printStackTrace();
			}
			return 0;
		}
	}

	@Override
	public int write(byte[] buff, int ofst, int size) {
		try
		{
			if (this.canWrite)
			{
				this.conn.getOutputStream().write(buff, ofst, size);
				return size;
			}
			else
			{
				return 0;
			}
		}
		catch (Exception ex)
		{
			return 0;
		}
	}

	@Override
	public int flush() {
		return 0;
	}

	@Override
	public void close() {
		this.conn.disconnect();
	}

	@Override
	public boolean recover() {
		return false;
	}

	public static byte[] getAsBytes(String url, int expectedStatusCode)
	{
		try
		{
			HTTPMyClient cli = new HTTPMyClient(url, "GET");
			if (cli.GetRespStatus() != expectedStatusCode)
			{
				cli.close();
				return null;
			}
			byte[] buff = cli.readToEnd();
			cli.close();
			return buff;
		}
		catch (IOException ex)
		{
			if (debug)
			{
				ex.printStackTrace();
			}
			return null;
		}
	}

	public static String getAsString(String url, int expectedStatusCode)
	{
		byte []ret = getAsBytes(url, expectedStatusCode);
		if (ret == null)
		{
			return null;
		}
		return new String(ret, StandardCharsets.UTF_8);
	}

	public static byte[] getAsBytes(String url, SharedInt statusCode)
	{
		return getAsBytes(url, null, statusCode);
	}

	public static byte[] getAsBytes(String url, Map<String, String> customHeaders, SharedInt statusCode)
	{
		try
		{
			HTTPMyClient cli = new HTTPMyClient(url, "GET");
			cli.addHeaders(customHeaders);
			if (cli.GetRespStatus() <= 0)
			{
				cli.close();
				return null;
			}
			if (statusCode != null)
			{
				statusCode.value = cli.GetRespStatus();
			}
			byte[] buff = cli.readToEnd();
			cli.close();
			return buff;
		}
		catch (IOException ex)
		{
			if (debug)
			{
				ex.printStackTrace();
			}
			return null;
		}
	}

	public static String getAsString(String url, SharedInt statusCode)
	{
		byte []ret = getAsBytes(url, statusCode);
		if (ret == null)
		{
			return null;
		}
		return new String(ret, StandardCharsets.UTF_8);
	}

	public static byte[] formPostAsBytes(String url, Map<String, String> formParams, SharedInt statusCode)
	{
		return formPostAsBytes(url, formParams, null, statusCode);
	}

	public static byte[] formPostAsBytes(String url, Map<String, String> formParams, Map<String, String> customHeaders, SharedInt statusCode)
	{
		try
		{
			HTTPMyClient cli = new HTTPMyClient(url, "POST");
			cli.addHeaders(customHeaders);
			Iterator<String> names = formParams.keySet().iterator();
			if (names != null && names.hasNext())
			{
				cli.formBegin();
				while (names.hasNext())
				{
					String name = names.next();
					cli.formAdd(name, formParams.get(name));
				}
			}
			if (statusCode != null)
			{
				statusCode.value = cli.GetRespStatus();
			}
			if (debug)
			{
				int i = 0;
				int j = cli.getRespHeaderCnt();
				while (i < j)
				{
					System.out.println("Header "+i+" = "+cli.getRespHeader(i));
					i++;
				}
			}
			byte[] buff = cli.readToEnd();
			cli.close();
			return buff;
		}
		catch (IOException ex)
		{
			if (debug)
			{
				ex.printStackTrace();
			}
			return null;
		}
	}

	public static String formPostAsString(String url, Map<String, String> formParams, SharedInt statusCode)
	{
		byte []ret = formPostAsBytes(url, formParams, statusCode);
		if (ret == null)
		{
			return null;
		}
		return new String(ret, StandardCharsets.UTF_8);
	}

	public static String formPostAsString(String url, Map<String, String> formParams, Map<String, String> customHeaders, SharedInt statusCode)
	{
		byte []ret = formPostAsBytes(url, formParams, customHeaders, statusCode);
		if (ret == null)
		{
			return null;
		}
		return new String(ret, StandardCharsets.UTF_8);
	}
}
