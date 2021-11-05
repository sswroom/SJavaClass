package org.sswr.util.math;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.sswr.util.data.ByteTool;
import org.sswr.util.data.SharedBool;
import org.sswr.util.data.SharedDouble;

public class Polyline extends PointCollection
{
	protected double[] pointArr;
	protected int[] ptOfstArr;
	protected int flags;
	protected int color;

	public Polyline(int srid, double[] pointArr)
	{
		super(srid);
		this.pointArr = pointArr.clone();
		this.ptOfstArr = new int[1];;
		this.ptOfstArr[0] = 0;
		this.flags = 0;
		this.color = 0;
	}

	public Polyline(int srid, int nPtOfst, int nPoint)
	{
		super(srid);
		if (nPtOfst == 0)
		{
			nPtOfst = 1;
		}
		this.pointArr = new double[nPoint * 2];
		this.ptOfstArr = new int[nPtOfst];
		this.flags = 0;
		this.color = 0;
	}

	public VectorType getVectorType()
	{
		return VectorType.Polyline;
	}

	public int[] getPtOfstList()
	{
		return this.ptOfstArr;
	}

	public double[] getPointList()
	{
		return this.pointArr;
	}

	public void getCenter(SharedDouble x, SharedDouble y)
	{
		double maxLength = 0;
		int maxId = 0;
		double currLength;
		int i = (this.pointArr.length >> 1) - 1;
		int j = this.ptOfstArr.length;
		int k;
		double lastX;
		double lastY;
		double thisX;
		double thisY;
		while (j-- > 0)
		{
			lastX = this.pointArr[(i << 1)];
			lastY = this.pointArr[(i << 1) + 1];
			currLength = 0;
			k = this.ptOfstArr[j];
			while (i-- > k)
			{
				thisX = this.pointArr[(i << 1)];
				thisY = this.pointArr[(i << 1) + 1];
				currLength += Math.sqrt((thisX - lastX) * (thisX - lastX) + (thisY - lastY) * (thisY - lastY));
				lastX = thisX;
				lastY = thisY;
			}
			if (currLength > maxLength)
			{
				maxLength = currLength;
				maxId = j;
			}
		}
	
		if (maxLength == 0)
		{
			x.value = this.pointArr[0];
			y.value = this.pointArr[1];
			return;
		}
		i = this.ptOfstArr[maxId];
		if (maxId >= this.ptOfstArr.length - 1)
		{
			j = this.pointArr.length >> 1;
		}
		else
		{
			j = this.ptOfstArr[maxId + 1];
		}
		maxLength = maxLength * 0.5;
		lastX = this.pointArr[i << 1];
		lastY = this.pointArr[(i << 1) + 1];
		while (i < j)
		{
			i++;
			thisX = this.pointArr[(i << 1)];
			thisY = this.pointArr[(i << 1) + 1];
			currLength = Math.sqrt((thisX - lastX) * (thisX - lastX) + (thisY - lastY) * (thisY - lastY));
			if (currLength >= maxLength)
			{
				x.value = lastX + (thisX - lastX) * maxLength / currLength;
				y.value = lastY + (thisY - lastY) * maxLength / currLength;
				return;
			}
			else
			{
				maxLength -= currLength;
			}
			lastX = thisX;
			lastY = thisY;
		}
		x.value = this.pointArr[0];
		y.value = this.pointArr[1];
	}

	public Vector2D clone()
	{
		Polyline pl = new Polyline(this.srid, this.ptOfstArr.length, this.pointArr.length >> 1);
		pl.pointArr = this.pointArr.clone();
		pl.ptOfstArr = this.ptOfstArr.clone();
		pl.flags = this.flags;
		pl.color = this.color;
		return pl;
	}

	public void getBounds(SharedDouble minX, SharedDouble minY, SharedDouble maxX, SharedDouble maxY)
	{
		int i = this.pointArr.length;
		double x1;
		double y1;
		double x2;
		double y2;
		x1 = x2 = this.pointArr[0];
		y1 = y2 = this.pointArr[1];
		while (i > 2)
		{
			i -= 2;
			if (x1 > this.pointArr[i])
			{
				x1 = this.pointArr[i];
			}
			if (x2 < this.pointArr[i])
			{
				x2 = this.pointArr[i];
			}
			if (y1 > this.pointArr[i + 1])
			{
				y1 = this.pointArr[i + 1];
			}
			if (y2 < this.pointArr[i + 1])
			{
				y2 = this.pointArr[i + 1];
			}
			i -= 2;
		}
		minX.value = x1;
		minY.value = y1;
		maxX.value = x2;
		maxY.value = y2;
	}

