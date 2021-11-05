package org.sswr.util.math;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.sswr.util.data.ByteTool;
import org.sswr.util.data.SharedDouble;

public class Polygon extends PointCollection
{
	protected double []pointArr;
	protected int []ptOfstArr;

	public Polygon(int srid, int nPtOfst, int nPoint)
	{
		super(srid);
		this.pointArr = new double[nPoint << 1];
		this.ptOfstArr = new int[nPtOfst];
	}

	public VectorType getVectorType()
	{
		return VectorType.Polygon;
	}

	public int []getPtOfstList()
	{
		return this.ptOfstArr;
	}

	public double []getPointList()
	{
		return this.pointArr;
	}

	public Vector2D clone()
	{
		Polygon pg = new Polygon(this.srid, this.ptOfstArr.length, this.pointArr.length >> 1);
		ByteTool.copyArray(pg.pointArr, 0, this.pointArr, 0, this.pointArr.length);
		ByteTool.copyArray(pg.ptOfstArr, 0, this.ptOfstArr, 0, this.ptOfstArr.length);
		return pg;
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
		if (this.insideVector(x, y))
		{
			if (nearPtX != null && nearPtY != null)
			{
				nearPtX.value = x;
				nearPtY.value = y;
			}
			return 0;
		}
	
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
		if (vec.getVectorType() != VectorType.Polygon)
			return false;
		Polygon pg = (Polygon)vec;
		double []newPoints;
		int nPoint = (this.pointArr.length + pg.pointArr.length) >> 1;
		int []newPtOfsts;
		int nPtOfst = this.ptOfstArr.length + pg.ptOfstArr.length;
		
		newPoints = new double[nPoint * 2];
		newPtOfsts = new int[nPtOfst];
		ByteTool.copyArray(newPoints, 0, this.pointArr, 0, this.pointArr.length);
		ByteTool.copyArray(newPoints, this.pointArr.length, pg.pointArr, 0, pg.pointArr.length);
		ByteTool.copyArray(newPtOfsts, 0, this.ptOfstArr, 0, this.ptOfstArr.length);
		int i = pg.ptOfstArr.length;
		int j = i + this.ptOfstArr.length;
		int k = this.pointArr.length >> 1;
		while (i-- > 0)
		{
			j--;
			newPtOfsts[j] = pg.ptOfstArr[i] + k;
		}
		this.ptOfstArr = newPtOfsts;
		this.pointArr = newPoints;
		return true;
	}
	
	public void convCSys(CoordinateSystem srcCSys, CoordinateSystem destCSys)
	{
		SharedDouble x = new SharedDouble();
		SharedDouble y = new SharedDouble();
		int i = this.pointArr.length >> 1;
		while (i-- > 0)
		{
			CoordinateSystem.convertXYZ(srcCSys, destCSys, this.pointArr[(i << 1)], this.pointArr[(i << 1) + 1], 0, x, y, null);
			this.pointArr[(i << 1)] = x.value;
			this.pointArr[(i << 1) + 1] = y.value;
		}
	}

	public boolean insideVector(double x, double y)
	{
		double thisX;
		double thisY;
		double lastX;
		double lastY;
		int j;
		int k;
		int l;
		int m;
		int leftCnt = 0;
		double tmpX;
	
		k = this.ptOfstArr.length;
		l = this.pointArr.length >> 1;
	
		while (k-- > 0)
		{
			m = this.ptOfstArr[k];
	
			lastX = this.pointArr[(m << 1) + 0];
			lastY = this.pointArr[(m << 1) + 1];
			while (l-- > m)
			{
				thisX = this.pointArr[(l << 1) + 0];
				thisY = this.pointArr[(l << 1) + 1];
				j = 0;
				if (lastY > y)
					j += 1;
				if (thisY > y)
					j += 1;
	
				if (j == 1)
				{
					tmpX = lastX - (lastX - thisX) * (lastY - y) / (lastY - thisY);
					if (tmpX == x)
					{
						return true;
					}
					else if (tmpX < x)
						leftCnt++;
				}
				else if (thisY == y && lastY == y)
				{
					if ((thisX >= x && lastX <= x) || (lastX >= x && thisX <= x))
					{
						return true;
					}
				}
				else if (thisY == y && thisX == x)
				{
					return true;
				}
	
				lastX = thisX;
				lastY = thisY;
			}
			l++;
		}
	
		return (leftCnt & 1) != 0;
	}

