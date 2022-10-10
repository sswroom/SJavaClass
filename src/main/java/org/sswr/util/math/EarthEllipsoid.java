package org.sswr.util.math;

import org.sswr.util.data.SharedDouble;
import org.sswr.util.math.geometry.Polyline;
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

	public EarthEllipsoid(EarthEllipsoidType eet)
	{
		switch (eet)
		{
		default:
		case OTHER:
			this.semiMajorAxis = 6378137.0;
			this.inverseFlattening = 191.0;
			break;
		case PLESSIS:
			this.semiMajorAxis = 6376523.0;
			this.inverseFlattening = 308.64;
			break;
		case EVEREST1830:
			this.semiMajorAxis = 6377299.365;
			this.inverseFlattening = 300.80172554;
			break;
		case EVEREST1830M:
			this.semiMajorAxis = 6377304.063;
			this.inverseFlattening = 300.8017;
			break;
		case EVEREST1830N:
			this.semiMajorAxis = 6377298.556;
			this.inverseFlattening = 300.8017;
			break;
		case AIRY1830:
			this.semiMajorAxis = 6377563.396;
			this.inverseFlattening = 299.3249646;
			break;
		case AIRY1830M:
			this.semiMajorAxis = 6377340.189;
			this.inverseFlattening = 299.32495141450600500090538973015;
			break;
		case BESSEL1841:
			this.semiMajorAxis = 6377397.155;
			this.inverseFlattening = 299.1528128;
			break;
		case CLARKE1866:
			this.semiMajorAxis = 6378206.4;
			this.inverseFlattening = 294.9786982;
			break;
		case CLARKE1878:
			this.semiMajorAxis = 6378190.0;
			this.inverseFlattening = 293.4659980;
			break;
		case CLARKE1880:
			this.semiMajorAxis = 6378249.145;
			this.inverseFlattening = 293.465;
			break;
		case HELMERT1906:
			this.semiMajorAxis = 6378200.0;
			this.inverseFlattening = 298.3;
			break;
		case HAYFORD1910:
			this.semiMajorAxis = 6378388.0;
			this.inverseFlattening = 297.0;
			break;
		case INTL1924:
			this.semiMajorAxis = 6378388.0;
			this.inverseFlattening = 297.0;
			break;
		case KRASSOVSKY1940:
			this.semiMajorAxis = 6378245.0;
			this.inverseFlattening = 298.3;
			break;
		case WGS66:
			this.semiMajorAxis = 6378145.0;
			this.inverseFlattening = 298.25;
			break;
		case AUSTRALIAN1966:
			this.semiMajorAxis = 6378160.0;
			this.inverseFlattening = 298.25;
			break;
		case NEWINTL1967:
			this.semiMajorAxis = 6378157.5;
			this.inverseFlattening = 298.24961539;
			break;
		case GPS67:
			this.semiMajorAxis = 6378160.0;
			this.inverseFlattening = 298.247167427;
			break;
		case SAM1969:
			this.semiMajorAxis = 6378160.0;
			this.inverseFlattening = 298.25;
			break;
		case WGS72:
			this.semiMajorAxis = 6378135.0;
			this.inverseFlattening = 298.26;
			break;
		case GRS80:
			this.semiMajorAxis = 6378137.0;
			this.inverseFlattening = 298.257222101;
			break;
		case WGS84:
			this.semiMajorAxis = 6378137.0;
			this.inverseFlattening = 298.257223563;
			break;
		case WGS84_OGC:
			this.semiMajorAxis = 6378137.0;
			this.inverseFlattening = 298.257222932867;
			break;
		case IERS1989:
			this.semiMajorAxis = 6378136.0;
			this.inverseFlattening = 298.257;
			break;
		case IERS2003:
			this.semiMajorAxis = 6378136.6;
			this.inverseFlattening = 298.25642;
			break;
		}
		this.eet = eet;
		this.semiMinorAxis = this.semiMajorAxis * (1.0 - 1.0 / this.inverseFlattening);
		double f = 1 - getSemiMinorAxis() / this.semiMajorAxis;
		this.eccentricity = Math.sqrt(2 * f - f * f);
	}
	
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
					totalDist += calSurfaceDistance(lastY, lastX, points[j].y, points[j].x, unit);
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
					dist = calSurfaceDistance(lastY, lastX, points[j].y, points[j].x, unit);
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

	public void toCartesianCoordRad(double rLat, double rLon, double h, SharedDouble x, SharedDouble y, SharedDouble z)
	{
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

	public void fromCartesianCoordRad(double x, double y, double z, SharedDouble outLat, SharedDouble outLon, SharedDouble h)
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
		outLat.value = rLat;
		outLon.value = rLon;
		h.value = p / Math.cos(rLat) - v;
	}

	public void toCartesianCoordDeg(double dLat, double dLon, double h, SharedDouble x, SharedDouble y, SharedDouble z)
	{
		this.toCartesianCoordRad(dLat * Math.PI / 180.0, dLon * Math.PI / 180.0, h, x, y, z);
	}

	public void tromCartesianCoordDeg(double x, double y, double z, SharedDouble dLat, SharedDouble dLon, SharedDouble h)
	{
		this.fromCartesianCoordRad(x, y, z, dLat, dLon, h);
		dLat.value = dLat.value * 180.0 / Math.PI;
		dLon.value = dLon.value * 180.0 / Math.PI;
	}
}
