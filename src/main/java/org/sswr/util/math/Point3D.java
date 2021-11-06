package org.sswr.util.math;

import org.sswr.util.data.SharedDouble;

public class Point3D extends Point2D
{
	private double z;
	
	public Point3D(int srid, double x, double y, double z)
	{
		super(srid, x, y);
		this.z = z;
	}

	public Vector2D clone()
	{
		return new Point3D(this.srid, this.x, this.y, this.z);
	}

	public void getCenter3D(SharedDouble x, SharedDouble y, SharedDouble z)
	{
		x.value = this.x;
		y.value = this.y;
		z.value = this.z;
	}

	public boolean support3D()
	{
		return true;
	}

	public void convCSys(CoordinateSystem srcCSys, CoordinateSystem destCSys)
	{
		SharedDouble tmpX = new SharedDouble();
		SharedDouble tmpY = new SharedDouble();
		SharedDouble tmpZ = new SharedDouble();
		CoordinateSystem.convertXYZ(srcCSys, destCSys, this.x, this.y, this.z, tmpX, tmpY, tmpZ);
		this.x = tmpX.value;
		this.y = tmpY.value;
		this.z = tmpZ.value;
	}
}
