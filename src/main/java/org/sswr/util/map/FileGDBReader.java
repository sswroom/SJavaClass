package org.sswr.util.map;

import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sswr.util.data.ByteTool;
import org.sswr.util.data.DateTimeUtil;
import org.sswr.util.data.SharedLong;
import org.sswr.util.data.StringUtil;
import org.sswr.util.data.UUID;
import org.sswr.util.db.ColumnDef;
import org.sswr.util.db.ColumnType;
import org.sswr.util.db.DBReader;
import org.sswr.util.io.StreamData;
import org.sswr.util.math.Point2D;
import org.sswr.util.math.Point3D;
import org.sswr.util.math.Polygon;
import org.sswr.util.math.Polyline;
import org.sswr.util.math.Polyline3D;
import org.sswr.util.math.Vector2D;
import org.sswr.util.math.WKTWriter;

public class FileGDBReader extends DBReader
{
	private StreamData fd;
	private long currOfst;
	private FileGDBTableInfo tableInfo;
	private int rowSize;
	private byte []rowData;
	private int objectId;
	private boolean []fieldNull;
	private int []fieldOfst;
	private List<Integer> colList;

	private FileGDBFieldInfo getField(int fieldIndex)
	{
		List<FileGDBFieldInfo> fields = this.tableInfo.getFields();
		if (fieldIndex < 0 || fieldIndex >= fields.size())
		{
			return null;
		}
		return fields.get(fieldIndex);
	}

	private int getFieldIndex(int colIndex)
	{
		if (this.colList == null || this.colList.size() == 0)
		{
			return colIndex;
		}
		if (colIndex < 0 || colIndex >= this.colList.size())
		{
			return -1;
		}
		return this.colList.get(colIndex);
	}

	public FileGDBReader(StreamData fd, long ofst, FileGDBTableInfo tableInfo, List<String> colList)
	{
		this.fd = fd.getPartialData(ofst, fd.getDataSize() - ofst);
		this.currOfst = 0;
		this.tableInfo = tableInfo.clone();
		this.rowSize = 0;
		this.rowData = null;
		this.objectId = 0;
		int fieldCnt = tableInfo.getFields().size();
		this.fieldNull = new boolean[fieldCnt];
		this.fieldOfst = new int[fieldCnt];
		if (colList == null || colList.size() == 0)
		{
			this.colList = null;
			return;
		}
		List<FileGDBFieldInfo> fields = this.tableInfo.getFields();
		Map<String, Integer> fieldMap = new HashMap<String, Integer>();
		int i = 0;
		int j = fields.size();
		while (i < j)
		{
			fieldMap.put(fields.get(i).getName().toUpperCase(), i);
			i++;
		}
		i = 0;
		j = colList.size();
		this.colList = new ArrayList<Integer>();
		Integer fieldIndex;
		while (i < j)
		{
			fieldIndex = fieldMap.get(colList.get(i).toUpperCase());
			if (fieldIndex == null)
			{
				throw new IllegalArgumentException("Column name "+colList.get(i)+" not found");
			}
			this.colList.add(fieldIndex);
			i++;
		}
	}

	public void close()
	{
		this.fd.close();
	}

