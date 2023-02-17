package org.sswr.util.math.geometry;

import java.util.Objects;

import org.sswr.util.data.SharedDouble;
import org.sswr.util.math.Coord2DDbl;
import org.sswr.util.math.CoordinateSystem;
import org.sswr.util.math.MathUtil;

public abstract class PointOfstCollection extends PointCollection
{
	protected int []ptOfstArr;
	protected double []zArr;
	protected double []mArr;

	public PointOfstCollection(int srid, int nPtOfst, int nPoint, Coord2DDbl []pointArr, boolean hasZ, boolean hasM)
	{
		super(srid, nPoint, pointArr);
		this.ptOfstArr = new int[nPtOfst];
		if (hasZ)
		{
			this.zArr = new double[nPoint];
		}
		if (hasM)
		{
			this.mArr = new double[nPoint];
		}
	}

	public int []getPtOfstList()
	{
		return this.ptOfstArr;
	}

	public Coord2DDbl getCenter()
	{
		double maxLength = 0;
		int maxId = 0;
		double currLength;
		int i = this.pointArr.length - 1;
		int j = this.ptOfstArr.length;
		int k;
		Coord2DDbl lastPt;
		Coord2DDbl thisPt;
		while (j-- > 0)
		{
			lastPt = this.pointArr[i];
			currLength = 0;
			k = this.ptOfstArr[j];
			while (i-- > k)
			{
				thisPt = this.pointArr[i];
				currLength += Math.sqrt((thisPt.x - lastPt.x) * (thisPt.x - lastPt.x) + (thisPt.y - lastPt.y) * (thisPt.y - lastPt.y));
				lastPt = thisPt;
			}
			if (currLength > maxLength)
			{
				maxLength = currLength;
				maxId = j;
			}
		}
	
		if (maxLength == 0)
		{
			return this.pointArr[0];
		}
		i = this.ptOfstArr[maxId];
		if (maxId >= this.ptOfstArr.length - 1)
		{
			j = this.pointArr.length;
		}
		else
		{
			j = this.ptOfstArr[maxId + 1];
		}
		maxLength = maxLength * 0.5;
		lastPt = this.pointArr[i];
		while (i < j)
		{
			i++;
			thisPt = this.pointArr[i];
			currLength = Math.sqrt((thisPt.x - lastPt.x) * (thisPt.x - lastPt.x) + (thisPt.y - lastPt.y) * (thisPt.y - lastPt.y));
			if (currLength >= maxLength)
			{
				return new Coord2DDbl(lastPt.x + (thisPt.x - lastPt.x) * maxLength / currLength,
					lastPt.y + (thisPt.y - lastPt.y) * maxLength / currLength);
			}
			else
			{
				maxLength -= currLength;
			}
			lastPt = thisPt;
		}
		return this.pointArr[0];
	}

	public void convCSys(CoordinateSystem srcCSys, CoordinateSystem destCSys)
	{
		if (this.zArr != null)
		{
			SharedDouble tmpX = new SharedDouble();
			SharedDouble tmpY = new SharedDouble();
			SharedDouble tmpZ = new SharedDouble();
			int i = this.pointArr.length;
			while (i-- > 0)
			{
				CoordinateSystem.convertXYZ(srcCSys, destCSys, this.pointArr[i].x, this.pointArr[i].y, this.zArr[i], tmpX, tmpY, tmpZ);
				this.pointArr[i].x = tmpX.value;
				this.pointArr[i].y = tmpY.value;
				this.zArr[i] = tmpZ.value;
			}
			this.srid = destCSys.getSRID();
		}
		else
		{
			CoordinateSystem.convertXYArray(srcCSys, destCSys, this.pointArr, this.pointArr);
			this.srid = destCSys.getSRID();
		}
	}


	@Override
	public int hashCode() {
		return Objects.hash(ptOfstArr, zArr, mArr);
	}

	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof PointOfstCollection)) {
			return false;
		}
		PointOfstCollection pointOfstCollection = (PointOfstCollection) o;
		return this.getVectorType() == pointOfstCollection.getVectorType() &&
				Objects.equals(ptOfstArr, pointOfstCollection.ptOfstArr) &&
				Objects.equals(zArr, pointOfstCollection.zArr) &&
				Objects.equals(mArr, pointOfstCollection.mArr) &&
				Objects.equals(pointArr, pointOfstCollection.pointArr) &&
				this.srid == pointOfstCollection.srid;
	}

	@Override
	public boolean equalsNearly(Vector2D vec) {
		if (vec == this)
			return true;
		if (!(vec instanceof PointOfstCollection)) {
			return false;
		}
		if (vec.getVectorType() == this.getVectorType() && this.hasZ() == vec.hasZ() && this.hasM() == vec.hasM())
		{
			PointOfstCollection pl = (PointOfstCollection)vec;
			int[] ptOfst = pl.getPtOfstList();
			Coord2DDbl []ptList = pl.getPointList();
			double []valArr;
			if (ptOfst.length != this.ptOfstArr.length || ptList.length != this.pointArr.length)
			{
				return false;
			}
			int i = ptOfst.length;
			while (i-- > 0)
			{
				if (ptOfst[i] != this.ptOfstArr[i])
				{
					return false;
				}
			}
			i = ptList.length;
			while (i-- > 0)
			{
				if (!ptList[i].equalsNearly(this.pointArr[i]))
				{
					return false;
				}
			}
			if (this.zArr != null)
			{
				valArr = pl.zArr;
				i = this.zArr.length;
				while (i-- > 0)
				{
					if (!MathUtil.nearlyEqualsDbl(valArr[i], this.zArr[i]))
					{
						return false;
					}
				}
			}
			if (this.mArr != null)
			{
				valArr = pl.mArr;
				i = this.mArr.length;
				while (i-- > 0)
				{
					if (!MathUtil.nearlyEqualsDbl(valArr[i], this.mArr[i]))
					{
						return false;
					}
				}
			}
			return true;
		}
		else
		{
			return false;
		}
	}

	public boolean hasZ()
	{
		return this.zArr != null;
	}

	public double []getZList()
	{
		return this.zArr;
	}

	public boolean hasM()
	{
		return this.mArr != null;
	}

	public double []getMList()
	{
		return this.mArr;
	}
}
