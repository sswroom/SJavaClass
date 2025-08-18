package org.sswr.util.math.chart;

import org.sswr.util.math.Coord2DDbl;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public abstract class Axis
{
	private @Nullable String name;
	private double labelRotate;
	public Axis()
	{
		this.name = null;
		this.labelRotate = 0;
	}

	public abstract @Nonnull DataType getType();
	public abstract void calcX(@Nonnull ChartData data, @Nonnull Coord2DDbl[] pos, double minX, double maxX);
	public abstract void calcY(@Nonnull ChartData data, @Nonnull Coord2DDbl[] pos, double minY, double maxY);
	public void setName(@Nullable String name)
	{
		this.name = name;
	}

	public @Nullable String getName()
	{
		return this.name;
	}

	public void setLabelRotate(double labelRotate)
	{
		this.labelRotate = labelRotate;
	}

	public double getLabelRotate()
	{
		return this.labelRotate;
	}
}
