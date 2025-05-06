package org.sswr.util.crypto.cert;

import org.sswr.util.net.ASN1Data;

import jakarta.annotation.Nonnull;

public class MyX509CertReq extends MyX509File
{
	public MyX509CertReq(String sourceName, byte[] buff, int ofst, int size)
	{
		super(sourceName, buff, ofst, size);
	}

	@Override
	@Nonnull
	public FileType getFileType()
	{
		return FileType.CertRequest;
	}

	@Override
	@Nonnull
	public ASN1Data clone()
	{
		return new MyX509CertReq(this.sourceName, this.buff, 0, this.buff.length);
	}

	@Override
	@Nonnull
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