	public double calSqrDistance(double x, double y, SharedDouble nearPtX, SharedDouble nearPtY)
	{
		int k;
		int l;
		int m;
		int []ptOfsts;
		double[] points;
	
		ptOfsts = this.ptOfstArr;
		points = this.pointArr;
	
		k = ptOfsts.length;
		l = points.length >> 1;
	
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
				calH = points[(l << 1) + 1] - points[(l << 1) + 3];
				calW = points[(l << 1) + 0] - points[(l << 1) + 2];
	
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
				}
				else
				{
					calY = ((calX - (points[(l << 1) + 0])) * calH / calW) + points[(l << 1) + 1];
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
				}
			}
		}
		k = this.pointArr.length >> 1;
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
			}
		}
		if (nearPtX != null && nearPtY != null)
		{
			nearPtX.value = calPtX;
			nearPtY.value = calPtY;
		}
		return dist;
	}

	public boolean joinVector(Vector2D vec)
	{
		if (vec.getVectorType() != VectorType.Polyline)
		{
			return false;
		}
		Polyline pl = (Polyline)vec;
		int []newPtOfsts = new int[this.ptOfstArr.length + pl.ptOfstArr.length];
		double []newPoints = new double[this.pointArr.length + pl.pointArr.length];
		ByteTool.copyArray(newPtOfsts, 0, this.ptOfstArr, 0, this.ptOfstArr.length);
		ByteTool.copyArray(newPoints, 0, this.pointArr, 0, this.pointArr.length);
		ByteTool.copyArray(newPoints, this.pointArr.length, pl.pointArr, 0, pl.pointArr.length);
		int i = pl.ptOfstArr.length;
		while (i-- > 0)
		{
			newPtOfsts[this.ptOfstArr.length + i] = pl.ptOfstArr[i] + (this.pointArr.length >> 1);
		}
		this.ptOfstArr = newPtOfsts;
		this.pointArr = newPoints;
		this.optimizePolyline();
		return true;	
	}

	public void convCSys(CoordinateSystem srcCSys, CoordinateSystem destCSys)
	{
		SharedDouble x = new SharedDouble();
		SharedDouble y = new SharedDouble();
		int i = this.pointArr.length;
		while (i-- > 0)
		{
			CoordinateSystem.convertXYZ(srcCSys, destCSys, this.pointArr[(i << 1)], this.pointArr[(i << 1) + 1], 0, x, y, null);
			this.pointArr[(i << 1)] = x.value;
			this.pointArr[(i << 1) + 1] = y.value;
		}
	}

	public Polyline splitByPoint(double x, double y)
	{
		int k;
		int l;
		int []ptOfsts;
	
		ptOfsts = this.ptOfstArr;
	
		k = this.ptOfstArr.length;
		l = this.pointArr.length >> 1;
	
		double calPtX;
		double calPtY;
		boolean isPoint;
		SharedDouble tmpX = new SharedDouble();
		SharedDouble tmpY = new SharedDouble();
		SharedBool tmpBool = new SharedBool();
		int minId = this.getPointNo(x, y, tmpBool, tmpX, tmpY);
		isPoint = tmpBool.value;
		calPtX = tmpX.value;
		calPtY = tmpY.value;
		int []oldPtOfsts;
		int []newPtOfsts;
		double []oldPoints;
		double []newPoints;
		Polyline newPL;
		if (isPoint)
		{
			if (minId == (this.pointArr.length >> 1) - 1 || minId == 0 || minId == -1)
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
	
			k = this.ptOfstArr.length;
			while (k-- > 0)
			{
				if (oldPtOfsts[k] < minId)
				{
					break;
				}
			}
			newPtOfsts = new int[k + 1];
			newPoints = new double[(minId + 1) * 2];
			l = minId + 1;
			while (l-- > 0)
			{
				newPoints[(l << 1) + 0] = oldPoints[(l << 1) + 0];
				newPoints[(l << 1) + 1] = oldPoints[(l << 1) + 1];
			}
			l = k + 1;
			while (l-- > 0)
			{
				newPtOfsts[l] = oldPtOfsts[l];
			}
	
			newPL = new Polyline(this.srid, this.ptOfstArr.length - k, (this.pointArr.length >> 1) - minId);
			newPtOfsts = newPL.getPtOfstList();
			l = this.ptOfstArr.length;
			while (--l > k)
			{
				newPtOfsts[l - k] = ptOfsts[l] - minId;
			}
			newPtOfsts[0] = 0;
			newPoints = newPL.getPointList();
			l = this.pointArr.length >> 1;
			while (l-- > minId)
			{
				newPoints[((l - minId) << 1) + 0] = oldPoints[(l << 1) + 0];
				newPoints[((l - minId) << 1) + 1] = oldPoints[(l << 1) + 1];
			}
			this.ptOfstArr = newPtOfsts;
			this.pointArr = newPoints;
	
			return newPL;
		}
		else
		{
			oldPtOfsts = this.ptOfstArr;
			oldPoints = this.pointArr;
	
			k = this.ptOfstArr.length;
			while (k-- > 0)
			{
				if (oldPtOfsts[k] <= minId)
				{
					break;
				}
			}
			newPtOfsts = new int[k + 1];
			newPoints = new double[(minId + 2) * 2];
			l = minId + 1;
			while (l-- > 0)
			{
				newPoints[(l << 1) + 0] = oldPoints[(l << 1) + 0];
				newPoints[(l << 1) + 1] = oldPoints[(l << 1) + 1];
			}
			newPoints[((minId + 1) << 1) + 0] = calPtX;
			newPoints[((minId + 1) << 1) + 1] = calPtY;
	
			l = k + 1;
			while (l-- > 0)
			{
				newPtOfsts[l] = oldPtOfsts[l];
			}
	
			newPL = new Polyline(this.srid, this.ptOfstArr.length - k, this.pointArr.length - minId);
			newPtOfsts = newPL.getPtOfstList();
			l = this.ptOfstArr.length;
			while (--l > k)
			{
				newPtOfsts[l - k] = ptOfsts[l] - minId;
			}
			newPtOfsts[0] = 0;
			newPoints = newPL.getPointList();
			l = this.pointArr.length >> 1;
			while (--l > minId)
			{
				newPoints[((l - minId) << 1) + 0] = oldPoints[(l << 1) + 0];
				newPoints[((l - minId) << 1) + 1] = oldPoints[(l << 1) + 1];
			}
			newPoints[0] = calPtX;
			newPoints[1] = calPtY;
	
			this.ptOfstArr = newPtOfsts;
			this.pointArr = newPoints;
	
			return newPL;
		}
	}

	public void optimizePolyline()
	{
		double []tmpPoints = new double[this.pointArr.length];
		int nPoint = this.pointArr.length >> 1;
		int nPtOfst = this.ptOfstArr.length;
		int lastPoints = this.pointArr.length >> 1;
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
				if (this.pointArr[((lastChkPoint - 1) << 1)] == this.pointArr[(thisPoints << 1)] && this.pointArr[((lastChkPoint - 1) << 1) + 1] == this.pointArr[(thisPoints << 1) + 1])
				{
					ByteTool.copyArray(tmpPoints, 0, this.pointArr, thisPoints << 1, 2 * (lastPoints - thisPoints));
					if (lastPoints < nPoint)
					{
						ByteTool.copyArray(this.pointArr, (lastPoints << 1) - 2, this.pointArr, (lastPoints << 1), 2 * (nPoint - lastPoints));
					}
					if (lastChkPoint < thisPoints)
					{
						ByteTool.copyArray(tmpPoints, 2 * (lastPoints - thisPoints), this.pointArr, lastChkPoint << 1, 2 * (thisPoints - lastChkPoint));
						ByteTool.copyArray(this.pointArr, lastChkPoint << 1, tmpPoints, 2, 2 * (lastPoints - lastChkPoint - 1));
					}
					else
					{
						ByteTool.copyArray(this.pointArr, lastChkPoint << 1, tmpPoints, 2, 2 * (lastPoints - thisPoints - 1));
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
				else if (this.pointArr[(thisChkPoint << 1)] == this.pointArr[((lastPoints - 1) << 1)] && this.pointArr[(thisChkPoint << 1) + 1] == this.pointArr[((lastPoints - 1) << 1) + 1])
				{
					ByteTool.copyArray(tmpPoints, 0, this.pointArr, thisPoints << 1, 2 * (lastPoints - thisPoints));
					if (lastPoints < nPoint)
					{
						ByteTool.copyArray(this.pointArr, (lastPoints << 1) - 2, this.pointArr, (lastPoints << 1), 2 * (nPoint - lastPoints));
					}
					ByteTool.copyArray(tmpPoints, (lastPoints - thisPoints) << 1, this.pointArr, (thisChkPoint + 1) << 1, 2 * (thisPoints - thisChkPoint - 1));
					ByteTool.copyArray(this.pointArr, thisChkPoint << 1, tmpPoints, 0, 2 * (lastPoints - thisChkPoint - 1));
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
				else if (this.pointArr[(thisChkPoint << 1)] == this.pointArr[(thisPoints << 1)] && this.pointArr[(thisChkPoint << 1) + 1] == this.pointArr[(thisPoints << 1) + 1])
				{
					double []srcPt;
					int destOfst;
					int ptCnt;
	
					ByteTool.copyArray(tmpPoints, 0, this.pointArr, thisPoints << 1, 2 * (lastPoints - thisPoints));
					if (lastPoints < nPoint)
					{
						ByteTool.copyArray(this.pointArr, (lastPoints << 1) - 2, this.pointArr, (lastPoints << 1), 2 * (nPoint - lastPoints));
					}
					ByteTool.copyArray(this.pointArr, (thisChkPoint + lastPoints - thisPoints - 1) << 1, this.pointArr, thisChkPoint << 1, 2 * (thisPoints - thisChkPoint));
	
					srcPt = tmpPoints;
					destOfst = thisChkPoint << 1;
					ptCnt = (lastPoints - thisPoints - 1);
					while (ptCnt-- > 0)
					{
						this.pointArr[destOfst] = srcPt[(ptCnt << 1) + 2];
						this.pointArr[destOfst + 1] = srcPt[(ptCnt << 1) + 3];
						destOfst += 2;
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
				else if (this.pointArr[((lastChkPoint - 1) << 1)] == this.pointArr[((lastPoints - 1) << 1)] && this.pointArr[((lastChkPoint - 1) << 1) + 1] == this.pointArr[((lastPoints - 1) << 1) + 1])
				{
					double []srcPt;
					int destOfst;
					int ptCnt;
	
					ByteTool.copyArray(tmpPoints, 0, this.pointArr, thisPoints << 1, 2 * (lastPoints - thisPoints));
					if (lastPoints < nPoint)
					{
						ByteTool.copyArray(this.pointArr, (lastPoints << 1) - 2, this.pointArr, (lastPoints << 1), 2 * (nPoint - lastPoints));
					}
					if (lastChkPoint < thisPoints)
					{
						ByteTool.copyArray(this.pointArr, (lastChkPoint + lastPoints - thisPoints - 1) << 1, this.pointArr, lastChkPoint << 1, 2 * (thisPoints - lastChkPoint));
					}
					srcPt = tmpPoints;
					destOfst = lastChkPoint << 1;
					ptCnt = (lastPoints - thisPoints - 1);
					while (ptCnt-- > 0)
					{
						this.pointArr[destOfst] = srcPt[(ptCnt << 1)];
						this.pointArr[destOfst + 1] = srcPt[(ptCnt << 1) + 1];
						destOfst += 2;
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

	public int getPointNo(double x, double y, SharedBool isPoint, SharedDouble calPtXOut, SharedDouble calPtYOut)
	{
		int k;
		int l;
		int m;
		int []ptOfsts;
		double []points;
	
		ptOfsts = this.ptOfstArr;
		points = this.pointArr;
	
		k = this.ptOfstArr.length;
		l = this.pointArr.length >> 1;
	
		double calBase;
		double calH;
		double calW;
		double calX;
		double calY;
		double calD;
		double dist = 0x7fffffff;
		double calPtX = 0;
		double calPtY = 0;
		int minId = -1;
		boolean isPointI = false;
	
		while (k-- > 0)
		{
			m = ptOfsts[k];
			l--;
			while (l-- > m)
			{
				calH = points[(l << 1) + 1] - points[(l << 1) + 3];
				calW = points[(l << 1) + 0] - points[(l << 1) + 2];
	
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
				}
				else
				{
					calY = ((calX - (points[(l << 1) + 0])) * calH / calW) + points[(l << 1) + 1];
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
					isPointI = false;
					minId = l;
				}
			}
		}
		k = this.pointArr.length >> 1;
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
		return minId;
	}

	public Polygon createPolygonByDist(double dist)
	{
		int nPoint = this.pointArr.length >> 1;
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

		deg = Math.atan2(this.pointArr[2] - this.pointArr[0], this.pointArr[3] - this.pointArr[1]);
		lastPtX = -Math.cos(deg) * dist + this.pointArr[0];
		lastPtY = Math.sin(deg) * dist + this.pointArr[1];

		outPoints.add(lastPtX);
		outPoints.add(lastPtY);

		i = 2;
		while (i < nPoint)
		{
			deg = Math.atan2(this.pointArr[(i << 1) + 0] - this.pointArr[(i << 1) - 2], this.pointArr[(i << 1) + 1] - this.pointArr[(i << 1) - 1]);
			nextPtX = -Math.cos(deg) * dist + this.pointArr[(i << 1) - 2];
			nextPtY = Math.sin(deg) * dist + this.pointArr[(i << 1) - 1];

			t1 = (this.pointArr[(i << 1) - 3] - this.pointArr[(i << 1) - 1]) / (this.pointArr[(i << 1) - 4] - this.pointArr[(i << 1) - 2]);
			t2 = (this.pointArr[(i << 1) - 1] - this.pointArr[(i << 1) + 1]) / (this.pointArr[(i << 1) - 2] - this.pointArr[(i << 1) + 0]);
			if (t1 != t2)
			{
				double x1 = this.pointArr[(i << 1) - 4];
				double x2 = this.pointArr[(i << 1) - 2];
				double x3 = this.pointArr[(i << 1) + 0];
				double x4 = lastPtX;
				double x6 = nextPtX;
				double y1 = this.pointArr[(i << 1) - 3];
				double y2 = this.pointArr[(i << 1) - 1];
				double y3 = this.pointArr[(i << 1) + 1];
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

		deg = Math.atan2(this.pointArr[(nPoint << 1) - 2] - this.pointArr[(nPoint << 1) - 4], this.pointArr[(nPoint << 1) - 1] - this.pointArr[(nPoint << 1) - 3]);
		lastPtX = -Math.cos(deg) * dist + this.pointArr[(nPoint << 1) - 2];
		lastPtY = Math.sin(deg) * dist + this.pointArr[(nPoint << 1) - 1];

		outPoints.add(lastPtX);
		outPoints.add(lastPtY);

		lastPtX = Math.cos(deg) * dist + this.pointArr[(nPoint << 1) - 2];
		lastPtY = -Math.sin(deg) * dist + this.pointArr[(nPoint << 1) - 1];

		outPoints.add(lastPtX);
		outPoints.add(lastPtY);

		i = nPoint;
		while (i > 2)
		{
			i -= 1;
			deg = Math.atan2(this.pointArr[(i << 1) - 4] - this.pointArr[(i << 1) - 2], this.pointArr[(i << 1) - 3] - this.pointArr[(i << 1) - 1]);
			nextPtX = -Math.cos(deg) * dist + this.pointArr[(i << 1) - 4];
			nextPtY = Math.sin(deg) * dist + this.pointArr[(i << 1) - 3];

			t2 = (this.pointArr[(i << 1) - 3] - this.pointArr[(i << 1) - 1]) / (this.pointArr[(i << 1) - 4] - this.pointArr[(i << 1) - 2]);
			t1 = (this.pointArr[(i << 1) - 1] - this.pointArr[(i << 1) + 1]) / (this.pointArr[(i << 1) - 2] - this.pointArr[(i << 1) + 0]);

			if (t1 != t2)
			{
				double x1 = this.pointArr[(i << 1) + 0];
				double x2 = this.pointArr[(i << 1) - 2];
				double x3 = this.pointArr[(i << 1) - 4];
				double x4 = lastPtX;
				double x6 = nextPtX;
				double y1 = this.pointArr[(i << 1) + 1];
				double y2 = this.pointArr[(i << 1) - 1];
				double y3 = this.pointArr[(i << 1) - 3];
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
		deg = Math.atan2(this.pointArr[2] - this.pointArr[0], this.pointArr[3] - this.pointArr[1]);

		lastPtX = Math.cos(deg) * dist + this.pointArr[0];
		lastPtY = -Math.sin(deg) * dist + this.pointArr[1];

		outPoints.add(lastPtX);
		outPoints.add(lastPtY);

		Polygon pg;
		int nPoints;
		double []pts;
		pg = new Polygon(this.srid, 1, outPoints.size() >> 1);
		pts = pg.getPointList();
		nPoints = pts.length;
		i = 0;
		while (i < nPoints)
		{
			pts[i] = outPoints.get(i);
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
