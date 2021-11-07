package org.sswr.util.io;

public abstract class PackageFile extends ParsedObject
{
	protected PackageFile(String sourceName)
	{
		super(sourceName);
	}

	public abstract StreamData getItemStmData(String name);
}
