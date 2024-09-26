package org.sswr.util.map;

import org.sswr.util.io.ParsedObject;
import org.sswr.util.io.ParserType;

import jakarta.annotation.Nonnull;

public class MapDrawLayer extends ParsedObject
{
	protected MapDrawLayer(@Nonnull String sourceName) {
		super(sourceName);
	}

	@Override
	@Nonnull 
	public ParserType getParserType() {
		return ParserType.MapLayer;
	}
}
