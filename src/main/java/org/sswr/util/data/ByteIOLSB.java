package org.sswr.util.data;

import jakarta.annotation.Nonnull;

public class ByteIOLSB implements ByteIO
{

	@Override
	public int readInt32(@Nonnull byte[] buff, int index) {
		return ByteTool.readInt32(buff, index);
	}

	@Override
	public int readInt16(@Nonnull byte[] buff, int index) {
		return ByteTool.readInt16(buff, index);
	}

	@Override
	public float readFloat(@Nonnull byte[] buff, int index) {
		return ByteTool.readSingle(buff, index);
	}

	@Override
	public void writeInt32(@Nonnull byte[] buff, int index, int val) {
		ByteTool.writeInt32(buff, index, val);
	}

	@Override
	public void writeInt16(@Nonnull byte[] buff, int index, int val) {
		ByteTool.writeInt16(buff, index, val);
	}
}
