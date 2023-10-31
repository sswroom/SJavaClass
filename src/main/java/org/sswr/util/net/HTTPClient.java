package org.sswr.util.net;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import org.sswr.util.basic.HiResClock;
import org.sswr.util.crypto.MyX509Cert;
import org.sswr.util.data.textenc.FormEncoding;
import org.sswr.util.data.textenc.URIEncoding;
import org.sswr.util.io.IOStream;
import org.sswr.util.io.MemoryStream;

public abstract class HTTPClient extends IOStream
{
	protected SocketFactory sockf;
	protected HiResClock clk;

	protected InetAddress svrAddr;
	protected boolean canWrite;
	protected StringBuilder sbForm;
	protected String boundary;
	protected MemoryStream mstm;
	protected String url;
	protected int respCode;

	protected HTTPClient(SocketFactory sockf, boolean kaConn)
	{
		super("HTTPClient");
		this.sockf = sockf;
		this.canWrite = false;
		this.svrAddr = null;
		this.sbForm = null;
		this.boundary = null;
		this.mstm = null;
		this.url = null;
		this.respCode = 0;
//		this.kaConn = kaConn;
	}

	public abstract boolean isError();

	public boolean isDown()
	{
		return this.isError();
	}

	public abstract void addHeader(String name, String value);
	public abstract void addHeaders(Map<String, String> headers);
	public abstract void endRequest();
	
	public abstract boolean isSecureConn();
	public abstract List<MyX509Cert> getServerCerts();

	public boolean formBegin()
	{
		return formBegin(false);
	}

	public boolean formBegin(boolean hasFile)
	{
		if (this.canWrite && this.sbForm == null && this.boundary == null)
		{
			if (hasFile)
			{
				this.boundary = "---------------------------Boundary" + System.currentTimeMillis();
				this.mstm = new MemoryStream();
				this.addContentType("multipart/form-data; boundary="+this.boundary);
			}
			else
			{
				this.addContentType("application/x-www-form-urlencoded");
				this.sbForm = new StringBuilder();
				return true;
			}
		}
		return false;
	}

	public boolean formAdd(String name, String value)
	{
		if (this.sbForm != null)
		{
			if (this.sbForm.length() > 0)
			{
				this.sbForm.append('&');
			}
			this.sbForm.append(URIEncoding.uriEncode(name));
			this.sbForm.append('=');
			this.sbForm.append(URIEncoding.uriEncode(value));
			return true;
		}
		else if (this.boundary != null)
		{
			String s = "--"+this.boundary+"\r\nContent-Disposition: form-data; name=\""+FormEncoding.formEncode(name)+"\"\r\n\r\n"+URIEncoding.uriEncode(value)+"\r\n";
			this.mstm.write(s.getBytes(StandardCharsets.UTF_8));
			return true;
		}
		return false;
	}

	public boolean formAddFile(String name, File filePath)
	{
		if (this.boundary != null)
		{
			FileInputStream fis = null;
			try
			{
				fis = new FileInputStream(filePath);
				long fileLength = fis.getChannel().size();
				if (fileLength > 0 && fileLength < 104857600)
				{
					StringBuilder sb = new StringBuilder();
					sb.append("--");
					sb.append(this.boundary);
					sb.append("\r\nContent-Disposition: form-data; ");
					sb.append("name=\"");
					sb.append(FormEncoding.formEncode(name));
					sb.append("\"; ");
					sb.append("filename=\"");
					sb.append(FormEncoding.formEncode(filePath.getName()));
					sb.append("\"\r\n");

					String mime = MIME.getMIMEFromFileName(filePath.getName());
					sb.append("Content-Type: ");
					sb.append(mime);
					sb.append("\r\n\r\n");
					byte[] fileCont = fis.readAllBytes();
					this.mstm.write(sb.toString().getBytes(StandardCharsets.UTF_8));
					this.mstm.write(fileCont);
					this.mstm.write("\r\n".getBytes());
					fis.close();
					return true;
				}
				fis.close();
				return false;
			}
			catch (FileNotFoundException ex)
			{
				ex.printStackTrace();
				return false;
			}
			catch (IOException ex)
			{
				ex.printStackTrace();
				if (fis != null)
				{
					try
					{
						fis.close();
					}
					catch (IOException ex2)
					{

					}
				}
				return false;
			}
		}
		return false;
	}

	public void addTimeHeader(String name, ZonedDateTime dt)
	{
		this.addHeader(name, WebUtil.date2Str(dt));
	}

	public void addContentType(String contType)
	{
		this.addHeader("Content-Type", contType);
	}

	public void addContentLength(long leng)
	{
		this.addHeader("Content-Length", String.valueOf(leng));
	}


	public abstract int getRespHeaderCnt();
	public abstract String getRespHeader(int index);
	public abstract String getRespHeader(String name);
	public abstract long getContentLength();
	public abstract String getContentEncoding();
	public abstract ZonedDateTime getLastModified();

	public String getURL()
	{
		return this.url;
	}

	public int getRespStatus()
	{
		this.endRequest();
		return this.respCode;
	}

	public InetAddress getSvrAddr()
	{
		return this.svrAddr;
	}

	public static HTTPClient createClient(SocketFactory sockf, SSLEngine ssl, String userAgent, boolean kaConn, boolean isSecure)
	{
		return null;
	}

	public static HTTPClient createConnect(SocketFactory sockf, SSLEngine ssl, String url, RequestMethod method, boolean kaConn)
	{
		try
		{
			return new HTTPMyClient(url, method);
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
			return null;
		}
	}
	
	public static boolean isHTTPURL(String url)
	{
		if (url == null)
		{
			return false;
		}
		return url.startsWith("http://") || url.startsWith("https://");
	}

	public static void prepareSSL(SSLEngine ssl)
	{
	}

	public static boolean loadContent(SocketFactory sockf, SSLEngine ssl, String url, IOStream stm, long maxSize)
	{
		HTTPClient cli = HTTPClient.createConnect(sockf, ssl, url, RequestMethod.HTTP_GET, true);
		if (cli == null)
			return false;
		if (cli.getRespStatus() != 200)
		{
			cli.close();
			return false;
		}
		byte[] buff = new byte[2048];
		int readSize;
		while ((readSize = cli.read(buff, 0, buff.length)) > 0)
		{
			if (readSize > maxSize)
			{
				cli.close();
				return false;
			}
			stm.write(buff, 0, readSize);
			maxSize -= readSize;
		}
		cli.close();
		return true;
	}

	public static boolean loadContent(SocketFactory sockf, SSLEngine ssl, String url, StringBuilder sb, long maxSize)
	{
		HTTPClient cli = HTTPClient.createConnect(sockf, ssl, url, RequestMethod.HTTP_GET, true);
		if (cli == null)
			return false;
		if (cli.getRespStatus() != 200)
		{
			cli.close();
			return false;
		}
		byte []buff = new byte[2048];
		int readSize;
		while ((readSize = cli.read(buff, 0, buff.length)) > 0)
		{
			if (readSize > maxSize)
			{
				cli.close();
				return false;
			}
			sb.append(new String(buff, 0, readSize, StandardCharsets.UTF_8));
			maxSize -= readSize;
		}
		cli.close();
		return true;
	}
}
