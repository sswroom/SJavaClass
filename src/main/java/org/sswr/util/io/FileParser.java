package org.sswr.util.io;

import org.sswr.util.io.stmdata.FileData;

public abstract class FileParser extends ParserBase
{
	public abstract ParsedObject parseFileHdr(StreamData fd, PackageFile pkgFile, ParserType targetType, byte[] hdr, int hdrOfst, int hdrSize);

	public ParsedObject parseFile(StreamData fd, PackageFile pkgFile, ParserType targetType)
	{
		byte[] hdr = new byte[hdrSize];
		fd.getRealData(0, hdrSize, hdr, 0);
		return parseFileHdr(fd, pkgFile, targetType, hdr, 0, hdrSize);
	}

	public ParsedObject parseFilePath(String filePath)
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
