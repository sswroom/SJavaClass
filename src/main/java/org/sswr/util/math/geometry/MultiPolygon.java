package org.sswr.util.math.geometry;

import jakarta.annotation.Nonnull;

public class MultiPolygon extends MultiGeometry<Polygon>
{
	public MultiPolygon(int srid)
	{
		super(srid);
	}

	@Nonnull
	public VectorType getVectorType()
	{
		return VectorType.MultiPolygon;
	}

	@Nonnull
	public Vector2D clone()
	{
		MultiPolygon newObj = new MultiPolygon(this.srid);
		int i = 0;
		int j = this.geometries.size();
		while (i < j)
		{
			newObj.addGeometry((Polygon)this.geometries.get(i).clone());
			i++;
		}
		return newObj;
	}
}
