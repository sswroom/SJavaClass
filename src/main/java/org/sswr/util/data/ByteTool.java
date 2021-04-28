package org.sswr.util.data;

public class ByteTool {
	public static int readInt(byte buff[], int index)
	{
		return (buff[index] & 0xff) | 
			((buff[index + 1] & 0xff) << 8) |
			((buff[index + 2] & 0xff) << 16) |
			((buff[index + 3] & 0xff) << 24);
	}

	public static long readLong(byte buff[], int index)
	{
		return ((long) buff[index + 7] << 56)
       | ((long) buff[index + 6] & 0xff) << 48
       | ((long) buff[index + 5] & 0xff) << 40
       | ((long) buff[index + 4] & 0xff) << 32
       | ((long) buff[index + 3] & 0xff) << 24
       | ((long) buff[index + 2] & 0xff) << 16
       | ((long) buff[index + 1] & 0xff) << 8
       | ((long) buff[index + 0] & 0xff);

	}

	public static double readDouble(byte buff[], int index)
	{
		return Double.longBitsToDouble(readLong(buff, index));
	}
}
