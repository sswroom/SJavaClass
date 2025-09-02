package org.sswr.util.math.geometry;

import java.util.Iterator;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class MultiCurve extends MultiGeometry<Vector2D>
{
	public MultiCurve(int srid)
	{
		super(srid);
	}

	public void addGeometry(@Nonnull Vector2D geometry)
	{
		VectorType t = geometry.getVectorType();
		if (t == VectorType.CompoundCurve || t == VectorType.LineString)
		{
			this.geometries.add(geometry);
		}
		else
		{
			System.out.println("Error: Adding "+t.name()+" to MultiCurve");
		}
	}

	@Nonnull
	public VectorType getVectorType()
	{
		return VectorType.MultiCurve;
	}

	@Nonnull
	public Vector2D clone()
	{
		MultiCurve newObj = new MultiCurve(this.srid);
		Iterator<Vector2D> it = this.iterator();
		while (it.hasNext())
		{
			newObj.addGeometry(it.next().clone());
		}
		return newObj;
	}

	public boolean hasCurve()
	{
		return true;
	}

	@Nullable
	public Vector2D toSimpleShape()
	{
		Polyline newObj = new Polyline(this.srid);
		Iterator<Vector2D> it = this.iterator();
		while (it.hasNext())
		{
			Vector2D vec;
			if ((vec = it.next().toSimpleShape()) != null)
			{
				if (vec.getVectorType() == VectorType.LineString)
				{
					newObj.addGeometry((LineString)vec);
				}
				else
				{
					System.out.println("Error: MultiCurve SimpleShape is not polygon");
					return null;
				}
			}
			else
			{
				System.out.println("Error: Error in MultiCurve converting to simple shape");
				return null;
			}
		}
		return newObj;
	}
}
