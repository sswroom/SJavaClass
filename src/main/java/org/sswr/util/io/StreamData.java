package org.sswr.util.io;

public interface StreamData
{
	public void close();

	public int getRealData(long dataOffset, int length, byte[] buffer, int buffOfst);
	public String getFullName();
	public String getShortName();
	public void setFullName(String fullName);
	public long getDataSize();

	public StreamData getPartialData(long offset, long length);
	public boolean isFullFile();
	public String getFullFileName();
	public boolean isLoading();
	public int getSeekCount();
}