	public boolean readNext()
	{
		byte []sizeBuff = new byte[4];
		this.rowData = null;
		while (true)
		{
			if (this.fd.getRealData(this.currOfst, 4, sizeBuff, 0) != 4)
			{
				return false;
			}
			int size = ByteTool.readInt32(sizeBuff, 0);
			if (size < 0)
			{
				this.currOfst += 4 + -size;
			}
			else
			{
				this.rowSize = size;
				break;
			}
		}
	
		if (this.currOfst + 4 + this.rowSize > this.fd.getDataSize())
		{
			return false;
		}
		this.rowData = new byte[this.rowSize];
		if (this.fd.getRealData(this.currOfst + 4, this.rowSize, this.rowData, 0) != this.rowSize)
		{
			this.rowData = null;
			return false;
		}
		this.objectId++;
		this.currOfst += 4 + this.rowSize;
		int rowOfst = (this.tableInfo.getNullableCnt() + 7) >> 3;
		int nullIndex = 0;
		FileGDBFieldInfo field;
		SharedLong v = new SharedLong();
		int i = 0;
		int j = this.tableInfo.getFields().size();
		while (i < j)
		{
			field = this.tableInfo.getFields().get(i);
			this.fieldNull[i] = false;
			if ((field.getFlags() & 1) != 0)
			{
				this.fieldNull[i] = ((this.rowData[(nullIndex >> 3)] & (1 << (nullIndex & 7))) != 0);
				nullIndex++;
			}
			this.fieldOfst[i] = rowOfst;
			if (!this.fieldNull[i])
			{
				switch (field.getFieldType())
				{
				case 0:
					rowOfst += 2;
					break;
				case 1:
					rowOfst += 4;
					break;
				case 2:
					rowOfst += 4;
					break;
				case 3:
					rowOfst += 8;
					break;
				case 4:
					rowOfst = FileGDBUtil.readVarUInt(this.rowData, rowOfst, v);
					rowOfst += (int)v.value;
					break;
				case 5:
					rowOfst += 8;
					break;
				case 6:
					break;
				case 7:
					rowOfst = FileGDBUtil.readVarUInt(this.rowData, rowOfst, v);
					rowOfst += (int)v.value;
					break;
				case 8:
					rowOfst = FileGDBUtil.readVarUInt(this.rowData, rowOfst, v);
					rowOfst += (int)v.value;
					break;
				case 9:
					//////////////////////////////
					break;
				case 10:
				case 11:
					rowOfst += 16;
					break;
				case 12:
					rowOfst = FileGDBUtil.readVarUInt(this.rowData, rowOfst, v);
					rowOfst += (int)v.value;
					break;
				}
			}
			i++;
		}
		return true;
	}

	public int colCount()
	{
		if (this.colList == null)
		{
			return this.tableInfo.getFields().size();
		}
		else
		{
			return this.colList.size();
		}
	}

	public int getRowChanged()
	{
		return 0;
	}

	public int getInt32(int colIndex)
	{
		if (this.rowData == null)
		{
			return 0;
		}
		int fieldIndex = getFieldIndex(colIndex);
		FileGDBFieldInfo field = this.getField(fieldIndex);
		if (field == null || this.fieldNull[fieldIndex])
		{
			return 0;
		}
		switch (field.getFieldType())
		{
		case 0:
			return ByteTool.readInt16(this.rowData, this.fieldOfst[fieldIndex]);
		case 1:
			return ByteTool.readInt32(this.rowData, this.fieldOfst[fieldIndex]);
		case 2:
			return (int)ByteTool.readSingle(this.rowData, this.fieldOfst[fieldIndex]);
		case 3:
			return (int)ByteTool.readDouble(this.rowData, this.fieldOfst[fieldIndex]);
		case 5:
			return (int)ByteTool.readDouble(this.rowData, this.fieldOfst[fieldIndex]);
		case 6:
			return this.objectId;
		}
		return 0;
	}

	public long getInt64(int colIndex)
	{
		if (this.rowData == null)
		{
			return 0;
		}
		int fieldIndex = getFieldIndex(colIndex);
		FileGDBFieldInfo field = this.getField(fieldIndex);
		if (field == null || this.fieldNull[fieldIndex])
		{
			return 0;
		}
		switch (field.getFieldType())
		{
		case 0:
			return ByteTool.readInt16(this.rowData, this.fieldOfst[fieldIndex]);
		case 1:
			return ByteTool.readInt32(this.rowData, this.fieldOfst[fieldIndex]);
		case 2:
			return (long)ByteTool.readSingle(this.rowData, this.fieldOfst[fieldIndex]);
		case 3:
			return (long)ByteTool.readDouble(this.rowData, this.fieldOfst[fieldIndex]);
		case 5:
			return (long)ByteTool.readDouble(this.rowData, this.fieldOfst[fieldIndex]);
		case 6:
			return this.objectId;
		}
		return 0;
	}

