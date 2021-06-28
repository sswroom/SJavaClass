package org.sswr.util.data;

public class ByteIOMSB implements ByteIO
{
	
	@Override
	public int readInt32(byte[] buff, int index) {
		return ByteTool.readMInt32(buff, index);
	}

	@Override
	public int readInt16(byte[] buff, int index) {
		return ByteTool.readMInt16(buff, index);
	}

	@Override
	public float readFloat(byte[] buff, int index) {
		return ByteTool.readMSingle(buff, index);
	}

	@Override
	public void writeInt32(byte[] buff, int index, int val) {
		ByteTool.writeMInt32(buff, index, val);
	}

	@Override
	public void writeInt16(byte[] buff, int index, int val) {
		ByteTool.writeMInt16(buff, index, val);
	}
}
