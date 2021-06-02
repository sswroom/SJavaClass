package org.sswr.util.data;

public class ByteTool {
	public static int readInt32(byte buff[], int index)
	{
		return (buff[index] & 0xff) | 
			((buff[index + 1] & 0xff) << 8) |
			((buff[index + 2] & 0xff) << 16) |
			((buff[index + 3] & 0xff) << 24);
	}

	public static int readMInt32(byte buff[], int index)
	{
		return ((buff[index] & 0xff) << 24) | 
			((buff[index + 1] & 0xff) << 16) |
			((buff[index + 2] & 0xff) << 8) |
			(buff[index + 3] & 0xff);
	}

	public static int readMUInt16(byte buff[], int index)
	{
		return ((buff[index] & 0xff) << 8) | 
			(buff[index + 1] & 0xff);
	}

	public static int readMInt16(byte buff[], int index)
	{
		return (((int)buff[index]) << 8) | 
			(buff[index + 1] & 0xff);
	}

	public static long readInt64(byte buff[], int index)
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

	public static long readMInt64(byte buff[], int index)
	{
		return ((long) buff[index + 0] << 56)
       | ((long) buff[index + 1] & 0xff) << 48
       | ((long) buff[index + 2] & 0xff) << 40
       | ((long) buff[index + 3] & 0xff) << 32
       | ((long) buff[index + 4] & 0xff) << 24
       | ((long) buff[index + 5] & 0xff) << 16
       | ((long) buff[index + 6] & 0xff) << 8
       | ((long) buff[index + 7] & 0xff);
	}

	public static double readDouble(byte buff[], int index)
	{
		return Double.longBitsToDouble(readInt64(buff, index));
	}

	public static void writeMInt32(byte buff[], int index, int val)
	{
		buff[index + 0] = (byte)((val >> 24) & 0xff);
		buff[index + 1] = (byte)((val >> 16) & 0xff);
		buff[index + 2] = (byte)((val >> 8) & 0xff);
		buff[index + 3] = (byte)(val & 0xff);
	}

	public static void writeMInt64(byte buff[], int index, long val)
	{
		buff[index + 0] = (byte)((val >> 56) & 0xff);
		buff[index + 1] = (byte)((val >> 48) & 0xff);
		buff[index + 2] = (byte)((val >> 40) & 0xff);
		buff[index + 3] = (byte)((val >> 32) & 0xff);
		buff[index + 4] = (byte)((val >> 24) & 0xff);
		buff[index + 5] = (byte)((val >> 16) & 0xff);
		buff[index + 6] = (byte)((val >> 8) & 0xff);
		buff[index + 7] = (byte)(val & 0xff);
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

	public static int ror32(int val, int cnt)
	{
		while (cnt < 0)
		{
			cnt += 32;
		}
		while (cnt >= 32)
		{
			cnt -= 32;
		}
		if (cnt == 0)
		{
			return val;
		}
		return (val << (32 - cnt)) | (((val >> 1) & 0x7fffffff) >> (cnt - 1));
	}

	public static long ror64(long val, int cnt)
	{
		while (cnt < 0)
		{
			cnt += 64;
		}
		while (cnt >= 64)
		{
			cnt -= 64;
		}
		if (cnt == 0)
		{
			return val;
		}
		return (val << (64 - cnt)) | (((val >> 1) & 0x7fffffffffffffffL) >> (cnt - 1));
	}

	public static int shr32(int val, int cnt)
	{
		if (cnt < 0)
		{
			return val << (-cnt);
		}
		if (cnt >= 32)
		{
			return 0;
		}
		if (cnt == 0)
		{
			return val;
		}
		return (((val >> 1) & 0x7fffffff) >> (cnt - 1));
	}

	public static long shr64(long val, int cnt)
	{
		if (cnt < 0)
		{
			return val << (-cnt);
		}
		if (cnt >= 64)
		{
			return 0;
		}
		if (cnt == 0)
		{
			return val;
		}
		return (((val >> 1) & 0x7fffffffffffffffL) >> (cnt - 1));
	}

	public static int countBit32(int v)
	{
		int cnt = 0;
		int i = 1;
		while (i != 0)
		{
			if ((v & i) != 0)
			{
				cnt++;
			}
			i = i << 1;
		}
		return cnt;
	}

	public static int countBit64(long v)
	{
		int cnt = 0;
		long i = 1;
		while (i != 0)
		{
			if ((v & i) != 0)
			{
				cnt++;
			}
			i = i << 1;
		}
		return cnt;
	}

	public static void copyArray(byte destArr[], int destOfst, byte srcArr[], int srcOfst, int size)
	{
		int i = 0;
		while (i < size)
		{
			destArr[destOfst + i] = srcArr[srcOfst + i];
			i++;
		}
	}

	public static void clearArray(byte arr[], int ofst, int size)
	{
		int i = 0;
		while (i < size)
		{
			arr[ofst + i] = 0;
			i++;
		}
	}
}
