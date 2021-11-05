package org.sswr.util.math;

import org.sswr.util.data.SharedDouble;
import org.sswr.util.math.unit.Distance;

public abstract class ProjectedCoordinateSystem extends CoordinateSystem
{
	protected GeographicCoordinateSystem gcs;
	protected double falseEasting;
	protected double falseNorthing;
	protected double centralMeridian;
	protected double latitudeOfOrigin;
	protected double scaleFactor;
	protected UnitType unit;

	public ProjectedCoordinateSystem(String sourceName, int srid, String projName, double falseEasting, double falseNorthing, double centralMeridian, double latitudeOfOrigin, double scaleFactor, GeographicCoordinateSystem gcs, UnitType unit)
	{
		super(sourceName, srid, projName);

		this.falseEasting = falseEasting;
		this.falseNorthing = falseNorthing;
		this.centralMeridian = centralMeridian;
		this.latitudeOfOrigin = latitudeOfOrigin;
		this.scaleFactor = scaleFactor;
		this.gcs = gcs;
		this.unit = unit;
	}

	public double calSurfaceDistanceXY(double x1, double y1, double x2, double y2, Distance.DistanceUnit unit)
	{
		Double xDiff = x2 - x1;
		Double yDiff = y2 - y1;
		Double d = Math.sqrt(xDiff * xDiff + yDiff * yDiff);
		if (unit != Distance.DistanceUnit.Meter)
		{
			d = Distance.convert(Distance.DistanceUnit.Meter, unit, d);
		}
		return d;
	}

	public double calPLDistance(Polyline pl, Distance.DistanceUnit unit)
	{
		int []ptOfsts;
		double []points;
		ptOfsts = pl.getPtOfstList();
		points = pl.getPointList();
		int i = ptOfsts.length;
		int j = points.length >> 1;
		int k;
		double totalDist = 0;
		boolean hasLast;
		double lastX = 0;
		double lastY = 0;
		while (i-- > 0)
		{
			k = ptOfsts[i];
			hasLast = false;
			while (j-- > k)
			{
				if (hasLast)
				{
					totalDist += calSurfaceDistanceXY(lastX, lastY, points[(j << 1)], points[(j << 1) + 1], unit);
				}
				hasLast = true;
				lastX = points[(j << 1)];
				lastY = points[(j << 1) + 1];
			}
			j++;
		}
		return totalDist;
	}

	public double calPLDistance3D(Polyline3D pl, Distance.DistanceUnit unit)
	{
		int []ptOfsts;
		double []points;
		double []alts;
		ptOfsts = pl.getPtOfstList();
		points = pl.getPointList();
		alts = pl.getAltitudeList();
		int i = ptOfsts.length;
		int j = points.length >> 1;
		int k;
		double dist;
		double totalDist = 0;
		boolean hasLast;
		double lastX = 0;
		double lastY = 0;
		double lastH = 0;
		while (i-- > 0)
		{
			k = ptOfsts[i];
			hasLast = false;
			while (j-- > k)
			{
				if (hasLast)
				{
					dist = calSurfaceDistanceXY(lastX, lastY, points[(j << 1)], points[(j << 1) + 1], unit);
					dist = Math.sqrt(dist * dist + (alts[j] - lastH) * (alts[j] - lastH));
					totalDist += dist;
				}
				hasLast = true;
				lastX = points[(j << 1)];
				lastY = points[(j << 1) + 1];
				lastH = alts[j];
			}
			j++;
		}
		return totalDist;
	}

	public abstract CoordinateSystem clone();
	public abstract CoordinateSystemType getCoordSysType();
	
	public boolean isProjected()
	{
		return true;
	}

	public void toString(StringBuilder sb)
	{
		sb.append("Projected File Name: ");
		sb.append(this.sourceName);
		sb.append("\r\nProjected Name: ");
		sb.append(this.csysName);
		sb.append("\r\nFalse Easting: ");
		sb.append(this.falseEasting);
		sb.append("\r\nFalse Northing: ");
		sb.append(this.falseNorthing);
		sb.append("\r\nCentral Meridian: ");
		sb.append(this.centralMeridian);
		sb.append("\r\nLatitude Of Origin: ");
		sb.append(this.latitudeOfOrigin);
		sb.append("\r\nScale Factor: ");
		sb.append(this.scaleFactor);
		sb.append("\r\n");
		this.gcs.toString(sb);
	}

	public GeographicCoordinateSystem getGeographicCoordinateSystem()
	{
		return this.gcs;
	}

	public abstract void toGeographicCoordinate(double projX, double projY, SharedDouble geoX, SharedDouble geoY);
	public abstract void fromGeographicCoordinate(double geoX, double geoY, SharedDouble projX, SharedDouble projY);
	
	public boolean sameProjection(ProjectedCoordinateSystem csys)
	{
		if (this.falseEasting != csys.falseEasting)
			return false;
		if (this.falseNorthing != csys.falseNorthing)
			return false;
		if (this.centralMeridian != csys.centralMeridian)
			return false;
		if (this.latitudeOfOrigin != csys.latitudeOfOrigin)
			return false;
		if (this.scaleFactor != csys.scaleFactor)
			return false;
		return this.gcs.equals(csys.gcs);
	}

	public double getLatitudeOfOrigin()
	{
		return this.latitudeOfOrigin;
	}

	public double getCentralMeridian()
	{
		return this.centralMeridian;
	}

	public double getScaleFactor()
	{
		return this.scaleFactor;
	}

	public double getFalseEasting()
	{
		return this.falseEasting;
	}

	public double getFalseNorthing()
	{
		return this.falseNorthing;
	}

	public UnitType getUnit()
	{
		return this.unit;
	}
}
