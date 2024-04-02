package org.sswr.util.math.geometry;

import org.sswr.util.math.Coord2DDbl;
import org.sswr.util.math.CoordinateSystem;
import org.sswr.util.math.RectAreaDbl;

public abstract class Vector2D
{
	public enum VectorType
	{
		Unknown,
		Point,
		LineString,
		Polygon,
		MultiPoint,
		Polyline, //MultiLineString
		MultiPolygon,
		GeometryCollection,
		CircularString,
		CompoundCurve,
		CurvePolygon,
		MultiCurve,
		MultiSurface,
		Curve,
		Surface,
		PolyhedralSurface,
		Tin,
		Triangle,
		LinearRing,

		Image,
		String,
		Ellipse,
		PieArea
	}

	protected int srid;

	public Vector2D(int srid)
	{
		this.srid = srid;
	}

	public abstract VectorType getVectorType();
	public abstract Coord2DDbl getCenter();
	public abstract Vector2D clone();
	public abstract RectAreaDbl getBounds();
	public abstract double calBoundarySqrDistance(Coord2DDbl pt, Coord2DDbl nearPt);
	public double calSqrDistance(Coord2DDbl pt, Coord2DDbl nearPt)
	{
		if (this.insideOrTouch(pt))
		{
			nearPt.x = pt.x;
			nearPt.y = pt.y;
			return 0;
		}
		return this.calBoundarySqrDistance(pt, nearPt);
	}

	public abstract double calArea();
	public abstract boolean joinVector(Vector2D vec);

	public boolean hasZ()
	{
		return false;
	}

	public boolean hasM()
	{
		return false;
	}

	public abstract void convCSys(CoordinateSystem srcCSys, CoordinateSystem destCSys);
	public abstract boolean equals(Vector2D vec, boolean sameTypeOnly, boolean nearlyVal);
	public abstract boolean insideOrTouch(Coord2DDbl coord);

	public int getSRID()
	{
		return this.srid;
	}

	public void setSRID(int srid)
	{
		this.srid = srid;
	}
}
