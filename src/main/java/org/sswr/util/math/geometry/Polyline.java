package org.sswr.util.math.geometry;

import java.util.Iterator;

import org.sswr.util.data.ByteTool;
import org.sswr.util.data.SharedBool;
import org.sswr.util.data.SharedDouble;
import org.sswr.util.math.Coord2DDbl;

public class Polyline extends MultiGeometry<LineString>
{
	protected int flags;
	protected int color;

	public Polyline(int srid)
	{
		super(srid);
		this.flags = 0;
		this.color = 0;
	}

	public VectorType getVectorType()
	{
		return VectorType.Polyline;
	}

	public Vector2D clone()
	{
		Polyline pl = new Polyline(this.srid);
		Iterator<LineString> it = this.geometries.iterator();
		while (it.hasNext())
		{
			pl.addGeometry((LineString)it.next().clone());
		}
		pl.flags = this.flags;
		pl.color = this.color;
		return pl;
	}

	public double calBoundarySqrDistance(Coord2DDbl pt, Coord2DDbl nearPt)
	{
		Iterator<LineString> it = this.geometries.iterator();
		if (!it.hasNext())
		{
			nearPt.x = 0;
			nearPt.y = 0;
			return 9999999.0;
		}
		Coord2DDbl minPt = new Coord2DDbl();
		double minDist = it.next().calBoundarySqrDistance(pt, minPt);
		while (it.hasNext())
		{
			Coord2DDbl thisPt = new Coord2DDbl();
			double thisDist = it.next().calBoundarySqrDistance(pt, thisPt);
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
		if (vec.getVectorType() != VectorType.Polyline || this.hasZ() != vec.hasZ() || this.hasM() != vec.hasM())
		{
			return false;
		}
		Polyline pl = (Polyline)vec;
		Iterator<LineString> it = pl.iterator();
		while (it.hasNext())
		{
			this.addGeometry((LineString)it.next().clone());
		}
		return true;
	}

	public void addFromPtOfst(int[] ptOfstList, Coord2DDbl[] pointList, double[] zList, double[] mList)
	{
		LineString lineString;
		int i = 0;
		int j;
		int k;
		Coord2DDbl[] ptArr;
		double[] zArr;
		double[] mArr;
		int nPtOfst = ptOfstList.length;
		int nPoint = pointList.length;
		while (i < nPtOfst)
		{
			j = ptOfstList[i];
			if (i + 1 >= nPtOfst)
				k = nPoint;
			else
				k = ptOfstList[i + 1];
			lineString = new LineString(this.srid, k - j, zList != null, mList != null);
			ptArr = lineString.getPointList();
			zArr = lineString.getZList();
			mArr = lineString.getMList();
			ByteTool.copyArray(ptArr, 0, pointList, j, (k - j));
			if (zList != null)
			{
				ByteTool.copyArray(zArr, 0, zList, j, (k - j));
			}
			if (mList != null)
			{
				ByteTool.copyArray(mArr, 0, mList, j, (k - j));
			}
			this.addGeometry(lineString);
			i++;
		}
	}

	public double calcLength()
	{
		double dist = 0;
		Iterator<LineString> it = this.iterator();
		while (it.hasNext())
		{
			dist += it.next().calcLength();
		}
		return dist;
	}

	public int fillPointOfstList(Coord2DDbl[] pointList, int[] ptOfstList, double[] zList, double[] mList)
	{
		int totalCnt = 0;
		int nPoint;
		LineString lineString;
		Coord2DDbl[] thisPtList;
		double[] dList;
		Iterator<LineString> it = this.geometries.iterator();
		int i = 0;
		while (it.hasNext())
		{
			ptOfstList[i] = totalCnt;
			lineString = it.next();
			thisPtList = lineString.getPointList();
			nPoint = thisPtList.length;
			ByteTool.copyArray(pointList, totalCnt, thisPtList, 0, nPoint);
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
			totalCnt += nPoint;
			i++;
		}
		return totalCnt;
	}

	public Coord2DDbl calcPosAtDistance(double dist)
	{
		LineString lineString;
		Coord2DDbl[] points;
		Coord2DDbl lastPt = new Coord2DDbl(0, 0);
		Iterator<LineString> it = this.geometries.iterator();
		int k;
		int nPoint;
		Coord2DDbl diff;
		double thisDist;
		while (it.hasNext())
		{
			lineString = it.next();
			points = lineString.getPointList();
			nPoint = points.length;
			if (dist <= 0)
			{
				return points[0];
			}
			else
			{
				lastPt = points[nPoint - 1];
				k = 1;
				while (k < nPoint)
				{
					diff = points[k - 1].subtract(points[k]);
					thisDist = Math.sqrt(diff.x * diff.x + diff.y * diff.y);
					if (thisDist > dist)
					{
						return points[k - 1].add(points[k].subtract(points[k - 1]).multiply(dist / thisDist));
					}
					else
					{
						dist -= thisDist;
					}
					k++;
				}
			}
		}
		return lastPt;
	}

	public Polyline splitByPoint(Coord2DDbl pt)
	{
		Coord2DDbl calPt = new Coord2DDbl();
		SharedDouble calZ = new SharedDouble();
		SharedDouble calM = new SharedDouble();
		SharedBool isPoint = new SharedBool();
		int minId = this.getPointNo(pt, isPoint, calPt, calZ, calM);
	
		LineString lineString;
		Polyline newPL;
		
		if (minId == -1)
			return null;
		Iterator<LineString> it = this.geometries.iterator();
		int i = 0;
		while (it.hasNext())
		{
			lineString = it.next();
			int nPoint = lineString.getPointCount();
			if (minId == 0 && isPoint.value)
			{
				if (i == 0)
					return null;
				newPL = new Polyline(this.srid);
				while ((lineString = this.geometries.remove(i)) != null)
				{
					newPL.addGeometry(lineString);
				}
				return newPL;
			}
			else if (minId == nPoint - 1)
			{
				if (i + 1 == this.geometries.size())
					return null;
				newPL = new Polyline(this.srid);
				while ((lineString = this.geometries.remove(i + 1)) != null)
				{
					newPL.addGeometry(lineString);
				}
				return newPL;
			}
			else if (minId < nPoint)
			{
				newPL = new Polyline(this.srid);
				if ((lineString = lineString.splitByPoint(pt)) != null)
				{
					newPL.addGeometry(lineString);
				}
				while ((lineString = this.geometries.remove(i + 1)) != null)
				{
					newPL.addGeometry(lineString);
				}
				return newPL;
			}
			else
			{
				minId -= nPoint;
			}
			i++;
		}
		return null;
	}

/* 	public void optimizePolyline()
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
	}*/

	public int getPointNo(Coord2DDbl pt, SharedBool isPoint, Coord2DDbl calPtOutPtr, SharedDouble calZOutPtr, SharedDouble calMOutPtr)
	{
		int k;
		int l;
		Coord2DDbl[] points;
		double[] zArr;
		double[] mArr;
		double calBase;
		Coord2DDbl calDiff;
		Coord2DDbl calSqDiff;
		Coord2DDbl calPt = new Coord2DDbl();
		Coord2DDbl calPtOut = new Coord2DDbl(0, 0);
		double calZOut = 0;
		double calMOut = 0;
		double calZ = 0;
		double calM = 0;
		double calD;
		double dist = 0x7fffffff;
		int minId = -1;
		boolean isPointI = false;
	
		LineString lineString;
		int currId = 0;
		Iterator<LineString> it = this.geometries.iterator();
		while (it.hasNext())
		{
			lineString = it.next();
			k = 0;
			zArr = lineString.getZList();
			mArr = lineString.getMList();
			points = lineString.getPointList();
			l = points.length;
			while (k < l)
			{
				calDiff = pt.subtract(points[k]);
				calSqDiff = calDiff.multiply(calDiff);
				calD = calSqDiff.x + calSqDiff.y;
				if (calD < dist)
				{
					dist = calD;
					calPtOut = points[k];
					calZOut = (zArr != null)?zArr[k]:0;
					calMOut = (mArr != null)?mArr[k]:0;
					minId = (currId + k);
					isPointI = true;
				}
	
				k++;
				if (k < l)
				{
					calDiff = points[k - 1].subtract(points[k]);
	
					if (calDiff.x == 0 && calDiff.y == 0)
					{
						calPt.x = pt.x;
						calPt.y = pt.y;
						calZ = (zArr != null)?zArr[k - 1]:0;
						calM = (mArr != null)?mArr[k - 1]:0;
					}
					else
					{
						if (calDiff.y == 0)
						{
							calPt.x = pt.x;
						}
						else
						{
							calSqDiff = calDiff.multiply(calDiff);
							calBase = calSqDiff.x + calSqDiff.y;
							calPt.x = calSqDiff.x * pt.x;
							calPt.x += calSqDiff.y * points[k - 1].x;
							calPt.x += (pt.y - points[k - 1].y) * calDiff.x * calDiff.y;
							calPt.x /= calBase;
	
							if (calDiff.x == 0)
							{
								////////////////////////////////
								calZ = 0;
								calM = 0;
							}
						}
	
						if (calDiff.x == 0)
						{
							calPt.y = pt.y;
						}
						else
						{
							Double ratio = (calPt.x - (points[k - 1].x)) / calDiff.x;
							calPt.y = (ratio * calDiff.y) + points[k - 1].y;
							if (zArr != null)
							{
								calZ = (ratio * (zArr[k - 1] - zArr[k])) + zArr[k - 1];
							}
							else
							{
								calZ = 0;
							}
							if (mArr != null)
							{
								calM = (ratio * (mArr[k - 1] - mArr[k])) + mArr[k - 1];
							}
							else
							{
								calM = 0;
							}
						}
					}
	
					if (calDiff.x < 0)
					{
						if (points[k - 1].x > calPt.x)
							continue;
						if (points[k].x < calPt.x)
							continue;
					}
					else
					{
						if (points[k - 1].x < calPt.x)
							continue;
						if (points[k].x > calPt.x)
							continue;
					}
	
					if (calDiff.y < 0)
					{
						if (points[k - 1].y > calPt.y)
							continue;
						if (points[k].y < calPt.y)
							continue;
					}
					else
					{
						if (points[k - 1].y < calPt.y)
							continue;
						if (points[k].y > calPt.y)
							continue;
					}
	
					calDiff = pt.subtract(calPt);
					calSqDiff = calDiff.multiply(calDiff);
					calD = calSqDiff.x + calSqDiff.y;
					if (calD < dist)
					{
						dist = calD;
						calPtOut = calPt;
						calZOut = calZ;
						calMOut = calM;
						isPointI = false;
						minId = (currId + k - 1);
					}
				}
			}
			currId += l;
		}
	
		isPoint.value = isPointI;
		calPtOutPtr.set(calPtOut);
		calZOutPtr.value = calZOut;
		calMOutPtr.value = calMOut;
		return minId;
	}

/* 	public Polygon createPolygonByDist(double dist)
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
	}*/

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
