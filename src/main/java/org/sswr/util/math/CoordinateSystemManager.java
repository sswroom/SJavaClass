package org.sswr.util.math;

import org.sswr.util.math.CoordinateSystem.PrimemType;
import org.sswr.util.math.CoordinateSystem.UnitType;
import org.sswr.util.math.EarthEllipsoid.EarthEllipsoidType;
import org.sswr.util.math.unit.Angle.AngleUnit;

public class CoordinateSystemManager
{
	public static SpheroidInfo srGetSpheroid(int epsgId)
	{
		switch (epsgId)
		{
		case 7012:
			return new SpheroidInfo(7012, EarthEllipsoidType.CLARKE1880, "Clarke 1880 (RGS)");
		case 7022:
			return new SpheroidInfo(7022, EarthEllipsoidType.INTL1924, "International 1924");
		case 7030:
			return new SpheroidInfo(7030, EarthEllipsoidType.WGS84, "WGS 84");
		default:
			return null;
		}
	}

	public static DatumInfo srGetDatum(int epsgId)
	{
		switch (epsgId)
		{
		case 6326:
			return new DatumInfo(6326,  7030,  "WGS_1984", 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, AngleUnit.Radian);
		case 6600:
			return new DatumInfo(6600,  7012,  "Anguilla_1957", 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, AngleUnit.Radian);
		case 6601:
			return new DatumInfo(6601,  7012,  "Antigua_1943", 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, AngleUnit.Radian);
		case 6602:
			return new DatumInfo(6602,  7012,  "Dominica_1945", 0, 0, 0, 725, 685, 536, 0, 0, 0, 0, AngleUnit.Arcsecond);
		case 6603:
			return new DatumInfo(6603,  7012,  "Grenada_1953", 0, 0, 0, 72, 213.7, 93, 0, 0, 0, 0, AngleUnit.Arcsecond);
		case 6611:
			return new DatumInfo(6611,  7022,  "Hong_Kong_1980", 0, 0, 0, -162.619, -276.959, -161.764, 0.067753, -2.24365, -1.15883, -1.09425, AngleUnit.Arcsecond);
		default:
			return null;
		}
	}

	public static GeogcsSRInfo srGetGeogcsInfo(int epsgId)
	{
		switch (epsgId)
		{
		case 4326:
			return new GeogcsSRInfo(4326,  6326,  "WGS 84", PrimemType.Greenwich, UnitType.Degree, 0.0174532925199433);
		case 4600:
			return new GeogcsSRInfo(4600,  6600,  "Anguilla 1957", PrimemType.Greenwich, UnitType.Degree, 0.0174532925199433);
		case 4601:
			return new GeogcsSRInfo(4601,  6601,  "Antigua 1943", PrimemType.Greenwich, UnitType.Degree, 0.0174532925199433);
		case 4602:
			return new GeogcsSRInfo(4602,  6602,  "Dominica 1945", PrimemType.Greenwich, UnitType.Degree, 0.0174532925199433);
		case 4603:
			return new GeogcsSRInfo(4603,  6603,  "Grenada 1953", PrimemType.Greenwich, UnitType.Degree, 0.0174532925199433);
		case 4611:
			return new GeogcsSRInfo(4611,  6611,  "Hong Kong 1980", PrimemType.Greenwich, UnitType.Degree, 0.0174532925199433);
		default:
			return null;
		}
	}

	public static CoordinateSystem srCreateCSys(int epsgId)
	{
		switch (epsgId)
		{
		case 2000:
		case 2001:
		case 2002:
		case 2003:
		case 2326:
		case 3857:
		case 102100:
		case 102140:
		case 900913:
			return srCreateProjCSys(epsgId);
		case 4326:
		case 4600:
		case 4601:
		case 4602:
		case 4603:
		case 4611:
			return srCreateGeogCSys(epsgId);
		}
		System.out.println("Unsupported SRID: "+epsgId);
		return null;
	}

	public static CoordinateSystem srCreateCSysOrDef(int epsgId)
	{
		CoordinateSystem csys;
		if ((csys = srCreateCSys(epsgId)) != null)
			return csys;
		else
			return createWGS84Csys();
	}