	public String getString(int colIndex)
	{
		if (this.rowData == null)
		{
			return null;
		}
		int fieldIndex = getFieldIndex(colIndex);
		FileGDBFieldInfo field = this.getField(fieldIndex);
		if (field == null || this.fieldNull[fieldIndex])
		{
			return null;
		}
		SharedLong v = new SharedLong();
		int ofst;
		switch (field.getFieldType())
		{
		case 0:
			return String.valueOf(ByteTool.readInt16(this.rowData, this.fieldOfst[fieldIndex]));
		case 1:
			return String.valueOf(ByteTool.readInt32(this.rowData, this.fieldOfst[fieldIndex]));
		case 2:
			return String.valueOf(ByteTool.readSingle(this.rowData, this.fieldOfst[fieldIndex]));
		case 3:
			return String.valueOf(ByteTool.readDouble(this.rowData, this.fieldOfst[fieldIndex]));
		case 12:
		case 4:
			ofst = FileGDBUtil.readVarUInt(this.rowData, this.fieldOfst[fieldIndex], v);
			return new String(this.rowData, ofst, (int)v.value, StandardCharsets.UTF_8);
		case 5:
			{
				ZonedDateTime dt = FileGDBUtil.toDateTime(ByteTool.readDouble(this.rowData, this.fieldOfst[fieldIndex]));
				return DateTimeUtil.toString(dt, "yyyy-MM-dd HH:mm:ss.fff");
			}
		case 6:
			return String.valueOf(this.objectId);
		case 7:
			{
				Vector2D vec = this.getVector(fieldIndex);
				if (vec != null)
				{
					WKTWriter writer = new WKTWriter();
					return writer.generateWKT(vec);
				}
			}
			return null;
		case 8:
			{
				byte[] binBuff = this.getBinary(fieldIndex);
				return StringUtil.toHex(binBuff, 0, binBuff.length, (char)0);
			}
		case 10:
		case 11:
			{
				return new UUID(this.rowData, this.fieldOfst[fieldIndex]).toString();
			}
		}
		return null;
	}

	public ZonedDateTime getDate(int colIndex)
	{
		if (this.rowData == null)
		{
			return null;
		}
		int fieldIndex = getFieldIndex(colIndex);
		FileGDBFieldInfo field = this.getField(fieldIndex);
		if (field == null)
		{
			return null;
		}
		else if (this.fieldNull[fieldIndex])
		{
			return null;
		}
		switch (field.getFieldType())
		{
		case 5:
			return FileGDBUtil.toDateTime(ByteTool.readDouble(this.rowData, this.fieldOfst[fieldIndex]));
		}
		return null;
	}

	public double getDbl(int colIndex)
	{
		if (this.rowData == null)
		{
			return 0;
		}
		int fieldIndex = getFieldIndex(colIndex);
		FileGDBFieldInfo field = this.getField(fieldIndex);
		if (field == null || this.fieldNull[fieldIndex])
		{
			return 0;
		}
		switch (field.getFieldType())
		{
		case 0:
			return ByteTool.readInt16(this.rowData, this.fieldOfst[fieldIndex]);
		case 1:
			return ByteTool.readInt32(this.rowData, this.fieldOfst[fieldIndex]);
		case 2:
			return ByteTool.readSingle(this.rowData, this.fieldOfst[fieldIndex]);
		case 3:
			return ByteTool.readDouble(this.rowData, this.fieldOfst[fieldIndex]);
		case 5:
			return ByteTool.readDouble(this.rowData, this.fieldOfst[fieldIndex]);
		case 6:
			return this.objectId;
		}
		return 0;
	}

	public boolean getBool(int colIndex)
	{
		return this.getInt32(colIndex) != 0;
	}

