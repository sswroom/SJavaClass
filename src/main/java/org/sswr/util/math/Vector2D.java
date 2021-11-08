package org.sswr.util.math;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
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

	public Geometry toGeometry()
	{
		GeometryFactory factory = new GeometryFactory(new PrecisionModel(), this.srid);
		if (this.getVectorType() == VectorType.Point)
		{
			Point2D pt = (Point2D)this;
			return factory.createPoint(new Coordinate(pt.x, pt.y));
		}
		else if (this.getVectorType() == VectorType.Polyline)
		{
			Polyline pl = (Polyline)this;
			if (pl.ptOfstArr.length != 1)
			{
				return null;
			}
			Coordinate[] coordinates = new Coordinate[pl.pointArr.length >> 1];
			if (pl.support3D())
			{
				Polyline3D pl2 = (Polyline3D)pl;
				double[] attList = pl2.getAltitudeList();
				int i = 0;
				int j = pl2.pointArr.length >> 1;
				while (i < j)
				{
					coordinates[i] = new Coordinate(pl2.pointArr[i << 1], pl2.pointArr[(i << 1) + 1], attList[i]);
					i++;
				}
			}
			else
			{
				int i = 0;
				int j = pl.pointArr.length >> 1;
				while (i < j)
				{
					coordinates[i] = new Coordinate(pl.pointArr[i << 1], pl.pointArr[(i << 1) + 1]);
					i++;
				}
			}
			return factory.createLineString(coordinates);
		}
		else if (this.getVectorType() == VectorType.Polygon)
		{
			Polygon pg = (Polygon)this;
			if (pg.ptOfstArr.length != 1)
			{
				return null;
			}
			Coordinate[] coordinates = new Coordinate[pg.pointArr.length >> 1];
			int i = 0;
			int j = pg.pointArr.length >> 1;
			while (i < j)
			{
				coordinates[i] = new Coordinate(pg.pointArr[i << 1], pg.pointArr[(i << 1) + 1]);
				i++;
			}
			return factory.createLinearRing(coordinates);
		}
		return null;
	}
}
