package org.sswr.util.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.sswr.util.data.DataTools;
import org.sswr.util.data.JSONParser;
import org.sswr.util.data.StringUtil;
import org.sswr.util.io.FileUtil;

public class HttpUtil
{
	public static final String PART_SEPERATOR = "\t";
	private static final int FILE_BUFFER_SIZE = 65536;

	public static String getSiteRoot(ServletRequest req)
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


	public static String headerEscape(ServletRequest req, String fileName)
	{
		//Firefox:
		{
			StringBuilder sb = new StringBuilder();
			sb.append('\"');
			byte[] bytes = fileName.getBytes(StandardCharsets.UTF_8);
			byte b;
			int i = 0;
			int j = bytes.length;
			while (i < j)
			{
				b = bytes[i];
				if (b < 32 || b >= 128 || b == '%')
				{
					sb.append('%');
					sb.append(StringUtil.toHex(b));
				}
				else
				{
					sb.append((char)b);
				}
				i++;
			}
			sb.append('\"');
			return sb.toString();
		}
	}

	public static void addContentDisposition(ServletRequest req, HttpServletResponse resp, boolean attachment, String fileName)
	{
		if (attachment)
		{
			if (fileName != null)
			{
				resp.addHeader("Content-Disposition", "attachment; filename="+headerEscape(req, fileName));
			}
			else
			{
				resp.addHeader("Content-Disposition", "attachment");
			}
		}
		else
		{
			resp.addHeader("Content-Disposition", "inline; filename="+headerEscape(req, fileName));
		}
	}

	public static boolean responseFile(File file, boolean attachment, String fileName, HttpServletRequest req, HttpServletResponse resp) throws IOException
	{
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
		resp.setContentType(URLConnection.guessContentTypeFromName(file.getName()));
		if (fileName != null || attachment)
		{
			addContentDisposition(req, resp, attachment, fileName);
		}
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

	public static boolean responseFileContent(String filePath, HttpServletRequest req, HttpServletResponse resp) throws IOException
	{
		return responseFile(new File(FileUtil.getRealPath(filePath)), false, null, req, resp);
	}

	public static boolean responseFileContent(File file, String fileName, HttpServletRequest req, HttpServletResponse resp) throws IOException
	{
		return responseFile(file, false, fileName, req, resp);
	}

	public static boolean responseFileDownload(String filePath, String fileName, HttpServletRequest req, HttpServletResponse resp) throws IOException
	{
		return responseFile(new File(FileUtil.getRealPath(filePath)), true, fileName, req, resp);
	}

	public static boolean responseFileDownload(File file, String fileName, HttpServletRequest req, HttpServletResponse resp) throws IOException
	{
		return responseFile(file, true, fileName, req, resp);
	}
	
	private static Map<String, Object> fromParamMap(Map<String, String[]> paramMap)
	{
		Iterator<String> itKeys = paramMap.keySet().iterator();
		String key;
		String vals[];
		Map<String, Object> retMap = new HashMap<String, Object>();
		while (itKeys.hasNext())
		{
			key = itKeys.next();
			vals = paramMap.get(key);
			if (vals.length == 1)
			{
				retMap.put(key, vals[0]);
			}
			else
			{
				retMap.put(key, DataTools.createList(vals));
			}
		}
		return retMap;

	}

	public static Map<String, Object> parseParams(HttpServletRequest req, List<Part> fileList)
	{
		if (req.getMethod().equals("GET"))
		{
			return fromParamMap(req.getParameterMap());
		}
		String contentType = req.getContentType();
		if (contentType != null)
		{
			if (contentType.startsWith("multipart/form-data") || contentType.startsWith("multipart/mixed"))
			{
				try
				{
					Collection<Part> parts = req.getParts();
					Object partArr[] = parts.toArray();
					Part part;
					int i = 0;
					int j = partArr.length;
					Map<String, Object> retMap = new HashMap<String, Object>();
					while (i < j)
					{
						part = (Part)partArr[i];
						if (part.getSubmittedFileName() != null)
						{
							if (fileList != null)
							{
								fileList.add(part);
							}
						}
						else
						{
							String name = part.getName();
							String value = new String(part.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
							Object oldVal = retMap.get(name);
							if (oldVal == null)
							{
								retMap.put(name, value);
							}
							else if (oldVal instanceof List)
							{
								@SuppressWarnings("unchecked")
								List<Object> sarr = (List<Object>)oldVal;
								sarr.add(value);
							}
							else
							{
								List<Object> sarr = new ArrayList<Object>();
								sarr.add(oldVal);
								sarr.add(value);
								retMap.put(name, sarr);
							}
						}
						i++;
					}
					return retMap;
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
					return new HashMap<String, Object>();
				}
			}
			else if (contentType.startsWith("application/x-www-form-urlencoded"))
			{
				return fromParamMap(req.getParameterMap());
			}
			else if (contentType.startsWith("application/json"))
			{
				try
				{
					byte[] buff = req.getInputStream().readAllBytes();
					Object json = JSONParser.parse(new String(buff, StandardCharsets.UTF_8));
					if (json == null || !(json instanceof Map))
					{
						return new HashMap<String, Object>();
					}
					@SuppressWarnings("unchecked")
					HashMap<String, Object> retMap = (HashMap<String, Object>)json;
					return retMap;
				}
				catch (IOException ex)
				{
					ex.printStackTrace();
					return new HashMap<String, Object>();
				}
				catch (IllegalArgumentException ex)
				{
					ex.printStackTrace();
					return new HashMap<String, Object>();
				}
			}
		}

		return new HashMap<String, Object>();
	}
}
