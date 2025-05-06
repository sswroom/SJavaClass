package org.sswr.util.crypto.cert;

import org.sswr.util.net.ASN1Data;

import jakarta.annotation.Nonnull;

public class MyX509PKCS12 extends MyX509File
{
	public MyX509PKCS12(@Nonnull String sourceName, @Nonnull byte[] buff, int ofst, int size)
	{
		super(sourceName, buff, ofst, size);
	}

	@Override
	@Nonnull
	public FileType getFileType()
	{
		return FileType.PKCS12;
	}

	@Override
	@Nonnull
	public ASN1Data clone()
	{
		return new MyX509PKCS12(this.sourceName, this.buff, 0, this.buff.length);
	}

	@Override
	@Nonnull
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		if (isPFX(this.buff, 0, this.buff.length, "1"))
		{
			appendPFX(this.buff, 0, this.buff.length, "1", sb, null);
		}
		return sb.toString();
	}
}
