package org.sswr.util.math.geometry;

import java.util.Objects;

import org.sswr.util.math.MathUtil;

public class PointM extends Point2D
{
	protected double m;
	public PointM(int srid, double x, double y, double m)
	{
		super(srid, x, y);
		this.m = m;
	}

	public Vector2D clone()
	{
		PointM pt;
		pt = new PointM(this.srid, this.pos.x, this.pos.y, this.m);
		return pt;
	}
	
	public double getM()
	{
		return this.m;
	}
	
	public boolean hasM()
	{
		return true;
	}
	
	@Override
	public boolean equals(Vector2D vec, boolean sameTypeOnly, boolean nearlyVal) {
		if (vec == this)
			return true;
		if (!(vec instanceof PointM)) {
			return false;
		}
		PointM pointM = (PointM)vec;
		if (nearlyVal)
			return this.srid == pointM.srid && this.pos.equalsNearly(pointM.pos) && MathUtil.nearlyEqualsDbl(this.m, pointM.m);
		else
			return this.srid == pointM.srid && this.pos.equals(pointM.pos) && m == pointM.m;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.srid, this.pos, this.m);
	}
}
