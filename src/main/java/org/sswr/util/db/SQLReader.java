package org.sswr.util.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZonedDateTime;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.sswr.util.data.DateTimeUtil;
import org.sswr.util.data.GeometryUtil;
import org.sswr.util.data.MSGeography;
import org.sswr.util.data.StringUtil;
import org.sswr.util.db.DBUtil.DBType;
import org.sswr.util.math.geometry.Vector2D;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class SQLReader extends DBReader
{
	private DBType dbType;
	private ResultSet rs;

	public SQLReader(@Nonnull DBType dbType, @Nonnull ResultSet rs)
	{
		this.dbType = dbType;
		this.rs = rs;
	}

	@Override
	public void close()
	{
		if (this.rs != null)
		{
			try
			{
				this.rs.close();
			}
			catch (SQLException ex)
			{

			}
			this.rs = null;
		}
	}

	@Override
	public boolean readNext()
	{
		if (this.rs == null) return false;
		try
		{
			return this.rs.next();
		}
		catch (SQLException ex)
		{
			return false;
		}
	}

	@Override
	public int colCount()
	{
		if (this.rs == null) return 0;
		return this.colCount();
	}

	@Override
	public int getRowChanged()
	{
		if (this.rs == null) return -1;
		return 0;
	}

	@Override
	public int getInt32(int colIndex)
	{
		if (this.rs == null) return 0;
		try
		{
			return this.rs.getInt(colIndex + 1);
		}
		catch (SQLException ex)
		{
			return 0;
		}
	}

	@Override
	public long getInt64(int colIndex)
	{
		if (this.rs == null) return 0;
		try
		{
			return this.rs.getLong(colIndex + 1);
		}
		catch (SQLException ex)
		{
			return 0;
		}
	}

	@Override
	@Nullable
	public String getString(int colIndex)
	{
		if (this.rs == null) return null;
		try
		{
			return this.rs.getString(colIndex + 1);
		}
		catch (SQLException ex)
		{
			return null;
		}
	}

	@Override
	@Nullable
	public ZonedDateTime getDate(int colIndex)
	{
		if (this.rs == null) return null;
		try
		{
			return DateTimeUtil.newZonedDateTime(this.rs.getTimestamp(colIndex + 1));
		}
		catch (SQLException ex)
		{
			return null;
		}
	}

	@Override
	public double getDbl(int colIndex)
	{
		if (this.rs == null) return 0;
		try
		{
			return this.rs.getDouble(colIndex + 1);
		}
		catch (SQLException ex)
		{
			return 0;
		}
	}

	@Override
	public boolean getBool(int colIndex)
	{
		return this.getInt32(colIndex + 1) != 0;
	}

	@Override
	@Nullable
	public byte[] getBinary(int colIndex)
	{
		if (this.rs == null) return null;
		try
		{
			return this.rs.getBytes(colIndex + 1);
		}
		catch (SQLException ex)
		{
			return null;
		}
	}

	@Override
	@Nullable
	public Vector2D getVector(int colIndex)
	{
		if (this.rs == null) return null;
		try
		{
			byte bytes[] = rs.getBytes(colIndex + 1);
			if (bytes == null)
				return null;
			if (this.dbType == DBType.MSSQL)
			{
				return MSGeography.parseBinary(bytes);
			}
			else if (this.dbType == DBType.PostgreSQL || this.dbType == DBType.PostgreSQLESRI)
			{
				if (dbType == DBType.PostgreSQL)
				{
					bytes = StringUtil.hex2Bytes(new String(bytes));
				}
				org.sswr.util.math.WKBReader reader = new org.sswr.util.math.WKBReader(0);
				return reader.parseWKB(bytes, 0, bytes.length, null);
			}
			else
			{
				System.out.println("SQLReader DBType: "+this.dbType);
				org.sswr.util.math.WKBReader reader = new org.sswr.util.math.WKBReader(0);
				return reader.parseWKB(bytes, 0, bytes.length, null);
			}
		}
		catch (SQLException ex)
		{
			ex.printStackTrace();
			return null;
		}
	}

	@Override
	@Nullable
	public Geometry getGeometry(int colIndex)
	{
		if (this.rs == null) return null;
		try
		{
			byte bytes[] = rs.getBytes(colIndex + 1);
			if (bytes == null)
			{
				return null;
			}
			else if (dbType == DBType.MSSQL)
			{
				Vector2D vec = MSGeography.parseBinary(bytes);
				if (vec == null)
					return null;
				return GeometryUtil.fromVector2D(vec);
			}
			else if (dbType == DBType.PostgreSQL || dbType == DBType.PostgreSQLESRI)
			{
				if (dbType == DBType.PostgreSQL)
				{
					bytes = StringUtil.hex2Bytes(new String(bytes));
				}
				WKBReader reader = new WKBReader();
				try
				{
					return reader.read(bytes);
				}
				catch (ParseException ex)
				{
					ex.printStackTrace();
					return null;
				}
			}
			else
			{
				WKBReader reader = new WKBReader();
				try
				{
					return reader.read(bytes);
				}
				catch (ParseException ex)
				{
					ex.printStackTrace();
					return null;
				}
			}
		}
		catch (SQLException ex)
		{
			ex.printStackTrace();
			return null;
		}
	}

	@Override
	@Nullable
	public Object getObject(int colIndex)
	{
		if (this.rs == null) return null;
		try
		{
			return this.rs.getObject(colIndex);
		}
		catch (SQLException ex)
		{
			return null;
		}
	}

	@Override
	public boolean isNull(int colIndex)
	{
		if (this.rs == null) return true;
		try
		{
			return this.rs.getObject(colIndex + 1) == null;
		}
		catch (SQLException ex)
		{
			return true;
		}
	}

	@Override
	public String getName(int colIndex)
	{
		if (this.rs == null) return null;
		try
		{
			return this.rs.getMetaData().getColumnName(colIndex);
		}
		catch (SQLException ex)
		{
			return null;
		}
	}

	@Override
	@Nonnull
	public ColumnType getColumnType(int colIndex)
	{
		if (this.rs == null) return ColumnType.Unknown;
		try
		{
			return DBUtil.parseColType(this.dbType, this.rs.getMetaData().getColumnTypeName(colIndex), null);
		}
		catch (SQLException ex)
		{
			return ColumnType.Unknown;
		}
	}

	@Override
	public ColumnDef getColumnDef(int colIndex)
	{
		if (this.rs == null) return null;
		try
		{
			if (colIndex < 0 || colIndex >= this.rs.getMetaData().getColumnCount())
			{
				return null;
			}
			ColumnDef col = new ColumnDef(this.rs.getMetaData().getColumnName(colIndex));
			col.setColType(this.getColumnType(colIndex));
			col.setColSize(this.rs.getMetaData().getPrecision(colIndex));
			col.setColDP(this.rs.getMetaData().getScale(colIndex));
			col.setNotNull(this.rs.getMetaData().isNullable(colIndex) == 0);
			col.setAutoInc(this.rs.getMetaData().isAutoIncrement(colIndex));
			col.setPk(false);
			return col;
		}
		catch (SQLException ex)
		{
			return null;
		}
	}
	
}
