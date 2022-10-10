package org.sswr.util.data;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.io.WKTWriter;
import org.sswr.util.math.CoordinateSystem;
import org.sswr.util.math.CoordinateSystemManager;
import org.sswr.util.math.unit.Distance.DistanceUnit;

public class GeometryUtil
{
	public static String toWKT(Geometry geometry)
	{
		WKTWriter writer = new WKTWriter();
		return writer.write(geometry);
	}

	public static double calcMaxDistanceFromCenter(Geometry geometry, DistanceUnit unit)
	{
		return calcMaxDistanceFromPoint(geometry.getCentroid(), geometry, unit);
	}

	public static double calcMaxDistanceFromPoint(Point point, Geometry geometry, DistanceUnit unit)
	{
		Coordinate[] coordinates = geometry.getCoordinates();
		Coordinate ccoord = point.getCoordinate();
		int srid = geometry.getSRID();
		double maxDist = -1;
		Coordinate maxCoord = ccoord;
		double thisDist;
		int i = 0;
		int j = coordinates.length;
		while (i < j)
		{
			thisDist = coordinates[i].distance(ccoord);
			if (thisDist > maxDist)
			{
				maxDist = thisDist;
				maxCoord = coordinates[i];
			}
			i++;
		}
		CoordinateSystem csys = CoordinateSystemManager.srCreateCSys(srid);
		if (csys == null)
		{
			return maxDist;
		}
		else
		{
			return csys.calSurfaceDistanceXY(ccoord.x, ccoord.y, maxCoord.x, maxCoord.y, unit);
		}
	}
}
