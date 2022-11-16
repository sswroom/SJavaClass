package org.sswr.util.data;

import org.sswr.util.math.Coord2DDbl;
import org.sswr.util.math.geometry.LineString;
import org.sswr.util.math.geometry.MultiPolygon;
import org.sswr.util.math.geometry.Point2D;
import org.sswr.util.math.geometry.PointZ;
import org.sswr.util.math.geometry.Polygon;
import org.sswr.util.math.geometry.Polyline;
import org.sswr.util.math.geometry.Vector2D;

public class MSGeography
{
	public static Vector2D parseBinary(byte[] buff)
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
			else if (buff[5] == 0x0D) //Point Z
			{
				if (buff.length < 30)
				{
					return null;
				}
				PointZ pt = new PointZ(srid, ByteTool.readDouble(buff, 6), ByteTool.readDouble(buff, 14), ByteTool.readDouble(buff, 22));
				return pt;
			}
			else if (buff[5] == 4) //Shape 2D
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
				if (buff[shapeInd + 8] == 1)
				{
					if (nShapes != 1)
					{
						System.out.println("MSGeography: Type 4-1 must be single shape\r\n");
						return null;
					}
					if (nPoints == 0)
					{
						System.out.println("MSGeography: Point empty\r\n");
						return null;
					}
					else if (nPoints != 1)
					{
						System.out.println("MSGeography: Type 4-1 must be single point\r\n");
						return null;
					}
					Point2D pt = new Point2D(srid, ByteTool.readDouble(buff, pointInd + 0), ByteTool.readDouble(buff, pointInd + 8));
					return pt;
				}
				else if (buff[shapeInd + 8] == 2)
				{
					if (nShapes != 1)
					{
						System.out.println("MSGeography: Type 4-2 must be single shape\r\n");
						return null;
					}
					Polyline pl;
					int i;
					int j;
					pl = new Polyline(srid, nFigures, nPoints, false, false);
					Coord2DDbl []points = pl.getPointList();
					i = 0;
					j = points.length;
					while (i < j)
					{
						points[i].x = ByteTool.readDouble(buff, pointInd + i * 16);
						points[i].y = ByteTool.readDouble(buff, pointInd + i * 16 + 8);
						i++;
					}
					if (nFigures > 1)
					{
						int[] ofstList = pl.getPtOfstList();
						i = 0;
						j = ofstList.length;
						while (i < j)
						{
							ofstList[i] = ByteTool.readInt32(buff, figureInd + i * 5 + 1);
							i++;
						}
					}
					return pl;
				}
				else if (buff[shapeInd + 8] == 3)
				{
					if (nShapes != 1)
					{
						System.out.println("MSGeography: Type 4-3 must be single shape\r\n");
						return null;
					}
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
				else if (buff[shapeInd + 8] == 5)
				{
					Polyline pl = new Polyline(srid, nFigures, nPoints, false, false);
					Coord2DDbl []points = pl.getPointList();
					int i = 0;
					while (i < nPoints)
					{
						points[i].x = ByteTool.readDouble(buff, pointInd + i * 16);
						points[i].y = ByteTool.readDouble(buff, pointInd + i * 16 + 8);
						i++;
					}
					if (nFigures > 1)
					{
						int []ofstList = pl.getPtOfstList();
						int j = ofstList.length;
						i = 0;
						while (i < j)
						{
							ofstList[i] = ByteTool.readInt32(buff, figureInd + i * 5 + 1);
							i++;
						}
					}
					return pl;
				}
				else if (buff[shapeInd + 8] == 6) //MultiPolygon
				{
					MultiPolygon mpg;
					Polygon pg;
					int i;
					int j;
					int k;
					int l;
					mpg = new MultiPolygon(srid, false, false);
					if (nFigures > 1)
					{
						i = 0;
						j = 0;
						while (i < nFigures)
						{
							i++;
							if (i == nFigures)
							{
								k = nPoints;
							}
							else
							{
								k = ByteTool.readInt32(buff, figureInd + i * 5 + 1);
							}
							pg = new Polygon(srid, 1, k - j, false, false);
							Coord2DDbl []points = pg.getPointList();
							l = 0;
							while (j < k)
							{
								points[l].x = ByteTool.readDouble(buff, pointInd + j * 16);
								points[l].y = ByteTool.readDouble(buff, pointInd + j * 16 + 8);
								j++;
								l++;
							}
							mpg.addGeometry(pg);
						}
					}
					else
					{
						pg = new Polygon(srid, 1, nPoints, false, false);
						Coord2DDbl []points = pg.getPointList();
						i = 0;
						j = points.length;
						while (i < j)
						{
							points[i].x = ByteTool.readDouble(buff, pointInd + i * 16);
							points[i].y = ByteTool.readDouble(buff, pointInd + i * 16 + 8);
							i++;
						}
						mpg.addGeometry(pg);
					}
					return mpg;
				}
				else if (buff[shapeInd + 8] == 7) //GeometryCollection
				{
					if (nPoints == 0)
					{
						System.out.println("MSGeography: GeometryCollection found\r\n");
					}
					else
					{
						System.out.println("MSGeography: GeometryCollection not supported\r\n");
					}
				}
				else
				{
					System.out.println("MSGeography: Type 4, Unsupported type "+buff[shapeInd + 8]+"\r\n");
	/*				Text::StringBuilderUTF8 sb;
					sb.AppendHexBuff(buffPtr, buffSize, ' ', Text::LineBreakType::CRLF);
					printf("%s\r\n", sb.ToString());*/
				}
			}
			else if (buff[5] == 5) //Shape Z
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
				if (buff[shapeInd + 8] == 2)
				{
					if (nShapes != 1)
					{
						System.out.println("MSGeography: Type 5-2, Multi shape not supported\r\n");
						return null;
					}
					LineString pl;
					int i;
					int j;
					pl = new LineString(srid, nPoints, true, false);
					Coord2DDbl []points = pl.getPointList();
					double []zList = pl.getZList();
					i = 0;
					j = points.length;
					while (i < j)
					{
						points[i].x = ByteTool.readDouble(buff, pointInd + i * 16);
						points[i].y = ByteTool.readDouble(buff, pointInd + i * 16 + 8);
						i++;
					}
					pointInd += j * 16;
					i = 0;
					while (i < j)
					{
						zList[i] = ByteTool.readDouble(buff, pointInd + i * 8);
						i++;
					}
					if (nFigures > 1)
					{
						System.out.println("MSGeography: Type 5, LineString 3D with nFigures > 1\r\n");
					}
					return pl;
				}
				else if (buff[shapeInd + 8] == 3)
				{
					if (nShapes != 1)
					{
						System.out.println("MSGeography: Type 5-3, Multi shape not supported\r\n");
						return null;
					}
					Polygon pg;
					int i;
					int j;
					pg = new Polygon(srid, nFigures, nPoints, true, false);
					Coord2DDbl []points = pg.getPointList();
					double []zList = pg.getZList();
					i = 0;
					j = points.length;
					while (i < j)
					{
						points[i].x = ByteTool.readDouble(buff, pointInd + i * 16);
						points[i].y = ByteTool.readDouble(buff, pointInd + i * 16 + 8);
						i++;
					}
					pointInd += j * 16;
					i = 0;
					while (i < j)
					{
						zList[i] = ByteTool.readDouble(buff, pointInd + i * 8);
						i++;
					}
					if (nFigures > 1)
					{
						int []ofstList = pg.getPtOfstList();
						i = 0;
						j = ofstList.length;
						while (i < j)
						{
							ofstList[i] = ByteTool.readInt32(buff, figureInd + i * 5 + 1);
							i++;
						}
					}
					return pg;
				}
				else if (buff[shapeInd + 8] == 5)
				{
					Polyline pl;
					int i;
					int j;
					pl = new Polyline(srid, nFigures, nPoints, true, false);
					Coord2DDbl []points = pl.getPointList();
					double []zList = pl.getZList();
					i = 0;
					j = points.length;
					while (i < j)
					{
						points[i].x = ByteTool.readDouble(buff, pointInd + i * 16);
						points[i].y = ByteTool.readDouble(buff, pointInd + i * 16 + 8);
						i++;
					}
					pointInd += j * 16;
					i = 0;
					while (i < j)
					{
						zList[i] = ByteTool.readDouble(buff, pointInd + i * 8);
						i++;
					}
					if (nFigures > 1)
					{
						int []ofstList = pl.getPtOfstList();
						i = 0;
						while (i < j)
						{
							ofstList[i] = ByteTool.readInt32(buff, figureInd + i * 5 + 1);
							i++;
						}
					}
					return pl;
				}
				else if (buff[shapeInd + 8] == 6)
				{
					MultiPolygon mpg;
					mpg = new MultiPolygon(srid, true, false);
					shapeInd += 9;
					int thisFigure;
					int nextFigure = ByteTool.readInt32(buff, shapeInd + 4);
					int thisPtOfst;
					int nextPtOfst = ByteTool.readInt32(buff, figureInd + nextFigure * 5 + 1);
					int i;
					int j;
					int pointIndTmp;
					i = 1;
					j = nShapes;
					while (i < j)
					{
						thisFigure = nextFigure;
						thisPtOfst = nextPtOfst;
						if (i + 1 >= j)
						{
							nextFigure = nFigures;
							nextPtOfst = nPoints;
						}
						else
						{
							nextFigure = ByteTool.readInt32(buff, shapeInd + 13);
							nextPtOfst = ByteTool.readInt32(buff, figureInd + nextFigure * 5 + 1);
						}
						Polygon pg;
						int k;
						int l;
						pg = new Polygon(srid, nextFigure - thisFigure, nextPtOfst - thisPtOfst, true, false);
						Coord2DDbl []points = pg.getPointList();
						double []zList = pg.getZList();
						k = 0;
						l = points.length;
						while (k < l)
						{
							points[k].x = ByteTool.readDouble(buff, pointInd + (k + thisPtOfst) * 16);
							points[k].y = ByteTool.readDouble(buff, pointInd + (k + thisPtOfst) * 16 + 8);
							k++;
						}
						pointIndTmp = pointInd + nPoints * 16;
						k = 0;
						while (k < l)
						{
							zList[k] = ByteTool.readDouble(buff, pointIndTmp + (k + thisPtOfst) * 8);
							k++;
						}
						if ((nextFigure - thisFigure) > 1)
						{
							int []ofstList = pg.getPtOfstList();
							k = 0;
							l = ofstList.length;
							while (k < l)
							{
								ofstList[k] = ByteTool.readInt32(buff, figureInd + (k + thisFigure) * 5 + 1) - thisPtOfst;
								k++;
							}
						}
						mpg.addGeometry(pg);
	
						shapeInd += 9;
						i++;
					}
					return mpg;
				}
				else
				{
					System.out.println("MSGeography: Type 5, Unsupported type "+buff[shapeInd + 8]+"\r\n");
				}
			}
			else if (buff[5] == 7) //Shape ZM
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
				if (buff[shapeInd + 8] == 2)
				{
					if (nShapes != 1)
					{
						System.out.println("MSGeography: Type 7-2, Multi shape not supported\r\n");
						return null;
					}
					LineString pl;
					int i;
					int j;
					pl = new LineString(srid, nPoints, true, true);
					Coord2DDbl []points = pl.getPointList();
					double []zList = pl.getZList();
					double []mList = pl.getMList();
					i = 0;
					j = points.length;
					while (i < j)
					{
						points[i].x = ByteTool.readDouble(buff, pointInd + i * 16);
						points[i].y = ByteTool.readDouble(buff, pointInd + i * 16 + 8);
						i++;
					}
					pointInd += j * 16;
					i = 0;
					while (i < j)
					{
						zList[i] = ByteTool.readDouble(buff, pointInd + i * 8);
						i++;
					}
					pointInd += j * 8;
					i = 0;
					while (i < j)
					{
						mList[i] = ByteTool.readDouble(buff, pointInd + i * 8);
						i++;
					}
					if (nFigures > 1)
					{
						System.out.println("MSGeography: Type 7, LineString ZM with nFigures > 1\r\n");
					}
					return pl;
				}
	/*			else if (buff[shapeInd + 8] == 3)
				{
					if (nShapes != 1)
					{
						System.out.println("MSGeography: Type 7, Multi shape not supported\r\n");
						Text::StringBuilderUTF8 sb;
						sb.AppendHexBuff(shapePtr, nShapes * 9, ' ', Text::LineBreakType::CRLF);
						System.out.println("MSGeography: Type 7: shape buff: %s\r\n", sb.ToString());
						return 0;
					}
					Math::Geometry::Polygon *pg;
					UOSInt i;
					UOSInt j;
					NEW_CLASS(pg, Math::Geometry::Polygon(srid, nFigures, nPoints, true, false));
					Math::Coord2DDbl *points = pg.GetPointList(&j);
					Double *zList = pg.GetZList(&j);
					i = 0;
					while (i < j)
					{
						points[i] = Math::Coord2DDbl(ReadDouble(buff, pointInd + i * 16]), ReadDouble(buff, pointInd + i * 16 + 8]));
						i++;
					}
					pointPtr += j * 16;
					i = 0;
					while (i < j)
					{
						zList[i] = ReadDouble(buff, pointInd + i * 8]);
						i++;
					}
					if (nFigures > 1)
					{
						UInt32 *ofstList = pg.GetPtOfstList(&j);
						i = 0;
						while (i < j)
						{
							ofstList[i] = ReadUInt32(buff, figureInd + i * 5 + 1]);
							i++;
						}
					}
					return pg;
				}*/
				else if (buff[shapeInd + 8] == 5)
				{
					Polyline pl;
					int i;
					int j;
					pl = new Polyline(srid, nFigures, nPoints, true, true);
					Coord2DDbl []points = pl.getPointList();
					double []zList = pl.getZList();
					double []mList = pl.getMList();
					i = 0;
					j = points.length;
					while (i < j)
					{
						points[i].x = ByteTool.readDouble(buff, pointInd + i * 16);
						points[i].y = ByteTool.readDouble(buff, pointInd + i * 16 + 8);
						i++;
					}
					pointInd += j * 16;
					i = 0;
					while (i < j)
					{
						zList[i] = ByteTool.readDouble(buff, pointInd + i * 8);
						i++;
					}
					pointInd += j * 8;
					i = 0;
					while (i < j)
					{
						mList[i] = ByteTool.readDouble(buff, pointInd + i * 8);
						i++;
					}
					if (nFigures > 1)
					{
						int []ofstList = pl.getPtOfstList();
						i = 0;
						j = ofstList.length;
						while (i < j)
						{
							ofstList[i] = ByteTool.readInt32(buff, figureInd + i * 5 + 1);
							i++;
						}
					}
					return pl;
				}
				else
				{
					System.out.println("MSGeography: Type 7, Unsupported type "+buff[shapeInd + 8]+"\r\n");
				}
			}
			else if (buff[5] == 20) // LineString
			{
				if (buff.length < 38)
				{
					System.out.println("MSGeography: Type 20, buffSize too short: "+buff.length+"\r\n");
					return null;
				}
				LineString pl;
				pl = new LineString(srid, 2, false, false);
				Coord2DDbl []points = pl.getPointList();
				points[0].x = ByteTool.readDouble(buff, 6);
				points[0].y = ByteTool.readDouble(buff, 14);
				points[1].x = ByteTool.readDouble(buff, 22);
				points[1].y = ByteTool.readDouble(buff, 30);
				return pl;
			}
			else if (buff[5] == 21) // LineString Z
			{
				if (buff.length < 54)
				{
					return null;
				}
				LineString pl = new LineString(srid, 2, true, false);
				Coord2DDbl []points = pl.getPointList();
				double []zList = pl.getZList();
				points[0].x = ByteTool.readDouble(buff, 6);
				points[0].y = ByteTool.readDouble(buff, 14);
				points[1].x = ByteTool.readDouble(buff, 22);
				points[1].y = ByteTool.readDouble(buff, 30);
				zList[0] = ByteTool.readDouble(buff, 38);
				zList[1] = ByteTool.readDouble(buff, 46);
				return pl;
			}
			else if (buff[5] == 23) // LineString ZM
			{
				if (buff.length < 70)
				{
					return null;
				}
				LineString pl;
				pl = new LineString(srid, 2, true, true);
				Coord2DDbl []points = pl.getPointList();
				double []zList = pl.getZList();
				double []mList = pl.getMList();
				points[0].x = ByteTool.readDouble(buff, 6);
				points[0].y = ByteTool.readDouble(buff, 14);
				points[1].x = ByteTool.readDouble(buff, 22);
				points[1].y = ByteTool.readDouble(buff, 30);
				zList[0] = ByteTool.readDouble(buff, 38);
				zList[1] = ByteTool.readDouble(buff, 46);
				mList[0] = ByteTool.readDouble(buff, 54);
				mList[1] = ByteTool.readDouble(buff, 62);
				return pl;
			}
			else
			{
				System.out.println("MSGeography: Unsupported type "+buff[5]+"\r\n");
			}
		}
		return null;
	}
}
