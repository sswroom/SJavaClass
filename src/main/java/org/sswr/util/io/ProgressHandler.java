package org.sswr.util.io;

import jakarta.annotation.Nonnull;

public interface ProgressHandler {
	public void progressStart(@Nonnull String name, long count);
	public void progressUpdate(long currCount, long newCount);
	public void progressEnd();
}
