package org.sswr.util.math.geometry;

public class MultiPolygon extends MultiGeometry<Polygon>
{
	public MultiPolygon(int srid)
	{
		super(srid);
	}

	public VectorType getVectorType()
	{
		return VectorType.MultiPolygon;
	}

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
