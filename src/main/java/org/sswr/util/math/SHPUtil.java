package org.sswr.util.math;

import org.sswr.util.data.ByteTool;
import org.sswr.util.math.geometry.LineString;
import org.sswr.util.math.geometry.Point2D;
import org.sswr.util.math.geometry.Polyline;
import org.sswr.util.math.geometry.Vector2D;

public class SHPUtil {
/* 	static Vector2D parseShpRecord(int srid, byte[] buff)
	{
		if (buff.length < 4)
			return null;
		int shpType = ByteTool.readInt32(buff, 0);
		switch (shpType)
		{
		case 0: //Null
			return null;
		case 1: //Point
			if (buff.length >= 20)
			{
				return new Point2D(srid, ByteTool.readDouble(buff, 4), ByteTool.readDouble(buff, 12));
			}
			return null;
		case 3: //Polyline
			if (buff.length >= 44)
			{
				int nPtOfst;
				int nPoint;
				Polyline pl;
				nPtOfst = ByteTool.readInt32(buff, 36);
				nPoint = ByteTool.readInt32(buff, 40);
				if (buff.length >= 44 + nPtOfst * 4 + nPoint * 16)
				{
					int buffIndex = 0;
					UOSInt i;
					UOSInt j;
					UOSInt k;
					int[] ptOfsts;
					Math::Coord2DDbl *points;
					LineString lineString;
					pl = new Polyline(srid);
					ptOfsts = new int[nPtOfst];
					buffIndex += 44;
					i = 0;
					while (i < nPtOfst)
					{
						ptOfsts[i] = ByteTool.readInt32(buff, buffIndex);
						buff += 4;
						i++;
					}
					i = 0;
					while (i < nPtOfst)
					{
						if (i + 1 == nPtOfst)
						{
							k = nPoint - ptOfsts[i];
						}
						else
						{
							k = ptOfsts[i + 1] - ptOfsts[i];
						}
						NEW_CLASSNN(lineString, Math::Geometry::LineString(srid, k, false, false));
						points = lineString->GetPointList(j);
						j = 0;
						while (j < k)
						{
							points[j].x = ReadDouble(&buff[0]);
							points[j].y = ReadDouble(&buff[8]);
							buff += 16;
							j++;
						}
						pl->AddGeometry(lineString);
						i++;
					}
					MemFree(ptOfsts);
					return pl;
				}
			}
			return 0;
		case 5: //Polygon
			if (buffSize >= 44)
			{
				UInt32 nPtOfst;
				UInt32 nPoint;
				Math::Geometry::Polygon *pg;
				NotNullPtr<Math::Geometry::LinearRing> lr;
				nPtOfst = ReadUInt32(&buff[36]);
				nPoint = ReadUInt32(&buff[40]);
				if (buffSize >= 44 + nPtOfst * 4 + nPoint * 16)
				{
					UOSInt tmpV;
					const UInt8 *ptOfsts;
					Math::Coord2DDbl *points;
					UOSInt i = 0;
					UOSInt j = 0;
					UOSInt k;
					buff += 44;
					ptOfsts = buff;
					buff += nPtOfst * 4;
					NEW_CLASS(pg, Math::Geometry::Polygon(srid));
					while (i < nPtOfst)
					{
						i++;
						if (i >= nPtOfst)
							k = nPoint;
						else
							k = ReadUInt32(&ptOfsts[i * 4]);
						NEW_CLASSNN(lr, Math::Geometry::LinearRing(srid, (k - j), false, false));
						points = lr->GetPointList(tmpV);
						while (tmpV-- > 0)
						{
							points->x = ReadDouble(&buff[0]);
							points->y = ReadDouble(&buff[8]);
							points++;
							buff += 16;
						}
						j = k;
						pg->AddGeometry(lr);
					}
					return pg;
				}
			}
			return 0;
		case 8: //Multipoint
			if (false)
			{
			}
			return 0;
		case 9: //PointZ
			if (buffSize >= 28)
			{
				Math::Geometry::PointZ *pt;
				NEW_CLASS(pt, Math::Geometry::PointZ(srid, ReadDouble(&buff[4]), ReadDouble(&buff[12]), ReadDouble(&buff[20])));
				return pt;
			}
			return 0;
		case 10: //PolylineZ
			if (buffSize >= 44)
			{
				UInt32 nPtOfst;
				UInt32 nPoint;
				Math::Geometry::Polyline *pl;
				nPtOfst = ReadUInt32(&buff[36]);
				nPoint = ReadUInt32(&buff[40]);
				if (buffSize >= 44 + nPtOfst * 4 + nPoint * 24)
				{
					UOSInt i;
					UOSInt j;
					UOSInt k;
					UInt32 *ptOfsts;
					NotNullPtr<Math::Geometry::LineString> lineString;
					Math::Coord2DDbl *points;
					Double *alts;
					NEW_CLASS(pl, Math::Geometry::Polyline(srid));
					ptOfsts = MemAlloc(UInt32, nPtOfst);
					buff += 44;
					i = 0;
					while (i < nPtOfst)
					{
						ptOfsts[i] = ReadUInt32(buff);
						buff += 4;
						i++;
					}
					i = 0;
					while (i < nPtOfst)
					{
						if (i + 1 == nPtOfst)
						{
							k = nPoint - ptOfsts[i];
						}
						else
						{
							k = ptOfsts[i + 1] - ptOfsts[i];
						}
						NEW_CLASSNN(lineString, Math::Geometry::LineString(srid, k, true, false));
						points = lineString->GetPointList(j);
						j = 0;
						while (j < k)
						{
							points[j].x = ReadDouble(&buff[0]);
							points[j].y = ReadDouble(&buff[8]);
							buff += 16;
							j++;
						}
						pl->AddGeometry(lineString);
						i++;
					}
					i = 0;
					while (i < nPtOfst)
					{
						if (pl->GetItem(i).SetTo(lineString))
						{
							alts = lineString->GetZList(k);
							j = 0;
							while (j < k)
							{
								alts[j] = ReadDouble(&buff[0]);
								buff += 8;
								j++;
							}
						}
						i++;
					}
					MemFree(ptOfsts);
					return pl;
				}
			}
			return 0;
		case 11: //PointZM
			if (buffSize >= 36)
			{
				Math::Geometry::PointZM *pt;
				NEW_CLASS(pt, Math::Geometry::PointZM(srid, ReadDouble(&buff[4]), ReadDouble(&buff[12]), ReadDouble(&buff[20]), ReadDouble(&buff[28])));
				return pt;
			}
			return 0;
		case 13: //PolylineZM
			if (buffSize >= 44)
			{
				UInt32 nPtOfst;
				UInt32 nPoint;
				Math::Geometry::Polyline *pl;
				nPtOfst = ReadUInt32(&buff[36]);
				nPoint = ReadUInt32(&buff[40]);
				if (buffSize >= 44 + nPtOfst * 4 + nPoint * 32)
				{
					UOSInt i;
					UOSInt j;
					UOSInt k;
					UInt32 *ptOfsts;
					NotNullPtr<Math::Geometry::LineString> lineString;
					Math::Coord2DDbl *points;
					Double *dArr;
					NEW_CLASS(pl, Math::Geometry::Polyline(srid));
					ptOfsts = MemAlloc(UInt32, nPtOfst);
					buff += 44;
					i = 0;
					while (i < nPtOfst)
					{
						ptOfsts[i] = ReadUInt32(buff);
						buff += 4;
						i++;
					}
					i = 0;
					while (i < nPtOfst)
					{
						if (i + 1 == nPtOfst)
						{
							k = nPoint - ptOfsts[i];
						}
						else
						{
							k = ptOfsts[i + 1] - ptOfsts[i];
						}
						NEW_CLASSNN(lineString, Math::Geometry::LineString(srid, k, true, true));
						points = lineString->GetPointList(j);
						j = 0;
						while (j < k)
						{
							points[j].x = ReadDouble(&buff[0]);
							points[j].y = ReadDouble(&buff[8]);
							buff += 16;
							j++;
						}
						pl->AddGeometry(lineString);
						i++;
					}
					i = 0;
					while (i < nPtOfst)
					{
						if (pl->GetItem(i).SetTo(lineString))
						{
							dArr = lineString->GetZList(k);
							j = 0;
							while (j < k)
							{
								dArr[j] = ReadDouble(&buff[0]);
								buff += 8;
								j++;
							}
						}
						i++;
					}
					i = 0;
					while (i < nPtOfst)
					{
						if (pl->GetItem(i).SetTo(lineString))
						{
							dArr = lineString->GetMList(k);
							j = 0;
							while (j < k)
							{
								dArr[j] = ReadDouble(&buff[0]);
								buff += 8;
								j++;
							}
						}
						i++;
					}
					MemFree(ptOfsts);
					return pl;
				}
			}
			return 0;
		case 15: //PolygonZM
			if (buffSize >= 44)
			{
				UInt32 nPtOfst;
				UInt32 nPoint;
				Math::Geometry::Polygon *pg;
				nPtOfst = ReadUInt32(&buff[36]);
				nPoint = ReadUInt32(&buff[40]);
				if (buffSize >= 44 + nPtOfst * 4 + nPoint * 16)
				{
					UOSInt i;
					UInt32 *ptOfsts = MemAlloc(UInt32, nPtOfst);
					Math::Coord2DDbl *points = MemAllocA(Math::Coord2DDbl, nPoint);
					Double *zArr = MemAlloc(Double, nPoint);
					Double *mArr = MemAlloc(Double, nPoint);
					NEW_CLASS(pg, Math::Geometry::Polygon(srid));
					buff += 44;
					i = 0;
					while (i < nPtOfst)
					{
						ptOfsts[i] = ReadUInt32(buff);
						buff += 4;
						i++;
					}
					i = 0;
					while (i < nPoint)
					{
						points[i].x = ReadDouble(&buff[0]);
						points[i].y = ReadDouble(&buff[8]);
						buff += 16;
						i++;
					}
					i = 0;
					while (i < nPoint)
					{
						zArr[i] = ReadDouble(&buff[0]);
						buff += 8;
						i++;
					}
					i = 0;
					while (i < nPoint)
					{
						mArr[i] = ReadDouble(&buff[0]);
						buff += 8;
						i++;
					}
					pg->AddFromPtOfst(ptOfsts, nPtOfst, points, nPoint, zArr, mArr);
					MemFree(mArr);
					MemFree(zArr);
					MemFreeA(points);
					MemFree(ptOfsts);
					return pg;
				}
			}
			return 0;
		case 18: //MultipointZM
			return 0;
		case 19: //PolygonZ
			if (buffSize >= 44)
			{
				UInt32 nPtOfst;
				UInt32 nPoint;
				Math::Geometry::Polygon *pg;
				nPtOfst = ReadUInt32(&buff[36]);
				nPoint = ReadUInt32(&buff[40]);
				if (buffSize >= 44 + nPtOfst * 4 + nPoint * 16)
				{
					UOSInt i;
					UInt32 *ptOfsts = MemAlloc(UInt32, nPtOfst);
					Math::Coord2DDbl *points = MemAllocA(Math::Coord2DDbl, nPoint);
					Double *zArr = MemAlloc(Double, nPoint);
					NEW_CLASS(pg, Math::Geometry::Polygon(srid));
					buff += 44;
					i = 0;
					while (i < nPtOfst)
					{
						ptOfsts[i] = ReadUInt32(buff);
						buff += 4;
						i++;
					}
					i = 0;
					while (i < nPoint)
					{
						points[i].x = ReadDouble(&buff[0]);
						points[i].y = ReadDouble(&buff[8]);
						buff += 16;
						i++;
					}
					i = 0;
					while (i < nPoint)
					{
						zArr[i] = ReadDouble(&buff[0]);
						buff += 8;
						i++;
					}
					pg->AddFromPtOfst(ptOfsts, nPtOfst, points, nPoint, zArr, 0);
					MemFree(zArr);
					MemFreeA(points);
					MemFree(ptOfsts);
					return pg;
				}
			}
			return 0;
		case 20: //MultipointZ
			return 0;
		case 21: //PointM
			if (buffSize >= 28)
			{
				Math::Geometry::PointM *pt;
				NEW_CLASS(pt, Math::Geometry::PointM(srid, ReadDouble(&buff[4]), ReadDouble(&buff[12]), ReadDouble(&buff[20])));
				// measure = ReadDouble(&buff[20]);
				return pt;
			}
			return 0;
		case 23: //PolylineM
			if (buffSize >= 44)
			{
				UInt32 nPtOfst;
				UInt32 nPoint;
				Math::Geometry::Polyline *pl;
				nPtOfst = ReadUInt32(&buff[36]);
				nPoint = ReadUInt32(&buff[40]);
				if (buffSize >= 44 + nPtOfst * 4 + nPoint * 16)
				{
					UOSInt i;
					UOSInt j;
					UOSInt k;
					UInt32 *ptOfsts;
					NotNullPtr<Math::Geometry::LineString> lineString;
					Math::Coord2DDbl *points;
					Double *mArr;
					NEW_CLASS(pl, Math::Geometry::Polyline(srid));
					ptOfsts = MemAlloc(UInt32, nPtOfst);
					buff += 44;
					i = 0;
					while (i < nPtOfst)
					{
						ptOfsts[i] = ReadUInt32(buff);
						buff += 4;
						i++;
					}
					i = 0;
					while (i < nPtOfst)
					{
						if (i + 1 == nPtOfst)
						{
							k = nPoint - ptOfsts[i];
						}
						else
						{
							k = ptOfsts[i + 1] - ptOfsts[i];
						}
						NEW_CLASSNN(lineString, Math::Geometry::LineString(srid, k, false, true));
						points = lineString->GetPointList(j);
						j = 0;
						while (j < k)
						{
							points[j].x = ReadDouble(&buff[0]);
							points[j].y = ReadDouble(&buff[8]);
							buff += 16;
							j++;
						}
						pl->AddGeometry(lineString);
						i++;
					}
					i = 0;
					while (i < nPtOfst)
					{
						if (pl->GetItem(i).SetTo(lineString))
						{
							mArr = lineString->GetMList(k);
							j = 0;
							while (j < k)
							{
								mArr[j] = ReadDouble(&buff[0]);
								buff += 8;
								j++;
							}
						}
						i++;
					}
					MemFree(ptOfsts);
					return pl;
				}
			}
			return 0;
		case 25: //PolygonM
			if (buffSize >= 44)
			{
				UInt32 nPtOfst;
				UInt32 nPoint;
				Math::Geometry::Polygon *pg;
				nPtOfst = ReadUInt32(&buff[36]);
				nPoint = ReadUInt32(&buff[40]);
				if (buffSize >= 44 + nPtOfst * 4 + nPoint * 16)
				{
					UOSInt i;
					UInt32 *ptOfsts = MemAlloc(UInt32, nPtOfst);
					Math::Coord2DDbl *points = MemAllocA(Math::Coord2DDbl, nPoint);
					Double *mArr = MemAlloc(Double, nPoint);
					NEW_CLASS(pg, Math::Geometry::Polygon(srid));
					buff += 44;
					i = 0;
					while (i < nPtOfst)
					{
						ptOfsts[i] = ReadUInt32(buff);
						buff += 4;
						i++;
					}
					i = 0;
					while (i < nPoint)
					{
						points[i].x = ReadDouble(&buff[0]);
						points[i].y = ReadDouble(&buff[8]);
						buff += 16;
						i++;
					}
					i = 0;
					while (i < nPoint)
					{
						mArr[i] = ReadDouble(&buff[0]);
						buff += 8;
						i++;
					}
					pg->AddFromPtOfst(ptOfsts, nPtOfst, points, nPoint, 0, mArr);
					MemFree(mArr);
					MemFreeA(points);
					MemFree(ptOfsts);
					return pg;
				}
			}
			return 0;
		case 28: //MultipointM
		case 31: //MultiPatchM
		case 32: //MultiPatch
		case 50: //GeneralPolyline
		case 51: //GeneralPolygon
		case 52: //GeneralPoint
		case 53: //GeneralMultipoint
		case 54: //GeneralMultiPatch
		default:
			return 0;
		}
	}*/
}
