package org.sswr.util.map.esri;

import java.util.ArrayList;
import java.util.List;

import org.sswr.util.data.DataTools;
import org.sswr.util.data.GeometryUtil;
import org.sswr.util.math.Coord2DDbl;
import org.sswr.util.math.geometry.CircularString;
import org.sswr.util.math.geometry.CompoundCurve;
import org.sswr.util.math.geometry.CurvePolygon;
import org.sswr.util.math.geometry.LineString;
import org.sswr.util.math.geometry.LinearRing;
import org.sswr.util.math.geometry.MultiCurve;
import org.sswr.util.math.geometry.MultiPolygon;
import org.sswr.util.math.geometry.MultiSurface;
import org.sswr.util.math.geometry.Polygon;
import org.sswr.util.math.geometry.Polyline;
import org.sswr.util.math.geometry.Vector2D;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class ESRICurve {
	private static class CurveInfo
	{
		public int type;
		public int startIndex;
	}

	private static class ArcInfo extends CurveInfo
	{
		public Coord2DDbl center;
//		public int bits;
	}

	private static class BezierCurveInfo extends CurveInfo
	{
		public Coord2DDbl point1;
		public Coord2DDbl point2;
	}
	
	private static class EllipticArcInfo extends CurveInfo
	{
//		Coord2DDbl center;
//		double rotation;
//		double semiMajor;
//		double minorMajorRatio;
//		int bits;
	}

	private int srid;
	private List<Coord2DDbl> ptList;
	private List<Integer> partList;
	private List<Double> zList;
	private List<Double> mList;
	private List<CurveInfo> curveList;

	public ESRICurve(int srid, @Nonnull int[] ptOfstList, @Nonnull Coord2DDbl[] ptArr, @Nullable double[] zArr, @Nullable double[] mArr)
	{
		this.srid = srid;
		this.ptList = new ArrayList<Coord2DDbl>();
		int i = 0;
		int j = ptArr.length;
		while (i < j) { this.ptList.add(ptArr[i++]); }
		this.partList = new ArrayList<Integer>();
		i = 0;
		j = ptOfstList.length;
		while (i < j) { this.partList.add(ptOfstList[i++]); }
		this.zList = new ArrayList<Double>();
		this.mList = new ArrayList<Double>();
		this.curveList = new ArrayList<>();
		double[] nnArr;
		if ((nnArr = zArr) != null)
		{
			i = 0;
			j = nnArr.length;
			while (i < j) this.zList.add(nnArr[i++]);
		}
		if ((nnArr = mArr) != null)
		{
			i = 0;
			j = nnArr.length;
			while (i < j) this.mList.add(nnArr[i++]);
		}
	}

	public void addArc(int index, @Nonnull Coord2DDbl center, int bits)
	{
		if (index >= ptList.size())
		{
			System.out.println("ESRICurve: Arc index out of range: "+index);
			return;
		}
		if (center.x < 800000)
		{
			System.out.println("Arc out of range: "+index+", "+center.x+", "+center.y+", "+bits);
		}
		ArcInfo arc = new ArcInfo();
		arc.type = 1;
		arc.startIndex = index;
		arc.center = center;
//		arc.bits = bits;
		this.curveList.add(arc);
	}

	public void addBezier3Curve(int index, @Nonnull Coord2DDbl point1, @Nonnull Coord2DDbl point2)
	{
		if (index >= ptList.size())
		{
			System.out.println("ESRICurve: Bezier3Curve index out of range: "+index);
			return;
		}
		if (point1.x < 800000 || point2.x < 800000)
		{
			System.out.println("BezierArc out of range: "+index+", "+point1.x+", "+point1.y+", "+point2.x+", "+point2.y);
		}
		BezierCurveInfo curve = new BezierCurveInfo();
		curve.type = 5;
		curve.startIndex = index;
		curve.point1 = point1;
		curve.point2 = point2;
		this.curveList.add(curve);	
	}

	public void addEllipticArc(int index, @Nonnull Coord2DDbl center, double rotation, double semiMajor, double minorMajorRatio, int bits)
	{
		if (index >= ptList.size())
		{
			System.out.println("ESRICurve: EllipticArc index out of range: "+index);
			return;
		}
		EllipticArcInfo curve = new EllipticArcInfo();
		curve.type = 4;
		curve.startIndex = index;
//		curve.center = center;
//		curve.rotation = rotation;
//		curve.semiMajor = semiMajor;
//		curve.minorMajorRatio = minorMajorRatio;
//		curve.bits = bits;
		this.curveList.add(curve);
	}

	public @Nonnull Vector2D createPolygon()
	{
		if (this.curveList.size() > 0)
		{
			List<Coord2DDbl> ptList = new ArrayList<Coord2DDbl>();
			MultiSurface ms;
			CurvePolygon cpg;
			CompoundCurve cc;
			LineString ls;
			ptList.addAll(this.ptList);
			ms = new MultiSurface(this.srid);
			cpg = new CurvePolygon(this.srid);
			cc = new CompoundCurve(this.srid);
			CurveInfo curve;
			Coord2DDbl[] curvePts = new Coord2DDbl[3];
			int indexOfst = 0;
			int endPart;
			int i = 0;
			int currIndex = 0;
			int partI = 1;
			if (partI >= this.partList.size())
			{
				endPart = ptList.size() - indexOfst;
			}
			else
			{
				endPart = this.partList.get(partI);
			}
			while (i < this.curveList.size())
			{
				curve = this.curveList.get(i);
				while (curve.startIndex >= endPart)
				{
					if (currIndex + 1 < endPart + indexOfst)
					{
						if (cc.getCount() == 0)
						{
							ls = new LinearRing(this.srid, ptList.subList(currIndex, endPart + indexOfst).toArray(new Coord2DDbl[0]), null, null);
							cpg.addGeometry(ls);
							currIndex = endPart + indexOfst;
						}
						else
						{
							ls = new LineString(this.srid, ptList.subList(currIndex, endPart + indexOfst).toArray(new Coord2DDbl[0]), null, null);
							cc.addGeometry(ls);
							cpg.addGeometry(cc);
							currIndex = endPart + indexOfst;
							cc = new CompoundCurve(this.srid);
						}
					}
					else
					{
						currIndex = endPart + indexOfst;
					}

					partI++;
					if (partI >= this.partList.size())
					{
						endPart = ptList.size() - indexOfst;
					}
					else
					{
						endPart = this.partList.get(partI);
					}
				}
				if (curve.startIndex + indexOfst > currIndex)
				{
					ls = new LineString(this.srid, ptList.subList(currIndex, curve.startIndex + indexOfst + 1).toArray(new Coord2DDbl[0]), null, null);
					cc.addGeometry(ls);
				}
				if (curve.type == 5)
				{
					BezierCurveInfo bcurve = (BezierCurveInfo)curve;
					List<Coord2DDbl> tmpPts = new ArrayList<Coord2DDbl>();
					GeometryUtil.bezierCurveToLine(ptList.get(bcurve.startIndex + indexOfst), bcurve.point1, bcurve.point2, ptList.get(bcurve.startIndex + indexOfst + 1), 10, tmpPts);
					DataTools.insertRange(ptList, bcurve.startIndex + indexOfst + 1, tmpPts.size() - 2, tmpPts, 1);
					indexOfst += tmpPts.size() - 2;
				}
				else if (curve.type == 4)
				{
					//NN<EllipticArcInfo> earc = NN<EllipticArcInfo>::ConvertFrom(curve);
					//ptList.Insert(earc.startIndex + indexOfst + 1, earc.center);
					//indexOfst += 1;
				}
				else //if (curve.type == 1)
				{
					ArcInfo arc = (ArcInfo)curve;
					curvePts[0] = ptList.get(arc.startIndex + indexOfst);
					curvePts[1] = arc.center;
					curvePts[2] = ptList.get(arc.startIndex + indexOfst + 1);
					ls = new CircularString(this.srid, curvePts, null, null);
					cc.addGeometry(ls);
					currIndex = arc.startIndex + indexOfst + 1;
				}
				i++;
			}
			while (partI < this.partList.size())
			{
				if (cc.getCount() == 0)
				{
					ls = new LinearRing(this.srid, ptList.subList(currIndex, endPart + indexOfst).toArray(new Coord2DDbl[0]), null, null);
					cpg.addGeometry(ls);
					currIndex = endPart + indexOfst;
				}
				else
				{
					ls = new LineString(this.srid, ptList.subList(currIndex, endPart + indexOfst).toArray(new Coord2DDbl[0]), null, null);
					cc.addGeometry(ls);
					cpg.addGeometry(cc);
					currIndex = endPart + indexOfst;
					cc = new CompoundCurve(this.srid);
				}

				partI++;
				if (partI >= this.partList.size())
				{
					endPart = ptList.size() - indexOfst;
				}
				else
				{
					endPart = this.partList.get(partI);
				}
			}
			if (currIndex + 1 < ptList.size() - indexOfst)
			{
				if (cc.getCount() == 0)
				{
					ls = new LinearRing(this.srid, ptList.subList(currIndex, endPart + indexOfst).toArray(new Coord2DDbl[0]), null, null);
					cpg.addGeometry(ls);
					currIndex = endPart + indexOfst;
				}
				else
				{
					ls = new LineString(this.srid, ptList.subList(currIndex, endPart + indexOfst).toArray(new Coord2DDbl[0]), null, null);
					cc.addGeometry(ls);
					cpg.addGeometry(cc);
					currIndex = endPart + indexOfst;
				}
			}
			else
			{
				if (cc.getCount() == 0)
				{
				}
				else
				{
					cpg.addGeometry(cc);
				}
			}
			ms.addGeometry(cpg);
			return ms;
		}
		else
		{
			double[] zArr = null;
			double[] mArr = null;
			int i;
			if (this.ptList.size() == this.zList.size())
			{
				zArr = new double[this.zList.size()];
				i = this.zList.size();
				while (i-- > 0)
				{
					zArr[i] = this.zList.get(i);
				}
			}
			if (this.ptList.size() == this.mList.size())
			{
				mArr = new double[this.mList.size()];
				i = this.mList.size();
				while (i-- > 0)
				{
					mArr[i] = this.mList.get(i);
				}
			}
			int[] partArr = new int[this.partList.size()];
			i = this.partList.size();
			while (i-- > 0)
			{
				partArr[i] = this.partList.get(i);
			}
			Polygon pg;
			MultiPolygon mpg;
			pg = new Polygon(this.srid);
			pg.addFromPtOfst(partArr, this.ptList.toArray(new Coord2DDbl[0]), zArr, mArr);
			mpg = new MultiPolygon(this.srid);
			mpg.addGeometry(pg);
			return mpg;
		}
	}

	public @Nonnull Vector2D createPolyline()
	{
		if (this.curveList.size() > 0)
		{
			List<Coord2DDbl> ptList = new ArrayList<Coord2DDbl>();
			MultiCurve mc;
			CompoundCurve cc;
			LineString ls;
			ptList.addAll(this.ptList);
			mc = new MultiCurve(this.srid);
			cc = new CompoundCurve(this.srid);
			CurveInfo curve;
			Coord2DDbl[] curvePts = new Coord2DDbl[3];
			int indexOfst = 0;
			int endPart;
			int i = 0;
			int currIndex = 0;
			int partI = 1;
			if (partI >= this.partList.size())
			{
				endPart = ptList.size() - indexOfst;
			}
			else
			{
				endPart = this.partList.get(partI);
			}
			while (i < this.curveList.size())
			{
				curve = this.curveList.get(i);
				while (curve.startIndex >= endPart)
				{
					if (currIndex + 1 < endPart + indexOfst)
					{
						if (cc.getCount() == 0)
						{
							ls = new LineString(this.srid, ptList.subList(currIndex, endPart + indexOfst).toArray(new Coord2DDbl[0]), null, null);
							mc.addGeometry(ls);
							currIndex = endPart + indexOfst;
						}
						else
						{
							ls = new LineString(this.srid, ptList.subList(currIndex, endPart + indexOfst).toArray(new Coord2DDbl[0]), null, null);
							cc.addGeometry(ls);
							mc.addGeometry(cc);
							currIndex = endPart + indexOfst;
							cc = new CompoundCurve(this.srid);
						}
					}
					else
					{
						currIndex = endPart + indexOfst;
					}
	
					partI++;
					if (partI >= this.partList.size())
					{
						endPart = ptList.size() - indexOfst;
					}
					else
					{
						endPart = this.partList.get(partI);
					}
				}
				if (curve.startIndex + indexOfst > currIndex)
				{
					ls = new LineString(this.srid, ptList.subList(currIndex, curve.startIndex + indexOfst + 1).toArray(new Coord2DDbl[0]), null, null);
					cc.addGeometry(ls);
				}
				if (curve.type == 5)
				{
					BezierCurveInfo bcurve = (BezierCurveInfo)curve;
					List<Coord2DDbl> tmpPts = new ArrayList<Coord2DDbl>();
					GeometryUtil.bezierCurveToLine(ptList.get(bcurve.startIndex + indexOfst), bcurve.point1, bcurve.point2, ptList.get(bcurve.startIndex + indexOfst + 1), 10, tmpPts);
					DataTools.insertRange(ptList, bcurve.startIndex + indexOfst + 1, tmpPts.size() - 2, tmpPts, 1);
					indexOfst += tmpPts.size() - 2;
				}
				else if (curve.type == 4)
				{
					//NN<EllipticArcInfo> earc = NN<EllipticArcInfo>::ConvertFrom(curve);
					//ptList.Insert(earc.startIndex + indexOfst + 1, earc.center);
					//indexOfst += 1;
				}
				else //if (curve.type == 1)
				{
					ArcInfo arc = (ArcInfo)curve;
					curvePts[0] = ptList.get(arc.startIndex + indexOfst);
					curvePts[1] = arc.center;
					curvePts[2] = ptList.get(arc.startIndex + indexOfst + 1);
					ls = new CircularString(this.srid, curvePts, null, null);
					cc.addGeometry(ls);
					currIndex = arc.startIndex + indexOfst + 1;
				}
				i++;
			}
			while (partI < this.partList.size())
			{
				if (cc.getCount() == 0)
				{
					ls = new LineString(this.srid, ptList.subList(currIndex, endPart + indexOfst).toArray(new Coord2DDbl[0]), null, null);
					mc.addGeometry(ls);
					currIndex = endPart + indexOfst;
				}
				else
				{
					ls = new LineString(this.srid, ptList.subList(currIndex, endPart + indexOfst).toArray(new Coord2DDbl[0]), null, null);
					cc.addGeometry(ls);
					mc.addGeometry(cc);
					currIndex = endPart + indexOfst;
					cc = new CompoundCurve(this.srid);
				}
	
				partI++;
				if (partI >= this.partList.size())
				{
					endPart = ptList.size() - indexOfst;
				}
				else
				{
					endPart = this.partList.get(partI);
				}
			}
			if (currIndex + 1 < ptList.size() - indexOfst)
			{
				if (cc.getCount() == 0)
				{
					ls = new LineString(this.srid, ptList.subList(currIndex, endPart + indexOfst).toArray(new Coord2DDbl[0]), null, null);
					mc.addGeometry(ls);
					currIndex = endPart + indexOfst;
				}
				else
				{
					ls = new LineString(this.srid, ptList.subList(currIndex, endPart + indexOfst).toArray(new Coord2DDbl[0]), null, null);
					cc.addGeometry(ls);
					mc.addGeometry(cc);
					currIndex = endPart + indexOfst;
				}
			}
			else
			{
				if (cc.getCount() == 0)
				{
				}
				else
				{
					mc.addGeometry(cc);
				}
			}
			return mc;
		}
		else
		{
			double[] zArr = null;
			double[] mArr = null;
			int i;
			if (this.ptList.size() == this.zList.size())
			{
				zArr = new double[this.zList.size()];
				i = this.zList.size();
				while (i-- > 0)
				{
					zArr[i] = this.zList.get(i);
				}
			}
			if (this.ptList.size() == this.mList.size())
			{
				mArr = new double[this.mList.size()];
				i = this.mList.size();
				while (i-- > 0)
				{
					mArr[i] = this.mList.get(i);
				}
			}
			int[] partArr = new int[this.partList.size()];
			i = this.partList.size();
			while (i-- > 0)
			{
				partArr[i] = this.partList.get(i);
			}
			Polyline pl = new Polyline(this.srid);
			pl.addFromPtOfst(partArr, this.ptList.toArray(new Coord2DDbl[0]), zArr, mArr);
			return pl;
		}
	}
}
