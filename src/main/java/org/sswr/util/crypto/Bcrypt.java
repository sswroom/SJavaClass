package org.sswr.util.crypto;

import org.sswr.util.crypto.BlockCipher.ChainMode;
import org.sswr.util.data.RandomBytesGenerator;
import org.sswr.util.data.StringUtil;
import org.sswr.util.data.textbinenc.Radix64Enc;

public class Bcrypt
{
	private Radix64Enc radix64;
	private byte[] calcHash(int cost, byte[] salt, String password)
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

	public boolean isMatch(String hash, String password)
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
			byte[] myCTxt = this.calcHash(count, salt, password);
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

	public String genHash(int cost, String password)
	{
		if (cost < 4 || cost > 31)
		{
			return null;
		}
		RandomBytesGenerator rand = new RandomBytesGenerator();
		return this.genHash(cost, rand.nextBytes(16), password);
	}

	public String genHash(int cost, byte[] salt, String password)
	{
		if (salt == null || salt.length != 16)
		{
			return null;
		}
		if (cost < 4 || cost > 31)
		{
			return null;
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
		byte[] hashCTxt = this.calcHash(cost, salt, password);
		sb.append(this.radix64.encodeBin(hashCTxt, 0, 23));
		return sb.toString();
	}
}
