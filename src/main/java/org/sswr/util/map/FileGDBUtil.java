package org.sswr.util.map;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;

import org.sswr.util.data.ByteTool;
import org.sswr.util.data.SharedInt;
import org.sswr.util.data.SharedLong;
import org.sswr.util.math.CoordinateSystemManager;

public class FileGDBUtil
{
	public static final byte HAS_M_FLAG = 4;
	public static final byte HAS_Z_FLAG = 2;

	public static FileGDBTableInfo parseFieldDesc(byte[] fieldDesc)
	{
		FileGDBFieldInfo field;
		FileGDBTableInfo table = new FileGDBTableInfo();
		table.setFields(new ArrayList<FileGDBFieldInfo>());
		int descSize = ByteTool.readInt32(fieldDesc, 0);
		int fieldDescOfst = 4;
		table.setGeometryType(fieldDesc[fieldDescOfst + 4]);
		table.setTableFlags(fieldDesc[fieldDescOfst + 5]);
		table.setGeometryFlags(fieldDesc[fieldDescOfst + 7]);
		int fieldCnt = ByteTool.readUInt16(fieldDesc, fieldDescOfst + 8);
		int ofst = 10 + fieldDescOfst;
		boolean valid = true;
		while (fieldCnt-- > 0)
		{
			if (fieldDesc[ofst] == 0 || (ofst + 1 + fieldDesc[ofst] * 2) > descSize + fieldDescOfst)
			{
				valid = false;
				break;
			}
			field = new FileGDBFieldInfo();
			field.setName(new String(fieldDesc, ofst + 1, (fieldDesc[ofst] & 0xff) * 2, StandardCharsets.UTF_16LE));
			ofst += 1 + (fieldDesc[ofst] & 0xff) * 2;
			if (fieldDesc[ofst] == 0)
			{
				ofst += 1;
			}
			else if ((ofst + 1 + fieldDesc[ofst] * 2) > descSize + fieldDescOfst)
			{
				valid = false;
				break;
			}
			else
			{
				field.setAlias(new String(fieldDesc, ofst + 1, (fieldDesc[ofst] & 0xff) * 2, StandardCharsets.UTF_16LE));
				ofst += 1 + (fieldDesc[ofst] & 0xff) * 2;
			}
			field.setFieldType(fieldDesc[ofst]);
			if (field.getFieldType() == 4)
			{
				if (ofst + 6 > descSize + fieldDescOfst)
				{
					valid = false;
					break;
				}
				field.setFieldSize(ByteTool.readInt32(fieldDesc, ofst + 1));
				field.setFlags(fieldDesc[ofst + 5]);
				ofst += 6;
			}
			else
			{
				if (ofst + 3 > descSize + fieldDescOfst)
				{
					valid = false;
					break;
				}
				field.setFieldSize(fieldDesc[ofst + 1]);
				field.setFlags(fieldDesc[ofst + 2]);
				ofst += 3;
			}
			if ((field.getFlags() & 1) != 0)
			{
				table.setNullableCnt(table.getNullableCnt() + 1);
			}
			if (field.getFieldType() == 7)
			{
				int srsLen = ByteTool.readUInt16(fieldDesc, ofst);
				byte[] csysBuff = new String(fieldDesc, ofst + 2, srsLen, StandardCharsets.UTF_16LE).getBytes(StandardCharsets.UTF_8);
				SharedInt csysLen = new SharedInt();
				csysLen.value = csysBuff.length;
				table.setCsys(CoordinateSystemManager.parsePRJBuff("FileGDB", csysBuff, 0, csysLen));
				ofst += 2 + srsLen;
				byte flags = fieldDesc[ofst];
				ofst += 1;
				table.setXOrigin(ByteTool.readDouble(fieldDesc, ofst));
				table.setYOrigin(ByteTool.readDouble(fieldDesc, ofst + 8));
				table.setXyScale(ByteTool.readDouble(fieldDesc, ofst + 16));
				ofst += 24;
				if ((flags & HAS_M_FLAG) != 0)
				{
					table.setMOrigin(ByteTool.readDouble(fieldDesc, ofst));
					table.setMScale(ByteTool.readDouble(fieldDesc, ofst + 8));
					ofst += 16;
				}
				if ((flags & HAS_Z_FLAG) != 0)
				{
					table.setZOrigin(ByteTool.readDouble(fieldDesc, ofst));
					table.setZScale(ByteTool.readDouble(fieldDesc, ofst + 8));
					ofst += 16;
				}
				table.setXyTolerance(ByteTool.readDouble(fieldDesc, ofst));
				ofst += 8;
				if ((flags & HAS_M_FLAG) != 0)
				{
					table.setMTolerance(ByteTool.readDouble(fieldDesc, ofst));
					ofst += 8;
				}
				if ((flags & HAS_Z_FLAG) != 0)
				{
					table.setZTolerance(ByteTool.readDouble(fieldDesc, ofst));
					ofst += 8;
				}
				table.setXMin(ByteTool.readDouble(fieldDesc, ofst));
				table.setYMin(ByteTool.readDouble(fieldDesc, ofst + 8));
				table.setXMax(ByteTool.readDouble(fieldDesc, ofst + 16));
				table.setYMax(ByteTool.readDouble(fieldDesc, ofst + 24));
				ofst += 32;
				if ((table.getGeometryFlags() & 0x80) != 0)
				{
					table.setZMin(ByteTool.readDouble(fieldDesc, ofst));
					table.setZMax(ByteTool.readDouble(fieldDesc, ofst + 8));
					ofst += 16;
				}
				if ((table.getGeometryFlags() & 0x40) != 0)
				{
					table.setMMin(ByteTool.readDouble(fieldDesc, ofst));
					table.setMMax(ByteTool.readDouble(fieldDesc, ofst + 8));
					ofst += 16;
				}
				int gridCnt = ByteTool.readInt32(fieldDesc, ofst + 1);
				int i = 0;
				double []spatialGrid = new double[gridCnt];
				ofst += 5;
				while (i < gridCnt)
				{
					spatialGrid[i] = ByteTool.readDouble(fieldDesc, ofst);
					ofst += 8;
					i++;
				}
				table.setSpatialGrid(spatialGrid);
			}
			else if (field.getFieldType() == 9)
			{
				/////////////////////////////////////
				valid = false;
				break;
			}
	
			if ((field.getFlags() & 4) != 0 && (field.getFieldType() < 6)) //has default value
			{
				if (ofst >= descSize + fieldDescOfst || (ofst + 1 + fieldDesc[ofst] > descSize + fieldDescOfst))
				{
					valid = false;
					break;
				}
				field.setDefSize(fieldDesc[ofst]);
				field.setDefValue(Arrays.copyOfRange(fieldDesc, ofst + 1, ofst + 1 + (field.getDefSize() & 0xff)));
				ofst += 1 + (field.getDefSize() & 0xff);
			}
			table.getFields().add(field);
			if (ofst < descSize + fieldDescOfst && fieldDesc[ofst] == 0)
			{
				ofst++;
			}
		}
		if (!valid || ofst != descSize + fieldDescOfst)
		{
			return null;
		}
		return table;
	}

