package org.sswr.util.math;

import org.sswr.util.data.SharedDouble;
import org.sswr.util.io.ParsedObject;
import org.sswr.util.math.unit.Distance;

public abstract class CoordinateSystem extends ParsedObject
{
	protected String csysName;
	protected int srid;

	protected CoordinateSystem(String sourceName, int srid, String csysName)
	{
		super(sourceName);
		this.srid = srid;
		this.csysName = csysName;
	}

	public abstract double calSurfaceDistanceXY(double x1, double y1, double x2, double y2, Distance.DistanceUnit unit);
	public abstract double calPLDistance(Polyline pl, Distance.DistanceUnit unit);
	public abstract double calPLDistance3D(Polyline3D pl, Distance.DistanceUnit unit);
	public abstract CoordinateSystem clone();
	public abstract CoordinateSystemType getCoordSysType();
	public abstract boolean isProjected();
	public abstract void toString(StringBuilder sb);

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

	public static void convertXYZ(CoordinateSystem srcCoord, CoordinateSystem destCoord, double srcX, double srcY, double srcZ, SharedDouble destX, SharedDouble destY, SharedDouble destZ)
	{
		SharedDouble tmpX;
		SharedDouble tmpY;
		SharedDouble tmpZ;
		if (srcCoord.isProjected())
		{
			ProjectedCoordinateSystem pcs = (ProjectedCoordinateSystem)srcCoord;
			tmpX = new SharedDouble();
			tmpY = new SharedDouble();
			pcs.ToGeographicCoordinate(srcX, srcY, tmpX, tmpY);
			srcCoord = pcs.getGeographicCoordinateSystem();
			srcX = tmpX.value;
			srcY = tmpY.value;
		}
		if (srcCoord.equals(destCoord))
		{
			destX.value = srcX;
			destY.value = srcY;
			if (destZ != null)
				destZ.value = srcZ;
			return;
		}
		tmpX = new SharedDouble();
		tmpY = new SharedDouble();
		tmpZ = new SharedDouble();
		((GeographicCoordinateSystem)srcCoord).toCartesianCoord(srcY, srcX, srcZ, tmpX, tmpY, tmpZ);
		srcX = tmpX.value;
		srcY = tmpY.value;
		srcZ = tmpZ.value;
	
		if (destCoord.IsProjected())
		{
			ProjectedCoordinateSystem pcs = (ProjectedCoordinateSystem)destCoord;
			GeographicCoordinateSystem gcs = pcs.getGeographicCoordinateSystem();
			gcs.rromCartesianCoord(srcX, srcY, srcZ, tmpY, tmpX, tmpZ);
			pcs.rromGeographicCoordinate(tmpX.value, tmpY.value, destX, destY);
			if (destZ != null)
				destZ.value = tmpZ.value;
		}
		else
		{
			GeographicCoordinateSystem gcs = (GeographicCoordinateSystem)destCoord;;
			gcs.FromCartesianCoord(srcX, srcY, srcZ, destY, destX, tmpZ);
			if (destZ != null)
				destZ.value = tmpZ.value;
		}
	}
}
