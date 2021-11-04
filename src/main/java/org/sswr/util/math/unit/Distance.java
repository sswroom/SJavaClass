package org.sswr.util.math.unit;

public class Distance
{
	public enum DistanceUnit
	{
		Meter,
		Centimeter,
		Millimeter,
		Micrometer,
		Nanometer,
		Picometer,
		Kilometer,
		Inch,
		Foot,
		Yard,
		Mile,
		NauticalMile,
		AU,
		Lightsecond,
		Lightminute,
		Lighthour,
		Lightday,
		Lightweek,
		Lightyear,
		Emu,
		Point,
		Pixel,
	}

	public static double getUnitRatio(DistanceUnit unit)
	{
		switch (unit)
		{
		case Meter:
			return 1.0;
		case Centimeter:
			return 0.01;
		case Millimeter:
			return 0.001;
		case Micrometer:
			return 0.000001;
		case Nanometer:
			return 0.000000001;
		case Picometer:
			return 0.000000000001;
		case Kilometer:
			return 1000.0;
		case Inch:
			return 0.0254;
		case Foot:
			return 0.0254 * 12.0;
		case Yard:
			return 0.0254 * 36.0;
		case Mile:
			return 0.0254 * 12.0 * 5280;
		case NauticalMile:
			return 1852.0;
		case AU:
			return 149597870700.0;
		case Lightsecond:
			return 299792458.0;
		case Lightminute:
			return 17987547480.0;
		case Lighthour:
			return 299792458.0 * 3600.0;
		case Lightday:
			return 299792458.0 * 86400.0;
		case Lightweek:
			return 299792458.0 * 604800.0;
		case Lightyear:
			return 299792458.0 * 31557600.0;
		case Emu:
			return 1 / 36000000.0;
		case Point:
			return 0.0254 / 72.0;
		case Pixel:
			return 0.0254 / 96.0;
		}
		return 1;
	}

	public static String getUnitShortName(DistanceUnit unit)
	{
		switch (unit)
		{
		case Meter:
			return "m";
		case Centimeter:
			return "cm";
		case Millimeter:
			return "mm";
		case Micrometer:
			return "μm";
		case Nanometer:
			return "nm";
		case Picometer:
			return "pm";
		case Kilometer:
			return "km";
		case Inch:
			return "\"";
		case Foot:
			return "ft";
		case Yard:
			return "yd";
		case Mile:
			return "milw";
		case NauticalMile:
			return "nm";
		case AU:
			return "AU";
		case Lightsecond:
			return "ls";
		case Lightminute:
			return "lm";
		case Lighthour:
			return "lh";
		case Lightday:
			return "ld";
		case Lightweek:
			return "lw";
		case Lightyear:
			return "ly";
		case Emu:
			return "emu";
		case Point:
			return "pt";
		case Pixel:
			return "px";
		}
		return "";
	}

	public static String getUnitName(DistanceUnit unit)
	{
		switch (unit)
		{
		case Meter:
			return "Meter";
		case Centimeter:
			return "Centimeter";
		case Millimeter:
			return "Millimeter";
		case Micrometer:
			return "Micrometer";
		case Nanometer:
			return "Nanometer";
		case Picometer:
			return "Picometer";
		case Kilometer:
			return "Kilometer";
		case Inch:
			return "Inch";
		case Foot:
			return "Foot";
		case Yard:
			return "Yard";
		case Mile:
			return "Mile";
		case NauticalMile:
			return "Nautical Mile";
		case AU:
			return "Astronomical unit";
		case Lightsecond:
			return "Light-second";
		case Lightminute:
			return "Light-minute";
		case Lighthour:
			return "Light-hour";
		case Lightday:
			return "Light-day";
		case Lightweek:
			return "Light-week";
		case Lightyear:
			return "Light-year";
		case Emu:
			return "English Metric Unit";
		case Point:
			return "Point";
		case Pixel:
			return "Pixel";
		}
		return "";
	}

	public static double convert(DistanceUnit fromUnit, DistanceUnit toUnit, double fromValue)
	{
		return fromValue * getUnitRatio(fromUnit) / getUnitRatio(toUnit);
	}
}
