package org.sswr.util.math.geometry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.sswr.util.data.ByteTool;
import org.sswr.util.data.SharedBool;
import org.sswr.util.data.SharedDouble;
import org.sswr.util.math.Coord2DDbl;

public class Polyline extends PointOfstCollection
{
	protected int flags;
	protected int color;

	public Polyline(int srid, Coord2DDbl[] pointArr, boolean hasZ, boolean hasM)
	{
		super(srid, 1, pointArr.length, pointArr, hasZ, hasM);
		this.flags = 0;
		this.color = 0;
	}

	public Polyline(int srid, int nPtOfst, int nPoint, boolean hasZ, boolean hasM)
	{
		super(srid, nPtOfst, nPoint, null, hasZ, hasM);
		this.flags = 0;
		this.color = 0;
	}

	public VectorType getVectorType()
	{
		return VectorType.Polyline;
	}

	public Vector2D clone()
	{
		Polyline pl = new Polyline(this.srid, this.ptOfstArr.length, this.pointArr.length, this.hasZ(), this.hasM());
		ByteTool.copyArray(pl.pointArr, 0, this.pointArr, 0, this.pointArr.length);
		ByteTool.copyArray(pl.ptOfstArr, 0, this.ptOfstArr, 0, this.ptOfstArr.length);
		if (this.zArr != null)
		{	
			ByteTool.copyArray(pl.zArr, 0, this.zArr, 0, this.zArr.length);
		}
		if (this.mArr != null)
		{	
			ByteTool.copyArray(pl.mArr, 0, this.mArr, 0, this.mArr.length);
		}
		pl.flags = this.flags;
		pl.color = this.color;
		return pl;
	}

	public double calSqrDistance(Coord2DDbl pt, Coord2DDbl nearPt)
	{
		int k;
		int l;
		int m;
		int []ptOfsts;
		Coord2DDbl[] points;
	
		ptOfsts = this.ptOfstArr;
		points = this.pointArr;
	
		k = ptOfsts.length;
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
	
		while (k-- > 0)
		{
			m = ptOfsts[k];
			l--;
			while (l-- > m)
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
		}
		k = this.pointArr.length;
		while (k-- > 0)
		{
			calH = pt.y - points[k].y;
			calW = pt.x - points[k].x;
			calD = calW * calW + calH * calH;
			if (calD < dist)
			{
				dist = calD;
				calPtX = points[k].x;
				calPtY = points[k].y;
			}
		}
		if (nearPt != null)
		{
			nearPt.x = calPtX;
			nearPt.y = calPtY;
		}
		return dist;
	}

