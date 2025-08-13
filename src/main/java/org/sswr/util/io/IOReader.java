package org.sswr.util.io;

import org.sswr.util.data.StringBuilderUTF8;

import jakarta.annotation.Nonnull;

public interface IOReader {
	public void close();
	public int readLine(@Nonnull byte[] buff, int ofst, int maxCharCnt); // <0 error
	public boolean readLine(@Nonnull StringBuilderUTF8 sb, int maxCharCnt);
	public int getLastLineBreak(@Nonnull char []buff, int ofst);
	public int getLastLineBreak(@Nonnull byte []buff, int ofst);
	public boolean getLastLineBreak(@Nonnull StringBuilderUTF8 sb);
	public boolean isLineBreak();
	public boolean readToEnd(@Nonnull StringBuilderUTF8 sb);
}
