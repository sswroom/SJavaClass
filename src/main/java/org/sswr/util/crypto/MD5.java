package org.sswr.util.crypto;

import org.sswr.util.data.ByteTool;

import jakarta.annotation.Nonnull;

public class MD5 extends Hash
{
	private long msgLeng;
	private int[] h;
	private byte[] buff;
	private int buffSize;

	private static void step1(@Nonnull int[] vals, int w, int x, int y, int z, int dataNum, int s)
	{
		vals[w] += vals[z] ^ (vals[x] & (vals[y] ^ vals[z]));
		vals[w] += dataNum;
		vals[w] = ByteTool.ror32(vals[w], 32-s);
		vals[w] += vals[x];
	}
	
	private static void step2(@Nonnull int[] vals, int w, int x, int y, int z, int dataNum, int s)
	{
		vals[w] += vals[y] ^ (vals[z] & (vals[x] ^ vals[y]));
		vals[w] += dataNum;
		vals[w] = ByteTool.ror32(vals[w], 32-s);
		vals[w] += vals[x];
	}

	private static void step3(@Nonnull int[] vals, int w, int x, int y, int z, int dataNum, int s)
	{
		vals[w] += vals[z] ^ vals[y] ^ vals[x];
		vals[w] += dataNum;
		vals[w] = ByteTool.ror32(vals[w], 32-s);
		vals[w] += vals[x];
	}

	private static void step4(@Nonnull int[] vals, int w, int x, int y, int z, int dataNum, int s)
	{
		vals[w] += vals[y] ^ (vals[x] | ~vals[z]);
		vals[w] += dataNum;
		vals[w] = ByteTool.ror32(vals[w], 32-s);
		vals[w] += vals[x];
	}

