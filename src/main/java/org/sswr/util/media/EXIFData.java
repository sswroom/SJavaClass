package org.sswr.util.media;

import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.sswr.util.data.ArtificialQuickSort;
import org.sswr.util.data.ByteIO;
import org.sswr.util.data.ByteIOLSB;
import org.sswr.util.data.ByteIOMSB;
import org.sswr.util.data.ByteTool;
import org.sswr.util.data.DateTimeUtil;
import org.sswr.util.data.LineBreakType;
import org.sswr.util.data.SharedDouble;
import org.sswr.util.data.SharedInt;
import org.sswr.util.data.SharedLong;
import org.sswr.util.data.StringUtil;
import org.sswr.util.io.ResourceLoader;
import org.sswr.util.io.StreamData;

public class EXIFData
{
	private static List<EXIFInfo> defInfos;
	private static List<EXIFInfo> exifInfos;
	private static List<EXIFInfo> gpsInfos;
	private static List<EXIFInfo> panasonicInfos;
	private static List<EXIFInfo> canonInfos;
	private static List<EXIFInfo> olympusInfos;
	private static List<EXIFInfo> olympus2010Infos;
	private static List<EXIFInfo> olympus2020Infos;
	private static List<EXIFInfo> olympus2030Infos;
	private static List<EXIFInfo> olympus2040Infos;
	private static List<EXIFInfo> olympus2050Infos;
	private static List<EXIFInfo> casio1Infos;
	private static List<EXIFInfo> casio2Infos;
	private static List<EXIFInfo> flirInfos;

	private Map<Integer, EXIFItem> exifMap;
	private EXIFMaker exifMaker;

	private void toExifBuff(byte[] buff, Iterable<EXIFItem> exifList, SharedInt startOfst, SharedInt otherOfst)
	{
		int objCnt;
		Iterator<EXIFItem> itExif = exifList.iterator();
		int j;
		int k;
		EXIFItem exif;
	
		objCnt = 0;
		k = otherOfst.value;
		j = startOfst.value + 2;
		while (itExif.hasNext())
		{
			exif = itExif.next();
			if (exif.type == EXIFType.BYTES)
			{
				ByteTool.writeInt16(buff, j, exif.id);
				ByteTool.writeInt16(buff, j + 2, 1);
				ByteTool.writeInt32(buff, j + 4, exif.size);
				if (exif.size <= 4)
				{
					ByteTool.writeInt32(buff, j + 8, 0);
					ByteTool.copyArray(buff, j + 8, exif.dataBuff, 0, exif.size);
					j += 12;
				}
				else
				{
					ByteTool.writeInt32(buff, j + 8, k);
					ByteTool.copyArray(buff, k, exif.dataBuff, 0, exif.size);
					k += exif.size;
					j += 12;
				}
				objCnt++;
			}
			else if (exif.type == EXIFType.STRING)
			{
				ByteTool.writeInt16(buff, j, exif.id);
				ByteTool.writeInt16(buff, j + 2, 2);
				ByteTool.writeInt32(buff, j + 4, exif.size);
				if (exif.size <= 4)
				{
					ByteTool.writeInt32(buff, j + 8, 0);
					ByteTool.copyArray(buff, j + 8, exif.dataBuff, 0, exif.size);
					j += 12;
				}
				else
				{
					ByteTool.writeInt32(buff, j + 8, k);
					ByteTool.copyArray(buff, k, exif.dataBuff, 0, exif.size);
					k += exif.size;
					j += 12;
				}
				objCnt++;
			}
			else if (exif.type == EXIFType.UINT16)
			{
				ByteTool.writeInt16(buff, j, exif.id);
				ByteTool.writeInt16(buff, j + 2, 3);
				ByteTool.writeInt32(buff, j + 4, exif.size);
				if (exif.size <= 2)
				{
					ByteTool.writeInt32(buff, j + 8, 0);
					ByteTool.copyArray(buff, j + 8, exif.dataBuff, 0, exif.size << 1);
					j += 12;
				}
				else
				{
					ByteTool.writeInt32(buff, j + 8, k);
					ByteTool.copyArray(buff, k, exif.dataBuff, 0, exif.size << 1);
					k += exif.size << 1;
					j += 12;
				}
				objCnt++;
			}
			else if (exif.type == EXIFType.UINT32)
			{
				ByteTool.writeInt16(buff, j, exif.id);
				ByteTool.writeInt16(buff, j + 2, 4);
				ByteTool.writeInt32(buff, j + 4, exif.size);
				if (exif.size <= 1)
				{
					ByteTool.writeInt32(buff, j + 8, 0);
					ByteTool.copyArray(buff, j + 8, exif.dataBuff, 0, exif.size << 2);
					j += 12;
				}
				else
				{
					ByteTool.writeInt32(buff, j + 8, k);
					ByteTool.copyArray(buff, k, exif.dataBuff, 0, exif.size << 2);
					k += exif.size << 2;
					j += 12;
				}
				objCnt++;
			}
			else if (exif.type == EXIFType.RATIONAL)
			{
				ByteTool.writeInt16(buff, j, exif.id);
				ByteTool.writeInt16(buff, j + 2, 5);
				ByteTool.writeInt32(buff, j + 4, exif.size);
				ByteTool.writeInt32(buff, j + 8, k);
				ByteTool.copyArray(buff, k, exif.dataBuff, 0, exif.size << 3);
				k += exif.size << 3;
				j += 12;
				objCnt++;
			}
			else if (exif.type == EXIFType.OTHER)
			{
				ByteTool.writeInt16(buff, j, exif.id);
				ByteTool.writeInt16(buff, j + 2, 7);
				ByteTool.writeInt32(buff, j + 4, exif.size);
				if (exif.size <= 4)
				{
					ByteTool.writeInt32(buff, j + 8, 0);
					ByteTool.copyArray(buff, j + 8, exif.dataBuff, 0, exif.size);
					j += 12;
				}
				else
				{
					ByteTool.writeInt32(buff, j + 8, k);
					ByteTool.copyArray(buff, k, exif.dataBuff, 0, exif.size);
					k += exif.size;
					j += 12;
				}
				objCnt++;
			}
			else if (exif.type == EXIFType.INT16)
			{
				ByteTool.writeInt16(buff, j, exif.id);
				ByteTool.writeInt16(buff, j + 2, 8);
				ByteTool.writeInt32(buff, j + 4, exif.size);
				if (exif.size <= 2)
				{
					ByteTool.writeInt32(buff, j + 8, 0);
					ByteTool.copyArray(buff, j + 8, exif.dataBuff, 0, exif.size << 1);
					j += 12;
				}
				else
				{
					ByteTool.writeInt32(buff, j + 8, k);
					ByteTool.copyArray(buff, k, exif.dataBuff, 0, exif.size << 1);
					k += exif.size << 1;
					j += 12;
				}
				objCnt++;
			}
			else if (exif.type == EXIFType.SUBEXIF)
			{
				ByteTool.writeInt16(buff, j, exif.id);
				ByteTool.writeInt16(buff, j + 2, 4);
				ByteTool.writeInt32(buff, j + 4, 1);
				exif.ofst = j + 8;
				j += 12;
				objCnt++;
			}
			else if (exif.type == EXIFType.DOUBLE)
			{
				ByteTool.writeInt16(buff, j, exif.id);
				ByteTool.writeInt16(buff, j + 2, 12);
				ByteTool.writeInt32(buff, j + 4, exif.size);
				ByteTool.writeInt32(buff, j + 8, k);
				ByteTool.copyArray(buff, k, exif.dataBuff, 0, exif.size << 3);
				k += exif.size << 3;
				j += 12;
				objCnt++;
			}
		}
		ByteTool.writeInt32(buff, j, 0);
		ByteTool.writeInt16(buff, startOfst.value, objCnt);
		j += 4;
	
		itExif = exifList.iterator();
		while (itExif.hasNext())
		{
			exif = itExif.next();
			if (exif.type == EXIFType.SUBEXIF)
			{
				ByteTool.writeInt32(buff, exif.ofst, j);
				SharedInt sj = new SharedInt(j);
				SharedInt sk = new SharedInt(k);
				exif.subExif.toExifBuff(buff, sj, sk);
				j = sj.value;
				k = sk.value;
			}
		}
		startOfst.value = j;
		otherOfst.value = k;
	}

	private void getExifBuffSize(Iterable<EXIFItem> exifList, SharedInt size, SharedInt endOfst)
	{
		int i = 6;
		int j = 6;
		Iterator<EXIFItem> itExif = exifList.iterator();
		SharedInt l = new SharedInt();
		SharedInt m = new SharedInt();
		EXIFItem exif;
	
		while (itExif.hasNext())
		{
			exif = itExif.next();
			if (exif.type == EXIFType.BYTES)
			{
				i += 12;
				if (exif.size <= 4)
				{
					j += 12;
				}
				else
				{
					j += 12 + exif.size;
				}
			}
			else if (exif.type == EXIFType.STRING)
			{
				i += 12;
				if (exif.size <= 4)
					j += 12;
				else
					j += 12 + exif.size;
			}
			else if (exif.type == EXIFType.UINT16)
			{
				i += 12;
				if (exif.size <= 2)
					j += 12;
				else
					j += 12 + (exif.size << 1);
			}
			else if (exif.type == EXIFType.UINT32)
			{
				i += 12;
				if (exif.size <= 1)
					j += 12;
				else
					j += 12 + (exif.size << 2);
			}
			else if (exif.type == EXIFType.RATIONAL)
			{
				i += 12;
				j += 12 + (exif.size << 3);
			}
			else if (exif.type == EXIFType.OTHER)
			{
				i += 12;
				if (exif.size <= 4)
					j += 12;
				else
					j += 12 + exif.size;
			}
			else if (exif.type == EXIFType.INT16)
			{
				i += 12;
				if (exif.size <= 2)
					j += 12;
				else
					j += 12 + (exif.size << 1);
			}
			else if (exif.type == EXIFType.SUBEXIF)
			{
				i += 12;
				j += 12;
				exif.subExif.getExifBuffSize(l, m);
				i += m.value;
				j += l.value;
			}
			else if (exif.type == EXIFType.DOUBLE)
			{
				i += 12;
				j += 12 + (exif.size << 3);
			}
		}
		size.value = j;
		endOfst.value = i;
	}

	public EXIFData(EXIFMaker exifMaker)
	{
		this.exifMaker = exifMaker;
		this.exifMap = new HashMap<Integer, EXIFItem>();
	}

	public EXIFMaker getEXIFMaker()
	{
		return this.exifMaker;
	}

	public EXIFData clone()
	{
		EXIFItem item;
		Iterator<EXIFItem> itItems = this.exifMap.values().iterator();
		EXIFData newExif;
		newExif = new EXIFData(this.exifMaker);
		while (itItems.hasNext())
		{
			item = itItems.next();
			switch (item.type)
			{
			case BYTES:
				newExif.addBytes(item.id, item.size, item.dataBuff, 0);
				break;
			case STRING:
				newExif.addString(item.id, item.size, item.dataBuff, 0);
				break;
			case UINT16:
				newExif.addUInt16(item.id, item.size, item.dataBuff, 0);
				break;
			case UINT32:
				newExif.addUInt32(item.id, item.size, item.dataBuff, 0);
				break;
			case RATIONAL:
				newExif.addRational(item.id, item.size, item.dataBuff, 0);
				break;
			case OTHER:
				newExif.addOther(item.id, item.size, item.dataBuff, 0);
				break;
			case INT16:
				newExif.addInt16(item.id, item.size, item.dataBuff, 0);
				break;
			case SUBEXIF:
				newExif.addSubEXIF(item.id, item.subExif.clone());
				break;
			case DOUBLE:
				newExif.addDouble(item.id, item.size, item.dataBuff, 0);
				break;
			case UNKNOWN:
			default:
				break;
			}
		}
		return newExif;
	}

