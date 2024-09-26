package org.sswr.util.math.geometry;

import java.util.Objects;

import org.sswr.util.basic.Vector3;
import org.sswr.util.math.CoordinateSystem;
import org.sswr.util.math.MathUtil;

import jakarta.annotation.Nonnull;

public class PointZ extends Point2D
{
	protected double z;
	
	public PointZ(int srid, double x, double y, double z)
	{
		super(srid, x, y);
		this.z = z;
	}

	@Nonnull
	public Vector2D clone()
	{
		return new PointZ(this.srid, this.pos.x, this.pos.y, this.z);
	}

	public double getZ()
	{
		return this.z;
	}

	public Vector3 getPos3D()
	{
		return new Vector3(this.pos, this.z);
	}

	public boolean hasZ()
	{
		return true;
	}

	public void convCSys(@Nonnull CoordinateSystem srcCSys, @Nonnull CoordinateSystem destCSys)
	{
		Vector3 tmpPos = CoordinateSystem.convert3D(srcCSys, destCSys, new Vector3(this.pos, this.z));
		this.pos = tmpPos.getXY();
		this.z = tmpPos.getZ();
		this.srid = destCSys.getSRID();
	}

	@Override
	public boolean equals(@Nonnull Vector2D vec, boolean sameTypeOnly, boolean nearlyVal) {
		if (vec == this)
			return true;
		if (!(vec instanceof PointZ)) {
			return false;
		}
		PointZ pointZ = (PointZ)vec;
		if (nearlyVal)
			return srid == pointZ.srid && !pointZ.hasM() && MathUtil.nearlyEqualsDbl(z, pointZ.z) && pos.equalsNearly(pointZ.pos);
		else
			return srid == pointZ.srid && !pointZ.hasM() && z == pointZ.z && pos.equals(pointZ.pos);
	}

	@Override
	public int hashCode() {
		return Objects.hash(z, this.pos);
	}
}
