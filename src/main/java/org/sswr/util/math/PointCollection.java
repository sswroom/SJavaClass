package org.sswr.util.math;

import org.sswr.util.data.SharedDouble;

public abstract class PointCollection extends Vector2D
{
	public PointCollection(int srid)
	{
		super(srid);
	}

	public abstract int[] getPtOfstList();
	public abstract double[] getPointList();
	public void getCenter(SharedDouble x, SharedDouble y)
	{
		double[] points = this.getPointList();
	
		double maxX;
		double maxY;
		double minX;
		double minY;
		double v;
		if (points.length <= 0)
		{
			x.value = 0;
			y.value = 0;
		}
		else
		{
			int i = points.length >> 1;
			minX = maxX = points[0];
			minY = maxY = points[1];
	
			while (i-- > 0)
			{
				v = points[(i << 1)];
				if (v > maxX)
				{
					maxX = v;
				}
				if (v < minX)
				{
					minX = v;
				}
				v = points[(i << 1) + 1];
				if (v > maxY)
				{
					maxY = v;
				}
				else if (v < minY)
				{
					minY = v;
				}
			}
			x.value = (minX + maxX) * 0.5;
			y.value = (minY + maxY) * 0.5;
		}
	}
}
