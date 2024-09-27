package org.sswr.util.data;

import org.sswr.util.math.Coord2DDbl;
import org.sswr.util.math.geometry.LineString;
import org.sswr.util.math.geometry.LinearRing;
import org.sswr.util.math.geometry.MultiPolygon;
import org.sswr.util.math.geometry.Point2D;
import org.sswr.util.math.geometry.PointZ;
import org.sswr.util.math.geometry.Polygon;
import org.sswr.util.math.geometry.Polyline;
import org.sswr.util.math.geometry.Vector2D;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class MSGeography
{
	@Nullable
	public static Vector2D parseBinary(@Nullable byte[] buff)
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
					LineString lineString;
					int i;
					int j;
					int k;
					int l;
					Coord2DDbl []points;
					pl = new Polyline(srid);
					if (nFigures <= 1)
					{
						lineString = new LineString(srid, nPoints, false, false);
						points = lineString.getPointList();
						i = 0;
						j = points.length;
						while (i < j)
						{
							points[i] = new Coord2DDbl(ByteTool.readDouble(buff, pointInd + i * 16), ByteTool.readDouble(buff, pointInd + i * 16 + 8));
							i++;
						}
						pl.addGeometry(lineString);
					}
					else
					{
						i = 0;
						while (i < nFigures)
						{
							l = ByteTool.readInt32(buff, figureInd + i * 5 + 1);
							if (i + 1 == nFigures)
							{
								k = nPoints;
							}
							else
							{
								k = ByteTool.readInt32(buff, figureInd + i * 5 + 6);
							}
							lineString = new LineString(srid, k - l, false, false);
							points = lineString.getPointList();
							j = 0;
							while (l < k)
							{
								points[j] = new Coord2DDbl(ByteTool.readDouble(buff, pointInd + l * 16), ByteTool.readDouble(buff, pointInd + l * 16 + 8));
								l++;
								j++;
							}
							pl.addGeometry(lineString);
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
					Polygon pg = new Polygon(srid);
					LinearRing lr;
					int i;
					int j;
					int k;
					int l;
					j = 0;
					i = 0;
					while (i < nFigures)
					{
						if (i + 1 >= nFigures)
							k = nPoints;
						else
							k = ByteTool.readInt32(buff, figureInd + i * 5 + 1);
						lr = new LinearRing(srid, (k - j), false ,false);
						Coord2DDbl []points = lr.getPointList();
						l = 0;
						while (j < k)
						{
							points[l] = new Coord2DDbl(ByteTool.readDouble(buff, pointInd + j * 16), ByteTool.readDouble(buff, pointInd + j * 16 + 8));
							j++;
							l++;
						}
						pg.addGeometry(lr);
						i++;
					}
					return pg;
				}
				else if (buff[shapeInd + 8] == 5)
				{
					Polyline pl = new Polyline(srid);
					LineString lineString;
					int i;
					int j;
					int k;
					int l;
					Coord2DDbl[] points;
					if (nFigures <= 1)
					{
						lineString = new LineString(srid, nPoints, false, false);
						points = lineString.getPointList();
						i = 0;
						j = points.length;
						while (i < j)
						{
							points[i] = new Coord2DDbl(ByteTool.readDouble(buff, pointInd + i * 16), ByteTool.readDouble(buff, pointInd + i * 16 + 8));
							i++;
						}
						pl.addGeometry(lineString);
					}
					else
					{
						i = 0;
						while (i < nFigures)
						{
							l = ByteTool.readInt32(buff, figureInd + i * 5 + 1);
							if (i + 1 == nFigures)
							{
								k = nPoints;
							}
							else
							{
								k = ByteTool.readInt32(buff, figureInd + i * 5 + 6);
							}
							lineString = new LineString(srid, k - l, false, false);
							points = lineString.getPointList();
							j = 0;
							while (l < k)
							{
								points[j] = new Coord2DDbl(ByteTool.readDouble(buff, pointInd + l * 16), ByteTool.readDouble(buff, pointInd + l * 16 + 8));
								l++;
								j++;
							}
							pl.addGeometry(lineString);
							i++;
						}
					}
					return pl;
				}
				else if (buff[shapeInd + 8] == 6) //MultiPolygon
				{
					MultiPolygon mpg;
					Polygon pg;
					LinearRing lr;
					int i;
					int j;
					int k;
					int l;
					mpg = new MultiPolygon(srid);
					if (nFigures == 0)
						nFigures = 1;
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
						pg = new Polygon(srid);
						lr = new LinearRing(srid, (k - j), false, false);
						Coord2DDbl[] points = lr.getPointList();
						l = 0;
						while (j < k)
						{
							points[l] = new Coord2DDbl(ByteTool.readDouble(buff, pointInd + j * 16), ByteTool.readDouble(buff, pointInd + j * 16 + 8));
							j++;
							l++;
						}
						pg.addGeometry(lr);
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
					if (zList != null)
					{
						i = 0;
						while (i < j)
						{
							zList[i] = ByteTool.readDouble(buff, pointInd + i * 8);
							i++;
						}
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
					LinearRing lr;
					int i;
					int j;
					int k;
					int l;
					int zInd = pointInd + nPoints * 16;
					if (nFigures == 0)
						nFigures = 1;
					pg = new Polygon(srid);
					i = 0;
					j = 0;
					while (i < nFigures)
					{
						i++;
						if (i >= nFigures)
							k = nPoints;
						else
							k = ByteTool.readInt32(buff, figureInd + i * 5 + 1);
						lr = new LinearRing(srid, (k - j), true, false);
						Coord2DDbl[] points = lr.getPointList();
						double[] zList = lr.getZList();
						l = 0;
						while (j < k)
						{
							points[l] = new Coord2DDbl(ByteTool.readDouble(buff, pointInd + j * 16), ByteTool.readDouble(buff, pointInd + j * 16 + 8));
							if (zList != null)
								zList[l] = ByteTool.readDouble(buff, zInd + j * 8);
							j++;
							l++;
						}
						pg.addGeometry(lr);
					}
					return pg;
				}
				else if (buff[shapeInd + 8] == 5)
				{
					int zInd = pointInd + nPoints * 16;
					Polyline pl;
					LineString lineString;
					int i;
					int j;
					int k;
					int l;
					Coord2DDbl[] points;
					double[] zArr;					
					pl = new Polyline(srid);
					if (nFigures <= 1)
					{
						lineString = new LineString(srid, nPoints, true, false);
						points = lineString.getPointList();
						zArr = lineString.getZList();
						i = 0;
						j = points.length;
						while (i < j)
						{
							points[i] = new Coord2DDbl(ByteTool.readDouble(buff, pointInd + i * 16), ByteTool.readDouble(buff, pointInd + i * 16 + 8));
							if (zArr != null)
								zArr[i] = ByteTool.readDouble(buff, zInd + i * 8);
							i++;
						}
						pl.addGeometry(lineString);
					}
					else
					{
						i = 0;
						while (i < nFigures)
						{
							l = ByteTool.readInt32(buff, figureInd + i * 5 + 1);
							if (i + 1 == nFigures)
							{
								k = nPoints;
							}
							else
							{
								k = ByteTool.readInt32(buff, figureInd + i * 5 + 6);
							}
							lineString = new LineString(srid, k - l, true, false);
							points = lineString.getPointList();
							zArr = lineString.getZList();
							j = 0;
							while (l < k)
							{
								points[j] = new Coord2DDbl(ByteTool.readDouble(buff, pointInd + l * 16), ByteTool.readDouble(buff, pointInd + l * 16 + 8));
								if (zArr != null)
									zArr[j] = ByteTool.readDouble(buff, zInd + l * 8);
								l++;
								j++;
							}
							pl.addGeometry(lineString);
							i++;
						}
					}
					return pl;
				}
				else if (buff[shapeInd + 8] == 6)
				{
					MultiPolygon mpg;
					mpg = new MultiPolygon(srid);
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
						LinearRing lr;
						int k;
						int l;
						int m = thisFigure;
						pointIndTmp = pointInd + nPoints * 16;
						pg = new Polygon(srid);
						k = 0;
						while (m < nextFigure)
						{
							if (m + 1 >= nextFigure)
								l = nextFigure - thisFigure;
							else
								l = ByteTool.readInt32(buff, figureInd + (k + thisFigure) * 5 + 1);
							lr = new LinearRing(srid, l - k, true, false);
							Coord2DDbl[] points = lr.getPointList();
							@SuppressWarnings("null")
							@Nonnull double[] zList = lr.getZList();
							while (k < l)
							{
								points[k] = new Coord2DDbl(ByteTool.readDouble(buff, pointInd + (k + thisPtOfst) * 16), ByteTool.readDouble(buff, pointInd + (k + thisPtOfst) * 16 + 8));
								zList[k] = ByteTool.readDouble(buff, pointIndTmp + (k + thisPtOfst) * 8);
								k++;
							}
							pg.addGeometry(lr);
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
					if (zList != null)
					{
						i = 0;
						while (i < j)
						{
							zList[i] = ByteTool.readDouble(buff, pointInd + i * 8);
							i++;
						}
					}
					pointInd += j * 8;
					if (mList != null)
					{
						i = 0;
						while (i < j)
						{
							mList[i] = ByteTool.readDouble(buff, pointInd + i * 8);
							i++;
						}
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
					int zInd = pointInd + nPoints * 16;
					int mInd = zInd + nPoints * 8;
					Polyline pl;
					LineString lineString;
					int i;
					int j;
					int k;
					int l;
					Coord2DDbl []points;
					pl = new Polyline(srid);
					if (nFigures <= 1)
					{
						lineString = new LineString(srid, nPoints, true, true);
						points = lineString.getPointList();
						@SuppressWarnings("null")
						@Nonnull double []zArr = lineString.getZList();
						@SuppressWarnings("null")
						@Nonnull double []mArr = lineString.getMList();
						i = 0;
						j = points.length;
						while (i < j)
						{
							points[i] = new Coord2DDbl(ByteTool.readDouble(buff, pointInd + i * 16), ByteTool.readDouble(buff, pointInd + i * 16 + 8));
							zArr[i] = ByteTool.readDouble(buff, zInd + i * 8);
							mArr[i] = ByteTool.readDouble(buff, mInd + i * 8);
							i++;
						}
						pl.addGeometry(lineString);
					}
					else
					{
						i = 0;
						while (i < nFigures)
						{
							l = ByteTool.readInt32(buff, figureInd + i * 5 + 1);
							if (i + 1 == nFigures)
							{
								k = nPoints;
							}
							else
							{
								k = ByteTool.readInt32(buff, figureInd + i * 5 + 6);
							}
							lineString = new LineString(srid, k - l, true, true);
							points = lineString.getPointList();
							@SuppressWarnings("null")
							@Nonnull double []zArr = lineString.getZList();
							@SuppressWarnings("null")
							@Nonnull double []mArr = lineString.getMList();
							j = 0;
							while (l < k)
							{
								points[j] = new Coord2DDbl(ByteTool.readDouble(buff, pointInd + l * 16), ByteTool.readDouble(buff, pointInd + l * 16 + 8));
								zArr[j] = ByteTool.readDouble(buff, zInd + l * 8);
								mArr[j] = ByteTool.readDouble(buff, mInd + l * 8);
								l++;
								j++;
							}
							pl.addGeometry(lineString);
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
				@SuppressWarnings("null")
				@Nonnull double []zList = pl.getZList();
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
				@SuppressWarnings("null")
				@Nonnull double []zList = pl.getZList();
				@SuppressWarnings("null")
				@Nonnull double []mList = pl.getMList();
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
