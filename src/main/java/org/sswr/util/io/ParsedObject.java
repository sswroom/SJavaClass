package org.sswr.util.io;

public class ParsedObject
{
	protected String sourceName;

	protected ParsedObject(String sourceName)
	{
		this.sourceName = sourceName;
	}

	public String getSourceNameObj()
	{
		return this.sourceName;
	}

	public void setSourceName(String sourceName)
	{
		this.sourceName = sourceName;
	}
}
