package org.sswr.util.crypto;

import org.sswr.util.crypto.hash.Hash;
import org.sswr.util.data.ByteTool;

import jakarta.annotation.Nonnull;

public class PBKDF2
{
	@Nonnull
	private static byte []f(@Nonnull byte salt[], int iterationCount, int i, @Nonnull Hash hashFunc)
	{
		byte buff1[];
		byte buff2[];
		byte iBuff[] = new byte[4];
		ByteTool.writeMInt32(iBuff, 0, i);
		hashFunc.clear();
		hashFunc.calc(salt, 0, salt.length);
		hashFunc.calc(iBuff, 0, 4);
		buff2 = hashFunc.getValue();
		buff1 = buff2;
		i = 1;
		while (i < iterationCount)
		{
			i++;
			hashFunc.clear();
			hashFunc.calc(buff2, 0, buff2.length);
			buff2 = hashFunc.getValue();
			ByteTool.arrayXOR(buff1, 0, buff1, 0, buff2, 0, buff1.length);
		}
		return buff1;
	}

	@Nonnull
	public static byte []pbkdf2(@Nonnull byte salt[], int iterationCount, int dkLen, @Nonnull Hash hashFunc)
	{
		byte outBuff[] = new byte[dkLen];
		byte blockBuff[];
		int i = 1;
		int ofst = 0;
		while (ofst < dkLen)
		{
			blockBuff = f(salt, iterationCount, i, hashFunc);
			if (ofst + blockBuff.length > dkLen)
			{
				ByteTool.copyArray(outBuff, ofst, blockBuff, 0, dkLen - ofst);
				ofst = dkLen;
				break;
			}
			else
			{
				ByteTool.copyArray(outBuff, ofst, blockBuff, 0, blockBuff.length);
				ofst += blockBuff.length;
			}
			i++;
		}
		return outBuff;
	}
}
