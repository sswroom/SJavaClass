package org.sswr.util.io;

public abstract class PackageFile extends ParsedObject
{
	protected PackageFile(String sourceName)
	{
		super(sourceName);
	}

	public ParserType getParserType()
	{
		return ParserType.PackageFile;
	}

	public abstract StreamData getItemStmData(String name);
}
