package org.sswr.util.crypto;

import org.sswr.util.net.ASN1Data;

import jakarta.annotation.Nonnull;

public class MyX509PKCS7 extends MyX509File
{
	public MyX509PKCS7(@Nonnull String sourceName, @Nonnull byte[] buff, int ofst, int size)
	{
		super(sourceName, buff, ofst, size);
	}

	@Override
	@Nonnull
	public FileType getFileType()
	{
		return FileType.PKCS7;
	}

	@Override
	@Nonnull
	public ASN1Data clone()
	{
		return new MyX509PKCS7(this.sourceName, this.buff, 0, this.buff.length);
	}

	@Override
	@Nonnull
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		if (isContentInfo(this.buff, 0, this.buff.length, "1"))
		{
			appendContentInfo(this.buff, 0, this.buff.length, "1", sb, null, ContentDataType.Unknown);
		}
		return sb.toString();
	}	
}