	public byte[] getBinary(int colIndex)
	{
		if (this.rowData == null)
		{
			return null;
		}
		int fieldIndex = getFieldIndex(colIndex);
		FileGDBFieldInfo field = this.getField(fieldIndex);
		if (field == null || this.fieldNull[fieldIndex])
		{
			return null;
		}
		SharedLong v = new SharedLong();
		int ofst;
		switch (field.getFieldType())
		{
		case 0:
			return Arrays.copyOfRange(this.rowData, this.fieldOfst[fieldIndex], this.fieldOfst[fieldIndex] + 2);
		case 1:
			return Arrays.copyOfRange(this.rowData, this.fieldOfst[fieldIndex], this.fieldOfst[fieldIndex] + 4);
		case 2:
			return Arrays.copyOfRange(this.rowData, this.fieldOfst[fieldIndex], this.fieldOfst[fieldIndex] + 4);
		case 3:
			return Arrays.copyOfRange(this.rowData, this.fieldOfst[fieldIndex], this.fieldOfst[fieldIndex] + 8);
		case 12:
		case 4:
			ofst = FileGDBUtil.readVarUInt(this.rowData, this.fieldOfst[fieldIndex], v);
			return Arrays.copyOfRange(this.rowData, ofst, ofst + (int)v.value);
		case 5:
			return Arrays.copyOfRange(this.rowData, this.fieldOfst[fieldIndex], this.fieldOfst[fieldIndex] + 8);
		case 6:
			{
				byte []ret = new byte[4];
				ByteTool.writeInt32(ret, 0, this.objectId);
				return ret;
			}
		case 7:
			ofst = FileGDBUtil.readVarUInt(this.rowData, this.fieldOfst[fieldIndex], v);
			return Arrays.copyOfRange(this.rowData, ofst, ofst + (int)v.value);
		case 8:
			ofst = FileGDBUtil.readVarUInt(this.rowData, this.fieldOfst[fieldIndex], v);
			return Arrays.copyOfRange(this.rowData, ofst, ofst + (int)v.value);
		case 10:
		case 11:
			return Arrays.copyOfRange(this.rowData, this.fieldOfst[fieldIndex], this.fieldOfst[fieldIndex] + 16);
		}
		return null;
	}

