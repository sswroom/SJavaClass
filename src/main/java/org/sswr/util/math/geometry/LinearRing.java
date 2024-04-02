package org.sswr.util.math.geometry;

import org.sswr.util.data.ByteTool;
import org.sswr.util.math.Coord2DDbl;

public class LinearRing extends LineString {
	public LinearRing(int srid, int nPoint, boolean hasZ, boolean hasM)
	{
		super(srid, nPoint, hasZ, hasM);
	}

	public LinearRing(int srid, Coord2DDbl[] pointArr, double[] zArr, double[] mArr)
	{
		super(srid, pointArr, zArr, mArr);
	}

	public VectorType getVectorType()
	{
		return VectorType.LinearRing;
	}

	public Vector2D clone()
	{
		LinearRing lr;
		lr = new LinearRing(this.srid, this.pointArr.length, this.zArr != null, this.mArr != null);
		ByteTool.copyArray(lr.pointArr, 0, this.pointArr, 0, this.pointArr.length);
		if (this.zArr != null)
		{
			ByteTool.copyArray(lr.zArr, 0, this.zArr, 0, this.pointArr.length);
		}
		if (this.mArr != null)
		{
			ByteTool.copyArray(lr.mArr, 0, this.mArr, 0, this.pointArr.length);
		}
		return lr;
	}

	public boolean insideOrTouch(Coord2DDbl coord)
	{
		double thisX;
		double thisY;
		double lastX;
		double lastY;
		int j;
		int l;
		int leftCnt = 0;
		double tmpX;
	
		l = this.pointArr.length;
		lastX = this.pointArr[0].x;
		lastY = this.pointArr[0].y;
		while (l-- > 0)
		{
			thisX = this.pointArr[l].x;
			thisY = this.pointArr[l].y;
			j = 0;
			if (lastY > coord.y)
				j += 1;
			if (thisY > coord.y)
				j += 1;
	
			if (j == 1)
			{
				tmpX = lastX - (lastX - thisX) * (lastY - coord.y) / (lastY - thisY);
				if (tmpX == coord.x)
				{
					return true;
				}
				else if (tmpX < coord.x)
					leftCnt++;
			}
			else if (thisY == coord.y && lastY == coord.y)
			{
				if ((thisX >= coord.x && lastX <= coord.x) || (lastX >= coord.x && thisX <= coord.x))
				{
					return true;
				}
			}
			else if (thisY == coord.y && thisX == coord.x)
			{
				return true;
			}
	
			lastX = thisX;
			lastY = thisY;
		}
	
		return (leftCnt & 1) != 0;
	}

	public double calArea()
	{
		Coord2DDbl lastPt = this.pointArr[0];
		double total = 0;
		int i = this.pointArr.length;
		while (i-- > 0)
		{
			total = total + (lastPt.x * this.pointArr[i].y - lastPt.y * this.pointArr[i].x);
			lastPt = this.pointArr[i];
		}
		if (total < 0)
			return total * -0.5;
		else
			return total * 0.5;
	}

	public boolean isOpen()
	{
		return !this.pointArr[0].equals(this.pointArr[this.pointArr.length - 1]);
	}

	public boolean isClose()
	{
		return this.pointArr[0].equals(this.pointArr[this.pointArr.length - 1]);
	}

	public static LinearRing createFromCircle(int srid, Coord2DDbl center, double radiusX, double radiusY, int nPoints)
	{
		LinearRing lr = new LinearRing(srid, nPoints + 1, false, false);
		double ratio = 2 * Math.PI / nPoints;
		int i;
		int j;
		double angle;
		Coord2DDbl[] pointList = lr.getPointList();
		i = 0;
		j = pointList.length;
		while (i < j)
		{
			angle = i * ratio;
			pointList[i] = center.add(new Coord2DDbl(radiusX * Math.cos(angle), radiusY * Math.sin(angle)));
			i++;
		}
		return lr;	
	}
}
