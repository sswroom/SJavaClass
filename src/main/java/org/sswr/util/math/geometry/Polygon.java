package org.sswr.util.math.geometry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.sswr.util.data.ByteTool;
import org.sswr.util.math.Coord2DDbl;

public class Polygon extends PointOfstCollection
{
	public Polygon(int srid, int nPtOfst, int nPoint, boolean hasZ, boolean hasM)
	{
		super(srid, nPtOfst, nPoint, null, hasZ, hasM);
	}

	public VectorType getVectorType()
	{
		return VectorType.Polygon;
	}

	public Vector2D clone()
	{
		Polygon pg = new Polygon(this.srid, this.ptOfstArr.length, this.pointArr.length, this.hasZ(), this.hasM());
		ByteTool.copyArray(pg.pointArr, 0, this.pointArr, 0, this.pointArr.length);
		ByteTool.copyArray(pg.ptOfstArr, 0, this.ptOfstArr, 0, this.ptOfstArr.length);
		if (this.zArr != null)
		{
			ByteTool.copyArray(pg.zArr, 0, this.zArr, 0, this.zArr.length);
		}
		if (this.mArr != null)
		{
			ByteTool.copyArray(pg.mArr, 0, this.mArr, 0, this.mArr.length);
		}
		return pg;
	}

	public double calSqrDistance(Coord2DDbl pt, Coord2DDbl nearPt)
	{
		if (this.insideVector(pt))
		{
			if (nearPt != null)
			{
				nearPt.x = pt.x;
				nearPt.y = pt.y;
			}
			return 0;
		}
	
		int k;
		int l;
		int m;
		int []ptOfsts;
		Coord2DDbl []points;
	
		ptOfsts = this.ptOfstArr;
		points = this.pointArr;
	
		k = this.ptOfstArr.length;
		l = this.pointArr.length;
	
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
		k = this.pointArr.length >> 1;
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

	public boolean joinVector(Vector2D vec)
	{
		if (vec.getVectorType() != VectorType.Polygon)
			return false;
		Polygon pg = (Polygon)vec;
		Coord2DDbl []newPoints;
		int nPoint = (this.pointArr.length + pg.pointArr.length);
		int []newPtOfsts;
		int nPtOfst = this.ptOfstArr.length + pg.ptOfstArr.length;
		
		newPoints = new Coord2DDbl[nPoint];
		newPtOfsts = new int[nPtOfst];
		ByteTool.copyArray(newPoints, 0, this.pointArr, 0, this.pointArr.length);
		ByteTool.copyArray(newPoints, this.pointArr.length, pg.pointArr, 0, pg.pointArr.length);
		ByteTool.copyArray(newPtOfsts, 0, this.ptOfstArr, 0, this.ptOfstArr.length);
		int i = pg.ptOfstArr.length;
		int j = i + this.ptOfstArr.length;
		int k = this.pointArr.length;
		while (i-- > 0)
		{
			j--;
			newPtOfsts[j] = pg.ptOfstArr[i] + k;
		}
		this.ptOfstArr = newPtOfsts;
		this.pointArr = newPoints;
		return true;
	}

	public boolean insideVector(Coord2DDbl coord)
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
		l = this.pointArr.length;
	
		while (k-- > 0)
		{
			m = this.ptOfstArr[k];
	
			lastX = this.pointArr[m].x;
			lastY = this.pointArr[m].y;
			while (l-- > m)
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
	
	
		i = this.pointArr.length;
		j = this.ptOfstArr.length;
		while (j-- > 0)
		{
			nextPart = this.ptOfstArr[j];
			lastPtX = this.pointArr[nextPart].x;
			lastPtY = this.pointArr[nextPart].y;
			lastIndex = nextPart;
			while (i-- > nextPart)
			{
				thisPtX = this.pointArr[i].x;
				thisPtY = this.pointArr[i].y;
	
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
							lastChkPtX = this.pointArr[nextChkPart].x;
							lastChkPtY = this.pointArr[nextChkPart].y;
						}
	
						while (k-- > nextChkPart)
						{
							thisChkPtX = this.pointArr[k].x;
							thisChkPtY = this.pointArr[k].y;
	
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
		Coord2DDbl []points;
		double []zArr;
		double []mArr;
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
	
		i = this.pointArr.length;
		while (this.ptOfstArr.length > 1)
		{
			j = this.ptOfstArr[this.ptOfstArr.length - 1];
			tmpPG = new Polygon(this.srid, 1, i - j, this.zArr != null, this.mArr != null);
			points = tmpPG.getPointList();
			ByteTool.copyArray(points, 0, this.pointArr, j, (i - j));
			if (this.zArr != null)
			{
				zArr = tmpPG.getZList();
				ByteTool.copyArray(zArr, 0, this.zArr, j, (i - j));
			}
			if (this.mArr != null)
			{
				mArr = tmpPG.getZList();
				ByteTool.copyArray(mArr, 0, this.mArr, j, (i - j));
			}
			tmpPG.splitByJunction(results);
			
			this.pointArr = Arrays.copyOf(this.pointArr, j);
			this.ptOfstArr = Arrays.copyOf(this.ptOfstArr, this.ptOfstArr.length - 1);
			if (this.zArr != null)
				this.zArr = Arrays.copyOf(this.zArr, j);
			if (this.mArr != null)
				this.mArr = Arrays.copyOf(this.mArr, j);
			i = j;
		}
	
		junctionX = new ArrayList<Double>();
		junctionY = new ArrayList<Double>();
		junctionPtNum = new ArrayList<Integer>();
	
		i = this.pointArr.length;
		lastPtX = this.pointArr[0].x;
		lastPtY = this.pointArr[0].y;
		lastIndex = 0;
		while (i-- > 0)
		{
			thisPtX = this.pointArr[i].x;
			thisPtY = this.pointArr[i].y;
	
			if (thisPtX != lastPtX || thisPtY != lastPtY)
			{
				m1 = (lastPtY - thisPtY) / (lastPtX - thisPtX);
	
				lastChkPtX = thisPtX;
				lastChkPtY = thisPtY;
				lastChkIndex = i;
				j = i;
				while (j-- > 0)
				{
					thisChkPtX = this.pointArr[j].x;
					thisChkPtY = this.pointArr[j].y;
	
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