	private static void calcBlock(@Nonnull int[] hVals, @Nonnull byte[] blocks, int ofst, int nblock)
	{
		int[] blk = new int[16];
		int[] vals = new int[4];
		vals[0] = hVals[0];
		vals[1] = hVals[1];
		vals[2] = hVals[2];
		vals[3] = hVals[3];
		int a = 0;
		int b = 1;
		int c = 2;
		int d = 3;
	
		while (nblock-- > 0)
		{
			blk[0] = ByteTool.readInt32(blocks, ofst);
			blk[1] = ByteTool.readInt32(blocks, ofst + 4);
			blk[2] = ByteTool.readInt32(blocks, ofst + 8);
			blk[3] = ByteTool.readInt32(blocks, ofst + 12);
			blk[4] = ByteTool.readInt32(blocks, ofst + 16);
			blk[5] = ByteTool.readInt32(blocks, ofst + 20);
			blk[6] = ByteTool.readInt32(blocks, ofst + 24);
			blk[7] = ByteTool.readInt32(blocks, ofst + 28);
			blk[8] = ByteTool.readInt32(blocks, ofst + 32);
			blk[9] = ByteTool.readInt32(blocks, ofst + 36);
			blk[10] = ByteTool.readInt32(blocks, ofst + 40);
			blk[11] = ByteTool.readInt32(blocks, ofst + 44);
			blk[12] = ByteTool.readInt32(blocks, ofst + 48);
			blk[13] = ByteTool.readInt32(blocks, ofst + 52);
			blk[14] = ByteTool.readInt32(blocks, ofst + 56);
			blk[15] = ByteTool.readInt32(blocks, ofst + 60);
			step1(vals, a, b, c, d, blk[0] + 0xd76aa478, 7);
			step1(vals, d, a, b, c, blk[1] + 0xe8c7b756, 12);
			step1(vals, c, d, a, b, blk[2] + 0x242070db, 17);
			step1(vals, b, c, d, a, blk[3] + 0xc1bdceee, 22);
			step1(vals, a, b, c, d, blk[4] + 0xf57c0faf, 7);
			step1(vals, d, a, b, c, blk[5] + 0x4787c62a, 12);
			step1(vals, c, d, a, b, blk[6] + 0xa8304613, 17);
			step1(vals, b, c, d, a, blk[7] + 0xfd469501, 22);
			step1(vals, a, b, c, d, blk[8] + 0x698098d8, 7);
			step1(vals, d, a, b, c, blk[9] + 0x8b44f7af, 12);
			step1(vals, c, d, a, b, blk[10] + 0xffff5bb1, 17);
			step1(vals, b, c, d, a, blk[11] + 0x895cd7be, 22);
			step1(vals, a, b, c, d, blk[12] + 0x6b901122, 7);
			step1(vals, d, a, b, c, blk[13] + 0xfd987193, 12);
			step1(vals, c, d, a, b, blk[14] + 0xa679438e, 17);
			step1(vals, b, c, d, a, blk[15] + 0x49b40821, 22);
	
			step2(vals, a, b, c, d, blk[1] + 0xf61e2562, 5);
			step2(vals, d, a, b, c, blk[6] + 0xc040b340, 9);
			step2(vals, c, d, a, b, blk[11] + 0x265e5a51, 14);
			step2(vals, b, c, d, a, blk[0] + 0xe9b6c7aa, 20);
			step2(vals, a, b, c, d, blk[5] + 0xd62f105d, 5);
			step2(vals, d, a, b, c, blk[10] + 0x02441453, 9);
			step2(vals, c, d, a, b, blk[15] + 0xd8a1e681, 14);
			step2(vals, b, c, d, a, blk[4] + 0xe7d3fbc8, 20);
			step2(vals, a, b, c, d, blk[9] + 0x21e1cde6, 5);
			step2(vals, d, a, b, c, blk[14] + 0xc33707d6, 9);
			step2(vals, c, d, a, b, blk[3] + 0xf4d50d87, 14);
			step2(vals, b, c, d, a, blk[8] + 0x455a14ed, 20);
			step2(vals, a, b, c, d, blk[13] + 0xa9e3e905, 5);
			step2(vals, d, a, b, c, blk[2] + 0xfcefa3f8, 9);
			step2(vals, c, d, a, b, blk[7] + 0x676f02d9, 14);
			step2(vals, b, c, d, a, blk[12] + 0x8d2a4c8a, 20);
	
			step3(vals, a, b, c, d, blk[5] + 0xfffa3942, 4);
			step3(vals, d, a, b, c, blk[8] + 0x8771f681, 11);
			step3(vals, c, d, a, b, blk[11] + 0x6d9d6122, 16);
			step3(vals, b, c, d, a, blk[14] + 0xfde5380c, 23);
			step3(vals, a, b, c, d, blk[1] + 0xa4beea44, 4);
			step3(vals, d, a, b, c, blk[4] + 0x4bdecfa9, 11);
			step3(vals, c, d, a, b, blk[7] + 0xf6bb4b60, 16);
			step3(vals, b, c, d, a, blk[10] + 0xbebfbc70, 23);
			step3(vals, a, b, c, d, blk[13] + 0x289b7ec6, 4);
			step3(vals, d, a, b, c, blk[0] + 0xeaa127fa, 11);
			step3(vals, c, d, a, b, blk[3] + 0xd4ef3085, 16);
			step3(vals, b, c, d, a, blk[6] + 0x04881d05, 23);
			step3(vals, a, b, c, d, blk[9] + 0xd9d4d039, 4);
			step3(vals, d, a, b, c, blk[12] + 0xe6db99e5, 11);
			step3(vals, c, d, a, b, blk[15] + 0x1fa27cf8, 16);
			step3(vals, b, c, d, a, blk[2] + 0xc4ac5665, 23);
	
			step4(vals, a, b, c, d, blk[0] + 0xf4292244, 6);
			step4(vals, d, a, b, c, blk[7] + 0x432aff97, 10);
			step4(vals, c, d, a, b, blk[14] + 0xab9423a7, 15);
			step4(vals, b, c, d, a, blk[5] + 0xfc93a039, 21);
			step4(vals, a, b, c, d, blk[12] + 0x655b59c3, 6);
			step4(vals, d, a, b, c, blk[3] + 0x8f0ccc92, 10);
			step4(vals, c, d, a, b, blk[10] + 0xffeff47d, 15);
			step4(vals, b, c, d, a, blk[1] + 0x85845dd1, 21);
			step4(vals, a, b, c, d, blk[8] + 0x6fa87e4f, 6);
			step4(vals, d, a, b, c, blk[15] + 0xfe2ce6e0, 10);
			step4(vals, c, d, a, b, blk[6] + 0xa3014314, 15);
			step4(vals, b, c, d, a, blk[13] + 0x4e0811a1, 21);
			step4(vals, a, b, c, d, blk[4] + 0xf7537e82, 6);
			step4(vals, d, a, b, c, blk[11] + 0xbd3af235, 10);
			step4(vals, c, d, a, b, blk[2] + 0x2ad7d2bb, 15);
			step4(vals, b, c, d, a, blk[9] + 0xeb86d391, 21);
	
			vals[a] += hVals[0];
			vals[b] += hVals[1];
			vals[c] += hVals[2];
			vals[d] += hVals[3];
			hVals[0] = vals[a];
			hVals[1] = vals[b];
			hVals[2] = vals[c];
			hVals[3] = vals[d];
			ofst += 16;
		}
	}

