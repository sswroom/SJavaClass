package org.sswr.util.io;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public interface StreamData
{
	public void close();

	public int getRealData(long dataOffset, int length, @Nonnull byte[] buffer, int buffOfst);
	@Nonnull
	public String getFullName();
	@Nullable
	public String getShortName();
	public void setFullName(@Nonnull String fullName);
	public long getDataSize();

	@Nonnull
	public StreamData getPartialData(long offset, long length);
	public boolean isFullFile();
	@Nonnull
	public String getFullFileName();
	public boolean isLoading();
	public int getSeekCount();
}
