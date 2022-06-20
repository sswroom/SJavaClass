package org.sswr.util.crypto;

public class MyX509CertReq extends MyX509File
{
	public MyX509CertReq(String sourceName, byte[] buff, int ofst, int size)
	{
		super(sourceName, buff, ofst, size);
	}

	@Override
	public FileType getFileType()
	{
		return FileType.CertRequest;
	}

	@Override
	public ASN1Data clone()
	{
		return new MyX509CertReq(this.sourceName, this.buff, 0, this.buff.length);
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		if (isCertificateRequest(this.buff, 0, this.buff.length, "1"))
		{
			appendCertificateRequest(this.buff, 0, this.buff.length, "1", sb);
		}
		return sb.toString();
	}
	
}
