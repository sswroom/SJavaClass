package org.sswr.util.io;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public abstract class PackageFile extends ParsedObject
{
	protected PackageFile(@Nonnull String sourceName)
	{
		super(sourceName);
	}

	@Nonnull
	public ParserType getParserType()
	{
		return ParserType.PackageFile;
	}

	@Nullable
	public abstract StreamData getItemStmData(@Nonnull String name);
	public abstract @Nonnull PackageFile clone();
}