	private void addItem(int id, int cnt, byte[] buff, int buffOfst, EXIFType type, int itemSize)
	{
		EXIFItem item = new EXIFItem();
		item.id = id;
		item.type = type;
		item.size = cnt;
		item.dataBuff = new byte[cnt * itemSize];
		ByteTool.copyArray(item.dataBuff, 0, buff, buffOfst, cnt * itemSize);
		item = this.exifMap.put(id, item);
	}

	public void addBytes(int id, int cnt, byte[] buff, int buffOfst)
	{
		addItem(id, cnt, buff, buffOfst, EXIFType.BYTES, 1);
	}

	public void addString(int id, int cnt, byte[] buff, int buffOfst)
	{
		addItem(id, cnt, buff, buffOfst, EXIFType.STRING, 1);
	}

	public void addUInt16(int id, int cnt, byte[] buff, int buffOfst)
	{
		addItem(id, cnt, buff, buffOfst, EXIFType.UINT16, 2);
	}

	public void addUInt32(int id, int cnt, byte[] buff, int buffOfst)
	{
		addItem(id, cnt, buff, buffOfst, EXIFType.UINT32, 4);
	}
	public void addRational(int id, int cnt, byte[] buff, int buffOfst)
	{
		addItem(id, cnt, buff, buffOfst, EXIFType.RATIONAL, 8);
	}

	public void addOther(int id, int cnt, byte[] buff, int buffOfst)
	{
		addItem(id, cnt, buff, buffOfst, EXIFType.OTHER, 1);
	}

	public void addInt16(int id, int cnt, byte[] buff, int buffOfst)
	{
		addItem(id, cnt, buff, buffOfst, EXIFType.INT16, 2);
	}

	public void addSubEXIF(int id, EXIFData exif)
	{
		EXIFItem item = new EXIFItem();
		item.id = id;
		item.type = EXIFType.SUBEXIF;
		item.size = 1;
		item.dataBuff = null;
		item.subExif = exif;
		item = this.exifMap.put(id, item);
	}

	public void addDouble(int id, int cnt, byte[] buff, int buffOfst)
	{
		addItem(id, cnt, buff, buffOfst, EXIFType.DOUBLE, 8);
	}

	public void remove(int id)
	{
		this.exifMap.remove(id);
	}

	public int getExifIds(List<Integer> idArr)
	{
		idArr.addAll(this.exifMap.keySet());
		return this.exifMap.size();
	}

	public EXIFType getExifType(int id)
	{
		EXIFItem item = this.exifMap.get(id);
		if (item == null)
			return EXIFType.UNKNOWN;
		return item.type;
	}

	public int getExifCount(int id)
	{
		EXIFItem item = this.exifMap.get(id);
		if (item == null)
			return 0;
		return item.size;
	}

	public EXIFItem getExifItem(int id)
	{
		return this.exifMap.get(id);
	}

	public byte[] getExifUInt16(int id)
	{
		EXIFItem item = this.exifMap.get(id);
		if (item == null)
			return null;
		if (item.type != EXIFType.UINT16)
			return null;
		return item.dataBuff;
	}

	public byte[] getExifUInt32(int id)
	{
		EXIFItem item = this.exifMap.get(id);
		if (item == null)
			return null;
		if (item.type != EXIFType.UINT16)
			return null;
		return item.dataBuff;
	}

	public EXIFData getExifSubexif(int id)
	{
		EXIFItem item = this.exifMap.get(id);
		if (item == null)
			return null;
		if (item.type != EXIFType.SUBEXIF)
			return null;
		return item.subExif;
	}

	public byte[] getExifOther(int id)
	{
		EXIFItem item = this.exifMap.get(id);
		if (item == null)
			return null;
		if (item.type != EXIFType.OTHER)
			return null;
		return item.dataBuff;
	}

	public ZonedDateTime getPhotoDate()
	{
		EXIFItem item;
		if (this.exifMaker == EXIFMaker.STANDARD)
		{
			if ((item = this.exifMap.get(36867)) != null)
			{
				if (item.type == EXIFType.STRING)
				{
					return DateTimeUtil.parse(getItemString(item));
				}
			}
			if ((item = this.exifMap.get(36868)) != null)
			{
				if (item.type == EXIFType.STRING)
				{
					return DateTimeUtil.parse(getItemString(item));
				}
			}
			if ((item = this.exifMap.get(34665)) != null)
			{
				if (item.type == EXIFType.SUBEXIF)
				{
					ZonedDateTime zdt = item.subExif.getPhotoDate();
					if (zdt != null)
						return zdt;
				}
			}
			if ((item = this.exifMap.get(306)) != null)
			{
				if (item.type == EXIFType.STRING)
				{
					return DateTimeUtil.parse(getItemString(item));
				}
			}
		}
		return null;
	}

	public String getPhotoMake()
	{
		EXIFItem item;
		if (this.exifMaker == EXIFMaker.STANDARD)
		{
			if ((item = this.exifMap.get(271)) != null)
			{
				if (item.type == EXIFType.STRING)
				{
					return getItemString(item);
				}
			}
		}
		return null;
	}

	public String getPhotoModel()
	{
		EXIFItem item;
		if (this.exifMaker == EXIFMaker.STANDARD)
		{
			if ((item = this.exifMap.get(272)) != null)
			{
				if (item.type == EXIFType.STRING)
				{
					return getItemString(item);
				}
			}
		}
		if (this.exifMaker == EXIFMaker.CANON)
		{
			if ((item = this.exifMap.get(6)) != null)
			{
				if (item.type == EXIFType.STRING)
				{
					return getItemString(item);
				}
			}
		}
		return null;		
	}

	public String getPhotoLens()
	{
		EXIFItem item;
		if (this.exifMaker == EXIFMaker.CANON)
		{
			if ((item = this.exifMap.get(149)) != null)
			{
				if (item.type == EXIFType.STRING)
				{
					return getItemString(item);
				}
			}
		}
		if (this.exifMaker == EXIFMaker.PANASONIC)
		{
			if ((item = this.exifMap.get(81)) != null)
			{
				if (item.type == EXIFType.STRING)
				{
					return getItemString(item);
				}
			}
		}
		return null;
	}

	public double getPhotoFNumber()
	{
		EXIFItem item;
		if (this.exifMaker == EXIFMaker.STANDARD)
		{
			if ((item = this.exifMap.get(33437)) != null)
			{
				if (item.type == EXIFType.RATIONAL && item.size == 1)
				{
					return ByteTool.readInt32(item.dataBuff, 0) / (double)ByteTool.readInt32(item.dataBuff, 4);
				}
			}
			if ((item = this.exifMap.get(34665)) != null)
			{
				if (item.type == EXIFType.SUBEXIF)
				{
					return item.subExif.getPhotoFNumber();
				}
			}
		}
		return 0;
	}

	public double getPhotoExpTime()
	{
		EXIFItem item;
		if (this.exifMaker == EXIFMaker.STANDARD)
		{
			if ((item = this.exifMap.get(33434)) != null)
			{
				if (item.type == EXIFType.RATIONAL && item.size == 1)
				{
					return ByteTool.readInt32(item.dataBuff, 0) / (double)ByteTool.readInt32(item.dataBuff, 4);
				}
			}
			if ((item = this.exifMap.get(34665)) != null)
			{
				if (item.type == EXIFType.SUBEXIF)
				{
					return item.subExif.getPhotoExpTime();
				}
			}
		}
		return 0;
	}

	public int getPhotoISO()
	{
		EXIFItem item;
		if (this.exifMaker == EXIFMaker.STANDARD)
		{
			if ((item = this.exifMap.get(34855)) != null)
			{
				if (item.type == EXIFType.UINT16 && item.size == 1)
				{
					return ByteTool.readInt16(item.dataBuff, 0);
				}
				else if (item.type == EXIFType.UINT32 && item.size == 1)
				{
					return ByteTool.readInt32(item.dataBuff, 0);
				}
			}
			if ((item = this.exifMap.get(34665)) != null)
			{
				if (item.type == EXIFType.SUBEXIF)
				{
					return item.subExif.getPhotoISO();
				}
			}
		}
		return 0;		
	}

	public double getPhotoFocalLength()
	{
		EXIFItem item;
		if (this.exifMaker == EXIFMaker.STANDARD)
		{
			if ((item = this.exifMap.get(37386)) != null)
			{
				if (item.type == EXIFType.RATIONAL && item.size == 1)
				{
					return ByteTool.readInt32(item.dataBuff, 0) / (double)ByteTool.readInt32(item.dataBuff, 4);
				}
			}
			if ((item = this.exifMap.get(34665)) != null)
			{
				if (item.type == EXIFType.SUBEXIF)
				{
					return item.subExif.getPhotoFocalLength();
				}
			}
		}
		return 0;
	}

	public boolean getPhotoLocation(SharedDouble lat, SharedDouble lon, SharedDouble altitude, SharedLong gpsTimeTick)
	{
		EXIFData subExif = this.getExifSubexif(34853);
		if (subExif != null)
		{
			boolean succ = true;
	/*
		{1, L"GPSLatitudeRef"},
		{2, L"GPSLatitude"},
		{3, L"GPSLongitudeRef"},
		{4, L"GPSLongitude"},
		{5, L"GPSAltitudeRef"},
		{6, L"GPSAltitude"},
		{7, L"GPSTimeStamp"},*/
			EXIFItem item1;
			EXIFItem item2;
			double val = 0;
			item1 = subExif.getExifItem(1);
			item2 = subExif.getExifItem(2);
			if (item1 != null && item2 != null)
			{
				if (item2.type == EXIFType.RATIONAL)
				{
					if (item2.size == 3)
					{
						val = ByteTool.readInt32(item2.dataBuff, 0) / (double)ByteTool.readInt32(item2.dataBuff, 4);
						val += ByteTool.readInt32(item2.dataBuff, 8) / (double)ByteTool.readInt32(item2.dataBuff, 12) / 60.0;
						val += ByteTool.readInt32(item2.dataBuff, 16) / (double)ByteTool.readInt32(item2.dataBuff, 20) / 3600.0;
					}
					else if (item2.size == 1)
					{
						val = ByteTool.readInt32(item2.dataBuff, 0) / (double)ByteTool.readInt32(item2.dataBuff, 4);
					}
					else
					{
						succ = false;
					}
				}
				else
				{
					succ = false;
				}
				if (item1.type == EXIFType.STRING)
				{
					if (getItemString(item1).equals("S"))
					{
						val = -val;
					}
				}
				else
				{
					succ = false;
				}
				if (lat != null)
				{
					lat.value = val;
				}
			}
			else
			{
				succ = false;
			}
			item1 = subExif.getExifItem(3);
			item2 = subExif.getExifItem(4);
			if (item1 != null && item2 != null)
			{
				if (item2.type == EXIFType.RATIONAL)
				{
					if (item2.size == 3)
					{
						val = ByteTool.readInt32(item2.dataBuff, 0) / (double)ByteTool.readInt32(item2.dataBuff, 4);
						val += ByteTool.readInt32(item2.dataBuff, 8) / (double)ByteTool.readInt32(item2.dataBuff, 12) / 60.0;
						val += ByteTool.readInt32(item2.dataBuff, 16) / (double)ByteTool.readInt32(item2.dataBuff, 20) / 3600.0;
					}
					else if (item2.size == 1)
					{
						val = ByteTool.readInt32(item2.dataBuff, 0) / (double)ByteTool.readInt32(item2.dataBuff, 4);
					}
					else
					{
						succ = false;
					}
				}
				else
				{
					succ = false;
				}
				if (item1.type == EXIFType.STRING)
				{
					if (getItemString(item1).equals("W"))
					{
						val = -val;
					}
				}
				else
				{
					succ = false;
				}
				if (lon != null)
				{
					lon.value = val;
				}
			}
			else
			{
				succ = false;
			}
			item1 = subExif.getExifItem(5);
			item2 = subExif.getExifItem(6);
			if (item1 != null && item2 != null)
			{
				if (item2.type == EXIFType.RATIONAL)
				{
					if (item2.size == 1)
					{
						val = ByteTool.readInt32(item2.dataBuff, 0) / (double)ByteTool.readInt32(item2.dataBuff, 4);
					}
					else
					{
						succ = false;
					}
				}
				else
				{
					succ = false;
				}
				if (item1.type == EXIFType.BYTES && item1.size == 1)
				{
					if (item1.dataBuff[0] == 1)
					{
						val = -val;
					}
				}
				else
				{
					succ = false;
				}
				if (altitude != null)
				{
					altitude.value = val;
				}
			}
			else
			{
				succ = false;
			}
			item1 = subExif.getExifItem(7);
			item2 = subExif.getExifItem(29);
			if (item1 != null && item2 != null)
			{
				int hh = 0;
				int mm = 0;
				int ss = 0;
				int ms = 0;
	
				if (item1.type == EXIFType.RATIONAL && item1.size == 3)
				{
					if (ByteTool.readInt32(item1.dataBuff, 4) != 1 || ByteTool.readInt32(item1.dataBuff, 12) != 1)
					{
						succ = false;
					}
					else
					{
						hh = ByteTool.readInt32(item1.dataBuff, 0);
						mm = ByteTool.readInt32(item1.dataBuff, 8);
						val = ByteTool.readInt32(item1.dataBuff, 16) / (double)ByteTool.readInt32(item1.dataBuff, 20);
						ss = (int)val;
						ms = (int)((val - ss) * 1000);
					}
				}
				else
				{
					succ = false;
				}
				if (item2.type == EXIFType.STRING && item2.size == 11)
				{
					String[] dateArr = StringUtil.split(getItemString(item2), ":");
					if (dateArr.length != 3)
					{
						succ = false;
					}
					else if (gpsTimeTick != null)
					{
						ZonedDateTime dt = ZonedDateTime.of(StringUtil.toIntegerS(dateArr[0], 0), StringUtil.toIntegerS(dateArr[1], 0), StringUtil.toIntegerS(dateArr[2], 0), hh, mm, ss, ms * 1000, ZoneId.systemDefault());
						gpsTimeTick.value = DateTimeUtil.getTimeMillis(dt);
					}
				}
				else
				{
					succ = false;
				}
			}
			else
			{
				succ = false;
			}
			return succ;
		}
		return false;
	}

