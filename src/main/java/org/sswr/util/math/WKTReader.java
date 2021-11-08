package org.sswr.util.math;

import org.sswr.util.data.StringUtil;

public class WKTReader
{
	private String lastError;
	private int srid;

	public WKTReader(int srid)
	{
		this.srid = srid;
		this.lastError = null;
	}

	public Vector2D parseWKT(byte[] wkt)
	{
		if (StringUtil.startsWith(wkt, 0, "POINT("))
		{
			return null;
		}
		else if (StringUtil.startsWith(wkt, 0, "POLYLINE("))
		{
			return null;
		}
		else if (StringUtil.startsWith(wkt, 0, "POLYGON("))
		{
			return null;
		}
		return null;
	}

	public String getLastError()
	{
		return this.lastError;
	}
}
