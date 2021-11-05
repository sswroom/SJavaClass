package org.sswr.util.math;

import org.sswr.util.data.SharedDouble;

public class MercatorProjectedCoordinateSystem extends ProjectedCoordinateSystem
{
	public MercatorProjectedCoordinateSystem(String sourceName, int srid, String projName, double falseEasting, double falseNorthing, double centralMeridian, double latitudeOfOrigin, double scaleFactor, GeographicCoordinateSystem gcs, UnitType unit)
	{
		super(sourceName, srid, projName, falseEasting, falseNorthing, centralMeridian, latitudeOfOrigin, scaleFactor, gcs, unit);
	}

	public CoordinateSystem clone()
	{
		return new MercatorProjectedCoordinateSystem(this.sourceName, this.srid, this.csysName, this.falseEasting, this.falseNorthing, this.centralMeridian, this.latitudeOfOrigin, this.scaleFactor, (GeographicCoordinateSystem)this.gcs.clone(), this.unit);
	}

	public CoordinateSystemType getCoordSysType()
	{
		return CoordinateSystemType.MercatorProjected;
	}

	public void toGeographicCoordinate(double projX, double projY, SharedDouble geoX, SharedDouble geoY)
	{
		EarthEllipsoid ellipsoid = this.gcs.getEllipsoid();
		double aF = ellipsoid.getSemiMajorAxis() * this.scaleFactor;
		double rLatL = (projY - this.falseNorthing) / aF + (this.latitudeOfOrigin * Math.PI / 180.0);
		double rLastLat;
		double e = ellipsoid.getEccentricity();
		double e2 = e * e;
		double tmpV;
		while (true)
		{
			tmpV = projY - this.falseNorthing - this.calcM(rLatL);
			rLastLat = rLatL;
			rLatL = rLatL + tmpV / aF;
			if (rLastLat == rLatL || (tmpV < 0.000000001 && tmpV > -0.000000001))
				break;
		}
		double sLat = Math.sin(rLatL);
		double tLat = Math.tan(rLatL);
		double tLat2 = tLat * tLat;
		double tLat4 = tLat2 * tLat2;
		double secLat = 1 / Math.cos(rLatL);
		double tmp = 1 - e2 * sLat * sLat;
		double v = aF / Math.sqrt(tmp);
		double v2 = v * v;
		double v3 = v * v2;
		double v5 = v3 * v2;
		double v7 = v5 * v2;
		double p = v * (1 - e2) / tmp;
		double nb2 = v / p - 1;
	
		double ser7 = tLat / (2 * p * v);
		double ser8 = tLat / (24 * p * v3) * (5 + 3 * tLat2 + nb2 - 9 * tLat2 * nb2);
		double ser9 = tLat / (720 * p * v5) * (61 + 90 * tLat2 + 45 * tLat4);
		double ser10 = secLat / v;
		double ser11 = secLat / (6 * v3) * (v / p + 2 * tLat2);
		double ser12 = secLat / (120 * v5) * (5 + 28 * tLat2 + 24 * tLat4);
		double ser12a = secLat / (5040 * v7) * (61 + 662 * tLat2 + 1320 * tLat4 + 720 * tLat4 * tLat2);
	
		double eDiff = projX - this.falseEasting;
		double outX = (this.centralMeridian * Math.PI / 180.0) + ser10 * eDiff - ser11 * Math.pow(eDiff, 3) + ser12 * Math.pow(eDiff, 5) - ser12a * Math.pow(eDiff, 7);
		double outY = rLatL - ser7 * Math.pow(eDiff, 2) + ser8 * Math.pow(eDiff, 4) - ser9 * Math.pow(eDiff, 6);
	
		geoX.value = (outX * 180.0 / Math.PI);
		geoY.value = (outY * 180.0 / Math.PI);
	}

	public void fromGeographicCoordinate(double geoX, double geoY, SharedDouble projX, SharedDouble projY)
	{
		EarthEllipsoid ellipsoid = this.gcs.getEllipsoid();
		double rLat = geoY * Math.PI / 180.0;
		double rLon = geoX * Math.PI / 180.0;
		double rLon0 = this.centralMeridian * Math.PI / 180;
		double sLat = Math.sin(rLat);
		double a = ellipsoid.getSemiMajorAxis();
		double e = ellipsoid.getEccentricity();
		double e2 = e * e;
		double tmp = 1 - e2 * sLat * sLat;
		double v = a * this.scaleFactor / Math.sqrt(tmp);
		double p = v * (1 - e2) / tmp;
		double nb2 = v / p - 1;
		double m = this.calcM(rLat);
		double cLat = Math.cos(rLat);
		double tLat = Math.tan(rLat);
		double tLat2 = tLat * tLat;
		double tLat4 = tLat2 * tLat2;
		double cLat3 = cLat * cLat * cLat;
		double cLat5 = cLat3 * cLat * cLat;
		
		double ser1 = m + this.falseNorthing;
		double ser2 = v * 0.5 * cLat * sLat;
		double ser3 = v / 24 * sLat * cLat3 * (5 - tLat2 + 9 * nb2);
		double ser3a = v / 720 * sLat * cLat5 * (61 - 58 * tLat2 + tLat4);
		double ser4 = v * cLat;
		double ser5 = v / 6 * cLat3 * (v / p - tLat2);
		double ser6 = v / 120 * cLat5 * (5 - 18 * tLat2 + tLat4 + 14 * nb2 - 58 * tLat2 * nb2);
		double dlon = rLon - rLon0;
		double dlon2 = dlon * dlon;
		double dlon4 = dlon2 * dlon2;
	
		projX.value = this.falseEasting + ser4 * dlon + ser5 * dlon * dlon2 + ser6 * dlon * dlon4;
		projY.value = ser1 + ser2 * dlon2 + ser3 * dlon4 + ser3a * dlon4 * dlon2;
	}

	public double calcM(double rLat)
	{
		EarthEllipsoid ellipsoid = this.gcs.getEllipsoid();
		double a = ellipsoid.getSemiMajorAxis();
		double b = ellipsoid.getSemiMinorAxis();
		double n = (a - b) / (a + b);
		double n2 = n * n;
		double n3 = n2 * n;
		double rLat0 = this.latitudeOfOrigin * Math.PI / 180;
		double m;
		m = (1 + n + 1.25 * n2 + 1.25 * n3) * (rLat - rLat0);
		m = m - (3 * n + 3 * n2  + 2.625 * n3) * Math.sin(rLat - rLat0) * Math.cos(rLat + rLat0);
		m = m + (1.875 * n2 + 1.875 * n3) * Math.sin(2 * (rLat - rLat0)) * Math.cos(2 * (rLat + rLat0));
		m = m - 35 / 24 * n3 * Math.sin(3 * (rLat - rLat0)) * Math.cos(3 * (rLat + rLat0));
		m = m * b * this.scaleFactor;
		return m;
	}
}