	public static int readVarUInt(byte []buff, int ofst, SharedLong val)
	{
		long v = 0;
		int i = 0;
		long currV;
		while (true)
		{
			currV = buff[ofst];
			ofst++;
			v = v | ((currV & 0x7F) << i);
			if ((currV & 0x80) == 0)
			{
				break;
			}
			i += 7;
		}
		val.value = v;
		return ofst;
	}

	public static int readVarInt(byte []buff, int ofst, SharedLong val)
	{
		boolean sign = (buff[0] & 0x40) != 0;
		long v = 0;
		int i = 0;
		long currV;
		currV = buff[ofst];
		ofst++;
		i = 6;
		v = currV & 0x3f;
		while ((currV & 0x80) != 0)
		{
			currV = buff[ofst];
			ofst++;
			v = v | ((currV & 0x7F) << i);
			i += 7;
		}
		if (sign)
		{
			val.value = -v;
		}
		else
		{
			val.value = v;
		}
		return ofst;
	}

	public static ZonedDateTime toDateTime(double v)
	{
		int days = (int)v;
		ZonedDateTime dt = ZonedDateTime.ofInstant(Instant.ofEpochMilli((days - 25569) * 86400000L + (int)((v - days) * 86400000)), ZoneOffset.UTC);
		return dt.withZoneSameLocal(ZoneId.systemDefault());
	}

	public static String geometryTypeGetName(byte t)
	{
		switch (t)
		{
		case 0:
			return "None";
		case 1:
			return "Point";
		case 2:
			return "Multipoint";
		case 3:
			return "Polyline";
		case 4:
			return "Polygon";
		case 5:
			return "Rectangle";
		case 6:
			return "Path";
		case 7:
			return "Mixed";
		case 9:
			return "Multipath";
		case 11:
			return "Ring";
		case 13:
			return "Line";
		case 14:
			return "Circular Arc";
		case 15:
			return "Bezier Curves";
		case 16:
			return "Elliptic Curves";
		case 17:
			return "Geometry Collection";
		case 18:
			return "Triangle Strip";
		case 19:
			return "Triangle Fan";
		case 20:
			return "Ray";
		case 21:
			return "Sphere";
		case 22:
			return "TIN";
		default:
			return "Unknown";
		}
	}

	public static String fieldTypeGetName(byte t)
	{
		switch (t)
		{
		case 0:
			return "Int16";
		case 1:
			return "Int32";
		case 2:
			return "Float32";
		case 3:
			return "Float64";
		case 4:
			return "String";
		case 5:
			return "Datetime";
		case 6:
			return "ObjectId";
		case 7:
			return "Geometry";
		case 8:
			return "Binary";
		case 9:
			return "Raster";
		case 10:
			return "UUID";
		case 11:
			return "UUID";
		case 12:
			return "XML";
		default:
			return "Unknown";
		}
	}
}
