package org.sswr.util.math.geometry;

public class CompoundCurve extends MultiGeometry<LineString>
{
	public CompoundCurve(int srid, boolean hasZ, boolean hasM)
	{
		super(srid, hasZ, hasM);
	}

	public void addGeometry(LineString geometry)
	{
		VectorType t = geometry.getVectorType();
		if (t == VectorType.CircularString || t == VectorType.LineString)
		{
			this.geometries.add(geometry);
		}
	}

	public VectorType getVectorType()
	{
		return VectorType.CompoundCurve;
	}

	public Vector2D clone()
	{
		CompoundCurve newObj = new CompoundCurve(this.srid, this.hasZ, this.hasM);
		int i = 0;
		int j = this.geometries.size();
		while (i < j)
		{
			newObj.addGeometry((LineString)this.geometries.get(i).clone());
			i++;
		}
		return newObj;
	}
}
