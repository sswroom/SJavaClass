package org.sswr.util.math;

import org.sswr.util.data.SharedDouble;
import org.sswr.util.math.unit.Distance;

public class EarthEllipsoid
{
	public enum EarthEllipsoidType
	{
		OTHER,
		PLESSIS,
		EVEREST1830,
		EVEREST1830M,
		EVEREST1830N,
		AIRY1830,
		AIRY1830M,
		BESSEL1841,
		CLARKE1866,
		CLARKE1878,
		CLARKE1880,
		HELMERT1906,
		HAYFORD1910,
		INTL1924,
		KRASSOVSKY1940,
		WGS66,
		AUSTRALIAN1966,
		NEWINTL1967,
		GPS67,
		SAM1969,
		WGS72,
		GRS80,
		WGS84,
		WGS84_OGC,
		IERS1989,
		IERS2003
	}

	private double semiMajorAxis;
	private double inverseFlattening;
	private double semiMinorAxis;
	private double eccentricity;
	private EarthEllipsoidType eet;

	public EarthEllipsoid(double semiMajorAxis, double inverseFlattening, EarthEllipsoidType eet)
	{
		this.eet = eet;
		this.semiMajorAxis = semiMajorAxis;
		this.inverseFlattening = inverseFlattening;
		this.semiMinorAxis = this.semiMajorAxis * (1.0 - 1.0 / this.inverseFlattening);
		double f = 1 - getSemiMinorAxis() / this.semiMajorAxis;
		this.eccentricity = Math.sqrt(2 * f - f * f);
	}

	public double calSurfaceDistance(double dLat1, double dLon1, double dLat2, double dLon2, Distance.DistanceUnit unit)
	{
		double r;
		double rLat1;
		double rLon1;
		double rLat2;
		double rLon2;
		if (dLat1 == dLat2 && dLon1 == dLon2)
			return 0;
	
		rLat1 = dLat1 * Math.PI / 180.0;
		rLon1 = dLon1 * Math.PI / 180.0;
		rLat2 = dLat2 * Math.PI / 180.0;
		rLon2 = dLon2 * Math.PI / 180.0;
		double y = (rLat1 + rLat2) * 0.5;
		double tmpV = this.eccentricity * Math.sin(y);
		r = this.semiMajorAxis * (1 - this.eccentricity * this.eccentricity) / Math.pow(1 - tmpV * tmpV, 1.5);
		double d = Math.acos(Math.cos(rLat1) * Math.cos(rLon1) * Math.cos(rLat2) * Math.cos(rLon2) + Math.cos(rLat1) * Math.sin(rLon1) * Math.cos(rLat2) * Math.sin(rLon2) + Math.sin(rLat1) * Math.sin(rLat2)) * r;
		if (d > 0 || d < 0)
		{
			if (unit != Distance.DistanceUnit.Meter)
			{
				d = Distance.convert(Distance.DistanceUnit.Meter, unit, d);
			}
		}
		else if (d != 0)
		{
			d = 0;
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
					totalDist += calSurfaceDistance(lastY, lastX, points[(j << 1) + 1], points[(j << 1)], unit);
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
					dist = calSurfaceDistance(lastY, lastX, points[(j << 1) + 1], points[(j << 1)], unit);
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

	public double getSemiMajorAxis()
	{
		return this.semiMajorAxis;
	}

	public double getSemiMinorAxis()
	{
		return this.semiMinorAxis;
	}

	public double getInverseFlattening()
	{
		return this.inverseFlattening;
	}

	public double getEccentricity()
	{
		return this.eccentricity;
	}

	public double calLonByDist(double lat, double lon, double distM)
	{
		Double rlat = lat * Math.PI / 180.0;
		Double r = calRadiusAtLat(lat);
		Double diff = Math.atan2(Math.sin(distM / r) * Math.cos(rlat), Math.cos(distM / r));
		return lon + diff * 180.0 / Math.PI;
	}

	public double calLatByDist(double lat, double distM)
	{
		Double r = calRadiusAtLat(lat);
		Double rlat = lat * Math.PI / 180.0;
		return 180.0 / Math.PI * (rlat + (distM / r));
	}

	public double calRadiusAtLat(double lat)
	{
		Double rlat = lat * Math.PI / 180.0;
		Double ec = Math.cos(rlat) * this.eccentricity;
		return this.semiMajorAxis / Math.sqrt(1.0 - ec * ec);
	}

	public boolean equals(EarthEllipsoid ellipsoid)
	{
		return ellipsoid.semiMajorAxis == this.semiMajorAxis && ellipsoid.inverseFlattening == this.inverseFlattening;
	}

	public String getName()
	{
		return this.eet.toString();
	}

	public void set(EarthEllipsoid ellipsoid)
	{
		this.semiMajorAxis = ellipsoid.semiMajorAxis;
		this.semiMinorAxis = ellipsoid.semiMinorAxis;
		this.inverseFlattening = ellipsoid.inverseFlattening;
		this.eccentricity = ellipsoid.eccentricity;
		this.eet = ellipsoid.eet;		
	}

	public EarthEllipsoid clone()
	{
		return new EarthEllipsoid(this.semiMajorAxis, this.inverseFlattening, this.eet);
	}

	public void toCartesianCoord(double dLat, double dLon, double h, SharedDouble x, SharedDouble y, SharedDouble z)
	{
		double rLat = dLat * Math.PI / 180.0;
		double rLon = dLon * Math.PI / 180.0;
		double cLat = Math.cos(rLat);
		double sLat = Math.sin(rLat);
		double cLon = Math.cos(rLon);
		double sLon = Math.sin(rLon);
		double e2 = this.eccentricity * this.eccentricity;
		double v = this.semiMajorAxis / Math.sqrt(1 - e2 * sLat * sLat);
		x.value = (v + h) * cLat * cLon;
		y.value = (v + h) * cLat * sLon;
		z.value = ((1 - e2) * v + h) * sLat;
	}

	public void fromCartesianCoord(double x, double y, double z, SharedDouble dLat, SharedDouble dLon, SharedDouble h)
	{
		double e2 = this.eccentricity * this.eccentricity;
		double rLon = Math.atan2(y, x);
		double p = Math.sqrt(x * x + y * y);
		double rLat = Math.atan2(z, p * (1 - e2));
		double sLat;
		double thisLat;
		double v = 0;
		int i = 10;
		while (i-- > 0)
		{
			sLat = Math.sin(rLat);
			v = this.semiMajorAxis / Math.sqrt(1 - e2 * sLat * sLat);
			thisLat = Math.atan2(z + e2 * v * sLat, p);
			if (thisLat == rLat)
				break;
			rLat = thisLat;
		}
		dLat.value = rLat * 180 / Math.PI;
		dLon.value = rLon * 180 / Math.PI;
		h.value = p / Math.cos(rLat) - v;
	}
}