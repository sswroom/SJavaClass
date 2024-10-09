package org.sswr.util.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import org.sswr.util.data.DataTools;
import org.sswr.util.data.JSONParser;
import org.sswr.util.data.StringUtil;
import org.sswr.util.io.FileUtil;
import org.sswr.util.io.StreamUtil;

public class HttpUtil
{
	public static final String PART_SEPERATOR = "\t";
	private static final int FILE_BUFFER_SIZE = 65536;

	@Nonnull
	public static String getScheme(@Nonnull ServletRequest req)
	{
		String scheme = req.getScheme();
		if (req instanceof HttpServletRequest)
		{
			String proto = ((HttpServletRequest)req).getHeader("X-Forwarded-Proto");
			if (proto != null)
			{
				scheme = proto;
			}
		}
		return scheme;
	}

	@Nonnull
	public static String getServerName(@Nonnull ServletRequest req)
	{
		String serverName = req.getServerName();
		if (req instanceof HttpServletRequest)
		{
			String forHost = ((HttpServletRequest)req).getHeader("X-Forwarded-Host");
			if (forHost != null)
			{
				serverName = forHost;
			}
		}
		return serverName;
	}

	public static int getServerPort(@Nonnull ServletRequest req)
	{
		int port = req.getServerPort();
		if (req instanceof HttpServletRequest)
		{
			Integer iPort = StringUtil.toInteger(((HttpServletRequest)req).getHeader("X-Forwarded-Port"));
			if (iPort != null)
			{
				port = iPort;
			}
		}
		return port;	
	}

	@Nonnull
	public static String getSiteRoot(@Nonnull ServletRequest req)
	{
		String scheme = getScheme(req);
		int serverPort = getServerPort(req);
		String url = scheme+"://"+getServerName(req);
		if (scheme.equals("https") && serverPort == 443)
		{

		}
		else if (scheme.equals("http") && serverPort == 80)
		{

		}
		else
		{
			url += ":"+serverPort;
		}
		return url;
	}


	@Nonnull
	public static String headerEscape(@Nonnull ServletRequest req, @Nonnull String fileName)
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

