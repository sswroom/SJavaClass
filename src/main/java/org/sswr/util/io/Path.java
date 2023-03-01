package org.sswr.util.io;

import java.io.File;

public class Path
{
	public static final char PATH_SEPERATOR = File.separatorChar;

	public static boolean createDirectory(String path)
	{
		return new File(path).mkdirs();
	}

	public static boolean deleteFile(String fileName)
	{
		return new File(fileName).delete();
	}

	public static PathType getPathType(String path)
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
}
