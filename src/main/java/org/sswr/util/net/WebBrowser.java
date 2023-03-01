package org.sswr.util.net;

import java.nio.charset.StandardCharsets;

import org.sswr.util.crypto.CRC32R;
import org.sswr.util.data.SharedObject;
import org.sswr.util.data.StringUtil;
import org.sswr.util.data.URLString;
import org.sswr.util.data.textbinenc.Base64Enc;
import org.sswr.util.data.textenc.URIEncoding;
import org.sswr.util.io.Path;
import org.sswr.util.io.PathType;
import org.sswr.util.io.StreamData;
import org.sswr.util.io.stmdata.FileData;
import org.sswr.util.io.stmdata.MemoryDataRef;

public class WebBrowser
{
	private SocketFactory sockf;
	private SSLEngine ssl;
	private String cacheDir;
	private CRC32R hash;
	private HTTPQueue queue;
	
	private String getLocalFileName(String url)
	{
		String scheme;
		StringBuilder sb = new StringBuilder();
		sb.append(this.cacheDir);
		if (sb.length() > 0 && sb.charAt(sb.length() - 1) != Path.PATH_SEPERATOR)
		{
			sb.append(Path.PATH_SEPERATOR);
		}
		scheme = URLString.getURIScheme(url);
		if (scheme.equals("HTTP") || scheme.equals("HTTPS"))
		{
			sb.append(scheme);
			sb.append(Path.PATH_SEPERATOR);
			sb.append(URLString.getURLHost(url).replace(':', '+'));
			Path.createDirectory(sb.toString());
			sb.append(Path.PATH_SEPERATOR);
			url = url.substring(url.indexOf(':') + 3);
			url = url.substring(url.indexOf('/'));
			byte[] urlBytes = url.getBytes(StandardCharsets.UTF_8);
			this.hash.clear();
			this.hash.calc(urlBytes, 0, urlBytes.length);
			sb.append(StringUtil.toHex(this.hash.getValue()));
			return sb.toString();
		}
		else
		{
			return null;
		}
	}

	public WebBrowser(SocketFactory sockf, SSLEngine ssl, String cacheDir)
	{
		this.sockf = sockf;
		this.ssl = ssl;
		this.cacheDir = cacheDir;
		this.hash = new CRC32R();
		this.queue = new HTTPQueue(sockf, ssl);
	}

	public void close()
	{
		this.queue.clear();
	}

	public StreamData getData(String url, boolean forceReload, SharedObject<String> contentType)
	{
		String scheme;
		PathType pt = Path.getPathType(url);
		/////////////////////////////////////////////
		if (pt == PathType.File)
		{
			FileData fd = new FileData(url, false);
			if (contentType != null)
			{
				contentType.value = MIME.getMIMEFromFileName(url);
			}
			return fd;
		}
		if ((scheme = URLString.getURIScheme(url)) == null)
			return null;
		if (scheme.equals("FILE"))
		{
			String filePath = URLString.getURLFilePath(url);
			FileData fd = new FileData(filePath, false);
			if (contentType != null)
			{
				contentType.value = MIME.getMIMEFromFileName(url);
			}
			return fd;
		}
		else if (scheme.equals("HTTP"))
		{
			HTTPData data;
			String fileName = getLocalFileName(url);
			data = new HTTPData(this.sockf, this.ssl, this.queue, url, fileName, forceReload);
			return data;
		}
		else if (scheme.equals("HTTPS"))
		{
			HTTPData data;
			String fileName = getLocalFileName(url);
			data = new HTTPData(this.sockf, this.ssl, this.queue, url, fileName, forceReload);
			return data;
		}
		else if (scheme.equals("FTP"))
		{
	/*		IO::Stream *stm = Net::URL::OpenStream(url, this.sockf);
			IO::StmData::StreamDataStream *data;
			if (stm == 0)
				return 0;
			NEW_CLASS(data, IO::StmData::*/
			return null;
		}
		else if (scheme.equals("DATA"))
		{
			MemoryDataRef fd;
			int urlInd = 5;
			char c;
			int urlEnd = url.length();
			if (contentType != null)
			{
				StringBuilder sbContType = new StringBuilder();
				while (urlInd < urlEnd)
				{
					c = url.charAt(urlInd++);
					if (c == ';')
					{
						break;
					}
					else
					{
						sbContType.append(c);
					}
				}
				contentType.value = sbContType.toString();
			}
			else
			{
				while (urlInd < urlEnd)
				{
					c = url.charAt(urlInd++);
					if (c == ';')
					{
						break;
					}
				}
			}
			if (url.substring(urlInd).startsWith("base64,"))
			{
				Base64Enc b64 = new Base64Enc();
				String decURL = URIEncoding.uriDecode(url.substring(urlInd + 7));
				byte[] ret = b64.decodeBin(decURL);
				fd = new MemoryDataRef(ret, 0, ret.length);
				return fd;
			}
			else
			{
				return null;
			}
		}
		else
		{
			return null;
		}
	}
}
