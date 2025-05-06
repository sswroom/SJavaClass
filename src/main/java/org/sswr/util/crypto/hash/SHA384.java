package org.sswr.util.crypto.hash;

import org.sswr.util.data.ByteTool;

import jakarta.annotation.Nonnull;

public final class SHA384 extends Hash
{
	private long intermediateHash[];
	private byte messageBlock[];
	private long messageLength;
	private int messageBlockIndex;

	public SHA384()
	{
		this.intermediateHash = new long[8];
		this.messageBlock = new byte[128];
		this.clear();
	}

	@Nonnull
	public String getName()
	{
		return "SHA-384";
	}

	@Nonnull
	public Hash clone()
	{
		SHA384 sha384 = new SHA384();
		sha384.messageLength = this.messageLength;
		sha384.messageBlockIndex = this.messageBlockIndex;
		sha384.messageBlock = this.messageBlock.clone();
		sha384.intermediateHash = this.intermediateHash.clone();
		return sha384;
	}

	public void clear()
	{
		this.messageLength         = 0;
		this.messageBlockIndex     = 0;
		
		this.intermediateHash[0]   = 0xcbbb9d5dc1059ed8L;
		this.intermediateHash[1]   = 0x629a292a367cd507L;
		this.intermediateHash[2]   = 0x9159015a3070dd17L;
		this.intermediateHash[3]   = 0x152fecd8f70e5939L;
		this.intermediateHash[4]   = 0x67332667ffc00b31L;
		this.intermediateHash[5]   = 0x8eb44a8768581511L;
		this.intermediateHash[6]   = 0xdb0c2e0d64f98fa7L;
		this.intermediateHash[7]   = 0x47b5481dbefa4fa4L;
	}

	public void calc(@Nonnull byte buff[], int ofst, int buffSize)
	{
		this.messageLength += (buffSize << 3);
		if ((buffSize + this.messageBlockIndex) < 128)
		{
			ByteTool.copyArray(this.messageBlock, this.messageBlockIndex, buff, ofst, buffSize);
			this.messageBlockIndex += buffSize;
			return;
		}
		
		if (this.messageBlockIndex > 0)
		{
			ByteTool.copyArray(this.messageBlock, this.messageBlockIndex, buff, ofst, 128 - this.messageBlockIndex);
			SHA512.calcBlock(this.intermediateHash, this.messageBlock, 0);
			ofst += 128 - this.messageBlockIndex;
			buffSize -= 128 - this.messageBlockIndex;
			this.messageBlockIndex = 0;
		}
	
		while (buffSize >= 128)
		{
			SHA512.calcBlock(this.intermediateHash, buff, ofst);
			ofst += 128;
			buffSize -= 128;
		}
		if (buffSize > 0)
		{
			ByteTool.copyArray(this.messageBlock, 0, buff, ofst, this.messageBlockIndex = buffSize);
		}
	}

	@Nonnull
	public byte []getValue()
	{
		byte calBuff[] = new byte[128];
		long intHash[] = this.intermediateHash.clone();
	
		int i;
		if (this.messageBlockIndex < 111)
		{
			ByteTool.copyArray(calBuff, 0, this.messageBlock, 0, messageBlockIndex);
			i = messageBlockIndex;
			calBuff[i++] = (byte)0x80;
			while (i < 120)
			{
				calBuff[i++] = 0;
			}
		}
		else
		{
			ByteTool.copyArray(calBuff, 0, this.messageBlock, 0, messageBlockIndex);
			i = messageBlockIndex;
			calBuff[i++] = (byte)0x80;
			while (i < 128)
			{
				calBuff[i++] = 0;
			}
			SHA512.calcBlock(this.intermediateHash, calBuff, 0);
	
			ByteTool.clearArray(calBuff, 0, 120);
		}
	
		ByteTool.writeMInt64(calBuff, 120, this.messageLength);
		SHA512.calcBlock(this.intermediateHash, calBuff, 0);
		byte retBuff[] = new byte[48];
		i = 6;
		while (i > 0)
		{
			i--;
			ByteTool.writeMInt64(retBuff, i * 8, this.intermediateHash[i]);
		}
		this.intermediateHash = intHash;
		return retBuff;
	}

	public int getBlockSize()
	{
		return 128;
	}

	public int getResultSize()
	{
		return 48;
	}
}
