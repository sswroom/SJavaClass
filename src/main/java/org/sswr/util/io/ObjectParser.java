package org.sswr.util.io;

public abstract class ObjectParser extends ParserBase {
	public abstract ParsedObject parseObject(ParsedObject pobj, PackageFile pkgFile, ParserType targetType);
}
