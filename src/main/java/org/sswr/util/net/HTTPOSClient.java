package org.sswr.util.net;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.cert.Certificate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLPeerUnverifiedException;

import org.sswr.util.crypto.CertUtil;
import org.sswr.util.crypto.MyX509Cert;
import org.sswr.util.data.DateTimeUtil;
import org.sswr.util.data.SharedInt;

public class HTTPOSClient extends HTTPClient
{
	private static final boolean debug = false;
	private HttpURLConnection conn;
	private RequestMethod method;
	private boolean hasContType;
	private String userAgent;

	public HTTPOSClient(SocketFactory sockf, String userAgent, boolean kaConn)
	{
		super(sockf, kaConn);
		this.userAgent = userAgent;
	}

	@Override
	public boolean connect(String url, RequestMethod method, boolean defHeaders)
	{
		if (!url.startsWith("http://") && !url.startsWith("https://"))
		{
			return false;
		}
		this.hasContType = false;
		this.url = url;
		this.method = method;
		try
		{
			Proxy proxy = null;
			if (this.sockf != null)
			{
				proxy = this.sockf.getProxy();
			}
			URL targetURL = new URL(url);
			HttpURLConnection.setFollowRedirects(false);
			this.svrAddr = InetAddress.getByName(targetURL.getHost());
			this.conn = (HttpURLConnection)targetURL.openConnection(proxy);
			this.respCode = 0;
			switch (this.method)
			{
			case HTTP_POST:
				this.conn.setDoOutput(true);
				this.conn.setRequestMethod("POST");
				this.canWrite = true;
				break;
			case HTTP_PUT:
				this.conn.setDoOutput(true);
				this.conn.setRequestMethod("PUT");
				this.canWrite = true;
				break;
			case HTTP_PATCH:
				this.conn.setDoOutput(true);
				this.conn.setRequestMethod("PATCH");
				this.canWrite = true;
				break;
			case HTTP_GET:
				this.conn.setRequestMethod("GET");
				this.canWrite = false;
				break;
			case HTTP_CONNECT:
				this.conn.setRequestMethod("CONNECT");
				this.canWrite = false;
				break;
			case HTTP_DELETE:
				this.conn.setDoOutput(true);
				this.conn.setRequestMethod("DELETE");
				this.canWrite = true;
				break;
			case Unknown:
			default:
				this.conn.setRequestMethod("GET");
				this.canWrite = false;
				break;
			}
			if (this.userAgent != null)
			{
				this.conn.setRequestProperty("User-Agent", this.userAgent);
			}
			return true;
		}
		catch (IOException ex)
		{
			if (debug)
			{
				ex.printStackTrace();
			}
			return false;
		}
	}

	public void setReadTimeout(int timeoutMS)
	{
		this.conn.setReadTimeout(timeoutMS);
	}

	public boolean isError()
	{
		return this.conn == null;
	}

	public void addHeader(String name, String value)
	{
		if (name.equalsIgnoreCase("Content-Type"))
		{
			this.hasContType = true;
		}
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
		else if (this.canWrite && this.mstm != null)
		{
			this.canWrite = false;
			String s = "--"+this.boundary+"--\r\n";
			this.mstm.write(s.getBytes(StandardCharsets.UTF_8));

			byte []buff = this.mstm.getBuff();
			this.addContentLength(this.mstm.getLength());
			try
			{
				this.conn.getOutputStream().write(buff, 0, (int)this.mstm.getLength());
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

	public boolean isSecureConn()
	{
		return this.conn instanceof HttpsURLConnection;
	}

	public List<MyX509Cert> getServerCerts()
	{
		try
		{
			if (this.conn instanceof HttpsURLConnection)
			{
				HttpsURLConnection sslConn = (HttpsURLConnection)this.conn;
				Certificate[] certs = sslConn.getServerCertificates();
				List<MyX509Cert> ret = new ArrayList<MyX509Cert>();
				int i = 0;
				int j = certs.length;
				while (i < j)
				{
					MyX509Cert myCert = CertUtil.toMyCert(certs[i]);
					if (myCert != null)
						ret.add(myCert);
					i++;
				}
				return ret;
			}
		}
		catch (SSLPeerUnverifiedException ex)
		{
			ex.printStackTrace();
		}
		return null;
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
				if (!this.hasContType)
				{
					this.hasContType = true;
					this.addContentType("application/octet-stream");
				}
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
		HTTPOSClient cli = new HTTPOSClient(null, null, false);
		cli.connect(url, RequestMethod.HTTP_GET, false);
		if (cli.getRespStatus() != expectedStatusCode)
		{
			cli.close();
			return null;
		}
		byte[] buff = cli.readToEnd();
		cli.close();
		return buff;
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
		HTTPOSClient cli = new HTTPOSClient(null, null, false);
		cli.connect(url, RequestMethod.HTTP_GET, true);
		cli.addHeaders(customHeaders);
		if (cli.getRespStatus() <= 0)
		{
			cli.close();
			return null;
		}
		if (statusCode != null)
		{
			statusCode.value = cli.getRespStatus();
		}
		byte[] buff = cli.readToEnd();
		cli.close();
		return buff;
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

	public static byte[] formPostAsBytes(String url, Map<String, String> formParams, SharedInt statusCode, int timeoutMS)
	{
		return formPostAsBytes(url, formParams, null, statusCode, timeoutMS);
	}

	public static byte[] formPostAsBytes(String url, Map<String, String> formParams, Map<String, String> customHeaders, SharedInt statusCode, int timeoutMS)
	{
		HTTPOSClient cli = new HTTPOSClient(null, null, false);
		cli.connect(url, RequestMethod.HTTP_POST, true);
		if (timeoutMS != 0)
			cli.setReadTimeout(timeoutMS);
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
			statusCode.value = cli.getRespStatus();
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

	public static String formPostAsString(String url, Map<String, String> formParams, SharedInt statusCode, int timeoutMS)
	{
		byte []ret = formPostAsBytes(url, formParams, statusCode, timeoutMS);
		if (ret == null)
		{
			return null;
		}
		return new String(ret, StandardCharsets.UTF_8);
	}

	public static String formPostAsString(String url, Map<String, String> formParams, Map<String, String> customHeaders, SharedInt statusCode, int timeoutMS)
	{
		byte []ret = formPostAsBytes(url, formParams, customHeaders, statusCode, timeoutMS);
		if (ret == null)
		{
			return null;
		}
		return new String(ret, StandardCharsets.UTF_8);
	}
}
