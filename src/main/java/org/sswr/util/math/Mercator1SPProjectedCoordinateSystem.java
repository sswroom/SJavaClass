package org.sswr.util.math;

public class Mercator1SPProjectedCoordinateSystem extends ProjectedCoordinateSystem
{
	public Mercator1SPProjectedCoordinateSystem(String sourceName, int srid, String projName, double falseEasting, double falseNorthing, double centralMeridian, double latitudeOfOrigin, double scaleFactor, GeographicCoordinateSystem gcs, UnitType unit)
	{
		super(sourceName, srid, projName, falseEasting, falseNorthing, centralMeridian, latitudeOfOrigin, scaleFactor, gcs, unit);
	}

	public CoordinateSystem clone()
	{
		return new Mercator1SPProjectedCoordinateSystem(this.sourceName, this.srid, this.csysName, this.falseEasting, this.falseNorthing, this.getCentralMeridianDegree(), this.getLatitudeOfOriginDegree(), this.scaleFactor, (GeographicCoordinateSystem)this.gcs.clone(), this.unit);
	}

	public CoordinateSystemType getCoordSysType()
	{
		return CoordinateSystemType.Mercator1SPProjected;
	}

	public Coord2DDbl toGeographicCoordinateRad(Coord2DDbl projPos)
	{
		EarthEllipsoid ellipsoid = this.gcs.getEllipsoid();
		double rLon0 = this.rcentralMeridian;
		double a = ellipsoid.getSemiMajorAxis();
		return new Coord2DDbl(((projPos.x - this.falseEasting) / a + rLon0) * 180.0 / Math.PI,
			(Math.atan(Math.exp((projPos.y - this.falseNorthing) / a)) - Math.PI * 0.25) * 2);
	}

	public Coord2DDbl fromGeographicCoordinateRad(Coord2DDbl geoPos)
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