	public boolean getGeoBounds(int imgW, int imgH, SharedInt srid, SharedDouble minX, SharedDouble minY, SharedDouble maxX, SharedDouble maxY)
	{
		EXIFItem item;
		EXIFItem item2;
		item = this.exifMap.get(33922);
		item2 = this.exifMap.get(33550);
		if (item == null || item2 == null)
			return false;
	
		if (srid != null)
		{
			srid.value = 0;
		}
	//	Math::CoordinateSystem *coord = Math::CoordinateSystemManager::CreateGeogCoordinateSystemDefName(Math::GeographicCoordinateSystem::GCST_WGS84);
		byte[] ptr = item.dataBuff;
		Double imgX = ByteTool.readDouble(ptr, 0);
		Double imgY = ByteTool.readDouble(ptr, 8);
		Double mapX = ByteTool.readDouble(ptr, 24);
		Double mapY = ByteTool.readDouble(ptr, 32);
		Double mppX;
		Double mppY;
	
		ptr = item2.dataBuff;
		mppX = ByteTool.readDouble(ptr, 0);
		mppY = ByteTool.readDouble(ptr, 8);
	
	/*	*minX = coord.CalLonByDist(mapY, mapX, -imgX * mppX);
		*maxY = coord.CalLatByDist(mapY, imgY * mppY);
		*maxX = coord.CalLonByDist(mapY, mapX, (imgW - imgX) * mppX);
		*minY = coord.CalLatByDist(mapY, (imgH - imgY) * mppY);*/
		minX.value = mapX - imgX * mppX;
		maxY.value = mapY + imgY * mppY;
		maxX.value = mapX + ((double)imgW - imgX) * mppX;
		minY.value = mapY - ((double)imgH - imgY) * mppY;
	
	//	DEL_CLASS(coord);
		return true;		
	}

	public RotateType getRotateType()
	{
		EXIFItem item;
		item = this.exifMap.get(274);
		if (item == null)
		{
			return RotateType.NONE;
		}
		int v;
		if (item.type == EXIFType.UINT16)
		{
			v = ByteTool.readUInt16(item.dataBuff, 0);
		}
		else
		{
			v = ByteTool.readInt32(item.dataBuff, 0);
		}
		if (v == 6)
		{
			return RotateType.CW90;
		}
		else if (v == 3)
		{
			return RotateType.CW180;
		}
		else if (v == 8)
		{
			return RotateType.CW270;
		}
		return RotateType.NONE;
	}

	public double getHDPI()
	{
		EXIFItem item;
		item = this.exifMap.get(282);
		if (item == null)
		{
			return 0;
		}
		if (item.type != EXIFType.RATIONAL)
		{
			return 0;
		}
		return ByteTool.readInt32(item.dataBuff, 0) / (double)ByteTool.readInt32(item.dataBuff, 4);
	}

	public double getVDPI()
	{
		EXIFItem item;
		item = this.exifMap.get(283);
		if (item == null)
		{
			return 0;
		}
		if (item.type != EXIFType.RATIONAL)
		{
			return 0;
		}
		return ByteTool.readInt32(item.dataBuff, 0) / (double)ByteTool.readInt32(item.dataBuff, 4);
	}

	public void setWidth(int width)
	{
		byte[] bwidth = new byte[4];
		ByteTool.writeInt32(bwidth, 0, width);
		this.addUInt32(256, 1, bwidth, 0);

		EXIFItem item;
		item = this.exifMap.get(34665);
		if (item != null && item.type == EXIFType.SUBEXIF)
		{
			item.subExif.addUInt32(40962, 1, bwidth, 0);
		}
	}

	public void setHeight(int height)
	{
		byte[] bheight = new byte[4];
		ByteTool.writeInt32(bheight, 0, height);
		this.addUInt32(257, 1, bheight, 0);

		EXIFItem item;
		item = this.exifMap.get(34665);
		if (item != null && item.type == EXIFType.SUBEXIF)
		{
			item.subExif.addUInt32(40963, 1, bheight, 0);
		}
	}

	public boolean toString(StringBuilder sb, String linePrefix)
	{
		List<Integer> exifIds = new ArrayList<Integer>();
		EXIFItem exItem;
		int i;
		int j;
		int k;
		int v;
	
		sb.append("EXIF Content:");
		this.getExifIds(exifIds);
		ArtificialQuickSort.sort(exifIds);
		i = 0;
		j = exifIds.size();
		while (i < j)
		{
			v = exifIds.get(i);
			sb.append("\r\n");
			if (linePrefix != null)
				sb.append(linePrefix);
			sb.append("Id = ");
			sb.append(v);
			sb.append(", name = ");
			sb.append(getEXIFName(this.exifMaker, v));
			exItem = this.getExifItem(v);

			if (v == 34665)
			{
				v = 34665;
			}

			if (exItem.type == EXIFType.SUBEXIF)
			{
				List<Integer> subExIds = new ArrayList<Integer>();
				int i2;
				int j2;
				int v2;
				EXIFItem subExItem;
				EXIFData subExif = exItem.subExif;
				i2 = 0;
				j2 = subExif.getExifIds(subExIds);
				while (i2 < j2)
				{
					v2 = subExIds.get(i2);
					sb.append("\r\n");
					if (linePrefix != null)
						sb.append(linePrefix);
					sb.append(" Subid = ");
					sb.append(v2);
					sb.append(", name = ");
					sb.append(getEXIFName(this.exifMaker, v, v2));
	
					subExItem = subExif.getExifItem(v2);
					if (subExItem.type == EXIFType.STRING)
					{
						sb.append(", value = ");
						sb.append(getItemString(subExItem));
					}
					else if (subExItem.type == EXIFType.DOUBLE)
					{
						k = 0;
						while (k < subExItem.size)
						{
							if (k == 0)
							{
								sb.append(", value = ");
							}
							else
							{
								sb.append(", ");
							}
							sb.append(StringUtil.fromDouble(ByteTool.readDouble(subExItem.dataBuff, k * 8)));
							k++;
						}
					}
					else if (subExItem.type == EXIFType.BYTES)
					{
						byte[] valBuff = subExItem.dataBuff;
						sb.append(", value = ");
						if (subExItem.size > 1024)
						{
							sb.append(subExItem.size);
							sb.append(" bytes: ");
							StringUtil.appendHex(sb, valBuff, 0, 256, ' ', LineBreakType.CRLF);
							sb.append("\r\n...\r\n");
							StringUtil.appendHex(sb, valBuff, (subExItem.size & ~15) - 256, 256 + (subExItem.size & 15), ' ', LineBreakType.CRLF);
						}
						else
						{
							StringUtil.appendHex(sb, valBuff, 0, subExItem.size, ' ', LineBreakType.CRLF);
						}
					}
					else if (subExItem.type == EXIFType.UINT16)
					{
						byte[] valBuff = subExItem.dataBuff;
						k = 0;
						while (k < subExItem.size)
						{
							if (k == 0)
							{
								sb.append(", value = ");
							}
							else
							{
								sb.append(", ");
							}
							sb.append(ByteTool.readUInt16(valBuff, k * 2));
							k++;
						}
					}
					else if (subExItem.type == EXIFType.UINT32)
					{
						byte[] valBuff = subExItem.dataBuff;
						k = 0;
						while (k < subExItem.size)
						{
							if (k == 0)
							{
								sb.append(", value = ");
							}
							else
							{
								sb.append(", ");
							}
							sb.append(ByteTool.readInt32(valBuff, k * 4));
							k++;
						}
					}
					else if (subExItem.type == EXIFType.RATIONAL)
					{
						byte[] valBuff;
						valBuff = subExItem.dataBuff;
						k = 0;
						while (k < subExItem.size)
						{
							if (k == 0)
							{
								sb.append(", value = ");
							}
							else
							{
								sb.append(", ");
							}
							sb.append(ByteTool.readInt32(valBuff, k * 8));
							sb.append(" / ");
							sb.append(ByteTool.readInt32(valBuff, k * 8 + 4));
							if (ByteTool.readInt32(valBuff, k * 8 + 4) != 0)
							{
								sb.append(" (");
								sb.append(ByteTool.readInt32(valBuff, k * 8) / (double)ByteTool.readInt32(valBuff, k * 8 + 4));
								sb.append(")");
							}
							k++;
						}
					}
					else if (subExItem.type == EXIFType.INT16)
					{
						byte[] valBuff = subExItem.dataBuff;
						k = 0;
						while (k < subExItem.size)
						{
							if (k == 0)
							{
								sb.append(", value = ");
							}
							else
							{
								sb.append(", ");
							}
							sb.append(ByteTool.readInt16(valBuff, k * 2));
							k++;
						}
					}
					else if (subExItem.id == 37500)
					{
						byte[] valBuff = subExItem.dataBuff;
						EXIFData innerExif = parseMakerNote(valBuff, 0, subExItem.size);
						if (innerExif != null)
						{
							String thisPrefix;
							sb.append(", Format = ");
							sb.append(getEXIFMakerName(innerExif.getEXIFMaker()));
							sb.append(", Inner ");
							if (linePrefix != null)
							{
								thisPrefix = "  " + linePrefix;
							}
							else
							{
								thisPrefix = "  ";
							}
							innerExif.toString(sb, thisPrefix);
						}
						else
						{
							sb.append(", value (Other) = ");
							StringUtil.appendHex(sb, valBuff, 0, subExItem.size, ' ', LineBreakType.CRLF);
						}
					}
					else if (subExItem.type == EXIFType.OTHER)
					{
						byte[] valBuff;
						valBuff = subExItem.dataBuff;
						if (this.exifMaker == EXIFMaker.OLYMPUS && subExItem.id == 0)
						{
							sb.append(", value = ");
							sb.append(getItemString(subExItem));
						}
						else
						{
							sb.append(", value (Other) = ");
							StringUtil.appendHex(sb, valBuff, 0, subExItem.size, ' ', LineBreakType.CRLF);
						}
					}
					else
					{
						byte[] valBuff = subExItem.dataBuff;
						sb.append(", value (Unk) = ");
						StringUtil.appendHex(sb, valBuff, 0, subExItem.size, ' ', LineBreakType.CRLF);
					}
					
					i2++;
				}
			}
			else if (exItem.type == EXIFType.STRING)
			{
				sb.append(", value = ");
				sb.append(getItemString(exItem));
			}
			else if (exItem.type == EXIFType.DOUBLE)
			{
				byte[] valBuff = exItem.dataBuff;
				k = 0;
				while (k < exItem.size)
				{
					if (k == 0)
					{
						sb.append(", value = ");
					}
					else
					{
						sb.append(", ");
					}
					sb.append(ByteTool.readDouble(valBuff, k * 8));
					k++;
				}
			}
			else if (exItem.type == EXIFType.BYTES)
			{
				byte[] valBuff = exItem.dataBuff;
				sb.append(", value = ");
				if (exItem.id >= 40091 && exItem.id <= 40095)
				{
					if (valBuff[exItem.size - 2] == 0)
					{
						sb.append(StringUtil.fromUTF8Z(valBuff, 0));
					}
					else
					{
						sb.append(new String(valBuff, 0, exItem.size, StandardCharsets.UTF_16LE));
					}
				}
				else
				{
					if (exItem.size > 1024)
					{
						sb.append(exItem.size);
						sb.append(" bytes: ");
						StringUtil.appendHex(sb, valBuff, 0, 256, ' ', LineBreakType.CRLF);
						sb.append("\r\n...\r\n");
						StringUtil.appendHex(sb, valBuff, (exItem.size & ~15) - 256, 256 + (exItem.size & 15), ' ', LineBreakType.CRLF);
					}
					else
					{
						StringUtil.appendHex(sb, valBuff, 0, exItem.size, ' ', LineBreakType.CRLF);
					}
				}
			}
			else if (exItem.type == EXIFType.UINT16)
			{
				byte[] valBuff = exItem.dataBuff;
				if (this.exifMaker == EXIFMaker.CANON && exItem.id == 1)
				{
					this.toStringCanonCameraSettings(sb, linePrefix, valBuff, 0, exItem.size);
				}
				else if (this.exifMaker == EXIFMaker.CANON && exItem.id == 2)
				{
					this.toStringCanonFocalLength(sb, linePrefix, valBuff, 0, exItem.size);
				}
				else if (this.exifMaker == EXIFMaker.CANON && exItem.id == 4)
				{
					this.toStringCanonShotInfo(sb, linePrefix, valBuff, 0, exItem.size);
				}
				else
				{
					k = 0;
					while (k < exItem.size)
					{
						if (k == 0)
						{
							sb.append(", value = ");
						}
						else
						{
							sb.append(", ");
						}
						sb.append(ByteTool.readUInt16(valBuff, k * 2));
						k++;
					}
				}
			}
			else if (exItem.type == EXIFType.UINT32)
			{
				byte[] valBuff = exItem.dataBuff;
				k = 0;
				while (k < exItem.size)
				{
					if (k == 0)
					{
						sb.append(", value = ");
					}
					else
					{
						sb.append(", ");
					}
					sb.append(ByteTool.readInt32(valBuff, k));
					k++;
				}
			}
			else if (exItem.type == EXIFType.RATIONAL)
			{
				byte[] valBuff = exItem.dataBuff;
				k = 0;
				while (k < exItem.size)
				{
					if (k == 0)
					{
						sb.append(", value = ");
					}
					else
					{
						sb.append(", ");
					}
					sb.append(ByteTool.readInt32(valBuff, k * 8));
					sb.append(" / ");
					sb.append(ByteTool.readInt32(valBuff, k * 8 + 4));
					if (ByteTool.readInt32(valBuff, k * 8 + 4) != 0)
					{
						sb.append(" (");
						sb.append(ByteTool.readInt32(valBuff, k * 8) / (double)ByteTool.readInt32(valBuff, k * 8 + 4));
						sb.append(")");
					}
					k++;
				}
			}
			else if (exItem.type == EXIFType.INT16)
			{
				byte[] valBuff = exItem.dataBuff;
				k = 0;
				while (k < exItem.size)
				{
					if (k == 0)
					{
						sb.append(", value = ");
					}
					else
					{
						sb.append(", ");
					}
					sb.append(ByteTool.readInt16(valBuff, k * 2));
					k++;
				}
			}
			else if (exItem.type == EXIFType.OTHER)
			{
				if (this.exifMaker == EXIFMaker.OLYMPUS && exItem.id == 521)
				{
					sb.append(", value = ");
					sb.append(getItemString(exItem));
				}
				else
				{
		//			UInt8 *valBuff;
		//			valBuff = (UInt8*)exItem.dataBuff;
					sb.append(", Other: size = ");
					sb.append(exItem.size);
		//			sb.AppendHex(valBuff, subExItem.size, ' ', Text::StringBuilder::LBT_CRLF);
				}
			}
			else
			{
	/*			UInt8 *valBuff;
				if (exItem.size <= 4)
				{
					valBuff = (UInt8*)&exItem.value;
				}
				else
				{
					valBuff = (UInt8*)exItem.dataBuff;
				}*/
				sb.append(", Unknown: size = ");
				sb.append(exItem.size);
	//			sb.AppendHex(valBuff, subExItem.size, ' ', Text::StringBuilder::LBT_CRLF);
			}
	
			i++;
		}
		return true;
	}

