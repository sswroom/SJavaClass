package org.sswr.util.math;

import org.sswr.util.basic.Point;
import org.sswr.util.data.ByteTool;
import org.sswr.util.data.LineBreakType;
import org.sswr.util.data.SharedInt;
import org.sswr.util.data.StringUtil;
import org.sswr.util.math.geometry.LineString;
import org.sswr.util.math.geometry.LinearRing;
import org.sswr.util.math.geometry.Point2D;
import org.sswr.util.math.geometry.PointZ;
import org.sswr.util.math.geometry.Polygon;
import org.sswr.util.math.geometry.Vector2D;

public class WKBReader {
	public static abstract class ByteReader
	{
		public abstract int readInt32(byte[] buff, int ofst);
		public abstract double readDouble(byte[] buff, int ofst);
	}

	public static class IByteReader extends ByteReader
	{
		public int readInt32(byte[] buff, int ofst)
		{
			return ByteTool.readInt32(buff, ofst);
		}

		public double readDouble(byte[] buff, int ofst)
		{
			return ByteTool.readDouble(buff, ofst);
		}
	}

	public static class MByteReader extends ByteReader
	{
		public int readInt32(byte[] buff, int ofst)
		{
			return ByteTool.readMInt32(buff, ofst);
		}

		public double readDouble(byte[] buff, int ofst)
		{
			return ByteTool.readMDouble(buff, ofst);
		}
	}

	private int srid;

	public WKBReader(int srid)
	{
		this.srid = srid;
	}

