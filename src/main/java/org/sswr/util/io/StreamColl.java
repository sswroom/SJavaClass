package org.sswr.util.io;

import java.io.InputStream;
import java.util.Iterator;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public interface StreamColl
{
	public void close();
	@Nonnull
	public Iterator<String> listFiles();
	public boolean hasFile(@Nonnull String fileName);
	public long getStmSize(@Nonnull String fileName);
	@Nullable
	public InputStream openStream(@Nonnull String fileName);
}