	public boolean toStringCanonCameraSettings(StringBuilder sb, String linePrefix, byte[] valBuff, int valOfst, int valCnt)
	{
		boolean isInt16;
		boolean isUInt16;
		int k;
		k = 0;
		while (k < valCnt)
		{
			sb.append("\r\n");
			if (linePrefix != null)
				sb.append(linePrefix);
			sb.append(" ");
			isInt16 = false;
			isUInt16 = false;
			switch (k)
			{
			case 1:
				sb.append("MacroMode = ");
				switch (ByteTool.readInt16(valBuff, valOfst + k * 2))
				{
				case 1:
					sb.append("1-Macro");
					break;
				case 2:
					sb.append("2-Normal");
					break;
				default:
					sb.append(ByteTool.readInt16(valBuff, valOfst + k * 2));
					break;
				}
				break;
			case 2:
				sb.append("SelfTimer = ");
				isInt16 = true;
				break;
			case 3:
				sb.append("Quality = ");
				switch (ByteTool.readInt16(valBuff, valOfst + k * 2))
				{
				case -1:
					sb.append("-1-n/a");
					break;
				case 1:
					sb.append("1-Economy");
					break;
				case 2:
					sb.append("2-Normal");
					break;
				case 3:
					sb.append("3-Fine");
					break;
				case 4:
					sb.append("4-RAW");
					break;
				case 5:
					sb.append("5-Superfine");
					break;
				case 7:
					sb.append("7-CRAW");
					break;
				case 130:
					sb.append("130-Normal Movie");
					break;
				case 131:
					sb.append("131-Movie (2)");
					break;
				default:
					sb.append(ByteTool.readInt16(valBuff, valOfst + k * 2));
					break;
				}
				break;
			case 4:
				sb.append("CanonFlashMode = ");
				switch (ByteTool.readInt16(valBuff, valOfst + k * 2))
				{
				case -1:
					sb.append("-1-n/a");
					break;
				case 0:
					sb.append("0-Off");
					break;
				case 1:
					sb.append("1-Auto");
					break;
				case 2:
					sb.append("2-On");
					break;
				case 3:
					sb.append("3-Red-eye Reduction");
					break;
				case 4:
					sb.append("4-Slow Sync");
					break;
				case 5:
					sb.append("5-Red-eye Reduction (Auto)");
					break;
				case 6:
					sb.append("6-Red-eye Reduction (On)");
					break;
				case 16:
					sb.append("16-External Flash");
					break;
				default:
					sb.append(ByteTool.readInt16(valBuff, valOfst + k * 2));
					break;
				}
				break;
			case 5:
				sb.append("ContinuousDrive = ");
				switch (ByteTool.readInt16(valBuff, valOfst + k * 2))
				{
				case 0:
					sb.append("0-Single");
					break;
				case 1:
					sb.append("1-Continuous");
					break;
				case 2:
					sb.append("2-Movie");
					break;
				case 3:
					sb.append("3-Continuous, Speed Priority");
					break;
				case 4:
					sb.append("4-Continuous, Low");
					break;
				case 5:
					sb.append("5-Continuous, High");
					break;
				case 6:
					sb.append("6-Silent Single");
					break;
				case 9:
					sb.append("9-Single, Silent");
					break;
				case 10:
					sb.append("10-Continuous, Silent");
					break;
				default:
					sb.append(ByteTool.readInt16(valBuff, valOfst + k * 2));
					break;
				}
				break;
			case 7:
				sb.append("FocusMode = ");
				switch (ByteTool.readInt16(valBuff, valOfst + k * 2))
				{
				case 0:
					sb.append("0-One-shot AF");
					break;
				case 1:
					sb.append("1-AI Servo AF");
					break;
				case 2:
					sb.append("2-AI Focus AF");
					break;
				case 3:
					sb.append("3-Manual Focus");
					break;
				case 4:
					sb.append("4-Single");
					break;
				case 5:
					sb.append("5-Continuous");
					break;
				case 6:
					sb.append("6-Manual Focus");
					break;
				case 16:
					sb.append("16-Pan Focus");
					break;
				case 256:
					sb.append("256-AF+MF");
					break;
				case 512:
					sb.append("512-Movie Snap Focus");
					break;
				case 519:
					sb.append("519-Movie Servo AF");
					break;
				default:
					sb.append(ByteTool.readInt16(valBuff, valOfst + k * 2));
					break;
				}
				break;
			case 9:
				sb.append("RecordMode = ");
				switch (ByteTool.readInt16(valBuff, valOfst + k * 2))
				{
				case 1:
					sb.append("1-JPEG");
					break;
				case 2:
					sb.append("2-CRW+THM");
					break;
				case 3:
					sb.append("3-AVI+THM");
					break;
				case 4:
					sb.append("4-TIF");
					break;
				case 5:
					sb.append("5-TIF+JPEG");
					break;
				case 6:
					sb.append("6-CR2");
					break;
				case 7:
					sb.append("7-CR2+JPEG");
					break;
				case 9:
					sb.append("9-MOV");
					break;
				case 10:
					sb.append("10-MP4");
					break;
				case 11:
					sb.append("11-CRM");
					break;
				case 12:
					sb.append("12-CR3");
					break;
				case 13:
					sb.append("13-CR3+JPEG");
					break;
				default:
					sb.append(ByteTool.readInt16(valBuff, valOfst + k * 2));
					break;
				}
				break;
			case 10:
				sb.append("CanonImageSize = ");
				switch (ByteTool.readInt16(valBuff, valOfst + k * 2))
				{
				case -1:
					sb.append("-1-n/a");
					break;
				case 0:
					sb.append("0-Large");
					break;
				case 1:
					sb.append("1-Medium");
					break;
				case 2:
					sb.append("2-Small");
					break;
				case 5:
					sb.append("5-Medium 1");
					break;
				case 6:
					sb.append("6-Medium 2");
					break;
				case 7:
					sb.append("7-Medium 3");
					break;
				case 8:
					sb.append("8-Postcard");
					break;
				case 9:
					sb.append("9-Widescreen");
					break;
				case 10:
					sb.append("10-Medium Widescreen");
					break;
				case 14:
					sb.append("14-Small 1");
					break;
				case 15:
					sb.append("15-Small 2");
					break;
				case 16:
					sb.append("16-Small 3");
					break;
				case 128:
					sb.append("128-640x480 Movie");
					break;
				case 129:
					sb.append("129-Medium Movie");
					break;
				case 130:
					sb.append("130-Small Movie");
					break;
				case 137:
					sb.append("137-1280x720 Movie");
					break;
				case 142:
					sb.append("142-1920x1080 Movie");
					break;
				case 143:
					sb.append("143-4096x2160 Movie");
					break;
				default:
					sb.append(ByteTool.readInt16(valBuff, valOfst + k * 2));
					break;
				}
				break;
			case 11:
				sb.append("EasyMode = ");
				switch (ByteTool.readInt16(valBuff, valOfst + k * 2))
				{
				case 0:
					sb.append("0-Full auto");
					break;
				case 1:
					sb.append("1-Manual");
					break;
				case 2:
					sb.append("2-Landscape");
					break;
				case 3:
					sb.append("3-Fast shutter");
					break;
				case 4:
					sb.append("4-Slow shutter");
					break;
				case 5:
					sb.append("5-Night");
					break;
				case 6:
					sb.append("6-Grey Scale");
					break;
				case 7:
					sb.append("7-Sepia");
					break;
				case 8:
					sb.append("8-Portrait");
					break;
				case 9:
					sb.append("9-Sports");
					break;
				case 10:
					sb.append("10-Macro");
					break;
				case 11:
					sb.append("11-Black & White");
					break;
				case 12:
					sb.append("12-Pan focus");
					break;
				case 13:
					sb.append("13-Vivid");
					break;
				case 14:
					sb.append("14-Neutral");
					break;
				case 15:
					sb.append("15-Flash Off");
					break;
				case 16:
					sb.append("16-Long Shutter");
					break;
				case 17:
					sb.append("17-Super Macro");
					break;
				case 18:
					sb.append("18-Foliage");
					break;
				case 19:
					sb.append("19-Indoor");
					break;
				case 20:
					sb.append("20-Fireworks");
					break;
				case 21:
					sb.append("21-Beach");
					break;
				case 22:
					sb.append("22-Underwater");
					break;
				case 23:
					sb.append("23-Snow");
					break;
				case 24:
					sb.append("24-Kids & Pets");
					break;
				case 25:
					sb.append("25-Night Snapshot");
					break;
				case 26:
					sb.append("26-Digital Macro");
					break;
				case 27:
					sb.append("27-My Colors");
					break;
				case 28:
					sb.append("28-Movie Snap");
					break;
				case 29:
					sb.append("29-Super Macro 2");
					break;
				case 30:
					sb.append("30-Color Accent");
					break;
				case 31:
					sb.append("31-Color Swap");
					break;
				case 32:
					sb.append("32-Aquarium");
					break;
				case 33:
					sb.append("33-ISO 3200");
					break;
				case 34:
					sb.append("34-ISO 6400");
					break;
				case 35:
					sb.append("35-Creative Light Effect");
					break;
				case 36:
					sb.append("36-Easy");
					break;
				case 37:
					sb.append("37-Quick Shot");
					break;
				case 38:
					sb.append("38-Creative Auto");
					break;
				case 39:
					sb.append("39-Zoom Blur");
					break;
				case 40:
					sb.append("40-Low Light");
					break;
				case 41:
					sb.append("41-Nostalgic");
					break;
				case 42:
					sb.append("42-Super Vivid");
					break;
				case 43:
					sb.append("43-Poster Effect");
					break;
				case 44:
					sb.append("44-Face Self-Time");
					break;
				case 45:
					sb.append("45-Smile");
					break;
				case 46:
					sb.append("46-Wink Self-Timer");
					break;
				case 47:
					sb.append("47-Fisheye Effect");
					break;
				case 48:
					sb.append("48-Miniature Effect");
					break;
				case 49:
					sb.append("49-High-speed Burst");
					break;
				case 50:
					sb.append("50-Best Image Selection");
					break;
				case 51:
					sb.append("51-High Dynamic Range");
					break;
				case 52:
					sb.append("52-Handheld Night Scene");
					break;
				case 53:
					sb.append("53-Movie Digest");
					break;
				case 54:
					sb.append("54-Live View Control");
					break;
				case 55:
					sb.append("55-Discreet");
					break;
				case 56:
					sb.append("56-Blur Reduction");
					break;
				case 57:
					sb.append("57-Monochrome");
					break;
				case 58:
					sb.append("58-Toy Camera Effect");
					break;
				case 59:
					sb.append("59-Scene Intelligent Auto");
					break;
				case 60:
					sb.append("60-High-speed Burst HQ");
					break;
				case 61:
					sb.append("61-Smooth Skin");
					break;
				case 62:
					sb.append("62-Soft Focus");
					break;
				case 257:
					sb.append("257-Spotlight");
					break;
				case 258:
					sb.append("258-Night 2");
					break;
				case 259:
					sb.append("259-Night+");
					break;
				case 260:
					sb.append("260-Super Night");
					break;
				case 261:
					sb.append("261-Sunset");
					break;
				case 263:
					sb.append("263-Night Scene");
					break;
				case 264:
					sb.append("264-Surface");
					break;
				case 265:
					sb.append("265-Low Light 2");
					break;
				default:
					sb.append(ByteTool.readInt16(valBuff, valOfst + k * 2));
					break;
				}
				break;
			case 12:
				sb.append("DigitalZoom = ");
				switch (ByteTool.readInt16(valBuff, valOfst + k * 2))
				{
				case 0:
					sb.append("0-None");
					break;
				case 1:
					sb.append("1-2x");
					break;
				case 2:
					sb.append("2-4x");
					break;
				case 3:
					sb.append("3-Other");
					break;
				default:
					sb.append(ByteTool.readInt16(valBuff, valOfst + k * 2));
					break;
				}
				break;
			case 13:
				sb.append("Contrast = ");
				isInt16 = true;
				break;
			case 14:
				sb.append("Saturation = ");
				isInt16 = true;
				break;
			case 15:
				sb.append("Sharpness = ");
				isInt16 = true;
				break;
			case 16:
				sb.append("CameraISO = ");
				isInt16 = true;
				break;
			case 17:
				sb.append("MeteringMode = ");
				switch (ByteTool.readInt16(valBuff, valOfst + k * 2))
				{
				case 0:
					sb.append("0-Default");
					break;
				case 1:
					sb.append("1-Spot");
					break;
				case 2:
					sb.append("2-Average");
					break;
				case 3:
					sb.append("3-Evaluative");
					break;
				case 4:
					sb.append("4-Partial");
					break;
				case 5:
					sb.append("5-Center-weighted average");
					break;
				default:
					sb.append(ByteTool.readInt16(valBuff, valOfst + k * 2));
					break;
				}
				break;
			case 18:
				sb.append("FocusRange = ");
				switch (ByteTool.readInt16(valBuff, valOfst + k * 2))
				{
				case 0:
					sb.append("0-Manual");
					break;
				case 1:
					sb.append("1-Auto");
					break;
				case 2:
					sb.append("2-Not Known");
					break;
				case 3:
					sb.append("3-Macro");
					break;
				case 4:
					sb.append("4-Very Close");
					break;
				case 5:
					sb.append("5-Close");
					break;
				case 6:
					sb.append("6-Middle Range");
					break;
				case 7:
					sb.append("7-Far Range");
					break;
				case 8:
					sb.append("8-Pan Focus");
					break;
				case 9:
					sb.append("9-Super Macro");
					break;
				case 10:
					sb.append("10-Infinity");
					break;
				default:
					sb.append(ByteTool.readInt16(valBuff, valOfst + k * 2));
					break;
				}
				break;
			case 19:
				sb.append("AFPoint = ");
				switch (ByteTool.readInt16(valBuff, valOfst + k * 2))
				{
				case 0x2005:
					sb.append("0x2005-Manual AF point selection ");
					break;
				case 0x3000:
					sb.append("0x3000-None (MF)");
					break;
				case 0x3001:
					sb.append("0x3001-Auto AF point selection");
					break;
				case 0x3002:
					sb.append("0x3002-Right");
					break;
				case 0x3003:
					sb.append("0x3003-Center");
					break;
				case 0x3004:
					sb.append("0x3004-Left");
					break;
				case 0x4001:
					sb.append("0x4001-Auto AF point selection");
					break;
				case 0x4006:
					sb.append("0x4006-Face Detect");
					break;
				default:
					sb.append(ByteTool.readInt16(valBuff, valOfst + k * 2));
					break;
				}
				break;
			case 20:
				sb.append("CanonExposureMode = ");
				switch (ByteTool.readInt16(valBuff, valOfst + k * 2))
				{
				case 0:
					sb.append("0-Easy");
					break;
				case 1:
					sb.append("1-Program AE");
					break;
				case 2:
					sb.append("2-Shutter speed priority AE");
					break;
				case 3:
					sb.append("3-Aperture-priority AE");
					break;
				case 4:
					sb.append("4-Manual");
					break;
				case 5:
					sb.append("5-Depth-of-field AE");
					break;
				case 6:
					sb.append("6-M-Dep");
					break;
				case 7:
					sb.append("7-Bulb");
					break;
				default:
					sb.append(ByteTool.readInt16(valBuff, valOfst + k * 2));
					break;
				}
				break;
			case 22:
				sb.append("LensType = ");
				this.toStringCanonLensType(sb, ByteTool.readInt16(valBuff, valOfst + k * 2));
				break;
			case 23:
				sb.append("MaxFocalLength = ");
				isUInt16 = true;
				break;
			case 24:
				sb.append("MinFocalLength = ");
				isUInt16 = true;
				break;
			case 25:
				sb.append("FocalUnits = ");
				isInt16 = true;
				break;
			case 26:
				sb.append("MaxAperture = ");
				isInt16 = true;
				break;
			case 27:
				sb.append("MinAperture = ");
				isInt16 = true;
				break;
			case 28:
				sb.append("FlashActivity = ");
				isInt16 = true;
				break;
			case 29:
				sb.append("FlashBits = 0x");
				sb.append(StringUtil.toHex16(ByteTool.readInt16(valBuff, valOfst + k * 2)));
				break;
			case 32:
				sb.append("FocusContinuous = ");
				switch (ByteTool.readInt16(valBuff, valOfst + k * 2))
				{
				case 0:
					sb.append("0-Single");
					break;
				case 1:
					sb.append("1-Continuous");
					break;
				case 8:
					sb.append("8-Manual");
					break;
				default:
					sb.append(ByteTool.readInt16(valBuff, valOfst + k * 2));
					break;
				}
				break;
			case 33:
				sb.append("AESetting = ");
				switch (ByteTool.readInt16(valBuff, valOfst + k * 2))
				{
				case 0:
					sb.append("0-Normal AE");
					break;
				case 1:
					sb.append("1-Exposure Compensation");
					break;
				case 2:
					sb.append("2-AE Lock");
					break;
				case 3:
					sb.append("3-AE Lock + Exposure Comp.");
					break;
				case 4:
					sb.append("4-No AE");
					break;
				default:
					sb.append(ByteTool.readInt16(valBuff, valOfst + k * 2));
					break;
				}
				break;
			case 34:
				sb.append("ImageStabilization = ");
				switch (ByteTool.readInt16(valBuff, valOfst + k * 2))
				{
				case 0:
					sb.append("0-Off");
					break;
				case 1:
					sb.append("1-On");
					break;
				case 2:
					sb.append("2-Shoot Only");
					break;
				case 3:
					sb.append("3-Panning");
					break;
				case 4:
					sb.append("4-Dynamic");
					break;
				case 256:
					sb.append("256-Off");
					break;
				case 257:
					sb.append("257-On");
					break;
				case 258:
					sb.append("258-Shoot Only");
					break;
				case 259:
					sb.append("259-Panning");
					break;
				case 260:
					sb.append("260-Dynamic");
					break;
				default:
					sb.append(ByteTool.readInt16(valBuff, valOfst + k * 2));
					break;
				}
				break;
			case 35:
				sb.append("DisplayAperture = ");
				isInt16 = true;
				break;
			case 36:
				sb.append("ZoomSourceWidth = ");
				isInt16 = true;
				break;
			case 37:
				sb.append("ZoomTargetWidth = ");
				isInt16 = true;
				break;
			case 39:
				sb.append("AESetting = ");
				switch (ByteTool.readInt16(valBuff, valOfst + k * 2))
				{
				case 0:
					sb.append("0-Center");
					break;
				case 1:
					sb.append("1-AF Point");
					break;
				default:
					sb.append(ByteTool.readInt16(valBuff, valOfst + k * 2));
					break;
				}
				break;
			case 40:
				sb.append("PhotoEffect = ");
				switch (ByteTool.readInt16(valBuff, valOfst + k * 2))
				{
				case 0:
					sb.append("0-Off");
					break;
				case 1:
					sb.append("1-Vivid");
					break;
				case 2:
					sb.append("2-Neutral");
					break;
				case 3:
					sb.append("3-Smooth");
					break;
				case 4:
					sb.append("4-Sepia");
					break;
				case 5:
					sb.append("5-B&W");
					break;
				case 6:
					sb.append("6-Custom");
					break;
				case 100:
					sb.append("100-My Color Data");
					break;
				default:
					sb.append(ByteTool.readInt16(valBuff, valOfst + k * 2));
					break;
				}
				break;
			case 41:
				sb.append("ManualFlashOutput = ");
				switch (ByteTool.readInt16(valBuff, valOfst + k * 2))
				{
				case 0:
					sb.append("0-n/a");
					break;
				case 0x500:
					sb.append("0x500-Full");
					break;
				case 0x502:
					sb.append("0x500-Medium");
					break;
				case 0x504:
					sb.append("0x500-Low");
					break;
				case 0x7fff:
					sb.append("0x7fff-n/a");
					break;
				default:
					sb.append("0x");
					sb.append(StringUtil.toHex16(ByteTool.readInt16(valBuff, valOfst + k * 2)));
					break;
				}
				break;
			case 42:
				sb.append("ColorTone = ");
				isInt16 = true;
				break;
			case 46:
				sb.append("SRAWQuality = ");
				switch (ByteTool.readInt16(valBuff, valOfst + k * 2))
				{
				case 0:
					sb.append("0-n/a");
					break;
				case 1:
					sb.append("1-sRAW1(mRAW)");
					break;
				case 2:
					sb.append("2-sRAW2(sRAW)");
					break;
				default:
					sb.append("0x");
					sb.append(StringUtil.toHex16(ByteTool.readInt16(valBuff, valOfst + k * 2)));
					break;
				}
				break;
			default:
				sb.append("Unknown(");
				sb.append(k);
				sb.append(") = ");
				isInt16 = true;
				break;
			}
			if (isInt16)
			{
				sb.append(ByteTool.readInt16(valBuff, valOfst + k * 2));
			}
			else if (isUInt16)
			{
				sb.append(ByteTool.readUInt16(valBuff, valOfst + k * 2));
			}
			k++;
		}
		return true;
	}

