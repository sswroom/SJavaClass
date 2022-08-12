package org.sswr.util.math.geometry;

public class MultiPolygon extends MultiGeometry<Polygon>
{
	public MultiPolygon(int srid, boolean hasZ, boolean hasM)
	{
		super(srid, hasZ, hasM);
	}

	public VectorType getVectorType()
	{
		return VectorType.MultiPolygon;
	}

	public Vector2D clone()
	{
		MultiPolygon newObj = new MultiPolygon(this.srid, this.hasZ, this.hasM);
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
