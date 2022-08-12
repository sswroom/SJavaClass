package org.sswr.util.math.geometry;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.sswr.util.math.Coord2DDbl;
import org.sswr.util.math.CoordinateSystem;
import org.sswr.util.math.RectAreaDbl;

public abstract class Vector2D
{
	public enum VectorType
	{
		Unknown,
		Point,
		LineString,
		Polygon,
		MultiPoint,
		Polyline, //MultiLineString
		MultiPolygon,
		GeometryCollection,
		CircularString,
		CompoundCurve,
		CurvePolygon,
		MultiCurve,
		MultiSurface,
		Curve,
		Surface,
		PolyhedralSurface,
		Tin,
		Triangle,

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
	public abstract Coord2DDbl getCenter();
	public abstract Vector2D clone();
	public abstract void getBounds(RectAreaDbl bounds);
	public abstract double calSqrDistance(Coord2DDbl pt, Coord2DDbl nearPt);
	public abstract boolean joinVector(Vector2D vec);

	public boolean hasZ()
	{
		return false;
	}

	public boolean hasM()
	{
		return false;
	}

	public abstract void convCSys(CoordinateSystem srcCSys, CoordinateSystem destCSys);
	public abstract boolean equals(Object vec);

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
			Coord2DDbl center = pt.getCenter();
			return factory.createPoint(new Coordinate(center.x, center.y));
		}
		else if (this.getVectorType() == VectorType.Polyline)
		{
			Polyline pl = (Polyline)this;
			if (pl.ptOfstArr.length != 1)
			{
				return null;
			}
			Coordinate[] coordinates = new Coordinate[pl.pointArr.length];
			if (pl.hasZ())
			{
				double[] attList = pl.getZList();
				int i = 0;
				int j = pl.pointArr.length;
				while (i < j)
				{
					coordinates[i] = new Coordinate(pl.pointArr[i].x, pl.pointArr[i].y, attList[i]);
					i++;
				}
			}
			else
			{
				int i = 0;
				int j = pl.pointArr.length;
				while (i < j)
				{
					coordinates[i] = new Coordinate(pl.pointArr[i].x, pl.pointArr[i].y);
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
			Coordinate[] coordinates = new Coordinate[pg.pointArr.length];
			int i = 0;
			int j = pg.pointArr.length;
			while (i < j)
			{
				coordinates[i] = new Coordinate(pg.pointArr[i].x, pg.pointArr[i].y);
				i++;
			}
			return factory.createLinearRing(coordinates);
		}
		return null;
	}
}
