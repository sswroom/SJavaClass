package org.sswr.util.math;

public class Coord2DDbl
{
	public double x;
	public double y;

	public Coord2DDbl()
	{
		this.x = 0;
		this.y = 0;
	}

	public Coord2DDbl(double x, double y)
	{
		this.x = x;
		this.y = y;
	}

	public static Coord2DDbl fromLatLon(double lat, double lon)
	{
		return new Coord2DDbl(lon, lat);
	}

	public double calcLengTo(Coord2DDbl coord)
	{
		double diffX = this.x - coord.x;
		double diffY = this.y - coord.y;
		diffX *= diffX;
		diffY *= diffY;
		return Math.sqrt(diffX + diffY);
	}

	public Coord2DDbl clone()
	{
		return new Coord2DDbl(this.x, this.y);
	}

	public void set(Coord2DDbl coord)
	{
		this.x = coord.x;
		this.y = coord.y;
	}
	
	public Coord2DDbl setMin(Coord2DDbl coord)
	{
		if (coord.x < this.x)
			this.x = coord.x;
		if (coord.y < this.y)
			this.y = coord.y;
		return this;
	}

	public Coord2DDbl setMax(Coord2DDbl coord)
	{
		if (coord.x > this.x)
			this.x = coord.x;
		if (coord.y > this.y)
			this.y = coord.y;
		return this;
	}

	public Coord2DDbl add(double val)
	{
		return new Coord2DDbl(this.x + val, this.y + val);
	}

	public Coord2DDbl add(Coord2DDbl val)
	{
		return new Coord2DDbl(this.x + val.x, this.y + val.y);
	}

	public Coord2DDbl subtract(double val)
	{
		return new Coord2DDbl(this.x - val, this.y - val);
	}

	public Coord2DDbl subtract(Coord2DDbl val)
	{
		return new Coord2DDbl(this.x - val.x, this.y - val.y);
	}

	public Coord2DDbl multiply(double val)
	{
		return new Coord2DDbl(this.x * val, this.y * val);
	}

	public Coord2DDbl multiply(Coord2DDbl val)
	{
		return new Coord2DDbl(this.x * val.x, this.y * val.y);
	}

	public boolean equals(Coord2DDbl coord)
	{
		return (this.x == coord.x) && (this.y == coord.y);
	}

	public boolean equalsNearly(Coord2DDbl coord)
	{
		return MathUtil.nearlyEqualsDbl(this.x, coord.x) && MathUtil.nearlyEqualsDbl(this.y, coord.y);
	}

	public boolean isZero()
	{
		return this.x == 0 && this.y == 0;
	}
}
