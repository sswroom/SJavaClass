package org.sswr.util.math.geometry;

import java.util.ArrayList;
import java.util.List;

import org.sswr.util.data.ByteTool;
import org.sswr.util.data.GeometryUtil;
import org.sswr.util.math.Coord2DDbl;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class CircularString extends LineString {
	public CircularString(int srid, int nPoint, boolean hasZ, boolean hasM)
	{
		super(srid, nPoint | 1, hasZ, hasM);
	}

	public CircularString(int srid, @Nonnull Coord2DDbl[] pointArr, @Nullable double[] zArr, @Nullable double[] mArr)
	{
		super(srid, pointArr, zArr, mArr);
	}
	
	@Nonnull
	public VectorType getVectorType()
	{
		return VectorType.CircularString;
	}

	@Nonnull
	public Vector2D clone()
	{
		CircularString pl;
		pl = new CircularString(this.srid, this.pointArr.length, this.zArr != null, this.mArr != null);
		ByteTool.copyArray(pl.pointArr, 0, this.pointArr, 0, this.pointArr.length);
		if (this.zArr != null)
		{	
			ByteTool.copyArray(pl.zArr, 0, this.zArr, 0, this.pointArr.length);
		}
		if (this.mArr != null)
		{	
			ByteTool.copyArray(pl.mArr, 0, this.mArr, 0, this.pointArr.length);
		}
		return pl;
	}
	public boolean hasCurve() { return true; }
	public @Nullable Vector2D toSimpleShape()
	{
		if (this.pointArr.length > 2 && (this.pointArr.length & 1) != 0)
		{
			List<Coord2DDbl> ptList = new ArrayList<Coord2DDbl>();
			int ret = 0;
			int i = 2;
			while (i < this.pointArr.length)
			{
				if (ret > 0)
				{
					ptList.remove(ptList.size() - 1);
					ret--;
				}
				ret += GeometryUtil.arcToLine(this.pointArr[i - 2], pointArr[i - 1], pointArr[i], 2.5, ptList);
			}
			LineString ls = new LineString(this.srid, ptList.toArray(new Coord2DDbl[0]), null, null);
			return ls;
		}
		return null;
	}

}
