package org.sswr.util.crypto;

import org.sswr.util.data.ByteTool;

public class SHA224 extends Hash
{
	private int intermediateHash[];
	private byte messageBlock[];
	private long messageLength;
	private int messageBlockIndex;

	public SHA224()
	{
		this.intermediateHash = new int[8];
		this.messageBlock = new byte[64];
		this.clear();
	}

	public String getName()
	{
		return "SHA-224";
	}

	public Hash clone()
	{
		SHA224 sha224 = new SHA224();
		sha224.messageLength = this.messageLength;
		sha224.messageBlockIndex = this.messageBlockIndex;
		sha224.messageBlock = this.messageBlock.clone();
		sha224.intermediateHash = this.intermediateHash.clone();
		return sha224;
	}

	public void clear()
	{
		this.messageLength         = 0;
		this.messageBlockIndex     = 0;
		
		this.intermediateHash[0]   = 0xc1059ed8;
		this.intermediateHash[1]   = 0x367cd507;
		this.intermediateHash[2]   = 0x3070dd17;
		this.intermediateHash[3]   = 0xf70e5939;
		this.intermediateHash[4]   = 0xffc00b31;
		this.intermediateHash[5]   = 0x68581511;
		this.intermediateHash[6]   = 0x64f98fa7;
		this.intermediateHash[7]   = 0xbefa4fa4;
	}

	public void calc(byte buff[], int ofst, int buffSize)
	{
		this.messageLength += (buffSize << 3);
		if ((buffSize + this.messageBlockIndex) < 64)
		{
			ByteTool.copyArray(this.messageBlock, this.messageBlockIndex, buff, ofst, buffSize);
			this.messageBlockIndex += buffSize;
			return;
		}
		
		if (this.messageBlockIndex > 0)
		{
			ByteTool.copyArray(this.messageBlock, this.messageBlockIndex, buff, ofst, 64 - this.messageBlockIndex);
			SHA256.calcBlock(this.intermediateHash, this.messageBlock, 0);
			ofst += 64 - this.messageBlockIndex;
			buffSize -= 64 - this.messageBlockIndex;
			this.messageBlockIndex = 0;
		}
	
		while (buffSize >= 64)
		{
			SHA256.calcBlock(this.intermediateHash, buff, ofst);
			ofst += 64;
			buffSize -= 64;
		}
		if (buffSize > 0)
		{
			ByteTool.copyArray(this.messageBlock, 0, buff, ofst, this.messageBlockIndex = buffSize);
		}
	}

	public byte []getValue()
	{
		byte calBuff[] = new byte[64];
		int intHash[] = this.intermediateHash.clone();
	
		int i;
		if (this.messageBlockIndex < 55)
		{
			ByteTool.copyArray(calBuff, 0, this.messageBlock, 0, messageBlockIndex);
			i = messageBlockIndex;
			calBuff[i++] = (byte)0x80;
			while (i < 56)
			{
				calBuff[i++] = 0;
			}
		}
		else
		{
			ByteTool.copyArray(calBuff, 0, this.messageBlock, 0, messageBlockIndex);
			i = messageBlockIndex;
			calBuff[i++] = (byte)0x80;
			while (i < 64)
			{
				calBuff[i++] = 0;
			}
			SHA256.calcBlock(this.intermediateHash, calBuff, 0);
	
			ByteTool.clearArray(calBuff, 0, 56);
		}
	
		ByteTool.writeMInt64(calBuff, 56, this.messageLength);
		SHA256.calcBlock(this.intermediateHash, calBuff, 0);
		byte retBuff[] = new byte[28];
		i = 7;
		while (i > 0)
		{
			i--;
			ByteTool.writeMInt32(retBuff, i * 4, this.intermediateHash[i]);
		}
		this.intermediateHash = intHash;
		return retBuff;
	}

	public int getBlockSize()
	{
		return 64;
	}

	public int getResultSize()
	{
		return 28;
	}	
}
