package org.sswr.util.math.geometry;

public class CurvePolygon extends MultiGeometry<Vector2D>
{
	public CurvePolygon(int srid, boolean hasZ, boolean hasM)
	{
		super(srid, hasZ, hasM);
	}

	public void addGeometry(Vector2D geometry)
	{
		VectorType t = geometry.getVectorType();
		if (t == VectorType.CircularString || t == VectorType.CompoundCurve || t == VectorType.LineString)
		{
			this.geometries.add(geometry);
		}
	}

	public VectorType getVectorType()
	{
		return VectorType.CurvePolygon;
	}

	public Vector2D clone()
	{
		CurvePolygon newObj = new CurvePolygon(this.srid, this.hasZ, this.hasM);
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
