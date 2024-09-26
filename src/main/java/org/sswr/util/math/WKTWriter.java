package org.sswr.util.math;

import java.util.Iterator;

import org.sswr.util.math.geometry.CompoundCurve;
import org.sswr.util.math.geometry.CurvePolygon;
import org.sswr.util.math.geometry.GeometryCollection;
import org.sswr.util.math.geometry.LineString;
import org.sswr.util.math.geometry.LinearRing;
import org.sswr.util.math.geometry.MultiPolygon;
import org.sswr.util.math.geometry.MultiSurface;
import org.sswr.util.math.geometry.Point2D;
import org.sswr.util.math.geometry.PointM;
import org.sswr.util.math.geometry.PointZ;
import org.sswr.util.math.geometry.PointZM;
import org.sswr.util.math.geometry.Polygon;
import org.sswr.util.math.geometry.Polyline;
import org.sswr.util.math.geometry.Vector2D;
import org.sswr.util.math.geometry.Vector2D.VectorType;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class WKTWriter implements VectorTextWriter
{
	private String lastError;
	private boolean reverseAxis;
	
	private static void appendLineString(@Nonnull StringBuilder sb, @Nonnull LineString pl, boolean reverseAxis)
	{
		sb.append('(');
		int nPoint;
		Coord2DDbl []pointList = pl.getPointList();
		double []zArr = pl.getZList();
		double []mArr = pl.getMList();
		int i;
		nPoint = pointList.length;
		if (zArr != null || mArr != null)
		{
			if (zArr != null && mArr != null)
			{
				i = 0;
				while (i < nPoint)
				{
					if (i > 0) sb.append(',');
					if (reverseAxis)
					{
						sb.append(pointList[i].y);
						sb.append(' ');
						sb.append(pointList[i].x);
					}
					else
					{
						sb.append(pointList[i].x);
						sb.append(' ');
						sb.append(pointList[i].y);
					}
					sb.append(' ');
					sb.append(zArr[i]);
					sb.append(' ');
					sb.append(mArr[i]);
					i++;
				}
			}
			else if (mArr != null)
			{
				i = 0;
				while (i < nPoint)
				{
					if (i > 0) sb.append(',');
					if (reverseAxis)
					{
						sb.append(pointList[i].y);
						sb.append(' ');
						sb.append(pointList[i].x);
					}
					else
					{
						sb.append(pointList[i].x);
						sb.append(' ');
						sb.append(pointList[i].y);
					}
					sb.append(" NaN ");
					sb.append(mArr[i]);
					i++;
				}
			}
			else if (zArr != null)
			{
				i = 0;
				while (i < nPoint)
				{
					if (i > 0) sb.append(',');
					if (reverseAxis)
					{
						sb.append(pointList[i].y);
						sb.append(' ');
						sb.append(pointList[i].x);
					}
					else
					{
						sb.append(pointList[i].x);
						sb.append(' ');
						sb.append(pointList[i].y);
					}
					sb.append(' ');
					sb.append(zArr[i]);
					i++;
				}
			}
		}
		else
		{
			if (reverseAxis)
			{
				i = 0;
				while (i < nPoint)
				{
					if (i > 0) sb.append(',');
					sb.append(pointList[i].y);
					sb.append(' ');
					sb.append(pointList[i].x);
					i++;
				}
			}
			else
			{
				i = 0;
				while (i < nPoint)
				{
					if (i > 0) sb.append(',');
					sb.append(pointList[i].x);
					sb.append(' ');
					sb.append(pointList[i].y);
					i++;
				}
			}
		}
		sb.append(')');
	}

	private static void appendPolygon(@Nonnull StringBuilder sb, @Nonnull Polygon pg, boolean reverseAxis)
	{
		Iterator<LinearRing> it = pg.iterator();
		boolean found = false;
		sb.append('(');
		while (it.hasNext())
		{
			if (found)
				sb.append(',');
			appendLineString(sb, it.next(), reverseAxis);
			found = true;
		}
		sb.append(')');
	}

	private static void appendPolyline(@Nonnull StringBuilder sb, @Nonnull Polyline pl, boolean reverseAxis)
	{
		sb.append('(');
		Iterator<LineString> it = pl.iterator();
		boolean found = false;
		while (it.hasNext())
		{
			if (found) sb.append(',');
			appendLineString(sb, it.next(), reverseAxis);
			found = true;
		}
		sb.append(')');
	}

	private static void appendCompoundCurve(@Nonnull StringBuilder sb, @Nonnull CompoundCurve cc, boolean reverseAxis)
	{
		sb.append('(');
		LineString pl;
		int i = 0;
		int j = cc.getCount();
		while (i < j)
		{
			if (i > 0) sb.append(',');
			pl = cc.getItem(i);
			if (pl.getVectorType() == VectorType.CircularString)
			{
				sb.append("CIRCULARSTRING");
			}
			appendLineString(sb, pl, reverseAxis);
			i++;
		}
		sb.append(')');
	}

	private static void appendCurvePolygon(@Nonnull StringBuilder sb, @Nonnull CurvePolygon cpg, boolean reverseAxis)
	{
		sb.append('(');
		Vector2D geometry;
		int i = 0;
		int j = cpg.getCount();
		while (i < j)
		{
			if (i > 0) sb.append(',');
			geometry = cpg.getItem(i);
			VectorType t = geometry.getVectorType();
			if (t == VectorType.LineString)
			{
				appendLineString(sb, (LineString)geometry, reverseAxis);
			}
			else if (t == VectorType.CircularString)
			{
				sb.append("CIRCULARSTRING");
				appendLineString(sb, (LineString)geometry, reverseAxis);
			}
			else if (t == VectorType.CompoundCurve)
			{
				sb.append("COMPOUNDCURVE");
				appendCompoundCurve(sb, (CompoundCurve)geometry, reverseAxis);
			}
			i++;
		}
		sb.append(')');
	}

	private static void appendMultiPolygon(@Nonnull StringBuilder sb, @Nonnull MultiPolygon mpg, boolean reverseAxis)
	{
		Iterator<Polygon> it = mpg.iterator();
		boolean found = false;
		sb.append('(');
		while (it.hasNext())
		{
			if (found) sb.append(',');
			appendPolygon(sb, it.next(), reverseAxis);
			found = true;
		}
		sb.append(')');
	}

	private static void appendMultiSurface(@Nonnull StringBuilder sb, @Nonnull MultiSurface ms, boolean reverseAxis)
	{
		sb.append('(');
		Vector2D geometry;
		int i = 0;
		int j = ms.getCount();
		while (i < j)
		{
			if (i > 0) sb.append(',');
			geometry = ms.getItem(i);
			VectorType t = geometry.getVectorType();
			if (t == VectorType.CurvePolygon)
			{
				sb.append("CURVEPOLYGON");
				appendCurvePolygon(sb, (CurvePolygon)geometry, reverseAxis);
			}
			else if (t == VectorType.Polygon)
			{
				sb.append("POLYGON");
				appendPolygon(sb, (Polygon)geometry, reverseAxis);
			}
			else
			{
				System.out.println("Unknown type in multisurface: "+t);
			}
			i++;
		}
		sb.append(')');
	}
	
	private boolean appendGeometryCollection(@Nonnull StringBuilder sb, @Nonnull GeometryCollection geoColl)
	{
		sb.append('(');
		Vector2D geometry;
		Iterator<Vector2D> it = geoColl.iterator();
		boolean found = false;
		while (it.hasNext())
		{
			if (found) sb.append(',');
			geometry = it.next();
			if (!toText(sb, geometry))
				return false;
			found = true;
		}
		sb.append(')');
		return true;
	}

	public WKTWriter()
	{
		this.lastError = null;
		this.reverseAxis = false;
	}

	@Nonnull
	public String getWriterName()
	{
		return "Well Known Text (WKT)";
	}

	public boolean toText(@Nonnull StringBuilder sb, @Nonnull Vector2D vec)
	{
		switch (vec.getVectorType())
		{
		case Point:
			sb.append("POINT(");
			if (vec.hasZ())
			{
				PointZ pt = (PointZ)vec;
				Coord2DDbl center = pt.getCenter();
				if (this.reverseAxis)
				{
					sb.append(center.y);
					sb.append(' ');
					sb.append(center.x);
				}
				else
				{
					sb.append(center.x);
					sb.append(' ');
					sb.append(center.y);
				}
				sb.append(' ');
				sb.append(pt.getZ());
				if (vec.hasM())
				{
					sb.append(' ');
					sb.append(((PointZM)pt).getM());
				}
			}
			else
			{
				Point2D pt = (Point2D)vec;
				Coord2DDbl coord;
				coord = pt.getCenter();
				if (this.reverseAxis)
				{
					sb.append(coord.y);
					sb.append(' ');
					sb.append(coord.x);
				}
				else
				{
					sb.append(coord.x);
					sb.append(' ');
					sb.append(coord.y);
				}
				if (vec.hasM())
				{
					sb.append(" NAN ");
					sb.append(((PointM)pt).getM());
				}
			}
			sb.append(")");
			return true;
		case Polygon:
			sb.append("POLYGON");
			appendPolygon(sb, (Polygon)vec, this.reverseAxis);
			return true;
		case Polyline:
			sb.append("MULTILINESTRING");
			appendPolyline(sb, (Polyline)vec, this.reverseAxis);
			return true;
		case MultiPolygon:
			sb.append("MULTIPOLYGON");
			appendMultiPolygon(sb, (MultiPolygon)vec, this.reverseAxis);
			return true;
		case LineString:
			sb.append("LINESTRING");
			appendLineString(sb, (LineString)vec, this.reverseAxis);
			return true;
		case CircularString:
			sb.append("CIRCULARSTRING");
			appendLineString(sb, (LineString)vec, this.reverseAxis);
			return true;
		case CompoundCurve:
			sb.append("COMPOUNDCURVE");
			appendCompoundCurve(sb, (CompoundCurve)vec, this.reverseAxis);
			return true;
		case CurvePolygon:
			sb.append("CURVEPOLYGON");
			appendCurvePolygon(sb, (CurvePolygon)vec, this.reverseAxis);
			return true;
		case MultiSurface:
			sb.append("MULTISURFACE");
			appendMultiSurface(sb, (MultiSurface)vec, this.reverseAxis);
			return true;
		case GeometryCollection:
			sb.append("GEOMETRYCOLLECTION");
			return appendGeometryCollection(sb, (GeometryCollection)vec);
		case MultiPoint:
		case MultiCurve:
		case Curve:
		case Surface:
		case PolyhedralSurface:
		case Tin:
		case Triangle:
		case Image:
		case String:
		case Ellipse:
		case PieArea:
		case Unknown:
		default:
			sb.append("Unsupported vector type");
			this.lastError = "Unsupported vector type";
			return false;
		}
	}

	@Nullable
	public String generateWKT(@Nonnull Vector2D vec)
	{
		StringBuilder sb = new StringBuilder();
		if (toText(sb, vec))
		{
			return sb.toString();
		}
		return null;
	}

	@Nullable
	public String getLastError()
	{
		return this.lastError;
	}

	public void setReverseAxis(boolean reverseAxis)
	{
		this.reverseAxis = reverseAxis;
	}
}