	public Vector2D parseWKB(byte[] wkb, int initOfst, int len, SharedInt sizeUsed)
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
					if (ofst + 4 > len)
					{
						return null;
					}
					int numPoints = reader.readInt32(wkb, ofst);
					ofst += 4;
					if (ofst + numPoints * 16 > endOfst)
					{
						return null;
					}
					int tmp;
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
			if (len < ofst + 4)
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
			if (len < ofst + 4)
				return null;
			else
			{
				UInt32 numParts = readUInt32(&wkb[ofst]);
				ofst += 4;
				UOSInt i;
				UOSInt j;
				if (numParts < 1)
				{
					return null;
				}
				Math::Geometry::Polygon *pg;
				NEW_CLASS(pg, Math::Geometry::Polygon(srid));
				i = 0;
				while (i < numParts)
				{
					if (ofst + 4 > len)
					{
						DEL_CLASS(pg);
						return null;
					}
					UInt32 numPoints = readUInt32(&wkb[ofst]);
					ofst += 4;
					if (ofst + numPoints * 24 > len)
					{
						DEL_CLASS(pg);
						return null;
					}
					UOSInt tmp;
					NN<Math::Geometry::LinearRing> lr;
					NEW_CLASSNN(lr, Math::Geometry::LinearRing(srid, numPoints, false, true));
					Coord2DDbl[] points = lr.GetPointList(tmp);
					double[] mList;
					if (lr.GetMList(tmp).SetTo(mList))
					{
						j = 0;
						while (j < numPoints)
						{
							points[j] = Math::Coord2DDbl(readDouble(&wkb[ofst]), readDouble(&wkb[ofst + 8]));
							mList[j] = readDouble(&wkb[ofst + 16]);
							ofst += 24;
							j++;
						}
						pg.AddGeometry(lr);
					}
					else
					{
						lr.Delete();
					}
					i++;
				}
				if (sizeUsed != null) sizeUsed.value = ofst - initOfst;
				return pg;
			}
		case 3003: //PolygonZM
		case 0xC0000003:
			if (len < ofst + 4)
				return null;
			else
			{
				UInt32 numParts = readUInt32(&wkb[ofst]);
				ofst += 4;
				UOSInt i;
				UOSInt j;
				if (numParts < 1)
				{
					return null;
				}
				Math::Geometry::Polygon *pg;
				NEW_CLASS(pg, Math::Geometry::Polygon(srid));
				i = 0;
				while (i < numParts)
				{
					if (ofst + 4 > len)
					{
						DEL_CLASS(pg);
						return null;
					}
					UInt32 numPoints = readUInt32(&wkb[ofst]);
					ofst += 4;
					if (ofst + numPoints * 32 > len)
					{
						DEL_CLASS(pg);
						return null;
					}
					UOSInt tmp;
					NN<Math::Geometry::LinearRing> lr;
					NEW_CLASSNN(lr, Math::Geometry::LinearRing(srid, numPoints, true, true))
					Coord2DDbl[] points = lr.GetPointList(tmp);
					double[] zList;
					double[] mList;
					if (lr.GetZList(tmp).SetTo(zList) && lr.GetMList(tmp).SetTo(mList))
					{
						j = 0;
						while (j < numPoints)
						{
							points[j] = Math::Coord2DDbl(readDouble(&wkb[ofst]), readDouble(&wkb[ofst + 8]));
							zList[j] = readDouble(&wkb[ofst + 16]);
							mList[j] = readDouble(&wkb[ofst + 24]);
							ofst += 32;
							j++;
						}
						pg.AddGeometry(lr);
					}
					else
					{
						lr.Delete();
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
			if (len < ofst + 4)
				return null;
			else
			{
				UInt32 nPolygon = readUInt32(&wkb[ofst]);
				ofst += 4;
				UOSInt thisSize;
				UOSInt i;
				NN<Math::Geometry::Vector2D> vec;
				Math::Geometry::MultiPolygon *mpg;
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
				NEW_CLASS(mpg, Math::Geometry::MultiPolygon(srid));
				i = 0;
				while (i < nPolygon)
				{
					if (!this.ParseWKB(&wkb[ofst], len - ofst, thisSize).SetTo(vec))
					{
						DEL_CLASS(mpg);
						return null;
					}
					else if (vec.GetVectorType() != Math::Geometry::Vector2D::VectorType::Polygon)
					{
						printf("WKBMultipolygon: wrong type: %d\r\n", (Int32)vec.GetVectorType());
						vec.Delete();
						DEL_CLASS(mpg);
						return null;
					}
					else
					{
						mpg.AddGeometry(NN<Math::Geometry::Polygon>::ConvertFrom(vec));
						ofst += thisSize;
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
			if (len < ofst + 4)
				return null;
			else
			{
				UInt32 nGeometry = readUInt32(&wkb[ofst]);
				ofst += 4;
				UOSInt thisSize;
				UOSInt i;
				NN<Math::Geometry::Vector2D> vec;
				Math::Geometry::GeometryCollection *mpg;
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
				NEW_CLASS(mpg, Math::Geometry::GeometryCollection(srid));
				i = 0;
				while (i < nGeometry)
				{
					if (!this.ParseWKB(&wkb[ofst], len - ofst, thisSize).SetTo(vec))
					{
						DEL_CLASS(mpg);
						return null;
					}
					else
					{
						mpg.AddGeometry(vec);
						ofst += thisSize;
					}
					i++;
				}
				if (sizeUsed != null) sizeUsed.value = ofst - initOfst;
				return mpg;
			}
		case 8: //CircularString
			if (len < ofst + 4)
				return null;
			else
			{
				UInt32 numPoints = readUInt32(&wkb[ofst]);
				ofst += 4;
				if (numPoints < 2 || (ofst + numPoints * 16 > len))
					return null;
				Math::Geometry::CircularString *pl;
				UOSInt i;
				NEW_CLASS(pl, Math::Geometry::CircularString(srid, numPoints, false, false));
				Coord2DDbl[] points = pl.GetPointList(i);
				i = 0;
				while (i < numPoints)
				{
					points[i] = Math::Coord2DDbl(readDouble(&wkb[ofst + 0]), readDouble(&wkb[ofst + 8]));
					ofst += 16;
					i++;
				}
				if (sizeUsed != null) sizeUsed.value = ofst - initOfst;
				return pl;
			}
		case 1008: //CircularStringZ
		case 0x80000008:
			if (len < ofst + 4)
				return null;
			else
			{
				UInt32 numPoints = readUInt32(&wkb[ofst]);
				ofst += 4;
				if (numPoints < 2 || (ofst + numPoints * 24 > len))
					return null;
				Math::Geometry::CircularString *pl;
				UOSInt i;
				NEW_CLASS(pl, Math::Geometry::CircularString(srid, numPoints, true, false));
				Coord2DDbl[] points = pl.GetPointList(i);
				double[] zArr;
				if (pl.GetZList(i).SetTo(zArr))
				{
					i = 0;
					while (i < numPoints)
					{
						points[i] = Math::Coord2DDbl(readDouble(&wkb[ofst + 0]), readDouble(&wkb[ofst + 8]));
						zArr[i] = readDouble(&wkb[ofst + 16]);
						ofst += 24;
						i++;
					}
					if (sizeUsed != null) sizeUsed.value = ofst - initOfst;
					return pl;
				}
				else
				{
					DEL_CLASS(pl);
					return null;
				}
			}
		case 2008: //CircularStringM
		case 0x40000008:
			if (len < 9)
				return null;
			else
			{
				UInt32 numPoints = readUInt32(&wkb[ofst]);
				ofst += 4;
				if (numPoints < 2 || (ofst + numPoints * 24 > len))
					return null;
				Math::Geometry::CircularString *pl;
				UOSInt i;
				NEW_CLASS(pl, Math::Geometry::CircularString(srid, numPoints, false, true));
				Coord2DDbl[] points = pl.GetPointList(i);
				double[] mArr;
				if (pl.GetMList(i).SetTo(mArr))
				{
					i = 0;
					while (i < numPoints)
					{
						points[i] = Math::Coord2DDbl(readDouble(&wkb[ofst + 0]), readDouble(&wkb[ofst + 8]));
						mArr[i] = readDouble(&wkb[ofst + 16]);
						ofst += 24;
						i++;
					}
					if (sizeUsed != null) sizeUsed.value = ofst - initOfst;
					return pl;
				}
				else
				{
					DEL_CLASS(pl);
					return null;
				}
			}
		case 3008: //CircularStringZM
		case 0xC0000008:
			if (len < 9)
				return null;
			else
			{
				UInt32 numPoints = readUInt32(&wkb[ofst]);
				ofst += 4;
				if (numPoints < 2 || (ofst + numPoints * 32 > len))
					return null;
				Math::Geometry::CircularString *pl;
				UOSInt i;
				NEW_CLASS(pl, Math::Geometry::CircularString(srid, numPoints, true, true));
				Coord2DDbl[] points = pl.GetPointList(i);
				double[] zArr;
				double[] mArr;
				if (pl.GetZList(i).SetTo(zArr) && pl.GetMList(i).SetTo(mArr))
				{
					i = 0;
					while (i < numPoints)
					{
						points[i] = Math::Coord2DDbl(readDouble(&wkb[ofst + 0]), readDouble(&wkb[ofst + 8]));
						zArr[i] = readDouble(&wkb[ofst + 16]);
						mArr[i] = readDouble(&wkb[ofst + 24]);
						ofst += 32;
						i++;
					}
					if (sizeUsed != null) sizeUsed.value = ofst - initOfst;
					return pl;
				}
				else
				{
					DEL_CLASS(pl);
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
			if (len < ofst + 4)
				return null;
			else
			{
				UInt32 nPolyline = readUInt32(&wkb[ofst]);
				ofst += 4;
				UOSInt thisSize;
				UOSInt i;
				NN<Math::Geometry::Vector2D> vec;
				Math::Geometry::CompoundCurve *cpl;
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
				NEW_CLASS(cpl, Math::Geometry::CompoundCurve(srid));
				i = 0;
				while (i < nPolyline)
				{
					if (!this.ParseWKB(&wkb[ofst], len - ofst, thisSize).SetTo(vec))
					{
						DEL_CLASS(cpl);
						return null;
					}
					else
					{
						Math::Geometry::Vector2D::VectorType t = vec.GetVectorType();
						if (t == Math::Geometry::Vector2D::VectorType::CircularString || t == Math::Geometry::Vector2D::VectorType::LineString)
						{
							cpl.AddGeometry(NN<Math::Geometry::LineString>::ConvertFrom(vec));
							ofst += thisSize;
						}
						else
						{
							printf("WKBCurvePolyline: wrong type: %d\r\n", (Int32)vec.GetVectorType());
							vec.Delete();
							DEL_CLASS(cpl);
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
			if (len < ofst + 4)
				return null;
			else
			{
				UInt32 nCPolyline = readUInt32(&wkb[ofst]);
				ofst += 4;
				UOSInt thisSize;
				UOSInt i;
				NN<Math::Geometry::Vector2D> vec;
				Math::Geometry::CurvePolygon *cpg;
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
				NEW_CLASS(cpg, Math::Geometry::CurvePolygon(srid));
				i = 0;
				while (i < nCPolyline)
				{
					if (!this.ParseWKB(&wkb[ofst], len - ofst, thisSize).SetTo(vec))
					{
						DEL_CLASS(cpg);
						return null;
					}
					else
					{
						Math::Geometry::Vector2D::VectorType t = vec.GetVectorType();
						if (t == Math::Geometry::Vector2D::VectorType::CircularString || t == Math::Geometry::Vector2D::VectorType::CompoundCurve || t == Math::Geometry::Vector2D::VectorType::LineString)
						{
							cpg.AddGeometry(vec);
							ofst += thisSize;
						}
						else
						{
							printf("WKBCurvePolygon: wrong type: %d\r\n", (Int32)vec.GetVectorType());
							vec.Delete();
							DEL_CLASS(cpg);
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
			if (len < ofst + 4)
				return null;
			else
			{
				UInt32 nCPolyline = readUInt32(&wkb[ofst]);
				ofst += 4;
				UOSInt thisSize;
				UOSInt i;
				NN<Math::Geometry::Vector2D> vec;
				Math::Geometry::MultiSurface *cpg;
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
				NEW_CLASS(cpg, Math::Geometry::MultiSurface(srid));
				i = 0;
				while (i < nCPolyline)
				{
					if (!this.ParseWKB(&wkb[ofst], len - ofst, thisSize).SetTo(vec))
					{
						DEL_CLASS(cpg);
						return null;
					}
					else
					{
						Math::Geometry::Vector2D::VectorType t = vec.GetVectorType();
						if (t == Math::Geometry::Vector2D::VectorType::CurvePolygon || t == Math::Geometry::Vector2D::VectorType::Polygon)
						{
							cpg.AddGeometry(vec);
							ofst += thisSize;
						}
						else
						{
							printf("WKBMultiSurface: wrong type: %d\r\n", (Int32)vec.GetVectorType());
							vec.Delete();
							DEL_CLASS(cpg);
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
				StringUtil.appendHex(wkb, ofst, len, ' ', LineBreakType.CRLF);
				System.out.println("WKBReader: Unsupported type: "+geomType);
				System.out.println(sb.toString());
			}
			break;
		}
		return null;
	}
}
