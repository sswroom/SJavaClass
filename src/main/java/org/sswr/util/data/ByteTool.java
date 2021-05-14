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

	public static long combineToLong(int lo, int hi)
	{
		return (0xffffffffL & (long)lo) | (((long)hi) << 32);
	}

	public static int toSUInt8(double v)
	{
		if (v >= 255.0)
		{
			return 255;
		}
		else if (v < 0)
		{
			return 0;
		}
		else
		{
			return (int)v;
		}
	}

	public static int toSUInt16(double v)
	{
		if (v >= 65535.0)
		{
			return 65535;
		}
		else if (v < 0)
		{
			return 0;
		}
		else
		{
			return (int)v;
		}
	}
}
