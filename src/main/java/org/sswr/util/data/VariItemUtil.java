package org.sswr.util.data;

import java.sql.Timestamp;
import java.time.ZonedDateTime;

import org.locationtech.jts.geom.Geometry;
import org.sswr.util.math.geometry.Vector2D;

import jakarta.annotation.Nullable;

public class VariItemUtil {
	public static double asF64(@Nullable Object v)
	{
		if (v == null)
			return Double.NaN;
		if (v instanceof String)
		{
			return StringUtil.toDoubleS((String)v, Double.NaN);
		}
		System.out.println("VariItemUtil.asF64: Unknown type: "+v.getClass().toString());
		return Double.NaN;
	}

	public static @Nullable Timestamp asTimestamp(@Nullable Object v)
	{
		if (v == null)
			return null;
		if (v instanceof ZonedDateTime)
		{
			return DateTimeUtil.toTimestamp((ZonedDateTime)v);
		}
		else if (v instanceof String)
		{
			return DateTimeUtil.toTimestamp(DateTimeUtil.parse((String)v));
		}
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

	@Nullable
	public static byte[] asByteArr(@Nullable Object v)
	{
		if (v == null)
			return null;
		if (v instanceof byte[])
		{
			return (byte[])v;
		}
		System.out.println("VariItemUtil.asByteArr: Unknown type: "+v.getClass().toString());
		return null;
	}

	@Nullable
	public static Vector2D asVector(@Nullable Object v)
	{
		if (v == null)
			return null;
		if (v instanceof Vector2D)
		{
			return (Vector2D)v;
		}
		if (v instanceof Geometry)
		{
			return GeometryUtil.toVector2D((Geometry)v);
		}
		System.out.println("VariItemUtil.asVector: Unknown type: "+v.getClass().toString());
		return null;
	}

	@Nullable
	public static Geometry asGeometry(@Nullable Object v)
	{
		if (v == null)
			return null;
		if (v instanceof Vector2D)
		{
			return GeometryUtil.fromVector2D((Vector2D)v);
		}
		if (v instanceof Geometry)
		{
			return (Geometry)v;
		}
		System.out.println("VariItemUtil.asVector: Unknown type: "+v.getClass().toString());
		return null;
	}
}
