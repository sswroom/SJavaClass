package org.sswr.util.math;

import org.sswr.util.data.SharedDouble;
import org.sswr.util.math.geometry.Polyline;
import org.sswr.util.math.unit.Distance;

public abstract class ProjectedCoordinateSystem extends CoordinateSystem
{
	protected GeographicCoordinateSystem gcs;
	protected double falseEasting;
	protected double falseNorthing;
	protected double rcentralMeridian;
	protected double rlatitudeOfOrigin;
	protected double scaleFactor;
	protected UnitType unit;

	public ProjectedCoordinateSystem(String sourceName, int srid, String projName, double falseEasting, double falseNorthing, double dcentralMeridian, double dlatitudeOfOrigin, double scaleFactor, GeographicCoordinateSystem gcs, UnitType unit)
	{
		super(sourceName, srid, projName);

		this.falseEasting = falseEasting;
		this.falseNorthing = falseNorthing;
		this.rcentralMeridian = dcentralMeridian * Math.PI / 180;
		this.rlatitudeOfOrigin = dlatitudeOfOrigin * Math.PI / 180;
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
		Coord2DDbl []points;
		ptOfsts = pl.getPtOfstList();
		points = pl.getPointList();
		int i = ptOfsts.length;
		int j = points.length;
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
					totalDist += calSurfaceDistanceXY(lastX, lastY, points[j].x, points[j].y, unit);
				}
				hasLast = true;
				lastX = points[j].x;
				lastY = points[j].y;
			}
			j++;
		}
		return totalDist;
	}

	public double calPLDistance3D(Polyline pl, Distance.DistanceUnit unit)
	{
		int []ptOfsts;
		Coord2DDbl []points;
		double []alts;
		ptOfsts = pl.getPtOfstList();
		points = pl.getPointList();
		alts = pl.getZList();
		int i = ptOfsts.length;
		int j = points.length;
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
					dist = calSurfaceDistanceXY(lastX, lastY, points[j].x, points[j].y, unit);
					dist = Math.sqrt(dist * dist + (alts[j] - lastH) * (alts[j] - lastH));
					totalDist += dist;
				}
				hasLast = true;
				lastX = points[j].x;
				lastY = points[j].y;
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
		sb.append(this.getCentralMeridianDegree());
		sb.append("\r\nLatitude Of Origin: ");
		sb.append(this.getLatitudeOfOriginDegree());
		sb.append("\r\nScale Factor: ");
		sb.append(this.scaleFactor);
		sb.append("\r\n");
		this.gcs.toString(sb);
	}

	public GeographicCoordinateSystem getGeographicCoordinateSystem()
	{
		return this.gcs;
	}

	public abstract void toGeographicCoordinateRad(double projX, double projY, SharedDouble geoX, SharedDouble geoY);
	public abstract void fromGeographicCoordinateRad(double geoX, double geoY, SharedDouble projX, SharedDouble projY);
	public void toGeographicCoordinateDeg(double projX, double projY, SharedDouble geoX, SharedDouble geoY)
	{
		this.toGeographicCoordinateRad(projX, projY, geoX, geoY);
		geoX.value = geoX.value * 180 / Math.PI;
		geoY.value = geoY.value * 180 / Math.PI;
	}

	public void fromGeographicCoordinateDeg(double geoX, double geoY, SharedDouble projX, SharedDouble projY)
	{
		this.fromGeographicCoordinateRad(geoX * Math.PI / 180.0, geoY * Math.PI / 180.0, projX, projY);
	}

	public boolean sameProjection(ProjectedCoordinateSystem csys)
	{
		if (this.falseEasting != csys.falseEasting)
			return false;
		if (this.falseNorthing != csys.falseNorthing)
			return false;
		if (this.rcentralMeridian != csys.rcentralMeridian)
			return false;
		if (this.rlatitudeOfOrigin != csys.rlatitudeOfOrigin)
			return false;
		if (this.scaleFactor != csys.scaleFactor)
			return false;
		return this.gcs.equals(csys.gcs);
	}

	public double getLatitudeOfOriginDegree()
	{
		return this.rlatitudeOfOrigin * 180 / Math.PI;
	}

	public double getCentralMeridianDegree()
	{
		return this.rcentralMeridian * 180 / Math.PI;
	}

	public double getLatitudeOfOriginRadian()
	{
		return this.rlatitudeOfOrigin;
	}

	public double getCentralMeridianRadian()
	{
		return this.rcentralMeridian;
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
