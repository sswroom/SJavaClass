package org.sswr.util.crypto;

import org.sswr.util.data.ByteTool;

import jakarta.annotation.Nonnull;

public class SHA512 extends Hash
{
	private long intermediateHash[];
	private byte messageBlock[];
	private long messageLength;
	private int messageBlockIndex;

	static void calcBlock(@Nonnull long intermediateHash[], @Nonnull byte messageBlock[], int ofst)
	{
		long w[] = new long[80];
		long k[] =    {
			0x428a2f98d728ae22L, 0x7137449123ef65cdL, 0xb5c0fbcfec4d3b2fL, 0xe9b5dba58189dbbcL, 0x3956c25bf348b538L,
			0x59f111f1b605d019L, 0x923f82a4af194f9bL, 0xab1c5ed5da6d8118L, 0xd807aa98a3030242L, 0x12835b0145706fbeL, 
			0x243185be4ee4b28cL, 0x550c7dc3d5ffb4e2L, 0x72be5d74f27b896fL, 0x80deb1fe3b1696b1L, 0x9bdc06a725c71235L, 
			0xc19bf174cf692694L, 0xe49b69c19ef14ad2L, 0xefbe4786384f25e3L, 0x0fc19dc68b8cd5b5L, 0x240ca1cc77ac9c65L, 
			0x2de92c6f592b0275L, 0x4a7484aa6ea6e483L, 0x5cb0a9dcbd41fbd4L, 0x76f988da831153b5L, 0x983e5152ee66dfabL, 
			0xa831c66d2db43210L, 0xb00327c898fb213fL, 0xbf597fc7beef0ee4L, 0xc6e00bf33da88fc2L, 0xd5a79147930aa725L, 
			0x06ca6351e003826fL, 0x142929670a0e6e70L, 0x27b70a8546d22ffcL, 0x2e1b21385c26c926L, 0x4d2c6dfc5ac42aedL, 
			0x53380d139d95b3dfL, 0x650a73548baf63deL, 0x766a0abb3c77b2a8L, 0x81c2c92e47edaee6L, 0x92722c851482353bL, 
			0xa2bfe8a14cf10364L, 0xa81a664bbc423001L, 0xc24b8b70d0f89791L, 0xc76c51a30654be30L, 0xd192e819d6ef5218L, 
			0xd69906245565a910L, 0xf40e35855771202aL, 0x106aa07032bbd1b8L, 0x19a4c116b8d2d0c8L, 0x1e376c085141ab53L, 
			0x2748774cdf8eeb99L, 0x34b0bcb5e19b48a8L, 0x391c0cb3c5c95a63L, 0x4ed8aa4ae3418acbL, 0x5b9cca4f7763e373L, 
			0x682e6ff3d6b2b8a3L, 0x748f82ee5defb2fcL, 0x78a5636f43172f60L, 0x84c87814a1f0ab72L, 0x8cc702081a6439ecL, 
			0x90befffa23631e28L, 0xa4506cebde82bde9L, 0xbef9a3f7b2c67915L, 0xc67178f2e372532bL, 0xca273eceea26619cL, 
			0xd186b8c721c0c207L, 0xeada7dd6cde0eb1eL, 0xf57d4f7fee6ed178L, 0x06f067aa72176fbaL, 0x0a637dc5a2c898a6L, 
			0x113f9804bef90daeL, 0x1b710b35131c471bL, 0x28db77f523047d84L, 0x32caab7b40c72493L, 0x3c9ebe0a15c9bebcL, 
			0x431d67c49c100d4cL, 0x4cc5d4becb3e42b6L, 0x597f299cfc657e2aL, 0x5fcb6fab3ad6faecL, 0x6c44198c4a475817L};
		int t;
		long s0;
		long s1;
		long ch;
		long maj;
		long temp1, temp2;
		long a, b, c, d, e, f, g, h;
	
		t = 0;
		while (t < 16)
		{
			w[t] = ByteTool.readMInt64(messageBlock, ofst + t * 8);
			t++;
		}
	
		while(t < 80)
		{
			s0 = ByteTool.ror64(w[t - 15], 1) ^ ByteTool.ror64(w[t - 15], 8) ^ ByteTool.shr64(w[t - 15], 7);
			s1 = ByteTool.ror64(w[t - 2], 19) ^ ByteTool.ror64(w[t - 2],  61) ^ ByteTool.shr64(w[t - 2], 6);
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
		while (t < 80)
		{
			s1 = ByteTool.ror64(e, 14) ^ ByteTool.ror64(e, 18) ^ ByteTool.ror64(e, 41);
			ch = (e & f) ^ ((~e) & g);
			temp1 = h + s1 + ch + k[t] + w[t];
			s0 = ByteTool.ror64(a, 28) ^ ByteTool.ror64(a, 34) ^ ByteTool.ror64(a, 39);
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

	public SHA512()
	{
		this.intermediateHash = new long[8];
		this.messageBlock = new byte[128];
		this.clear();
	}

	@Nonnull
	public String getName()
	{
		return "SHA-512";
	}

	@Nonnull
	public Hash clone()
	{
		SHA512 sha512 = new SHA512();
		sha512.messageLength = this.messageLength;
		sha512.messageBlockIndex = this.messageBlockIndex;
		sha512.messageBlock = this.messageBlock.clone();
		sha512.intermediateHash = this.intermediateHash.clone();
		return sha512;
	}

	public void clear()
	{
		this.messageLength         = 0;
		this.messageBlockIndex     = 0;
		
		this.intermediateHash[0]   = 0x6a09e667f3bcc908L;
		this.intermediateHash[1]   = 0xbb67ae8584caa73bL;
		this.intermediateHash[2]   = 0x3c6ef372fe94f82bL;
		this.intermediateHash[3]   = 0xa54ff53a5f1d36f1L;
		this.intermediateHash[4]   = 0x510e527fade682d1L;
		this.intermediateHash[5]   = 0x9b05688c2b3e6c1fL;
		this.intermediateHash[6]   = 0x1f83d9abfb41bd6bL;
		this.intermediateHash[7]   = 0x5be0cd19137e2179L;
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
			calcBlock(this.intermediateHash, this.messageBlock, 0);
			ofst += 128 - this.messageBlockIndex;
			buffSize -= 128 - this.messageBlockIndex;
			this.messageBlockIndex = 0;
		}
	
		while (buffSize >= 128)
		{
			calcBlock(this.intermediateHash, buff, ofst);
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
			calcBlock(this.intermediateHash, calBuff, 0);
	
			ByteTool.clearArray(calBuff, 0, 120);
		}
	
		ByteTool.writeMInt64(calBuff, 120, this.messageLength);
		calcBlock(this.intermediateHash, calBuff, 0);
		byte retBuff[] = new byte[64];
		i = 8;
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
		return 64;
	}
}
