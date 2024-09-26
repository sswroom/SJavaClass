package org.sswr.util.data;

import jakarta.annotation.Nonnull;

public class RandomBytesGenerator
{
	private RandomMT19937 random;
	public RandomBytesGenerator()
	{
		this.random = null;
	}

	@Nonnull
	public byte[] nextBytes(int len)
	{
		if (this.random == null)
		{
			this.random = new RandomMT19937((int)System.currentTimeMillis());
		}
		byte[] retBuff = new byte[len];
		int i = 0;
		while (len >= 4)
		{
			ByteTool.writeInt32(retBuff, i, this.random.nextInt32());
			len -= 4;
			i += 4;
		}
		if (len > 0)
		{
			byte[] tmpBuff = new byte[4];
			ByteTool.writeInt32(tmpBuff, 0, this.random.nextInt32());
			while (len-- > 0)
			{
				retBuff[i + len] = tmpBuff[len];
			}
		}
		return retBuff;	
	}
}
