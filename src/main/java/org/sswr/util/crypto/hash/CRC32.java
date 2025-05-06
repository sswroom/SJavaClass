package org.sswr.util.crypto.hash;

public class CRC32
{
	public static int getPolynormialIEEE()
	{
		return 0x04C11DB7;
	}
	
	public static int getPolynormialCastagnoli()
	{
		return 0x1EDC6F41;
	}
		
}
