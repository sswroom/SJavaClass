package org.sswr.util.math.geometry;

import java.util.Iterator;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

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

	public boolean hasCurve() { return true; }
	public @Nullable Vector2D toSimpleShape()
	{
		MultiPolygon newObj = new MultiPolygon(this.srid);
		Iterator<Vector2D> it = this.iterator();
		while (it.hasNext())
		{
			Vector2D vec;
			if ((vec = it.next().toSimpleShape()) != null)
			{
				if (vec.getVectorType() == VectorType.Polygon)
				{
					newObj.addGeometry((Polygon)vec);
				}
				else
				{
					System.out.println("Error: MultiSurface SimpleShape is not polygon");
					return null;
				}
			}
			else
			{
				System.out.println("Error: Error in MultiSurface converting to simple shape");
				return null;
			}
		}
		return newObj;
	}
}
