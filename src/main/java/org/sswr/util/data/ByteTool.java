package org.sswr.util.data;

import java.nio.charset.StandardCharsets;

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

	public static int readUInt16(byte buff[], int index)
	{
		return ((buff[index + 1] & 0xff) << 8) | 
			(buff[index] & 0xff);
	}

	public static int readInt16(byte buff[], int index)
	{
		return (((int)buff[index + 1]) << 8) | 
			(buff[index] & 0xff);
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

	public static int readMUInt24(byte buff[], int index)
	{
		return ((buff[index] & 0xff) << 16) | 
			((buff[index + 1] & 0xff) << 8) | 
			(buff[index + 2] & 0xff);
	}

	public static int readMInt24(byte buff[], int index)
	{
		return (((int)buff[index + 2]) << 16) | 
			((buff[index + 1] & 0xff) << 8) | 
			(buff[index + 0] & 0xff);
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

	public static float readSingle(byte buff[], int index)
	{
		return Float.intBitsToFloat(readInt32(buff, index));
	}

	public static float readMSingle(byte buff[], int index)
	{
		return Float.intBitsToFloat(readMInt32(buff, index));
	}

	public static double readDouble(byte buff[], int index)
	{
		return Double.longBitsToDouble(readInt64(buff, index));
	}

	public static double readMDouble(byte buff[], int index)
	{
		return Double.longBitsToDouble(readMInt64(buff, index));
	}

	public static void writeInt16(byte buff[], int index, int val)
	{
		buff[index + 0] = (byte)(val & 0xff);
		buff[index + 1] = (byte)((val >> 8) & 0xff);
	}

	public static void writeInt32(byte buff[], int index, int val)
	{
		buff[index + 0] = (byte)(val & 0xff);
		buff[index + 1] = (byte)((val >> 8) & 0xff);
		buff[index + 2] = (byte)((val >> 16) & 0xff);
		buff[index + 3] = (byte)((val >> 24) & 0xff);
	}

	public static void writeMInt16(byte buff[], int index, int val)
	{
		buff[index + 0] = (byte)((val >> 8) & 0xff);
		buff[index + 1] = (byte)(val & 0xff);
	}

	public static void writeMInt24(byte buff[], int index, int val)
	{
		buff[index + 0] = (byte)((val >> 16) & 0xff);
		buff[index + 1] = (byte)((val >> 8) & 0xff);
		buff[index + 2] = (byte)(val & 0xff);
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

	public static int readUTF8(StringBuilder sb, byte dataBuff[], int dataOfst)
	{
		byte b = dataBuff[dataOfst];
		char code;
		if (b < 0x80)
		{
			sb.append((char)b);
			return dataOfst + 1;
		}
		else if ((b & 0xe0) == 0xc0)
		{
			sb.append((char)(((b & 0x1f) << 6) | (dataBuff[dataOfst + 1] & 0x3f)));
			return dataOfst + 2;
		}
		else if ((b & 0xf0) == 0xe0)
		{
			sb.append((char)(((b & 0x0f) << 12) | ((dataBuff[dataOfst + 1] & 0x3f) << 6) | (dataBuff[dataOfst + 2] & 0x3f)));
			return dataOfst + 3;
		}
		else if ((b & 0xf8) == 0xf0)
		{
			code = (char)(((b & 0x7) << 18) | ((dataBuff[0] & 0x3f) << 12) | ((dataBuff[1] & 0x3f) << 6) | (dataBuff[2] & 0x3f));
			sb.append(code);
			return dataOfst + 4;
		}
		else if ((b & 0xfc) == 0xf8)
		{
			code = (char)(((b & 0x3) << 24) | ((dataBuff[0] & 0x3f) << 18) | ((dataBuff[1] & 0x3f) << 12) | ((dataBuff[2] & 0x3f) << 6) | (dataBuff[3] & 0x3f));
			sb.append(code);
			return dataOfst + 5;
		}
		else if ((b & 0xfe) == 0xfc)
		{
			code = (char)(((b & 0x1) << 30) | ((dataBuff[0] & 0x3f) << 24) | ((dataBuff[1] & 0x3f) << 18) | ((dataBuff[2] & 0x3f) << 12) | ((dataBuff[3] & 0x3f) << 6) | (dataBuff[4] & 0x3f));
			sb.append(code);
			return dataOfst + 6;
		}
		return dataOfst + 6;
	}

	public static int writeUTF8(byte buff[], int ofst, char c)
	{
		if (c < 0x80)
		{
			buff[ofst++] = (byte)c;
		}
		else if (c < 0x800)
		{
			buff[ofst++] = (byte)(0xc0 | (c >> 6));
			buff[ofst++] = (byte)(0x80 | (c & 0x3f));
		}
		else if (c < 0x10000)
		{
			buff[ofst++] = (byte)(0xe0 | (c >> 12));
			buff[ofst++] = (byte)(0x80 | ((c >> 6) & 0x3f));
			buff[ofst++] = (byte)(0x80 | (c & 0x3f));
		}
		else if (c < 0x200000)
		{
			buff[ofst++] = (byte)(0xf0 | (c >> 18));
			buff[ofst++] = (byte)(0x80 | ((c >> 12) & 0x3f));
			buff[ofst++] = (byte)(0x80 | ((c >> 6) & 0x3f));
			buff[ofst++] = (byte)(0x80 | (c & 0x3f));
		}
		else if (c < 0x4000000)
		{
			buff[ofst++] = (byte)(0xf8 | (c >> 24));
			buff[ofst++] = (byte)(0x80 | ((c >> 18) & 0x3f));
			buff[ofst++] = (byte)(0x80 | ((c >> 12) & 0x3f));
			buff[ofst++] = (byte)(0x80 | ((c >> 6) & 0x3f));
			buff[ofst++] = (byte)(0x80 | (c & 0x3f));
		}
		else
		{
			buff[ofst++] = (byte)(0xfc | ByteTool.shr32(c, 30));
			buff[ofst++] = (byte)(0x80 | ((c >> 24) & 0x3f));
			buff[ofst++] = (byte)(0x80 | ((c >> 18) & 0x3f));
			buff[ofst++] = (byte)(0x80 | ((c >> 12) & 0x3f));
			buff[ofst++] = (byte)(0x80 | ((c >> 6) & 0x3f));
			buff[ofst++] = (byte)(0x80 | (c & 0x3f));
		}
		return ofst;		
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

	public static byte shr8(byte val, int cnt)
	{
		if (cnt < 0)
		{
			return (byte)(val << (-cnt));
		}
		if (cnt >= 32)
		{
			return 0;
		}
		if (cnt == 0)
		{
			return val;
		}
		return (byte)((val & (int)0xff) >> cnt);
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
		if (destArr == srcArr && destOfst > srcOfst)
		{
			while (size-- > 0)
			{
				destArr[destOfst + size] = srcArr[srcOfst + size];
			}
		}
		else
		{
			int i = 0;
			while (i < size)
			{
				destArr[destOfst + i] = srcArr[srcOfst + i];
				i++;
			}
		}
	}

	public static void copyArray(int destArr[], int destOfst, int srcArr[], int srcOfst, int size)
	{
		if (destArr == srcArr && destOfst > srcOfst)
		{
			while (size-- > 0)
			{
				destArr[destOfst + size] = srcArr[srcOfst + size];
			}
		}
		else
		{
			int i = 0;
			while (i < size)
			{
				destArr[destOfst + i] = srcArr[srcOfst + i];
				i++;
			}
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

	public static void arrayXOR(byte destArr[], int destOfst, byte srcArr1[], int srcOfst1, byte srcArr2[], int srcOfst2, int size)
	{
		while (size-- > 0)
		{
			destArr[destOfst++] = (byte)(srcArr1[srcOfst1++] ^ srcArr2[srcOfst2++]);
		}
	}

	public static void arrayFill(byte destArr[], int destOfst, int size, byte b)
	{
		while (size-- > 0)
		{
			destArr[destOfst++] = b;
		}
	}

	public static boolean strEquals(byte[] buff, int ofst, String s)
	{
		byte[] sbuff = s.getBytes(StandardCharsets.UTF_8);
		int i = sbuff.length;
		if (buff.length < ofst + i + 1)
		{
			return false;
		}
		if (buff[ofst + i] != 0)
		{
			return false;
		}
		while (i-- > 0)
		{
			if (buff[ofst + i] != sbuff[i])
			{
				return false;
			}
		}
		return true;
	}
}
