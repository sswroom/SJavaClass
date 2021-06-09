package org.sswr.util.data.textbinenc;

import java.nio.charset.Charset;

public class CharsetTextBinEnc extends TextBinEnc
{
	private Charset cs;

	public CharsetTextBinEnc(Charset cs)
	{
		if (cs == null)
		{
			this.cs = Charset.defaultCharset();
		}
		else
		{
			this.cs = cs;
		}
	}

	public String encodeBin(byte []dataBuff, int dataOfst, int buffSize)
	{
		return new String(dataBuff, dataOfst, buffSize, this.cs);
	}

	public byte []decodeBin(String s)
	{
		return s.getBytes(this.cs);
	}

	public String getName()
	{
		return "Charset Encoding";
	}	
}
