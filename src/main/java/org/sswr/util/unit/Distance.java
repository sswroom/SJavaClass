package org.sswr.util.unit;

public class Distance {
	public enum DistanceUnit
	{
		DU_INCH,
		DU_CENTIMETER,
		DU_EMU,
		DU_POINT,
		DU_PIXEL
	};
	
	public static double getUnitRatio(DistanceUnit unit)
	{
		switch (unit)
		{
		case DU_INCH:
			return 0.0254;
		case DU_CENTIMETER:
			return 0.01;		
		case DU_EMU:
			return 1 / 36000000.0;
		case DU_POINT:
			return 0.0254 / 72.0;
		case DU_PIXEL:
			return 0.0254 / 96.0;
		default:
			return 1.0;
		}
	}

	public static double convert(DistanceUnit fromUnit, DistanceUnit toUnit, double fromValue)
	{
		return fromValue * getUnitRatio(fromUnit) / getUnitRatio(toUnit);
	}
}
