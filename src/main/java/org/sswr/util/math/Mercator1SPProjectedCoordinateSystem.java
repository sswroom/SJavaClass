package org.sswr.util.math;

import org.sswr.util.data.SharedDouble;

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

	public void toGeographicCoordinateRad(double projX, double projY, SharedDouble geoX, SharedDouble geoY)
	{
		EarthEllipsoid ellipsoid = this.gcs.getEllipsoid();
		double rLon0 = this.rcentralMeridian;
		double a = ellipsoid.getSemiMajorAxis();
		geoX.value = ((projX - this.falseEasting) / a + rLon0) * 180.0 / Math.PI;
		geoY.value = (Math.atan(Math.exp((projY - this.falseNorthing) / a)) - Math.PI * 0.25) * 2;
	}

	public void fromGeographicCoordinateRad(double geoX, double geoY, SharedDouble projX, SharedDouble projY)
	{
		EarthEllipsoid ellipsoid = this.gcs.getEllipsoid();
		double rLat = geoY;
		double rLon = geoX;
		double rLon0 = this.rcentralMeridian;
		double a = ellipsoid.getSemiMajorAxis();
		double dlon = rLon - rLon0;
		projX.value = this.falseEasting + dlon * a;
		projY.value = this.falseNorthing + a * Math.log(Math.tan(Math.PI * 0.25 + rLat * 0.5));
	}
}
