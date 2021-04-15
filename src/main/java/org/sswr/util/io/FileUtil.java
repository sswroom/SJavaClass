package org.sswr.util.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileUtil {
	public static String getRealPath(String path, boolean appendSeperator)
	{
		if (path.startsWith("~/"))
		{
			path = System.getProperty("user.home") + path.substring(1);
		}
		if (appendSeperator && !path.endsWith(File.separator))
		{
			path = path + File.separator;
		}
		return path;
	}

	public static String getRealPath(String path)
	{
		return getRealPath(path, true);
	}

	public static boolean copyFile(File srcFile, File destFile)
	{
		long fileLen = srcFile.length();
		long copyLen = 0;
		try
		{
			int readCnt;
			byte fileBuff[] = new byte[65536];
			FileInputStream fis;
			FileOutputStream fos;
			fis = new FileInputStream(srcFile);
			fos = new FileOutputStream(destFile);
			while (true)
			{
				readCnt = fis.read(fileBuff, 0, fileBuff.length);
				if (readCnt <= 0)
				{
					break;
				}
				fos.write(fileBuff, 0, readCnt);
				copyLen += readCnt;
			}
			fos.close();
			fis.close();
			if (copyLen == fileLen)
			{
				destFile.setLastModified(srcFile.lastModified());
				return true;
			}
			else
			{
				destFile.delete();
				return false;
			}
		}
		catch (FileNotFoundException ex)
		{
			return false;
		}
		catch (IOException ex)
		{
			return false;
		}
	}

	public static boolean moveFile(File srcFile, String destPath)
	{
		File destFile = new File(destPath);
		if (srcFile.renameTo(destFile))
		{
			return true;
		}
		if (copyFile(srcFile, destFile))
		{
			srcFile.delete();
			return true;
		}
		return false;
	}
}
