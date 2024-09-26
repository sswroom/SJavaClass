package org.sswr.util.io;

import jakarta.annotation.Nonnull;

public abstract class ParsedObject
{
	protected String sourceName;

	protected ParsedObject(@Nonnull String sourceName)
	{
		this.sourceName = sourceName;
	}

	public void dispose()
	{
	}

	@Nonnull
	public abstract ParserType getParserType();

	@Nonnull
	public String getSourceNameObj()
	{
		return this.sourceName;
	}

	public void setSourceName(@Nonnull String sourceName)
	{
		this.sourceName = sourceName;
	}
}
