package org.sswr.util.math;

import java.nio.charset.StandardCharsets;

import org.sswr.util.data.SharedDouble;
import org.sswr.util.data.StringUtil;
import org.sswr.util.math.geometry.Point2D;
import org.sswr.util.math.geometry.PointZ;
import org.sswr.util.math.geometry.Vector2D;

public class WKTReader
{
	private String lastError;
	private int srid;

	private int nextDouble(byte[] wkt, int ofst, SharedDouble val)
	{
		int startOfst = ofst;
		byte c;
		while (true)
		{
			if (ofst >= wkt.length)
			{
				break;
			}
			c = wkt[ofst];
			if (c == ' ')
			{
				break;
			}
			else if (c == ',')
			{
				break;
			}
			else if (c == ')')
			{
				break;
			}
			ofst++;
		}
		int len = (ofst - startOfst);
		if (len > 100 || len == 0)
		{
			return -1;
		}
		String s = new String(wkt, startOfst, ofst - startOfst, StandardCharsets.UTF_8);
		Double d = StringUtil.toDouble(s);
		if (d != null)
		{
			val.value = d.doubleValue();
			return ofst;
		}
		return -1;
	}
	
	public WKTReader(int srid)
	{
		this.srid = srid;
		this.lastError = null;
	}

	public Vector2D parseWKT(byte[] wkt)
	{
		if (StringUtil.startsWith(wkt, 0, "POINT("))
		{
			int ofst = 0;
			SharedDouble x = new SharedDouble();
			SharedDouble y = new SharedDouble();
			SharedDouble z = new SharedDouble();
			ofst += 5;
			while (wkt[ofst] == ' ')
			{
				ofst++;
			}
			if (wkt[ofst] != '(')
			{
				return null;
			}
			ofst++;
			ofst = nextDouble(wkt, ofst, x);
			if (ofst < 0 || wkt[ofst] != ' ')
			{
				return null;
			}
			while (wkt[++ofst] == ' ');
			ofst = nextDouble(wkt, ofst, y);
			if (ofst < 0)
			{
				return null;
			}
			if (wkt[ofst] == ')' && ofst + 1 >= wkt.length)
			{
				return new Point2D(this.srid, x.value, y.value);
			}
			else if (wkt[ofst] != ' ')
			{
				return null;
			}
			while (wkt[++ofst] == ' ');
			ofst = nextDouble(wkt, ofst, z);
			if (ofst < 0)
			{
				return null;
			}
			if (wkt[ofst] == ')' && ofst + 1 >= wkt.length)
			{
				return new PointZ(this.srid, x.value, y.value, z.value);
			}
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
