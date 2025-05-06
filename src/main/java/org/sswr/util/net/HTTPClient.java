package org.sswr.util.net;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.sswr.util.basic.HiResClock;
import org.sswr.util.crypto.cert.MyX509Cert;
import org.sswr.util.data.DateTimeUtil;
import org.sswr.util.data.SharedDouble;
import org.sswr.util.data.StringUtil;
import org.sswr.util.data.textenc.FormEncoding;
import org.sswr.util.data.textenc.URIEncoding;
import org.sswr.util.io.IOStream;
import org.sswr.util.io.MemoryStream;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public abstract class HTTPClient extends IOStream
{
	@Nonnull protected TCPClientFactory clif;
	@Nonnull protected HiResClock clk;

	protected InetAddress svrAddr;
	protected boolean canWrite;
	protected StringBuilder sbForm;

	protected long contLeng;
	protected int respCode;
	protected int hdrLen;

	protected String boundary;
	protected MemoryStream mstm;

	protected boolean kaConn;
	protected String url;
	protected @Nullable String forceHost;
	protected long totalUpload;
	protected long totalDownload;

	protected HTTPClient(@Nonnull TCPClientFactory clif, boolean kaConn)
	{
		super("HTTPClient");
		this.clk = new HiResClock();
		this.clif = clif;
		this.canWrite = false;
		this.contLeng = 0;
		this.respCode = 0;
		this.url = null;
		this.sbForm = null;
		this.hdrLen = 0;
		this.boundary = null;
		this.mstm = null;
		this.totalUpload = 0;
		this.totalDownload = 0;
		this.forceHost = null;
		this.kaConn = kaConn;
		this.svrAddr = null;
	}

	public abstract boolean isError();

	public boolean isDown()
	{
		return this.isError();
	}

	public abstract boolean connect(@Nonnull String url, @Nonnull RequestMethod method, @Nullable SharedDouble timeDNS, @Nullable SharedDouble timeConn, boolean defHeaders);

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

	public void forceHostName(@Nonnull String hostName)
	{
		if (hostName.length() > 0)
			this.forceHost = hostName;
		else
			this.forceHost = null;
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

	public double getTotalTime()
	{
		return this.clk.getTimeDiff();
	}

	public int getHdrLen()
	{
		return this.hdrLen;
	}

	public long getTotalUpload()
	{
		return this.totalUpload;
	}

	public long getTotalDownload()
	{
		return this.totalDownload;
	}

	@Nullable
	public InetAddress getSvrAddr()
	{
		return this.svrAddr;
	}

	public static ZonedDateTime parseDateStr(@Nonnull String dateStr)
	{
		String tmps;
		String[] ptrs;
		String[] ptrs2;
		String[] ptrs3;
		int i;
		if ((i = dateStr.indexOf(", ")) != -1)
		{
			tmps = dateStr.substring(i + 2);
			if (tmps.indexOf('-') == -1)
			{
				ptrs = StringUtil.split(tmps, " ");
				if (ptrs.length >= 4)
				{
					ptrs2 = StringUtil.split(ptrs[3], ":");
					if (ptrs2.length == 3)
					{
						return ZonedDateTime.of(StringUtil.toIntegerS(ptrs[2], 0),
							DateTimeUtil.parseMonthStr(ptrs[1]),
							StringUtil.toIntegerS(ptrs[0], 0),
							StringUtil.toIntegerS(ptrs2[0], 0),
							StringUtil.toIntegerS(ptrs2[1], 0),
							StringUtil.toIntegerS(ptrs2[2], 0),
							0,
							ZoneOffset.ofHours(0));
					}
				}
			}
			else
			{
				ptrs = StringUtil.split(tmps, " ");
				if (ptrs.length >= 2)
				{
					ptrs2 = StringUtil.split(ptrs[1], ":");
					ptrs3 = StringUtil.split(ptrs[0], "-");
					if (ptrs2.length >= 3 && ptrs3.length >= 3)
					{
						return ZonedDateTime.of(StringUtil.toIntegerS(ptrs3[2], 0) + ((2000 / 100) * 100),
							DateTimeUtil.parseMonthStr(ptrs3[1]),
							StringUtil.toIntegerS(ptrs3[0], 0),
							StringUtil.toIntegerS(ptrs2[0], 0),
							StringUtil.toIntegerS(ptrs2[1], 0),
							StringUtil.toIntegerS(ptrs2[2], 0),
							0,
							ZoneOffset.ofHours(0));
					}
					else
					{
						return null;
					}
				}
			}
		}
		else
		{
			ptrs = StringUtil.split(dateStr, " ");
			if (ptrs.length > 3)
			{
				ptrs2 = StringUtil.split(ptrs[ptrs.length - 2], ":");
				if (ptrs2.length == 3)
				{
					return ZonedDateTime.of(StringUtil.toIntegerS(ptrs[ptrs.length - 1], 0),
						DateTimeUtil.parseMonthStr(ptrs[1]),
						StringUtil.toIntegerS(ptrs[ptrs.length - 3], 0),
						StringUtil.toIntegerS(ptrs2[0], 0),
						StringUtil.toIntegerS(ptrs2[1], 0),
						StringUtil.toIntegerS(ptrs2[2], 0),
						0,
						ZoneOffset.ofHours(0));
				}
			}
		}
		return null;
	}

	@Nonnull
	public static HTTPClient createClient(@Nonnull TCPClientFactory clif, @Nullable SSLEngine ssl, @Nullable String userAgent, boolean kaConn, boolean isSecure)
	{
		if (isSecure && ssl == null)
			return new HTTPOSClient(clif, userAgent, kaConn);
		else
			return new HTTPMyClient(clif, ssl, userAgent, kaConn);
	}

	@Nonnull
	public static HTTPClient createConnect(@Nullable SocketFactory sockf, @Nullable SSLEngine ssl, @Nonnull String url, @Nonnull RequestMethod method, boolean kaConn)
	{
		HTTPClient cli = createClient(new TCPClientFactory(sockf), ssl, null, kaConn, url.toUpperCase().startsWith("HTTPS://"));
		cli.connect(url, method, null, null, true);
		return cli;
	}

	@Nonnull
	public static HTTPClient createConnect(@Nonnull TCPClientFactory clif, @Nullable SSLEngine ssl, @Nonnull String url, @Nonnull RequestMethod method, boolean kaConn)
	{
		HTTPClient cli = createClient(clif, ssl, null, kaConn, url.toUpperCase().startsWith("HTTPS://"));
		cli.connect(url, method, null, null, true);
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
