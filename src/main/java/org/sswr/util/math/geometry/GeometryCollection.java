package org.sswr.util.math.geometry;

import java.util.Iterator;

public class GeometryCollection extends MultiGeometry<Vector2D> {
	public GeometryCollection(int srid)
	{
		super(srid);
	}

	public VectorType getVectorType()
	{
		return VectorType.GeometryCollection;
	}

	public Vector2D clone()
	{
		GeometryCollection newObj = new GeometryCollection(this.srid);
		Iterator<Vector2D> it = this.geometries.iterator();
		while (it.hasNext())
		{
			newObj.addGeometry(it.next().clone());
		}
		return newObj;
	
	}
}
