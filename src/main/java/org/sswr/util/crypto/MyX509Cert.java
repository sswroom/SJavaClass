package org.sswr.util.crypto;

public class MyX509Cert extends MyX509File
{
	public MyX509Cert(String sourceName, byte[] buff, int ofst, int size)
	{
		super(sourceName, buff, ofst, size);
	}

	public String getSubjectCN()
	{
		ASN1Item tmpBuff;
		if (ASN1Util.pduGetItemType(this.buff, 0, this.buff.length, "1.1.1") == ASN1Util.IT_CONTEXT_SPECIFIC_0)
		{
			tmpBuff = ASN1Util.pduGetItem(this.buff, 0, this.buff.length, "1.1.6");
		}
		else
		{
			tmpBuff = ASN1Util.pduGetItem(this.buff, 0, this.buff.length, "1.1.5");
		}
		if (tmpBuff != null && tmpBuff.itemType == ASN1Util.IT_SEQUENCE)
		{
			return nameGetCN(this.buff, tmpBuff.ofst, tmpBuff.ofst + tmpBuff.len);
		}
		else
		{
			return null;
		}
	}

	public String getIssuerCN()
	{
		ASN1Item tmpBuff;
		if (ASN1Util.pduGetItemType(this.buff, 0, this.buff.length, "1.1.1") == ASN1Util.IT_CONTEXT_SPECIFIC_0)
		{
			tmpBuff = ASN1Util.pduGetItem(this.buff, 0, this.buff.length, "1.1.4");
		}
		else
		{
			tmpBuff = ASN1Util.pduGetItem(this.buff, 0, this.buff.length, "1.1.3");
		}
		if (tmpBuff != null && tmpBuff.itemType == ASN1Util.IT_SEQUENCE)
		{
			return nameGetCN(this.buff, tmpBuff.ofst, tmpBuff.ofst + tmpBuff.len);
		}
		else
		{
			return null;
		}
	}

	@Override
	public FileType getFileType()
	{
		return FileType.Cert;
	}

	@Override
	public ASN1Data clone()
	{
		return new MyX509Cert(this.sourceName, this.buff, 0, this.buff.length);
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		if (isCertificate(this.buff, 0, this.buff.length, "1"))
		{
			appendCertificate(this.buff, 0, this.buff.length, "1", sb, null);
		}
		return sb.toString();
	}
}
