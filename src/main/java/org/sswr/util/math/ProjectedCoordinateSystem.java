package org.sswr.util.math;

import org.sswr.util.data.SharedDouble;
import org.sswr.util.math.geometry.LineString;
import org.sswr.util.math.geometry.Vector2D.VectorType;
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

	public double calSurfaceDistance(Coord2DDbl pos1, Coord2DDbl pos2, Distance.DistanceUnit unit)
	{
		Coord2DDbl diff = pos2.subtract(pos1);
		diff = diff.multiply(diff);
		double d = Math.sqrt(diff.x + diff.y);
		if (unit != Distance.DistanceUnit.Meter)
		{
			d = Distance.convert(Distance.DistanceUnit.Meter, unit, d);
		}
		return d;
	}

	public double calLineStringDistance(LineString lineString, boolean include3D, Distance.DistanceUnit unit)
	{
		int nPoint;
		Coord2DDbl[] points;
		double[] alts;
		points = lineString.getPointList();
		nPoint = points.length;
		int j = nPoint;
		double totalDist = 0;
		double dist;
		Coord2DDbl lastPt;
		double lastH;
		if (j == 0)
			return 0;
		if (include3D && (alts = lineString.getZList()) != null)
		{
			if (lineString.getVectorType() == VectorType.LinearRing)
			{
				lastPt = points[0];
				lastH = alts[0];
			}
			else
			{
				j--;
				lastPt = points[j];
				lastH = alts[j];
			}
			while (j-- > 0)
			{
				dist = calSurfaceDistance(lastPt, points[j], unit);
				dist = Math.sqrt(dist * dist + (alts[j] - lastH) * (alts[j] - lastH));
				totalDist += dist;
				lastPt = points[j];
				lastH = alts[j];
			}
			return totalDist;
		}
		else
		{
			if (lineString.getVectorType() == VectorType.LinearRing)
			{
				lastPt = points[0];
			}
			else
			{
				j--;
				lastPt = points[j];
			}
			while (j-- > 0)
			{
				totalDist += calSurfaceDistance(lastPt, points[j], unit);
				lastPt = points[j];
			}
			return totalDist;
		}
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