	public boolean toStringCanonFocalLength(StringBuilder sb, String linePrefix, byte[] valBuff, int valOfst, int valCnt)
	{
		boolean isInt16;
		boolean isUInt16;
		int k;
		k = 0;
		while (k < valCnt)
		{
			sb.append("\r\n");
			if (linePrefix != null)
				sb.append(linePrefix);
			sb.append(" ");
			isInt16 = false;
			isUInt16 = false;
			switch (k)
			{
			case 0:
				sb.append("FocalType = ");
				switch (ByteTool.readInt16(valBuff, valOfst + k * 2))
				{
				case 1:
					sb.append("1-Fixed");
					break;
				case 2:
					sb.append("2-Zoom");
					break;
				default:
					sb.append(ByteTool.readInt16(valBuff, valOfst + k * 2));
					break;
				}
				break;
			case 1:
				sb.append("FocalLength = ");
				isUInt16 = true;
				break;
			case 2:
				sb.append("FocalPlaneXSize = ");
				isUInt16 = true;
				break;
			case 3:
				sb.append("FocalPlaneYSize = ");
				isUInt16 = true;
				break;
			default:
				sb.append("Unknown(");
				sb.append(k);
				sb.append(") = ");
				isInt16 = true;
				break;
			}
			if (isInt16)
			{
				sb.append(ByteTool.readInt16(valBuff, valOfst + k * 2));
			}
			else if (isUInt16)
			{
				sb.append(StringUtil.toHex16(ByteTool.readInt16(valBuff, valOfst + k * 2)));
			}
			k++;
		}
		return true;
	}

