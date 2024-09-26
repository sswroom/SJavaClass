package org.sswr.util.math.geometry;

import org.sswr.util.data.ByteTool;

import jakarta.annotation.Nonnull;

public class CircularString extends LineString {
	public CircularString(int srid, int nPoint, boolean hasZ, boolean hasM)
	{
		super(srid, nPoint | 1, hasZ, hasM);
	}

	@Nonnull
	public VectorType getVectorType()
	{
		return VectorType.CircularString;
	}

	@Nonnull
	public Vector2D clone()
	{
		CircularString pl;
		pl = new CircularString(this.srid, this.pointArr.length, this.zArr != null, this.mArr != null);
		ByteTool.copyArray(pl.pointArr, 0, this.pointArr, 0, this.pointArr.length);
		if (this.zArr != null)
		{	
			ByteTool.copyArray(pl.zArr, 0, this.zArr, 0, this.pointArr.length);
		}
		if (this.mArr != null)
		{	
			ByteTool.copyArray(pl.mArr, 0, this.mArr, 0, this.pointArr.length);
		}
		return pl;
	}
}