	public double calSqrDistance3D(Coord2DDbl pt, double z, Coord2DDbl nearPt, SharedDouble nearZ)
	{
		if (!this.hasZ())
		{
			if (nearZ != null)
				nearZ.value = z;
			return calSqrDistance(pt, nearPt);
		}
		int k;
		int l;
		int m;
		int []ptOfsts;
		Coord2DDbl[] points;
		double[] zArr;
	
		ptOfsts = this.ptOfstArr;
		points = this.pointArr;
		zArr = this.zArr;

		k = ptOfsts.length;
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
	
		while (k-- > 0)
		{
			m = ptOfsts[k];
			l--;
			while (l-- > m)
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

	public boolean joinVector(Vector2D vec)
	{
		if (vec.getVectorType() != VectorType.Polyline || this.hasZ() != vec.hasZ() || this.hasM() != vec.hasM())
		{
			return false;
		}
		Polyline pl = (Polyline)vec;
		int []newPtOfsts = new int[this.ptOfstArr.length + pl.ptOfstArr.length];
		ByteTool.copyArray(newPtOfsts, 0, this.ptOfstArr, 0, this.ptOfstArr.length);
		int i = pl.ptOfstArr.length;
		while (i-- > 0)
		{
			newPtOfsts[this.ptOfstArr.length + i] = pl.ptOfstArr[i] + this.pointArr.length;
		}
		this.ptOfstArr = newPtOfsts;

		Coord2DDbl []newPoints = new Coord2DDbl[this.pointArr.length + pl.pointArr.length];
		ByteTool.copyArray(newPoints, 0, this.pointArr, 0, this.pointArr.length);
		ByteTool.copyArray(newPoints, this.pointArr.length, pl.pointArr, 0, pl.pointArr.length);
		this.pointArr = newPoints;

		if (this.zArr != null)
		{
			double []newZ = new double[this.zArr.length + pl.zArr.length];
			ByteTool.copyArray(newZ, 0, this.zArr, 0, this.zArr.length);
			ByteTool.copyArray(newZ, this.zArr.length, pl.zArr, 0, pl.zArr.length);
			this.zArr = newZ;
		}
	
		if (this.mArr != null)
		{
			double []newM = new double[this.mArr.length + pl.mArr.length];
			ByteTool.copyArray(newM, 0, this.mArr, 0, this.mArr.length);
			ByteTool.copyArray(newM, this.mArr.length, pl.mArr, 0, pl.mArr.length);
			this.mArr = newM;
		}
	
		this.optimizePolyline();
		return true;	
	}

	public Polyline splitByPoint(Coord2DDbl pt)
	{
		int k;
		int l;
		int []ptOfsts;
	
		ptOfsts = this.ptOfstArr;
	
		k = this.ptOfstArr.length;
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
		int []oldPtOfsts;
		int []newPtOfsts;
		Coord2DDbl []oldPoints;
		Coord2DDbl []newPoints;
		double []oldZ;
		double []newZ;
		double []oldM;
		double []newM;
		Polyline newPL;
		int nPtOfst = this.ptOfstArr.length;
		int nPoint = this.pointArr.length;
		if (isPoint)
		{
			if (minId == this.pointArr.length - 1 || minId == 0 || minId == -1)
			{
				return null;
			}
			k = this.ptOfstArr.length;
			while (k-- > 1)
			{
				if (this.ptOfstArr[k] == minId || (this.ptOfstArr[k] - 1) == minId)
				{
					return null;
				}
			}
			
			oldPtOfsts = this.ptOfstArr;
			oldPoints = this.pointArr;
			oldZ = this.zArr;
			oldM = this.mArr;
	
			k = this.ptOfstArr.length;
			while (k-- > 0)
			{
				if (oldPtOfsts[k] < minId)
				{
					break;
				}
			}
			newPtOfsts = new int[k + 1];
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
			l = k + 1;
			while (l-- > 0)
			{
				newPtOfsts[l] = oldPtOfsts[l];
			}
	
			this.ptOfstArr = newPtOfsts;
			this.pointArr = newPoints;
			this.zArr = newZ;
			this.mArr = newM;
			newPL = new Polyline(this.srid, nPtOfst - k, nPoint - minId, this.zArr != null, this.mArr != null);
			newPtOfsts = newPL.getPtOfstList();
			l = this.ptOfstArr.length;
			while (--l > k)
			{
				newPtOfsts[l - k] = ptOfsts[l] - minId;
			}
			newPtOfsts[0] = 0;
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
				while (l-- > minId)
				{
					newZ[l - minId] = oldZ[l];
				}
			}
			if (oldM != null)
			{
				l = nPoint;
				newM = newPL.getMList();
				while (l-- > minId)
				{
					newM[l - minId] = oldM[l];
				}
			}
			return newPL;
		}
		else
		{
			oldPtOfsts = this.ptOfstArr;
			oldPoints = this.pointArr;
			oldZ = this.zArr;
			oldM = this.mArr;
		
			k = nPtOfst;
			while (k-- > 0)
			{
				if (oldPtOfsts[k] <= minId)
				{
					break;
				}
			}
			newPtOfsts = new int[k + 1];
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
	
			l = k + 1;
			while (l-- > 0)
			{
				newPtOfsts[l] = oldPtOfsts[l];
			}
	
			this.ptOfstArr = newPtOfsts;
			this.pointArr = newPoints;
			this.zArr = newZ;
			this.mArr = newM;
			newPL = new Polyline(this.srid, nPtOfst - k, nPoint - minId, oldZ != null, oldM != null);

			newPtOfsts = newPL.getPtOfstList();
			l = this.ptOfstArr.length;
			while (--l > k)
			{
				newPtOfsts[l - k] = ptOfsts[l] - minId;
			}
			newPtOfsts[0] = 0;

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
				l = nPoint;
				while (--l > minId)
				{
					newZ[l - minId] = oldZ[l];
				}
				newZ[0] = calZ;
			}

			if (oldM != null)
			{
				newM = newPL.getMList();
				l = nPoint;
				while (--l > minId)
				{
					newM[l - minId] = oldM[l];
				}
				newM[0] = calM;
			}
			return newPL;
		}
	}

