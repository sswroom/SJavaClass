package org.sswr.util.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLConnection;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sswr.util.io.FileUtil;

public class HttpUtil {
	private static final int FILE_BUFFER_SIZE = 65536;

	public static String getSiteRoot(HttpServletRequest req)
	{
		String url = req.getScheme()+"://"+req.getServerName();
		if (req.getScheme().equals("https") && req.getServerPort() == 443)
		{

		}
		else if (req.getScheme().equals("http") && req.getServerPort() == 80)
		{

		}
		else
		{
			url += ":"+req.getServerPort();
		}
		return url;
	}


	public static boolean responseFileContent(String fileName, HttpServletRequest req, HttpServletResponse resp) throws IOException
	{
		File file = new File(FileUtil.getRealPath(fileName));
		if (!file.exists())
		{
			return false;
		}
		long since = req.getDateHeader("If-Modified-Since");
		long lastModified = file.lastModified();
		if (since >= 0 && since + 999 >= lastModified)
		{
			resp.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
			resp.addDateHeader("Last-Modified", lastModified);
			return true;
		}
		long fileLen = file.length();
		String range = req.getHeader("Range");
		if (range != null && range.startsWith("bytes="))
		{
			int len = range.length();
			int sp = range.indexOf("-");
			if (sp > 0)
			{
				try
				{
					long startOfst = -1;
					long endOfst = -1;
					if (sp > 6)
					{
						startOfst = Long.parseLong(range.substring(6, sp));
					}
					if (sp + 1 < len)
					{
						endOfst = Long.parseLong(range.substring(sp + 1));
					}
					if (startOfst >= 0 && endOfst >= startOfst && endOfst < fileLen)
					{
						return partialResponse(resp, file, startOfst, endOfst);
					}
					else if (startOfst < 0 && endOfst >= 0 && endOfst <= fileLen)
					{
						return partialResponse(resp, file, fileLen - endOfst, fileLen - 1);
					}
					else if (startOfst >= 0 && startOfst < fileLen && endOfst < 0)
					{
						return partialResponse(resp, file, startOfst, fileLen - 1);
					}
				}
				catch (Exception ex)
				{

				}
			}
		}

		resp.setStatus(HttpServletResponse.SC_OK);
		resp.setContentType(URLConnection.guessContentTypeFromName(fileName));
		resp.addDateHeader("Last-Modified", lastModified);
		resp.addHeader("Accept-Ranges", "bytes");
		resp.setContentLengthLong(fileLen);
		if (fileLen <= FILE_BUFFER_SIZE)
		{
			FileInputStream fis = new FileInputStream(file);
			byte fileBuff[] = fis.readAllBytes();
			fis.close();
			resp.getOutputStream().write(fileBuff);
		}
		else
		{
			long lengLeft = fileLen;
			FileInputStream fis = new FileInputStream(file);
			ServletOutputStream ostm = resp.getOutputStream();
			byte fileBuff[] = new byte[FILE_BUFFER_SIZE];
			int readCnt;
			while (lengLeft > FILE_BUFFER_SIZE)
			{
				readCnt = fis.read(fileBuff, 0, FILE_BUFFER_SIZE);
				if (readCnt <= 0)
				{
					break;
				}
				ostm.write(fileBuff, 0, readCnt);
				lengLeft -= readCnt;
			}
			if (lengLeft > 0)
			{
				readCnt = fis.read(fileBuff, 0, FILE_BUFFER_SIZE);
				if (readCnt > 0)
				{
					ostm.write(fileBuff, 0, readCnt);
					lengLeft -= readCnt;
				}
			}
			fis.close();
			ostm.close();
		}

		return true;
	}

	private static boolean partialResponse(HttpServletResponse resp, File file, long startOfst, long endOfst) throws IOException
	{
		resp.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
		resp.setContentType(URLConnection.guessContentTypeFromName(file.getName()));
		resp.addDateHeader("Last-Modified", file.lastModified());
		resp.addHeader("Accept-Ranges", "bytes");
		resp.addHeader("Content-Range", "bytes "+startOfst+"-"+endOfst+"/"+file.length());
		long lengLeft = endOfst - startOfst + 1;
		resp.setContentLengthLong(lengLeft);
		if (lengLeft <= FILE_BUFFER_SIZE)
		{
			FileInputStream fis = new FileInputStream(file);
			fis.skip(startOfst);
			byte fileBuff[] = fis.readNBytes((int)lengLeft);
			fis.close();
			resp.getOutputStream().write(fileBuff);
		}
		else
		{
			FileInputStream fis = new FileInputStream(file);
			ServletOutputStream ostm = resp.getOutputStream();
			byte fileBuff[] = new byte[FILE_BUFFER_SIZE];
			int readCnt;
			fis.skip(startOfst);
			while (lengLeft > FILE_BUFFER_SIZE)
			{
				readCnt = fis.read(fileBuff, 0, FILE_BUFFER_SIZE);
				if (readCnt <= 0)
				{
					break;
				}
				ostm.write(fileBuff, 0, readCnt);
				lengLeft -= readCnt;
			}
			if (lengLeft > 0)
			{
				readCnt = fis.read(fileBuff, 0, FILE_BUFFER_SIZE);
				if (readCnt > 0)
				{
					ostm.write(fileBuff, 0, readCnt);
					lengLeft -= readCnt;
				}
			}
			fis.close();
			ostm.close();
		}
		return true;
	}

	public static boolean responseFileDownload(String filePath, String fileName, HttpServletRequest req, HttpServletResponse resp) throws IOException
	{
		File file = new File(FileUtil.getRealPath(filePath));
		if (!file.exists())
		{
			return false;
		}
		long since = req.getDateHeader("If-Modified-Since");
		long lastModified = file.lastModified();
		if (since >= 0 && since + 999 >= lastModified)
		{
			resp.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
			resp.addDateHeader("Last-Modified", lastModified);
			return true;
		}
		long fileLen = file.length();
		resp.setStatus(HttpServletResponse.SC_OK);
		resp.setContentType(URLConnection.guessContentTypeFromName(filePath));
		resp.addDateHeader("Last-Modified", lastModified);
		resp.addHeader("Content-Disposition", "attachment; filename=\""+fileName+"\"");
		resp.setContentLengthLong(fileLen);
		if (fileLen <= FILE_BUFFER_SIZE)
		{
			FileInputStream fis = new FileInputStream(file);
			byte fileBuff[] = fis.readAllBytes();
			fis.close();
			resp.getOutputStream().write(fileBuff);
		}
		else
		{
			long lengLeft = fileLen;
			FileInputStream fis = new FileInputStream(file);
			ServletOutputStream ostm = resp.getOutputStream();
			byte fileBuff[] = new byte[FILE_BUFFER_SIZE];
			int readCnt;
			while (lengLeft > FILE_BUFFER_SIZE)
			{
				readCnt = fis.read(fileBuff, 0, FILE_BUFFER_SIZE);
				if (readCnt <= 0)
				{
					break;
				}
				ostm.write(fileBuff, 0, readCnt);
				lengLeft -= readCnt;
			}
			if (lengLeft > 0)
			{
				readCnt = fis.read(fileBuff, 0, FILE_BUFFER_SIZE);
				if (readCnt > 0)
				{
					ostm.write(fileBuff, 0, readCnt);
					lengLeft -= readCnt;
				}
			}
			fis.close();
			ostm.close();
		}
		return true;
	}
}
