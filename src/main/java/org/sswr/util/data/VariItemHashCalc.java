package org.sswr.util.data;

import java.nio.charset.StandardCharsets;

import org.sswr.util.crypto.hash.CRC32;
import org.sswr.util.crypto.hash.CRC32R;

import jakarta.annotation.Nullable;

public class VariItemHashCalc
{
	private CRC32R crc;
	public VariItemHashCalc()
	{
		this.crc = new CRC32R(CRC32.getPolynormialCastagnoli());
	}

	public long hash(@Nullable Object item)
	{
		if (item == null)
		{
			return 0;
		}
		if (item instanceof String)
		{
			byte[] b = ((String)item).getBytes(StandardCharsets.UTF_8);
			return crc.calcDirect(b);
		}
		System.out.println("VariItemHashCalc.hash: Unsupported type: "+item.getClass().toString());
		return 0;
	}
}
