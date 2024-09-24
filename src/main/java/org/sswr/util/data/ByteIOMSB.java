package org.sswr.util.data;

import jakarta.annotation.Nonnull;

public class ByteIOMSB implements ByteIO
{
	
	@Override
	public int readInt32(@Nonnull byte[] buff, int index) {
		return ByteTool.readMInt32(buff, index);
	}

	@Override
	public int readInt16(@Nonnull byte[] buff, int index) {
		return ByteTool.readMInt16(buff, index);
	}

	@Override
	public float readFloat(@Nonnull byte[] buff, int index) {
		return ByteTool.readMSingle(buff, index);
	}

	@Override
	public void writeInt32(@Nonnull byte[] buff, int index, int val) {
		ByteTool.writeMInt32(buff, index, val);
	}

	@Override
	public void writeInt16(@Nonnull byte[] buff, int index, int val) {
		ByteTool.writeMInt16(buff, index, val);
	}
}
