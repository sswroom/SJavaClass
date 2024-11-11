package org.sswr.util.media;

import org.sswr.util.basic.Point;

import jakarta.annotation.Nonnull;

public final class ColorCoordinate
{
	private double X;
	private double Y;
	private double Z;

	public ColorCoordinate(double X, double Y, double Z)
	{
		this.setFromXYZ(X, Y, Z);
	}

	public void setFromxyY(double x, double y, double Y)
	{
		this.Y = Y;
		this.X = Y * x / y;
		this.Z = Y * (1 - x - y) / y;
	}

	public void setFromXYZ(double X, double Y, double Z)
	{
		this.X = X;
		this.Y = Y;
		this.Z = Z;
	}

	@Nonnull
	public Point getxy()
	{
		double sum = this.X + this.Y + this.Z;
		return new Point(this.X / sum, this.Y / sum);
	}
}