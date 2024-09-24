package org.sswr.util.data;

import jakarta.annotation.Nonnull;

public interface ByteIO
{
	public int readInt32(@Nonnull byte[] buff, int index);
	public int readInt16(@Nonnull byte[] buff, int index);
	public float readFloat(@Nonnull byte[] buff, int index);
	public void writeInt32(@Nonnull byte[] buff, int index, int val);
	public void writeInt16(@Nonnull byte[] buff, int index, int val);
}
