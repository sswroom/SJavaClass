package org.sswr.util.io;

import jakarta.annotation.Nonnull;

public interface IOWriter
{
	public boolean writeStr(@Nonnull String str);
	public boolean writeLine(@Nonnull String str);
	public boolean writeLine();
}
