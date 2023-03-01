package org.sswr.util.io;

public interface ProgressHandler {
	public void progressStart(String name, long count);
	public void progressUpdate(long currCount, long newCount);
	public void progressEnd();
}
