package org.sswr.util.media;

import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.sswr.util.data.ByteIO;
import org.sswr.util.data.ByteIOLSB;
import org.sswr.util.data.ByteIOMSB;
import org.sswr.util.data.ByteTool;
import org.sswr.util.data.DateTimeUtil;
import org.sswr.util.data.SharedDouble;
import org.sswr.util.data.SharedInt;
import org.sswr.util.data.SharedLong;
import org.sswr.util.data.StringUtil;
import org.sswr.util.io.StreamData;

public class EXIFData
{
	private Map<Integer, EXIFItem> exifMap;
	private EXIFMaker exifMaker;

	private void toExifBuff(byte[] buff, List<EXIFItem> exifList, SharedInt startOfst, SharedInt otherOfst)
	{
		int objCnt;
		int i;
		int j;
		int k;
		EXIFItem exif;
	
		objCnt = 0;
		k = otherOfst.value;
		j = startOfst.value + 2;
		i = 0;
		while (i < exifList.size())
		{
			exif = exifList.get(i);
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
			i++;
		}
		ByteTool.writeInt32(buff, j, 0);
		ByteTool.writeInt16(buff, startOfst.value, objCnt);
		j += 4;
	
		i = 0;
		while (i < exifList.size())
		{
			exif = exifList.get(i);
			if (exif.type == EXIFType.SUBEXIF)
			{
				ByteTool.writeInt32(buff, exif.ofst, j);
				SharedInt sj = new SharedInt(j);
				SharedInt sk = new SharedInt(k);
				exif.subExif.toExifBuff(buff, sj, sk);
				j = sj.value;
				k = sk.value;
			}
			i++;
		}
		startOfst.value = j;
		otherOfst.value = k;
	}

