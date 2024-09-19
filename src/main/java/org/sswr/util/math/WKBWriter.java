package org.sswr.util.math;

import org.sswr.util.data.ByteTool;
import org.sswr.util.io.IOStream;
import org.sswr.util.io.MemoryStream;
import org.sswr.util.math.geometry.LinearRing;
import org.sswr.util.math.geometry.Polygon;
import org.sswr.util.math.geometry.Vector2D;

public class WKBWriter {
	private boolean isoMode;

	public WKBWriter(boolean isoMode)
	{
		this.isoMode = isoMode;
	}
	
	public byte[] write(Vector2D vec)
	{
		MemoryStream stm = new MemoryStream();
		if (write(stm, vec))
		{
			return stm.getBuff();
		}
		return null;
	}

	public boolean write(IOStream stm, Vector2D vec)
	{
		byte[] buff = new byte[64];
		int i;
		int j;
		int k;
		int nPoint;
		int geomType;
		buff[0] = 1;
		switch (vec.getVectorType())
		{
		case Polygon:
			{
				Polygon pg = (Polygon)vec;
				LinearRing lr;
				Coord2DDbl[] points;
				geomType = 3;
				j = pg.getCount();
				if (vec.hasZ())
				{
					if (this.isoMode)
					{
						geomType += 1000;
						if (vec.hasM())
						{
							geomType += 2000;
						}
					}
					else
					{
						geomType |= 0x80000000;
						if (vec.hasM())
						{
							geomType |= 0x40000000;
						}
					}
				}
				if (vec.getSRID() != 0)
				{
					geomType |= 0x20000000;
					ByteTool.writeInt32(buff, 1, geomType);
					ByteTool.writeInt32(buff, 5, vec.getSRID());
					ByteTool.writeInt32(buff, 9, j);
					stm.write(buff, 0, 13);
				}
				else
				{
					ByteTool.writeInt32(buff, 1, geomType);
					ByteTool.writeInt32(buff, 5, j);
					stm.write(buff, 0, 9);
				}
				i = 0;
				while (i < j)
				{
					if ((lr = pg.getItem(i)) != null)
					{
						points = lr.getPointList();
						nPoint = points.length;
						if (!pg.hasZ())
						{
							ByteTool.writeInt32(buff, 0, nPoint);
							stm.write(buff, 0, 4);
							stm.write(ByteTool.toByteArray(points, true));
						}
						else
						{
							ByteTool.writeInt32(buff, 0, nPoint);
							stm.write(buff, 0, 4);
							double[] zList;
							double[] mList;
							k = 0;
							 if (!pg.hasM())
							{
								if ((zList = lr.getZList()) != null)
								{
									while (k < nPoint)
									{
										ByteTool.writeDouble(buff, 0, points[k].x);
										ByteTool.writeDouble(buff, 8, points[k].y);
										ByteTool.writeDouble(buff, 16, zList[k]);
										stm.write(buff, 0, 24);
										k++;
									}
								}
								else
								{
									while (k < nPoint)
									{
										ByteTool.writeDouble(buff, 0, points[k].x);
										ByteTool.writeDouble(buff, 8, points[k].y);
										ByteTool.writeDouble(buff, 16, Double.NaN);
										stm.write(buff, 0, 24);
										k++;
									}
								}
							}
							else
							{
								if ((zList = lr.getZList()) != null)
								{
									if ((mList = lr.getMList()) != null)
									{
										while (k < nPoint)
										{
											ByteTool.writeDouble(buff, 0, points[k].x);
											ByteTool.writeDouble(buff, 8, points[k].y);
											ByteTool.writeDouble(buff, 16, zList[k]);
											ByteTool.writeDouble(buff, 24, mList[k]);
											stm.write(buff, 0, 32);
											k++;
										}
									}
									else
									{
										while (k < nPoint)
										{
											ByteTool.writeDouble(buff, 0, points[k].x);
											ByteTool.writeDouble(buff, 8, points[k].y);
											ByteTool.writeDouble(buff, 16, zList[k]);
											ByteTool.writeDouble(buff, 24, Double.NaN);
											stm.write(buff, 0, 32);
											k++;
										}
									}
								}
								else
								{
									if ((mList = lr.getMList()) != null)
									{
										while (k < nPoint)
										{
											ByteTool.writeDouble(buff, 0, points[k].x);
											ByteTool.writeDouble(buff, 8, points[k].y);
											ByteTool.writeDouble(buff, 16, Double.NaN);
											ByteTool.writeDouble(buff, 24, mList[k]);
											stm.write(buff, 0, 32);
											k++;
										}
									}
									else
									{
										while (k < nPoint)
										{
											ByteTool.writeDouble(buff, 0, points[k].x);
											ByteTool.writeDouble(buff, 8, points[k].y);
											ByteTool.writeDouble(buff, 16, Double.NaN);
											ByteTool.writeDouble(buff, 24, Double.NaN);
											stm.write(buff, 0, 32);
											k++;
										}
									}
								}
							}
	
						}
					}
					else
					{
						ByteTool.writeInt32(buff, 0, 0);
						stm.write(buff, 0, 4);
					}
					i++;
				}
				return true;
			}
		case Point:
		case LineString:
		case MultiPoint:
		case Polyline: //MultiLineString
		case MultiPolygon:
		case GeometryCollection:
		case CircularString:
		case CompoundCurve:
		case CurvePolygon:
		case MultiCurve:
		case MultiSurface:
		case Curve:
		case Surface:
		case PolyhedralSurface:
		case Tin:
		case Triangle:
		case LinearRing:
		case Image:
		case String:
		case Ellipse:
		case PieArea:
		case Unknown:
		default:	
			System.out.println("WKBWriter: Unsupported type: "+vec.getVectorType());
			return false;
		}
	}
}
