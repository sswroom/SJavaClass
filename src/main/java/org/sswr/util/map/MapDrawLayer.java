package org.sswr.util.map;

import org.sswr.util.io.ParsedObject;
import org.sswr.util.io.ParserType;

public class MapDrawLayer extends ParsedObject
{
	protected MapDrawLayer(String sourceName) {
		super(sourceName);
	}

	@Override
	public ParserType getParserType() {
		return ParserType.MapLayer;
	}
}
