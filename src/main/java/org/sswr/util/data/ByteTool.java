package org.sswr.util.data;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.sswr.util.math.Coord2DDbl;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class ByteTool {
	public static int readInt32(@Nonnull byte buff[], int index)
	{
		return (buff[index] & 0xff) | 
			((buff[index + 1] & 0xff) << 8) |
			((buff[index + 2] & 0xff) << 16) |
			((buff[index + 3] & 0xff) << 24);
	}

	public static int readMInt32(@Nonnull byte buff[], int index)
	{
		return ((buff[index] & 0xff) << 24) | 
			((buff[index + 1] & 0xff) << 16) |
			((buff[index + 2] & 0xff) << 8) |
			(buff[index + 3] & 0xff);
	}

	public static int readUInt16(@Nonnull byte buff[], int index)
	{
		return ((buff[index + 1] & 0xff) << 8) | 
			(buff[index] & 0xff);
	}

	public static int readInt16(@Nonnull byte buff[], int index)
	{
		return (((int)buff[index + 1]) << 8) | 
			(buff[index] & 0xff);
	}

	public static int readMUInt16(@Nonnull byte buff[], int index)
	{
		return ((buff[index] & 0xff) << 8) | 
			(buff[index + 1] & 0xff);
	}

	public static int readMInt16(@Nonnull byte buff[], int index)
	{
		return (((int)buff[index]) << 8) | 
			(buff[index + 1] & 0xff);
	}

	public static int readUInt24(@Nonnull byte buff[], int index)
	{
		return ((buff[index + 2] & 0xff) << 16) | 
			((buff[index + 1] & 0xff) << 8) | 
			(buff[index + 0] & 0xff);
	}

	public static int readMUInt24(@Nonnull byte buff[], int index)
	{
		return ((buff[index] & 0xff) << 16) | 
			((buff[index + 1] & 0xff) << 8) | 
			(buff[index + 2] & 0xff);
	}

	public static int readMInt24(@Nonnull byte buff[], int index)
	{
		return (((int)buff[index + 2]) << 16) | 
			((buff[index + 1] & 0xff) << 8) | 
			(buff[index + 0] & 0xff);
	}

	public static long readInt64(@Nonnull byte buff[], int index)
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

	public static long readMInt64(@Nonnull byte buff[], int index)
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

	public static float readSingle(@Nonnull byte buff[], int index)
	{
		return Float.intBitsToFloat(readInt32(buff, index));
	}

	public static float readMSingle(@Nonnull byte buff[], int index)
	{
		return Float.intBitsToFloat(readMInt32(buff, index));
	}

	public static double readDouble(@Nonnull byte buff[], int index)
	{
		return Double.longBitsToDouble(readInt64(buff, index));
	}

	public static double readMDouble(@Nonnull byte buff[], int index)
	{
		return Double.longBitsToDouble(readMInt64(buff, index));
	}

	public static void writeInt16(@Nonnull byte buff[], int index, int val)
	{
		buff[index + 0] = (byte)(val & 0xff);
		buff[index + 1] = (byte)((val >> 8) & 0xff);
	}

	public static void writeInt32(@Nonnull byte buff[], int index, int val)
	{
		buff[index + 0] = (byte)(val & 0xff);
		buff[index + 1] = (byte)((val >> 8) & 0xff);
		buff[index + 2] = (byte)((val >> 16) & 0xff);
		buff[index + 3] = (byte)((val >> 24) & 0xff);
	}

	public static void writeInt64(@Nonnull byte buff[], int index, long val)
	{
		buff[index + 0] = (byte)(val & 0xff);
		buff[index + 1] = (byte)((val >> 8) & 0xff);
		buff[index + 2] = (byte)((val >> 16) & 0xff);
		buff[index + 3] = (byte)((val >> 24) & 0xff);
		buff[index + 4] = (byte)((val >> 32) & 0xff);
		buff[index + 5] = (byte)((val >> 40) & 0xff);
		buff[index + 6] = (byte)((val >> 48) & 0xff);
		buff[index + 7] = (byte)((val >> 56) & 0xff);
	}

	public static void writeMInt16(@Nonnull byte buff[], int index, int val)
	{
		buff[index + 0] = (byte)((val >> 8) & 0xff);
		buff[index + 1] = (byte)(val & 0xff);
	}

	public static void writeMInt24(@Nonnull byte buff[], int index, int val)
	{
		buff[index + 0] = (byte)((val >> 16) & 0xff);
		buff[index + 1] = (byte)((val >> 8) & 0xff);
		buff[index + 2] = (byte)(val & 0xff);
	}

	public static void writeMInt32(@Nonnull byte buff[], int index, int val)
	{
		buff[index + 0] = (byte)((val >> 24) & 0xff);
		buff[index + 1] = (byte)((val >> 16) & 0xff);
		buff[index + 2] = (byte)((val >> 8) & 0xff);
		buff[index + 3] = (byte)(val & 0xff);
	}

	public static void writeMInt64(@Nonnull byte buff[], int index, long val)
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

	public static void writeSingle(@Nonnull byte buff[], int index, float val)
	{
		writeInt32(buff, index, Float.floatToIntBits(val));
	}

	public static void writeMSingle(@Nonnull byte buff[], int index, float val)
	{
		writeMInt32(buff, index, Float.floatToIntBits(val));
	}

	public static void writeDouble(@Nonnull byte buff[], int index, double val)
	{
		writeInt64(buff, index, Double.doubleToLongBits(val));
	}

	public static void writeMDouble(@Nonnull byte buff[], int index, double val)
	{
		writeMInt64(buff, index, Double.doubleToLongBits(val));
	}

	public static int readUTF8(@Nonnull StringBuilder sb, @Nonnull byte dataBuff[], int dataOfst)
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

	public static int writeUTF8(@Nonnull byte buff[], int ofst, char c)
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
		return (byte)((val & 0xff) >> cnt);
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

	public static void copyArray(@Nonnull byte destArr[], int destOfst, @Nonnull byte srcArr[], int srcOfst, int size)
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

	public static void copyArray(@Nonnull int destArr[], int destOfst, @Nonnull int srcArr[], int srcOfst, int size)
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

	public static void copyArray(@Nonnull int destArr[], int destOfst, @Nonnull List<Integer> srcArr, int srcOfst, int size)
	{
		int i = 0;
		while (i < size)
		{
			destArr[destOfst + i] = srcArr.get(srcOfst + i);
			i++;
		}
	}

	public static void copyArray(@Nonnull double destArr[], int destOfst, @Nonnull double srcArr[], int srcOfst, int size)
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

	public static void copyArray(@Nonnull Coord2DDbl destArr[], int destOfst, @Nonnull Coord2DDbl srcArr[], int srcOfst, int size)
	{
		if (destArr == srcArr && destOfst > srcOfst)
		{
			while (size-- > 0)
			{
				destArr[destOfst + size] = srcArr[srcOfst + size].clone();
			}
		}
		else
		{
			int i = 0;
			while (i < size)
			{
				destArr[destOfst + i] = srcArr[srcOfst + i].clone();
				i++;
			}
		}
	}

	public static void copyArray(@Nonnull Coord2DDbl destArr[], int destOfst, @Nonnull List<Double> srcArr, int srcOfst, int destCnt)
	{
		int i = 0;
		while (i < destCnt)
		{
			destArr[destOfst + i] = new Coord2DDbl(srcArr.get(srcOfst + i * 2), srcArr.get(srcOfst + i * 2 + 1));
			i++;
		}
	}

	public static void copyArray(@Nonnull Object destArr[], int destOfst, @Nonnull Object srcArr[], int srcOfst, int size)
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

	public static void clearArray(@Nonnull byte arr[], int ofst, int size)
	{
		int i = 0;
		while (i < size)
		{
			arr[ofst + i] = 0;
			i++;
		}
	}

	public static void arrayXOR(@Nonnull byte destArr[], int destOfst, @Nonnull byte srcArr1[], int srcOfst1, @Nonnull byte srcArr2[], int srcOfst2, int size)
	{
		while (size-- > 0)
		{
			destArr[destOfst++] = (byte)(srcArr1[srcOfst1++] ^ srcArr2[srcOfst2++]);
		}
	}

	public static void arrayFill(@Nonnull byte destArr[], int destOfst, int size, byte b)
	{
		while (size-- > 0)
		{
			destArr[destOfst++] = b;
		}
	}

	public static boolean strEquals(@Nonnull byte[] buff, int ofst, @Nonnull String s)
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

	public static boolean byteEquals(@Nonnull byte[] buff1, int ofst1, @Nonnull byte[] buff2, int ofst2, int len)
	{
		while (len-- > 0)
		{
			if (buff1[ofst1 + len] != buff2[ofst2 + len])
				return false;
		}
		return true;
	}

	public static <T> void listAddArray(@Nonnull List<T> list, @Nonnull T[] arr, int ofst, int len)
	{
		int i = 0;
		while (i < len)
		{
			list.add(arr[ofst + i]);
			i++;
		}
	}

	@Nonnull
	public static byte[] toByteArray(@Nonnull Coord2DDbl[] arr, boolean lsb)
	{
		return toByteArray(arr, 0, arr.length, lsb);
	}

	@Nonnull
	public static byte[] toByteArray(@Nonnull Coord2DDbl[] arr, int ofst, int len, boolean lsb)
	{
		byte[] ret = new byte[len * 16];
		int i = 0;
		if (lsb)
		{
			while (i < len)
			{
				writeDouble(ret, i * 16 + 0, arr[ofst + i].x);
				writeDouble(ret, i * 16 + 8, arr[ofst + i].y);
				i++;
			}
		}
		else
		{
			while (i < len)
			{
				writeMDouble(ret, i * 16 + 0, arr[ofst + i].x);
				writeMDouble(ret, i * 16 + 8, arr[ofst + i].y);
				i++;
			}
		}
		return ret;
	}

	public static int intOr(@Nullable Integer val, int nullVal)
	{
		if (val == null)
			return nullVal;
		else
			return val.intValue();
	}

	public static byte[] subArray(@Nonnull byte[] buff, int ofst, int size)
	{
		byte[] ret = new byte[size];
		copyArray(ret, 0, buff, ofst, size);
		return ret;
	}
}
