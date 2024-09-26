package org.sswr.util.math.geometry;

import java.util.ArrayList;
import java.util.Iterator;

import org.sswr.util.math.Coord2DDbl;

import jakarta.annotation.Nonnull;

public class CurvePolygon extends MultiGeometry<Vector2D>
{
	public CurvePolygon(int srid)
	{
		super(srid);
	}

	public void addGeometry(@Nonnull Vector2D geometry)
	{
		VectorType t = geometry.getVectorType();
		if (t == VectorType.CircularString || t == VectorType.CompoundCurve || t == VectorType.LineString)
		{
			this.geometries.add(geometry);
		}
	}

	@Nonnull
	public VectorType getVectorType()
	{
		return VectorType.CurvePolygon;
	}

	@Nonnull
	public Vector2D clone()
	{
		CurvePolygon newObj = new CurvePolygon(this.srid);
		int i = 0;
		int j = this.geometries.size();
		while (i < j)
		{
			newObj.addGeometry(this.geometries.get(i).clone());
			i++;
		}
		return newObj;
	}	

	@Nonnull
	public Vector2D curveToLine()
	{
		Polygon pg = new Polygon(this.srid);
		LinearRing lr;
		ArrayList<Coord2DDbl> ptList = new ArrayList<Coord2DDbl>();
		Vector2D vec;
		Iterator<Vector2D> it = this.iterator();
		while (it.hasNext())
		{
			vec = it.next();
			if (vec.getVectorType() == VectorType.CompoundCurve)
			{
				ptList.clear();
				((CompoundCurve)vec).getDrawPoints(ptList);
				lr = new LinearRing(this.srid, ptList.toArray(new Coord2DDbl[0]), null, null);
				pg.addGeometry(lr);
			}
			else if (vec.getVectorType() == VectorType.LineString)
			{
				Coord2DDbl[] ptArr = ((LineString)vec).getPointList();
				lr = new LinearRing(this.srid, ptArr, null, null);
				pg.addGeometry(lr);
			}
			else
			{
				System.out.println("CurvePolygon: CurveToLine unexpected type: "+vec.getVectorType());
			}
		}
		return pg;
	}

	public boolean insideOrTouch(@Nonnull Coord2DDbl coord)
	{
		return this.curveToLine().insideOrTouch(coord);
	}
}
