package org.sswr.util.math;

import org.sswr.util.basic.Vector3;
import org.sswr.util.io.ParsedObject;
import org.sswr.util.io.ParserType;
import org.sswr.util.math.geometry.LineString;
import org.sswr.util.math.unit.Distance;

public abstract class CoordinateSystem extends ParsedObject
{
	public enum PrimemType
	{
		Greenwich;

		public int getId(PrimemType t)
		{
			switch (t)
			{
			case Greenwich:
				return 8901;
			}
			return 0;
		}
	}

	public enum UnitType
	{
		Metre,
		Degree;

		public int getId(UnitType t)
		{
			switch (t)
			{
			case Metre:
				return 9001;
			case Degree:
				return 9122;
			}
			return 0;
		}
	}

	protected String csysName;
	protected int srid;

	protected CoordinateSystem(String sourceName, int srid, String csysName)
	{
		super(sourceName);
		this.srid = srid;
		this.csysName = csysName;
	}

	public abstract double calSurfaceDistance(Coord2DDbl pos1, Coord2DDbl pos2, Distance.DistanceUnit unit);
	public abstract double calLineStringDistance(LineString pl, boolean include3D, Distance.DistanceUnit unit);
	public abstract CoordinateSystem clone();
	public abstract CoordinateSystemType getCoordSysType();
	public abstract boolean isProjected();
	public abstract void toString(StringBuilder sb);

	public ParserType getParserType()
	{
		return ParserType.CoordinateSystem;
	}

	public boolean equals(CoordinateSystem csys)
	{
		if (this == csys)
			return true;
		CoordinateSystemType cst = this.getCoordSysType();
		if (cst != csys.getCoordSysType())
			return false;
		if (cst == CoordinateSystemType.Geographic)
		{
			GeographicCoordinateSystem gcs1 = (GeographicCoordinateSystem)this;
			GeographicCoordinateSystem gcs2 = (GeographicCoordinateSystem)csys;
			return gcs1.getEllipsoid().equals(gcs2.getEllipsoid());
		}
		else if (cst == CoordinateSystemType.PointMapping)
		{
			return false;
		}
		else
		{
			ProjectedCoordinateSystem pcs1 = (ProjectedCoordinateSystem)this;
			ProjectedCoordinateSystem pcs2 = (ProjectedCoordinateSystem)csys;
			return pcs1.sameProjection(pcs2);
		}
	}
	public String getCSysName()
	{
		return this.csysName;
	}

	public int getSRID()
	{
		return this.srid;
	}

	public static Coord2DDbl convert(CoordinateSystem srcCoord, CoordinateSystem destCoord, Coord2DDbl coord)
	{
		return convert3D(srcCoord, destCoord, new Vector3(coord, 0)).getXY();
	}

	public static Vector3 convert3D(CoordinateSystem srcCoord, CoordinateSystem destCoord, Vector3 srcPos)
	{
		Vector3 destPos = srcPos.clone();
		if (srcCoord.isProjected())
		{
			ProjectedCoordinateSystem pcs = (ProjectedCoordinateSystem)srcCoord;
			destPos = new Vector3(pcs.toGeographicCoordinateDeg(destPos.toCoord2D()), destPos.getZ());
			srcCoord = pcs.getGeographicCoordinateSystem();
		}
		if (srcCoord.equals(destCoord))
		{
			return destPos;
		}
		destPos = ((GeographicCoordinateSystem)srcCoord).toCartesianCoordDeg(destPos);
	
		if (destCoord.isProjected())
		{
			ProjectedCoordinateSystem pcs = (ProjectedCoordinateSystem)destCoord;
			GeographicCoordinateSystem gcs = pcs.getGeographicCoordinateSystem();
			destPos = gcs.fromCartesianCoordRad(destPos);
			destPos = new Vector3(pcs.fromGeographicCoordinateRad(destPos.getXY()), destPos.getZ());
		}
		else
		{
			GeographicCoordinateSystem gcs = (GeographicCoordinateSystem)destCoord;;
			destPos = gcs.fromCartesianCoordDeg(destPos);
		}
		return destPos;
	}
	
	public static void convertXYArray(CoordinateSystem srcCoord, CoordinateSystem destCoord, Coord2DDbl []srcArr, Coord2DDbl []destArr)
	{
		int i;
		boolean srcRad = false;
		if (srcCoord.isProjected())
		{
			ProjectedCoordinateSystem pcs = (ProjectedCoordinateSystem)srcCoord;
			i = srcArr.length;
			while (i-- > 0)
			{
				destArr[i] = pcs.toGeographicCoordinateRad(srcArr[i]);
			}
			srcCoord = pcs.getGeographicCoordinateSystem();
			srcArr = destArr;
			srcRad = true;
		}
		if (srcCoord.equals(destCoord))
		{
			if (srcRad)
			{
				i = srcArr.length;
				while (i-- > 0)
				{
					destArr[i].y = srcArr[i].y * 180.0 / Math.PI;
					destArr[i].x = srcArr[i].x * 180.0 / Math.PI;
				}
			}
			else if (srcArr != destArr)
			{
				i = srcArr.length;
				while (i-- > 0)
				{
					destArr[i] = srcArr[i].clone();
				}
			}
			return;
		}
		Vector3 tmpPos;
		if (destCoord.isProjected())
		{
			ProjectedCoordinateSystem pcs = (ProjectedCoordinateSystem)destCoord;
			GeographicCoordinateSystem gcs = pcs.getGeographicCoordinateSystem();
			if (srcRad)
			{
				i = srcArr.length;
				while (i-- > 0)
				{
					tmpPos = ((GeographicCoordinateSystem)srcCoord).toCartesianCoordRad(new Vector3(srcArr[i], 0));
					tmpPos = gcs.fromCartesianCoordRad(tmpPos);
					destArr[i] = pcs.fromGeographicCoordinateRad(tmpPos.getXY());
				}
			}
			else
			{
				i = srcArr.length;
				while (i-- > 0)
				{
					tmpPos = ((GeographicCoordinateSystem)srcCoord).toCartesianCoordDeg(new Vector3(srcArr[i], 0));
					tmpPos = gcs.fromCartesianCoordRad(tmpPos);
					destArr[i] = pcs.fromGeographicCoordinateRad(tmpPos.getXY());
				}
			}
		}
		else
		{
			GeographicCoordinateSystem gcs = (GeographicCoordinateSystem)destCoord;
			if (srcRad)
			{
				i = srcArr.length;
				while (i-- > 0)
				{
					tmpPos = ((GeographicCoordinateSystem)srcCoord).toCartesianCoordRad(new Vector3(srcArr[i], 0));
					destArr[i] = gcs.fromCartesianCoordDeg(tmpPos).getXY();
				}
			}
			else
			{
				i = srcArr.length;
				while (i-- > 0)
				{
					tmpPos = ((GeographicCoordinateSystem)srcCoord).toCartesianCoordDeg(new Vector3(srcArr[i], 0));
					destArr[i] = gcs.fromCartesianCoordDeg(tmpPos).getXY();
				}
			}
		}
	}

}
