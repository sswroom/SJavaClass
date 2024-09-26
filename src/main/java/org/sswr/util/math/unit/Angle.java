package org.sswr.util.math.unit;

import jakarta.annotation.Nonnull;

public class Angle
{
	public enum AngleUnit
	{
		Radian,
		Gradian,
		Turn,
		Degree,
		Arcminute,
		Arcsecond,
		Milliarcsecond,
		Microarcsecond
	};

	public static double getUnitRatio(@Nonnull AngleUnit unit)
	{
		switch (unit)
		{
		case Radian:
			return 1;
		case Gradian:
			return Math.PI / 200.0;
		case Turn:
			return Math.PI * 2.0;
		case Degree:
			return Math.PI / 180.0;
		case Arcminute:
			return Math.PI / 10800.0;
		case Arcsecond:
			return Math.PI / 648000.0;
		case Milliarcsecond:
			return Math.PI / 648000000.0;
		case Microarcsecond:
			return Math.PI / 648000000000.0;
		}
		return 1;
	}

	@Nonnull
	public static String getUnitShortName(@Nonnull AngleUnit unit)
	{
		switch (unit)
		{
		case Radian:
			return "rad";
		case Gradian:
			return "grad";
		case Turn:
			return "";
		case Degree:
			return "°";
		case Arcminute:
			return "′";
		case Arcsecond:
			return "″";
		case Milliarcsecond:
			return "mas";
		case Microarcsecond:
			return "μas";
		}
		return "";
	}

	@Nonnull
	public static String getUnitName(@Nonnull AngleUnit unit)
	{
		switch (unit)
		{
		case Radian:
			return "Radian";
		case Gradian:
			return "Gradian";
		case Turn:
			return "Turns";
		case Degree:
			return "Degree";
		case Arcminute:
			return "Arcminute";
		case Arcsecond:
			return "Arcsecond";
		case Milliarcsecond:
			return "Milliarcsecond";
		case Microarcsecond:
			return "Microarcsecond";
		}
		return "";
	}

	public static double convert(@Nonnull AngleUnit fromUnit, @Nonnull AngleUnit toUnit, double fromValue)
	{
		return fromValue * getUnitRatio(fromUnit) / getUnitRatio(toUnit);
	}
}