	public Vector2D getVector(int colIndex)
	{
		if (this.rowData == null)
		{
			return null;
		}
		int fieldIndex = getFieldIndex(colIndex);
		FileGDBFieldInfo field = this.getField(fieldIndex);
		if (field == null || this.fieldNull[fieldIndex])
		{
			return null;
		}
		if (field.getFieldType() != 7)
		{
			return null;
		}
		SharedLong geometryLen = new SharedLong();
		SharedLong geometryType = new SharedLong();
		int ofst;
		ofst = FileGDBUtil.readVarUInt(this.rowData, this.fieldOfst[fieldIndex], geometryLen);
		ofst = FileGDBUtil.readVarUInt(this.rowData, ofst, geometryType);
		double x;
		double y;
		double z = 0;
		SharedLong v = new SharedLong();
		int srid;
	/*
	#define SHPT_MULTIPOINT    8
	#define SHPT_MULTIPOINTM  28
	#define SHPT_MULTIPOINTZM 18
	#define SHPT_MULTIPOINTZ  20
	
	#define SHPT_MULTIPATCHM  31
	#define SHPT_MULTIPATCH   32
	
	#define SHPT_GENERALPOLYLINE    50
	#define SHPT_GENERALPOLYGON     51
	#define SHPT_GENERALPOINT       52
	#define SHPT_GENERALMULTIPOINT  53
	#define SHPT_GENERALMULTIPATCH  54*/
	
		switch ((int)(geometryType.value & 0xff))
		{
		case 1: //SHPT_POINT
		case 9: //SHPT_POINTZ
		case 11: //SHPT_POINTZM
		case 21: //SHPT_POINTM
			ofst = FileGDBUtil.readVarUInt(this.rowData, ofst, v);
			x = (double)(v.value - 1) / this.tableInfo.getXyScale() + this.tableInfo.getXOrigin();
			ofst = FileGDBUtil.readVarUInt(this.rowData, ofst, v);
			y = (double)(v.value - 1) / this.tableInfo.getXyScale() + this.tableInfo.getYOrigin();
			if ((this.tableInfo.getGeometryFlags() & 0x80) != 0)
			{
				ofst = FileGDBUtil.readVarUInt(this.rowData, ofst, v);
				z = (double)(v.value - 1) / this.tableInfo.getZScale() + this.tableInfo.getZOrigin();
			}
			if ((this.tableInfo.getGeometryFlags() & 0x40) != 0)
			{
				ofst = FileGDBUtil.readVarUInt(this.rowData, ofst, v);
				//m = (double)(v - 1) / this.tableInfo.mScale + this.tableInfo.mOrigin;
			}
			srid = 0;
			if (this.tableInfo.getCsys() != null)
			{
				srid = this.tableInfo.getCsys().getSRID();
			}
			if ((this.tableInfo.getGeometryType() & 0x80) != 0)
			{
				Point3D pt = new Point3D(srid, x, y, z);
				return pt;
			}
			else
			{
				Point2D pt = new Point2D(srid, x, y);
				return pt;
			}
		case 3: //SHPT_ARC
		case 10: //SHPT_ARCZ
		case 13: //SHPT_ARCZM
		case 23: //SHPT_ARCM
			if (this.rowData[ofst] == 0)
			{
				return null;
			}
			else
			{
				SharedLong nPoints = new SharedLong();
				SharedLong nParts = new SharedLong();
				ofst = FileGDBUtil.readVarUInt(this.rowData, ofst, nPoints);
				ofst = FileGDBUtil.readVarUInt(this.rowData, ofst, nParts);
				ofst = FileGDBUtil.readVarUInt(this.rowData, ofst, v); //xmin
				ofst = FileGDBUtil.readVarUInt(this.rowData, ofst, v); //ymin
				ofst = FileGDBUtil.readVarUInt(this.rowData, ofst, v); //xmax
				ofst = FileGDBUtil.readVarUInt(this.rowData, ofst, v); //ymax
				Polyline pl;
				srid = 0;
				if (this.tableInfo.getCsys() != null)
				{
					srid = this.tableInfo.getCsys().getSRID();
				}
				int i;
				int []parts;
				double []points;
				double []altitiudes = null;
				if ((this.tableInfo.getGeometryFlags() & 0x80) != 0)
				{
					pl = new Polyline3D(srid, (int)nParts.value, (int)nPoints.value);
					altitiudes = ((Polyline3D)pl).getAltitudeList();
				}
				else
				{
					pl = new Polyline(srid, (int)nParts.value, (int)nPoints.value);
	
				}
				parts = pl.getPtOfstList();
				points = pl.getPointList();
				parts[0] = 0;
				int ptOfst = 0;
				i = 1;
				while (i < nParts.value)
				{
					ofst = FileGDBUtil.readVarUInt(this.rowData, ofst, v);
					ptOfst += (int)v.value;
					parts[i] = ptOfst;
					i++;
				}
				SharedLong iv = new SharedLong();
				int dx = 0;
				int dy = 0;
				i = 0;
				while (i < nPoints.value)
				{
					ofst = FileGDBUtil.readVarInt(this.rowData, ofst, iv);
					dx -= (int)iv.value;
					x = (double)(dx) / this.tableInfo.getXyScale() + this.tableInfo.getXOrigin();
					ofst = FileGDBUtil.readVarInt(this.rowData, ofst, iv);
					dy -= (int)iv.value;
					y = (double)(dy) / this.tableInfo.getXyScale() + this.tableInfo.getYOrigin();
					points[i * 2] = x;
					points[i * 2 + 1] = y;
					i++;
				}
				if ((this.tableInfo.getGeometryFlags() & 0x80) != 0)
				{
					dx = 0;
					i = 0;
					while (i < nPoints.value)
					{
						ofst = FileGDBUtil.readVarInt(this.rowData, ofst, iv);
						dx -= iv.value;
						z = (double)(dx) / this.tableInfo.getZScale() + this.tableInfo.getZOrigin();
						altitiudes[i] = z;
						i++;
					}
				}
				if ((this.tableInfo.getGeometryFlags() & 0x40) != 0)
				{
					dx = 0;
					i = 0;
					while (i < nPoints.value)
					{
						ofst = FileGDBUtil.readVarInt(this.rowData, ofst, iv);
						dx -= iv.value;
						//m = Math::Int64_Double(dx) / this.tableInfo.mScale + this.tableInfo.mOrigin;
						i++;
					}
				}
				return pl;
			}
		case 5: //SHPT_POLYGON
		case 15: //SHPT_POLYGONZM
		case 19: //SHPT_POLYGONZ
		case 25: //SHPT_POLYGONM
			if (this.rowData[ofst] == 0)
			{
				return null;
			}
			else
			{
				SharedLong nPoints = new SharedLong();
				SharedLong nParts = new SharedLong();
				ofst = FileGDBUtil.readVarUInt(this.rowData, ofst, nPoints);
				ofst = FileGDBUtil.readVarUInt(this.rowData, ofst, nParts);
				ofst = FileGDBUtil.readVarUInt(this.rowData, ofst, v); //xmin
				ofst = FileGDBUtil.readVarUInt(this.rowData, ofst, v); //ymin
				ofst = FileGDBUtil.readVarUInt(this.rowData, ofst, v); //xmax
				ofst = FileGDBUtil.readVarUInt(this.rowData, ofst, v); //ymax
				Polygon pg;
				srid = 0;
				if (this.tableInfo.getCsys() != null)
				{
					srid = this.tableInfo.getCsys().getSRID();
				}
				pg = new Polygon(srid, (int)nParts.value, (int)nPoints.value);
				int i;
				int []parts = pg.getPtOfstList();
				double []points = pg.getPointList();
				parts[0] = 0;
				int ptOfst = 0;
				i = 1;
				while (i < nParts.value)
				{
					ofst = FileGDBUtil.readVarUInt(this.rowData, ofst, v);
					ptOfst += (int)v.value;
					parts[i] = ptOfst;
					i++;
				}
				SharedLong iv = new SharedLong();
				int dx = 0;
				int dy = 0;
				i = 0;
				while (i < nPoints.value)
				{
					ofst = FileGDBUtil.readVarInt(this.rowData, ofst, iv);
					dx += (int)iv.value;
					x = (double)(dx) / this.tableInfo.getXyScale() + this.tableInfo.getXOrigin();
					ofst = FileGDBUtil.readVarInt(this.rowData, ofst, iv);
					dy += (int)iv.value;
					y = (double)(dy) / this.tableInfo.getXyScale() + this.tableInfo.getYOrigin();
					points[i * 2] = x;
					points[i * 2 + 1] = y;
					i++;
				}
				if ((this.tableInfo.getGeometryFlags() & 0x80) != 0)
				{
					dx = 0;
					i = 0;
					while (i < nPoints.value)
					{
						ofst = FileGDBUtil.readVarInt(this.rowData, ofst, iv);
						dx += iv.value;
						z = (double)(dx) / this.tableInfo.getZScale() + this.tableInfo.getZOrigin();
						i++;
					}
				}
				if ((this.tableInfo.getGeometryFlags() & 0x40) != 0)
				{
					dx = 0;
					i = 0;
					while (i < nPoints.value)
					{
						ofst = FileGDBUtil.readVarInt(this.rowData, ofst, iv);
						dx += iv.value;
						//m = Math::Int64_Double(dx) / this.tableInfo.mScale + this.tableInfo.mOrigin;
						i++;
					}
				}
				return pg;
			}
		case 50: //SHPT_GENERALPOLYLINE
			if (this.rowData[ofst] == 0)
			{
				return null;
			}
			else
			{
				SharedLong nPoints = new SharedLong();
				SharedLong nParts = new SharedLong();
				SharedLong nCurves = new SharedLong();
				ofst = FileGDBUtil.readVarUInt(this.rowData, ofst, nPoints);
				ofst = FileGDBUtil.readVarUInt(this.rowData, ofst, nParts);
				if ((geometryType.value & 0x20000000) != 0)
				{
					ofst = FileGDBUtil.readVarUInt(this.rowData, ofst, nCurves);
				}
				ofst = FileGDBUtil.readVarUInt(this.rowData, ofst, v); //xmin
				ofst = FileGDBUtil.readVarUInt(this.rowData, ofst, v); //ymin
				ofst = FileGDBUtil.readVarUInt(this.rowData, ofst, v); //xmax
				ofst = FileGDBUtil.readVarUInt(this.rowData, ofst, v); //ymax
				Polyline pl;
				srid = 0;
				int i;
				if (this.tableInfo.getCsys() != null)
				{
					srid = this.tableInfo.getCsys().getSRID();
				}
				int []parts;
				double []points;
				double []altitiudes = null;
				if ((geometryType.value & 0x80000000) != 0)
				{
					pl = new Polyline3D(srid, (int)nParts.value, (int)nPoints.value);
					altitiudes = ((Polyline3D)pl).getAltitudeList();
				}
				else
				{
					pl = new Polyline(srid, (int)nParts.value, (int)nPoints.value);
				}
				parts = pl.getPtOfstList();
				points = pl.getPointList();
				parts[0] = 0;
				int ptOfst = 0;
				i = 1;
				while (i < nParts.value)
				{
					ofst = FileGDBUtil.readVarUInt(this.rowData, ofst, v);
					ptOfst += (int)v.value;
					parts[i] = ptOfst;
					i++;
				}
				int j;
				int k;
				i = 0;
				while (i < nParts.value)
				{
					SharedLong iv = new SharedLong();
					int dx = 0;
					int dy = 0;
					j = parts[i];
					if (i + 1 < nParts.value)
					{
						k = parts[i + 1];
					}
					else
					{
						k = (int)nPoints.value;
					}
					while (j < k)
					{
						ofst = FileGDBUtil.readVarInt(this.rowData, ofst, iv);
						dx -= iv.value;
						x = (double)(dx) / this.tableInfo.getXyScale() + this.tableInfo.getXOrigin();
						ofst = FileGDBUtil.readVarInt(this.rowData, ofst, iv);
						dy -= iv.value;
						y = (double)(dy) / this.tableInfo.getXyScale() + this.tableInfo.getYOrigin();
						points[j * 2] = x;
						points[j * 2 + 1] = y;
						j++;
					}
					if ((geometryType.value & 0x80000000) != 0)
					{
						dx = 0;
						j = parts[i];
						while (j < k)
						{
							ofst = FileGDBUtil.readVarInt(this.rowData, ofst, iv);
							dx -= iv.value;
							z = (double)(dx) / this.tableInfo.getZScale() + this.tableInfo.getZOrigin();
							altitiudes[j] = z;
							j++;
						}
					}
					if ((geometryType.value & 0x40000000) != 0)
					{
						dx = 0;
						j = parts[i];
						while (j < k)
						{
							ofst = FileGDBUtil.readVarInt(this.rowData, ofst, iv);
							dx -= iv.value;
							//m = (double)(dx) / this.tableInfo.mScale + this.tableInfo.mOrigin;
						}
					}
					i++;
				}
			}
			break;
		}
		return null;
	}

