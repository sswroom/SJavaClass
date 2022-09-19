package org.sswr.util.crypto;

import org.sswr.util.net.ASN1Data;

public class MyX509PKCS12 extends MyX509File
{
	public MyX509PKCS12(String sourceName, byte[] buff, int ofst, int size)
	{
		super(sourceName, buff, ofst, size);
	}

	@Override
	public FileType getFileType()
	{
		return FileType.PKCS12;
	}

	@Override
	public ASN1Data clone()
	{
		return new MyX509PKCS12(this.sourceName, this.buff, 0, this.buff.length);
	}

	@Override
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
