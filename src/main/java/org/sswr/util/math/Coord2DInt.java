package org.sswr.util.math;

import jakarta.annotation.Nonnull;

public class Coord2DInt {
	public int x;
	public int y;

	public Coord2DInt(int x, int y)
	{
		this.x = x;
		this.y = y;
	}

	public double calcLengTo(Coord2DInt other)
	{
		int dx = this.x - other.x;
		int dy = this.y - other.y;
		return Math.sqrt(dx * dx + dy * dy);
	}

	public @Nonnull Coord2DInt swapXY()
	{
		return new Coord2DInt(this.y, this.x);
	}

	public @Nonnull Coord2DInt abs()
	{
		return new Coord2DInt(Math.abs(this.x), Math.abs(this.y));
	}

	public @Nonnull Coord2DInt add(@Nonnull Coord2DInt other)
	{
		return new Coord2DInt(this.x + other.x, this.y + other.y);
	}

	public @Nonnull Coord2DInt add(int other)
	{
		return new Coord2DInt(this.x + other, this.y + other);
	}

	public @Nonnull Coord2DInt sub(@Nonnull Coord2DInt other)
	{
		return new Coord2DInt(this.x - other.x, this.y - other.y);
	}

	public @Nonnull Coord2DInt sub(int other)
	{
		return new Coord2DInt(this.x - other, this.y - other);
	}

	public @Nonnull Coord2DInt mul(@Nonnull Coord2DInt other)
	{
		return new Coord2DInt(this.x * other.x, this.y * other.y);
	}

	public @Nonnull Coord2DInt mul(int other)
	{
		return new Coord2DInt(this.x * other, this.y * other);
	}

	public @Nonnull Coord2DInt div(@Nonnull Coord2DInt other)
	{
		return new Coord2DInt(this.x / other.x, this.y / other.y);
	}

	public @Nonnull Coord2DInt div(int other)
	{
		return new Coord2DInt(this.x / other, this.y / other);
	}

	public boolean equals(@Nonnull Coord2DInt other)
	{
		return this.x == other.x && this.y == other.y;
	}

	public boolean notEquals(@Nonnull Coord2DInt other)
	{
		return this.x != other.x || this.y != other.y;
	}

	public @Nonnull Coord2DDbl toDouble()
	{
		return new Coord2DDbl((double)this.x, (double)this.y);
	}
}
