package org.sswr.util.data;

import jakarta.annotation.Nonnull;

public interface ByteArray {
	@Nonnull
	public byte[] getBytes();
	public int getBytesOffset();
	public int getBytesLength();
}