	public static ProjectedCoordinateSystem srCreateProjCSys(int epsgId)
	{
		switch (epsgId)
		{
		case 2000:
			return new MercatorProjectedCoordinateSystem("EPSG:"+epsgId, epsgId, "Anguilla 1957 / British West Indies Grid", 400000, 0, -62, 0, 0.9995, srCreateGeogCSys(4600), UnitType.Metre);
		case 2001:
			return new MercatorProjectedCoordinateSystem("EPSG:"+epsgId, epsgId, "Antigua 1943 / British West Indies Grid", 400000, 0, -62, 0, 0.9995, srCreateGeogCSys(4601), UnitType.Metre);
		case 2002:
			return new MercatorProjectedCoordinateSystem("EPSG:"+epsgId, epsgId, "Dominica 1945 / British West Indies Grid", 400000, 0, -62, 0, 0.9995, srCreateGeogCSys(4602), UnitType.Metre);
		case 2003:
			return new MercatorProjectedCoordinateSystem("EPSG:"+epsgId, epsgId, "Grenada 1953 / British West Indies Grid",  400000, 0, -62, 0, 0.9995, srCreateGeogCSys(4603), UnitType.Metre);
		case 2326:
			return new MercatorProjectedCoordinateSystem("EPSG:"+epsgId, epsgId, "Hong Kong 1980 Grid System", 836694.05, 819069.80, 114.17855555555555555555555555556, 22.312133333333333333333333333333, 1, srCreateGeogCSys(4611), UnitType.Metre);
		case 3857:
			return new Mercator1SPProjectedCoordinateSystem("EPSG:"+epsgId, epsgId, "WGS 84 / Pseudo-Mercator", 0, 0, 0, 0, 1, srCreateGeogCSys(4326), UnitType.Metre);
		case 102100:
			return new Mercator1SPProjectedCoordinateSystem("EPSG:"+epsgId, epsgId, "WGS 84 / Pseudo-Mercator", 0, 0, 0, 0, 1, srCreateGeogCSys(4326), UnitType.Metre);
		case 102140:
			return new MercatorProjectedCoordinateSystem("EPSG:"+epsgId, epsgId, "Hong Kong 1980 Grid System", 836694.05, 819069.80, 114.17855555555555555555555555556, 22.312133333333333333333333333333, 1, srCreateGeogCSys(4611), UnitType.Metre);
		case 900913:
			return new Mercator1SPProjectedCoordinateSystem("EPSG:"+epsgId, epsgId, "Google_Maps_Global_Mercator", 0, 0, 0, 0, 1, srCreateGeogCSys(4326), UnitType.Metre);
		}
		return null;
	}

	public static GeographicCoordinateSystem srCreateGeogCSys(int epsgId)
	{
		GeogcsSRInfo geogcs = srGetGeogcsInfo(epsgId);
		if (geogcs == null)
		{
			return null;
		}
		DatumInfo datum = srGetDatum(geogcs.getDatum());
		if (datum == null)
		{
			return null;
		}
		SpheroidInfo spheroid = srGetSpheroid(datum.getSpheroid());
		if (spheroid == null)
		{
			return null;
		}
		EarthEllipsoid ellipsoid = new EarthEllipsoid(spheroid.getEet());
		DatumData data = fillDatumData(datum, datum.getDatumName(), ellipsoid, spheroid);
		return new GeographicCoordinateSystem("EPSG:"+epsgId, epsgId, geogcs.getName(), data, geogcs.getPrimem(), geogcs.getUnit());
	}

	public static DatumInfo getDatumInfoByName(String name)
	{
		if (name.startsWith("D_"))
		{
			name = name.substring(2);
		}
		switch (name.toUpperCase())
		{
		case "WGS_1984":
			return srGetDatum(6326);
		case "ANGUILLA_1957":
			return srGetDatum(6600);
		case "ANTIGUA_1943":
			return srGetDatum(6601);
		case "DOMINICA_1945":
			return srGetDatum(6602);
		case "GRENADA_1953":
			return srGetDatum(6603);
		case "HONG_KONG_1980":
			return srGetDatum(6611);
		}
		return null;
	}

	public static DatumData fillDatumData(DatumInfo datum, String name, EarthEllipsoid ee, SpheroidInfo spheroid)
	{
		DatumData data = new DatumData();
		if (datum != null)
		{
			data.setSrid(datum.getSrid());
			if (spheroid != null)
			{
				data.setSpheroid(new SpheroidData(datum.getSpheroid(), spheroid.getName(), ee));
			}
			else
			{
				data.setSpheroid(new SpheroidData(datum.getSpheroid(), datum.getDatumName(), ee));
			}
			data.setName(datum.getDatumName());
			data.setX0(datum.getX0());
			data.setY0(datum.getY0());
			data.setZ0(datum.getZ0());
			data.setCX(datum.getCX());
			data.setCY(datum.getCY());
			data.setCZ(datum.getCZ());
			data.setXAngle(datum.getXAngle());
			data.setYAngle(datum.getYAngle());
			data.setZAngle(datum.getZAngle());
			data.setScale(datum.getScale());
			data.setAunit(datum.getAunit());
		}
		else
		{
			data.setSrid(0);
			data.setSpheroid(new SpheroidData(0, name, ee));
			data.setName(name);
			data.setX0(0);
			data.setY0(0);
			data.setZ0(0);
			data.setCX(0);
			data.setCY(0);
			data.setCZ(0);
			data.setXAngle(0);
			data.setYAngle(0);
			data.setZAngle(0);
			data.setScale(0);
			data.setAunit(AngleUnit.Radian);
		}
		return data;
	}

	public static GeographicCoordinateSystem createWGS84Csys()
	{
		DatumInfo datum = getDatumInfoByName("WGS_1984");
		EarthEllipsoid ellipsoid = new EarthEllipsoid(EarthEllipsoidType.WGS84);
		DatumData data = fillDatumData(datum, "WGS_1984", ellipsoid, srGetSpheroid(datum.getSpheroid()));
		return new GeographicCoordinateSystem("WGS_1984", 4326, "WGS_1984", data, PrimemType.Greenwich, UnitType.Degree);
	}
}
