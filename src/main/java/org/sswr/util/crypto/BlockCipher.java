package org.sswr.util.crypto;

import org.sswr.util.data.ByteTool;

import jakarta.annotation.Nonnull;

public abstract class BlockCipher extends Encryption
{
	public enum ChainMode
	{
		ECB,
		CBC,
		PCBC,
		CFB,
		OFB
	};

	protected byte []iv;
	protected int blockSize;
	protected ChainMode cm;

	public BlockCipher(int blockSize)
	{
		this.blockSize = blockSize;
		this.cm = ChainMode.ECB;
		this.iv = new byte[blockSize];
		ByteTool.clearArray(this.iv, 0, blockSize);
	}

	@Nonnull
	public byte []encrypt(@Nonnull byte []inBuff, int inOfst, int inSize) throws EncryptionException
	{
		byte outBuff[];
		byte blk[];
		int outOfst;
		int blkCnt = inSize / this.blockSize;
		if (inSize > blkCnt * this.blockSize)
		{
			blkCnt++;
		}
		outBuff = new byte[blkCnt * this.blockSize];
		outOfst = 0;
		switch (this.cm)
		{
		case ECB:
			while (inSize >= this.blockSize)
			{
				this.encryptBlock(inBuff, inOfst, outBuff, outOfst);
				inOfst += this.blockSize;
				outOfst += this.blockSize;
				inSize -= this.blockSize;
			}
			if (inSize > 0)
			{
				blk = new byte[this.blockSize];
				ByteTool.clearArray(blk, 0, this.blockSize);
				ByteTool.copyArray(blk, 0, inBuff, inOfst, inSize);
				this.encryptBlock(blk, 0, outBuff, outOfst);
				outOfst += this.blockSize;
			}
			return outBuff;
		case CBC:
			blk = new byte[this.blockSize];
			ByteTool.copyArray(blk, 0, this.iv, 0, this.blockSize);
			while (inSize >= blockSize)
			{
				ByteTool.arrayXOR(blk, 0, inBuff, inOfst, blk, 0, this.blockSize);
				this.encryptBlock(blk, 0, outBuff, outOfst);
				ByteTool.copyArray(blk, 0, outBuff, outOfst, this.blockSize);
				inOfst += this.blockSize;
				outOfst += this.blockSize;
				inSize = inSize - this.blockSize;
			}
			if (inSize > 0)
			{
				ByteTool.arrayXOR(blk, 0, inBuff, inOfst, blk, 0, inSize);
				this.encryptBlock(blk, 0, outBuff, outOfst);
				outOfst += this.blockSize;
			}
			return outBuff;
		case PCBC:
			blk = new byte[this.blockSize];
			ByteTool.copyArray(blk, 0, this.iv, 0, this.blockSize);
			while (inSize >= blockSize)
			{
				ByteTool.arrayXOR(blk, 0, inBuff, inOfst, blk, 0, this.blockSize);
				this.encryptBlock(inBuff, inOfst, outBuff, outOfst);
				ByteTool.arrayXOR(inBuff, inOfst, outBuff, outOfst, blk, 0, this.blockSize);
				inOfst += this.blockSize;
				outOfst += this.blockSize;
				inSize = inSize - this.blockSize;
			}
			if (inSize > 0)
			{
				ByteTool.arrayXOR(blk, 0, inBuff, inOfst, blk, 0, inSize);
				this.encryptBlock(inBuff, inOfst, outBuff, outOfst);
				outOfst += this.blockSize;
			}
			return outBuff;
		case CFB:
			blk = new byte[this.blockSize];
			ByteTool.copyArray(blk, 0, this.iv, 0, this.blockSize);
			while (inSize >= blockSize)
			{
				this.encryptBlock(blk, 0, outBuff, outOfst);
				ByteTool.arrayXOR(outBuff, outOfst, inBuff, inOfst, outBuff, outOfst, this.blockSize);
				ByteTool.copyArray(blk, 0, outBuff, outOfst, this.blockSize);
				inOfst += this.blockSize;
				outOfst += this.blockSize;
				inSize = inSize - this.blockSize;
			}
			if (inSize > 0)
			{
				this.encryptBlock(blk, 0, outBuff, outOfst);
				ByteTool.arrayXOR(outBuff, outOfst, inBuff, inOfst, outBuff, outOfst, inSize);
				outOfst += this.blockSize;
			}
			return outBuff;
		case OFB:
			blk = new byte[this.blockSize];
			ByteTool.copyArray(blk, 0, this.iv, 0, this.blockSize);
			while (inSize >= blockSize)
			{
				this.encryptBlock(blk, 0, outBuff, outOfst);
				ByteTool.copyArray(blk, 0, outBuff, outOfst, this.blockSize);
				ByteTool.arrayXOR(outBuff, outOfst, inBuff, inOfst, outBuff, outOfst, this.blockSize);
				inOfst += this.blockSize;
				outOfst += this.blockSize;
				inSize = inSize - this.blockSize;
			}
			if (inSize > 0)
			{
				this.encryptBlock(blk, 0, outBuff, outOfst);
				ByteTool.arrayXOR(outBuff, outOfst, inBuff, inOfst, outBuff, outOfst, inSize);
				outOfst += this.blockSize;
			}
			return outBuff;
		default:
			throw new EncryptionException("Unsupported Chain Mode");
		}
	}

