package org.sswr.util.math.geometry;

import java.util.Objects;

import org.sswr.util.math.MathUtil;

public class PointZM extends PointZ
{
	protected double m;
	public PointZM(int srid, double x, double y, double z, double m)
	{
		super(srid, x, y, z);
		this.m = m;
	}


	public Vector2D clone()
	{
		PointZM pt;
		pt = new PointZM(this.srid, this.pos.x, this.pos.y, this.z, this.m);
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
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof PointZM)) {
			return false;
		}
		PointZM pointZM = (PointZM) o;
		return this.srid == pointZM.srid && this.pos.equals(pointZM.pos) && this.z == pointZM.z && m == pointZM.m;
	}

	@Override
	public boolean equalsNearly(Vector2D vec) {
		if (vec == this)
			return true;
		if (!(vec instanceof PointZM)) {
			return false;
		}
		PointZM pointZM = (PointZM) vec;
		return this.srid == pointZM.srid && this.pos.equalsNearly(pointZM.pos) && MathUtil.nearlyEqualsDbl(this.z, pointZM.z) && MathUtil.nearlyEqualsDbl(this.m, pointZM.m);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.srid, this.pos, this.z, this.m);
	}
}
