package org.sswr.util.math;

import jakarta.annotation.Nonnull;

public class Mercator1SPProjectedCoordinateSystem extends ProjectedCoordinateSystem
{
	public Mercator1SPProjectedCoordinateSystem(@Nonnull String sourceName, int srid, @Nonnull String projName, double falseEasting, double falseNorthing, double centralMeridian, double latitudeOfOrigin, double scaleFactor, @Nonnull GeographicCoordinateSystem gcs, @Nonnull UnitType unit)
	{
		super(sourceName, srid, projName, falseEasting, falseNorthing, centralMeridian, latitudeOfOrigin, scaleFactor, gcs, unit);
	}

	@Nonnull
	public CoordinateSystem clone()
	{
		return new Mercator1SPProjectedCoordinateSystem(this.sourceName, this.srid, this.csysName, this.falseEasting, this.falseNorthing, this.getCentralMeridianDegree(), this.getLatitudeOfOriginDegree(), this.scaleFactor, (GeographicCoordinateSystem)this.gcs.clone(), this.unit);
	}

	@Nonnull
	public CoordinateSystemType getCoordSysType()
	{
		return CoordinateSystemType.Mercator1SPProjected;
	}

	@Nonnull
	public Coord2DDbl toGeographicCoordinateRad(@Nonnull Coord2DDbl projPos)
	{
		EarthEllipsoid ellipsoid = this.gcs.getEllipsoid();
		double rLon0 = this.rcentralMeridian;
		double a = ellipsoid.getSemiMajorAxis();
		return new Coord2DDbl(((projPos.x - this.falseEasting) / a + rLon0) * 180.0 / Math.PI,
			(Math.atan(Math.exp((projPos.y - this.falseNorthing) / a)) - Math.PI * 0.25) * 2);
	}

	@Nonnull
	public Coord2DDbl fromGeographicCoordinateRad(@Nonnull Coord2DDbl geoPos)
	{
		EarthEllipsoid ellipsoid = this.gcs.getEllipsoid();
		double rLat = geoPos.getLat();
		double rLon = geoPos.getLon();
		double rLon0 = this.rcentralMeridian;
		double a = ellipsoid.getSemiMajorAxis();
		double dlon = rLon - rLon0;
		return new Coord2DDbl(this.falseEasting + dlon * a,
			this.falseNorthing + a * Math.log(Math.tan(Math.PI * 0.25 + rLat * 0.5)));
	}
}
