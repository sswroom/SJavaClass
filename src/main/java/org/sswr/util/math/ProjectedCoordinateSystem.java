package org.sswr.util.math;

import org.sswr.util.math.geometry.LineString;
import org.sswr.util.math.geometry.Vector2D.VectorType;
import org.sswr.util.math.unit.Distance;

import jakarta.annotation.Nonnull;

public abstract class ProjectedCoordinateSystem extends CoordinateSystem
{
	protected GeographicCoordinateSystem gcs;
	protected double falseEasting;
	protected double falseNorthing;
	protected double rcentralMeridian;
	protected double rlatitudeOfOrigin;
	protected double scaleFactor;
	protected UnitType unit;

	public ProjectedCoordinateSystem(@Nonnull String sourceName, int srid, @Nonnull String projName, double falseEasting, double falseNorthing, double dcentralMeridian, double dlatitudeOfOrigin, double scaleFactor, @Nonnull GeographicCoordinateSystem gcs, @Nonnull UnitType unit)
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

	public double calSurfaceDistance(@Nonnull Coord2DDbl pos1, @Nonnull Coord2DDbl pos2, @Nonnull Distance.DistanceUnit unit)
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

	public double calLineStringDistance(@Nonnull LineString lineString, boolean include3D, @Nonnull Distance.DistanceUnit unit)
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
	
	@Nonnull
	public abstract CoordinateSystem clone();
	@Nonnull
	public abstract CoordinateSystemType getCoordSysType();
	
	public boolean isProjected()
	{
		return true;
	}

	public void toString(@Nonnull StringBuilder sb)
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

	@Nonnull
	public GeographicCoordinateSystem getGeographicCoordinateSystem()
	{
		return this.gcs;
	}

	@Nonnull
	public abstract Coord2DDbl toGeographicCoordinateRad(@Nonnull Coord2DDbl projPos);
	@Nonnull
	public abstract Coord2DDbl fromGeographicCoordinateRad(@Nonnull Coord2DDbl geoPos);
	@Nonnull
	public Coord2DDbl toGeographicCoordinateDeg(@Nonnull Coord2DDbl projPos)
	{
		Coord2DDbl geoPos = this.toGeographicCoordinateRad(projPos);
		geoPos.x = geoPos.x * 180 / Math.PI;
		geoPos.y = geoPos.y * 180 / Math.PI;
		return geoPos;
	}

	@Nonnull
	public Coord2DDbl fromGeographicCoordinateDeg(@Nonnull Coord2DDbl geoPos)
	{
		return this.fromGeographicCoordinateRad(new Coord2DDbl(geoPos.x * Math.PI / 180.0, geoPos.y * Math.PI / 180.0));
	}

	public boolean sameProjection(@Nonnull ProjectedCoordinateSystem csys)
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

	@Nonnull
	public UnitType getUnit()
	{
		return this.unit;
	}
}