	@Nonnull
	public byte []decrypt(@Nonnull byte []inBuff, int inOfst, int inSize) throws EncryptionException
	{
		byte outBuff[];
		byte blk[];
		byte blk2[];
		int outOfst;
		int blkCnt = inSize / this.blockSize;
		if (inSize > blkCnt * this.blockSize)
		{
			blkCnt++;
		}
		outBuff = new byte[blkCnt * this.blockSize];
		outOfst = 0;
		switch (this.cm)
		{
		case ECB:
			while (inSize >= this.blockSize)
			{
				this.decryptBlock(inBuff, inOfst, outBuff, outOfst);
				inOfst += this.blockSize;
				outOfst += this.blockSize;
				inSize = inSize - this.blockSize;
			}
			if (inSize > 0)
			{
				blk = new byte[this.blockSize];
				ByteTool.clearArray(blk, 0, this.blockSize);
				ByteTool.copyArray(blk, 0, inBuff, inOfst, inSize);
				this.decryptBlock(blk, 0, outBuff, outOfst);
				outOfst += this.blockSize;
			}
			return outBuff;
		case CBC:
			blk = new byte[this.blockSize];
			ByteTool.copyArray(blk, 0, this.iv, 0, this.blockSize);
			while (inSize >= this.blockSize)
			{
				this.decryptBlock(inBuff, inOfst, outBuff, outOfst);
				ByteTool.arrayXOR(outBuff, outOfst, blk, 0, outBuff, outOfst, this.blockSize);
				ByteTool.copyArray(blk, 0, inBuff, inOfst, this.blockSize);
				inOfst += this.blockSize;
				outOfst += this.blockSize;
				inSize = inSize - this.blockSize;
			}
			if (inSize > 0)
			{
				blk2 = new byte[this.blockSize];
				ByteTool.clearArray(blk2, 0, this.blockSize);
				ByteTool.copyArray(blk2, 0, inBuff, inOfst, inSize);
				this.decryptBlock(blk2, 0, outBuff, outOfst);
				ByteTool.arrayXOR(outBuff, outOfst, blk, 0, outBuff, outOfst, this.blockSize);
				ByteTool.copyArray(blk, 0, inBuff, inOfst, this.blockSize);
				outOfst += this.blockSize;
			}
			return outBuff;
		case PCBC:
			blk = new byte[this.blockSize];
			ByteTool.copyArray(blk, 0, this.iv, 0, this.blockSize);
			while (inSize >= this.blockSize)
			{
				this.decryptBlock(inBuff, inOfst, outBuff, outOfst);
				ByteTool.arrayXOR(outBuff, outOfst, blk, 0, outBuff, outOfst, this.blockSize);
				ByteTool.arrayXOR(inBuff, inOfst, outBuff, outOfst, blk, 0, this.blockSize);
				inOfst += this.blockSize;
				outOfst += this.blockSize;
				inSize = inSize - this.blockSize;
			}
			if (inSize > 0)
			{
				blk2 = new byte[this.blockSize];
				ByteTool.clearArray(blk2, 0, this.blockSize);
				ByteTool.copyArray(blk2, 0, inBuff, inOfst, inSize);
				this.decryptBlock(blk2, 0, outBuff, outOfst);
				ByteTool.arrayXOR(outBuff, outOfst, blk, 0, outBuff, outOfst, this.blockSize);
				ByteTool.arrayXOR(inBuff, inOfst, outBuff, outOfst, blk, 0, this.blockSize);
				outOfst += this.blockSize;
			}
			return outBuff;
		case CFB:
			blk = new byte[this.blockSize];
			ByteTool.copyArray(blk, 0, this.iv, 0, this.blockSize);
			while (inSize >= this.blockSize)
			{
				this.encryptBlock(blk, 0, outBuff, outOfst);
				ByteTool.arrayXOR(outBuff, outOfst, inBuff, inOfst, outBuff, outOfst, this.blockSize);
				ByteTool.copyArray(blk, 0, inBuff, inOfst, this.blockSize);
				inOfst += this.blockSize;
				outOfst += this.blockSize;
				inSize = inSize - this.blockSize;
			}
			return outBuff;
		case OFB:
			blk = new byte[this.blockSize];
			ByteTool.copyArray(blk, 0, this.iv, 0, this.blockSize);
			while (inSize >= this.blockSize)
			{
				this.encryptBlock(blk, 0, outBuff, outOfst);
				ByteTool.copyArray(blk, 0, outBuff, outOfst, this.blockSize);
				ByteTool.arrayXOR(outBuff, outOfst, inBuff, inOfst, outBuff, outOfst, this.blockSize);
				inOfst += this.blockSize;
				outOfst += this.blockSize;
				inSize = inSize - this.blockSize;
			}
			return outBuff;
		default:
			throw new EncryptionException("Unsupported Chain Mode");
		}
	}

	public int getEncBlockSize()
	{
		return this.blockSize;
	}

	public int getDecBlockSize()
	{
		return this.blockSize;
	}

	public abstract int encryptBlock(@Nonnull byte []inBlock, int inOfst, @Nonnull byte []outBlock, int outOfst);
	public abstract int decryptBlock(@Nonnull byte []inBlock, int inOfst, @Nonnull byte []outBlock, int outOfst);

	public void setChainMode(@Nonnull ChainMode cm)
	{
		this.cm = cm;
	}

	public void setIV(@Nonnull byte []iv)
	{
		ByteTool.copyArray(this.iv, 0, iv, 0, this.blockSize);
	}
}
