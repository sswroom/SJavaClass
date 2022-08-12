package org.sswr.util.math.geometry;

public class MultiSurface extends MultiGeometry<Vector2D>
{
	public MultiSurface(int srid, boolean hasZ, boolean hasM)
	{
		super(srid, hasZ, hasM);
	}

	public void addGeometry(Vector2D geometry)
	{
		VectorType t = geometry.getVectorType();
		if (t == VectorType.CurvePolygon)
		{
			this.geometries.add(geometry);
		}
	}

	public VectorType getVectorType()
	{
		return VectorType.MultiSurface;
	}

	public Vector2D clone()
	{
		MultiSurface newObj = new MultiSurface(this.srid, this.hasZ, this.hasM);
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