	private void getExifBuffSize(List<EXIFItem> exifList, SharedInt size, SharedInt endOfst)
	{
		int i = 6;
		int j = 6;
		int k;
		SharedInt l = new SharedInt();
		SharedInt m = new SharedInt();
		EXIFItem exif;
	
		k = exifList.size();
		while (k-- > 0)
		{
			exif = exifList.get(k);
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
					String[] dateArr = getItemString(item2).split(":");
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

/*	public boolean toString(StringBuilder sb, const UTF8Char *linePrefix);
	public boolean toStringCanonCameraSettings(StringBuilder sb, const UTF8Char *linePrefix, UInt16 *valBuff, UOSInt valCnt);
	public boolean toStringCanonFocalLength(StringBuilder sb, const UTF8Char *linePrefix, UInt16 *valBuff, UOSInt valCnt);
	public boolean toStringCanonShotInfo(StringBuilder sb, const UTF8Char *linePrefix, UInt16 *valBuff, UOSInt valCnt);
	public boolean toStringCanonLensType(StringBuilder sb, UInt16 lensType);*/
	public void toExifBuff(byte[] buff, SharedInt startOfst, SharedInt otherOfst)
	{
	}

	public void getExifBuffSize(SharedInt size, SharedInt endOfst)
	{
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
		return new String(item.dataBuff, 0, item.size, StandardCharsets.UTF_8);
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
			fcnt = byteIO.readInt32(ifdEntries, [ifdOfst + 4);

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
				UInt16 tmp[2];
				if (fcnt == 1)
				{
					tmpBuff = new byte[2];
					tmp[0] = (UInt16)readInt16(&ifdEntries[ifdOfst + 8]);
					exif.AddUInt16(tag, fcnt, tmp);
				}
				else if (fcnt == 2)
				{
					tmp[0] = (UInt16)readInt16(&ifdEntries[ifdOfst + 8]);
					tmp[1] = (UInt16)readInt16(&ifdEntries[ifdOfst + 10]);
					exif.AddUInt16(tag, fcnt, tmp);
				}
				else
				{
					tmpBuff = MemAlloc(UInt8, fcnt << 1);
					MemCopyNO(tmpBuff, &buff[(UInt32)readInt32(&ifdEntries[ifdOfst + 8]) + readBase], fcnt << 1);
					j = fcnt << 1;
					while (j > 0)
					{
						j -= 2;
						*(UInt16*)&tmpBuff[j] = (UInt16)readInt16(&tmpBuff[j]);
					}
					exif.AddUInt16(tag, fcnt, (UInt16*)tmpBuff);
					MemFree(tmpBuff);
				}
			}
			else if (ftype == 4)
			{
				UInt32 tmp;
				if (fcnt == 1)
				{
					tmp = (UInt32)readInt32(&ifdEntries[ifdOfst + 8]);
					exif.AddUInt32(tag, fcnt, &tmp);
				}
				else
				{
					tmpBuff = MemAlloc(UInt8, fcnt << 2);
					MemCopyNO(tmpBuff, &buff[(UInt32)readInt32(&ifdEntries[ifdOfst + 8]) + readBase], fcnt << 2);
					j = fcnt << 2;
					while (j > 0)
					{
						j -= 4;
						*(UInt32*)&tmpBuff[j] = (UInt32)readInt32(&tmpBuff[j]);
					}
					exif.AddUInt32(tag, fcnt, (UInt32*)tmpBuff);
					MemFree(tmpBuff);
				}
			}
			else if (ftype == 5)
			{
				tmpBuff = MemAlloc(UInt8, fcnt << 3);
				MemCopyNO(tmpBuff, &buff[(UInt32)readInt32(&ifdEntries[ifdOfst + 8]) + readBase], fcnt << 3);
				j = fcnt << 3;
				while (j > 0)
				{
					j -= 8;
					*(UInt32*)&tmpBuff[j] = (UInt32)readInt32(&tmpBuff[j]);
					*(UInt32*)&tmpBuff[j + 4] = (UInt32)readInt32(&tmpBuff[j + 4]);
				}
				exif.AddRational(tag, fcnt, (UInt32*)tmpBuff);
				MemFree(tmpBuff);
			}
			else if (ftype == 7)
			{
				if (fcnt <= 4)
				{
					exif.AddOther(tag, fcnt, (UInt8*)&ifdEntries[ifdOfst + 8]);
				}
				else
				{
					UOSInt ofst = (UInt32)readInt32(&ifdEntries[ifdOfst + 8]) + readBase;
					if (ofst + fcnt > buffSize)
					{
						ofst = buffSize - fcnt;
					}
					exif.AddOther(tag, fcnt, &buff[ofst]);
				}
			}
			else if (ftype == 8)
			{
				Int16 tmp[2];
				if (fcnt == 1)
				{
					tmp[0] = readInt16(&ifdEntries[ifdOfst + 8]);
					exif.AddInt16(tag, fcnt, tmp);
				}
				else if (fcnt == 2)
				{
					tmp[0] = readInt16(&ifdEntries[ifdOfst + 8]);
					tmp[1] = readInt16(&ifdEntries[ifdOfst + 10]);
					exif.AddInt16(tag, fcnt, tmp);
				}
				else
				{
					tmpBuff = MemAlloc(UInt8, fcnt << 1);
					MemCopyNO(tmpBuff, &buff[(UInt32)readInt32(&ifdEntries[ifdOfst + 8]) + readBase], fcnt << 1);
					j = fcnt << 1;
					while (j > 0)
					{
						j -= 2;
						*(Int16*)&tmpBuff[j] = readInt16(&tmpBuff[j]);
					}
					exif.AddInt16(tag, fcnt, (Int16*)tmpBuff);
					MemFree(tmpBuff);
				}
			}
			else if (ftype == 12)
			{
				exif.AddDouble(tag, fcnt, (Double*)&buff[(UInt32)readInt32(&ifdEntries[ifdOfst + 8]) + readBase]);
			}
			else if (ftype == 13) //Olympus innerIFD
			{
				Media::EXIFData *subexif = ParseIFD(&buff[(UInt32)readInt32(&ifdEntries[ifdOfst + 8]) + readBase], buffSize - (UInt32)readInt32(&ifdEntries[ifdOfst + 8]) - readBase, readInt32, readInt16, 0, exifMaker, (UInt32)-readInt32(&ifdEntries[ifdOfst + 8]));
				if (subexif)
				{
					exif.AddSubEXIF(tag, subexif);
				}
			}
			else
			{
				j = 0;
			}

			ifdOfst += 12;
			i++;
		}

		if (nextOfst)
		{
			*nextOfst = (UInt32)readInt32(&ifdEntries[ifdCnt * 12]);
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
