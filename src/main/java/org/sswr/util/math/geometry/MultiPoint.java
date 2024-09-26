package org.sswr.util.math.geometry;

import java.util.Iterator;

import jakarta.annotation.Nonnull;

public class MultiPoint extends MultiGeometry<Point2D> {
	public MultiPoint(int srid)
	{
		super(srid);
	}

	@Nonnull
	public VectorType getVectorType()
	{
		return VectorType.MultiPoint;
	}

	@Nonnull
	public Vector2D clone()
	{
		MultiPoint newObj = new MultiPoint(this.srid);
		Iterator<Point2D> it = this.geometries.iterator();
		while (it.hasNext())
		{
			newObj.addGeometry(((Point2D)it.next().clone()));
		}
		return newObj;
	}
}