	public boolean toStringCanonShotInfo(StringBuilder sb, String linePrefix, byte[] valBuff, int valOfst, int valCnt)
	{
		boolean isInt16;
		boolean isUInt16;
		int k;
		k = 0;
		while (k < valCnt)
		{
			sb.append("\r\n");
			if (linePrefix != null)
				sb.append(linePrefix);
			sb.append(" ");
			isInt16 = false;
			isUInt16 = false;
			switch (k)
			{
			case 1:
				sb.append("AutoISO = ");
				isInt16 = true;
				break;
			case 2:
				sb.append("BaseISO = ");
				isInt16 = true;
				break;
			case 3:
				sb.append("MeasuredEV = ");
				isInt16 = true;
				break;
			case 4:
				sb.append("TargetAperture = ");
				isInt16 = true;
				break;
			case 5:
				sb.append("TargetExposureTime = ");
				isInt16 = true;
				break;
			case 6:
				sb.append("ExposureCompensation = ");
				isInt16 = true;
				break;
			case 7:
				sb.append("WhiteBalance = ");
				isInt16 = true;
				break;
			case 8:
				sb.append("SlowShutter = ");
				switch (ByteTool.readInt16(valBuff, valOfst + k * 2))
				{
				case -1:
					sb.append("-1-n/a");
					break;
				case 0:
					sb.append("0-Off");
					break;
				case 1:
					sb.append("1-Night Scene");
					break;
				case 2:
					sb.append("2-On");
					break;
				case 3:
					sb.append("3-None");
					break;
				default:
					sb.append(ByteTool.readInt16(valBuff, valOfst + k * 2));
					break;
				}
				break;
			case 9:
				sb.append("SequenceNumber = ");
				isInt16 = true;
				break;
			case 10:
				sb.append("OpticalZoomCode = ");
				isInt16 = true;
				break;
			case 12:
				sb.append("CameraTemperature = ");
				isInt16 = true;
				break;
			case 13:
				sb.append("FlashGuideNumber = ");
				isInt16 = true;
				break;
			case 14:
				sb.append("AFPointsInFocus = ");
				switch (ByteTool.readInt16(valBuff, valOfst + k * 2))
				{
				case 0x3000:
					sb.append("0x3000-None (MF)");
					break;
				case 0x3001:
					sb.append("0x3001-Right");
					break;
				case 0x3002:
					sb.append("0x3002-Center");
					break;
				case 0x3003:
					sb.append("0x3003-Center+Right");
					break;
				case 0x3004:
					sb.append("0x3004-Left");
					break;
				case 0x3005:
					sb.append("0x3005-Left+Right");
					break;
				case 0x3006:
					sb.append("0x3006-Left+Center");
					break;
				case 0x3007:
					sb.append("0x3007-All");
					break;
				default:
					sb.append("0x");
					sb.append(StringUtil.toHex16(ByteTool.readInt16(valBuff, valOfst + k * 2)));
					break;
				}
				break;
			case 15:
				sb.append("FlashExposureComp = ");
				isInt16 = true;
				break;
			case 16:
				sb.append("AutoExposureBracketing = ");
				switch (ByteTool.readInt16(valBuff, valOfst + k * 2))
				{
				case -1:
					sb.append("-1-On");
					break;
				case 0:
					sb.append("0-Off");
					break;
				case 1:
					sb.append("1-On (shot 1)");
					break;
				case 2:
					sb.append("2-On (shot 2)");
					break;
				case 3:
					sb.append("3-On (shot 3)");
					break;
				default:
					sb.append(ByteTool.readInt16(valBuff, valOfst + k * 2));
					break;
				}
				break;
			case 17:
				sb.append("AEBBracketValue = ");
				isInt16 = true;
				break;
			case 18:
				sb.append("ControlMode = ");
				switch (ByteTool.readInt16(valBuff, valOfst + k * 2))
				{
				case 0:
					sb.append("0-n/a");
					break;
				case 1:
					sb.append("1-Camera Local Control");
					break;
				case 3:
					sb.append("3-Computer Remote Control");
					break;
				default:
					sb.append(ByteTool.readInt16(valBuff, valOfst + k * 2));
					break;
				}
				break;
			case 19:
				sb.append("FocusDistanceUpper = ");
				isUInt16 = true;
				break;
			case 20:
				sb.append("FocusDistanceLower = ");
				isUInt16 = true;
				break;
			case 21:
				sb.append("FNumber = ");
				isInt16 = true;
				break;
			case 22:
				sb.append("ExposureTime = ");
				isInt16 = true;
				break;
			case 23:
				sb.append("MeasuredEV2 = ");
				isInt16 = true;
				break;
			case 24:
				sb.append("BulbDuration = ");
				isInt16 = true;
				break;
			case 26:
				sb.append("CameraType = ");
				switch (ByteTool.readInt16(valBuff, valOfst + k * 2))
				{
				case 0:
					sb.append("0-n/a");
					break;
				case 248:
					sb.append("248-EOS High-end");
					break;
				case 250:
					sb.append("250-Compact");
					break;
				case 252:
					sb.append("252-EOS Mid-range");
					break;
				case 255:
					sb.append("255-DV Camera");
					break;
				default:
					sb.append(ByteTool.readInt16(valBuff, valOfst + k * 2));
					break;
				}
				break;
			case 27:
				sb.append("AutoRotate = ");
				switch (ByteTool.readInt16(valBuff, valOfst + k * 2))
				{
				case -1:
					sb.append("-1-n/a");
					break;
				case 0:
					sb.append("0-None");
					break;
				case 1:
					sb.append("1-Rotate 90 CW");
					break;
				case 2:
					sb.append("1-Rotate 180");
					break;
				case 3:
					sb.append("3-Rotate 270 CW");
					break;
				default:
					sb.append(ByteTool.readInt16(valBuff, valOfst + k * 2));
					break;
				}
				break;
			case 28:
				sb.append("NDFilter = ");
				switch (ByteTool.readInt16(valBuff, valOfst + k * 2))
				{
				case -1:
					sb.append("-1-n/a");
					break;
				case 0:
					sb.append("0-Off");
					break;
				case 1:
					sb.append("1-On");
					break;
				default:
					sb.append(ByteTool.readInt16(valBuff, valOfst + k * 2));
					break;
				}
				break;
			case 29:
				sb.append("SelfTimer2 = ");
				isInt16 = true;
				break;
			case 33:
				sb.append("FlashOutput = ");
				isInt16 = true;
				break;
			default:
				sb.append("Unknown(");
				sb.append(k);
				sb.append(") = ");
				isInt16 = true;
				break;
			}
			if (isInt16)
			{
				sb.append(ByteTool.readInt16(valBuff, valOfst + k * 2));
			}
			else if (isUInt16)
			{
				sb.append(StringUtil.toHex16(ByteTool.readInt16(valBuff, valOfst + k * 2)));
			}
			k++;
		}
		return true;
	}

	public boolean toStringCanonLensType(StringBuilder sb, int lensType)
	{
		sb.append("0x");
		sb.append(StringUtil.toHex16(lensType));
		return true;
	}
	
	public void toExifBuff(byte[] buff, SharedInt startOfst, SharedInt otherOfst)
	{
		toExifBuff(buff, this.exifMap.values(), startOfst, otherOfst);
	}

	public void getExifBuffSize(SharedInt size, SharedInt endOfst)
	{
		getExifBuffSize(this.exifMap.values(), size, endOfst);
	}