	public Object getObject(int colIndex)
	{
		if (this.rowData == null)
		{
			return null;
		}
		int fieldIndex = getFieldIndex(colIndex);
		FileGDBFieldInfo field = this.getField(fieldIndex);
		if (field == null || this.fieldNull[fieldIndex])
		{
			return null;
		}
		SharedLong v = new SharedLong();
		int ofst;
		switch (field.getFieldType())
		{
		case 0:
			return ByteTool.readInt16(this.rowData, this.fieldOfst[fieldIndex]);
		case 1:
			return ByteTool.readInt32(this.rowData, this.fieldOfst[fieldIndex]);
		case 2:
			return ByteTool.readSingle(this.rowData, this.fieldOfst[fieldIndex]);
		case 3:
			return ByteTool.readDouble(this.rowData, this.fieldOfst[fieldIndex]);
		case 12:
		case 4:
			ofst = FileGDBUtil.readVarUInt(this.rowData, this.fieldOfst[fieldIndex], v);
			return new String(this.rowData, ofst, (int)v.value, StandardCharsets.UTF_8);
		case 5:
			return FileGDBUtil.toDateTime(ByteTool.readDouble(this.rowData, this.fieldOfst[fieldIndex]));
		case 6:
			return this.objectId;
		case 7:
			return this.getVector(colIndex);
		case 8:
			return this.getBinary(colIndex);
		case 10:
		case 11:
			return new UUID(this.rowData, this.fieldOfst[fieldIndex]);
		}
		return null;
	}

