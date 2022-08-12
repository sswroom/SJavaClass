package org.sswr.util.data;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKTWriter;

public class GeometryUtil
{
	public static String toWKT(Geometry geometry)
	{
		WKTWriter writer = new WKTWriter();
		return writer.write(geometry);
/*		String type = geometry.getGeometryType();
		if (type.equals("LinearRing"))
		{
			StringBuilder sb = new StringBuilder();
			Coordinate coords[] = geometry.getCoordinates();
			int i = 0;
			int j = coords.length;
			sb.append("POLYGON ((");
			while (i < j)
			{
				if (i > 0)
				{
					sb.append(",");
				}
				sb.append(coords[i].getX()+" "+coords[i].getY());
				i++;
			}
			sb.append("))");
			return sb.toString();
		}
		else if (type.equals("Point"))
		{
			StringBuilder sb = new StringBuilder();
			Coordinate coord = geometry.getCoordinate();
			sb.append("POINT (");
			sb.append(coord.getX()+" "+coord.getY());
			sb.append(")");
			return sb.toString();
		}
		return "Unknwon Geometry Type: "+type;*/
	}
}
