package org.sswr.util.map;

import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.locationtech.jts.geom.Geometry;
import org.sswr.util.data.ByteTool;
import org.sswr.util.data.DateTimeUtil;
import org.sswr.util.data.GeometryUtil;
import org.sswr.util.data.ObjectGetter;
import org.sswr.util.data.QueryConditions;
import org.sswr.util.data.SharedLong;
import org.sswr.util.data.StringUtil;
import org.sswr.util.data.UUID;
import org.sswr.util.db.AutoIncType;
import org.sswr.util.db.ColumnDef;
import org.sswr.util.db.ColumnType;
import org.sswr.util.db.DBReader;
import org.sswr.util.db.GeometryType;
import org.sswr.util.io.StreamData;
import org.sswr.util.map.esri.ESRICurve;
import org.sswr.util.math.Coord2DDbl;
import org.sswr.util.math.CoordinateSystem;
import org.sswr.util.math.WKTWriter;
import org.sswr.util.math.geometry.LineString;
import org.sswr.util.math.geometry.Point2D;
import org.sswr.util.math.geometry.PointM;
import org.sswr.util.math.geometry.PointZ;
import org.sswr.util.math.geometry.PointZM;
import org.sswr.util.math.geometry.Polygon;
import org.sswr.util.math.geometry.Polyline;
import org.sswr.util.math.geometry.Vector2D;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class FileGDBReader extends DBReader implements ObjectGetter
{
	private @Nonnull StreamData fd;
	private long currOfst;
	private long rowOfst;
	private @Nonnull FileGDBTableInfo tableInfo;
	private int rowSize;
	private byte []rowData;
	private int objectId;
	private boolean []fieldNull;
	private int []fieldOfst;
	private List<Integer> columnIndices;
	private int dataOfst;
	private int maxCnt;
	private int maxRowSize;
	private @Nullable QueryConditions conditions;

	private int indexCnt;
	private int indexNext;
	private byte[] indexBuff;


	@Nullable
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
		if (this.columnIndices != null)
		{
			if (colIndex >= this.columnIndices.size() || colIndex < 0)
			{
				return -1;
			}
			else
			{
				return this.columnIndices.get(colIndex);
			}
		}
		else
		{
			return colIndex;
		}
	}

	public FileGDBReader(@Nonnull StreamData fd, long ofst, @Nonnull FileGDBTableInfo tableInfo, @Nullable List<String> columnNames, int dataOfst, int maxCnt, @Nullable QueryConditions conditions, int maxRowSize)
	{
		this.indexCnt = 0;
		this.indexNext = 0;
		this.fd = fd.getPartialData(0, fd.getDataSize());
		this.currOfst = ofst;
		this.rowOfst = ofst;
		this.tableInfo = tableInfo.clone();
		this.rowSize = 0;
		this.objectId = 0;
		int fieldCnt = tableInfo.getFields().size();
		this.fieldNull = new boolean[fieldCnt];
		this.fieldOfst = new int[fieldCnt];
		this.dataOfst = dataOfst;
		this.maxRowSize = maxRowSize;
		this.maxCnt = maxCnt;
		if (this.maxCnt == 0)
		{
			this.maxCnt = -1;
		}
		this.columnIndices = null;
		this.conditions = conditions;
		List<String> nncolumnNames;
		if ((nncolumnNames = columnNames) != null)
		{
			this.columnIndices = new ArrayList<Integer>();
			int i = 0;
			int j = nncolumnNames.size();
			int k = 0;
			while (i < j)
			{
				boolean found = false;
				String name;
				if ((name = nncolumnNames.get(i)) != null)
				{
					k = this.tableInfo.getFields().size();
					while (k-- > 0)
					{
						if (this.tableInfo.getFields().get(k).getName().equals(name))
						{
							found = true;
							break;
						}
					}
				}
				if (!found)
				{
					this.columnIndices.clear();
					break;
				}
				this.columnIndices.add(k);
				i++;
			}
		}
	}

	public void close()
	{
		this.fd.close();
	}

	public boolean readNext()
	{
		QueryConditions nncondition;
		byte[] sizeBuff = new byte[4];
		while (true)
		{
			this.rowData = null;
			if (this.indexBuff.length > 0)
			{
				while (true)
				{
					if (this.indexNext >= this.indexCnt)
					{
						return false;
					}

					this.currOfst = ByteTool.readInt32(this.indexBuff, this.indexNext * 5 + 1);
					this.currOfst = (this.currOfst << 8) + (this.indexBuff[this.indexNext * 5] & 0xff);
					this.indexNext++;
					this.objectId = this.indexNext;
					if (this.currOfst != 0)
					{
						if (this.fd.getRealData(this.currOfst, 4, sizeBuff, 0) != 4)
						{
							return false;
						}
						this.rowOfst = this.currOfst;
						int size = ByteTool.readInt32(sizeBuff, 0);
						if (size < 0)
						{
							return false;
						}
						else
						{
							this.rowSize = size;
						}
						if (this.currOfst + 4 + this.rowSize > this.fd.getDataSize())
						{
							return false;
						}
						break;
					}
				}
			}
			else
			{
				boolean lastIsFree = false;
				while (true)
				{
					if (this.fd.getRealData(this.currOfst, 4, sizeBuff, 0) != 4)
					{
						return false;
					}
					int size = ByteTool.readInt32(sizeBuff, 0);
					if (size < 0)
					{
						this.currOfst += 4 + (-size);
						lastIsFree = true;
					}
					else if (size > this.maxRowSize && lastIsFree)
					{
						this.currOfst += 4;
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
				this.objectId++;
			}
			this.rowData = new byte[this.rowSize];
			if (this.fd.getRealData(this.currOfst + 4, this.rowSize, this.rowData, 0) != this.rowSize)
			{
				this.rowData = null;
				return false;
			}
			this.rowOfst = this.currOfst;
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
						if (rowOfst + v.value > this.rowSize)
						{
							return false;
						}
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
			boolean valid = true;
			if ((nncondition = conditions) != null)
			{
				try
				{
					valid = nncondition.isValid(this);
				}
				catch (IllegalAccessException | InvocationTargetException ex)
				{
					valid = true;
				}
			}
			if (valid)
			{
				if (this.dataOfst == 0)
				{
					if (this.maxCnt == 0)
					{
						return false;
					}
					this.maxCnt--;
					break;
				}
				this.dataOfst--;
			}
		}
		return true;
	}

	public int colCount()
	{
		if (this.columnIndices != null)
		{
			return this.columnIndices.size();
		}
		else
		{
			return this.tableInfo.getFields().size();
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
		case 4:
		case 12:
			{
				SharedLong v = new SharedLong();
				int ofst = FileGDBUtil.readVarUInt(this.rowData, this.fieldOfst[fieldIndex], v);
				return StringUtil.toIntegerS(new String(this.rowData, ofst, (int)v.value, StandardCharsets.UTF_8), 0);
			}
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
		case 4:
		case 12:
			{
				SharedLong v = new SharedLong();
				int ofst = FileGDBUtil.readVarUInt(this.rowData, this.fieldOfst[fieldIndex], v);
				return StringUtil.toLongS(new String(this.rowData, ofst, (int)v.value, StandardCharsets.UTF_8), 0);
			}
		}
		return 0;
	}

	//////////////////////////////////////////////////////////
	@Nullable
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
				return DateTimeUtil.toStringNoZone(dt);
			}
		case 6:
			return String.valueOf(this.objectId);
		case 7:
			{
				Vector2D vec = this.getVector(colIndex);
				if (vec != null)
				{
					WKTWriter writer = new WKTWriter();
					return writer.generateWKT(vec);
				}
			}
			return null;
		case 8:
			{
				byte[] binBuff = this.getBinary(colIndex);
				if (binBuff == null)
				{
					return null;
				}
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

	@Nullable
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
		case 4:
		case 12:
			{
				SharedLong v = new SharedLong();
				int ofst = FileGDBUtil.readVarUInt(this.rowData, this.fieldOfst[fieldIndex], v);
				String s = new String(this.rowData, ofst, (int)v.value);
				return DateTimeUtil.parse(s);
			}
		}
		return null;
	}

	public double getDblOrNAN(int colIndex)
	{
		if (this.rowData == null)
		{
			return Double.NaN;
		}
		int fieldIndex = getFieldIndex(colIndex);
		FileGDBFieldInfo field = this.getField(fieldIndex);
		if (field == null || this.fieldNull[fieldIndex])
		{
			return Double.NaN;
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
		case 4:
		case 12:
			{
				SharedLong v = new SharedLong();
				int ofst = FileGDBUtil.readVarUInt(this.rowData, this.fieldOfst[fieldIndex], v);
				return StringUtil.toDoubleS(new String(this.rowData, ofst, (int)v.value, StandardCharsets.UTF_8), Double.NaN);
			}
		}
		return Double.NaN;
	}

	public boolean getBool(int colIndex)
	{
		return this.getInt32(colIndex) != 0;
	}

	@Nullable
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

	@Nullable
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
		double m = 0;
		SharedLong v = new SharedLong();
		int srid;
		CoordinateSystem csys;
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
				m = (double)(v.value - 1) / this.tableInfo.getMScale() + this.tableInfo.getMOrigin();
			}
			srid = 0;
			if ((csys = this.tableInfo.getCsys()) != null)
			{
				srid = csys.getSRID();
			}
			if ((this.tableInfo.getGeometryType() & 0x80) != 0)
			{
				if ((this.tableInfo.getGeometryType() & 0x40) != 0)
				{
					return new PointZM(srid, x, y, z, m);
				}
				else
				{
					return new PointZ(srid, x, y, z);
				}
			}
			else
			{
				if ((this.tableInfo.getGeometryType() & 0x40) != 0)
				{
					return new PointM(srid, x, y, m);
				}
				else
				{
					return new Point2D(srid, x, y);
				}
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
				if ((csys = this.tableInfo.getCsys()) != null)
				{
					srid = csys.getSRID();
				}
				int i;
				int j;
				int k;
				LineString lineString;
				int []ptOfstList;
				Coord2DDbl []points;
				double []zArr;
				double []mArr;
				pl = new Polyline(srid);
				ptOfstList = new int[(int)nParts.value];
				ptOfstList[0] = 0;
				int ptOfst = 0;
				i = 1;
				while (i < nParts.value)
				{
					ofst = FileGDBUtil.readVarUInt(this.rowData, ofst, v);
					ptOfst += (int)v.value;
					ptOfstList[i] = ptOfst;
					i++;
				}
				SharedLong iv = new SharedLong();
				long dx = 0;
				long dy = 0;
				long dz = 0;
				long dm = 0;
				i = 0;
				while (i < nParts.value)
				{
					if (i + 1 == nParts.value)
					{
						k = (int)nPoints.value - ptOfstList[i];
					}
					else
					{
						k = ptOfstList[i + 1] - ptOfstList[i];
					}
					lineString = new LineString(srid, k, (this.tableInfo.getGeometryFlags() & 0x80) != 0, (this.tableInfo.getGeometryFlags() & 0x40) != 0);
					points = lineString.getPointList();
					zArr = lineString.getZList();
					mArr = lineString.getMList();
	
					j = 0;
					while (j < k)
					{
						ofst = FileGDBUtil.readVarInt(this.rowData, ofst, iv);
						dx += iv.value;
						x = (double)(dx) / this.tableInfo.getXyScale() + this.tableInfo.getXOrigin();
						ofst = FileGDBUtil.readVarInt(this.rowData, ofst, iv);
						dy += iv.value;
						y = (double)(dy) / this.tableInfo.getXyScale() + this.tableInfo.getYOrigin();
						points[j] = new Coord2DDbl(x, y);
						j++;
					}
					if ((this.tableInfo.getGeometryFlags() & 0x80) != 0 && zArr != null)
					{
						j = 0;
						while (j < k)
						{
							ofst = FileGDBUtil.readVarInt(this.rowData, ofst, iv);
							dz += iv.value;
							zArr[j] = (double)(dz) / this.tableInfo.getZScale() + this.tableInfo.getZOrigin();
							j++;
						}
					}
					if ((this.tableInfo.getGeometryFlags() & 0x40) != 0 && mArr != null)
					{
						j = 0;
						while (j < k)
						{
							ofst = FileGDBUtil.readVarInt(this.rowData, ofst, iv);
							dm += iv.value;
							mArr[j] = (double)(dm) / this.tableInfo.getMScale() + this.tableInfo.getMOrigin();
							j++;
						}
					}
					pl.addGeometry(lineString);

					i++;
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
				if ((csys = this.tableInfo.getCsys()) != null)
				{
					srid = csys.getSRID();
				}
				pg = new Polygon(srid);
				int i;
				int []parts = new int[(int)nParts.value];
				Coord2DDbl []points = new Coord2DDbl[(int)nPoints.value];
				double []zArr = null;
				double []mArr = null;
				if ((this.tableInfo.getGeometryFlags() & 0x80) != 0)
				{
					zArr = new double[(int)nPoints.value];
				}
				if ((this.tableInfo.getGeometryFlags() & 0x40) != 0)
				{
					mArr = new double[(int)nPoints.value];
				}
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
				long dx = 0;
				long dy = 0;
				i = 0;
				while (i < nPoints.value)
				{
					ofst = FileGDBUtil.readVarInt(this.rowData, ofst, iv);
					dx += iv.value;
					x = (double)(dx) / this.tableInfo.getXyScale() + this.tableInfo.getXOrigin();
					ofst = FileGDBUtil.readVarInt(this.rowData, ofst, iv);
					dy += iv.value;
					y = (double)(dy) / this.tableInfo.getXyScale() + this.tableInfo.getYOrigin();
					points[i] = new Coord2DDbl(x, y);
					i++;
				}
				if ((this.tableInfo.getGeometryFlags() & 0x80) != 0 && zArr != null)
				{
					dx = 0;
					i = 0;
					while (i < nPoints.value)
					{
						ofst = FileGDBUtil.readVarInt(this.rowData, ofst, iv);
						dx += iv.value;
						zArr[i] = (double)(dx) / this.tableInfo.getZScale() + this.tableInfo.getZOrigin();
						i++;
					}
				}
				if ((this.tableInfo.getGeometryFlags() & 0x40) != 0 && mArr != null)
				{
					dx = 0;
					i = 0;
					while (i < nPoints.value)
					{
						ofst = FileGDBUtil.readVarInt(this.rowData, ofst, iv);
						dx += iv.value;
						mArr[i] = (double)(dx) / this.tableInfo.getMScale() + this.tableInfo.getMOrigin();
						i++;
					}
				}
				pg.addFromPtOfst(parts, points, zArr, mArr);
				return pg.createMultiPolygon();
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
				srid = 0;
				if ((csys = this.tableInfo.getCsys()) != null)
				{
					srid = csys.getSRID();
				}
				int i;
				int j;
				int[] ptOfstList;
				Coord2DDbl[] points;
				double[] zArr = null;
				double[] mArr = null;
	
				double[] nnArr;
				ptOfstList = new int[(int)nParts.value];
				ptOfstList[0] = 0;
				int ptOfst = 0;
				i = 1;
				while (i < nParts.value)
				{
					ofst = FileGDBUtil.readVarUInt(this.rowData, ofst, v);
					ptOfst += (int)v.value;
					ptOfstList[i] = ptOfst;
					i++;
				}
				points = new Coord2DDbl[(int)nPoints.value];
				SharedLong iv = new SharedLong();
				long dx = 0;
				long dy = 0;
				long dz = 0;
				long dm = 0;
				j = 0;
				while (j < nPoints.value)
				{
					ofst = FileGDBUtil.readVarInt(this.rowData, ofst, iv);
					dx += iv.value;
					x = (double)dx / this.tableInfo.getXyScale() + this.tableInfo.getXOrigin();
					ofst = FileGDBUtil.readVarInt(this.rowData, ofst, iv);
					dy += iv.value;
					y = (double)dy / this.tableInfo.getXyScale() + this.tableInfo.getYOrigin();
					points[j] = new Coord2DDbl(x, y);
					j++;
				}
				if ((this.tableInfo.getGeometryFlags() & 0x80) != 0)
				{
					nnArr = new double[(int)nPoints.value];
					zArr = nnArr;
					j = 0;
					while (j < nPoints.value)
					{
						ofst = FileGDBUtil.readVarInt(this.rowData, ofst, iv);
						dz += iv.value;
						z = (double)dz / this.tableInfo.getZScale() + this.tableInfo.getZOrigin();
						nnArr[j] = z;
						j++;
					}
				}
				if ((this.tableInfo.getGeometryFlags() & 0x40) != 0)
				{
					nnArr = new double[(int)nPoints.value];
					mArr = nnArr;
					j = 0;
					while (j < nPoints.value)
					{
						ofst = FileGDBUtil.readVarInt(this.rowData, ofst, iv);
						dm += iv.value;
						m = (double)dm / this.tableInfo.getMScale() + this.tableInfo.getMOrigin();
						nnArr[j] = m;
						j++;
					}
				}
				Vector2D vec;
				ESRICurve curve = new ESRICurve(srid, ptOfstList, points, zArr, mArr);
				if (nCurves.value > 0)
				{
					int type;
					SharedLong uv = new SharedLong();
					i = 0;
					while (i < nCurves.value)
					{
						ofst = FileGDBUtil.readVarUInt(this.rowData, ofst, uv);
						type = this.rowData[ofst];
						if (type == 1) //esriSegmentArc
						{
							curve.addArc((int)uv.value, new Coord2DDbl(ByteTool.readDouble(this.rowData, ofst + 1), ByteTool.readDouble(this.rowData, ofst + 9)), ByteTool.readInt32(this.rowData, ofst + 17));
							ofst += 21;
						}
						else if (type == 4) //esriSegmentBezier3Curve
						{
							curve.addBezier3Curve((int)uv.value, new Coord2DDbl(ByteTool.readDouble(this.rowData, ofst + 1), ByteTool.readDouble(this.rowData, ofst + 9)), new Coord2DDbl(ByteTool.readDouble(this.rowData, ofst + 17), ByteTool.readDouble(this.rowData, ofst + 25)));
							ofst += 33;
						}
						else if (type == 5) //esriSegmentEllipticArc
						{
							curve.addEllipticArc((int)uv.value, new Coord2DDbl(ByteTool.readDouble(this.rowData, ofst + 1), ByteTool.readDouble(this.rowData, ofst + 9)), ByteTool.readDouble(this.rowData, ofst + 17), ByteTool.readDouble(this.rowData, ofst + 25), ByteTool.readDouble(this.rowData, ofst + 33), ByteTool.readInt32(this.rowData, ofst + 41));
							ofst += 45;
						}
						else
						{
							System.out.println("FGDB: Unsupported curve type: "+type);
							break;
						}
						i++;
					}
				}
				vec = curve.createPolyline();
				return vec;				
			}
		case 51: //SHPT_GENERALPOLYGON
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
				srid = 0;
				int i;
				if ((csys = this.tableInfo.getCsys()) != null)
				{
					srid = csys.getSRID();
				}
				int[] parts;
				Coord2DDbl[] points;
				double[] zArr = null;
				double[] mArr = null;
				double[] nnArr;
				parts = new int[(int)nParts.value];
				points = new Coord2DDbl[(int)nPoints.value];
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
				long dx = 0;
				long dy = 0;
				long dz = 0;
				long dm = 0;
				SharedLong iv = new SharedLong();
				j = 0;
				while (j < nPoints.value)
				{
					ofst = FileGDBUtil.readVarInt(this.rowData, ofst, iv);
					dx += iv.value;
					x = (double)dx / this.tableInfo.getXyScale() + this.tableInfo.getXOrigin();
					ofst = FileGDBUtil.readVarInt(this.rowData, ofst, iv);
					dy += iv.value;
					y = (double)dy / this.tableInfo.getXyScale() + this.tableInfo.getYOrigin();
					points[j].x = x;
					points[j].y = y;
					j++;
				}
				if ((geometryType.value & 0x80000000) != 0)
				{
					nnArr = new double[(int)nPoints.value];
					zArr = nnArr;
					j = 0;
					while (j < nPoints.value)
					{
						ofst = FileGDBUtil.readVarInt(this.rowData, ofst, iv);
						dz += iv.value;
						z = (double)dz / this.tableInfo.getZScale() + this.tableInfo.getZOrigin();
						nnArr[j] = z;
						j++;
					}
				}
				if ((geometryType.value & 0x40000000) != 0)
				{
					nnArr = new double[(int)nPoints.value];
					mArr = nnArr;
					j = 0;
					while (j < nPoints.value)
					{
						ofst = FileGDBUtil.readVarInt(this.rowData, ofst, iv);
						dm += iv.value;
						m = (double)dm / this.tableInfo.getMScale() + this.tableInfo.getMOrigin();
						nnArr[j] = m;
						j++;
					}
				}
				Vector2D vec;
				ESRICurve curve = new ESRICurve(srid, parts, points, zArr, mArr);
				if (nCurves.value > 0)
				{
					int type;
					SharedLong startIndex = new SharedLong();
					i = 0;
					while (i < nCurves.value)
					{
						ofst = FileGDBUtil.readVarUInt(this.rowData, ofst, startIndex);
						type = this.rowData[ofst];
						if (type == 1) //esriSegmentArc
						{
							curve.addArc((int)startIndex.value, new Coord2DDbl(ByteTool.readDouble(this.rowData, ofst + 1), ByteTool.readDouble(this.rowData, ofst + 9)), ByteTool.readInt32(this.rowData, ofst + 17));
							ofst += 21;
						}
						else if (type == 4) //esriSegmentBezier3Curve
						{
							curve.addBezier3Curve((int)startIndex.value, new Coord2DDbl(ByteTool.readDouble(this.rowData, ofst + 1), ByteTool.readDouble(this.rowData, ofst + 9)), new Coord2DDbl(ByteTool.readDouble(this.rowData, ofst + 17), ByteTool.readDouble(this.rowData, ofst + 25)));
							ofst += 33;
						}
						else if (type == 5) //esriSegmentEllipticArc
						{
							curve.addEllipticArc((int)startIndex.value, new Coord2DDbl(ByteTool.readDouble(this.rowData, ofst + 1), ByteTool.readDouble(this.rowData, ofst + 9)), ByteTool.readDouble(this.rowData, ofst + 17), ByteTool.readDouble(this.rowData, ofst + 25), ByteTool.readDouble(this.rowData, ofst + 33), ByteTool.readInt32(this.rowData, ofst + 41));
							ofst += 45;
						}
						else
						{
							System.out.println("FGDB: Unsupported curve type: "+type);
							break;
						}
						i++;
					}
				}
				vec = curve.createPolygon();
				return vec;
			}
		}
		return null;
	}

	@Nullable
	public Geometry getGeometry(int colIndex)
	{
		Vector2D vec = this.getVector(colIndex);
		if (vec == null) return null;
		return GeometryUtil.fromVector2D(vec);
	}

	@Nullable
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

	@Nullable
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

	@Nonnull
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
				return ColumnType.VarUTF8Char;
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
				return ColumnType.VarUTF8Char;
			default:
				return ColumnType.Unknown;
			}
		}
		return ColumnType.Unknown;
	}
	
	@Nullable
	public ColumnDef getColumnDef(int colIndex)
	{
		int fieldIndex = getFieldIndex(colIndex);
		FileGDBFieldInfo field = this.getField(fieldIndex);
		if (field == null)
		{
			return null;
		}
		CoordinateSystem csys;
		ColumnDef colDef = new ColumnDef(field.getName());
		colDef.setColSize(field.getFieldSize());
		colDef.setColType(this.getColumnType(colIndex));
		if (colDef.getColType() == ColumnType.DateTime)
		{
			colDef.setColSize(0);
		}
		else if (colDef.getColType() == ColumnType.Binary)
		{
			if (field.getFieldSize() == 0)
			{
				colDef.setColSize(1048576);			
			}		
		}
		colDef.setNotNull((field.getFlags() & 1) == 0);
		colDef.setPk(field.getFieldType() == 6);
		if (field.getFieldType() == 6)
		{
			long startIndex = 1;
			if (this.indexBuff != null && this.indexBuff.length > 0)
			{
				startIndex = (long)this.indexCnt + 1;
			}
			colDef.setAutoInc(AutoIncType.Default, startIndex, 1);
		}
		else
		{
			colDef.setAutoIncNone();
		}
		if (colDef.getColType() == ColumnType.Vector)
		{
			if ((csys = this.tableInfo.getCsys()) != null)
			{
				colDef.setColDP(csys.getSRID());
			}
			switch (this.tableInfo.getGeometryType())
			{
			default:
				System.out.println("FileGDBReader: geometryType not supported: "+this.tableInfo.getGeometryType());
			case 0:
			case 7:
				if ((this.tableInfo.getGeometryFlags() & 0xC0) == 0x80)
					colDef.setColSize(GeometryType.AnyZ.ordinal());
				else if ((this.tableInfo.getGeometryFlags() & 0xC0) == 0xC0)
					colDef.setColSize(GeometryType.AnyZM.ordinal());
				else if ((this.tableInfo.getGeometryFlags() & 0xC0) == 0x40)
					colDef.setColSize(GeometryType.AnyM.ordinal());
				else
					colDef.setColSize(GeometryType.Any.ordinal());
				break;
			case 1:
				if ((this.tableInfo.getGeometryFlags() & 0xC0) == 0x80)
					colDef.setColSize(GeometryType.PointZ.ordinal());
				else if ((this.tableInfo.getGeometryFlags() & 0xC0) == 0xC0)
					colDef.setColSize(GeometryType.PointZM.ordinal());
				else if ((this.tableInfo.getGeometryFlags() & 0xC0) == 0x40)
					colDef.setColSize(GeometryType.PointM.ordinal());
				else
					colDef.setColSize(GeometryType.Point.ordinal());
				break;
			case 2:
				if ((this.tableInfo.getGeometryFlags() & 0xC0) == 0x80)
					colDef.setColSize(GeometryType.MultipointZ.ordinal());
				else if ((this.tableInfo.getGeometryFlags() & 0xC0) == 0xC0)
					colDef.setColSize(GeometryType.MultipointZM.ordinal());
				else if ((this.tableInfo.getGeometryFlags() & 0xC0) == 0x40)
					colDef.setColSize(GeometryType.MultipointM.ordinal());
				else
					colDef.setColSize(GeometryType.Multipoint.ordinal());
				break;
			case 3:
				if ((this.tableInfo.getGeometryFlags() & 0xC0) == 0x80)
					colDef.setColSize(GeometryType.PolylineZ.ordinal());
				else if ((this.tableInfo.getGeometryFlags() & 0xC0) == 0xC0)
					colDef.setColSize(GeometryType.PolylineZM.ordinal());
				else if ((this.tableInfo.getGeometryFlags() & 0xC0) == 0x40)
					colDef.setColSize(GeometryType.PolylineM.ordinal());
				else
					colDef.setColSize(GeometryType.Polyline.ordinal());
				break;
			case 4:
				if ((this.tableInfo.getGeometryFlags() & 0xC0) == 0x80)
					colDef.setColSize(GeometryType.PolygonZ.ordinal());
				else if ((this.tableInfo.getGeometryFlags() & 0xC0) == 0xC0)
					colDef.setColSize(GeometryType.PolygonZM.ordinal());
				else if ((this.tableInfo.getGeometryFlags() & 0xC0) == 0x40)
					colDef.setColSize(GeometryType.PolygonM.ordinal());
				else
					colDef.setColSize(GeometryType.Polygon.ordinal());
				break;
			case 5:
				if ((this.tableInfo.getGeometryFlags() & 0xC0) == 0x80)
					colDef.setColSize(GeometryType.RectangleZ.ordinal());
				else if ((this.tableInfo.getGeometryFlags() & 0xC0) == 0xC0)
					colDef.setColSize(GeometryType.RectangleZM.ordinal());
				else if ((this.tableInfo.getGeometryFlags() & 0xC0) == 0x40)
					colDef.setColSize(GeometryType.RectangleM.ordinal());
				else
					colDef.setColSize(GeometryType.Rectangle.ordinal());
				break;
			case 6:
				if ((this.tableInfo.getGeometryFlags() & 0xC0) == 0x80)
					colDef.setColSize(GeometryType.PathZ.ordinal());
				else if ((this.tableInfo.getGeometryFlags() & 0xC0) == 0xC0)
					colDef.setColSize(GeometryType.PathZM.ordinal());
				else if ((this.tableInfo.getGeometryFlags() & 0xC0) == 0x40)
					colDef.setColSize(GeometryType.PathM.ordinal());
				else
					colDef.setColSize(GeometryType.Path.ordinal());
				break;
			}
		}	
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

	public long getRowFileOfst()
	{
		return this.rowOfst;
	}

	public void setIndex(@Nonnull StreamData fd, int indexCnt)
	{
		if (this.indexBuff == null || this.indexBuff.length == 0)
		{
			this.indexCnt = indexCnt;
			int len = this.indexCnt * 5;
			this.indexBuff = new byte[len];
			if (fd.getRealData(16, len, this.indexBuff, 0) != len)
			{
				this.indexBuff = null;
			}
		}
	}

	@Nullable
	public Object getObjectByName(@Nonnull String name)
	{
		List<FileGDBFieldInfo> fields = this.tableInfo.getFields();
		FileGDBFieldInfo field;
		int i = 0;
		int j = fields.size();
		while (i < j)
		{
			field = fields.get(i);
			if (field.getName().equals(name))
			{
				return getObject(i);
			}
			i++;
		}
		return null;
	}
}
