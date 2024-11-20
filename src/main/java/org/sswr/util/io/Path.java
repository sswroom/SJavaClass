package org.sswr.util.io;

import java.io.File;

import jakarta.annotation.Nonnull;

public class Path
{
	public static final char PATH_SEPERATOR = File.separatorChar;

	public static boolean createDirectory(@Nonnull String path)
	{
		return new File(path).mkdirs();
	}

	public static boolean deleteFile(@Nonnull String fileName)
	{
		return new File(fileName).delete();
	}

	@Nonnull
	public static PathType getPathType(@Nonnull String path)
	{
		File file = new File(path);
		if (!file.exists())
		{
			return PathType.Unknown;
		}
		else if (file.isDirectory())
		{
			return PathType.Directory;
		}
		else
		{
			return PathType.File;
		}
	}

	@Nonnull
	public static String getProcessFileName()
	{
		String path = System.getenv("PWD");
		if (path == null)
		{
			return "";
		}
		return path+"/.";
	}

	@Nonnull
	public static String appendPath(@Nonnull String path, @Nonnull String toAppend)
	{
		if (toAppend.startsWith(File.separator))
			return toAppend;
		int pathLen = path.length();
		int i = path.lastIndexOf(File.separator);
		PathType pt = getPathType(path);
		if (pt == PathType.File && i != -1)
		{
			path = path.substring(0, i);
			i = path.lastIndexOf(File.separator);
		}
		else if (i == pathLen - 1)
		{
			path = path.substring(0, i);
			i = path.lastIndexOf(File.separator);
		}
		while (true)
		{
			if (toAppend.startsWith(".."+File.separator))
			{
				if (i != -1)
				{
					path = path.substring(0, i);
					i = path.lastIndexOf(File.separator);
				}
				toAppend = toAppend.substring(3);
			}
			else if (toAppend.startsWith("."+File.separator))
			{
				toAppend = toAppend.substring(2);
			}
			else
			{
				break;
			}
		}
		return path + File.separator + toAppend;
	}
}
