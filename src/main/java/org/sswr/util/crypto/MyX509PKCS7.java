package org.sswr.util.crypto;

import org.sswr.util.net.ASN1Data;

public class MyX509PKCS7 extends MyX509File
{
	public MyX509PKCS7(String sourceName, byte[] buff, int ofst, int size)
	{
		super(sourceName, buff, ofst, size);
	}

	@Override
	public FileType getFileType()
	{
		return FileType.PKCS7;
	}

	@Override
	public ASN1Data clone()
	{
		return new MyX509PKCS7(this.sourceName, this.buff, 0, this.buff.length);
	}

	@Override
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
