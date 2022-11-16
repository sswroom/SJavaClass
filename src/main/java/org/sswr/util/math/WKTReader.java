package org.sswr.util.math;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import org.sswr.util.data.ByteTool;
import org.sswr.util.data.SharedDouble;
import org.sswr.util.data.StringUtil;
import org.sswr.util.math.geometry.LineString;
import org.sswr.util.math.geometry.MultiPolygon;
import org.sswr.util.math.geometry.Point2D;
import org.sswr.util.math.geometry.PointZ;
import org.sswr.util.math.geometry.Polygon;
import org.sswr.util.math.geometry.Polyline;
import org.sswr.util.math.geometry.Vector2D;

public class WKTReader
{
	private String lastError;
	private int srid;

	private int nextDouble(byte[] wkt, int ofst, SharedDouble val)
	{
		if (ofst + 3 <= wkt.length)
		{
			if ((wkt[ofst + 0] == 'i' || wkt[ofst + 0] == 'I') &&
				(wkt[ofst + 1] == 'n' || wkt[ofst + 1] == 'N') &&
				(wkt[ofst + 2] == 'f' || wkt[ofst + 2] == 'F'))
			{
				val.value = Double.POSITIVE_INFINITY;
				return ofst + 3;
			}
			if ((wkt[ofst + 0] == 'n' || wkt[ofst + 0] == 'N') &&
				(wkt[ofst + 1] == 'a' || wkt[ofst + 1] == 'A') &&
				(wkt[ofst + 2] == 'n' || wkt[ofst + 2] == 'N'))
			{
				val.value = Double.NaN;
				return ofst + 3;
			}
		}
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
		else if (StringUtil.startsWith(wkt, 0, "LINESTRING"))
		{
			int ofst = 0;
			ArrayList<Double> ptList = new ArrayList<Double>();
			ArrayList<Double> zList = new ArrayList<Double>();
			SharedDouble x = new SharedDouble();
			SharedDouble y = new SharedDouble();
			SharedDouble z = new SharedDouble();
			ofst += 10;
			while (wkt[ofst] == ' ')
			{
				ofst++;
			}
			if (wkt[ofst] != '(')
			{
				return null;
			}
			while (wkt[++ofst] == ' ');
			while (true)
			{
				ofst = nextDouble(wkt, ofst, x);
				if (ofst == -1 || wkt[ofst] != ' ')
				{
					return null;
				}
				while (wkt[++ofst] == ' ');
				ofst = nextDouble(wkt, ofst, y);
				if (ofst == -1)
				{
					return null;
				}
				while (wkt[ofst] == ' ')
				{
					while (wkt[++ofst] == ' ');
					ofst = nextDouble(wkt, ofst, z);
					if (ofst == -1)
					{
						return null;
					}
					zList.add(z.value);
				}
				ptList.add(x.value);
				ptList.add(y.value);
				if (wkt[ofst] == ')')
				{
					ofst++;
					break;
				}
				else if (wkt[ofst] == ',')
				{
					ofst++;
					continue;
				}
				else
				{
					return null;
				}
			}
			LineString pl;
			boolean hasM = false;
			boolean hasZ = false;
			if (zList.size() == ptList.size())
			{
				hasM = true;
				hasZ = true;
			}
			else if (zList.size() == (ptList.size() >> 1))
			{
				hasZ = true;
			}
			pl = new LineString(this.srid, ptList.size() >> 1, hasZ, hasM);
			int i;
			Coord2DDbl []ptArr = pl.getPointList();
			ByteTool.copyArray(ptArr, 0, ptList, 0, ptList.size() >> 1);
			if (hasM)
			{
				double []zArr = pl.getZList();
				double []mArr = pl.getMList();
				i = zArr.length;
				while (i-- > 0)
				{
					zArr[i] = zList.get(i << 1);
					mArr[i] = zList.get((i << 1) + 1);
				}
			}
			else if (hasZ)
			{
				double []zArr = pl.getZList();
				i = zArr.length;
				while (i-- > 0)
				{
					zArr[i] = zList.get(i);
				}
			}
			return pl;
		}
		else if (StringUtil.startsWith(wkt, 0, "POLYGON"))
		{
			int ofst = 0;
			ArrayList<Double> ptList = new ArrayList<Double>();
			ArrayList<Double> zList = new ArrayList<Double>();
			ArrayList<Integer> ptOfstList = new ArrayList<Integer>();
			SharedDouble x = new SharedDouble();
			SharedDouble y = new SharedDouble();
			SharedDouble z = new SharedDouble();
			ofst += 7;
			while (wkt[ofst] == ' ')
			{
				ofst++;
			}
			if (wkt[ofst] != '(')
			{
				return null;
			}
			while (wkt[++ofst] == ' ');
			while (true)
			{
				if (wkt[ofst] != '(')
				{
					return null;
				}
				ofst++;
				ptOfstList.add((ptList.size() >> 1));
				while (true)
				{
					ofst = nextDouble(wkt, ofst, x);
					if (ofst == -1 || wkt[ofst] != ' ')
					{
						return null;
					}
					while (wkt[++ofst] == ' ');
					ofst = nextDouble(wkt, ofst, y);
					if (ofst == -1)
					{
						return null;
					}
					while (wkt[ofst] == ' ')
					{
						while (wkt[++ofst] == ' ');
						ofst = nextDouble(wkt, ofst, z);
						if (ofst == -1)
						{
							return null;
						}
						zList.add(z.value);
					}
					ptList.add(x.value);
					ptList.add(y.value);
					if (wkt[ofst] == ')')
					{
						ofst++;
						break;
					}
					else if (wkt[ofst] == ',')
					{
						ofst++;
						continue;
					}
					else
					{
						return null;
					}
				}
				if (wkt[ofst] == ',')
				{
					ofst++;
					continue;
				}
				else if (wkt[ofst] == ')')
				{
					ofst++;
					break;
				}
				else
				{
					return null;
				}
			}
			if (ofst != wkt.length)
			{
				return null;
			}
			Polygon pg;
			pg = new Polygon(this.srid, ptOfstList.size(), ptList.size() >> 1, zList.size() == (ptList.size() >> 1), false);
			int []ptOfstArr = pg.getPtOfstList();
			ByteTool.copyArray(ptOfstArr, 0, ptOfstList, 0, ptOfstList.size());
			Coord2DDbl []ptArr = pg.getPointList();
			ByteTool.copyArray(ptArr, 0, ptList, 0, ptList.size() >> 1);
			if (pg.hasZ())
			{
				double []zArr = pg.getZList();
				int i = zArr.length;
				while (i-- > 0)
				{
					zArr[i] = zList.get(i);
				}
			}
			return pg;
		}
		else if (StringUtil.startsWith(wkt, 0, "MULTILINESTRING"))
		{
			int ofst = 0;
			ArrayList<Double> ptList = new ArrayList<Double>();
			ArrayList<Double> zList = new ArrayList<Double>();
			ArrayList<Integer> ptOfstList = new ArrayList<Integer>();
			SharedDouble x = new SharedDouble();
			SharedDouble y = new SharedDouble();
			SharedDouble z = new SharedDouble();
			ofst += 15;
			while (wkt[ofst] == ' ')
			{
				ofst++;
			}
			if (wkt[ofst] != '(')
			{
				return null;
			}
			while (wkt[++ofst] == ' ');
			while (true)
			{
				if (wkt[ofst] != '(')
				{
					return null;
				}
				ofst++;
				ptOfstList.add((ptList.size() >> 1));
				while (true)
				{
					ofst = nextDouble(wkt, ofst, x);
					if (ofst == -1 || wkt[ofst] != ' ')
					{
						return null;
					}
					while (wkt[++ofst] == ' ');
					ofst = nextDouble(wkt, ofst, y);
					if (ofst == -1)
					{
						return null;
					}
					while (wkt[ofst] == ' ')
					{
						while (wkt[++ofst] == ' ');
						ofst = nextDouble(wkt, ofst, z);
						if (ofst == 0)
						{
							return null;
						}
						zList.add(z.value);
					}
					ptList.add(x.value);
					ptList.add(y.value);
					if (wkt[ofst] == ')')
					{
						ofst++;
						break;
					}
					else if (wkt[ofst] == ',')
					{
						ofst++;
						continue;
					}
					else
					{
						return null;
					}
				}
				if (wkt[ofst] == ',')
				{
					ofst++;
					continue;
				}
				else if (wkt[ofst] == ')')
				{
					ofst++;
					break;
				}
				else
				{
					return null;
				}
			}
			if (ofst != wkt.length)
			{
				return null;
			}
			Polyline pl;
			boolean hasZ = false;
			boolean hasM = false;
			if (zList.size() == ptList.size())
			{
				hasZ = true;
				hasM = true;
			}
			else if (zList.size() == (ptList.size() >> 1))
			{
				hasZ = true;
			}
			pl = new Polyline(this.srid, ptOfstList.size(), ptList.size() >> 1, hasZ, hasM);
			int i;
			int[] ptOfstArr = pl.getPtOfstList();
			ByteTool.copyArray(ptOfstArr, 0, ptOfstList, 0, ptOfstList.size());
			Coord2DDbl []ptArr = pl.getPointList();
			ByteTool.copyArray(ptArr, 0, ptList, 0, ptList.size() >> 1);
			if (hasM)
			{
				double []zArr = pl.getZList();
				double []mArr = pl.getMList();
				i = zArr.length;
				while (i-- > 0)
				{
					zArr[i] = zList.get((i << 1));
					mArr[i] = zList.get((i << 1) + 1);
				}
			}
			else if (hasZ)
			{
				double []zArr = pl.getZList();
				i = zArr.length;
				while (i-- > 0)
				{
					zArr[i] = zList.get(i);
				}
			}
			return pl;
		}
		else if (StringUtil.startsWith(wkt, 0, "MULTIPOLYGON"))
		{
			int ofst = 0;
			MultiPolygon mpg = null;
			ArrayList<Double> ptList = new ArrayList<Double>();
			ArrayList<Double> zList = new ArrayList<Double>();
			ArrayList<Integer> ptOfstList = new ArrayList<Integer>();
			SharedDouble x = new SharedDouble();
			SharedDouble y = new SharedDouble();
			SharedDouble z = new SharedDouble();
			ofst += 12;
			while (wkt[ofst] == ' ')
			{
				ofst++;
			}
			if (wkt[ofst] != '(')
			{
				return null;
			}
			while (wkt[++ofst] == ' ');
			while (true)
			{
				ptList.clear();
				zList.clear();
				ptOfstList.clear();
				if (wkt[ofst] != '(')
				{
					return null;
				}
				while (wkt[++ofst] == ' ');
				while (true)
				{
					if (wkt[ofst] != '(')
					{
						return null;
					}
					ofst++;
					ptOfstList.add((ptList.size() >> 1));
					while (true)
					{
						ofst = nextDouble(wkt, ofst, x);
						if (ofst == -1 || wkt[ofst] != ' ')
						{
							return null;
						}
						while (wkt[++ofst] == ' ');
						ofst = nextDouble(wkt, ofst, y);
						if (ofst == -1)
						{
							return null;
						}
						while (wkt[ofst] == ' ')
						{
							while (wkt[++ofst] == ' ');
							ofst = nextDouble(wkt, ofst, z);
							if (ofst == -1)
							{
								return null;
							}
							zList.add(z.value);
						}
						ptList.add(x.value);
						ptList.add(y.value);
						if (wkt[ofst] == ')')
						{
							ofst++;
							break;
						}
						else if (wkt[ofst] == ',')
						{
							ofst++;
							continue;
						}
						else
						{
							return null;
						}
					}
					if (wkt[ofst] == ',')
					{
						ofst++;
						continue;
					}
					else if (wkt[ofst] == ')')
					{
						ofst++;
						break;
					}
					else
					{
						return null;
					}
				}
				Polygon pg;
				pg = new Polygon(this.srid, ptOfstList.size(), ptList.size() >> 1, zList.size() == (ptList.size() >> 1), false);
				int i;
				int []ptOfstArr = pg.getPtOfstList();
				ByteTool.copyArray(ptOfstArr, 0, ptOfstList, 0, ptOfstList.size());
				Coord2DDbl []ptArr = pg.getPointList();
				ByteTool.copyArray(ptArr, 0, ptList, 0, ptList.size() >> 1);
				if (pg.hasZ())
				{
					double []zArr = pg.getZList();
					i = zArr.length;
					while (i-- > 0)
					{
						zArr[i] = zList.get(i);
					}
				}
				if (mpg == null)
				{
					mpg = new MultiPolygon(this.srid, pg.hasZ(), pg.hasM());
				}
				mpg.addGeometry(pg);
	
				if (wkt[ofst] == ',')
				{
					ofst++;
					continue;
				}
				else if (wkt[ofst] == ')')
				{
					ofst++;
					break;
				}
				else
				{
					return null;
				}
			}
			if (ofst != wkt.length)
			{
				return null;
			}
			return mpg;
		}
		return null;
	}

	public String getLastError()
	{
		return this.lastError;
	}
}