	public void optimizePolyline()
	{
		if (this.zArr != null || this.mArr != null)
			return;
		Coord2DDbl []tmpPoints = new Coord2DDbl[this.pointArr.length];
		int nPoint = this.pointArr.length;
		int nPtOfst = this.ptOfstArr.length;
		int lastPoints = this.pointArr.length;
		int thisPoints;
		int lastChkPoint;
		int thisChkPoint;
		int i = this.ptOfstArr.length;
		int j;
		while (i-- > 0)
		{
			thisPoints = this.ptOfstArr[i];
			lastChkPoint = thisPoints;
			j = i;
			while (j-- > 0)
			{
				thisChkPoint = this.ptOfstArr[j];
				if (this.pointArr[lastChkPoint - 1].equals(this.pointArr[thisPoints]))
				{
					ByteTool.copyArray(tmpPoints, 0, this.pointArr, thisPoints, (lastPoints - thisPoints));
					if (lastPoints < nPoint)
					{
						ByteTool.copyArray(this.pointArr, lastPoints - 1, this.pointArr, lastPoints, nPoint - lastPoints);
					}
					if (lastChkPoint < thisPoints)
					{
						ByteTool.copyArray(tmpPoints, lastPoints - thisPoints, this.pointArr, lastChkPoint, thisPoints - lastChkPoint);
						ByteTool.copyArray(this.pointArr, lastChkPoint, tmpPoints, 1, lastPoints - lastChkPoint - 1);
					}
					else
					{
						ByteTool.copyArray(this.pointArr, lastChkPoint, tmpPoints, 1, lastPoints - thisPoints - 1);
					}
					nPtOfst -= 1;
					while (++j < i)
					{
						this.ptOfstArr[j] += lastPoints - thisPoints - 1;
					}
					while (j < nPtOfst)
					{
						this.ptOfstArr[j] = this.ptOfstArr[j + 1] - 1;
						j++;
					}
					nPoint -= 1;
					if (i >= nPtOfst)
					{
						thisPoints = nPoint;
					}
					else
					{
						thisPoints = this.ptOfstArr[i];
					}
					break;
				}
				else if (this.pointArr[thisChkPoint].equals(this.pointArr[lastPoints - 1]))
				{
					ByteTool.copyArray(tmpPoints, 0, this.pointArr, thisPoints, lastPoints - thisPoints);
					if (lastPoints < nPoint)
					{
						ByteTool.copyArray(this.pointArr, lastPoints - 1, this.pointArr, lastPoints, nPoint - lastPoints);
					}
					ByteTool.copyArray(tmpPoints, lastPoints - thisPoints, this.pointArr, thisChkPoint + 1, thisPoints - thisChkPoint - 1);
					ByteTool.copyArray(this.pointArr, thisChkPoint, tmpPoints, 0, lastPoints - thisChkPoint - 1);
					nPtOfst -= 1;
					while (++j < i)
					{
						this.ptOfstArr[j] += lastPoints - thisPoints - 1;
					}
					while (j < nPtOfst)
					{
						this.ptOfstArr[j] = this.ptOfstArr[j + 1] - 1;
						j++;
					}
					nPoint -= 1;
					if (i >= nPtOfst)
					{
						thisPoints = nPoint;
					}
					else
					{
						thisPoints = this.ptOfstArr[i];
					}
					break;
				}
				else if (this.pointArr[thisChkPoint].equals(this.pointArr[thisPoints]))
				{
					Coord2DDbl []srcPt;
					int destPtOfst;
					int ptCnt;
	
					ByteTool.copyArray(tmpPoints, 0, this.pointArr, thisPoints, lastPoints - thisPoints);
					if (lastPoints < nPoint)
					{
						ByteTool.copyArray(this.pointArr, lastPoints - 1, this.pointArr, lastPoints, nPoint - lastPoints);
					}
					ByteTool.copyArray(this.pointArr, (thisChkPoint + lastPoints - thisPoints - 1) << 1, this.pointArr, thisChkPoint << 1, 2 * (thisPoints - thisChkPoint));
	
					srcPt = tmpPoints;
					destPtOfst = thisChkPoint;
					ptCnt = (lastPoints - thisPoints - 1);
					while (ptCnt-- > 0)
					{
						this.pointArr[destPtOfst] = srcPt[ptCnt + 1].clone();
						destPtOfst += 1;
					}
					nPtOfst -= 1;
					while (++j < i)
					{
						this.ptOfstArr[j] += lastPoints - thisPoints - 1;
					}
					while (j < nPtOfst)
					{
						this.ptOfstArr[j] = this.ptOfstArr[j + 1] - 1;
						j++;
					}
					nPoint -= 1;
					if (i >= nPtOfst)
					{
						thisPoints = nPoint;
					}
					else
					{
						thisPoints = this.ptOfstArr[i];
					}
					break;
				}
				else if (this.pointArr[lastChkPoint - 1].equals(this.pointArr[lastPoints - 1]))
				{
					Coord2DDbl []srcPt;
					int destPtOfst;
					int ptCnt;
	
					ByteTool.copyArray(tmpPoints, 0, this.pointArr, thisPoints, lastPoints - thisPoints);
					if (lastPoints < nPoint)
					{
						ByteTool.copyArray(this.pointArr, lastPoints - 1, this.pointArr, lastPoints, nPoint - lastPoints);
					}
					if (lastChkPoint < thisPoints)
					{
						ByteTool.copyArray(this.pointArr, lastChkPoint + lastPoints - thisPoints - 1, this.pointArr, lastChkPoint, thisPoints - lastChkPoint);
					}
					srcPt = tmpPoints;
					destPtOfst = lastChkPoint;
					ptCnt = (lastPoints - thisPoints - 1);
					while (ptCnt-- > 0)
					{
						this.pointArr[destPtOfst] = srcPt[ptCnt].clone();
						destPtOfst += 1;
					}
					nPtOfst -= 1;
					while (++j < i)
					{
						this.ptOfstArr[j] += lastPoints - thisPoints - 1;
					}
					while (j < nPtOfst)
					{
						this.ptOfstArr[j] = this.ptOfstArr[j + 1] - 1;
						j++;
					}
					nPoint -= 1;
					if (i >= nPtOfst)
					{
						thisPoints = nPoint;
					}
					else
					{
						thisPoints = this.ptOfstArr[i];
					}
					break;
				}
				lastChkPoint = thisChkPoint;
			}
			lastPoints = thisPoints;
		}
		if (nPoint != this.pointArr.length << 1)
		{
			this.pointArr = Arrays.copyOf(this.pointArr, nPoint << 1);
		}
		if (nPtOfst != this.ptOfstArr.length)
		{
			this.ptOfstArr = Arrays.copyOf(this.ptOfstArr, nPtOfst);
		}	
	}

