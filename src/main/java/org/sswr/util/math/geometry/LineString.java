package org.sswr.util.math.geometry;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.sswr.util.basic.Vector3;
import org.sswr.util.data.ByteTool;
import org.sswr.util.data.SharedBool;
import org.sswr.util.data.SharedDouble;
import org.sswr.util.math.Coord2DDbl;
import org.sswr.util.math.CoordinateSystem;
import org.sswr.util.math.MathUtil;
import org.sswr.util.math.RectAreaDbl;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class LineString extends Vector2D
{
	protected Coord2DDbl[] pointArr;
	protected double []zArr;
	protected double []mArr;

	public LineString(int srid, int nPoint, boolean hasZ, boolean hasM)
	{
		super(srid);
		this.pointArr = new Coord2DDbl[nPoint];
		if (hasZ)
		{
			this.zArr = new double[nPoint];
		}
		else
		{
			this.zArr = null;
		}
		if (hasM)
		{
			this.mArr = new double[nPoint];
		}
		else
		{
			this.mArr = null;
		}
	}

	public LineString(int srid, @Nonnull Coord2DDbl[] pointArr, @Nullable double[] zArr, @Nullable double[] mArr)
	{
		super(srid);
		int i = 0;
		int j = pointArr.length;
		this.pointArr = new Coord2DDbl[pointArr.length];
		while (i < j)
		{
			this.pointArr[i] = pointArr[i].clone();
			i++;
		}
		if (zArr != null)
		{
			if (zArr.length != j)
				throw new IllegalArgumentException("zArr");
			this.zArr = new double[j];
			i = 0;
			while (i < j)
			{
				this.zArr[i] = zArr[i];
				i++;
			}
		}
		else
		{
			this.zArr = null;
		}
		if (mArr != null)
		{
			if (mArr.length != j)
				throw new IllegalArgumentException("mArr");
			this.mArr = new double[j];
			i = 0;
			while (i < j)
			{
				this.mArr[i] = mArr[i];
				i++;
			}
		}
		else
		{
			this.mArr = null;
		}
	}

	@Nonnull
	public VectorType getVectorType()
	{
		return VectorType.LineString;
	}

	@Nonnull
	public Coord2DDbl getCenter()
	{
		double maxX;
		double maxY;
		double minX;
		double minY;
		double v;
		if (this.pointArr.length <= 0)
		{
			return new Coord2DDbl(0, 0);
		}
		else
		{
			int i = this.pointArr.length;
			minX = maxX = this.pointArr[0].x;
			minY = maxY = this.pointArr[0].y;
	
			while (i-- > 0)
			{
				v = this.pointArr[i].x;
				if (v > maxX)
				{
					maxX = v;
				}
				if (v < minX)
				{
					minX = v;
				}
				v = this.pointArr[i].y;
				if (v > maxY)
				{
					maxY = v;
				}
				else if (v < minY)
				{
					minY = v;
				}
			}
			return new Coord2DDbl((minX + maxX) * 0.5, (minY + maxY) * 0.5);
		}
	}

	@Nonnull
	public Vector2D clone()
	{
		LineString pl = new LineString(this.srid, this.pointArr.length, this.hasZ(), this.hasM());
		ByteTool.copyArray(pl.pointArr, 0, this.pointArr, 0, this.pointArr.length);
		if (this.zArr != null)
		{	
			ByteTool.copyArray(pl.zArr, 0, this.zArr, 0, this.zArr.length);
		}
		if (this.mArr != null)
		{	
			ByteTool.copyArray(pl.mArr, 0, this.mArr, 0, this.mArr.length);
		}
		return pl;
	}

	@Nonnull
	public RectAreaDbl getBounds()
	{
		int i = this.pointArr.length;
		Coord2DDbl min;
		Coord2DDbl max;
		min = this.pointArr[0].clone();
		max = min.clone();
		while (i > 1)
		{
			i -= 1;
			min = min.setMin(this.pointArr[i]);
			max = max.setMax(this.pointArr[i]);
		}
		return new RectAreaDbl(min, max);		
	}

	public double calBoundarySqrDistance(@Nonnull Coord2DDbl pt, @Nullable Coord2DDbl nearPt)
	{
		int l;
		Coord2DDbl[] points;
	
		points = this.pointArr;
	
		l = points.length;
	
		double calBase;
		double calH;
		double calW;
		double calX;
		double calY;
		double calD;
		double dist = 0x7fffffff;
		double calPtX = 0;
		double calPtY = 0;
	
		l--;
		while (l-- > 0)
		{
			calH = points[l].y - points[l + 1].y;
			calW = points[l].x - points[l + 1].x;

			if (calH == 0)
			{
				calX = pt.x;
			}
			else
			{
				calX = (calBase = (calW * calW)) * pt.x;
				calBase += calH * calH;
				calX += calH * calH * (points[l].x);
				calX += (pt.y - points[l].y) * calH * calW;
				calX /= calBase;
			}

			if (calW == 0)
			{
				calY = pt.y;
			}
			else
			{
				calY = ((calX - (points[l].x)) * calH / calW) + points[l].y;
			}

			if (calW < 0)
			{
				if (points[l + 0].x > calX)
					continue;
				if (points[l + 1].x < calX)
					continue;
			}
			else
			{
				if (points[l + 0].x < calX)
					continue;
				if (points[l + 1].x > calX)
					continue;
			}

			if (calH < 0)
			{
				if (points[l + 0].y > calY)
					continue;
				if (points[l + 1].y < calY)
					continue;
			}
			else
			{
				if (points[l + 0].y < calY)
					continue;
				if (points[l + 1].y > calY)
					continue;
			}

			calH = pt.y - calY;
			calW = pt.x - calX;
			calD = calW * calW + calH * calH;
			if (calD < dist)
			{
				dist = calD;
				calPtX = calX;
				calPtY = calY;
			}
		}
		l = this.pointArr.length;
		while (l-- > 0)
		{
			calH = pt.y - points[l].y;
			calW = pt.x - points[l].x;
			calD = calW * calW + calH * calH;
			if (calD < dist)
			{
				dist = calD;
				calPtX = points[l].x;
				calPtY = points[l].y;
			}
		}
		if (nearPt != null)
		{
			nearPt.x = calPtX;
			nearPt.y = calPtY;
		}
		return dist;
	}

	public double calSqrDistance3D(@Nonnull Coord2DDbl pt, double z, @Nonnull Coord2DDbl nearPt, @Nullable SharedDouble nearZ)
	{
		if (!this.hasZ())
		{
			if (nearZ != null)
				nearZ.value = z;
			return calSqrDistance(pt, nearPt);
		}
		int k;
		int l;
		Coord2DDbl[] points;
		double[] zArr;
	
		points = this.pointArr;
		zArr = this.zArr;

		l = points.length;
	
		double calBase;
		double calDX;
		double calDY;
		double calDZ;
		double calX;
		double calY;
		double calZ;
		double calD;
		double dist = 0x7fffffff;
		double calPtX = 0;
		double calPtY = 0;
		double calPtZ = 0;
	
		l--;
		while (l-- > 0)
		{
			calDX = points[l].x - points[l + 1].x;
			calDY = points[l].y - points[l + 1].y;

			if (calDY == 0)
			{
				calX = pt.x;
				calZ = z;
			}
			else
			{
				calX = (calBase = (calDX * calDX)) * pt.x;
				calBase += calDY * calDY;
				calX += calDY * calDY * (points[l].x);
				calX += (pt.y - points[l].y) * calDY * calDX;
				calX /= calBase;
			}

			if (calDX == 0)
			{
				calY = pt.y;
			}
			else
			{
				calY = ((calX - (points[l].x)) * calDY / calDX) + points[l].y;
			}

			if (calDX < 0)
			{
				if (points[l + 0].x > calX)
					continue;
				if (points[l + 1].x < calX)
					continue;
			}
			else
			{
				if (points[l + 0].x < calX)
					continue;
				if (points[l + 1].x > calX)
					continue;
			}

			if (calDY < 0)
			{
				if (points[l + 0].y > calY)
					continue;
				if (points[l + 1].y < calY)
					continue;
			}
			else
			{
				if (points[l + 0].y < calY)
					continue;
				if (points[l + 1].y > calY)
					continue;
			}
			
			if (calDX != 0)
			{
				calZ = (calX - points[l + 1].x) * (zArr[l] - zArr[l + 1]) / calDX + zArr[l + 1]; 
			}
			else if (calDY != 0)
			{
				calZ = (calY - points[l + 1].y) * (zArr[l] - zArr[l + 1]) / calDY + zArr[l + 1]; 
			}
			else
			{
				calZ = zArr[l];
			}

			calDX = pt.x - calX;
			calDY = pt.y - calY;
			calDZ = z - calZ;
			calD = calDX * calDX + calDY * calDY + calDZ * calDZ;
			if (calD < dist)
			{
				dist = calD;
				calPtX = calX;
				calPtY = calY;
				calPtZ = calZ;
			}
		}
		k = this.pointArr.length;
		while (k-- > 0)
		{
			calDX = pt.x - points[k].x;
			calDY = pt.y - points[k].y;
			calDZ = z - zArr[k];
			calD = calDX * calDX + calDY * calDY + calDZ * calDZ;
			if (calD < dist)
			{
				dist = calD;
				calPtX = points[k].x;
				calPtY = points[k].y;
				calPtZ = zArr[k];
			}
		}
		if (nearPt != null)
		{
			nearPt.x = calPtX;
			nearPt.y = calPtY;
		}
		if (nearZ != null)
		{
			nearZ.value = calPtZ;
		}
		return dist;
	}

	public double calArea()
	{
		return 0;
	}

	public boolean joinVector(@Nonnull Vector2D vec)
	{
		if (vec.getVectorType() != VectorType.LineString || this.hasZ() != vec.hasZ() || this.hasM() != vec.hasM())
		{
			return false;
		}
		LineString ls = (LineString)vec;
		int i;
		int j;
		Coord2DDbl[] points = ls.pointArr;
		Coord2DDbl[] newPoints;
		if (points[0] == this.pointArr[this.pointArr.length - 1])
		{
			newPoints = new Coord2DDbl[this.pointArr.length + points.length - 1];
			ByteTool.copyArray(newPoints, 0, this.pointArr, 0, this.pointArr.length);
			ByteTool.copyArray(newPoints, this.pointArr.length, points, 1, points.length - 1);
			this.pointArr = newPoints;
			return true;
		}
		else if (points[points.length - 1] == this.pointArr[this.pointArr.length - 1])
		{
			newPoints = new Coord2DDbl[this.pointArr.length + points.length - 1];
			ByteTool.copyArray(newPoints, 0, this.pointArr, 0, this.pointArr.length);
			i = points.length - 1;
			j = this.pointArr.length;
			while (i-- > 0)
			{
				newPoints[j] = points[i];
				j++;
			}
			this.pointArr = newPoints;
			return true;
		}
		else if (points[points.length - 1] == this.pointArr[0])
		{
			newPoints = new Coord2DDbl[this.pointArr.length + points.length - 1];
			ByteTool.copyArray(newPoints, 0, points, 0, points.length - 1);
			ByteTool.copyArray(newPoints, points.length - 1, this.pointArr, 0, this.pointArr.length);
			this.pointArr = newPoints;
			return true;
		}
		else if (points[0] == this.pointArr[0])
		{
			newPoints = new Coord2DDbl[this.pointArr.length + points.length - 1];
			ByteTool.copyArray(newPoints, points.length - 1, this.pointArr, 0, this.pointArr.length);
			i = points.length - 1;
			j = 1;
			while (i-- > 0)
			{
				newPoints[i] = points[j];
				j++;
			}
			this.pointArr = newPoints;
			return true;
		}
		return false;
	}

	public boolean hasZ()
	{
		return this.zArr != null;
	}

	public boolean hasM()
	{
		return this.mArr != null;
	}

	public void convCSys(@Nonnull CoordinateSystem srcCSys, @Nonnull CoordinateSystem destCSys)
	{
		if (this.zArr != null)
		{
			Vector3 tmpPos;
			int i = this.pointArr.length;
			while (i-- > 0)
			{
				tmpPos = CoordinateSystem.convert3D(srcCSys, destCSys, new Vector3(this.pointArr[i].x, this.pointArr[i].y, this.zArr[i]));
				this.pointArr[i].x = tmpPos.getX();
				this.pointArr[i].y = tmpPos.getY();
				this.zArr[i] = tmpPos.getZ();
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
	public boolean equals(@Nonnull Vector2D vec, boolean sameTypeOnly, boolean nearlyVal) {
		if (vec == this)
			return true;
		if (!(vec instanceof LineString)) {
			return false;
		}
		LineString lineString = (LineString)vec;
		if (this.getVectorType() == lineString.getVectorType() && this.hasZ() == lineString.hasZ() && this.hasM() == lineString.hasM())
		{
			Coord2DDbl []ptList = lineString.getPointList();
			double []valArr;
			if (this.pointArr.length != ptList.length)
			{
				return false;
			}
			if (nearlyVal)
			{
				int i = ptList.length;
				while (i-- > 0)
				{
					if (!ptList[i].equalsNearly(this.pointArr[i]))
					{
						return false;
					}
				}
				if (this.zArr != null)
				{
					valArr = lineString.zArr;
					i = valArr.length;
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
					valArr = lineString.mArr;
					i = valArr.length;
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
				int i = ptList.length;
				while (i-- > 0)
				{
					if (!ptList[i].equals(this.pointArr[i]))
					{
						return false;
					}
				}
				if (this.zArr != null)
				{
					valArr = lineString.zArr;
					i = valArr.length;
					while (i-- > 0)
					{
						if (valArr[i] != this.zArr[i])
						{
							return false;
						}
					}
				}
				if (this.mArr != null)
				{
					valArr = lineString.mArr;
					i = valArr.length;
					while (i-- > 0)
					{
						if (valArr[i] != this.mArr[i])
						{
							return false;
						}
					}
				}
				return true;
			}
		}
		else
		{
			return false;
		}
	}

	public boolean insideOrTouch(@Nonnull Coord2DDbl coord)
	{
		double thisX;
		double thisY;
		double lastX;
		double lastY;
		int j;
		int l;
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
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(srid, pointArr, zArr, mArr);
	}

	@Nonnull
	public Coord2DDbl[] getPointList()
	{
		return this.pointArr;
	}

	public int getPointCount()
	{
		return this.pointArr.length;
	}

	@Nonnull
	public Coord2DDbl getPoint(int index)
	{
		if (index >= this.pointArr.length)
			return this.pointArr[0];
		else
			return this.pointArr[index];
	}

	public double calcLength()
	{
		double leng = 0;
		Coord2DDbl diff;
		int i = this.pointArr.length;
		while (i-- > 1)
		{
			diff = this.pointArr[i].subtract(this.pointArr[i - 1]);
			leng += Math.sqrt(diff.x * diff.x + diff.y * diff.y);
		}
		return leng;
	}

	@Nullable
	public double []getZList()
	{
		return this.zArr;
	}

	@Nullable
	public double []getMList()
	{
		return this.mArr;
	}

	@Nullable
	public LineString splitByPoint(@Nonnull Coord2DDbl pt)
	{
		int l;
	
		l = this.pointArr.length;
	
		double calPtX;
		double calPtY;
		double calZ;
		double calM;
		boolean isPoint;
		SharedDouble tmpX = new SharedDouble();
		SharedDouble tmpY = new SharedDouble();
		SharedDouble tmpZ = new SharedDouble();
		SharedDouble tmpM = new SharedDouble();
		SharedBool tmpBool = new SharedBool();
		int minId = this.getPointNo(pt, tmpBool, tmpX, tmpY, tmpZ, tmpM);
		isPoint = tmpBool.value;
		calPtX = tmpX.value;
		calPtY = tmpY.value;
		calZ = tmpZ.value;
		calM = tmpM.value;
		Coord2DDbl []oldPoints;
		Coord2DDbl []newPoints;
		double []oldZ;
		double []newZ;
		double []oldM;
		double []newM;
		LineString newPL;
		int nPoint = this.pointArr.length;
		if (isPoint)
		{
			if (minId == this.pointArr.length - 1 || minId == 0 || minId == -1)
			{
				return null;
			}
			
			oldPoints = this.pointArr;
			oldZ = this.zArr;
			oldM = this.mArr;
	
			newPoints = new Coord2DDbl[minId + 1];
			if (oldZ != null)
			{
				newZ = new double[minId + 1];
				ByteTool.copyArray(newZ, 0, oldZ, 0, minId + 1);
			}
			else
			{
				newZ = null;
			}
			if (oldM != null)
			{
				newM = new double[minId + 1];
				ByteTool.copyArray(newM, 0, oldM, 0, minId + 1);
			}
			else
			{
				newM = null;
			}
			l = minId + 1;
			while (l-- > 0)
			{
				newPoints[l] = oldPoints[l].clone();
			}
	
			this.pointArr = newPoints;
			this.zArr = newZ;
			this.mArr = newM;
			newPL = new LineString(this.srid, nPoint - minId, this.zArr != null, this.mArr != null);
			newPoints = newPL.getPointList();
			l = nPoint;
			while (l-- > minId)
			{
				newPoints[l - minId] = oldPoints[l].clone();
			}
			if (oldZ != null)
			{
				l = nPoint;
				newZ = newPL.getZList();
				if (newZ != null)
				{
					while (l-- > minId)
					{
						newZ[l - minId] = oldZ[l];
					}
				}
			}
			if (oldM != null)
			{
				l = nPoint;
				newM = newPL.getMList();
				if (newM != null)
				{
					while (l-- > minId)
					{
						newM[l - minId] = oldM[l];
					}
				}
			}
			return newPL;
		}
		else
		{
			oldPoints = this.pointArr;
			oldZ = this.zArr;
			oldM = this.mArr;
		
			newPoints = new Coord2DDbl[minId + 2];
			if (oldZ != null)
			{
				newZ = new double[minId + 2];
				ByteTool.copyArray(newZ, 0, oldZ, 0, minId + 1);
				newZ[minId + 1] = calZ;
			}
			else
			{
				newZ = null;
			}
			if (oldM != null)
			{
				newM = new double[minId + 2];
				ByteTool.copyArray(newM, 0, oldM, 0, minId + 1);
				newM[minId + 1] = calM;
			}
			else
			{
				newM = null;
			}
			l = minId + 1;
			while (l-- > 0)
			{
				newPoints[l] = oldPoints[l].clone();
			}
			newPoints[minId + 1] = new Coord2DDbl(calPtX, calPtY);
	
			this.pointArr = newPoints;
			this.zArr = newZ;
			this.mArr = newM;
			newPL = new LineString(this.srid, nPoint - minId, oldZ != null, oldM != null);

			newPoints = newPL.getPointList();
			l = nPoint;
			while (--l > minId)
			{
				newPoints[l - minId] = oldPoints[l].clone();
			}
			newPoints[0] = new Coord2DDbl(calPtX, calPtY);

			if (oldZ != null)
			{
				newZ = newPL.getZList();
				if (newZ != null)
				{
					l = nPoint;
					while (--l > minId)
					{
						newZ[l - minId] = oldZ[l];
					}
					newZ[0] = calZ;
				}
			}

			if (oldM != null)
			{
				newM = newPL.getMList();
				if (newM != null)
				{
					l = nPoint;
					while (--l > minId)
					{
						newM[l - minId] = oldM[l];
					}
					newM[0] = calM;
				}
			}
			return newPL;
		}
	}

	public int getPointNo(@Nonnull Coord2DDbl pt, @Nullable SharedBool isPoint, @Nullable SharedDouble calPtXOut, @Nullable SharedDouble calPtYOut, @Nullable SharedDouble calPtZOut, @Nullable SharedDouble calPtMOut)
	{
		int l;
		Coord2DDbl []points;
		double []zArr;
		double []mArr;
	
		points = this.pointArr;
		zArr = this.zArr;
		mArr = this.mArr;
	
		l = this.pointArr.length;
	
		double calBase;
		double calH;
		double calW;
		double calX;
		double calY;
		double calZ = 0;
		double calM = 0;
		double calD;
		double dist = 0x7fffffff;
		double calPtX = 0;
		double calPtY = 0;
		double calPtZ = 0;
		double calPtM = 0;
		int minId = -1;
		boolean isPointI = false;
	
		l--;
		while (l-- > 0)
		{
			calH = points[l].y - points[l + 1].y;
			calW = points[l].x - points[l + 1].x;

			if (calH == 0 && calW == 0)
			{
				calX = pt.x;
				calY = pt.y;
				if (zArr != null)
				{
					calZ = zArr[l];
				}
				else
				{
					calZ = 0;
				}
				if (mArr != null)
				{
					calM = mArr[l];
				}
				else
				{
					calM = 0;
				}
			}
			else
			{
				if (calH == 0)
				{
					calX = pt.x;
				}
				else
				{
					calX = (calBase = (calW * calW)) * pt.x;
					calBase += calH * calH;
					calX += calH * calH * points[l].x;
					calX += (pt.y - points[l].y) * calH * calW;
					calX /= calBase;

					if (calW == 0)
					{
						////////////////////////////////
						calZ = 0;
						calM = 0;
					}
				}
	
				if (calW == 0)
				{
					calY = pt.y;
				}
				else
				{
					double ratio = (calX - (points[l].x)) / calW;
					calY = (ratio * calH) + points[l].y;
					if (zArr != null)
					{
						calZ = (ratio * (zArr[l] - zArr[l + 1])) + zArr[l];
					}
					else
					{
						calZ = 0;
					}
					if (mArr != null)
					{
						calM = (ratio * (mArr[l] - mArr[l + 1])) + mArr[l];
					}
					else
					{
						calM = 0;
					}
				}
			}

			if (calW < 0)
			{
				if (points[l + 0].x > calX)
					continue;
				if (points[l + 1].x < calX)
					continue;
			}
			else
			{
				if (points[l + 0].x < calX)
					continue;
				if (points[l + 1].x > calX)
					continue;
			}

			if (calH < 0)
			{
				if (points[l + 0].y > calY)
					continue;
				if (points[l + 1].y < calY)
					continue;
			}
			else
			{
				if (points[l + 0].y < calY)
					continue;
				if (points[l + 1].y > calY)
					continue;
			}

			calH = pt.y - calY;
			calW = pt.x - calX;
			calD = calW * calW + calH * calH;
			if (calD < dist)
			{
				dist = calD;
				calPtX = calX;
				calPtY = calY;
				calPtZ = calZ;
				calPtM = calM;
				isPointI = false;
				minId = l;
			}
		}
		l = this.pointArr.length;
		while (l-- > 0)
		{
			calH = pt.y - points[l].y;
			calW = pt.x - points[l].x;
			calD = calW * calW + calH * calH;
			if (calD < dist)
			{
				dist = calD;
				calPtX = points[l].x;
				calPtY = points[l].y;
				calPtZ = (zArr != null)?zArr[l]:0;
				calPtM = (mArr != null)?mArr[l]:0;
				minId = l;
				isPointI = true;
			}
		}
	
		if (isPoint != null)
		{
			isPoint.value = isPointI;
		}
		if (calPtXOut != null)
		{
			calPtXOut.value = calPtX;
		}
		if (calPtYOut != null)
		{
			calPtYOut.value = calPtY;
		}
		if (calPtZOut != null)
		{
			calPtZOut.value = calPtZ;
		}
		if (calPtMOut != null)
		{
			calPtMOut.value = calPtM;
		}
		return minId;
	}

	@Nullable
	public Polygon createPolygonByDist(double dist)
	{
		int nPoint = this.pointArr.length;
		if (nPoint < 2)
			return null;

		List<Double> outPoints;
		double lastPtX = 0;
		double lastPtY = 0;
		double thisPtX = 0;
		double thisPtY = 0;
		double nextPtX;
		double nextPtY;
		double t1;
		double t2;
		double deg;
		int i;
		outPoints = new ArrayList<Double>();

		deg = Math.atan2(this.pointArr[1].x - this.pointArr[0].x, this.pointArr[1].y - this.pointArr[0].y);
		lastPtX = -Math.cos(deg) * dist + this.pointArr[0].x;
		lastPtY = Math.sin(deg) * dist + this.pointArr[0].y;

		outPoints.add(lastPtX);
		outPoints.add(lastPtY);

		i = 2;
		while (i < nPoint)
		{
			deg = Math.atan2(this.pointArr[i].x - this.pointArr[i - 1].x, this.pointArr[i].y - this.pointArr[i - 1].y);
			nextPtX = -Math.cos(deg) * dist + this.pointArr[i - 1].x;
			nextPtY = Math.sin(deg) * dist + this.pointArr[i - 1].y;

			t1 = (this.pointArr[i - 2].y - this.pointArr[i - 1].y) / (this.pointArr[i - 2].x - this.pointArr[i - 1].x);
			t2 = (this.pointArr[i - 1].y - this.pointArr[i + 0].y) / (this.pointArr[i - 1].x - this.pointArr[i + 0].x);
			if (t1 != t2)
			{
				double x1 = this.pointArr[i - 2].x;
				double x2 = this.pointArr[i - 1].x;
				double x3 = this.pointArr[i + 0].x;
				double x4 = lastPtX;
				double x6 = nextPtX;
				double y1 = this.pointArr[i - 2].y;
				double y2 = this.pointArr[i - 1].y;
				double y3 = this.pointArr[i + 0].y;
				double y4 = lastPtY;
				double y6 = nextPtY;

				thisPtX = (x4 * (x2 - x3) * (y2 - y1) - x6 * (x2 - x1) * (y2 - y3) + y6 * (x2 - x1) * (x2 - x3) - y4 * (x2 - x1) * (x2 - x3)) / ((y2 - y1) * (x2 - x3) - (x2 - x1) * (y2 - y3));
				if ((x2 - x1) == 0)
					thisPtY = y6 + (y2 - y3) / (x2 - x3) * (thisPtX - x6);
				else
					thisPtY = y4 + (y2 - y1) / (x2 - x1) * (thisPtX - x4);

				outPoints.add(thisPtX);
				outPoints.add(thisPtY);

			}
			lastPtX = thisPtX;
			lastPtY = thisPtY;
			i += 1;
		}

		deg = Math.atan2(this.pointArr[nPoint - 1].x - this.pointArr[nPoint - 2].x, this.pointArr[nPoint - 1].y - this.pointArr[nPoint - 2].y);
		lastPtX = -Math.cos(deg) * dist + this.pointArr[nPoint - 1].x;
		lastPtY = Math.sin(deg) * dist + this.pointArr[nPoint - 1].y;

		outPoints.add(lastPtX);
		outPoints.add(lastPtY);

		lastPtX = Math.cos(deg) * dist + this.pointArr[nPoint - 1].x;
		lastPtY = -Math.sin(deg) * dist + this.pointArr[nPoint - 1].y;

		outPoints.add(lastPtX);
		outPoints.add(lastPtY);

		i = nPoint;
		while (i > 2)
		{
			i -= 1;
			deg = Math.atan2(this.pointArr[i - 2].x - this.pointArr[i - 1].x, this.pointArr[i - 2].y - this.pointArr[i - 1].y);
			nextPtX = -Math.cos(deg) * dist + this.pointArr[i - 2].x;
			nextPtY = Math.sin(deg) * dist + this.pointArr[i - 2].y;

			t2 = (this.pointArr[i - 2].y - this.pointArr[i - 1].y) / (this.pointArr[i - 2].x - this.pointArr[i - 1].x);
			t1 = (this.pointArr[i - 1].y - this.pointArr[i + 0].y) / (this.pointArr[i - 1].x - this.pointArr[i + 0].x);

			if (t1 != t2)
			{
				double x1 = this.pointArr[i + 0].x;
				double x2 = this.pointArr[i - 1].x;
				double x3 = this.pointArr[i - 2].x;
				double x4 = lastPtX;
				double x6 = nextPtX;
				double y1 = this.pointArr[i + 0].y;
				double y2 = this.pointArr[i - 1].y;
				double y3 = this.pointArr[i - 2].y;
				double y4 = lastPtY;
				double y6 = nextPtY;



				thisPtX = (x4 * (x2 - x3) * (y2 - y1) - x6 * (x2 - x1) * (y2 - y3) + y6 * (x2 - x1) * (x2 - x3) - y4 * (x2 - x1) * (x2 - x3)) / ((y2 - y1) * (x2 - x3) - (x2 - x1) * (y2 - y3));
				if ((x2 - x1) == 0)
					thisPtY = y6 + (y2 - y3) / (x2 - x3) * (thisPtX - x6);
				else
					thisPtY = y4 + (y2 - y1) / (x2 - x1) * (thisPtX - x4);

				outPoints.add(thisPtX);
				outPoints.add(thisPtY);
			}

			lastPtX = thisPtX;
			lastPtY = thisPtY;
		}
		deg = Math.atan2(this.pointArr[1].x - this.pointArr[0].x, this.pointArr[1].y - this.pointArr[0].y);

		lastPtX = Math.cos(deg) * dist + this.pointArr[0].x;
		lastPtY = -Math.sin(deg) * dist + this.pointArr[0].y;

		outPoints.add(lastPtX);
		outPoints.add(lastPtY);

		Polygon pg;
		LinearRing lr;
		int nPoints;
		Coord2DDbl[] pts;
		pg = new Polygon(this.srid);
		lr = new LinearRing(this.srid, outPoints.size() >> 1, false, false);
		pts = lr.getPointList();
		nPoints = pts.length;
		i = 0;
		while (i < nPoints)
		{
			pts[i].x = outPoints.get((i << 1) + 0);
			pts[i].y = outPoints.get((i << 1) + 1);
			i++;
		}
		pg.addGeometry(lr);
		return pg;
	}

	@Nonnull
	public Polyline createPolyline()
	{
		Polyline pl = new Polyline(this.srid);
		pl.addGeometry((LineString)this.clone());
		return pl;
	}
}
