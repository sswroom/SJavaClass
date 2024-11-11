package org.sswr.util.crypto;

import org.sswr.util.data.ByteTool;

import jakarta.annotation.Nonnull;

public final class SHA256 extends Hash
{
	private int intermediateHash[];
	private byte messageBlock[];
	private long messageLength;
	private int messageBlockIndex;

	static void calcBlock(@Nonnull int intermediateHash[], @Nonnull byte messageBlock[], int ofst)
	{
		int w[] = new int[64];
		int k[] =    {
			0x428a2f98, 0x71374491, 0xb5c0fbcf, 0xe9b5dba5, 0x3956c25b, 0x59f111f1, 0x923f82a4, 0xab1c5ed5,
			0xd807aa98, 0x12835b01, 0x243185be, 0x550c7dc3, 0x72be5d74, 0x80deb1fe, 0x9bdc06a7, 0xc19bf174,
			0xe49b69c1, 0xefbe4786, 0x0fc19dc6, 0x240ca1cc, 0x2de92c6f, 0x4a7484aa, 0x5cb0a9dc, 0x76f988da,
			0x983e5152, 0xa831c66d, 0xb00327c8, 0xbf597fc7, 0xc6e00bf3, 0xd5a79147, 0x06ca6351, 0x14292967,
			0x27b70a85, 0x2e1b2138, 0x4d2c6dfc, 0x53380d13, 0x650a7354, 0x766a0abb, 0x81c2c92e, 0x92722c85,
			0xa2bfe8a1, 0xa81a664b, 0xc24b8b70, 0xc76c51a3, 0xd192e819, 0xd6990624, 0xf40e3585, 0x106aa070,
			0x19a4c116, 0x1e376c08, 0x2748774c, 0x34b0bcb5, 0x391c0cb3, 0x4ed8aa4a, 0x5b9cca4f, 0x682e6ff3,
			0x748f82ee, 0x78a5636f, 0x84c87814, 0x8cc70208, 0x90befffa, 0xa4506ceb, 0xbef9a3f7, 0xc67178f2};
		int t;
		int s0;
		int s1;
		int ch;
		int maj;
		int temp1, temp2;
		int a, b, c, d, e, f, g, h;
	
		t = 0;
		while (t < 16)
		{
			w[t] = ByteTool.readMInt32(messageBlock, ofst + t * 4);
			t++;
		}
	
		while(t < 64)
		{
			s0 = ByteTool.ror32(w[t - 15], 7) ^ ByteTool.ror32(w[t - 15], 18) ^ ByteTool.shr32(w[t - 15], 3);
			s1 = ByteTool.ror32(w[t - 2], 17) ^ ByteTool.ror32(w[t - 2],  19) ^ ByteTool.shr32(w[t - 2], 10);
			w[t] = w[t - 16] + s0 + w[t - 7] + s1;
			t++;
		}
	
		a = intermediateHash[0];
		b = intermediateHash[1];
		c = intermediateHash[2];
		d = intermediateHash[3];
		e = intermediateHash[4];
		f = intermediateHash[5];
		g = intermediateHash[6];
		h = intermediateHash[7];
	
		t = 0;
		while (t < 64)
		{
			s1 = ByteTool.ror32(e, 6) ^ ByteTool.ror32(e, 11) ^ ByteTool.ror32(e, 25);
			ch = (e & f) ^ ((~e) & g);
			temp1 = h + s1 + ch + k[t] + w[t];
			s0 = ByteTool.ror32(a, 2) ^ ByteTool.ror32(a, 13) ^ ByteTool.ror32(a, 22);
			maj = (a & b) ^ (a & c) ^ (b & c);
			temp2 = s0 + maj;
	
			h = g;
			g = f;
			f = e;
			e = d + temp1;
			d = c;
			c = b;
			b = a;
			a = temp1 + temp2;
	
			t++;
		}
	
		intermediateHash[0] += a;
		intermediateHash[1] += b;
		intermediateHash[2] += c;
		intermediateHash[3] += d;
		intermediateHash[4] += e;
		intermediateHash[5] += f;
		intermediateHash[6] += g;
		intermediateHash[7] += h;
	}

	public SHA256()
	{
		this.intermediateHash = new int[8];
		this.messageBlock = new byte[64];
		this.clear();
	}

	@Nonnull
	public String getName()
	{
		return "SHA-256";
	}

	@Nonnull
	public Hash clone()
	{
		SHA256 sha256 = new SHA256();
		sha256.messageLength = this.messageLength;
		sha256.messageBlockIndex = this.messageBlockIndex;
		sha256.messageBlock = this.messageBlock.clone();
		sha256.intermediateHash = this.intermediateHash.clone();
		return sha256;
	}

	public void clear()
	{
		this.messageLength         = 0;
		this.messageBlockIndex     = 0;
		
		this.intermediateHash[0]   = 0x6a09e667;
		this.intermediateHash[1]   = 0xbb67ae85;
		this.intermediateHash[2]   = 0x3c6ef372;
		this.intermediateHash[3]   = 0xa54ff53a;
		this.intermediateHash[4]   = 0x510e527f;
		this.intermediateHash[5]   = 0x9b05688c;
		this.intermediateHash[6]   = 0x1f83d9ab;
		this.intermediateHash[7]   = 0x5be0cd19;
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
		byte retBuff[] = new byte[32];
		i = 8;
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
		return 32;
	}
}
