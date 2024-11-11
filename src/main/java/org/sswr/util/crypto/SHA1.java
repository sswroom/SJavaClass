package org.sswr.util.crypto;

import org.sswr.util.data.ByteTool;

import jakarta.annotation.Nonnull;

public final class SHA1 extends Hash
{
	private int intermediateHash[];
	private byte messageBlock[];
	private long messageLength;
	private int messageBlockIndex;

	private void calcBlock(@Nonnull int intermediateHash[], @Nonnull byte messageBlock[], int ofst)
	{
		int w[] = new int[80];
		int k[] = {
			0x5A827999,
			0x6ED9EBA1,
			0x8F1BBCDC,
			0xCA62C1D6};
		int t;
		int temp;
		int a, b, c, d, e;
	
		t = 0;
		while (t < 16)
		{
			w[t] = ByteTool.readMInt32(messageBlock, ofst + t * 4);
			t++;
		}
	
		while (t < 80)
		{
			w[t] = ByteTool.ror32(w[t - 3] ^ w[t - 8] ^ w[t - 14] ^ w[t - 16], -1);
			t++;
		}
	
		a = intermediateHash[0];
		b = intermediateHash[1];
		c = intermediateHash[2];
		d = intermediateHash[3];
		e = intermediateHash[4];
	
		t = 0;
		while (t < 20)
		{
			temp = ByteTool.ror32(a, -5) + ((b & c) | ((~b) & d)) + e + w[t] + k[0];
			e = d;
			d = c;
			c = ByteTool.ror32(b, -30);
			b = a;
			a = temp;
			t++;
		}
	
		while (t < 40)
		{
			temp = ByteTool.ror32(a, -5) + (b ^ c ^ d) + e + w[t] + k[1];
			e = d;
			d = c;
			c = ByteTool.ror32(b, -30);
			b = a;
			a = temp;
			t++;
		}
	
		while(t < 60)
		{
			temp = ByteTool.ror32(a, -5) + ((b & c) | (b & d) | (c & d)) + e + w[t] + k[2];
			e = d;
			d = c;
			c = ByteTool.ror32(b, -30);
			b = a;
			a = temp;
			t++;
		}
	
		while(t < 80)
		{
			temp = ByteTool.ror32(a, -5) + (b ^ c ^ d) + e + w[t] + k[3];
			e = d;
			d = c;
			c = ByteTool.ror32(b, -30);
			b = a;
			a = temp;
			t++;
		}
		
		intermediateHash[0] += a;
		intermediateHash[1] += b;
		intermediateHash[2] += c;
		intermediateHash[3] += d;
		intermediateHash[4] += e;
	}

	public SHA1()
	{
		this.intermediateHash = new int[5];
		this.messageBlock = new byte[64];
		this.clear();
	}

	@Nonnull
	public String getName()
	{
		return "SHA-1";
	}

	@Nonnull
	public Hash clone()
	{
		SHA1 sha1 = new SHA1();
		sha1.messageLength = this.messageLength;
		sha1.messageBlockIndex = this.messageBlockIndex;
		sha1.messageBlock = this.messageBlock.clone();
		sha1.intermediateHash = this.intermediateHash.clone();
		return sha1;
	}

	public void clear()
	{
		this.messageLength         = 0;
		this.messageBlockIndex     = 0;
		
		this.intermediateHash[0]   = 0x67452301;
		this.intermediateHash[1]   = 0xEFCDAB89;
		this.intermediateHash[2]   = 0x98BADCFE;
		this.intermediateHash[3]   = 0x10325476;
		this.intermediateHash[4]   = 0xC3D2E1F0;
	}

	public void calc(@Nonnull byte buff[], int ofst, int buffSize)
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
			calcBlock(this.intermediateHash, this.messageBlock, 0);
			ofst += 64 - this.messageBlockIndex;
			buffSize -= 64 - this.messageBlockIndex;
			this.messageBlockIndex = 0;
		}
	
		while (buffSize >= 64)
		{
			calcBlock(this.intermediateHash, buff, ofst);
			ofst += 64;
			buffSize -= 64;
		}
		if (buffSize > 0)
		{
			ByteTool.copyArray(this.messageBlock, 0, buff, ofst, this.messageBlockIndex = buffSize);
		}
	}

	@Nonnull
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
			calcBlock(this.intermediateHash, calBuff, 0);
	
			ByteTool.clearArray(calBuff, 0, 56);
		}
	
		ByteTool.writeMInt64(calBuff, 56, this.messageLength);
		calcBlock(this.intermediateHash, calBuff, 0);
		byte retBuff[] = new byte[20];
		i = 5;
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
		return 20;
	}
}
