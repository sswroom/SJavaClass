package org.sswr.util.io;

import java.io.InputStream;
import java.util.Iterator;

public interface StreamColl
{
	public void close();
	public Iterator<String> listFiles();
	public boolean hasFile(String fileName);
	public long getStmSize(String fileName);
	public InputStream openStream(String fileName);
}
