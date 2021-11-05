package org.sswr.util.math;

import org.sswr.util.data.ByteTool;
import org.sswr.util.data.SharedDouble;

public class Polyline3D extends Polyline
{
	private double []altitudes;

	public Polyline3D(int srid, int nPtOfst, int nPoint)
	{
		super(srid, nPtOfst, nPoint);
		this.altitudes = new double[nPoint];
	}

	public Vector2D clone()
	{
		Polyline3D pl;
		pl = new Polyline3D(this.srid, this.ptOfstArr.length, this.pointArr.length >> 1);
		ByteTool.copyArray(pl.ptOfstArr, 0, this.ptOfstArr, 0, this.ptOfstArr.length);
		ByteTool.copyArray(pl.pointArr, 0, this.pointArr, 0, this.pointArr.length);
		ByteTool.copyArray(pl.altitudes, 0, this.altitudes, 0, this.altitudes.length);
		return pl;
	}

	public boolean support3D()
	{
		return true;
	}

	public boolean joinVector(Vector2D vec)
	{
		if (vec.getVectorType() != VectorType.Polyline || !vec.support3D())
		{
			return false;
		}
		////////////////////////////////////////////
		return false;
	}
	
	public void convCSys(CoordinateSystem srcCSys, CoordinateSystem destCSys)
	{
		SharedDouble x = new SharedDouble();
		SharedDouble y = new SharedDouble();
		SharedDouble z = new SharedDouble();
		int i = this.pointArr.length >> 1;
		while (i-- > 0)
		{
			CoordinateSystem.convertXYZ(srcCSys, destCSys, this.pointArr[(i << 1)], this.pointArr[(i << 1) + 1], this.altitudes[i], x, y, z);
			this.pointArr[(i << 1)] = x.value;
			this.pointArr[(i << 1) + 1] = y.value;
			this.altitudes[i] = z.value;
		}
	}