	public boolean hasJunction()
	{
		int i;
		int j;
		int k;
		int l;
		int nextPart;
		double lastPtX;
		double lastPtY;
		int lastIndex;
		double thisPtX;
		double thisPtY;
		int nextChkPart;
		double lastChkPtX;
		double lastChkPtY;
		int lastChkIndex;
		double thisChkPtX;
		double thisChkPtY;
	
		double m1;
		double m2 = 0;
		double intX;
		double intY;
	
	
		i = this.pointArr.length >> 1;
		j = this.ptOfstArr.length;
		while (j-- > 0)
		{
			nextPart = this.ptOfstArr[j];
			lastPtX = this.pointArr[(nextPart << 1) + 0];
			lastPtY = this.pointArr[(nextPart << 1) + 1];
			lastIndex = nextPart;
			while (i-- > nextPart)
			{
				thisPtX = this.pointArr[(i << 1) + 0];
				thisPtY = this.pointArr[(i << 1) + 1];
	
				if (thisPtX != lastPtX || thisPtY != lastPtY)
				{
					m1 = (lastPtY - thisPtY) / (lastPtX - thisPtX);
	
					nextChkPart = nextPart;
					lastChkPtX = thisPtX;
					lastChkPtY = thisPtY;
					lastChkIndex = i;
					k = i;
					l = j;
					l++;
					while (l-- > 0)
					{
						nextChkPart = this.ptOfstArr[l];
						if (l != j)
						{
							lastChkPtX = this.pointArr[(nextChkPart << 1) + 0];
							lastChkPtY = this.pointArr[(nextChkPart << 1) + 1];
						}
	
						while (k-- > nextChkPart)
						{
							thisChkPtX = this.pointArr[(k << 1) + 0];
							thisChkPtY = this.pointArr[(k << 1) + 1];
	
							if (k == i || k == lastIndex || lastChkIndex == i || lastChkIndex == lastIndex)
							{
							}
							else if (thisChkPtX != lastChkPtX || thisChkPtY != lastChkPtY)
							{
								if (lastChkPtX == thisChkPtX && lastPtX == thisPtX)
								{
								}
								else if (lastChkPtX == thisChkPtX)
								{
									intY = m1 * thisChkPtX - m1 * thisPtX + thisPtY;
									if (intY == lastChkPtY || intY == thisChkPtY || ((intY > thisChkPtY) ^ (intY > lastChkPtY)))
										return true;
								}
								else if (lastPtX == thisPtX)
								{
									intY = m2 * thisPtX - m2 * thisChkPtX + thisChkPtY;
									if (intY == lastPtY || intY == thisPtY || ((intY > thisPtY) ^ (intY > lastPtY)))
										return true;
								}
								else
								{
									m2 = (lastChkPtY - thisChkPtY) / (lastChkPtX - thisChkPtX);
									if (m1 != m2)
									{
										intX = (m1 * thisPtX - m2 * thisChkPtX + thisChkPtY - thisPtY) / (m1 - m2);
										if ((intX == thisChkPtX || intX == lastChkPtX || ((intX > thisChkPtX) ^ (intX > lastChkPtX))) && (intX == thisPtX || intX == lastPtX || ((intX > thisPtX) ^ (intX > lastPtX))))
											return true;
									}
								}
							}
	
							lastChkPtX = thisChkPtX;
							lastChkPtY = thisChkPtY;
							lastChkIndex = k;
						}
						k++;
					}
				}
				
	
				lastPtX = thisPtX;
				lastPtY = thisPtY;
				lastIndex = i;
			}
			i++;
		}
		return false;
	}

