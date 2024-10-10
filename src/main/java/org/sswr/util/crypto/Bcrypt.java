package org.sswr.util.crypto;

import org.sswr.util.crypto.BlockCipher.ChainMode;
import org.sswr.util.data.RandomBytesGenerator;
import org.sswr.util.data.StringUtil;
import org.sswr.util.data.textbinenc.Radix64Enc;

import jakarta.annotation.Nonnull;

public class Bcrypt
{
	private Radix64Enc radix64;

	@Nonnull
	private byte[] calcHash(int cost, @Nonnull byte[] salt, @Nonnull String password) throws EncryptionException
	{
		Blowfish bf = new Blowfish();
		bf.setChainMode(ChainMode.ECB);
		bf.eksBlowfishSetup(cost, salt, password);
		byte[] hashBuff = "OrpheanBeholderScryDoubt".getBytes();
		int i = 64;
		while (i-- > 0)
		{
			hashBuff = bf.encrypt(hashBuff, 0, 24);
		}
		return hashBuff;
	}

	public Bcrypt()
	{
		this.radix64 = new Radix64Enc("./ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789");
	}

	public boolean isMatch(@Nonnull String hash, @Nonnull String password)
	{
		if (hash.charAt(0) != '$')
		{
			return false;
		}
		String[] hashArr = StringUtil.split(hash.substring(1), "$");
		if (hashArr.length != 3)
		{
			return false;
		}
		if (hashArr[0].charAt(0) == '2' && hashArr[2].length() == 53)
		{
			byte[] salt = this.radix64.decodeBin(hashArr[2].substring(0, 22));
			byte[] hashCTxt = this.radix64.decodeBin(hashArr[2].substring(22));
			int count = Integer.parseInt(hashArr[1]);
			byte[] myCTxt;
			try
			{
				myCTxt = this.calcHash(count, salt, password);
			}
			catch (EncryptionException ex)
			{
				return false;
			}
			int i = hashCTxt.length;
			while (i-- > 0)
			{
				if (myCTxt[i] != hashCTxt[i])
					return false;
			}
			return true;
		}
		else
		{
			return false;
		}
	}

	@Nonnull
	public String genHash(int cost, @Nonnull String password)
	{
		if (cost < 4 || cost > 31)
		{
			throw new IllegalArgumentException("cost is out of range (4-31), value is "+cost);
		}
		RandomBytesGenerator rand = new RandomBytesGenerator();
		return this.genHash(cost, rand.nextBytes(16), password);
	}

	@Nonnull
	public String genHash(int cost, @Nonnull byte[] salt, @Nonnull String password)
	{
		if (salt.length != 16)
		{
			throw new IllegalArgumentException("salt must be 16 bytes long, current length is "+salt.length);
		}
		if (cost < 4 || cost > 31)
		{
			throw new IllegalArgumentException("cost is out of range (4-31), value is "+cost);
		}
		StringBuilder sb = new StringBuilder();
		sb.append("$2a$");
		if (cost < 10)
		{
			sb.append('0');
		}
		sb.append(cost);
		sb.append('$');
		
		sb.append(this.radix64.encodeBin(salt, 0, 16));
		byte[] hashCTxt;
		try
		{
			hashCTxt = this.calcHash(cost, salt, password);
			sb.append(this.radix64.encodeBin(hashCTxt, 0, 23));
		}
		catch (EncryptionException ex)
		{

		}
		return sb.toString();
	}
}