	public Polyline splitByPoint(double x, double y)
	{
		int k;
		int l;
		int m;
		int []ptOfsts;
		double []points;
	
		ptOfsts = this.ptOfstArr;
		points = this.pointArr;
	
		int nPtOfst = this.ptOfstArr.length;
		int nPoint = this.pointArr.length >> 1;
		k = nPtOfst;
		l = nPoint;
	
		double calBase;
		double calH;
		double calW;
		double calZD;
		double calX;
		double calY;
		double calZ;
		double calD;
		double dist = 0x7fffffff;
		double calPtX = 0;
		double calPtY = 0;
		double calPtZ = 0;
		int minId = 0;
		boolean isPoint = false;
	
		while (k-- > 0)
		{
			m = ptOfsts[k];
			l--;
			while (l-- > m)
			{
				calH = points[(l << 1) + 1] - points[(l << 1) + 3];
				calW = points[(l << 1) + 0] - points[(l << 1) + 2];
				calZD = altitudes[l] - altitudes[l + 1];
	
				if (calH == 0)
				{
					calX = x;
				}
				else
				{
					calX = (calBase = (calW * calW)) * x;
					calBase += calH * calH;
					calX += calH * calH * (points[(l << 1) + 0]);
					calX += (y - points[(l << 1) + 1]) * calH * calW;
					calX /= calBase;
				}
	
				if (calW == 0)
				{
					calY = y;
					if (calZD == 0)
					{
						calZ = altitudes[l];
					}
					else
					{
						calZ = ((calY - (points[(l << 1) + 1])) * calZD / calH) + altitudes[l];
					}
				}
				else
				{
					calY = ((calX - (points[(l << 1) + 0])) * calH / calW) + points[(l << 1) + 1];
					if (calZD == 0)
					{
						calZ = altitudes[l];
					}
					else
					{
						calZ = ((calX - (points[(l << 1) + 0])) * calZD / calW) + altitudes[l];
					}
				}
	
				if (calW < 0)
				{
					if (points[(l << 1) + 0] > calX)
						continue;
					if (points[(l << 1) + 2] < calX)
						continue;
				}
				else
				{
					if (points[(l << 1) + 0] < calX)
						continue;
					if (points[(l << 1) + 2] > calX)
						continue;
				}
	
				if (calH < 0)
				{
					if (points[(l << 1) + 1] > calY)
						continue;
					if (points[(l << 1) + 3] < calY)
						continue;
				}
				else
				{
					if (points[(l << 1) + 1] < calY)
						continue;
					if (points[(l << 1) + 3] > calY)
						continue;
				}
	
				calH = y - calY;
				calW = x - calX;
				calD = calW * calW + calH * calH;
				if (calD < dist)
				{
					dist = calD;
					calPtX = calX;
					calPtY = calY;
					calPtZ = calZ;
					isPoint = false;
					minId = l;
				}
			}
		}
		k = nPoint;
		while (k-- > 0)
		{
			calH = y - points[(k << 1) + 1];
			calW = x - points[(k << 1) + 0];
			calD = calW * calW + calH * calH;
			if (calD < dist)
			{
				dist = calD;
				calPtX = points[(k << 1) + 0];
				calPtY = points[(k << 1) + 1];
				calPtZ = altitudes[k];
				minId = k;
				isPoint = true;
			}
		}
		int []oldPtOfsts;
		int []newPtOfsts;
		double []oldPoints;
		double []newPoints;
		double []oldAltitudes;
		double []newAltitudes;
		Polyline3D newPL;
		if (isPoint)
		{
			if (minId == nPoint - 1 || minId == 0)
			{
				return null;
			}
			k = nPtOfst;
			while (k-- > 1)
			{
				if (this.ptOfstArr[k] == minId || (this.ptOfstArr[k] - 1) == minId)
				{
					return null;
				}
			}
			
			oldPtOfsts = this.ptOfstArr;
			oldPoints = this.pointArr;
			oldAltitudes = this.altitudes;
	
			k = nPtOfst;
			while (k-- > 0)
			{
				if (oldPtOfsts[k] < minId)
				{
					break;
				}
			}
			newPtOfsts = new int[k + 1];
			newPoints = new double[(minId + 1) * 2];
			newAltitudes = new double[minId + 1];
			l = minId + 1;
			while (l-- > 0)
			{
				newPoints[(l << 1) + 0] = oldPoints[(l << 1) + 0];
				newPoints[(l << 1) + 1] = oldPoints[(l << 1) + 1];
				newAltitudes[l] = oldAltitudes[l];
			}
			l = k + 1;
			while (l-- > 0)
			{
				newPtOfsts[l] = oldPtOfsts[l];
			}
	
			this.ptOfstArr = newPtOfsts;
			this.pointArr = newPoints;
			this.altitudes = newAltitudes;
			newPL = new Polyline3D(this.srid, nPtOfst - k, nPoint - minId);
			newPtOfsts = newPL.getPtOfstList();
			l = nPtOfst;
			while (--l > k)
			{
				newPtOfsts[l - k] = ptOfsts[l] - minId;
			}
			newPtOfsts[0] = 0;
			newPoints = newPL.getPointList();
			newAltitudes = newPL.getAltitudeList();
			l = nPoint;
			while (l-- > minId)
			{
				newPoints[((l - minId) << 1) + 0] = oldPoints[(l << 1) + 0];
				newPoints[((l - minId) << 1) + 1] = oldPoints[(l << 1) + 1];
				newAltitudes[l - minId] = oldAltitudes[l];
			}
			return newPL;
		}
		else
		{
			oldPtOfsts = this.ptOfstArr;
			oldPoints = this.pointArr;
			oldAltitudes = this.altitudes;
	
			k = nPtOfst;
			while (k-- > 0)
			{
				if (oldPtOfsts[k] < minId)
				{
					break;
				}
			}
			newPtOfsts = new int[k + 1];
			newPoints = new double[(minId + 2) * 2];
			newAltitudes = new double[minId + 2];
			l = minId + 1;
			while (l-- > 0)
			{
				newPoints[(l << 1) + 0] = oldPoints[(l << 1) + 0];
				newPoints[(l << 1) + 1] = oldPoints[(l << 1) + 1];
				newAltitudes[l] = oldAltitudes[l];
			}
			newPoints[((minId + 1) << 1) + 0] = calPtX;
			newPoints[((minId + 1) << 1) + 1] = calPtY;
			newAltitudes[minId + 1] = calPtZ;
	
			l = k + 1;
			while (l-- > 0)
			{
				newPtOfsts[l] = oldPtOfsts[l];
			}
	
			this.ptOfstArr = newPtOfsts;
			this.pointArr = newPoints;
			this.altitudes = newAltitudes;
			newPL = new Polyline3D(this.srid, nPtOfst - k, nPoint - minId);
			newPtOfsts = newPL.getPtOfstList();
			l = nPtOfst;
			while (--l > k)
			{
				newPtOfsts[l - k] = ptOfsts[l] - minId;
			}
			newPtOfsts[0] = 0;
			newPoints = newPL.getPointList();
			newAltitudes = newPL.getAltitudeList();
			l = nPoint;
			while (--l > minId)
			{
				newPoints[((l - minId) << 1) + 0] = oldPoints[(l << 1) + 0];
				newPoints[((l - minId) << 1) + 1] = oldPoints[(l << 1) + 1];
				newAltitudes[l - minId] = oldAltitudes[l];
			}
			newPoints[0] = calPtX;
			newPoints[1] = calPtY;
			newAltitudes[0] = calPtZ;
			return newPL;
		}
	}

	public double []getAltitudeList()
	{
		return this.altitudes;
	}
}
