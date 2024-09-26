package org.sswr.util.io;

import jakarta.annotation.Nonnull;

public interface FileSelector {
	public void addFilter(@Nonnull String pattern, @Nonnull String name);
}
