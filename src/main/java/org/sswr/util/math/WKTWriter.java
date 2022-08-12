package org.sswr.util.math;

import org.sswr.util.math.geometry.CompoundCurve;
import org.sswr.util.math.geometry.CurvePolygon;
import org.sswr.util.math.geometry.LineString;
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

public class WKTWriter implements VectorTextWriter
{
	private String lastError;
	
	private void appendLineString(StringBuilder sb, LineString pl)
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
					sb.append(pointList[i].x);
					sb.append(' ');
					sb.append(pointList[i].y);
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
					sb.append(pointList[i].x);
					sb.append(' ');
					sb.append(pointList[i].y);
					sb.append(" NULL ");
					sb.append(mArr[i]);
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
					sb.append(' ');
					sb.append(zArr[i]);
					i++;
				}
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
		sb.append(')');
	}

	private void appendPolygon(StringBuilder sb, Polygon pg)
	{
		int nPtOfst;
		int nPoint;
		int []ptOfstList = pg.getPtOfstList();
		Coord2DDbl []pointList = pg.getPointList();
		nPtOfst = ptOfstList.length;
		nPoint = pointList.length;
		int i;
		int j;
		int k;
		k = 0;
		i = 0;
		j = nPtOfst - 1;
		sb.append('(');
		while (i < j)
		{
			sb.append('(');
			while (k < ptOfstList[i + 1])
			{
				sb.append(pointList[k].x);
				sb.append(' ');
				sb.append(pointList[k].y);
				k++;
				if (k < ptOfstList[i + 1])
				{
					sb.append(',');
				}
			}
			sb.append(')');
			sb.append(',');
			i++;
		}
		sb.append('(');
		while (k < nPoint)
		{
			sb.append(pointList[k].x);
			sb.append(' ');
			sb.append(pointList[k].y);
			k++;
			if (k < nPoint)
			{
				sb.append(',');
			}
		}
		sb.append(')');
		sb.append(')');
	}

	private void appendPolyline(StringBuilder sb, Polyline pl)
	{
		sb.append('(');
		int nPtOfst;
		int nPoint;
		int []ptOfstList = pl.getPtOfstList();
		Coord2DDbl []pointList = pl.getPointList();
		nPtOfst = ptOfstList.length;
		nPoint = pointList.length;
		int i;
		int j;
		int k;
		k = 0;
		i = 0;
		j = nPtOfst - 1;
		while (i < j)
		{
			sb.append('(');
			while (k < ptOfstList[i + 1])
			{
				sb.append(pointList[k].x);
				sb.append(' ');
				sb.append(pointList[k].y);
				k++;
				if (k < ptOfstList[i + 1])
				{
					sb.append(',');
				}
			}
			sb.append(')');
			sb.append(',');
			i++;
		}
		sb.append('(');
		while (k < nPoint)
		{
			sb.append(pointList[k].x);
			sb.append(' ');
			sb.append(pointList[k].y);
			k++;
			if (k < nPoint)
			{
				sb.append(',');
			}
		}
		sb.append(')');
		sb.append(')');
	}

	private void appendPolyline3D(StringBuilder sb, Polyline pl)
	{
		sb.append('(');
		int nPtOfst;
		int nPoint;
		int []ptOfstList = pl.getPtOfstList();
		Coord2DDbl []pointList = pl.getPointList();
		double []zList = pl.getZList();
		nPtOfst = ptOfstList.length;
		nPoint = pointList.length;
		int i;
		int j;
		int k;
		k = 0;
		i = 0;
		j = nPtOfst - 1;
		while (i < j)
		{
			sb.append('(');
			while (k < ptOfstList[i + 1])
			{
				sb.append(pointList[k].x);
				sb.append(' ');
				sb.append(pointList[k].y);
				sb.append(' ');
				sb.append(zList[k]);
				k++;
				if (k < ptOfstList[i + 1])
				{
					sb.append(',');
				}
			}
			sb.append(')');
			sb.append(',');
			i++;
		}
		sb.append('(');
		while (k < nPoint)
		{
			sb.append(pointList[k].x);
			sb.append(' ');
			sb.append(pointList[k].y);
			sb.append(' ');
			sb.append(zList[k]);
			k++;
			if (k < nPoint)
			{
				sb.append(',');
			}
		}
		sb.append(')');
		sb.append(')');
	}

	private void appendCompoundCurve(StringBuilder sb, CompoundCurve cc)
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
			appendLineString(sb, pl);
			i++;
		}
		sb.append(')');
	}

	void appendCurvePolygon(StringBuilder sb, CurvePolygon cpg)
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
				appendLineString(sb, (LineString)geometry);
			}
			else if (t == VectorType.CircularString)
			{
				sb.append("CIRCULARSTRING");
				appendLineString(sb, (LineString)geometry);
			}
			else if (t == VectorType.CompoundCurve)
			{
				sb.append("COMPOUNDCURVE");
				appendCompoundCurve(sb, (CompoundCurve)geometry);
			}
			i++;
		}
		sb.append(')');
	}

	void appendMultiSurface(StringBuilder sb, MultiSurface ms)
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
				appendCurvePolygon(sb, (CurvePolygon)geometry);
			}
			else if (t == VectorType.Polygon)
			{
				sb.append("POLYGON");
				appendPolygon(sb, (Polygon)geometry);
			}
			i++;
		}
		sb.append(')');
	}

	public WKTWriter()
	{
		this.lastError = null;
	}

	public String getWriterName()
	{
		return "Well Known Text (WKT)";
	}

	public boolean toText(StringBuilder sb, Vector2D vec)
	{
		if (vec == null)
		{
			this.lastError = "Input vector is null";
			return false;
		}
		switch (vec.getVectorType())
		{
		case Point:
			sb.append("POINT(");
			if (vec.hasZ())
			{
				PointZ pt = (PointZ)vec;
				Coord2DDbl center = pt.getCenter();
				sb.append(center.x);
				sb.append(' ');
				sb.append(center.y);
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
				sb.append(coord.x);
				sb.append(' ');
				sb.append(coord.y);
				if (vec.hasM())
				{
					sb.append(" NULL ");
					sb.append(((PointM)pt).getM());
				}
			}
			sb.append(")");
			return true;
		case Polygon:
			sb.append("POLYGON");
			appendPolygon(sb, (Polygon)vec);
			return true;
		case Polyline:
			sb.append("MULTILINESTRING");
			{
				Polyline pl = (Polyline)vec;
				if (pl.hasZ())
				{
					appendPolyline3D(sb, pl);
				}
				else
				{
					appendPolyline(sb, pl);
				}
			}
			return true;
		case MultiPolygon:
			sb.append("MULTIPOLYGON");
			{
				MultiPolygon mpg = (MultiPolygon)vec;
				int i = 0;
				int j = mpg.getCount();
				sb.append('(');
				while (i < j)
				{
					if (i > 0)
					{
						sb.append(',');
					}
					appendPolygon(sb, mpg.getItem(i));
					i++;
				}
				sb.append(')');
			}
			return true;
		case LineString:
			sb.append("LINESTRING");
			appendLineString(sb, (LineString)vec);
			return true;
		case CircularString:
			sb.append("CIRCULARSTRING");
			appendLineString(sb, (LineString)vec);
			return true;
		case CompoundCurve:
			sb.append("COMPOUNDCURVE");
			appendCompoundCurve(sb, (CompoundCurve)vec);
			return true;
		case CurvePolygon:
			sb.append("CURVEPOLYGON");
			appendCurvePolygon(sb, (CurvePolygon)vec);
			return true;
		case MultiSurface:
			sb.append("MULTISURFACE");
			appendMultiSurface(sb, (MultiSurface)vec);
			return true;
		case MultiPoint:
		case GeometryCollection:
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

	public String generateWKT(Vector2D vec)
	{
		StringBuilder sb = new StringBuilder();
		if (toText(sb, vec))
		{
			return sb.toString();
		}
		return null;
	}

	public String getLastError()
	{
		return this.lastError;
	}
}
