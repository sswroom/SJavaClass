package org.sswr.util.net;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.sswr.util.basic.HiResClock;
import org.sswr.util.crypto.MyX509Cert;
import org.sswr.util.data.SharedDouble;
import org.sswr.util.data.textenc.FormEncoding;
import org.sswr.util.data.textenc.URIEncoding;
import org.sswr.util.io.IOStream;
import org.sswr.util.io.MemoryStream;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public abstract class HTTPClient extends IOStream
{
	@Nonnull protected TCPClientFactory clif;
	protected HiResClock clk;

	protected InetAddress svrAddr;
	protected boolean canWrite;
	protected StringBuilder sbForm;

	protected long contLeng;
	protected int respCode;

	protected String boundary;
	protected MemoryStream mstm;

	protected boolean kaConn;
	protected String url;
	protected long totalUpload;
	protected long totalDownload;

	protected HTTPClient(@Nonnull TCPClientFactory clif, boolean kaConn)
	{
		super("HTTPClient");
		this.clif = clif;
		this.canWrite = false;
		this.contLeng = 0;
		this.respCode = 0;
		this.url = null;
		this.sbForm = null;
		this.boundary = null;
		this.mstm = null;
		this.totalUpload = 0;
		this.totalDownload = 0;
		this.kaConn = kaConn;
		this.svrAddr = null;
	}

	public abstract boolean isError();

	public boolean isDown()
	{
		return this.isError();
	}

	public abstract boolean connect(@Nonnull String url, @Nonnull RequestMethod method, boolean defHeaders);

	public abstract void addHeader(@Nonnull String name, @Nonnull String value);
	public void addHeaders(@Nonnull Map<String, String> headers)
	{
		Iterator<String> hdrNames = headers.keySet().iterator();
		while (hdrNames.hasNext())
		{
			String name = hdrNames.next();
			this.addHeader(name, headers.get(name));
		}
	}

	public abstract void endRequest(@Nullable SharedDouble timeReq, @Nullable SharedDouble timeResp);
	public abstract void setReadTimeout(int timeoutMS);
	
	public abstract boolean isSecureConn();
	@Nullable
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

	public boolean formAdd(@Nonnull String name, @Nonnull String value)
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

	public boolean formAddFile(@Nonnull String name, @Nonnull File filePath)
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

	public void addTimeHeader(@Nonnull String name, @Nonnull ZonedDateTime dt)
	{
		this.addHeader(name, WebUtil.date2Str(dt));
	}

	public void addContentType(@Nonnull String contType)
	{
		this.addHeader("Content-Type", contType);
	}

	public void addContentLength(long leng)
	{
		this.addHeader("Content-Length", String.valueOf(leng));
	}


	public abstract int getRespHeaderCnt();
	@Nullable
	public abstract String getRespHeader(int index);
	@Nullable
	public abstract String getRespHeader(@Nonnull String name);

	public long getContentLength()
	{
		this.endRequest(null, null);
		return this.contLeng;
	}

	@Nullable
	public abstract String getContentEncoding();
	@Nullable
	public abstract ZonedDateTime getLastModified();

	@Nullable
	public String getContentType()
	{
		String contType = getRespHeader("Content-Type");
		if (contType == null)
			return null;
		int i = contType.indexOf(":");
		if (i < 0)
			return null;
		return contType.substring(i + 1).trim();
	}

	@Nullable
	public String getURL()
	{
		return this.url;
	}

	public int getRespStatus()
	{
		this.endRequest(null, null);
		return this.respCode;
	}

	@Nullable
	public InetAddress getSvrAddr()
	{
		return this.svrAddr;
	}

	@Nonnull
	public static HTTPClient createClient(@Nullable SocketFactory sockf, @Nullable SSLEngine ssl, @Nullable String userAgent, boolean kaConn, boolean isSecure)
	{
		return new HTTPOSClient(sockf, userAgent, kaConn);
	}

	@Nonnull
	public static HTTPClient createConnect(@Nullable SocketFactory sockf, @Nullable SSLEngine ssl, @Nonnull String url, @Nonnull RequestMethod method, boolean kaConn)
	{
		HTTPClient cli = createClient(sockf, ssl, null, kaConn, url.toUpperCase().startsWith("HTTPS://"));
		cli.connect(url, method, true);
		return cli;
	}
	
	public static boolean isHTTPURL(@Nonnull String url)
	{
		return url.startsWith("http://") || url.startsWith("https://");
	}

	public static void prepareSSL(@Nullable SSLEngine ssl)
	{
	}

	public static boolean loadContent(@Nullable SocketFactory sockf, @Nullable SSLEngine ssl, @Nonnull String url, @Nonnull IOStream stm, long maxSize)
	{
		HTTPClient cli = HTTPClient.createConnect(sockf, ssl, url, RequestMethod.HTTP_GET, true);
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

	public static boolean loadContent(@Nullable SocketFactory sockf, @Nullable SSLEngine ssl, @Nonnull String url, @Nonnull StringBuilder sb, long maxSize)
	{
		HTTPClient cli = HTTPClient.createConnect(sockf, ssl, url, RequestMethod.HTTP_GET, true);
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