	public static void addContentDisposition(@Nonnull ServletRequest req, @Nonnull HttpServletResponse resp, boolean attachment, @Nullable String fileName)
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
		else if (fileName != null)
		{
			resp.addHeader("Content-Disposition", "inline; filename="+headerEscape(req, fileName));
		}
	}

	public static boolean responseFile(@Nonnull File file, boolean attachment, @Nonnull String fileName, @Nonnull HttpServletRequest req, @Nonnull HttpServletResponse resp) throws IOException
	{
		if (!file.exists())
		{
			return false;
		}
		FileInputStream fis = new FileInputStream(file);
		boolean ret = responseFileStream(fis, file.lastModified(), attachment, file.getAbsolutePath(), fileName, req, resp);
		fis.close();
		return ret;
	}

	public static boolean responseFileStream(@Nonnull InputStream stm, long lastModified, boolean attachment, @Nonnull String fileName, @Nonnull HttpServletRequest req, @Nonnull HttpServletResponse resp) throws IOException
	{
		return responseFileStream(stm, lastModified, attachment, fileName, fileName, req, resp);
	}

	public static boolean responseFileStream(@Nonnull InputStream stm, long lastModified, boolean attachment, @Nonnull String srcFileName, @Nonnull String respFileName, @Nonnull HttpServletRequest req, @Nonnull HttpServletResponse resp) throws IOException
	{
		long since = req.getDateHeader("If-Modified-Since");
		if (lastModified != 0 && since >= 0 && since + 999 >= lastModified)
		{
			resp.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
			resp.addDateHeader("Last-Modified", lastModified);
			return true;
		}
		long fileLen = StreamUtil.getLength(stm);
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
						return partialResponse(resp, stm, srcFileName, lastModified, respFileName, fileLen, startOfst, endOfst);
					}
					else if (startOfst < 0 && endOfst >= 0 && endOfst <= fileLen)
					{
						return partialResponse(resp, stm, srcFileName, lastModified, respFileName, fileLen, fileLen - endOfst, fileLen - 1);
					}
					else if (startOfst >= 0 && startOfst < fileLen && endOfst < 0)
					{
						return partialResponse(resp, stm, srcFileName, lastModified, respFileName, fileLen, startOfst, fileLen - 1);
					}
				}
				catch (Exception ex)
				{

				}
			}
		}

		resp.setStatus(HttpServletResponse.SC_OK);
		resp.setContentType(URLConnection.guessContentTypeFromName(srcFileName));
		if (respFileName != null || attachment)
		{
			addContentDisposition(req, resp, attachment, respFileName);
		}
		resp.addDateHeader("Last-Modified", lastModified);
		resp.addHeader("Accept-Ranges", "bytes");
		resp.setContentLengthLong(fileLen);
		if (fileLen <= FILE_BUFFER_SIZE)
		{
			byte fileBuff[] = stm.readAllBytes();
			resp.getOutputStream().write(fileBuff);
		}
		else
		{
			long lengLeft = fileLen;
			ServletOutputStream ostm = resp.getOutputStream();
			byte fileBuff[] = new byte[FILE_BUFFER_SIZE];
			int readCnt;
			while (lengLeft > FILE_BUFFER_SIZE)
			{
				readCnt = stm.read(fileBuff, 0, FILE_BUFFER_SIZE);
				if (readCnt <= 0)
				{
					break;
				}
				ostm.write(fileBuff, 0, readCnt);
				lengLeft -= readCnt;
			}
			if (lengLeft > 0)
			{
				readCnt = stm.read(fileBuff, 0, FILE_BUFFER_SIZE);
				if (readCnt > 0)
				{
					ostm.write(fileBuff, 0, readCnt);
					lengLeft -= readCnt;
				}
			}
			ostm.close();
		}

		return true;
	}

	private static boolean partialResponse(@Nonnull HttpServletResponse resp, @Nonnull InputStream stm, @Nonnull String srcFileName, long lastModified, @Nonnull String respFileName, long fileLeng, long startOfst, long endOfst) throws IOException
	{
		resp.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
		resp.setContentType(URLConnection.guessContentTypeFromName(srcFileName));
		resp.addDateHeader("Last-Modified", lastModified);
		resp.addHeader("Accept-Ranges", "bytes");
		resp.addHeader("Content-Range", "bytes "+startOfst+"-"+endOfst+"/"+fileLeng);
		long lengLeft = endOfst - startOfst + 1;
		resp.setContentLengthLong(lengLeft);
		if (lengLeft <= FILE_BUFFER_SIZE)
		{
			stm.skip(startOfst);
			byte fileBuff[] = stm.readNBytes((int)lengLeft);
			resp.getOutputStream().write(fileBuff);
		}
		else
		{
			ServletOutputStream ostm = resp.getOutputStream();
			byte fileBuff[] = new byte[FILE_BUFFER_SIZE];
			int readCnt;
			stm.skip(startOfst);
			while (lengLeft > FILE_BUFFER_SIZE)
			{
				readCnt = stm.read(fileBuff, 0, FILE_BUFFER_SIZE);
				if (readCnt <= 0)
				{
					break;
				}
				ostm.write(fileBuff, 0, readCnt);
				lengLeft -= readCnt;
			}
			if (lengLeft > 0)
			{
				readCnt = stm.read(fileBuff, 0, FILE_BUFFER_SIZE);
				if (readCnt > 0)
				{
					ostm.write(fileBuff, 0, readCnt);
					lengLeft -= readCnt;
				}
			}
			ostm.close();
		}
		return true;
	}

	public static boolean responseFileContent(@Nonnull String filePath, @Nonnull HttpServletRequest req, @Nonnull HttpServletResponse resp) throws IOException
	{
		File file = new File(FileUtil.getRealPath(filePath));
		return responseFile(file, false, file.getName(), req, resp);
	}

	public static boolean responseFileContent(@Nonnull File file, @Nonnull String fileName, @Nonnull HttpServletRequest req, @Nonnull HttpServletResponse resp) throws IOException
	{
		return responseFile(file, false, fileName, req, resp);
	}

	public static boolean responseFileDownload(@Nonnull String filePath, @Nonnull String fileName, @Nonnull HttpServletRequest req, @Nonnull HttpServletResponse resp) throws IOException
	{
		return responseFile(new File(FileUtil.getRealPath(filePath)), true, fileName, req, resp);
	}

	public static boolean responseFileDownload(@Nonnull File file, @Nonnull String fileName, @Nonnull HttpServletRequest req, @Nonnull HttpServletResponse resp) throws IOException
	{
		return responseFile(file, true, fileName, req, resp);
	}
	
	@Nonnull
	private static Map<String, Object> fromParamMap(@Nonnull Map<String, String[]> paramMap)
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

	@Nonnull
	public static Map<String, Object> parseParams(@Nonnull HttpServletRequest req, @Nullable List<Part> fileList)
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
