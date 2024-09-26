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
}
