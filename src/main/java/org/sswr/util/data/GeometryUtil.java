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
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.sswr.util.math.Coord2DDbl;
import org.sswr.util.math.CoordinateSystem;
import org.sswr.util.math.CoordinateSystemManager;
import org.sswr.util.math.geometry.LineString;
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

public class GeometryUtil
{
	public static String toWKT(Geometry geometry)
	{
		org.sswr.util.math.WKTWriter writer = new org.sswr.util.math.WKTWriter();
		return writer.generateWKT(toVector2D(geometry));
	}

	public static Geometry createPointZ(double x, double y, double z, int srid)
	{
		GeometryFactory factory = new GeometryFactory(new PrecisionModel(), srid);
		return factory.createPoint(new Coordinate(x, y, z));
	}

	public static Geometry createLineStringZ(List<Double> points, int srid)
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

	public static Geometry fromVector2D(Vector2D vec)
	{
		if (vec == null)
		{
			return null;
		}
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
		else if (vec.getVectorType() == VectorType.Polyline)
		{
			Polyline pl = (Polyline)vec;
			int[] ptOfstArr = pl.getPtOfstList();
			org.locationtech.jts.geom.LineString[] lineStrings = new org.locationtech.jts.geom.LineString[ptOfstArr.length];
			Coord2DDbl[] ptArr = pl.getPointList();
			double[] zArr = pl.getZList();
			double[] mArr = pl.getMList();
			int ptEndOfst = ptArr.length;
			int i = ptOfstArr.length;
			int j;
			int k;
			Coordinate[] coordinates;
			while (i-- > 0)
			{
				j = ptEndOfst;
				k = ptOfstArr[i];
				coordinates = new Coordinate[ptEndOfst - k];
				if (zArr != null)
				{
					if (mArr != null)
					{
						while (j-- > k)
						{
							coordinates[j - k] = new CoordinateXYZM(ptArr[j].x, ptArr[j].y, zArr[j], mArr[j]);
						}
					}
					else
					{
						while (j-- > k)
						{
							coordinates[j - k] = new Coordinate(ptArr[j].x, ptArr[j].y, zArr[j]);
						}
					}
				}
				else
				{
					while (j-- > k)
					{
						coordinates[j - k] = new Coordinate(ptArr[j].x, ptArr[j].y);
					}
				}
				lineStrings[i] = factory.createLineString(coordinates);
				ptEndOfst = k;
			}
			return factory.createMultiLineString(lineStrings);
		}
		else if (vec.getVectorType() == VectorType.Polygon)
		{
			Polygon pg = (Polygon)vec;
			int[] ptOfstArr = pg.getPtOfstList();
			LinearRing[] lineStrings = new LinearRing[ptOfstArr.length];
			Coord2DDbl[] ptArr = pg.getPointList();
			double[] zArr = pg.getZList();
			double[] mArr = pg.getMList();
			int ptEndOfst = ptArr.length;
			int i = ptOfstArr.length;
			int j;
			int k;
			Coordinate[] coordinates;
			while (i-- > 0)
			{
				j = ptEndOfst;
				k = ptOfstArr[i];
				coordinates = new Coordinate[ptEndOfst - k];
				if (zArr != null)
				{
					if (mArr != null)
					{
						while (j-- > k)
						{
							coordinates[j - k] = new CoordinateXYZM(ptArr[j].x, ptArr[j].y, zArr[j], mArr[j]);
						}
					}
					else
					{
						while (j-- > k)
						{
							coordinates[j - k] = new Coordinate(ptArr[j].x, ptArr[j].y, zArr[j]);
						}
					}
				}
				else
				{
					while (j-- > k)
					{
						coordinates[j - k] = new Coordinate(ptArr[j].x, ptArr[j].y);
					}
				}
				lineStrings[i] = factory.createLinearRing(coordinates);
				ptEndOfst = k;
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

	public static Vector2D toVector2D(Geometry geometry)
	{
		if (geometry == null)
		{
			return null;
		}
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
				if (hasZ)
				{
					zList[i] = coords[i].getZ();
				}
				if (hasM)
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
			Polyline dest = new Polyline(src.getSRID(), src.getNumGeometries(), src.getNumPoints(), hasZ, hasM);
			Coord2DDbl[] coordd = dest.getPointList();
			double[] zList = dest.getZList();
			double[] mList = dest.getMList();
			int[] ofstd = dest.getPtOfstList();
			int i = 0;
			int j = src.getNumGeometries();
			int k = 0;
			int m;
			int n;
			Geometry geom;
			while (i < j)
			{
				ofstd[i] = k;
				geom = src.getGeometryN(i);
				Coordinate[] coords = geom.getCoordinates();
				m = 0;
				n = coords.length;
				while (m < n)
				{
					coordd[k + m] = new Coord2DDbl(coords[m].x, coords[m].y);
					if (hasZ)
					{
						zList[k + m] = coords[m].getZ();
					}
					if (hasM)
					{
						mList[k + m] = coords[m].getM();
					}
					m++;
				}
				k += n;
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
			Polygon dest = new Polygon(src.getSRID(), src.getNumGeometries(), src.getNumPoints(), hasZ, hasM);
			Coord2DDbl[] coordd = dest.getPointList();
			double[] zList = dest.getZList();
			double[] mList = dest.getMList();
			int[] ofstd = dest.getPtOfstList();
			int i = 0;
			int j = src.getNumGeometries();
			int k = 0;
			int m;
			int n;
			Geometry geom;
			while (i < j)
			{
				ofstd[i] = k;
				geom = src.getGeometryN(i);
				Coordinate[] coords = geom.getCoordinates();
				m = 0;
				n = coords.length;
				while (m < n)
				{
					coordd[k + m] = new Coord2DDbl(coords[m].x, coords[m].y);
					if (hasZ)
					{
						zList[k + m] = coords[m].getZ();
					}
					if (hasM)
					{
						mList[k + m] = coords[m].getM();
					}
					m++;
				}
				k += n;
				i++;
			}
			return dest;
		}
		case Geometry.TYPENAME_MULTIPOLYGON:
		{
			org.locationtech.jts.geom.MultiPolygon src = (org.locationtech.jts.geom.MultiPolygon)geometry;
			Coordinate[] allCoords = src.getCoordinates();
			boolean hasZ = allCoords.length > 0 && !Double.isNaN(allCoords[0].getZ());
			boolean hasM = allCoords.length > 0 && !Double.isNaN(allCoords[0].getM());
			MultiPolygon dest = new MultiPolygon(src.getSRID(), hasZ, hasM);
			int i = 0;
			int j = src.getNumGeometries();
			while (i < j)
			{
				dest.addGeometry((Polygon)toVector2D(src.getGeometryN(i)));
				i++;
			}
			return dest;
		}
		default:
			System.out.println("GeometryUtil.toVector2D: Unsupported type: "+geometry.getGeometryType());
			return null;
		}
	}

	public static double calcMaxDistanceFromCenter(Geometry geometry, DistanceUnit unit)
	{
		return calcMaxDistanceFromPoint(geometry.getCentroid(), geometry, unit);
	}

	public static double calcMaxDistanceFromPoint(Point point, Geometry geometry, DistanceUnit unit)
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
			return csys.calSurfaceDistanceXY(ccoord.x, ccoord.y, maxCoord.x, maxCoord.y, unit);
		}
	}

