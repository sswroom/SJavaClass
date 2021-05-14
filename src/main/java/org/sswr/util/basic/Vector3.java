package org.sswr.util.basic;

public class Vector3
{
	public double val[];
	
	public Vector3()
	{
		this.val = new double[3];
	}

	public double multiply(Vector3 val)
	{
		return this.val[0] * val.val[0] + this.val[1] * val.val[1] + this.val[2] * val.val[2];
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
