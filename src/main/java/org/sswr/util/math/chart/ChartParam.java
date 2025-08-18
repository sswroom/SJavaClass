package org.sswr.util.math.chart;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class ChartParam {
	public @Nonnull String name;
	public @Nonnull ChartData yData;
	public @Nonnull Axis yAxis;
	public @Nonnull ChartData xData;
	public int lineColor;
	public int fillColor;
	public @Nonnull ChartType chartType;
	public @Nullable String[] labels;

	public ChartParam(@Nonnull String name, @Nonnull ChartData yData, @Nonnull Axis yAxis, @Nonnull ChartData xData, int lineColor, int fillColor, @Nonnull ChartType chartType)
	{
		this.name = name;
		this.yData = yData;
		this.yAxis = yAxis;
		this.xData = xData;
		this.lineColor = lineColor;
		this.fillColor = fillColor;
		this.chartType = chartType;
		this.labels = null;
	}
}
