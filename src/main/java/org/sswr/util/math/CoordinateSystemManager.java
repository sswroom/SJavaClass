package org.sswr.util.math;

import java.io.FileInputStream;
import java.io.IOException;

import org.sswr.util.data.SharedInt;
import org.sswr.util.data.StringUtil;
import org.sswr.util.math.CoordinateSystem.PrimemType;
import org.sswr.util.math.CoordinateSystem.UnitType;
import org.sswr.util.math.EarthEllipsoid.EarthEllipsoidType;
import org.sswr.util.math.unit.Angle.AngleUnit;

public class CoordinateSystemManager
{
	public static CoordinateSystem parsePRJFile(String fileName)
	{
		try
		{
			FileInputStream fis = new FileInputStream(fileName);
			byte[] buff = fis.readNBytes(512);
			fis.close();
			if (buff.length == 512)
			{
				return null;
			}

			SharedInt buffSize = new SharedInt();
			buffSize.value = buff.length;
			return parsePRJBuff(fileName, buff, 0, buffSize);
		}
		catch (IOException ex)
		{
			return null;
		}
	}

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

	public static CoordinateSystem parsePRJBuff(String sourceName, byte[] prjBuff, int ofst, SharedInt parsedSize)
	{
		SharedInt tmpInt = new SharedInt();
		int i;
		int j;
		int nameOfst;
		int datumOfst = 0;
		int spIndex = 0;
		double a = 0;
		double f_1 = 0;
		CoordinateSystem csys = null;
		EarthEllipsoid.EarthEllipsoidType eet;
		GeographicCoordinateSystem gcs = null;
		PrimemType primem = PrimemType.Greenwich;
		UnitType unit = UnitType.Degree;
		byte c;
		int srid = 0;
		if (StringUtil.startsWith(prjBuff, ofst, "GEOGCS["))
		{
			i = 7;
			if (!parsePRJString(prjBuff, ofst + i, tmpInt))
				return null;
			j = tmpInt.value;
			nameOfst = i + 1;
			prjBuff[ofst + i + j - 1] = 0;
			i += j;
			while (true)
			{
				c = prjBuff[ofst + i];
				if (c == ']')
				{
					i++;
					break;
				}
				else if (c == ',')
				{
					i++;
					if (StringUtil.startsWith(prjBuff, ofst + i, "DATUM["))
					{
						i += 6;
						if (!parsePRJString(prjBuff, ofst + i, tmpInt))
							return null;
						j = tmpInt.value;
						datumOfst = i + 1;
						prjBuff[ofst + i + j - 1] = 0;
						i += j;
						while (true)
						{
							c = prjBuff[ofst + i];
							if (c == ']')
							{
								break;
							}
							else if (c == 0)
							{
								return null;
							}
							else if (c == ',')
							{
								i++;
								if (StringUtil.startsWith(prjBuff, ofst + i, "SPHEROID["))
								{
									i += 9;
									if (!parsePRJString(prjBuff, ofst + i, tmpInt))
										return null;
									j = tmpInt.value;
									prjBuff[ofst + i + j - 1] = 0;
									i += j;
									j = -1;
									spIndex = 1;
									while (true)
									{
										c = prjBuff[ofst + i];
										if (c == ']' || c == ',')
										{
											if (j >= 0)
											{
												if (spIndex == 1)
												{
													a = StringUtil.toDouble(new String(prjBuff, ofst + j, i - j));
												}
												else if (spIndex == 2)
												{
													f_1 = StringUtil.toDouble(new String(prjBuff, ofst + j, i - j));
												}
												spIndex++;
											}
											i++;
											if (c == ']')
												break;
											j = i;
										}
										else if (c == 0)
										{
											return null;
										}
										else
										{
											i++;
										}
									}
								}
								else
								{
									return null;
								}
							}
							else
							{
								return null;
							}
						}
						i++;
					}
					else if (StringUtil.startsWith(prjBuff, ofst + i, "PRIMEM["))
					{
						i += 7;
						if (!parsePRJString(prjBuff, ofst + i, tmpInt))
							return null;
						j = tmpInt.value;
						i += j;
						while (true)
						{
							c = prjBuff[ofst + i];
							if (c == ']')
								break;
							if (c == 0)
								return null;
							if (c == '[')
								return null;
							i++;
						}
						i++;
					}
					else if (StringUtil.startsWith(prjBuff, ofst + i, "UNIT["))
					{
						i += 5;
						if (!parsePRJString(prjBuff, ofst + i, tmpInt))
							return null;
						j = tmpInt.value;
						i += j;
						while (true)
						{
							c = prjBuff[ofst + i];
							if (c == ']')
								break;
							if (c == 0)
								return null;
							if (c == '[')
								return null;
							i++;
						}
						i++;
					}
					else
					{
						return null;
					}
				}
				else
				{
					return null;
				}
			}
			if (spIndex != 3)
			{
				return null;
			}
			if (parsedSize != null)
			{
				parsedSize.value = i;
			}
			eet = EarthEllipsoid.EarthEllipsoidType.OTHER;
			if (a == 6378137.0 && f_1 == 298.257223563)
			{
				eet = EarthEllipsoid.EarthEllipsoidType.WGS84;
			}
			else if (a == 6378137.0 && f_1 == 298.257222101)
			{
				eet = EarthEllipsoid.EarthEllipsoidType.GRS80;
			}
			else if (a == 6378206.4 && f_1 == 294.9786982)
			{
				eet = EarthEllipsoid.EarthEllipsoidType.CLARKE1866;
			}
			else if (a == 6378137.0 && f_1 == 298.257222932867)
			{
				eet = EarthEllipsoid.EarthEllipsoidType.WGS84_OGC;
			}
			else if (a == 6378388.0 && f_1 == 297.0)
			{
				eet = EarthEllipsoid.EarthEllipsoidType.INTL1924;
			}
			else if (a == 6378388.0 && f_1 == 297.0000000000601)
			{
				eet = EarthEllipsoid.EarthEllipsoidType.INTL1924;
			}
			if (eet != EarthEllipsoid.EarthEllipsoidType.OTHER)
			{
				EarthEllipsoid ellipsoid = new EarthEllipsoid(a, f_1, eet);
				DatumInfo datum = getDatumInfoByName(StringUtil.byte2String(prjBuff, ofst + datumOfst));
				DatumData data = fillDatumData(datum, StringUtil.byte2String(prjBuff, ofst + datumOfst), ellipsoid, null);
				csys = new GeographicCoordinateSystem(sourceName, srid, StringUtil.byte2String(prjBuff, ofst + nameOfst), data, primem, unit);
				return csys;
			}
			else
			{
				EarthEllipsoid ellipsoid = new EarthEllipsoid(a, f_1, eet);
				DatumInfo datum = getDatumInfoByName(StringUtil.byte2String(prjBuff, ofst + datumOfst));
				DatumData data = fillDatumData(datum, StringUtil.byte2String(prjBuff, ofst + datumOfst), ellipsoid, null);
				csys = new GeographicCoordinateSystem(sourceName, srid, StringUtil.byte2String(prjBuff, ofst + nameOfst), data, primem, unit);
				return csys;
			}
		}
		else if (StringUtil.startsWith(prjBuff, ofst, "PROJCS["))
		{
			CoordinateSystemType cst = CoordinateSystemType.Geographic;
			double falseEasting = -1;
			double falseNorthing = -1;
			double centralMeridian = -1;
			double scaleFactor = -1;
			double latitudeOfOrigin = -1;
			int nOfst;
			int vOfst;
			boolean commaFound;
	
			i = 7;
			if (!parsePRJString(prjBuff, ofst + i, tmpInt))
				return null;
			j = tmpInt.value;
			nameOfst = i + 1;
			prjBuff[ofst + i + j - 1] = 0;
			i += j;
			while (true)
			{
				c = prjBuff[ofst + i];
				if (c == ']')
				{
					i++;
					break;
				}
				else if (c == ',')
				{
					i++;
					if (StringUtil.startsWith(prjBuff, ofst + i, "GEOGCS["))
					{
						tmpInt.value = j;
						gcs = (GeographicCoordinateSystem)parsePRJBuff(sourceName, prjBuff, ofst + i, tmpInt);
						if (gcs == null)
							return null;
						j = tmpInt.value;
						i += j;
					}
					else if (StringUtil.startsWith(prjBuff, ofst + i, "PROJECTION["))
					{
						if (StringUtil.startsWith(prjBuff, ofst + i + 11, "\"Transverse_Mercator\"]"))
						{
							i += 33;
							cst = CoordinateSystemType.MercatorProjected;
						}
						else if (StringUtil.startsWith(prjBuff, ofst + i + 11, "\"Mercator_1SP\"]"))
						{
							i += 26;
							cst = CoordinateSystemType.Mercator1SPProjected;
						}
						else if (StringUtil.startsWith(prjBuff, ofst + i + 11, "\"Gauss_Kruger\"]"))
						{
							i += 26;
							cst = CoordinateSystemType.GausskrugerProjected;
						}
						else
						{
							return null;
						}
					}
					else if (StringUtil.startsWith(prjBuff, ofst + i, "PARAMETER["))
					{
						i += 10;
						if (!parsePRJString(prjBuff, ofst + i, tmpInt))
						{
							return null;
						}
						j = tmpInt.value;
						nOfst = i + 1;
						prjBuff[ofst + i + j - 1] = 0;
						i += j;
						if (prjBuff[ofst + i] != ',')
						{
							return null;
						}
						vOfst = i + 1;
						i++;
						while (true)
						{
							c = prjBuff[ofst + i];
							if (c == 0 || c == ',')
							{
								return null;
							}
							else if (c == ']')
							{
								prjBuff[ofst + i] = 0;
								i++;
								if (StringUtil.equalsICase(prjBuff, ofst + nOfst, "False_Easting"))
								{
									falseEasting = StringUtil.toDouble(prjBuff, ofst + vOfst);
								}
								else if (StringUtil.equalsICase(prjBuff, ofst + nOfst, "False_Northing"))
								{
									falseNorthing = StringUtil.toDouble(prjBuff, ofst + vOfst);
								}
								else if (StringUtil.equalsICase(prjBuff, ofst + nOfst, "Central_Meridian"))
								{
									centralMeridian = StringUtil.toDouble(prjBuff, ofst + vOfst);
								}
								else if (StringUtil.equalsICase(prjBuff, ofst + nOfst, "Scale_Factor"))
								{
									scaleFactor = StringUtil.toDouble(prjBuff, ofst + vOfst);
								}
								else if (StringUtil.equalsICase(prjBuff, ofst + nOfst, "Latitude_Of_Origin"))
								{
									latitudeOfOrigin = StringUtil.toDouble(prjBuff, ofst + vOfst);
								}
								else
								{
									return null;
								}
								break;
							}
							else
							{
								i++;
							}
						}
					}
					else if (StringUtil.startsWith(prjBuff, ofst + i, "UNIT["))
					{
						i += 5;
						if (!parsePRJString(prjBuff, ofst + i, tmpInt))
						{
							return null;
						}
						j = tmpInt.value;
						i += j;
						commaFound = false;
						while (true)
						{
							c = prjBuff[ofst + i];
							if (c == ',')
							{
								i++;
								if (commaFound)
								{
									return null;
								}
								commaFound = true;
							}
							else if (c == ']')
							{
								i++;
								if (!commaFound)
								{
									return null;
								}
								break;
							}
							else if (c == 0)
							{
								return null;
							}
							else
							{
								i++;
							}
						}
					}
					else
					{
						return null;
					}
				}
				else
				{
					return null;
				}
			}
			if (cst == CoordinateSystemType.Geographic || falseEasting == -1 || falseNorthing == -1 || centralMeridian == -1 || scaleFactor == -1 || latitudeOfOrigin == -1 || gcs == null)
			{
				return null;
			}
			if (parsedSize != null)
			{
				parsedSize.value = i;
			}
			if (cst == CoordinateSystemType.MercatorProjected || cst == CoordinateSystemType.GausskrugerProjected)
			{
				return new MercatorProjectedCoordinateSystem(sourceName, srid, StringUtil.byte2String(prjBuff, ofst + nameOfst), falseEasting, falseNorthing, centralMeridian, latitudeOfOrigin, scaleFactor, gcs, unit);
			}
			else if (cst == CoordinateSystemType.Mercator1SPProjected)
			{
				return new Mercator1SPProjectedCoordinateSystem(sourceName, srid, StringUtil.byte2String(prjBuff, ofst + nameOfst), falseEasting, falseNorthing, centralMeridian, latitudeOfOrigin, scaleFactor, gcs, unit);
			}
			else
			{
				return null;
			}
		}
	
		return null;
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

	private static boolean parsePRJString(byte []prjBuff, int ofst, SharedInt strSize)
	{
		int i;
		byte c;
		if (prjBuff[ofst] != '\"')
			return false;
		i = 1;
		while (ofst + i < prjBuff.length)
		{
			c = prjBuff[ofst + i];
			if (c == 0)
				return false;
			if (c == '\"')
			{
				i++;
				strSize.value = i;
				return true;
			}
			i++;
		}
		return false;
	}
}
