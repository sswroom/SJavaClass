package org.sswr.util.math.geometry;

import org.sswr.util.math.Coord2DDbl;
import org.sswr.util.math.CoordinateSystem;
import org.sswr.util.math.RectAreaDbl;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

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

	@Nonnull
	public abstract VectorType getVectorType();
	@Nonnull
	public abstract Coord2DDbl getCenter();
	@Nonnull
	public abstract Vector2D clone();
	@Nonnull
	public abstract RectAreaDbl getBounds();
	public abstract double calBoundarySqrDistance(@Nonnull Coord2DDbl pt, @Nonnull Coord2DDbl nearPt);
	public double calSqrDistance(@Nonnull Coord2DDbl pt, @Nonnull Coord2DDbl nearPt)
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
	public abstract boolean joinVector(@Nonnull Vector2D vec);

	public boolean hasZ()
	{
		return false;
	}

	public boolean hasM()
	{
		return false;
	}

	public abstract void convCSys(@Nonnull CoordinateSystem srcCSys, @Nonnull CoordinateSystem destCSys);
	public abstract boolean equals(@Nonnull Vector2D vec, boolean sameTypeOnly, boolean nearlyVal);
	public abstract boolean insideOrTouch(@Nonnull Coord2DDbl coord);
	public boolean hasCurve() { return false; }
	public @Nullable Vector2D toSimpleShape() { return this.clone(); }

	public int getSRID()
	{
		return this.srid;
	}

	public void setSRID(int srid)
	{
		this.srid = srid;
	}
}