	public MD5()
	{
		this.h = new int[4];
		this.buff = new byte[64];
		this.clear();
		this.msgLeng = 0;
		this.buffSize = 0;
	}

	@Override
	@Nonnull
	public String getName() {
		return "MD5";
	}

	@Override
	@Nonnull
	public Hash clone() {
		MD5 md5 = new MD5();
		md5.msgLeng = this.msgLeng;
		md5.h[0] = this.h[0];
		md5.h[1] = this.h[1];
		md5.h[2] = this.h[2];
		md5.h[3] = this.h[3];
		ByteTool.copyArray(md5.buff, 0, this.buff, 0, this.buffSize);
		md5.buffSize = this.buffSize;
		return md5;
	}

	@Override
	public void clear() {
		this.msgLeng = 0;
		this.h[0] = 0x67452301;
		this.h[1] = 0xEFCDAB89;
		this.h[2] = 0x98BADCFE;
		this.h[3] = 0x10325476;
		this.buffSize = 0;
	}

	@Override
	public void calc(@Nonnull byte[] buff, int ofst, int buffSize) {
		this.msgLeng += (buffSize << 3);
		if ((buffSize + this.buffSize) < 64)
		{
			ByteTool.copyArray(this.buff, this.buffSize, buff, ofst, buffSize);
			this.buffSize += buffSize;
			return;
		}
	
		if (this.buffSize > 0)
		{
			ByteTool.copyArray(this.buff, this.buffSize, buff, ofst, 64 - this.buffSize);
			calcBlock(this.h, this.buff, 0, 1);
			ofst += 64 - this.buffSize;
			buffSize -= 64 - this.buffSize;
			this.buffSize = 0;
		}
		if (buffSize >= 64)
		{
			calcBlock(this.h, buff, ofst, buffSize >> 6);
			ofst += buffSize & (int)~63;
			buffSize = buffSize & 63;
		}
		if (buffSize > 0)
		{
			ByteTool.copyArray(this.buff, 0, buff, ofst, this.buffSize = buffSize);
		}
	}

	@Override
	@Nonnull
	public byte[] getValue() {
		byte[] calBuff = new byte[64];
		int[] v = new int[4];
		v[0] = this.h[0];
		v[1] = this.h[1];
		v[2] = this.h[2];
		v[3] = this.h[3];
		int i;
		if (buffSize < 56)
		{
			ByteTool.copyArray(calBuff, 0, this.buff, 0, buffSize);
			i = buffSize;
			calBuff[i++] = (byte)0x80;
			while (i < 56)
			{
				calBuff[i++] = 0;
			}
			ByteTool.writeInt64(calBuff, 56, msgLeng);
			calcBlock(v, calBuff, 0, 1);
		}
		else
		{
			ByteTool.copyArray(calBuff, 0, this.buff, 0, buffSize);
			i = buffSize;
			calBuff[i++] = (byte)0x80;
			while (i < 64)
			{
				calBuff[i++] = 0;
			}
			calcBlock(v, calBuff, 0, 1);
	
			ByteTool.arrayFill(calBuff, 0, 56, (byte)0);
			ByteTool.writeInt64(calBuff, 56, msgLeng);
			calcBlock(v, calBuff, 0, 1);
		}
		byte[] buff = new byte[16];
		ByteTool.writeInt32(buff, 0, v[0]);
		ByteTool.writeInt32(buff, 4, v[1]);
		ByteTool.writeInt32(buff, 8, v[2]);
		ByteTool.writeInt32(buff, 12, v[3]);
		return buff;
	}

	@Override
	public int getBlockSize() {
		return 64;
	}

	@Override
	public int getResultSize() {
		return 16;
	}
	
}