	public boolean isNull(int colIndex)
	{
		int fieldIndex = getFieldIndex(colIndex);
		if (fieldIndex < 0 || fieldIndex >= this.tableInfo.getFields().size())
		{
			return true;
		}
		return this.fieldNull[fieldIndex];
	}

	public String getName(int colIndex)
	{
		int fieldIndex = getFieldIndex(colIndex);
		FileGDBFieldInfo field = this.getField(fieldIndex);
		if (field != null)
		{
			return field.getName();
		}
		return null;
	}

	public ColumnType getColumnType(int colIndex)
	{
		int fieldIndex = getFieldIndex(colIndex);
		FileGDBFieldInfo field = this.getField(fieldIndex);
		if (field != null)
		{
			switch (field.getFieldType())
			{
			case 0:
				return ColumnType.Int16;
			case 1:
				return ColumnType.Int32;
			case 2:
				return ColumnType.Float;
			case 3:
				return ColumnType.Double;
			case 4:
				return ColumnType.VarChar;
			case 5:
				return ColumnType.DateTime;
			case 6:
				return ColumnType.Int32;
			case 7:
				return ColumnType.Vector;
			case 8:
				return ColumnType.Binary;
			case 9:
				return ColumnType.Binary;
			case 10:
				return ColumnType.UUID;
			case 11:
				return ColumnType.UUID;
			case 12:
				return ColumnType.VarChar;
			default:
				return ColumnType.Unknown;
			}
		}
		return ColumnType.Unknown;
	}
	
