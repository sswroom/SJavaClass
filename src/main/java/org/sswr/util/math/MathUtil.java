package org.sswr.util.math;

public class MathUtil
{
	public static int double2Int32(double val)
	{
		return ((val) < 0)?(int)(val - 0.5):(int)(val + 0.5);
	}

	public static long double2Int64(double val)
	{
		return ((val) < 0)?(long)(val - 0.5):(long)(val + 0.5);
	}

	public static boolean nearlyEquals(double val1, double val2, double diffRatio)
	{
		if (Double.isInfinite(val1))
		{
			return Double.isInfinite(val2);
		}
		if (Double.isNaN(val1))
		{
			return Double.isNaN(val2);
		}
		Double aval1 = Math.abs(val1);
		Double aval2 = Math.abs(val2);
		Double diffV;
		if (aval1 > aval2)
			diffV = aval1 * diffRatio;
		else
			diffV = aval2 * diffRatio;
		Double diff = val1 - val2;
		return diff >= -diffV && diff <= diffV;
	}

	public static boolean nearlyEqualsDbl(double val1, double val2)
	{
		return nearlyEquals(val1, val2, 0.00000000001);
	}	
}
