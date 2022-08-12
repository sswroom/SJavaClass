package org.sswr.util.math.geometry;

import org.sswr.util.math.Coord2DDbl;
import org.sswr.util.math.CoordinateSystem;
import org.sswr.util.math.RectAreaDbl;

public abstract class PointCollection extends Vector2D
{
	protected Coord2DDbl []pointArr;

	public PointCollection(int srid, int nPoint, Coord2DDbl []pointArr)
	{
		super(srid);
		this.pointArr = new Coord2DDbl[nPoint];
		int i = nPoint;
		if (pointArr != null)
		{
			while (i-- > 0)
			{
				this.pointArr[i] = pointArr[i].clone();
			}
		}
		else
		{
			while (i-- > 0)
			{
				this.pointArr[i] = new Coord2DDbl();
			}
		}
	}

	public Coord2DDbl[] getPointList()
	{
		return this.pointArr;
	}

	public Coord2DDbl getCenter()
	{
		Coord2DDbl[] points = this.getPointList();
	
		if (points.length <= 0)
		{
			return new Coord2DDbl();
		}
		else
		{
			Coord2DDbl max = points[0].clone();
			Coord2DDbl min = max.clone();
			int i = points.length;
			while (i-- > 1)
			{
				max.setMax(points[i]);
				min.setMin(points[i]);
			}
			return new Coord2DDbl((min.x + max.x) * 0.5, (min.y + max.y) * 0.5);
		}
	}

	public void getBounds(RectAreaDbl bounds)
	{
		Coord2DDbl min = this.pointArr[0].clone();
		Coord2DDbl max = min.clone();
		int i = this.pointArr.length;
		while (i-- > 1)
		{
			min.setMin(this.pointArr[i]);
			max.setMax(this.pointArr[i]);
		}
		bounds.tl = min;
		bounds.br = max;
	}

	public void convCSys(CoordinateSystem srcCSys, CoordinateSystem destCSys)
	{
		CoordinateSystem.convertXYArray(srcCSys, destCSys, this.pointArr, this.pointArr);
	}
}
