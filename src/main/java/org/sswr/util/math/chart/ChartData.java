package org.sswr.util.math.chart;

import jakarta.annotation.Nonnull;

public interface ChartData
{
	public @Nonnull DataType getType();
	public @Nonnull ChartData clone();
	public int getCount();
}