	public EXIFData parseMakerNote(byte[] buff, int buffOfst, int buffSize)
	{
		EXIFData ret = null;
		if (ByteTool.strEquals(buff, buffOfst, "Panasonic"))
		{
			ret = parseIFD(buff, buffOfst + 12, buffSize - 12, new ByteIOLSB(), null, EXIFMaker.PANASONIC, 0);
			return ret;
		}
		else if (ByteTool.strEquals(buff, buffOfst, "OLYMPUS"))
		{
			if (buff[8] == 'I' && buff[9] == 'I')
			{
				ret = parseIFD(buff, buffOfst + 12, buffSize - 12, new ByteIOLSB(), null, EXIFMaker.OLYMPUS, 0);
				return ret;
			}
		}
		else if (ByteTool.strEquals(buff, buffOfst, "OLYMP"))
		{
			ret = parseIFD(buff, buffOfst + 8, buffSize - 8, new ByteIOLSB(), null, EXIFMaker.OLYMPUS, 0);
			return ret;
		}
		else if (ByteTool.strEquals(buff, buffOfst, "QVC"))
		{
			ret = parseIFD(buff, buffOfst + 6, buffSize - 6, new ByteIOMSB(), null, EXIFMaker.CASIO2, 0);
			return ret;
		}
		else
		{
			String maker = this.getPhotoMake();
			if (maker != null)
			{
				if (maker.equals("Canon"))
				{
					ret = parseIFD(buff, buffOfst, buffSize, new ByteIOLSB(), null, EXIFMaker.CANON, 0);
					return ret;
				}
				else if (maker.equals("CASIO"))
				{
					ret = parseIFD(buff, buffOfst, buffSize, new ByteIOMSB(), null, EXIFMaker.CASIO1, 0);
					return ret;
				}
				else if (maker.equals("FLIR Systems AB"))
				{
					ret = parseIFD(buff, buffOfst, buffSize, new ByteIOLSB(), null, EXIFMaker.FLIR, 0);
					return ret;
				}
			}
		}
		return ret;		
	}

	private static String getItemString(EXIFItem item)
	{
		if (item.dataBuff[item.size - 1] != 0)
		{
			return new String(item.dataBuff, 0, item.size, StandardCharsets.UTF_8);
		}
		else
		{
			return new String(item.dataBuff, 0, item.size - 1, StandardCharsets.UTF_8);
		}
	}


	public static String getEXIFMakerName(EXIFMaker exifMaker)
	{
		switch (exifMaker)
		{
		case PANASONIC:
			return "Panasonic";
		case CANON:
			return "Canon";
		case OLYMPUS:
			return "Olympus";
		case CASIO1:
			return "Casio Type 1";
		case CASIO2:
			return "Casio Type 2";
		case FLIR:
			return "FLIR";
		case STANDARD:
		default:
			return "Standard";
		}
	}

	public static String getEXIFName(EXIFMaker exifMaker, int id)
	{
		return getEXIFName(exifMaker, 0, id);
	}

	private static List<EXIFInfo> loadEXIFInfo(String name)
	{
		List<EXIFInfo> list = ResourceLoader.loadObjects(EXIFInfo.class, "EXIFData."+name+".txt", new String[]{"id", "name"});
		if (list == null) list = new ArrayList<EXIFInfo>();
		return list;
	}

	public static String getEXIFName(EXIFMaker exifMaker, int id, int subId)
	{
		List<EXIFInfo> infos;
		int cnt;
		if (panasonicInfos == null)
		{
			defInfos = loadEXIFInfo("defInfos");
			exifInfos = loadEXIFInfo("exifInfos");
			gpsInfos = loadEXIFInfo("gpsInfos");
			panasonicInfos = loadEXIFInfo("panasonicInfos");
			canonInfos = loadEXIFInfo("canonInfos");
			olympusInfos = loadEXIFInfo("olympusInfos");
			olympus2010Infos = loadEXIFInfo("olympus2010Infos");
			olympus2020Infos = loadEXIFInfo("olympus2020Infos");
			olympus2030Infos = loadEXIFInfo("olympus2030Infos");
			olympus2040Infos = loadEXIFInfo("olympus2040Infos");
			olympus2050Infos = loadEXIFInfo("olympus2050Infos");
			casio1Infos = loadEXIFInfo("casio1Infos");
			casio2Infos = loadEXIFInfo("casio2Infos");
			flirInfos = loadEXIFInfo("flirInfos");
		}

		if (id == 0)
		{
			if (exifMaker == EXIFMaker.PANASONIC)
			{
				infos = panasonicInfos;
				cnt = infos.size();
			}
			else if (exifMaker == EXIFMaker.CANON)
			{
				infos = canonInfos;
				cnt = infos.size();
			}
			else if (exifMaker == EXIFMaker.OLYMPUS)
			{
				infos = olympusInfos;
				cnt = infos.size();
			}
			else if (exifMaker == EXIFMaker.CASIO1)
			{
				infos = casio1Infos;
				cnt = infos.size();
			}
			else if (exifMaker == EXIFMaker.CASIO2)
			{
				infos = casio2Infos;
				cnt = infos.size();
			}
			else if (exifMaker == EXIFMaker.FLIR)
			{
				infos = flirInfos;
				cnt = infos.size();
			}
			else
			{
				infos = defInfos;
				cnt = infos.size();
			}
		}
		else if (id == 34665)
		{
			infos = exifInfos;
			cnt = infos.size();
		}
		else if (id == 34853)
		{
			infos = gpsInfos;
			cnt = infos.size();
		}
		else if (exifMaker == EXIFMaker.OLYMPUS)
		{
			if (id == 0x2010)
			{
				infos = olympus2010Infos;
				cnt = infos.size();
			}
			else if (id == 0x2020)
			{
				infos = olympus2020Infos;
				cnt = infos.size();
			}
			else if (id == 0x2030)
			{
				infos = olympus2030Infos;
				cnt = infos.size();
			}
			else if (id == 0x2040)
			{
				infos = olympus2040Infos;
				cnt = infos.size();
			}
			else if (id == 0x2050)
			{
				infos = olympus2050Infos;
				cnt = infos.size();
			}
			else
			{
				return "Unknown";
			}
		}
		else
		{
			return "Unknown";
		}
		int i = 0;
		int j = cnt - 1;
		int k;
		while (i <= j)
		{
			k = (i + j) >> 1;
			if (infos.get(k).getId() > subId)
			{
				j = k - 1;
			}
			else if (infos.get(k).getId() < subId)
			{
				i = k + 1;
			}
			else
			{
				return infos.get(k).getName();
			}
		}
		return "Unknown";
	}

	public static String getEXIFTypeName(EXIFType type)
	{
		switch (type)
		{
		case BYTES:
			return "Bytes";
		case STRING:
			return "String";
		case UINT16:
			return "UInt16";
		case UINT32:
			return "UInt32";
		case RATIONAL:
			return "Rational";
		case OTHER:
			return "Other";
		case INT16:
			return "Int16";
		case SUBEXIF:
			return "Exif";
		case DOUBLE:
			return "Double";
		case UNKNOWN:
		default:
			return "Unknown";
		}
	}

	public static EXIFData parseIFD(byte[] buff, int buffOfst, int buffSize, ByteIO byteIO, SharedInt nextOfst, EXIFMaker exifMaker, int readBase)
	{
		EXIFData exif;
		byte[] ifdEntries;
		int ifdCnt;
		int i;
		int ifdOfst;
		int tag;
		int ftype;
		int fcnt;

		ifdCnt = byteIO.readInt16(buff, buffOfst);
		ifdEntries = buff;

		byte[] tmpBuff;
		int j;
		exif = new EXIFData(exifMaker);

		if (readBase == 0)
		{
			readBase = 0x7fffffff;
			ifdOfst = buffOfst + 2;
			i = 0;
			while (i < ifdCnt)
			{
				tag = byteIO.readInt16(ifdEntries, ifdOfst) & 0xffff;
				ftype = byteIO.readInt16(ifdEntries, ifdOfst + 2);
				fcnt = byteIO.readInt32(ifdEntries, ifdOfst + 4);

				if (ftype == 1)
				{
					if (fcnt <= 4)
					{
					}
					else
					{
						if (readBase > byteIO.readInt32(ifdEntries, ifdOfst + 8))
						{
							readBase = byteIO.readInt32(ifdEntries, ifdOfst + 8);
						}
					}
				}
				else if (ftype == 2)
				{
					if (fcnt <= 4)
					{
					}
					else
					{
						if (readBase > byteIO.readInt32(ifdEntries, ifdOfst + 8))
						{
							readBase = byteIO.readInt32(ifdEntries, ifdOfst + 8);
						}
					}
				}
				else if (ftype == 3)
				{
					if (fcnt == 1)
					{
					}
					else if (fcnt == 2)
					{
					}
					else
					{
						if (readBase > byteIO.readInt32(ifdEntries, ifdOfst + 8))
						{
							readBase = byteIO.readInt32(ifdEntries, ifdOfst + 8);
						}
					}
				}
				else if (ftype == 4)
				{
					if (fcnt == 1)
					{
					}
					else
					{
						if (readBase > byteIO.readInt32(ifdEntries, ifdOfst + 8))
						{
							readBase = byteIO.readInt32(ifdEntries, ifdOfst + 8);
						}
					}
				}
				else if (ftype == 5)
				{
					if (readBase > byteIO.readInt32(ifdEntries, ifdOfst + 8))
					{
						readBase = byteIO.readInt32(ifdEntries, ifdOfst + 8);
					}
				}
				else if (ftype == 7)
				{
					if (fcnt <= 4)
					{
					}
					else
					{
						if (readBase > byteIO.readInt32(ifdEntries, ifdOfst + 8))
						{
							readBase = byteIO.readInt32(ifdEntries, ifdOfst + 8);
						}
					}
				}
				else if (ftype == 8)
				{
					if (fcnt == 1)
					{
					}
					else if (fcnt == 2)
					{
					}
					else
					{
						if (readBase > byteIO.readInt32(ifdEntries, ifdOfst + 8))
						{
							readBase = byteIO.readInt32(ifdEntries, ifdOfst + 8);
						}
					}
				}
				else if (ftype == 12)
				{
					if (readBase > byteIO.readInt32(ifdEntries, ifdOfst + 8))
					{
						readBase = byteIO.readInt32(ifdEntries, ifdOfst + 8);
					}
				}
				else if (ftype == 13)
				{
					if (readBase > byteIO.readInt32(ifdEntries, ifdOfst + 8))
					{
						readBase = byteIO.readInt32(ifdEntries, ifdOfst + 8);
					}
				}
				else
				{
					j = 0;
				}

				ifdOfst += 12;
				i++;
			}
			readBase = ifdCnt * 12 + 2 + 4 - readBase;
		}

		ifdOfst = buffOfst + 2;
		i = 0;
		while (i < ifdCnt)
		{
			tag = byteIO.readInt16(ifdEntries, ifdOfst) & 0xffff;
			ftype = byteIO.readInt16(ifdEntries, ifdOfst + 2);
			fcnt = byteIO.readInt32(ifdEntries, ifdOfst + 4);

			if (tag == 34665)
			{
				tag = 34665;
			}

			if (ftype == 1)
			{
				if (fcnt <= 4)
				{
					exif.addBytes(tag, fcnt, ifdEntries, ifdOfst + 8);
				}
				else
				{
					exif.addBytes(tag, fcnt, buff, buffOfst + byteIO.readInt32(ifdEntries, ifdOfst + 8) + readBase);
				}
			}
			else if (ftype == 2)
			{
				if (fcnt <= 4)
				{
					exif.addString(tag, fcnt, ifdEntries, ifdOfst + 8);
				}
				else
				{
					exif.addString(tag, fcnt, buff, buffOfst + byteIO.readInt32(ifdEntries, ifdOfst + 8) + readBase);
				}
			}
			else if (ftype == 3)
			{
				if (fcnt == 1)
				{
					tmpBuff = new byte[2];
					ByteTool.writeInt16(tmpBuff, 0, byteIO.readInt16(ifdEntries, ifdOfst + 8));
					exif.addUInt16(tag, fcnt, tmpBuff, 0);
				}
				else if (fcnt == 2)
				{
					tmpBuff = new byte[4];
					ByteTool.writeInt16(tmpBuff, 0, byteIO.readInt16(ifdEntries, ifdOfst + 8));
					ByteTool.writeInt16(tmpBuff, 2, byteIO.readInt16(ifdEntries, ifdOfst + 10));
					exif.addUInt16(tag, fcnt, tmpBuff, 0);
				}
				else
				{
					tmpBuff = new byte[fcnt << 1];
					ByteTool.copyArray(tmpBuff, 0, buff, buffOfst + byteIO.readInt32(ifdEntries,ifdOfst + 8) + readBase, fcnt << 1);
					j = fcnt << 1;
					while (j > 0)
					{
						j -= 2;
						ByteTool.writeInt16(tmpBuff, j, byteIO.readInt16(tmpBuff, j));
					}
					exif.addUInt16(tag, fcnt, tmpBuff, 0);
				}
			}
			else if (ftype == 4)
			{
				if (fcnt == 1)
				{
					int tmp = byteIO.readInt32(ifdEntries, ifdOfst + 8);
					if (tag == 34665 || tag == 34853)
					{
						EXIFData subexif = parseIFD(buff, buffOfst + tmp + readBase, buffSize - tmp - readBase, byteIO, null, EXIFMaker.STANDARD, readBase - buffOfst - 6);
						if (subexif != null)
						{
							exif.addSubEXIF(tag, subexif);
						}
					}
					else
					{
						tmpBuff = new byte[4];
						ByteTool.writeInt32(tmpBuff, 0, tmp);
						exif.addUInt32(tag, fcnt, tmpBuff, 0);
					}
				}
				else
				{
					tmpBuff = new byte[fcnt << 2];
					ByteTool.copyArray(tmpBuff, 0, buff, buffOfst + byteIO.readInt32(ifdEntries, ifdOfst + 8) + readBase, fcnt << 2);
					j = fcnt << 2;
					while (j > 0)
					{
						j -= 4;
						ByteTool.writeInt32(tmpBuff, j, byteIO.readInt32(tmpBuff, j));
					}
					exif.addUInt32(tag, fcnt, tmpBuff, 0);
				}
			}
			else if (ftype == 5)
			{
				tmpBuff = new byte[fcnt << 3];
				ByteTool.copyArray(tmpBuff, 0, buff, buffOfst + byteIO.readInt32(ifdEntries, ifdOfst + 8) + readBase, fcnt << 3);
				j = fcnt << 3;
				while (j > 0)
				{
					j -= 8;
					ByteTool.writeInt32(tmpBuff, j, byteIO.readInt32(tmpBuff, j));
					ByteTool.writeInt32(tmpBuff, j + 4, byteIO.readInt32(tmpBuff, j + 4));
				}
				exif.addRational(tag, fcnt, tmpBuff, 0);
			}
			else if (ftype == 7)
			{
				if (fcnt <= 4)
				{
					exif.addOther(tag, fcnt, ifdEntries, ifdOfst + 8);
				}
				else
				{
					int ofst = byteIO.readInt32(ifdEntries, ifdOfst + 8) + readBase;
					if (ofst + fcnt > buffSize)
					{
						ofst = buffSize - fcnt;
					}
					exif.addOther(tag, fcnt, buff, ofst);
				}
			}
			else if (ftype == 8)
			{
				if (fcnt == 1)
				{
					tmpBuff = new byte[2];
					ByteTool.writeInt16(tmpBuff, 0, byteIO.readInt16(ifdEntries, ifdOfst + 8));
					exif.addInt16(tag, fcnt, tmpBuff, 0);
				}
				else if (fcnt == 2)
				{
					tmpBuff = new byte[4];
					ByteTool.writeInt16(tmpBuff, 0, byteIO.readInt16(ifdEntries, ifdOfst + 8));
					ByteTool.writeInt16(tmpBuff, 2, byteIO.readInt16(ifdEntries, ifdOfst + 10));
					exif.addInt16(tag, fcnt, tmpBuff, 0);
				}
				else
				{
					tmpBuff = new byte[fcnt << 1];
					ByteTool.copyArray(tmpBuff, 0, buff, buffOfst + byteIO.readInt32(ifdEntries, ifdOfst + 8) + readBase, fcnt << 1);
					j = fcnt << 1;
					while (j > 0)
					{
						j -= 2;
						ByteTool.writeInt16(tmpBuff, j, byteIO.readInt16(tmpBuff, j));
					}
					exif.addInt16(tag, fcnt, tmpBuff, 0);
				}
			}
			else if (ftype == 12)
			{
				exif.addDouble(tag, fcnt, buff, buffOfst + byteIO.readInt32(ifdEntries, ifdOfst + 8) + readBase);
			}
			else if (ftype == 13) //Olympus innerIFD
			{
				EXIFData subexif = parseIFD(buff, buffOfst + byteIO.readInt32(ifdEntries, ifdOfst + 8) + readBase, buffSize - byteIO.readInt32(ifdEntries, ifdOfst + 8) - readBase, byteIO, null, exifMaker, -byteIO.readInt32(ifdEntries, ifdOfst + 8));
				if (subexif != null)
				{
					exif.addSubEXIF(tag, subexif);
				}
			}
			else
			{
				j = 0;
			}

			ifdOfst += 12;
			i++;
		}

		if (nextOfst != null)
		{
			nextOfst.value = byteIO.readInt32(ifdEntries, ifdCnt * 12);
		}
		return exif;
	}

