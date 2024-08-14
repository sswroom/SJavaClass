package org.sswr.util.parser;

public class FullParserList extends ParserList {
	public FullParserList()
	{
		this.addFileParser(new X509Parser());
		this.addFileParser(new ImageParser());
	}
}
