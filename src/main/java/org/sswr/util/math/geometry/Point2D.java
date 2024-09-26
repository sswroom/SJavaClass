package org.sswr.util.math.geometry;

import java.util.Objects;

import org.sswr.util.data.DataTools;
import org.sswr.util.math.Coord2DDbl;
import org.sswr.util.math.CoordinateSystem;
import org.sswr.util.math.RectAreaDbl;

import jakarta.annotation.Nonnull;

public class Point2D extends Vector2D
{
	protected Coord2DDbl pos;

	public Point2D(int srid, double x, double y)
	{
		super(srid);
		this.pos = new Coord2DDbl(x, y);
	}

	public Point2D(int srid, @Nonnull Coord2DDbl pos)
	{
		super(srid);
		this.pos = pos;
	}
	@Override
	public int hashCode() {
		return Objects.hash(pos);
	}

	@Override
	public String toString() {
		return DataTools.toObjectString(this);
	}

	@Nonnull
	public VectorType getVectorType()
	{
		return VectorType.Point;
	}

	@Nonnull
	public Coord2DDbl getCenter()
	{
		return this.pos;
	}

	@Nonnull
	public Vector2D clone()
	{
		return new Point2D(this.srid, this.pos.clone());
	}

	@Nonnull
	public RectAreaDbl getBounds()
	{
		return new RectAreaDbl(pos.clone(), pos.clone());
	}

	public double calBoundarySqrDistance(@Nonnull Coord2DDbl pt, @Nonnull Coord2DDbl nearPt)
	{
		double xDiff = pt.x - this.pos.x;
		double yDiff = pt.y - this.pos.y;
		nearPt.x = this.pos.x;
		nearPt.y = this.pos.y;
		return xDiff * xDiff + yDiff * yDiff;
	}

	public double calArea()
	{
		return 0;
	}

	public boolean joinVector(@Nonnull Vector2D vec)
	{
		return false;
	}

	public void convCSys(@Nonnull CoordinateSystem srcCSys, @Nonnull CoordinateSystem destCSys)
	{
		this.pos = CoordinateSystem.convert(srcCSys, destCSys, this.pos);
		this.srid = destCSys.getSRID();
	}

	@Override
	public boolean equals(@Nonnull Vector2D vec, boolean sameTypeOnly, boolean nearlyVal) {
		if (vec == this)
			return true;
		if (!(vec instanceof Point2D)) {
			return false;
		}
		Point2D point = (Point2D)vec;
		if (nearlyVal)
			return this.srid == point.srid && this.pos.equalsNearly(point.pos);
		else
			return this.srid == point.srid && this.pos.equals(point.pos);
	}

	public boolean insideOrTouch(@Nonnull Coord2DDbl coord)
	{
		return this.pos.equals(coord);
	}
}
