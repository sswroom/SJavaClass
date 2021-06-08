package org.sswr.util.data;

public class RandomMT19937
{
	private int mt[];
	private int mt_index;

	public RandomMT19937(int seed)
	{
		this.mt = new int[624];
		this.mt_index = 624;
	
		mt[0] = seed;
		int i = 1;
		while (i < 624)
		{
			this.mt[i] = (int)((1812433253L * (this.mt[i - 1] ^ ((this.mt[i - 1] >> 30) & 3)) + i) & 0xffffffff);
			i++;
		}
	}

	public int nextInt32()
	{
		if (this.mt_index >= 624)
		{
			int i = 0;
			while (i < 624)
			{
				int y = (this.mt[i] & 0x80000000) + (this.mt[(i + 1) % 624] & 0x7fffffff);
				this.mt[i] = this.mt[(i + 397) % 624] ^ ((y >> 1) & 0x7fffffff);
				if (y % 2 != 0)
				{
					this.mt[i] ^= 0x9908b0df;
				}
				i++;
			}
			this.mt_index = 0;
		}
	   
		int y = this.mt[this.mt_index];
		y ^= ByteTool.shr32(y, 11);
		y ^= (y << 7) & 0x9d2c5680;
		y ^= (y << 15) & 0xefc60000;
		y ^= ByteTool.shr32(y, 18);
	   
		this.mt_index++;
		return y;
	}
}
