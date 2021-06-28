package org.sswr.util.data;

public interface ByteIO
{
	public int readInt32(byte[] buff, int index);
	public int readInt16(byte[] buff, int index);
	public float readFloat(byte[] buff, int index);
	public void writeInt32(byte[] buff, int index, int val);
	public void writeInt16(byte[] buff, int index, int val);
}