	public void splitByJunction(List<Polygon> results)
	{
		int i;
		int j;
		Polygon tmpPG;
		double []points;
		ArrayList<Double> junctionX;
		ArrayList<Double> junctionY;
		ArrayList<Integer> junctionPtNum;
		double lastPtX;
		double lastPtY;
		double thisPtX;
		double thisPtY;
		int lastIndex;
		double lastChkPtX;
		double lastChkPtY;
		double thisChkPtX;
		double thisChkPtY;
		int lastChkIndex;
		double m1;
		double m2 = 0;
		double intX;
		double intY;
	
		i = this.pointArr.length >> 1;
		while (this.ptOfstArr.length > 1)
		{
			j = this.ptOfstArr[this.ptOfstArr.length - 1];
			tmpPG = new Polygon(this.srid, 1, i - j);
			points = tmpPG.getPointList();
			ByteTool.copyArray(points, 0, this.pointArr, j << 1, (i - j) << 1);
			tmpPG.splitByJunction(results);
			
			this.pointArr = Arrays.copyOf(this.pointArr, j << 1);
			this.ptOfstArr = Arrays.copyOf(this.ptOfstArr, this.ptOfstArr.length - 1);
			i = j;
		}
	
		junctionX = new ArrayList<Double>();
		junctionY = new ArrayList<Double>();
		junctionPtNum = new ArrayList<Integer>();
	
		i = this.pointArr.length >> 1;
		lastPtX = this.pointArr[0];
		lastPtY = this.pointArr[1];
		lastIndex = 0;
		while (i-- > 0)
		{
			thisPtX = this.pointArr[(i << 1) + 0];
			thisPtY = this.pointArr[(i << 1) + 1];
	
			if (thisPtX != lastPtX || thisPtY != lastPtY)
			{
				m1 = (lastPtY - thisPtY) / (lastPtX - thisPtX);
	
				lastChkPtX = thisPtX;
				lastChkPtY = thisPtY;
				lastChkIndex = i;
				j = i;
				while (j-- > 0)
				{
					thisChkPtX = this.pointArr[(j << 1) + 0];
					thisChkPtY = this.pointArr[(j << 1) + 1];
	
					if (j == i || j == lastIndex || lastChkIndex == i || lastChkIndex == lastIndex)
					{
					}
					else if (thisChkPtX != lastChkPtX || thisChkPtY != lastChkPtY)
					{
						if (lastChkPtX == thisChkPtX && lastPtX == thisPtX)
						{
						}
						else if (lastChkPtX == thisChkPtX)
						{
							intY = m1 * thisChkPtX - m1 * thisPtX + thisPtY;
							if (intY == lastChkPtY || intY == thisChkPtY || ((intY > thisChkPtY) ^ (intY > lastChkPtY)))
							{
								junctionX.add(thisChkPtX);
								junctionY.add(intY);
								junctionPtNum.add(j);
								junctionX.add(thisChkPtX);
								junctionY.add(intY);
								junctionPtNum.add(i);
							}
						}
						else if (lastPtX == thisPtX)
						{
							intY = m2 * thisPtX - m2 * thisChkPtX + thisChkPtY;
							if (intY == lastPtY || intY == thisPtY || ((intY > thisPtY) ^ (intY > lastPtY)))
							{
								junctionX.add(thisPtX);
								junctionY.add(intY);
								junctionPtNum.add(j);
								junctionX.add(thisPtX);
								junctionY.add(intY);
								junctionPtNum.add(i);
							}
						}
						else
						{
							m2 = (lastChkPtY - thisChkPtY) / (lastChkPtX - thisChkPtX);
							if (m1 != m2)
							{
								intX = (m1 * thisPtX - m2 * thisChkPtX + thisChkPtY - thisPtY) / (m1 - m2);
								if ((intX == thisChkPtX || intX == lastChkPtX || ((intX > thisChkPtX) ^ (intX > lastChkPtX))) && (intX == thisPtX || intX == lastPtX || ((intX > thisPtX) ^ (intX > lastPtX))))
								{
									intY = m2 * intX - m2 * thisChkPtX + thisChkPtY;
									junctionX.add(intX);
									junctionY.add(intY);
									junctionPtNum.add(j);
									junctionX.add(intY);
									junctionY.add(intY);
									junctionPtNum.add(i);
								}
							}
						}
					}
	
					lastChkPtX = thisChkPtX;
					lastChkPtY = thisChkPtY;
					lastChkIndex = j;
				}
			}
			
	
			lastPtX = thisPtX;
			lastPtY = thisPtY;
			lastIndex = i;
		}
		results.add(this);
	}
}
