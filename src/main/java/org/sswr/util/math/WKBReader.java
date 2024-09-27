package org.sswr.util.math;

import org.sswr.util.data.ByteTool;
import org.sswr.util.data.LineBreakType;
import org.sswr.util.data.SharedInt;
import org.sswr.util.data.StringUtil;
import org.sswr.util.math.geometry.CircularString;
import org.sswr.util.math.geometry.CompoundCurve;
import org.sswr.util.math.geometry.CurvePolygon;
import org.sswr.util.math.geometry.GeometryCollection;
import org.sswr.util.math.geometry.LineString;
import org.sswr.util.math.geometry.LinearRing;
import org.sswr.util.math.geometry.MultiPolygon;
import org.sswr.util.math.geometry.MultiSurface;
import org.sswr.util.math.geometry.Point2D;
import org.sswr.util.math.geometry.PointZ;
import org.sswr.util.math.geometry.Polygon;
import org.sswr.util.math.geometry.Vector2D;
import org.sswr.util.math.geometry.Vector2D.VectorType;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class WKBReader {
	public static abstract class ByteReader
	{
		public abstract int readInt32(@Nonnull byte[] buff, int ofst);
		public abstract double readDouble(@Nonnull byte[] buff, int ofst);
	}

	public static class IByteReader extends ByteReader
	{
		public int readInt32(@Nonnull byte[] buff, int ofst)
		{
			return ByteTool.readInt32(buff, ofst);
		}

		public double readDouble(@Nonnull byte[] buff, int ofst)
		{
			return ByteTool.readDouble(buff, ofst);
		}
	}

	public static class MByteReader extends ByteReader
	{
		public int readInt32(@Nonnull byte[] buff, int ofst)
		{
			return ByteTool.readMInt32(buff, ofst);
		}

		public double readDouble(@Nonnull byte[] buff, int ofst)
		{
			return ByteTool.readMDouble(buff, ofst);
		}
	}

	private int srid;

	public WKBReader(int srid)
	{
		this.srid = srid;
	}

	@Nullable
	public Vector2D parseWKB(@Nonnull byte[] wkb, int initOfst, int len, @Nullable SharedInt sizeUsed)
	{
		if (len < 5)
		{
			return null;
		}
		int endOfst = initOfst + len;
		byte byteOrder = wkb[initOfst + 0];
		ByteReader reader;
		if (byteOrder == 1)
		{
			reader = new IByteReader();
		}
		else
		{
			reader = new MByteReader();
		}
		int geomType = reader.readInt32(wkb, initOfst + 1);
		int srid = this.srid;
		int ofst = initOfst + 5;
		if ((geomType & 0x20000000) != 0)
		{
			geomType = geomType & 0xDFFFFFFF;
			srid = reader.readInt32(wkb, ofst);
			ofst += 4;
		}
	/*
		0x80000000 = Z
		0x40000000 = M

	#define WKB_POINT_TYPE 1
	#define WKB_LINESTRING_TYPE 2
	#define WKB_POLYGON_TYPE 3
	#define WKB_MULTIPOINT_TYPE 4
	#define WKB_MULTILINESTRING_TYPE 5
	#define WKB_MULTIPOLYGON_TYPE 6
	#define WKB_GEOMETRYCOLLECTION_TYPE 7
	#define WKB_CIRCULARSTRING_TYPE 8
	#define WKB_COMPOUNDCURVE_TYPE 9
	#define WKB_CURVEPOLYGON_TYPE 10
	#define WKB_MULTICURVE_TYPE 11
	#define WKB_MULTISURFACE_TYPE 12
	#define WKB_CURVE_TYPE 13
	#define WKB_SURFACE_TYPE 14
	#define WKB_POLYHEDRALSURFACE_TYPE 15
	#define WKB_TIN_TYPE 16
	#define WKB_TRIANGLE_TYPE 17
	*/
		switch (geomType)
		{
		case 1: //Point
			if (endOfst < ofst + 16)
				return null;
			else
			{
				Point2D pt = new Point2D(srid, reader.readDouble(wkb, ofst), reader.readDouble(wkb, ofst + 8));
				if (sizeUsed != null) sizeUsed.value = ofst + 16 - initOfst;
				return pt;
			}
		case 1001: //PointZ
		case 0x80000001:
			if (endOfst < ofst + 24)
				return null;
			else
			{
				PointZ pt = new PointZ(srid, reader.readDouble(wkb, ofst), reader.readDouble(wkb, ofst + 8), reader.readDouble(wkb, ofst + 16));
				if (sizeUsed != null) sizeUsed.value = ofst + 24 - initOfst;
				return pt;
			}
		case 2001: //PointM
		case 0x40000001:
			if (endOfst < ofst + 24)
				return null;
			else
			{
				Point2D pt = new Point2D(srid, reader.readDouble(wkb, ofst), reader.readDouble(wkb, ofst + 8));
				if (sizeUsed != null) sizeUsed.value = ofst + 24 - initOfst;
				return pt;
			}
		case 3001: //PointZM
		case 0xC0000001:
			if (endOfst < ofst + 32)
				return null;
			else
			{
				PointZ pt = new PointZ(srid, reader.readDouble(wkb, ofst), reader.readDouble(wkb, ofst + 8), reader.readDouble(wkb, ofst + 16));
				if (sizeUsed != null) sizeUsed.value = ofst + 32 - initOfst;
				return pt;
			}
		case 2: //LineString
			if (endOfst < ofst + 4)
				return null;
			else
			{
				int numPoints = reader.readInt32(wkb, ofst);
				ofst += 4;
				if (numPoints < 2 || (ofst + numPoints * 16 > endOfst))
					return null;
				LineString pl = new LineString(srid, numPoints, false, false);
				int i;
				Coord2DDbl[] points = pl.getPointList();
				i = 0;
				while (i < numPoints)
				{
					points[i] = new Coord2DDbl(reader.readDouble(wkb, ofst + 0), reader.readDouble(wkb, ofst + 8));
					ofst += 16;
					i++;
				}
				if (sizeUsed != null) sizeUsed.value = ofst - initOfst;
				return pl;
			}
		case 1002: //LineStringZ
		case 0x80000002:
			if (endOfst < ofst + 4)
				return null;
			else
			{
				int numPoints = reader.readInt32(wkb, ofst);
				ofst += 4;
				if (numPoints < 2 || (ofst + numPoints * 24 > endOfst))
					return null;
				LineString pl = new LineString(srid, numPoints, true, false);
				int i;
				Coord2DDbl[] points = pl.getPointList();
				double[] zArr;
				if ((zArr = pl.getZList()) != null)
				{
					i = 0;
					while (i < numPoints)
					{
						points[i] = new Coord2DDbl(reader.readDouble(wkb, ofst + 0), reader.readDouble(wkb, ofst + 8));
						zArr[i] = reader.readDouble(wkb, ofst + 16);
						ofst += 24;
						i++;
					}
					if (sizeUsed != null) sizeUsed.value = ofst - initOfst;
					return pl;
				}
				else
				{
					return null;
				}
			}
		case 2002: //LineStringM
		case 0x40000002:
			if (len < 9)
				return null;
			else
			{
				int numPoints = reader.readInt32(wkb, ofst);
				ofst += 4;
				if (numPoints < 2 || (ofst + numPoints * 24 > endOfst))
					return null;
				LineString pl = new LineString(srid, numPoints, false, true);
				int i;
				Coord2DDbl[] points = pl.getPointList();
				double[] mArr;
				if ((mArr = pl.getMList()) != null)
				{
					i = 0;
					while (i < numPoints)
					{
						points[i] = new Coord2DDbl(reader.readDouble(wkb, ofst + 0), reader.readDouble(wkb, ofst + 8));
						mArr[i] = reader.readDouble(wkb, ofst + 16);
						ofst += 24;
						i++;
					}
					if (sizeUsed != null) sizeUsed.value = ofst - initOfst;
					return pl;
				}
				else
				{
					return null;
				}
			}
		case 3002: //LineStringZM
		case 0xC0000002:
			if (len < 9)
				return null;
			else
			{
				int numPoints = reader.readInt32(wkb, ofst);
				ofst += 4;
				if (numPoints < 2 || (ofst + numPoints * 32 > endOfst))
					return null;
				LineString pl = new LineString(srid, numPoints, true, true);
				int i;
				Coord2DDbl[] points = pl.getPointList();
				double[] zArr;
				double[] mArr;
				if ((zArr = pl.getZList()) != null && (mArr = pl.getMList()) != null)
				{
					i = 0;
					while (i < numPoints)
					{
						points[i] = new Coord2DDbl(reader.readDouble(wkb, ofst + 0), reader.readDouble(wkb, ofst + 8));
						zArr[i] = reader.readDouble(wkb, ofst + 16);
						mArr[i] = reader.readDouble(wkb, ofst + 24);
						ofst += 32;
						i++;
					}
					if (sizeUsed != null) sizeUsed.value = ofst - initOfst;
					return pl;
				}
				else
				{
					return null;
				}
			}
		case 3: //Polygon
			if (endOfst < ofst + 4)
				return null;
			else
			{
				int numParts = reader.readInt32(wkb, ofst);
				ofst += 4;
				int i;
				int j;
				if (numParts < 1)
				{
					return null;
				}
				Polygon pg = new Polygon(srid);
				i = 0;
				while (i < numParts)
				{
					if (ofst + 4 > endOfst)
					{
						return null;
					}
					int numPoints = reader.readInt32(wkb, ofst);
					ofst += 4;
					if (ofst + numPoints * 16 > endOfst)
					{
						return null;
					}
					LinearRing lr = new LinearRing(srid, numPoints, false, false);
					Coord2DDbl[] points = lr.getPointList();
					j = 0;
					while (j < numPoints)
					{
						points[j] = new Coord2DDbl(reader.readDouble(wkb, ofst), reader.readDouble(wkb, ofst + 8));
						ofst += 16;
						j++;
					}
					pg.addGeometry(lr);
					i++;
				}
				if (sizeUsed != null) sizeUsed.value = ofst - initOfst;
				return pg;
			}
		case 1003: //PolygonZ
		case 0x80000003:
			if (endOfst < ofst + 4)
				return null;
			else
			{
				int numParts = reader.readInt32(wkb, ofst);
				ofst += 4;
				int i;
				int j;
				if (numParts < 1)
				{
					return null;
				}
				Polygon pg = new Polygon(srid);
				i = 0;
				while (i < numParts)
				{
					if (ofst + 4 > endOfst)
					{
						return null;
					}
					int numPoints = reader.readInt32(wkb, ofst);
					ofst += 4;
					if (ofst + numPoints * 24 > endOfst)
					{
						return null;
					}
					LinearRing lr = new LinearRing(srid, numPoints, true, false);
					Coord2DDbl[] points = lr.getPointList();
					double[] zList;
					if ((zList = lr.getZList()) != null)
					{
						j = 0;
						while (j < numPoints)
						{
							points[j] = new Coord2DDbl(reader.readDouble(wkb, ofst), reader.readDouble(wkb, ofst + 8));
							zList[j] = reader.readDouble(wkb, ofst + 16);
							ofst += 24;
							j++;
						}
						pg.addGeometry(lr);
					}
					else
					{
					}
					i++;
				}
				if (sizeUsed != null) sizeUsed.value = ofst - initOfst;
				return pg;
			}
		case 2003: //PolygonM
		case 0x40000003:
			if (endOfst < ofst + 4)
				return null;
			else
			{
				int numParts = reader.readInt32(wkb, ofst);
				ofst += 4;
				int i;
				int j;
				if (numParts < 1)
				{
					return null;
				}
				Polygon pg = new Polygon(srid);
				i = 0;
				while (i < numParts)
				{
					if (ofst + 4 > endOfst)
					{
						return null;
					}
					int numPoints = reader.readInt32(wkb, ofst);
					ofst += 4;
					if (ofst + numPoints * 24 > endOfst)
					{
						return null;
					}
					LinearRing lr = new LinearRing(srid, numPoints, false, true);
					Coord2DDbl[] points = lr.getPointList();
					double[] mList;
					if ((mList = lr.getMList()) != null)
					{
						j = 0;
						while (j < numPoints)
						{
							points[j] = new Coord2DDbl(reader.readDouble(wkb, ofst), reader.readDouble(wkb, ofst + 8));
							mList[j] = reader.readDouble(wkb, ofst + 16);
							ofst += 24;
							j++;
						}
						pg.addGeometry(lr);
					}
					else
					{
					}
					i++;
				}
				if (sizeUsed != null) sizeUsed.value = ofst - initOfst;
				return pg;
			}
		case 3003: //PolygonZM
		case 0xC0000003:
			if (endOfst < ofst + 4)
				return null;
			else
			{
				int numParts = reader.readInt32(wkb, ofst);
				ofst += 4;
				int i;
				int j;
				if (numParts < 1)
				{
					return null;
				}
				Polygon pg = new Polygon(srid);
				i = 0;
				while (i < numParts)
				{
					if (ofst + 4 > endOfst)
					{
						return null;
					}
					int numPoints = reader.readInt32(wkb, ofst);
					ofst += 4;
					if (ofst + numPoints * 32 > endOfst)
					{
						return null;
					}
					LinearRing lr = new LinearRing(srid, numPoints, true, true);
					Coord2DDbl[] points = lr.getPointList();
					double[] zList;
					double[] mList;
					if ((zList = lr.getZList()) != null && (mList = lr.getMList()) != null)
					{
						j = 0;
						while (j < numPoints)
						{
							points[j] = new Coord2DDbl(reader.readDouble(wkb, ofst), reader.readDouble(wkb, ofst + 8));
							zList[j] = reader.readDouble(wkb, ofst + 16);
							mList[j] = reader.readDouble(wkb, ofst + 24);
							ofst += 32;
							j++;
						}
						pg.addGeometry(lr);
					}
					else
					{
					}
					i++;
				}
				if (sizeUsed != null) sizeUsed.value = ofst - initOfst;
				return pg;
			}
		case 6: //MultiPolygon
		case 1006: //MultiPolygonZ
		case 2006: //MultiPolygonM
		case 3006: //MultiPolygonZM
		case 0x80000006:
		case 0x40000006:
		case 0xC0000006:
			if (endOfst < ofst + 4)
				return null;
			else
			{
				int nPolygon = reader.readInt32(wkb, ofst);
				ofst += 4;
				SharedInt thisSize = new SharedInt();
				int i;
				Vector2D vec;
				MultiPolygon mpg;
	/*			Bool hasZ;
				Bool hasM;
				if (geomType & 0xC0000000)
				{
					hasZ = (geomType & 0x80000000) != 0;
					hasM = (geomType & 0x40000000) != 0;
				}
				else
				{
					UInt32 t = geomType / 1000;
					hasZ = (t & 1) != 0;
					hasM = (t & 2) != 0;
				}*/
				mpg = new MultiPolygon(srid);
				i = 0;
				while (i < nPolygon)
				{
					if ((vec = this.parseWKB(wkb, ofst, endOfst - ofst, thisSize)) == null)
					{
						return null;
					}
					else if (vec.getVectorType() != VectorType.Polygon)
					{
						System.out.println("WKBMultipolygon: wrong type: "+ vec.getVectorType());
						return null;
					}
					else
					{
						mpg.addGeometry((Polygon)vec);
						ofst += thisSize.value;
					}
					i++;
				}
				if (sizeUsed != null) sizeUsed.value = ofst - initOfst;
				return mpg;
			}
		case 7: //GeometryCollection
		case 1007: //GeometryCollectionZ
		case 2007: //GeometryCollectionM
		case 3007: //GeometryCollectionZM
		case 0x80000007:
		case 0x40000007:
		case 0xC0000007:
			if (endOfst < ofst + 4)
				return null;
			else
			{
				int nGeometry = reader.readInt32(wkb, ofst);
				ofst += 4;
				SharedInt thisSize = new SharedInt();
				int i;
				Vector2D vec;
				GeometryCollection mpg;
	/*			Bool hasZ;
				Bool hasM;
				if (geomType & 0xC0000000)
				{
					hasZ = (geomType & 0x80000000) != 0;
					hasM = (geomType & 0x40000000) != 0;
				}
				else
				{
					UInt32 t = geomType / 1000;
					hasZ = (t & 1) != 0;
					hasM = (t & 2) != 0;
				}*/
				mpg = new GeometryCollection(srid);
				i = 0;
				while (i < nGeometry)
				{
					if ((vec = this.parseWKB(wkb, ofst, endOfst - ofst, thisSize)) == null)
					{
						return null;
					}
					else
					{
						mpg.addGeometry(vec);
						ofst += thisSize.value;
					}
					i++;
				}
				if (sizeUsed != null) sizeUsed.value = ofst - initOfst;
				return mpg;
			}
		case 8: //CircularString
			if (endOfst < ofst + 4)
				return null;
			else
			{
				int numPoints = reader.readInt32(wkb, ofst);
				ofst += 4;
				if (numPoints < 2 || (ofst + numPoints * 16 > endOfst))
					return null;
				CircularString pl;
				int i;
				pl = new CircularString(srid, numPoints, false, false);
				Coord2DDbl[] points = pl.getPointList();
				i = 0;
				while (i < numPoints)
				{
					points[i] = new Coord2DDbl(reader.readDouble(wkb, ofst + 0), reader.readDouble(wkb, ofst + 8));
					ofst += 16;
					i++;
				}
				if (sizeUsed != null) sizeUsed.value = ofst - initOfst;
				return pl;
			}
		case 1008: //CircularStringZ
		case 0x80000008:
			if (endOfst < ofst + 4)
				return null;
			else
			{
				int numPoints = reader.readInt32(wkb, ofst);
				ofst += 4;
				if (numPoints < 2 || (ofst + numPoints * 24 > endOfst))
					return null;
				CircularString pl;
				int i;
				pl = new CircularString(srid, numPoints, true, false);
				Coord2DDbl[] points = pl.getPointList();
				double[] zArr;
				if ((zArr = pl.getZList()) != null)
				{
					i = 0;
					while (i < numPoints)
					{
						points[i] = new Coord2DDbl(reader.readDouble(wkb, ofst + 0), reader.readDouble(wkb, ofst + 8));
						zArr[i] = reader.readDouble(wkb, ofst + 16);
						ofst += 24;
						i++;
					}
					if (sizeUsed != null) sizeUsed.value = ofst - initOfst;
					return pl;
				}
				else
				{
					return null;
				}
			}
		case 2008: //CircularStringM
		case 0x40000008:
			if (len < 9)
				return null;
			else
			{
				int numPoints = reader.readInt32(wkb, ofst);
				ofst += 4;
				if (numPoints < 2 || (ofst + numPoints * 24 > endOfst))
					return null;
				CircularString pl;
				int i;
				pl = new CircularString(srid, numPoints, false, true);
				Coord2DDbl[] points = pl.getPointList();
				double[] mArr;
				if ((mArr = pl.getMList()) != null)
				{
					i = 0;
					while (i < numPoints)
					{
						points[i] = new Coord2DDbl(reader.readDouble(wkb, ofst + 0), reader.readDouble(wkb, ofst + 8));
						mArr[i] = reader.readDouble(wkb, ofst + 16);
						ofst += 24;
						i++;
					}
					if (sizeUsed != null) sizeUsed.value = ofst - initOfst;
					return pl;
				}
				else
				{
					return null;
				}
			}
		case 3008: //CircularStringZM
		case 0xC0000008:
			if (len < 9)
				return null;
			else
			{
				int numPoints = reader.readInt32(wkb, ofst);
				ofst += 4;
				if (numPoints < 2 || (ofst + numPoints * 32 > endOfst))
					return null;
				CircularString pl;
				int i;
				pl = new CircularString(srid, numPoints, true, true);
				Coord2DDbl[] points = pl.getPointList();
				double[] zArr;
				double[] mArr;
				if ((zArr = pl.getZList()) != null && (mArr = pl.getMList()) != null)
				{
					i = 0;
					while (i < numPoints)
					{
						points[i] = new Coord2DDbl(reader.readDouble(wkb, ofst + 0), reader.readDouble(wkb, ofst + 8));
						zArr[i] = reader.readDouble(wkb, ofst + 16);
						mArr[i] = reader.readDouble(wkb, ofst + 24);
						ofst += 32;
						i++;
					}
					if (sizeUsed != null) sizeUsed.value = ofst - initOfst;
					return pl;
				}
				else
				{
					return null;
				}
			}
		case 9: //CompoundCurve
		case 1009: //CompoundCurveZ
		case 2009: //CompoundCurveM
		case 3009: //CompoundCurveZM
		case 0x80000009:
		case 0x40000009:
		case 0xC0000009:
			if (endOfst < ofst + 4)
				return null;
			else
			{
				int nPolyline = reader.readInt32(wkb, ofst);
				ofst += 4;
				SharedInt thisSize = new SharedInt();
				int i;
				Vector2D vec;
				CompoundCurve cpl;
	/*			Bool hasZ;
				Bool hasM;
				if (geomType & 0xC0000000)
				{
					hasZ = (geomType & 0x80000000) != 0;
					hasM = (geomType & 0x40000000) != 0;
				}
				else
				{
					UInt32 t = geomType / 1000;
					hasZ = (t & 1) != 0;
					hasM = (t & 2) != 0;
				}*/
				cpl = new CompoundCurve(srid);
				i = 0;
				while (i < nPolyline)
				{
					if ((vec = this.parseWKB(wkb, ofst, endOfst - ofst, thisSize)) == null)
					{
						return null;
					}
					else
					{
						VectorType t = vec.getVectorType();
						if (t == VectorType.CircularString || t == VectorType.LineString)
						{
							cpl.addGeometry((LineString)vec);
							ofst += thisSize.value;
						}
						else
						{
							System.out.println("WKBCurvePolyline: wrong type: "+vec.getVectorType());
							return null;
						}
					}
					i++;
				}
				if (sizeUsed != null) sizeUsed.value = ofst - initOfst;
				return cpl;
			}
		case 10: //CurvePolygon
		case 1010: //CurvePolygonZ
		case 2010: //CurvePolygonM
		case 3010: //CurvePolygonZM
		case 0x8000000A:
		case 0x4000000A:
		case 0xC000000A:
			if (endOfst < ofst + 4)
				return null;
			else
			{
				int nCPolyline = reader.readInt32(wkb, ofst);
				ofst += 4;
				SharedInt thisSize = new SharedInt();
				int i;
				Vector2D vec;
				CurvePolygon cpg;
	/*			Bool hasZ;
				Bool hasM;
				if (geomType & 0xC0000000)
				{
					hasZ = (geomType & 0x80000000) != 0;
					hasM = (geomType & 0x40000000) != 0;
				}
				else
				{
					UInt32 t = geomType / 1000;
					hasZ = (t & 1) != 0;
					hasM = (t & 2) != 0;
				}*/
				cpg = new CurvePolygon(srid);
				i = 0;
				while (i < nCPolyline)
				{
					if ((vec = this.parseWKB(wkb, ofst, endOfst - ofst, thisSize)) == null)
					{
						return null;
					}
					else
					{
						VectorType t = vec.getVectorType();
						if (t == VectorType.CircularString || t == VectorType.CompoundCurve || t == VectorType.LineString)
						{
							cpg.addGeometry(vec);
							ofst += thisSize.value;
						}
						else
						{
							System.out.println("WKBCurvePolygon: wrong type: "+vec.getVectorType());
							return null;
						}
					}
					i++;
				}
				if (sizeUsed != null) sizeUsed.value = ofst - initOfst;
				return cpg;
			}
		case 12: //MultiSurface
		case 1012: //MultiSurfaceZ
		case 2012: //MultiSurfaceM
		case 3012: //MultiSurfaceZM
		case 0x8000000C:
		case 0x4000000C:
		case 0xC000000C:
			if (endOfst < ofst + 4)
				return null;
			else
			{
				int nCPolyline = reader.readInt32(wkb, ofst);
				ofst += 4;
				SharedInt thisSize = new SharedInt();
				int i;
				Vector2D vec;
				MultiSurface cpg;
	/*			Bool hasZ;
				Bool hasM;
				if (geomType & 0xC0000000)
				{
					hasZ = (geomType & 0x80000000) != 0;
					hasM = (geomType & 0x40000000) != 0;
				}
				else
				{
					UInt32 t = geomType / 1000;
					hasZ = (t & 1) != 0;
					hasM = (t & 2) != 0;
				}*/
				cpg = new MultiSurface(srid);
				i = 0;
				while (i < nCPolyline)
				{
					if ((vec = this.parseWKB(wkb, ofst, endOfst - ofst, thisSize)) == null)
					{
						return null;
					}
					else
					{
						VectorType t = vec.getVectorType();
						if (t == VectorType.CurvePolygon || t == VectorType.Polygon)
						{
							cpg.addGeometry(vec);
							ofst += thisSize.value;
						}
						else
						{
							System.out.println("WKBMultiSurface: wrong type: " + vec.getVectorType());
							return null;
						}
					}
					i++;
				}
				if (sizeUsed != null) sizeUsed.value = ofst - initOfst;
				return cpg;
			}
		default:
			{
				StringBuilder sb = new StringBuilder();
				StringUtil.appendHex(sb, wkb, initOfst, len, ' ', LineBreakType.CRLF);
				System.out.println("WKBReader: Unsupported type: "+geomType);
				System.out.println(sb.toString());
			}
			break;
		}
		return null;
	}
}
