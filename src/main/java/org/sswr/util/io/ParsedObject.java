package org.sswr.util.io;

public abstract class ParsedObject
{
	protected String sourceName;

	protected ParsedObject(String sourceName)
	{
		this.sourceName = sourceName;
	}

	public void dispose()
	{
	}

	public abstract ParserType getParserType();

	public String getSourceNameObj()
	{
		return this.sourceName;
	}

	public void setSourceName(String sourceName)
	{
		this.sourceName = sourceName;
	}
}
