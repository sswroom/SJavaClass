package org.sswr.util.io;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public abstract class ObjectParser extends ParserBase {
	@Nullable
	public abstract ParsedObject parseObject(@Nonnull ParsedObject pobj, @Nullable PackageFile pkgFile, @Nonnull ParserType targetType);
}