	public int getPointNo(Coord2DDbl pt, SharedBool isPoint, SharedDouble calPtXOut, SharedDouble calPtYOut, SharedDouble calPtZOut, SharedDouble calPtMOut)
	{
		int k;
		int l;
		int m;
		int []ptOfsts;
		Coord2DDbl []points;
		double []zArr;
		double []mArr;
	
		ptOfsts = this.ptOfstArr;
		points = this.pointArr;
		zArr = this.zArr;
		mArr = this.mArr;
	
		k = this.ptOfstArr.length;
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
	
		while (k-- > 0)
		{
			m = ptOfsts[k];
			l--;
			while (l-- > m)
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
		}
		k = this.pointArr.length;
		while (k-- > 0)
		{
			calH = pt.y - points[k].y;
			calW = pt.x - points[k].x;
			calD = calW * calW + calH * calH;
			if (calD < dist)
			{
				dist = calD;
				calPtX = points[k].x;
				calPtY = points[k].y;
				calPtZ = (zArr != null)?zArr[k]:0;
				calPtM = (mArr != null)?mArr[k]:0;
				minId = k;
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

	public Polygon createPolygonByDist(double dist)
	{
		int nPoint = this.pointArr.length;
		if (nPoint < 2)
			return null;
		if (this.ptOfstArr.length > 1)
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
		int nPoints;
		Coord2DDbl []pts;
		pg = new Polygon(this.srid, 1, outPoints.size() >> 1, false, false);
		pts = pg.getPointList();
		nPoints = pts.length;
		i = 0;
		while (i < nPoints)
		{
			pts[i].x = outPoints.get((i << 1) + 0);
			pts[i].y = outPoints.get((i << 1) + 1);
			i++;
		}
		return pg;
	}

	public boolean hasColor()
	{
		return (this.flags & 1) != 0;
	}

	public int getColor()
	{
		return this.color;
	}

	public void setColor(int color)
	{
		this.color = color;
		this.flags |= 1;
	}
}
