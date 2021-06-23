package org.sswr.util.crypto;

import org.sswr.util.data.ByteTool;

public class CRC32R implements Hash
{
	private int[] crctab;
	private int currVal;

	private static void initTable(int[] tab, int rpn)
	{
		int i = 256;
		int j;
		int v;
		while (i-- > 0)
		{
			v = i;
			j = 8;
			while (j-- > 0)
			{
				if ((v & 1) != 0)
				{
					v = ((v >> 1) & 0x7fffffff) ^ rpn;
				}
				else
				{
					v = (v >> 1) & 0x7fffffff;
				}
			}
			tab[i] = v;
		}
	
		i = 256;
		while (i-- > 0)
		{
			tab[256  + i] = ByteTool.shr32(tab[0    + i] , 8) ^ tab[tab[0    + i] & 0xff];
			tab[512  + i] = ByteTool.shr32(tab[256  + i] , 8) ^ tab[tab[256  + i] & 0xff];
			tab[768  + i] = ByteTool.shr32(tab[512  + i] , 8) ^ tab[tab[512  + i] & 0xff];
			tab[1024 + i] = ByteTool.shr32(tab[768  + i] , 8) ^ tab[tab[768  + i] & 0xff];
			tab[1280 + i] = ByteTool.shr32(tab[1024 + i] , 8) ^ tab[tab[1024 + i] & 0xff];
			tab[1536 + i] = ByteTool.shr32(tab[1280 + i] , 8) ^ tab[tab[1280 + i] & 0xff];
			tab[1792 + i] = ByteTool.shr32(tab[1536 + i] , 8) ^ tab[tab[1536 + i] & 0xff];
			tab[2048 + i] = ByteTool.shr32(tab[1792 + i] , 8) ^ tab[tab[1792 + i] & 0xff];
			tab[2304 + i] = ByteTool.shr32(tab[2048 + i] , 8) ^ tab[tab[2048 + i] & 0xff];
			tab[2560 + i] = ByteTool.shr32(tab[2304 + i] , 8) ^ tab[tab[2304 + i] & 0xff];
			tab[2816 + i] = ByteTool.shr32(tab[2560 + i] , 8) ^ tab[tab[2560 + i] & 0xff];
			tab[3072 + i] = ByteTool.shr32(tab[2816 + i] , 8) ^ tab[tab[2816 + i] & 0xff];
			tab[3328 + i] = ByteTool.shr32(tab[3072 + i] , 8) ^ tab[tab[3072 + i] & 0xff];
			tab[3584 + i] = ByteTool.shr32(tab[3328 + i] , 8) ^ tab[tab[3328 + i] & 0xff];
			tab[3840 + i] = ByteTool.shr32(tab[3584 + i] , 8) ^ tab[tab[3584 + i] & 0xff];
		}
	}

	private static int reverse(int polynomial)
	{
		int v;
		int v2;
		int i = 32;
		v = polynomial;
		v2 = 0;
		while (i-- > 0)
		{
			v2 = ((v2 >> 1) & 0x7fffffff) | (v & 0x80000000);
			v <<= 1;
		}
		return v2;
	}

	private static int calc(byte[] buff, int buffOfst, int buffSize, int[]tab, int currVal)
	{
		while (buffSize >= 16)
		{
			int currVal1 = ByteTool.readInt32(buff, buffOfst) ^ currVal;
			int currVal2 = ByteTool.readInt32(buff, buffOfst + 4);
			int currVal3 = ByteTool.readInt32(buff, buffOfst + 8);
			int currVal4 = ByteTool.readInt32(buff, buffOfst + 12);
			buffOfst += 16;
			currVal  = tab[0    + ((currVal4 >> 24) & 0xff)];
			currVal ^= tab[256  + ((currVal4 >> 16) & 0xff)];
			currVal ^= tab[512  + ((currVal4 >> 8) & 0xff)];
			currVal ^= tab[768  +  (currVal4 & 0xff)];
			currVal ^= tab[1024 + ((currVal3 >> 24) & 0xff)];
			currVal ^= tab[1280 + ((currVal3 >> 16) & 0xff)];
			currVal ^= tab[1536 + ((currVal3 >> 8) & 0xff)];
			currVal ^= tab[1792 +  (currVal3 & 0xff)];
			currVal ^= tab[2048 + ((currVal2 >> 24) & 0xff)];
			currVal ^= tab[2304 + ((currVal2 >> 16) & 0xff)];
			currVal ^= tab[2560 + ((currVal2 >> 8) & 0xff)];
			currVal ^= tab[2816 +  (currVal2 & 0xff)];
			currVal ^= tab[3072 + ((currVal1 >> 24) & 0xff)];
			currVal ^= tab[3328 + ((currVal1 >> 16) & 0xff)];
			currVal ^= tab[3584 + ((currVal1 >> 8) & 0xff)];
			currVal ^= tab[3840 +  (currVal1  & 0xff)];
			buffSize -= 16;
		}
		while (buffSize >= 4)
		{
			currVal ^= ByteTool.readInt32(buff, buffOfst);
			buffOfst += 4;
			currVal = tab[768 + (currVal & 0xff)] ^ tab[512 + ((currVal >> 8) & 0xff)] ^  tab[256 + ((currVal >> 16) & 0xff)] ^ tab[0 + ((currVal >> 24) & 0xff)];
			buffSize -= 4;
		}
		while (buffSize-- > 0)
		{
			currVal = tab[(currVal & 0xff) ^ (buff[buffOfst] & 0xff)] ^ ByteTool.shr32(currVal, 8);
			buffOfst++;
		}
		return currVal;
	}

	private CRC32R(CRC32R crc)
	{
		this.crctab = new int[256 * 16];
		this.currVal = crc.currVal;
		ByteTool.copyArray(this.crctab, 0, crc.crctab, 0, 256 * 16);
	}
	
	private void initTable(int polynomial)
	{
		this.currVal = 0xffffffff;

		int rpn = reverse(polynomial);
		int []tab = crctab = new int[256 * 16];
		initTable(tab, rpn);
	}

	public CRC32R()
	{
		this.initTable(getPolynormialIEEE());
	}

	public CRC32R(int polynomial)
	{
		this.initTable(polynomial);
	}

	public String getName()
	{
		return "CRC (32-bit Reversed)";
	}
	
	public Hash clone()
	{
		return new CRC32R(this);
	}

	public void clear()
	{
		this.currVal = 0xffffffff;
	}

	public void calc(byte[] buff, int buffOfst, int buffSize)
	{
		this.currVal = calc(buff, buffOfst, buffSize, this.crctab, this.currVal);
	}

	public byte[] getValue()
	{
		byte[] buff = new byte[4];
		ByteTool.writeMInt32(buff, 0, ~this.currVal);
		return buff;
	}

	public int getBlockSize()
	{
		return 1;
	}

	public int getResultSize()
	{
		return 4;
	}

	public static int getPolynormialIEEE()
	{
		return 0x04C11DB7;
	}
}
