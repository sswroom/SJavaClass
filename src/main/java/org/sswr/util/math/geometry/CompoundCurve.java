package org.sswr.util.math.geometry;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.sswr.util.data.ByteTool;
import org.sswr.util.data.GeometryUtil;
import org.sswr.util.math.Coord2DDbl;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class CompoundCurve extends MultiGeometry<LineString>
{
	public CompoundCurve(int srid)
	{
		super(srid);
	}

	public void addGeometry(@Nonnull LineString geometry)
	{
		VectorType t = geometry.getVectorType();
		if (t == VectorType.CircularString || t == VectorType.LineString)
		{
			this.geometries.add(geometry);
		}
	}

	@Nonnull
	public VectorType getVectorType()
	{
		return VectorType.CompoundCurve;
	}

	@Nonnull
	public Vector2D clone()
	{
		CompoundCurve newObj = new CompoundCurve(this.srid);
		int i = 0;
		int j = this.geometries.size();
		while (i < j)
		{
			newObj.addGeometry((LineString)this.geometries.get(i).clone());
			i++;
		}
		return newObj;
	}


	public int getDrawPoints(@Nonnull List<Coord2DDbl> ptList)
	{
		int ret = 0;
		LineString ls;
		Coord2DDbl[] ptArr;
		Iterator<LineString> it = this.iterator();
		while (it.hasNext())
		{
			ls = it.next();
			ptArr = ls.getPointList();
			if (ptArr.length > 2 && (ptArr.length & 1) != 0 && ls.getVectorType() == VectorType.CircularString)
			{
				int i = 2;
				while (i < ptArr.length)
				{
					if (ret > 0)
					{
						ptList.remove(ptList.size() - 1);
						ret--;
					}
					ret += GeometryUtil.arcToLine(ptArr[i - 2], ptArr[i - 1], ptArr[i], 2.5, ptList);
				}
			}
			else
			{
				if (ret == 0 || ptList.get(ptList.size() - 1) != ptArr[0])
				{
					ret += ptArr.length;
					ByteTool.listAddArray(ptList, ptArr, 0, ptArr.length);
				}
				else
				{
					ret += ptArr.length - 1;
					ByteTool.listAddArray(ptList, ptArr, 1, ptArr.length - 1);
				}
			}
		}
		return ret;
	}	

	public boolean hasCurve() { return true; }
	public @Nullable Vector2D toSimpleShape()
	{
		List<Coord2DDbl> ptList = new ArrayList<Coord2DDbl>();
		LineString ls;
		this.getDrawPoints(ptList);
		ls = new LineString(this.srid, ptList.toArray(new Coord2DDbl[0]), null, null);
		return ls;
	}
}