	public static Coord2DDbl mercatorToProject(double lat, double lon)
	{
		double a = 6378137.0;
		return new Coord2DDbl(lon * a, a * Math.log(Math.tan(Math.PI * 0.25 + lat * 0.5)));
	}
	
	public static void calcHVAngleRad(Coord2DDbl ptCurr, Coord2DDbl ptNext, double heightCurr, double heightNext, SharedDouble hAngle, SharedDouble vAngle)
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
	
	public static void calcHVAngleDeg(Coord2DDbl ptCurr, Coord2DDbl ptNext, double heightCurr, double heightNext, SharedDouble hAngle, SharedDouble vAngle)
	{
		calcHVAngleRad(ptCurr.multiply(Math.PI / 180.0), ptNext.multiply(Math.PI / 180.0), heightCurr, heightNext, hAngle, vAngle);
		hAngle.value = hAngle.value * 180 / Math.PI;
		vAngle.value = vAngle.value * 180 / Math.PI;
	}

	public static Polygon createCircularPolygonWGS84(double lat, double lon, double radiusMeter, int nPoints)
	{
		CoordinateSystem csys4326 = CoordinateSystemManager.srCreateCSys(4326);
		CoordinateSystem csys3857 = CoordinateSystemManager.srCreateCSys(3857);
		SharedDouble outX = new SharedDouble();
		SharedDouble outY = new SharedDouble();
		SharedDouble outZ = new SharedDouble();
		CoordinateSystem.convertXYZ(csys4326, csys3857, lon, lat, 0, outX, outY, outZ);
		Polygon pg = new Polygon(3857, 1, nPoints + 1, false, false);
		Coord2DDbl[] ptArr = pg.getPointList();
		double pi2 = Math.PI * 2;
		double angle;
		int i = 0;
		while (i < nPoints)
		{
			angle = i * pi2 / nPoints;
			ptArr[i].x = outX.value + Math.sin(angle) * radiusMeter;
			ptArr[i].y = outY.value + Math.cos(angle) * radiusMeter;
			i++;
		}
		ptArr[nPoints] = ptArr[0].clone();
		pg.convCSys(csys3857, csys4326);
		return pg;
	}

	private static <T> void appendGeojsonList(Iterable<T> coll, StringBuilder sb) throws NoSuchFieldException, InvocationTargetException, IllegalAccessException
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

	private static void appendGeojsonGeometry(Geometry geom, StringBuilder sb)
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
		else
		{
			System.out.println("Unsupported geometry type: "+t);
		}
	}
	private static <T> void appendGeojson(T o, StringBuilder sb) throws NoSuchFieldException, InvocationTargetException, IllegalAccessException
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
	}

	public static <T> String toGeojsonList(Iterable<T> o)
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

	public static <T> String toGeojson(T o)
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
}
