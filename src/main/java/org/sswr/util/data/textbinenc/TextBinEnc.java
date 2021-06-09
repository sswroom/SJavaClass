package org.sswr.util.data.textbinenc;

public abstract class TextBinEnc
{
	public abstract String encodeBin(byte []dataBuff, int dataOfst, int buffSize);
	public abstract byte []decodeBin(String s);
	public abstract String getName();

	public String toString()
	{
		return getName();
	}
}
