package org.sswr.util.map;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.sswr.util.data.StringUtil;
import org.sswr.util.io.FileStream;
import org.sswr.util.io.SeekableStream;
import org.sswr.util.io.FileStream.BufferType;
import org.sswr.util.io.FileStream.FileMode;
import org.sswr.util.io.FileStream.FileShare;
import org.sswr.util.net.HTTPClient;
import org.sswr.util.net.HTTPServerUtil;
import org.sswr.util.net.RequestMethod;
import org.sswr.util.net.SSLEngine;
import org.sswr.util.net.SocketFactory;
import org.sswr.util.net.StatusCode;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class OSMCacheHandler {
	private static final boolean VERBOSE = false;
	public static class CacheStatus
	{
		public int reqCnt;
		public int remoteSuccCnt;
		public int remoteErrCnt;
		public int localCnt;
		public int cacheCnt;
	}

	private List<String> urls;
	private int urlNext;
	
	private String cacheDir;
	private SocketFactory sockf;
	private SSLEngine ssl;
	private CacheStatus status;

	private SeekableStream getTileData(int lev, int xTile, int yTile)
	{
		String path = this.cacheDir;
		if (!path.endsWith(File.separator))
		{
			path = path + File.separator;
		}
		path = path + lev + File.separator + xTile;
		new File(path).mkdirs();
		path = path + File.separator + yTile + ".png";

		FileStream fs = new FileStream(path, FileMode.ReadOnly, FileShare.DenyNone, BufferType.Normal);
		if (!fs.isError())
		{
			if (VERBOSE)
			{
				System.out.println("OSMCacheHandler: load from file cache");
			}
			synchronized(this.status)
			{
				this.status.localCnt++;
			}
			ZonedDateTime dt = fs.getModifyTime();
			if (System.currentTimeMillis() - dt.toInstant().toEpochMilli() >= 3600000)
			{
				fs.setModifyTime(ZonedDateTime.now());
				synchronized(this.status)
				{
					this.status.cacheCnt++;
				}
			}

			return fs;
		}
		fs.close();
		fs = null;

		String thisUrl;
		synchronized(this.urls)
		{
			if ((thisUrl = this.urls.get(this.urlNext)) == null)
			{
				if (VERBOSE)
				{
					System.out.println("OSMCacheHandler: Next url is error, urlNext = "+this.urlNext);
				}
				return null;
			}
			this.urlNext = (this.urlNext + 1) % this.urls.size();
		}
		String osmURL = thisUrl + lev + "/" + xTile + "/" + yTile + ".png";
		if (VERBOSE)
		{
			System.out.println("OSMCacheHandler: load from url: "+osmURL);
		}
		HTTPClient cli = HTTPClient.createClient(this.sockf, this.ssl, "OSMTileMap/1.0 SSWR/1.0", true, osmURL.startsWith("https://"));
		cli.connect(osmURL, RequestMethod.HTTP_GET, true);
	
		if (cli.getRespStatus() == 304)
		{
			FileStream imgFS = new FileStream(path, FileMode.Append, FileShare.DenyNone, BufferType.Normal);
			imgFS.setModifyTime(ZonedDateTime.now());
			if (VERBOSE)
			{
				System.out.println("OSMCacheHandler: Server response 304: "+osmURL);
			}
			fs = imgFS;
		}
		else if (cli.getRespStatus() == 200)
		{
			long contLeng = cli.getContentLength();
			int currPos = 0;
			int readSize;
			if (contLeng > 0 && contLeng <= 10485760)
			{
				byte[] imgBuff = new byte[(int)contLeng];
				while ((readSize = cli.read(imgBuff, currPos, (int)(contLeng - currPos))) > 0)
				{
					currPos += readSize;
					if (currPos >= contLeng)
					{
						break;
					}
				}
				if (currPos >= contLeng)
				{
					fs = new FileStream(path, FileMode.Create, FileShare.DenyRead, BufferType.NoWriteBuffer);
					fs.write(imgBuff);
					ZonedDateTime dt;
					if ((dt = cli.getLastModified()) != null)
					{
						fs.setModifyTime(dt);
					}
					fs.seekFromBeginning(0);
					synchronized (this.status)
					{
						this.status.remoteSuccCnt++;
					}
				}
				else
				{
					if (VERBOSE)
					{
						System.out.println("OSMCacheHandler: Read incomplete: "+currPos+", "+contLeng);
					}
					synchronized (this.status)
					{
						this.status.remoteErrCnt++;
					}
				}
			}
			else
			{
				if (VERBOSE)
				{
					System.out.println("OSMCacheHandler: Content length out of range: "+contLeng+", url = "+osmURL);
				}
				synchronized (this.status)
				{
					this.status.remoteErrCnt++;
				}
			}
		}
		else
		{
			if (VERBOSE)
			{
				System.out.println("OSMCacheHandler: Server response "+cli.getRespStatus()+": "+osmURL);
			}
		}
		cli.close();
		return fs;
	}

	public OSMCacheHandler(String url, String cacheDir, int maxLevel, SocketFactory sockf, SSLEngine ssl)
	{
		this.urls = new ArrayList<String>();
		if (url != null && url.length() > 0)
		{
			this.urls.add(url);
		}
		this.urlNext = 0;
		this.cacheDir = cacheDir;
		this.sockf = sockf;
		this.ssl = ssl;
		this.status = new CacheStatus();
		this.status.reqCnt = 0;
		this.status.remoteSuccCnt = 0;
		this.status.remoteErrCnt = 0;
		this.status.localCnt = 0;
		this.status.cacheCnt = 0;
	}

	public void addAlternateURL(String url)
	{
		this.urls.add(url);
	}
	
	public void getStatus(CacheStatus status)
	{
		status.reqCnt = this.status.reqCnt;
		status.remoteSuccCnt = this.status.remoteSuccCnt;
		status.remoteErrCnt = this.status.remoteErrCnt;
		status.localCnt = this.status.localCnt;
		status.cacheCnt = this.status.cacheCnt;
	}

	public boolean processRequest(HttpServletRequest req, HttpServletResponse resp, String subReq) throws IOException
	{
		if (VERBOSE)
		{
			System.out.println("OSMCacheHandler: subReq = "+subReq);
		}
		String[] sarr;
		sarr = StringUtil.split(subReq, "/");

		if (sarr.length != 4)
		{
			if (VERBOSE)
			{
				System.out.println("OSMCacheHandler: Split not = 4 ("+sarr.length+")");
			}
			resp.sendError(StatusCode.NOT_FOUND);
			return true;
		}

		synchronized(this.status)
		{
			this.status.reqCnt++;
		}

		int lev = StringUtil.toIntegerS(sarr[1], 0);
		int xTile = StringUtil.toIntegerS(sarr[2], 0);
		int yTile;
		int i = sarr[3].indexOf(".png");
		sarr[3] = sarr[3].substring(0, i);
		yTile = StringUtil.toIntegerS(sarr[3], 0);

		synchronized (this)
		{
			SeekableStream stm;
			if ((stm = getTileData(lev, xTile, yTile)) == null)
			{
				if (VERBOSE)
				{
					System.out.println("OSMCacheHandler: Get Tile Data failed, lev = "+lev+", xTile = "+xTile+", yTile = "+yTile);
				}
				resp.sendError(StatusCode.NOT_FOUND);
				return false;
			}
			else
			{
				long stmLeng = stm.getLength();
				HTTPServerUtil.addDefHeaders(resp, req);
				HTTPServerUtil.addContentType(resp, "image/png");
				HTTPServerUtil.addContentLength(resp, stmLeng);
				resp.addHeader("Cache-Control", "private");
				resp.addHeader("Access-Control-Allow-Origin", "*");
				HTTPServerUtil.addExpireTime(resp, ZonedDateTime.now().plus(1440, ChronoUnit.MINUTES));
				byte[]buff = stm.readToEnd();
				stm.close();

				OutputStream os = resp.getOutputStream();
				os.write(buff);
				os.close();
			}
			return true;

		}
	}
}
