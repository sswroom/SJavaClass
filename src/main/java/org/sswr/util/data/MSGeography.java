package org.sswr.util.data;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.sswr.util.math.Coord2DDbl;
import org.sswr.util.math.geometry.Point2D;
import org.sswr.util.math.geometry.Polygon;
import org.sswr.util.math.geometry.Vector2D;

public class MSGeography
{
	public static Geometry parseBinary(byte buff[])
	{
		if (buff == null || buff.length < 6)
		{
			return null;
		}
		int srid = ByteTool.readInt32(buff, 0);
		GeometryFactory factory = new GeometryFactory(new PrecisionModel(), srid);
		if (buff[4] == 1 || buff[4] == 2) //version 1 or 2
		{
			if (buff[5] == 0x0C) //Point 2D
			{
				if (buff.length < 22)
				{
					return null;
				}
				Point pt = factory.createPoint(new Coordinate(ByteTool.readDouble(buff, 6), ByteTool.readDouble(buff, 14)));
				return pt;
			}
			else if (buff[5] == 4) //Polygon 2D
			{
				int nPoints;
				int nFigures;
				int nShapes;
				int pointInd;
				int shapeInd;
				int ind;
				if (buff.length < 10)
				{
					return null;
				}
				nPoints = ByteTool.readInt32(buff, 6);
				pointInd = 10;
				ind = 10 + nPoints * 16;
				if (buff.length < ind + 4)
				{
					return null;
				}
				nFigures = ByteTool.readInt32(buff, ind);
				ind += 4 + nFigures * 5;
				if (buff.length < ind + 4)
				{
					return null;
				}
				nShapes = ByteTool.readInt32(buff, ind);
				shapeInd = ind + 4;
				if (buff.length < ind + 4 + nShapes * 9)
				{
					return null;
				}
				if (nShapes != 1)
				{
					return null;
				}
				if (buff[shapeInd + 8] == 3)
				{
					Coordinate coords[] = new Coordinate[nPoints];
					int i = 0;
					while (i < nPoints)
					{
						coords[i] = new Coordinate(ByteTool.readDouble(buff, pointInd + i * 16), ByteTool.readDouble(buff, pointInd + i * 16 + 8));
						i++;
					}
					return factory.createLinearRing(coords);

				}
			}
		}
		return null;
	}

	public static Vector2D parseBinaryAsVector2D(byte[] buff)
	{
		if (buff == null || buff.length < 6)
		{
			return null;
		}
		int srid = ByteTool.readInt32(buff, 0);
		if (buff[4] == 1 || buff[4] == 2) //version 1 or 2
		{
			if (buff[5] == 0x0C) //Point 2D
			{
				if (buff.length < 22)
				{
					return null;
				}
				Point2D pt = new Point2D(srid, ByteTool.readDouble(buff, 6), ByteTool.readDouble(buff, 14));
				return pt;
			}
			else if (buff[5] == 4) //Polygon 2D
			{
				int nPoints;
				int nFigures;
				int nShapes;
				int pointInd;
				int figureInd;
				int shapeInd;
				int ind;
				if (buff.length < 10)
				{
					return null;
				}
				nPoints = ByteTool.readInt32(buff, 6);
				pointInd = 10;
				ind = 10 + nPoints * 16;
				if (buff.length < ind + 4)
				{
					return null;
				}
				nFigures = ByteTool.readInt32(buff, ind);
				figureInd = ind + 4;
				ind += 4 + nFigures * 5;
				if (buff.length < ind + 4)
				{
					return null;
				}
				nShapes = ByteTool.readInt32(buff, ind);
				shapeInd = ind + 4;
				if (buff.length < ind + 4 + nShapes * 9)
				{
					return null;
				}
				if (nShapes != 1)
				{
					return null;
				}
				if (buff[shapeInd + 8] == 3)
				{
					Polygon pg = new Polygon(srid, nFigures, nPoints, false, false);
					Coord2DDbl []points = pg.getPointList();
					int i = 0;
					while (i < nPoints)
					{
						points[i].x = ByteTool.readDouble(buff, pointInd + i * 16);
						points[i].y = ByteTool.readDouble(buff, pointInd + i * 16 + 8);
						i++;
					}
					if (nFigures > 1)
					{
						int []ofstList = pg.getPtOfstList();
						int j = ofstList.length;
						i = 0;
						while (i < j)
						{
							ofstList[i] = ByteTool.readInt32(buff, figureInd + i * 5 + 1);
							i++;
						}
					}
					return pg;
				}
			}
		}
		return null;
	}
}