	public static EXIFData parseIFD(StreamData fd, long ofst, ByteIO byteIO, SharedInt nextOfst, long readBase)
	{
		EXIFData exif;
		byte[] ifdEntries;
		byte[] ifdBuff = new byte[2];
		int ifdCnt;
		int i;
		int readSize;
		int ifdOfst;
		int tag;
		int ftype;
		int fcnt;
		if (fd.getRealData(ofst, 2, ifdBuff, 0) != 2)
		{
			return null;
		}
		ifdCnt = ByteTool.readUInt16(ifdBuff, 0);
	
		readSize = ifdCnt * 12 + 4;
		ifdEntries = new byte[readSize];
		if (fd.getRealData(ofst + 2, readSize, ifdEntries, 0) != readSize)
		{
			return null;
		}
	
		byte[] tmpBuff;
		int j;
		exif = new EXIFData(EXIFMaker.STANDARD);
	
		ifdOfst = 0;
		i = 0;
		while (i < ifdCnt)
		{
			tag = byteIO.readInt16(ifdEntries, ifdOfst);
			ftype = byteIO.readInt16(ifdEntries, ifdOfst + 2);
			fcnt = byteIO.readInt32(ifdEntries, ifdOfst + 4);
	
			if (ftype == 1)
			{
				if (fcnt <= 4)
				{
					exif.addBytes(tag, fcnt, ifdEntries, ifdOfst + 8);
				}
				else
				{
					tmpBuff = new byte[fcnt];
					fd.getRealData(byteIO.readInt32(ifdEntries, ifdOfst + 8) + readBase, fcnt, tmpBuff, 0);
					exif.addBytes(tag, fcnt, tmpBuff, 0);
				}
			}
			else if (ftype == 2)
			{
				if (fcnt <= 4)
				{
					exif.addString(tag, fcnt, ifdEntries, ifdOfst + 8);
				}
				else
				{
					tmpBuff = new byte[fcnt];
					fd.getRealData(byteIO.readInt32(ifdEntries, ifdOfst + 8) + readBase, fcnt, tmpBuff, 0);
					exif.addString(tag, fcnt, tmpBuff, 0);
				}
			}
			else if (ftype == 3)
			{
				if (fcnt == 1)
				{
					byte[] tmp = new byte[2];
					ByteTool.writeInt16(tmp, 0, byteIO.readInt16(ifdEntries, ifdOfst + 8));
					exif.addUInt16(tag, fcnt, tmp, 0);
				}
				else if (fcnt == 2)
				{
					byte[] tmp = new byte[2];
					ByteTool.writeInt16(tmp, 0, byteIO.readInt16(ifdEntries, ifdOfst + 8));
					ByteTool.writeInt16(tmp, 2, byteIO.readInt16(ifdEntries, ifdOfst + 10));
					exif.addUInt16(tag, fcnt, tmp, 0);
				}
				else
				{
					tmpBuff = new byte[fcnt << 1];
					fd.getRealData(byteIO.readInt32(ifdEntries, ifdOfst + 8) + readBase, fcnt << 1, tmpBuff, 0);
					j = fcnt << 1;
					while (j > 0)
					{
						j -= 2;
						ByteTool.writeInt16(tmpBuff, j, byteIO.readInt16(tmpBuff, j));
					}
					exif.addUInt16(tag, fcnt, tmpBuff, 0);
				}
			}
			else if (ftype == 4)
			{
				int tmp;
				if (fcnt == 1)
				{
					tmp = byteIO.readInt32(ifdEntries, ifdOfst + 8);
					if (tag == 34665 || tag == 34853)
					{
						EXIFData subexif = parseIFD(fd, tmp + readBase, byteIO, null, readBase);
						if (subexif != null)
						{
							exif.addSubEXIF(tag, subexif);
						}
					}
					else
					{
						tmpBuff = new byte[4];
						ByteTool.writeInt32(tmpBuff, 0, tmp);
						exif.addUInt32(tag, fcnt, tmpBuff, 0);
					}
				}
				else
				{
					tmpBuff = new byte[fcnt << 2];
					fd.getRealData(byteIO.readInt32(ifdEntries, ifdOfst + 8) + readBase, fcnt << 2, tmpBuff, 0);
					j = fcnt << 2;
					while (j > 0)
					{
						j -= 4;
						ByteTool.writeInt32(tmpBuff, j, byteIO.readInt32(tmpBuff, j));
					}
					exif.addUInt32(tag, fcnt, tmpBuff, 0);
				}
			}
			else if (ftype == 5)
			{
				tmpBuff = new byte[fcnt << 3];
				fd.getRealData(byteIO.readInt32(ifdEntries, ifdOfst + 8) + readBase, fcnt << 3, tmpBuff, 0);
				j = fcnt << 3;
				while (j > 0)
				{
					j -= 8;
					ByteTool.writeInt32(tmpBuff, j, byteIO.readInt32(tmpBuff, j));
					ByteTool.writeInt32(tmpBuff, j + 4, byteIO.readInt32(tmpBuff, j + 4));
				}
				exif.addRational(tag, fcnt, tmpBuff, 0);
			}
			else if (ftype == 7)
			{
				if (fcnt <= 4)
				{
					exif.addOther(tag, fcnt, ifdEntries, ifdOfst + 8);
				}
				else
				{
					tmpBuff = new byte[fcnt];
					fd.getRealData(byteIO.readInt32(ifdEntries, ifdOfst + 8) + readBase, fcnt, tmpBuff, 0);
					exif.addOther(tag, fcnt, tmpBuff, 0);
				}
			}
			else if (ftype == 8)
			{
				if (fcnt == 1)
				{
					byte[] tmp = new byte[2];
					ByteTool.writeInt16(tmp, 0, byteIO.readInt16(ifdEntries, ifdOfst + 8));
					exif.addInt16(tag, fcnt, tmp, 0);
				}
				else
				{
					tmpBuff = new byte[fcnt << 1];
					fd.getRealData(byteIO.readInt32(ifdEntries, ifdOfst + 8) + readBase, fcnt << 1, tmpBuff, 0);
					j = fcnt << 1;
					while (j > 0)
					{
						j -= 2;
						ByteTool.writeInt16(tmpBuff, j, byteIO.readInt16(tmpBuff, j));
					}
					exif.addInt16(tag, fcnt, tmpBuff, 0);
				}
			}
			else if (ftype == 12)
			{
				tmpBuff = new byte[fcnt << 3];
				fd.getRealData(byteIO.readInt32(ifdEntries, ifdOfst + 8) + readBase, fcnt << 3, tmpBuff, 0);
				exif.addDouble(tag, fcnt, tmpBuff, 0);
			}
			else
			{
				j = 0;
			}
	
			ifdOfst += 12;
			i++;
		}
	
		if (nextOfst != null)
		{
			nextOfst.value = byteIO.readInt32(ifdEntries, ifdCnt * 12);
		}
		return exif;
	}
}
