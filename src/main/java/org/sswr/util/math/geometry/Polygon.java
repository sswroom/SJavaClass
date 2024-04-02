package org.sswr.util.math.geometry;

import java.util.ArrayList;
import java.util.Iterator;

import org.sswr.util.data.ByteTool;
import org.sswr.util.math.Coord2DDbl;

public class Polygon extends MultiGeometry<LinearRing>
{
	public Polygon(int srid)
	{
		super(srid);
	}

	public VectorType getVectorType()
	{
		return VectorType.Polygon;
	}

	public Vector2D clone()
	{
		Polygon pg = new Polygon(this.srid);
		Iterator<LinearRing> it = this.geometries.iterator();
		while (it.hasNext())
		{
			pg.addGeometry((LinearRing)it.next().clone());
		}
		return pg;
	}

	public double calBoundarySqrDistance(Coord2DDbl pt, Coord2DDbl nearPt)
	{
		double minDist = 100000000000.0;
		Coord2DDbl minPt = new Coord2DDbl(0, 0);
		Coord2DDbl thisPt = new Coord2DDbl();
		double thisDist;
		Iterator<LinearRing> it = this.geometries.iterator();
		while (it.hasNext())
		{
			thisDist = it.next().calBoundarySqrDistance(pt, thisPt);
			if (thisDist < minDist)
			{
				minDist = thisDist;
				minPt = thisPt;
			}
		}
		nearPt.x = minPt.x;
		nearPt.y = minPt.y;
		return minDist;
	}

	public boolean joinVector(Vector2D vec)
	{
		if (vec.getVectorType() != VectorType.Polygon || this.srid != vec.getSRID())
			return false;
		Polygon pg = (Polygon)vec;
		Iterator<LinearRing> it = pg.geometries.iterator();
		while (it.hasNext())
		{
			this.addGeometry((LinearRing)it.next().clone());
		}
		return true;
	}

	public boolean insideOrTouch(Coord2DDbl coord)
	{
		int insideCnt = 0;
		Iterator<LinearRing> it = this.geometries.iterator();
		while (it.hasNext())
		{
			if (it.next().insideOrTouch(coord))
				insideCnt++;
		}
		return (insideCnt & 1) != 0;
	}

/* 	public boolean hasJunction()
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
	}*/

	public MultiPolygon createMultiPolygon()
	{
		MultiPolygon mpg = new MultiPolygon(this.srid);
		if (this.geometries.size() <= 1)
		{
			mpg.addGeometry((Polygon)this.clone());
			return mpg;
		}
		ArrayList<Polygon> pgList = new ArrayList<Polygon>();
		Polygon pg;
		LinearRing lr;
		Iterator<LinearRing> it = this.geometries.iterator();
		int k;
		boolean found;
		while (it.hasNext())
		{
			lr = it.next();
			found = false;
			k = pgList.size();
			while (k-- > 0)
			{
				pg = pgList.get(k);
				if (pg != null && pg.insideOrTouch(lr.getPoint(0)))
				{
					found = true;
					pg.addGeometry((LinearRing)lr.clone());
					break;
				}
			}
			if (!found)
			{
				pg = new Polygon(this.srid);
				pg.addGeometry((LinearRing)lr.clone());
				pgList.add(pg);
			}
		}
		Iterator<Polygon> itPG = pgList.iterator();
		while (itPG.hasNext())
		{
			mpg.addGeometry(itPG.next());
		}
		return mpg;
	}

	public void addFromPtOfst(int[] ptOfstList, Coord2DDbl[] pointList, double[] zList, double[] mList)
	{
		LinearRing linearRing;
		int i = 0;
		int j;
		int k;
		int nPtOfst = ptOfstList.length;
		int nPoint = pointList.length;
		Coord2DDbl[] ptArr;
		double[] zArr;
		double[] mArr;
		while (i < nPtOfst)
		{
			j = ptOfstList[i];
			if (i + 1 >= nPtOfst)
				k = nPoint;
			else
				k = ptOfstList[i + 1];
			linearRing = new LinearRing(this.srid, k - j, zList != null, mList != null);
			ptArr = linearRing.getPointList();
			zArr = linearRing.getZList();
			mArr = linearRing.getMList();
			ByteTool.copyArray(ptArr, 0, pointList, j, (k - j));
			if (zList != null)
			{
				ByteTool.copyArray(zArr, 0, zList, j, (k - j));
			}
			if (mList != null)
			{
				ByteTool.copyArray(mArr, 0, mList, j, (k - j));
			}
			this.addGeometry(linearRing);
			i++;
		}
	}

	public int fillPointOfstList(Coord2DDbl[] pointList, int[] ptOfstList, double[] zList, double[] mList)
	{
		int totalCnt = 0;
		LineString lineString;
		Coord2DDbl[] thisPtList;
		double[] dList;
		Iterator<LinearRing> it = this.geometries.iterator();
		int i = 0;
		while (it.hasNext())
		{
			ptOfstList[i] = totalCnt;
			lineString = it.next();
			thisPtList = lineString.getPointList();
			ByteTool.copyArray(pointList, totalCnt, thisPtList, 0, thisPtList.length);
			if (zList != null)
			{
				dList = lineString.getZList();
				if (dList != null)
				{
					ByteTool.copyArray(zList, totalCnt, dList, 0, dList.length);
				}
			}
			if (mList != null)
			{
				dList = lineString.getMList();
				if (dList != null)
				{
					ByteTool.copyArray(mList, totalCnt, dList, 0, dList.length);
				}
			}
			totalCnt += thisPtList.length;
			i++;
		}
		return totalCnt;
	}
}