	public ColumnDef getColumnDef(int colIndex)
	{
		int fieldIndex = getFieldIndex(colIndex);
		FileGDBFieldInfo field = this.getField(fieldIndex);
		if (field == null)
		{
			return null;
		}
		ColumnDef colDef = new ColumnDef(field.getName());
		colDef.setColSize(field.getFieldSize());
		colDef.setColType(this.getColumnType(fieldIndex));
		colDef.setNotNull((field.getFlags() & 1) == 0);
		colDef.setPk(field.getFieldType() == 6);
		colDef.setAutoInc(field.getFieldType() == 6);
		if (field.getDefValue() != null)
		{
			if (field.getFieldType() == 0)
			{
				if (field.getDefValue().length == 2)
				{
					colDef.setDefVal(String.valueOf(ByteTool.readInt16(field.getDefValue(), 0)));
				}
			}
			else if (field.getFieldType() == 1)
			{
				if (field.getDefValue().length == 4)
				{
					colDef.setDefVal(String.valueOf(ByteTool.readInt32(field.getDefValue(), 0)));
				}
			}
			else if (field.getFieldType() == 2)
			{
				if (field.getDefValue().length == 4)
				{
					colDef.setDefVal(String.valueOf(ByteTool.readSingle(field.getDefValue(), 0)));
				}
			}
			else if (field.getFieldType() == 3)
			{
				if (field.getDefValue().length == 8)
				{
					colDef.setDefVal(String.valueOf(ByteTool.readDouble(field.getDefValue(), 0)));
				}
			}
			else if (field.getFieldType() == 4)
			{
				colDef.setDefVal(new String(field.getDefValue(), StandardCharsets.UTF_8));
			}
			else if (field.getFieldType() == 5)
			{
				if (field.getDefValue().length == 8)
				{
					colDef.setDefVal(DateTimeUtil.toString(FileGDBUtil.toDateTime(ByteTool.readDouble(field.getDefValue(), 0)), "yyyy-MM-dd HH:mm:ss.fff"));
				}
			}
		}
		return colDef;
	}
}
