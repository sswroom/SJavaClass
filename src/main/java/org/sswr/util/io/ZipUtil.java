package org.sswr.util.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZipUtil
{
	private static boolean extractEntry(ZipFile zip, ZipEntry entry, File destDir)
	{
		String destPath = destDir.getPath() + File.separator + entry.getName();
		if (entry.isDirectory())
		{
			new File(destPath).mkdirs();
		}
		else
		{
			byte[] buff;
			int readSize;
			try
			{
				File f = new File(destPath);
				FileOutputStream fos = new FileOutputStream(f);
				InputStream stm = zip.getInputStream(entry);
				buff = new byte[8192];
				while (true)
				{
					readSize = stm.read(buff, 0, 8192);
					if (readSize <= 0)
					{
						break;
					}
					fos.write(buff, 0, readSize);
				}
				fos.close();
				stm.close();
				f.setLastModified(entry.getTime());
			}
			catch (IOException ex)
			{
				ex.printStackTrace();
			}
		}

		return true;
	}

	public static boolean extract(ZipFile zip, File destDir)
	{
		if (!destDir.isDirectory())
		{
			return false;
		}
		Enumeration<? extends ZipEntry> entries = zip.entries();
		while (entries.hasMoreElements())
		{
			if (!extractEntry(zip, entries.nextElement(), destDir))
			{
				return false;
			}
		}
		return true;
	}
}
