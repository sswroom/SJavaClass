package org.sswr.util.math.geometry;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.sswr.util.data.ByteTool;
import org.sswr.util.data.SharedBool;
import org.sswr.util.data.SharedDouble;
import org.sswr.util.math.Coord2DDbl;
import org.sswr.util.math.CoordinateSystem;
import org.sswr.util.math.MathUtil;

public class LineString extends PointCollection
{
	protected double []zArr;
	protected double []mArr;

	public LineString(int srid, int nPoint, boolean hasZ, boolean hasM)
	{
		super(srid, nPoint, null);
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

	public VectorType getVectorType()
	{
		return VectorType.LineString;
	}

	
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

	public double calSqrDistance(Coord2DDbl pt, Coord2DDbl nearPt)
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

	public boolean joinVector(Vector2D vec)
	{
		if (vec.getVectorType() != VectorType.LineString || this.hasZ() != vec.hasZ() || this.hasM() != vec.hasM())
		{
			return false;
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
		}
		else
		{
			CoordinateSystem.convertXYArray(srcCSys, destCSys, this.pointArr, this.pointArr);
		}
	}

	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof LineString)) {
			return false;
		}
		LineString lineString = (LineString) o;
		return this.srid == lineString.srid && Objects.equals(pointArr, lineString.pointArr) && Objects.equals(zArr, lineString.zArr) && Objects.equals(mArr, lineString.mArr);
	}

	@Override
	public boolean equalsNearly(Vector2D vec) {
		if (vec == this)
			return true;
		if (!(vec instanceof LineString)) {
			return false;
		}
		LineString pl = (LineString) vec;
		if (this.getVectorType() == pl.getVectorType() && this.hasZ() == pl.hasZ() && this.hasM() == pl.hasM())
		{
			Coord2DDbl []ptList = pl.getPointList();
			double []valArr;
			if (this.pointArr.length != ptList.length)
			{
				return false;
			}
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
				valArr = pl.zArr;
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
				valArr = pl.mArr;
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
			return false;
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(srid, pointArr, zArr, mArr);
	}

	public double []getZList()
	{
		return this.zArr;
	}

	public double []getMList()
	{
		return this.mArr;
	}

	public LineString splitByPoint(Coord2DDbl pt)
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

	public int getPointNo(Coord2DDbl pt, SharedBool isPoint, SharedDouble calPtXOut, SharedDouble calPtYOut, SharedDouble calPtZOut, SharedDouble calPtMOut)
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
}
