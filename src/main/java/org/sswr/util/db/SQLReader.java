package org.sswr.util.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZonedDateTime;

import org.sswr.util.data.DateTimeUtil;
import org.sswr.util.data.MSGeography;
import org.sswr.util.db.DBUtil.DBType;
import org.sswr.util.math.Vector2D;

public class SQLReader extends DBReader
{
	private DBType dbType;
	private ResultSet rs;

	public SQLReader(DBType dbType, ResultSet rs)
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
	public Vector2D getVector(int colIndex)
	{
		if (this.rs == null) return null;
		try
		{
			byte bytes[] = rs.getBytes(colIndex + 1);
			if (this.dbType == DBType.MSSQL)
			{
				return MSGeography.parseBinaryAsVector2D(bytes);
			}
			else
			{
/*				WKBReader reader = new WKBReader();
				try
				{
					return readercol.setter.set(o, reader.read(bytes));
				}
				catch (ParseException ex)
				{
					sqlLogger.logException(ex);
				}*/
				return null;
			}
		}
		catch (SQLException ex)
		{
			return null;
		}
	}

	@Override
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
	public ColumnType getColumnType(int colIndex)
	{
		if (this.rs == null) return ColumnType.Unknown;
		try
		{
			String typeName = this.rs.getMetaData().getColumnTypeName(colIndex);
			switch (typeName.toUpperCase())
			{
			case "CHAR":
				return ColumnType.Char;
			case "VARCHAR":
				return ColumnType.VarChar;
			case "NCHAR":
				return ColumnType.NChar;
			case "NVARCHAR":
				return ColumnType.NVarChar;
			case "GEOMETRY":
				return ColumnType.Vector;
			default:
				return ColumnType.Unknown;
			}
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
