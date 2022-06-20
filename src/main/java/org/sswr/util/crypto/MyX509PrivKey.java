package org.sswr.util.crypto;

public class MyX509PrivKey extends MyX509File
{
	public MyX509PrivKey(String sourceName, byte[] buff, int ofst, int size)
	{
		super(sourceName, buff, ofst, size);
	}

	@Override
	public FileType getFileType()
	{
		return FileType.PrivateKey;
	}

	@Override
	public ASN1Data clone()
	{
		return new MyX509PrivKey(this.sourceName, this.buff, 0, this.buff.length);
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		if (isPrivateKeyInfo(this.buff, 0, this.buff.length, "1"))
		{
			appendPrivateKeyInfo(this.buff, 0, this.buff.length, "1", sb);
		}
		return sb.toString();
	}
}
