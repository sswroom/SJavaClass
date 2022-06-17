package org.sswr.util.crypto;

public class MyX509Cert extends MyX509File
{
	public MyX509Cert(String sourceName, byte[] buff, int ofst, int size)
	{
		super(sourceName, buff, ofst, size);
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
