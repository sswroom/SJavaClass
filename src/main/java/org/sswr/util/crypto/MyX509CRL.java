package org.sswr.util.crypto;

public class MyX509CRL extends MyX509File
{
	public MyX509CRL(String sourceName, byte[] buff, int ofst, int size)
	{
		super(sourceName, buff, ofst, size);
	}

	@Override
	public FileType getFileType()
	{
		return FileType.CRL;
	}

	@Override
	public ASN1Data clone()
	{
		return new MyX509CRL(this.sourceName, this.buff, 0, this.buff.length);
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		if (isCertificateList(this.buff, 0, this.buff.length, "1"))
		{
			appendCertificateList(this.buff, 0, this.buff.length, "1", sb, null);
		}
		return sb.toString();
	}
	
}
