package org.sswr.util.math.geometry;

import jakarta.annotation.Nonnull;

public class MultiSurface extends MultiGeometry<Vector2D>
{
	public MultiSurface(int srid)
	{
		super(srid);
	}

	public void addGeometry(@Nonnull Vector2D geometry)
	{
		VectorType t = geometry.getVectorType();
		if (t == VectorType.CurvePolygon)
		{
			this.geometries.add(geometry);
		}
	}

	@Nonnull
	public VectorType getVectorType()
	{
		return VectorType.MultiSurface;
	}

	@Nonnull
	public Vector2D clone()
	{
		MultiSurface newObj = new MultiSurface(this.srid);
		int i = 0;
		int j = this.geometries.size();
		while (i < j)
		{
			newObj.addGeometry(this.geometries.get(i).clone());
			i++;
		}
		return newObj;
	}	
}
