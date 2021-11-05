package org.sswr.util.math;

import java.io.FileInputStream;
import java.io.IOException;

import org.sswr.util.data.SharedInt;
import org.sswr.util.data.StringUtil;
import org.sswr.util.data.TimestampValidator;
import org.sswr.util.math.CoordinateSystem.PrimemType;
import org.sswr.util.math.CoordinateSystem.UnitType;

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
			prjBuff[i + j - 1] = 0;
			i += j;
			while (true)
			{
				c = prjBuff[i];
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
						prjBuff[i + j - 1] = 0;
						i += j;
						while (true)
						{
							c = prjBuff[i];
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
									prjBuff[i + j - 1] = 0;
									i += j;
									j = -1;
									spIndex = 1;
									while (true)
									{
										c = prjBuff[i];
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
							c = prjBuff[i];
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
							c = prjBuff[i];
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
				const Math::CoordinateSystemManager::DatumInfo *datum = GetDatumInfoByName((const UTF8Char*)&prjBuff[datumOfst]);
	
				DatumData data;
				FillDatumData(&data, datum, &prjBuff[datumOfst], &ellipsoid, 0);
				csys = new GeographicCoordinateSystem(sourceName, srid, (const UTF8Char*)&prjBuff[nameOfst], &data, primem, unit);
				return csys;
			}
			else
			{
				EarthEllipsoid ellipsoid = new EarthEllipsoid(a, f_1, eet);
				const Math::CoordinateSystemManager::DatumInfo *datum = GetDatumInfoByName((const UTF8Char*)&prjBuff[datumOfst]);
				DatumData1 data;
				FillDatumData(&data, datum, &prjBuff[datumOfst], &ellipsoid, 0);
				csys = new GeographicCoordinateSystem(sourceName, srid, (const UTF8Char*)&prjBuff[nameOfst], data, primem, unit);
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
				return 0;
			j = tmpInt.value;
			nameOfst = i + 1;
			prjBuff[i + j - 1] = 0;
			i += j;
			while (true)
			{
				c = prjBuff[i];
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
						if (gcs == 0)
							return null;
						j = tmpInt.value;
						i += j;
					}
					else if (StringUtil.startsWith(prjBuff, ofst + i, "PROJECTION["))
					{
						if (StringUtil.startsWith(prjBuff, ofst + i + 11, "\"Transverse_Mercator\"]"))
						{
							i += 33;
							cst = Math::CoordinateSystem::CST_MERCATORPROJECTED;
						}
						else if (StringUtil.startsWith(prjBuff, ofst + i + 11, "\"Mercator_1SP\"]"))
						{
							i += 26;
							cst = Math::CoordinateSystem::CST_MERCATOR1SPPROJECTED;
						}
						else if (StringUtil.startsWith(prjBuff, ofst + i + 11, "\"Gauss_Kruger\"]"))
						{
							i += 26;
							cst = Math::CoordinateSystem::CST_GAUSSKRUGERPROJECTED;
						}
						else
						{
							SDEL_CLASS(gcs);
							return null;
						}
					}
					else if (StringUtil.startsWith(prjBuff, ofst + i, "PARAMETER["))
					{
						i += 10;
						if (!parsePRJString(prjBuff, ofst + i, tmpInt))
						{
							return 0;
						}
						j = tmpInt.value;
						nOfst = i + 1;
						prjBuff[i + j - 1] = 0;
						i += j;
						if (prjBuff[i] != ',')
						{
							return null;
						}
						vOfst = i + 1;
						i++;
						while (true)
						{
							c = prjBuff[i];
							if (c == 0 || c == ',')
							{
								return null;
							}
							else if (c == ']')
							{
								prjBuff[i] = 0;
								i++;
								if (Text::StrEqualsICase(&prjBuff[nOfst], "False_Easting"))
								{
									falseEasting = Text::StrToDouble(&prjBuff[vOfst]);
								}
								else if (Text::StrEqualsICase(&prjBuff[nOfst], "False_Northing"))
								{
									falseNorthing = Text::StrToDouble(&prjBuff[vOfst]);
								}
								else if (Text::StrEqualsICase(&prjBuff[nOfst], "Central_Meridian"))
								{
									centralMeridian = Text::StrToDouble(&prjBuff[vOfst]);
								}
								else if (Text::StrEqualsICase(&prjBuff[nOfst], "Scale_Factor"))
								{
									scaleFactor = Text::StrToDouble(&prjBuff[vOfst]);
								}
								else if (Text::StrEqualsICase(&prjBuff[nOfst], "Latitude_Of_Origin"))
								{
									latitudeOfOrigin = Text::StrToDouble(&prjBuff[vOfst]);
								}
								else
								{
									SDEL_CLASS(gcs);
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
					else if (Text::StrStartsWith(&prjBuff[i], "UNIT["))
					{
						i += 5;
						if (!ParsePRJString(&prjBuff[i], &j))
						{
							SDEL_CLASS(gcs);
							return null;
						}
						i += j;
						commaFound = false;
						while (true)
						{
							c = prjBuff[i];
							if (c == ',')
							{
								i++;
								if (commaFound)
								{
									SDEL_CLASS(gcs);
									return null;
								}
								commaFound = true;
							}
							else if (c == ']')
							{
								i++;
								if (!commaFound)
								{
									SDEL_CLASS(gcs);
									return null;
								}
								break;
							}
							else if (c == 0)
							{
								SDEL_CLASS(gcs);
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
						SDEL_CLASS(gcs);
						return null;
					}
				}
				else
				{
					SDEL_CLASS(gcs);
					return null;
				}
			}
			if (cst == Math::CoordinateSystem::CST_GEOGRAPHIC || falseEasting == -1 || falseNorthing == -1 || centralMeridian == -1 || scaleFactor == -1 || latitudeOfOrigin == -1 || gcs == 0)
			{
				SDEL_CLASS(gcs);
				return null;
			}
			if (parsedSize != null)
			{
				parsedSize.value = i;
			}
			if (cst == Math::CoordinateSystem::CST_MERCATORPROJECTED || cst == Math::CoordinateSystem::CST_GAUSSKRUGERPROJECTED)
			{
				NEW_CLASS(csys, Math::MercatorProjectedCoordinateSystem(sourceName, srid, (const UTF8Char*)&prjBuff[nameOfst], falseEasting, falseNorthing, centralMeridian, latitudeOfOrigin, scaleFactor, gcs, unit));
				return csys;
			}
			else if (cst == Math::CoordinateSystem::CST_MERCATOR1SPPROJECTED)
			{
				NEW_CLASS(csys, Math::Mercator1SPProjectedCoordinateSystem(sourceName, srid, (const UTF8Char*)&prjBuff[nameOfst], falseEasting, falseNorthing, centralMeridian, latitudeOfOrigin, scaleFactor, gcs, unit));
				return csys;
			}
			else
			{
				return null;
			}
		}
	
		return null;
	}
}
