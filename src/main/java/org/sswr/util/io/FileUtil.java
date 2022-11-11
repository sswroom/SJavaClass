package org.sswr.util.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

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

	public static boolean copyFile(File srcFile, File destFile, boolean overwrite)
	{
		long fileLen = srcFile.length();
		long copyLen = 0;
		if (!overwrite && destFile.exists())
		{
			return false;
		}
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
				Files.setLastModifiedTime(Paths.get(destFile.getPath()), Files.getLastModifiedTime(Paths.get(srcFile.getPath())));
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

	public static boolean moveFile(File srcFile, String destPath, boolean overwrite)
	{
		File destFile = new File(destPath);
		if (srcFile.renameTo(destFile))
		{
			return true;
		}
		if (copyFile(srcFile, destFile, overwrite))
		{
			srcFile.delete();
			return true;
		}
		return false;
	}

	public static boolean copyDir(File srcDir, String destDir, boolean overwrite)
	{
		if (srcDir.isFile())
		{
			return copyFile(srcDir, new File(destDir), overwrite);
		}
		else
		{
			File destFile = new File(destDir);
			destFile.mkdirs();
			if (!destDir.endsWith(File.separator))
			{
				destDir = destDir + File.separator;
			}
			File[] files = srcDir.listFiles();
			int i = 0;
			int j = files.length;
			while (i < j)
			{
				if (!files[i].getName().startsWith(".") && !copyDir(files[i], destDir+files[i].getName(), overwrite))
				{
					return false;
				}
				i++;
			}
			return true;
		}
	}

	public static boolean moveDir(File srcDir, String destDir, boolean overwrite)
	{
		File destFile = new File(destDir);
		if (srcDir.renameTo(destFile))
		{
			return true;
		}
		if (copyDir(srcDir, destDir, overwrite))
		{
			deleteFileOrDir(srcDir);
			return true;
		}
		return false;
	}

	private static boolean fileNameMatchInner(char nameArr[], int nameIndex, char pattArr[], int pattIndex)
	{
		char p;
		while (nameIndex < nameArr.length)
		{
			if (pattIndex >= pattArr.length)
			{
				return false;
			}
			p = pattArr[pattIndex];
			if (p == '?')
			{
				pattIndex++;
				nameIndex++;
			}
			else if (p == '*')
			{
				pattIndex++;
				if (pattIndex >= pattArr.length)
				{
					return true;
				}
				while (nameIndex < nameArr.length)
				{
					if (fileNameMatchInner(nameArr, nameIndex, pattArr, pattIndex))
					{
						return true;
					}
					nameIndex++;
				}
				return false;
			}
			else if (p == nameArr[nameIndex])
			{
				nameIndex++;
				pattIndex++;
			}
			else
			{
				return false;
			}
		}
		return pattIndex >= pattArr.length || pattArr[pattIndex] == '*';
	}

	public static boolean fileNameMatch(String fileName, String pattern)
	{
		if (fileName.equals(pattern))
		{
			return true;
		}
		char nameArr[] = fileName.toCharArray();
		char pattArr[] = pattern.toCharArray();
		return fileNameMatchInner(nameArr, 0, pattArr, 0);
	}

	private static void searchInner(List<File> list, File basePath, String pattern)
	{
		String nextPattern = null;
		int i = pattern.indexOf(File.separator);
		if (i >= 0)
		{
			nextPattern = pattern.substring(i + 1);
			pattern = pattern.substring(0, i);
			if (nextPattern.equals(""))
			{
				nextPattern = null;
			}
		}
		File[] files = basePath.listFiles();
		int j;
		i = 0;
		j = files.length;
		while (i < j)
		{
			if (files[i].isDirectory())
			{
				if (fileNameMatch(files[i].getName(), pattern))
				{
					if (nextPattern != null)
					{
						searchInner(list, files[i], nextPattern);
					}
					else
					{
						list.add(files[i]);
					}
				}
			}
			else if (files[i].isFile() && nextPattern == null)
			{
				if (fileNameMatch(files[i].getName(), pattern))
				{
					list.add(files[i]);
				}
			}
			i++;
		}
	}

	public static List<File> search(String pattern)
	{
		pattern = getRealPath(pattern, false);
		int j = pattern.indexOf("*");
		int k = pattern.indexOf("?");
		if (j < 0 && k < 0)
		{
			return List.of(new File(pattern));
		}
		else if (j < 0)
		{
			j = k;
		}
		else if (k < 0)
		{

		}
		else if (k < j)
		{
			j = k;
		}
		List<File> list = new ArrayList<File>();
		int i = pattern.substring(0, j).lastIndexOf(File.separator);
		if (i < 0)
		{
			searchInner(list, new File("."), pattern);
		}
		else
		{
			searchInner(list, new File(pattern.substring(0, i)), pattern.substring(i + 1));
		}
		return list;
	}

	public static boolean deleteFile(String file)
	{
		return deleteFile(new File(file));
	}

	public static boolean deleteFile(File file)
	{
		return file.delete();
	}

	public static boolean deleteFileOrDir(String path)
	{
		return deleteFileOrDir(new File(path));
	}

	public static boolean deleteFileOrDir(File file)
	{
		if (file.isDirectory())
		{
			File[] files = file.listFiles();
			int i = 0;
			int j = files.length;
			while (i < j)
			{
				if (!deleteFileOrDir(files[i]))
					return false;
				i++;
			}
			return file.delete();
		}
		else if (file.isFile())
		{
			return file.delete();
		}
		else
		{
			return false;
		}
	}
}
