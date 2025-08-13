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
}
