package org.sswr.util.math.geometry;

import java.util.Objects;

import org.sswr.util.data.DataTools;
import org.sswr.util.data.SharedDouble;
import org.sswr.util.math.Coord2DDbl;
import org.sswr.util.math.CoordinateSystem;
import org.sswr.util.math.RectAreaDbl;

public class Point2D extends Vector2D
{
	protected Coord2DDbl pos;

	public Point2D(int srid, double x, double y)
	{
		super(srid);
		this.pos = new Coord2DDbl(x, y);
	}

	public Point2D(int srid, Coord2DDbl pos)
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

	public VectorType getVectorType()
	{
		return VectorType.Point;
	}

	public Coord2DDbl getCenter()
	{
		return this.pos;
	}

	public Vector2D clone()
	{
		return new Point2D(this.srid, this.pos.clone());
	}

	public RectAreaDbl getBounds()
	{
		return new RectAreaDbl(pos.clone(), pos.clone());
	}

	public double calBoundarySqrDistance(Coord2DDbl pt, Coord2DDbl nearPt)
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

	public boolean joinVector(Vector2D vec)
	{
		return false;
	}

	public void convCSys(CoordinateSystem srcCSys, CoordinateSystem destCSys)
	{
		SharedDouble tmpX = new SharedDouble();
		SharedDouble tmpY = new SharedDouble();
		CoordinateSystem.convertXYZ(srcCSys, destCSys, this.pos.x, this.pos.y, 0, tmpX, tmpY, null);
		this.pos.x = tmpX.value;
		this.pos.y = tmpY.value;
		this.srid = destCSys.getSRID();
	}

	@Override
	public boolean equals(Vector2D vec, boolean sameTypeOnly, boolean nearlyVal) {
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

	public boolean insideOrTouch(Coord2DDbl coord)
	{
		return this.pos.equals(coord);
	}
}
