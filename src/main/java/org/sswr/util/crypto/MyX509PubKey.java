package org.sswr.util.crypto;

public class MyX509PubKey extends MyX509File
{
	public MyX509PubKey(String sourceName, byte[] buff, int ofst, int size)
	{
		super(sourceName, buff, ofst, size);
	}

	@Override
	public FileType getFileType()
	{
		return FileType.PublicKey;
	}

	@Override
	public ASN1Data clone()
	{
		return new MyX509PubKey(this.sourceName, this.buff, 0, this.buff.length);
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		if (isPublicKeyInfo(this.buff, 0, this.buff.length, "1"))
		{
			appendPublicKeyInfo(this.buff, 0, this.buff.length, "1", sb);
		}
		return sb.toString();
	}

	public MyX509Key createKey()
	{
		ASN1Item keyTypeOID = ASN1Util.pduGetItem(this.buff, 0, this.buff.length, "1.1.1");
		ASN1Item keyData = ASN1Util.pduGetItem(this.buff, 0, this.buff.length, "1.2");
		if (keyTypeOID != null && keyData != null)
		{
			if (keyData.itemType == ASN1Util.IT_BIT_STRING)
			{
				return new MyX509Key(this.sourceName, this.buff, keyData.ofst + 1, keyData.len - 1, keyTypeFromOID(this.buff, keyTypeOID.ofst, keyTypeOID.len, true));
			}
			else
			{
				return new MyX509Key(this.sourceName, this.buff, keyData.ofst, keyData.len, keyTypeFromOID(this.buff, keyTypeOID.ofst, keyTypeOID.len, true));
			}
		}
		return null;
	}
}
