package org.sswr.util.data;

import java.sql.Timestamp;

import jakarta.annotation.Nullable;

public class VariItemUtil {
	public static double asF64(@Nullable Object v)
	{
		if (v == null)
			return Double.NaN;
		System.out.println("VariItemUtil.asF64: Unknown type: "+v.getClass().toString());
		return Double.NaN;
	}

	public static @Nullable Timestamp asTimestamp(@Nullable Object v)
	{
		if (v == null)
			return null;
		System.out.println("VariItemUtil.asTimestamp: Unknown type: "+v.getClass().toString());
		return null;
	}

	public static int asI32(@Nullable Object v)
	{
		if (v == null)
			return 0;
		System.out.println("VariItemUtil.asI32: Unknown type: "+v.getClass().toString());
		return 0;
	}

	public static long asI64(@Nullable Object v)
	{
		if (v == null)
			return 0;
		System.out.println("VariItemUtil.asI64: Unknown type: "+v.getClass().toString());
		return 0;
	}

	public static boolean asBool(@Nullable Object v)
	{
		if (v == null)
			return false;
		if (v instanceof Boolean)
		{
			return ((Boolean)v).booleanValue();
		}
		System.out.println("VariItemUtil.asBool: Unknown type: "+v.getClass().toString());
		return false;
	}

	public static String asStr(@Nullable Object v)
	{
		if (v == null)
			return null;
		return v.toString();
	}
}
