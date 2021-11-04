package org.sswr.util.math;

import org.sswr.util.data.SharedDouble;

public abstract class Vector2D
{
	public enum VectorType
	{
		Unknown,
		Point,
		Multipoint,
		Polyline,
		Polygon,
		Image,
		String,
		Ellipse,
		PieArea
	}

	protected int srid;

	public Vector2D(int srid)
	{
		this.srid = srid;
	}

	public abstract VectorType getVectorType();
	public abstract void getCenter(SharedDouble x, SharedDouble y);
	public abstract Vector2D clone();
	public abstract void getBounds(SharedDouble minX, SharedDouble minY, SharedDouble maxX, SharedDouble maxY);
	public abstract double calSqrDistance(double x, double y, SharedDouble nearPtX, SharedDouble nearPtY);
	public abstract boolean joinVector(Vector2D vec);
	public boolean support3D()
	{
		return false;
	}
	public abstract void convCSys(CoordinateSystem srcCSys, CoordinateSystem destCSys);

	public int getSRID()
	{
		return this.srid;
	}

	public void setSRID(int srid)
	{
		this.srid = srid;
	}
}
