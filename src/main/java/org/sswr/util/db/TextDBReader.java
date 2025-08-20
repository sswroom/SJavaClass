package org.sswr.util.db;

import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;

import org.locationtech.jts.geom.Geometry;
import org.sswr.util.data.DateTimeUtil;
import org.sswr.util.data.StringUtil;
import org.sswr.util.db.TextDB.DBData;
import org.sswr.util.math.geometry.Vector2D;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class TextDBReader extends DBReader
{
	private @Nonnull DBData data;
	private int index;
	private String[] row;

	public TextDBReader(@Nonnull DBData data)
	{
		this.row = null;
		this.index = 0;
		this.data = data;
	}

	public void close()
	{
	}

	public boolean readNext()
	{
		if (this.index >= data.valList.size())
			return false;
		this.row = data.valList.get(this.index);
		if (this.row != null)
		{
			this.index++;
			return true;
		}
		return false;
	}

	public int colCount()
	{
		return this.data.colList.size();
	}

	public int getRowChanged()
	{
		return -1;
	}

	public int getInt32(int colIndex)
	{
		String[] row;
		String s;
		if ((row = this.row) == null)
			return 0;
		if (colIndex >= this.data.colList.size() || colIndex < 0)
			return 0;
		if ((s = row[colIndex]) == null)
			return 0;
		return StringUtil.toIntegerS(s, 0);
	}

	public long getInt64(int colIndex)
	{
		String[] row;
		String s;
		if ((row = this.row) == null)
			return 0;
		if (colIndex >= this.data.colList.size() || colIndex < 0)
			return 0;
		if ((s = row[colIndex]) == null)
			return 0;
		return StringUtil.toLongS(s, 0);
	}

	public @Nullable String getString(int colIndex)
	{
		String[] row;
		String s;
		if ((row = this.row) == null)
			return null;
		if (colIndex >= this.data.colList.size() || colIndex < 0)
			return null;
		if ((s = row[colIndex]) == null)
			return null;
		return s;
	}

	public @Nullable ZonedDateTime getDate(int colIndex)
	{
		String[] row;
		String s;
		if ((row = this.row) == null)
			return null;
		if (colIndex >= this.data.colList.size() || colIndex < 0)
			return null;
		if ((s = row[colIndex]) == null)
			return null;
		return DateTimeUtil.parse(s);
	}

	public double getDblOrNAN(int colIndex)
	{
		String[] row;
		String s;
		if ((row = this.row) == null)
			return Double.NaN;
		if (colIndex >= this.data.colList.size() || colIndex < 0)
			return Double.NaN;
		if ((s = row[colIndex]) == null)
			return Double.NaN;
		return StringUtil.toDoubleS(s, Double.NaN);
	}

	public boolean getBool(int colIndex)
	{
		String[] row;
		String s;
		if ((row = this.row) == null)
			return false;
		if (colIndex >= this.data.colList.size() || colIndex < 0)
			return false;
		if ((s = row[colIndex]) == null)
			return false;
		if (s.equalsIgnoreCase("TRUE"))
			return true;
		else if (s.equalsIgnoreCase("FALSE"))
			return false;
		return StringUtil.toIntegerS(s, 0) != 0;
	}

	public @Nullable byte[] getBinary(int colIndex)
	{
		String[] row;
		String s;
		if ((row = this.row) == null)
			return null;
		if (colIndex >= this.data.colList.size() || colIndex < 0)
			return null;
		if ((s = row[colIndex]) == null)
			return null;
		return s.getBytes(StandardCharsets.UTF_8);
	}

	public @Nullable Vector2D getVector(int colIndex)
	{
		return null;
	}
	
	public @Nullable Geometry getGeometry(int colIndex)
	{
		return null;
	}

	public @Nullable Object getObject(int colIndex)
	{
		return getString(colIndex);
	}
	
	public boolean isNull(int colIndex)
	{
		String[] row;
		if ((row = this.row) == null)
			return true;
		if (colIndex >= this.data.colList.size() || colIndex < 0)
			return true;
		return (row[colIndex]) == null;
	}

	public @Nullable String getName(int colIndex)
	{
		if (colIndex < 0 || colIndex >= this.data.colList.size())
			return null;
		return this.data.colList.get(colIndex);
	}

	public @Nonnull ColumnType getColumnType(int colIndex)
	{
		if (colIndex >= this.data.colList.size() || colIndex < 0)
			return ColumnType.Unknown;
		return ColumnType.VarUTF8Char;
	}

	public @Nullable ColumnDef getColumnDef(int colIndex)
	{
		if (colIndex >= this.data.colList.size() || colIndex < 0)
		{
			return new ColumnDef("");
		}
		String colName;
		if ((colName = this.data.colList.get(colIndex)) == null)
		{
			return new ColumnDef("");
		}
		ColumnDef colDef = new ColumnDef(colName);
		colDef.setColSize(256);
		colDef.setColType(ColumnType.VarUTF8Char);
		colDef.setAttr(null);
		colDef.setColDP(0);
		colDef.setDefVal(null);
		colDef.setAutoIncNone();
		colDef.setNotNull(false);
		colDef.setPk(false);
		return colDef;
	}
}
