package org.sswr.util.data;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateXYM;
import org.locationtech.jts.geom.CoordinateXYZM;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.sswr.util.math.Coord2DDbl;
import org.sswr.util.math.CoordinateSystem;
import org.sswr.util.math.CoordinateSystemManager;
import org.sswr.util.math.geometry.LineString;
import org.sswr.util.math.geometry.LinearRing;
import org.sswr.util.math.geometry.MultiPolygon;
import org.sswr.util.math.geometry.Point2D;
import org.sswr.util.math.geometry.PointM;
import org.sswr.util.math.geometry.PointZ;
import org.sswr.util.math.geometry.PointZM;
import org.sswr.util.math.geometry.Polygon;
import org.sswr.util.math.geometry.Polyline;
import org.sswr.util.math.geometry.Vector2D;
import org.sswr.util.math.geometry.Vector2D.VectorType;
import org.sswr.util.math.unit.Distance.DistanceUnit;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class GeometryUtil
{
	@Nullable
	public static String toWKT(@Nonnull Geometry geometry)
	{
		org.sswr.util.math.WKTWriter writer = new org.sswr.util.math.WKTWriter();
		Vector2D vec = toVector2D(geometry);
		if (vec == null)
			return null;
		return writer.generateWKT(vec);
	}

	@Nonnull
	public static Geometry createPointZ(double x, double y, double z, int srid)
	{
		GeometryFactory factory = new GeometryFactory(new PrecisionModel(), srid);
		return factory.createPoint(new Coordinate(x, y, z));
	}

	@Nonnull
	public static Geometry createLineStringZ(@Nonnull List<Double> points, int srid)
	{
		GeometryFactory factory = new GeometryFactory(new PrecisionModel(), srid);
		Coordinate[] coordinates = new Coordinate[points.size() / 3];
		int i = 0;
		int j = coordinates.length;
		int k = 0;
		while (i < j)
		{
			coordinates[i] = new Coordinate(points.get(k), points.get(k + 1), points.get(k + 2));
			k += 3;
			i++;
		}
		return factory.createLineString(coordinates);
	}

	@Nullable
	public static Geometry fromVector2D(@Nonnull Vector2D vec)
	{
		GeometryFactory factory = new GeometryFactory(new PrecisionModel(), vec.getSRID());
		if (vec.getVectorType() == VectorType.Point)
		{
			if (vec.hasZ())
			{
				if (vec.hasM())
				{
					PointZM pt = (PointZM)vec;
					return factory.createPoint(new CoordinateXYZM(pt.getCenter().x, pt.getCenter().y, pt.getZ(), pt.getM()));
				}
				else
				{
					PointZ pt = (PointZ)vec;
					return factory.createPoint(new Coordinate(pt.getCenter().x, pt.getCenter().y, pt.getZ()));
				}
			}
			else
			{
				if (vec.hasM())
				{
					PointM pt = (PointM)vec;
					return factory.createPoint(new CoordinateXYM(pt.getCenter().x, pt.getCenter().y, pt.getM()));
				}
				else
				{
					Point2D pt = (Point2D)vec;
					return factory.createPoint(new Coordinate(pt.getCenter().x, pt.getCenter().y));
				}
			}
		}
		else if (vec.getVectorType() == VectorType.LineString)
		{
			LineString pl = (LineString)vec;
			Coord2DDbl[] ptArr = pl.getPointList();
			double[] zArr = pl.getZList();
			double[] mArr = pl.getMList();
			Coordinate[] coordinates = new Coordinate[ptArr.length];
			int i = ptArr.length;
			if (zArr != null)
			{
				if (mArr != null)
				{
					while (i-- > 0)
					{
						coordinates[i] = new CoordinateXYZM(ptArr[i].x, ptArr[i].y, zArr[i], mArr[i]);
					}
				}
				else
				{
					while (i-- > 0)
					{
						coordinates[i] = new Coordinate(ptArr[i].x, ptArr[i].y, zArr[i]);
					}
				}
			}
			else
			{
				while (i-- > 0)
				{
					coordinates[i] = new Coordinate(ptArr[i].x, ptArr[i].y);
				}
			}
			return factory.createLineString(coordinates);
		}
		else if (vec.getVectorType() == VectorType.LinearRing)
		{
			LinearRing lr = (LinearRing)vec;
			Coord2DDbl[] ptArr = lr.getPointList();
			double[] zArr = lr.getZList();
			double[] mArr = lr.getMList();
			Coordinate[] coordinates = new Coordinate[ptArr.length];
			int i = ptArr.length;
			if (zArr != null)
			{
				if (mArr != null)
				{
					while (i-- > 0)
					{
						coordinates[i] = new CoordinateXYZM(ptArr[i].x, ptArr[i].y, zArr[i], mArr[i]);
					}
				}
				else
				{
					while (i-- > 0)
					{
						coordinates[i] = new Coordinate(ptArr[i].x, ptArr[i].y, zArr[i]);
					}
				}
			}
			else
			{
				while (i-- > 0)
				{
					coordinates[i] = new Coordinate(ptArr[i].x, ptArr[i].y);
				}
			}
			return factory.createLinearRing(coordinates);
		}
		else if (vec.getVectorType() == VectorType.Polyline)
		{
			Polyline pl = (Polyline)vec;
			org.locationtech.jts.geom.LineString[] lineStrings = new org.locationtech.jts.geom.LineString[pl.getCount()];
			int i = pl.getCount();
			while (i-- > 0)
			{
				lineStrings[i] = (org.locationtech.jts.geom.LineString)fromVector2D(pl.getItemNN(i));
			}
			return factory.createMultiLineString(lineStrings);
		}
		else if (vec.getVectorType() == VectorType.Polygon)
		{
			Polygon pg = (Polygon)vec;
			org.locationtech.jts.geom.LinearRing[] lineStrings = new org.locationtech.jts.geom.LinearRing[pg.getCount()];
			int i = pg.getCount();
			while (i-- > 0)
			{
				lineStrings[i] = (org.locationtech.jts.geom.LinearRing)fromVector2D(pg.getItemNN(i));
			}
			if (lineStrings.length == 1)
			{
				return factory.createPolygon(lineStrings[0]);
			}
			else
			{
				return factory.createPolygon(lineStrings[0], Arrays.copyOfRange(lineStrings, 1, lineStrings.length));
			}
		}
		System.out.println("GeometryUtil: Unsupported type: "+vec.toString());
		return null;
	}

	@Nullable
	public static Vector2D toVector2D(@Nonnull Geometry geometry)
	{
		switch (geometry.getGeometryType())
		{
		case Geometry.TYPENAME_POINT:
		{
			org.locationtech.jts.geom.Point src = (org.locationtech.jts.geom.Point)geometry;
			Coordinate coord = src.getCoordinate();
			if (!Double.isNaN(coord.getZ()))
			{
				if (!Double.isNaN(coord.getM()))
				{
					return new PointZM(src.getSRID(), src.getX(), src.getY(), coord.getZ(), coord.getM());
				}
				else
				{
					return new PointZ(src.getSRID(), src.getX(), src.getY(), coord.getZ());
				}
			}
			else
			{
				if (!Double.isNaN(coord.getM()))
				{
					return new PointM(src.getSRID(), src.getX(), src.getY(), coord.getM());
				}
				else
				{
					return new Point2D(src.getSRID(), src.getX(), src.getY());
				}
			}
		}
		case Geometry.TYPENAME_LINESTRING:
		{
			org.locationtech.jts.geom.LineString src = (org.locationtech.jts.geom.LineString)geometry;
			Coordinate[] coords = src.getCoordinates();
			boolean hasZ = coords.length > 0 && !Double.isNaN(coords[0].getZ());
			boolean hasM = coords.length > 0 && !Double.isNaN(coords[0].getM());
			LineString dest = new LineString(src.getSRID(), coords.length, hasZ, hasM);
			Coord2DDbl[] coordd = dest.getPointList();
			double[] zList = dest.getZList();
			double[] mList = dest.getMList();
			int i = 0;
			int j = coords.length;
			while (i < j)
			{
				coordd[i] = new Coord2DDbl(coords[i].x, coords[i].y);
				if (hasZ && zList != null)
				{
					zList[i] = coords[i].getZ();
				}
				if (hasM && mList != null)
				{
					mList[i] = coords[i].getM();
				}
				i++;
			}
			return dest;
		}
		case Geometry.TYPENAME_MULTILINESTRING:
		{
			org.locationtech.jts.geom.MultiLineString src = (org.locationtech.jts.geom.MultiLineString)geometry;
			Coordinate[] allCoords = src.getCoordinates();
			boolean hasZ = allCoords.length > 0 && !Double.isNaN(allCoords[0].getZ());
			boolean hasM = allCoords.length > 0 && !Double.isNaN(allCoords[0].getM());
			Polyline dest = new Polyline(src.getSRID());
			LineString lineString;
			int i = 0;
			int j = src.getNumGeometries();
			int m;
			int n;
			Geometry geom;
			while (i < j)
			{
				geom = src.getGeometryN(i);
				Coordinate[] coords = geom.getCoordinates();
				lineString = new LineString(src.getSRID(), coords.length, hasZ, hasM);
				Coord2DDbl[] coordd = lineString.getPointList();
				double[] zList = lineString.getZList();
				double[] mList = lineString.getMList();
				m = 0;
				n = coords.length;
				while (m < n)
				{
					coordd[m] = new Coord2DDbl(coords[m].x, coords[m].y);
					if (hasZ && zList != null)
					{
						zList[m] = coords[m].getZ();
					}
					if (hasM && mList != null)
					{
						mList[m] = coords[m].getM();
					}
					m++;
				}
				dest.addGeometry(lineString);
				i++;
			}
			return dest;
		}
		case Geometry.TYPENAME_POLYGON:
		{
			org.locationtech.jts.geom.Polygon src = (org.locationtech.jts.geom.Polygon)geometry;
			Coordinate[] allCoords = src.getCoordinates();
			boolean hasZ = allCoords.length > 0 && !Double.isNaN(allCoords[0].getZ());
			boolean hasM = allCoords.length > 0 && !Double.isNaN(allCoords[0].getM());
			Polygon dest = new Polygon(src.getSRID());
			LinearRing lr;
			int i = 0;
			int j = src.getNumInteriorRing() + 1;
			int m;
			int n;
			org.locationtech.jts.geom.LinearRing geom;
			while (i < j)
			{
				if (i == 0)
				{
					geom = src.getExteriorRing();
				}
				else
				{
					geom = src.getInteriorRingN(i - 1);
				}
				Coordinate[] coords = geom.getCoordinates();
				lr = new LinearRing(src.getSRID(), coords.length, hasZ, hasM);
				Coord2DDbl[] coordd = lr.getPointList();
				double[] zList = lr.getZList();
				double[] mList = lr.getMList();
				m = 0;
				n = coords.length;
				while (m < n)
				{
					coordd[m] = new Coord2DDbl(coords[m].x, coords[m].y);
					if (hasZ && zList != null)
					{
						zList[m] = coords[m].getZ();
					}
					if (hasM && mList != null)
					{
						mList[m] = coords[m].getM();
					}
					m++;
				}
				dest.addGeometry(lr);
				i++;
			}
			return dest;
		}
		case Geometry.TYPENAME_MULTIPOLYGON:
		{
			org.locationtech.jts.geom.MultiPolygon src = (org.locationtech.jts.geom.MultiPolygon)geometry;
			MultiPolygon dest = new MultiPolygon(src.getSRID());
			int i = 0;
			int j = src.getNumGeometries();
			while (i < j)
			{
				Vector2D vec = toVector2D(src.getGeometryN(i));
				if (vec != null)
					dest.addGeometry((Polygon)vec);
				i++;
			}
			return dest;
		}
		default:
			System.out.println("GeometryUtil.toVector2D: Unsupported type: "+geometry.getGeometryType());
			return null;
		}
	}

	public static double calcMaxDistanceFromCenter(@Nonnull Geometry geometry, @Nonnull DistanceUnit unit)
	{
		return calcMaxDistanceFromPoint(geometry.getCentroid(), geometry, unit);
	}

	public static double calcMaxDistanceFromPoint(@Nonnull Point point, @Nonnull Geometry geometry, @Nonnull DistanceUnit unit)
	{
		Coordinate[] coordinates = geometry.getCoordinates();
		Coordinate ccoord = point.getCoordinate();
		int srid = geometry.getSRID();
		double maxDist = -1;
		Coordinate maxCoord = ccoord;
		double thisDist;
		int i = 0;
		int j = coordinates.length;
		while (i < j)
		{
			thisDist = coordinates[i].distance(ccoord);
			if (thisDist > maxDist)
			{
				maxDist = thisDist;
				maxCoord = coordinates[i];
			}
			i++;
		}
		CoordinateSystem csys = CoordinateSystemManager.srCreateCSys(srid);
		if (csys == null)
		{
			return maxDist;
		}
		else
		{
			return csys.calSurfaceDistance(new Coord2DDbl(ccoord.x, ccoord.y), new Coord2DDbl(maxCoord.x, maxCoord.y), unit);
		}
	}

	@Nonnull
	public static Coord2DDbl mercatorToProject(double lat, double lon)
	{
		double a = 6378137.0;
		return new Coord2DDbl(lon * a, a * Math.log(Math.tan(Math.PI * 0.25 + lat * 0.5)));
	}
	
	public static void calcHVAngleRad(@Nonnull Coord2DDbl ptCurr, @Nonnull Coord2DDbl ptNext, double heightCurr, double heightNext, @Nonnull SharedDouble hAngle, @Nonnull SharedDouble vAngle)
	{
		Coord2DDbl projCurr = mercatorToProject(ptCurr.y, ptCurr.x);
		Coord2DDbl projDiff = mercatorToProject(ptNext.y, ptNext.x).subtract(projCurr);
		double len = Math.sqrt(projDiff.x * projDiff.x + projDiff.y * projDiff.y);
		if (len == 0)
		{
			hAngle.value = 0;
			if (heightNext > heightCurr)
				vAngle.value = Math.PI;
			else if (heightNext < heightCurr)
				vAngle.value = -Math.PI;
			else
				vAngle.value = 0;
		}
		else
		{
			hAngle.value = Math.atan2(projDiff.x, projDiff.y);
			vAngle.value = Math.atan2(heightNext - heightCurr, len);
			if (hAngle.value < 0)
			{
				hAngle.value += 2 * Math.PI;
			}
		}
	}
	
	public static void calcHVAngleDeg(@Nonnull Coord2DDbl ptCurr, @Nonnull Coord2DDbl ptNext, double heightCurr, double heightNext, @Nonnull SharedDouble hAngle, @Nonnull SharedDouble vAngle)
	{
		calcHVAngleRad(ptCurr.multiply(Math.PI / 180.0), ptNext.multiply(Math.PI / 180.0), heightCurr, heightNext, hAngle, vAngle);
		hAngle.value = hAngle.value * 180 / Math.PI;
		vAngle.value = vAngle.value * 180 / Math.PI;
	}

	@Nonnull
	public static Polygon createCircularPolygonWGS84(double lat, double lon, double radiusMeter, int nPoints)
	{
		CoordinateSystem csys4326 = CoordinateSystemManager.srCreateCSysOrDef(4326);
		CoordinateSystem csys3857 = CoordinateSystemManager.srCreateCSysOrDef(3857);
		Coord2DDbl outPos = CoordinateSystem.convert(csys4326, csys3857, new Coord2DDbl(lon, lat));
		Polygon pg = new Polygon(3857);
		LinearRing lr = new LinearRing(3857, nPoints + 1, false, false);
		pg.addGeometry(lr);
		Coord2DDbl[] ptArr = lr.getPointList();
		double pi2 = Math.PI * 2;
		double angle;
		int i = 0;
		while (i < nPoints)
		{
			angle = i * pi2 / nPoints;
			ptArr[i].x = outPos.x + Math.sin(angle) * radiusMeter;
			ptArr[i].y = outPos.y + Math.cos(angle) * radiusMeter;
			i++;
		}
		ptArr[nPoints] = ptArr[0].clone();
		pg.convCSys(csys3857, csys4326);
		return pg;
	}

	private static double sqrtFix(double sqrtVal, double addVal, double targetVal)
	{
		double o1 = targetVal - (addVal + sqrtVal);
		double o2 = targetVal - (addVal - sqrtVal);
		if (o1 < 0)
			o1 = -o1;
		if (o2 < 0)
			o2 = -o2;
		if (o1 < o2)
			return addVal + sqrtVal;
		else
			return addVal - sqrtVal;
	}

	@Nonnull
	private static Coord2DDbl arcNearest(double x, double y, double h, double k, double r, int cnt)
	{
		double r2 = r * r;
		double thisX = x;
		double thisY = y;
		double tmpX;
		double tmpY;
		double angle;
		while (cnt-- > 0)
		{
			tmpY = sqrtFix(Math.sqrt(r2 - (thisX - h) * (thisX - h)), k, y);
			tmpX = sqrtFix(Math.sqrt(r2 - (thisY - k) * (thisY - k)), h, x);
			angle = Math.atan2(tmpX, tmpY);
			thisX += Math.sin(angle) * (tmpY - thisY);
			thisY += Math.cos(angle) * (tmpX - thisX);
		}
		return new Coord2DDbl(thisX, thisY);
	}
	
	public static int arcToLine(@Nonnull Coord2DDbl pt1, @Nonnull Coord2DDbl pt2, @Nonnull Coord2DDbl pt3, double minDist, @Nonnull List<Coord2DDbl> ptOut)
	{
		//(x – h)^2 + (y – k)^2 = r^2
		//2h(x1 - x2) + 2k(y1 - y2) = x1^2 - x2^2 + y1^2 - y2^2
		//2k = (x1^2 - x2^2 + y1^2 - y2^2 - 2h(x1 - x2)) / (y1 - y2)
		//(x1^2 - x3^2 + y1^2 - y3^2 - 2h(x1 - x3)) / (y1 - y3) = (x1^2 - x2^2 + y1^2 - y2^2 - 2h(x1 - x2)) / (y1 - y2)
		//(x1^2 - x3^2 + y1^2 - y3^2 - 2h(x1 - x3)) * (y1 - y2) = (x1^2 - x2^2 + y1^2 - y2^2 - 2h(x1 - x2)) * (y1 - y3)
		//(x1^2 - x3^2 + y1^2 - y3^2) * (y1 - y2) - 2h(x1 - x3) * (y1 - y2) = (x1^2 - x2^2 + y1^2 - y2^2) * (y1 - y3) - 2h(x1 - x2) * (y1 - y3)
		//(x1^2 - x3^2 + y1^2 - y3^2) * (y1 - y2) - (x1^2 - x2^2 + y1^2 - y2^2) * (y1 - y3) = 2h(x1 - x3) * (y1 - y2) - 2h(x1 - x2) * (y1 - y3)
		//(x1^2 - x3^2 + y1^2 - y3^2) * (y1 - y2) - (x1^2 - x2^2 + y1^2 - y2^2) * (y1 - y3) / (2 * (x1 - x3) * (y1 - y2) - 2 * (x1 - x2) * (y1 - y3)) = h
		Coord2DDbl d13 = pt1.subtract(pt3);
		Coord2DDbl d12 = pt1.subtract(pt2);
		Double c1 = (pt1.x * pt1.x - pt3.x * pt3.x + pt1.y * pt1.y - pt3.y * pt3.y) * d12.y;
		Double c2 = (pt1.x * pt1.x - pt2.x * pt2.x + pt1.y * pt1.y - pt2.y * pt2.y);
		Double c2b = c2 * d13.y;
		Double c3 = 2 * ((d13.x * d12.y) - (d12.x * d13.y));
		Double h = c1 - c2b / c3;
		Double k = (c2 - 2 * h * d12.x) / d12.y / 2;
		Double r = Math.sqrt((pt1.x - h) * (pt1.x - h) + (pt1.y - k) * (pt1.y - k));
		Coord2DDbl d23 = pt2.subtract(pt3);

		int initCnt = ptOut.size();
		ptOut.add(pt1.clone());
		double leng = Math.sqrt(d12.x * d12.x + d12.y * d12.y);
		int ptCnt = (int)Math.floor(leng / minDist);
		int i = 1;
		double di;
		double dcnt = ptCnt;
		while (i < ptCnt)
		{
			di = i;
			ptOut.add(arcNearest(pt1.x + (pt2.x - pt1.x) * di / dcnt, pt1.y + (pt2.y - pt1.y) * di / dcnt, h, k, r, 5));
			i++;
		}
		ptOut.add(pt2.clone());
		leng = Math.sqrt(d23.x * d23.x + d23.y * d23.y);
		ptCnt = (int)Math.floor(leng / minDist);
		dcnt = ptCnt;
		while (i < ptCnt)
		{
			di = i;
			ptOut.add(arcNearest(pt2.x + (pt3.x - pt2.x) * di / dcnt, pt2.y + (pt3.y - pt2.y) * di / dcnt, h, k, r, 5));
			i++;
		}
		ptOut.add(pt3.clone());
		return ptOut.size() - initCnt;
	}

	private static <T> void appendGeojsonList(@Nonnull Iterable<T> coll, @Nonnull StringBuilder sb) throws NoSuchFieldException, InvocationTargetException, IllegalAccessException
	{
		Iterator<T> it = coll.iterator();
		boolean first = true;
		sb.append("{\"type\":\"FeatureCollection\",\"features\":[");
		while (it.hasNext())
		{
			if (first)
			{
				first = false;
			}
			else
			{
				sb.append(",");
			}
			appendGeojson(it.next(), sb);
		}
		sb.append("]}");
	}

	private static void appendGeojsonGeometry(@Nonnull Geometry geom, @Nonnull StringBuilder sb)
	{
		String t = geom.getGeometryType();
		if (t.equals(Geometry.TYPENAME_POINT))
		{
			Coordinate coord = ((Point)geom).getCoordinate();
			sb.append("{\"type\":\"Point\",");
			sb.append("\"coordinates\":[");
			sb.append(coord.x);
			sb.append(",");
			sb.append(coord.y);
			if (!Double.isNaN(coord.z))
			{
				sb.append(",");
				sb.append(coord.getZ());
			}
			sb.append("]}");
		}
		else if (t.equals(Geometry.TYPENAME_POLYGON))
		{
			org.locationtech.jts.geom.Polygon src = (org.locationtech.jts.geom.Polygon)geom;
			Coordinate[] allCoords = src.getCoordinates();
			boolean hasZ = allCoords.length > 0 && !Double.isNaN(allCoords[0].getZ());
//			boolean hasM = allCoords.length > 0 && !Double.isNaN(allCoords[0].getM());
			int i = 0;
			int j = src.getNumGeometries();
			int m;
			int n;
			Geometry igeom;
			sb.append("{\"type\":\"Polygon\",");
			sb.append("\"coordinates\":[");
			while (i < j)
			{
				igeom = src.getGeometryN(i);
				Coordinate[] coords = igeom.getCoordinates();
				if (i > 0) sb.append(",");
				sb.append("[");
				m = 0;
				n = coords.length;
				while (m < n)
				{
					if (m > 0) sb.append(",");
					sb.append("[");
					sb.append(coords[m].x);
					sb.append(",");
					sb.append(coords[m].y);
					if (hasZ)
					{
						sb.append(",");
						sb.append(coords[m].getZ());
					}
					sb.append("]");
					m++;
				}
				sb.append("]");
				i++;
			}
			sb.append("]}");
		}
		else
		{
			System.out.println("GeometryUtil: Unsupported geometry type: "+t);
		}
	}

	private static <T> void appendGeojson(@Nonnull T o, @Nonnull StringBuilder sb) throws NoSuchFieldException, InvocationTargetException, IllegalAccessException
	{
		Class<?> cls = o.getClass();
		Field[] fields = cls.getDeclaredFields();
		Field geomField = null;
		int i = fields.length;
		while (i-- > 0)
		{
			if (fields[i].getType().equals(Geometry.class))
			{
				geomField = fields[i];
				break;
			}
		}
		if (geomField == null)
		{
			System.out.println("No geometry field found");
		}
		else
		{
			FieldGetter<T> getter = new FieldGetter<T>(cls, geomField.getName());
			Geometry geom = (Geometry)getter.get(o);
			if (geom != null)
			{
				sb.append("{\"type\":\"Feature\",\"geometry\":");
				appendGeojsonGeometry(geom, sb);
				sb.append(",\"geometry_name\":");
				JSText.toJSTextDQuote(sb, geomField.getName());
				sb.append(",\"properties\":{");
				boolean found = false;
				i = 0;
				int j = fields.length;
				while (i < j)
				{
					if (fields[i] == geomField)
					{

					}
					else
					{
						Class<?> t = fields[i].getType();
						if (!found)
						{
							found = true;
						}
						else
						{
							sb.append(",");
						}
						JSText.toJSTextDQuote(sb, fields[i].getName());
						sb.append(":");
						if (t.equals(String.class))
						{
							getter = new FieldGetter<T>(cls, fields[i].getName());
							String s = (String)getter.get(o);
							if (s == null)
							{
								sb.append("null");
							}
							else
							{
								JSText.toJSTextDQuote(sb, (String)getter.get(o));
							}
						}
						else if (t.equals(int.class) || t.equals(Integer.class))
						{
							getter = new FieldGetter<T>(cls, fields[i].getName());
							Integer v = (Integer)getter.get(o);
							if (v == null)
							{
								sb.append("null");
							}
							else
							{
								sb.append(v);
							}
						}
						else if (t.equals(double.class) || t.equals(Double.class))
						{
							getter = new FieldGetter<T>(cls, fields[i].getName());
							Double v = (Double)getter.get(o);
							if (v == null)
							{
								sb.append("null");
							}
							else
							{
								sb.append(v);
							}
						}
						else if (t.equals(Timestamp.class))
						{
							getter = new FieldGetter<T>(cls, fields[i].getName());
							Timestamp ts = (Timestamp)getter.get(o);
							if (ts == null)
							{
								sb.append("null");
							}
							else
							{
								JSText.toJSTextDQuote(sb, DateTimeUtil.toString(ts, "yyyy-MM-dd HH:mm:ss.fffffffff"));
							}
						}
						else
						{
							System.out.println("GeometryUtil.appendGeojson: Unknown properties type: "+t.toString());
							sb.append("null");
						}
					}
					i++;
				}
				sb.append("}");
				sb.append("}");
			}
			else
			{
				System.out.println("GeometryUtil.appendGeojson: Get Geometry = null");
			}
		}
	}

	@Nullable
	public static <T> String toGeojsonList(@Nonnull Iterable<T> o)
	{
		try
		{
			StringBuilder sb = new StringBuilder();
			appendGeojsonList(o, sb);
			return sb.toString();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			return null;
		}
	}

	@Nullable
	public static <T> String toGeojson(@Nonnull T o)
	{
		try
		{
			StringBuilder sb = new StringBuilder();
			appendGeojson(o, sb);
			return sb.toString();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			return null;
		}
	}

	@Nullable
	public static Geometry csysConv(@Nonnull Geometry geom, @Nonnull CoordinateSystem csysSrc, @Nonnull CoordinateSystem csysDest)
	{
		Vector2D vec = toVector2D(geom);
		if (vec == null)
		{
			return null;
		}
		vec.convCSys(csysSrc, csysDest);
		return fromVector2D(vec);
	}
}
