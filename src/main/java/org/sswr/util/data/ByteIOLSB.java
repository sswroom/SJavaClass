package org.sswr.util.data;

public class ByteIOLSB implements ByteIO
{

	@Override
	public int readInt32(byte[] buff, int index) {
		return ByteTool.readInt32(buff, index);
	}

	@Override
	public int readInt16(byte[] buff, int index) {
		return ByteTool.readInt16(buff, index);
	}

	@Override
	public float readFloat(byte[] buff, int index) {
		return ByteTool.readSingle(buff, index);
	}

	@Override
	public void writeInt32(byte[] buff, int index, int val) {
		ByteTool.writeInt32(buff, index, val);
	}

	@Override
	public void writeInt16(byte[] buff, int index, int val) {
		ByteTool.writeInt16(buff, index, val);
	}
}
