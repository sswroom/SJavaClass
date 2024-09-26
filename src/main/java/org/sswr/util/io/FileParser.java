package org.sswr.util.io;

import org.sswr.util.io.stmdata.FileData;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public abstract class FileParser extends ParserBase
{
	@Nullable
	public abstract ParsedObject parseFileHdr(@Nonnull StreamData fd, @Nullable PackageFile pkgFile, @Nonnull ParserType targetType, @Nonnull byte[] hdr, int hdrOfst, int hdrSize);

	@Nullable
	public ParsedObject parseFile(@Nonnull StreamData fd, @Nullable PackageFile pkgFile, @Nonnull ParserType targetType)
	{
		byte[] hdr = new byte[hdrSize];
		fd.getRealData(0, hdrSize, hdr, 0);
		return parseFileHdr(fd, pkgFile, targetType, hdr, 0, hdrSize);
	}

	@Nullable
	public ParsedObject parseFilePath(@Nonnull String filePath)
	{
		PackageFile pkg = null;
		int i = filePath.lastIndexOf(Path.PATH_SEPERATOR);
		if (i != -1)
		{
			String dir = filePath.substring(0, i);
			pkg = new DirectoryPackage(dir);
		}
		ParsedObject pobj;
		FileData fd = new FileData(filePath, false);
		pobj = this.parseFile(fd, pkg, ParserType.Unknown);
		fd.close();
		if (pkg != null)
			pkg.dispose();
		return pobj;
	}

	public static final int hdrSize = 512;
}
