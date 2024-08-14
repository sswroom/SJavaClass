package org.sswr.util.basic;

import org.sswr.util.math.Coord2DDbl;

public class Vector3
{
	public double val[];
	
	public Vector3()
	{
		this.val = new double[3];
	}

	public Vector3(double x, double y, double z)
	{
		this.val = new double[3];
		this.val[0] = x;
		this.val[1] = y;
		this.val[2] = z;
	}

	public Vector3(Coord2DDbl xy, double z)
	{
		this.val = new double[3];
		this.val[0] = xy.x;
		this.val[1] = xy.y;
		this.val[2] = z;
	}

	public double getLat()
	{
		return this.val[1];
	}

	public double getLon()
	{
		return this.val[0];
	}

	public double getH()
	{
		return this.val[2];
	}

	public double getX()
	{
		return this.val[0];
	}

	public double getY()
	{
		return this.val[1];
	}

	public double getZ()
	{
		return this.val[2];
	}

	public Coord2DDbl toCoord2D()
	{
		return new Coord2DDbl(this.val[0], this.val[1]);
	}

	public Coord2DDbl getXY()
	{
		return new Coord2DDbl(this.val[0], this.val[1]);
	}
	
	public double multiply(Vector3 val)
	{
		return this.val[0] * val.val[0] + this.val[1] * val.val[1] + this.val[2] * val.val[2];
	}

	public Vector3 clone()
	{
		return new Vector3(this.val[0], this.val[1], this.val[2]);
	}

	public void set(double val1, double val2, double val3)
	{
		this.val[0] = val1;
		this.val[1] = val2;
		this.val[2] = val3;
	}

	public void set(Vector3 val)
	{
		this.val[0] = val.val[0];
		this.val[1] = val.val[1];
		this.val[2] = val.val[2];
	}
}
