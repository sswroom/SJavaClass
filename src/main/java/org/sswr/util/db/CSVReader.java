package org.sswr.util.db;

import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;

import org.locationtech.jts.geom.Geometry;
import org.sswr.util.data.ByteTool;
import org.sswr.util.data.DateTimeUtil;
import org.sswr.util.data.ObjectGetter;
import org.sswr.util.data.QueryConditions;
import org.sswr.util.data.StringUtil;
import org.sswr.util.io.IOReader;
import org.sswr.util.io.IOStream;
import org.sswr.util.math.geometry.Vector2D;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

class CSVReader extends DBReader implements ObjectGetter
{
	static class CSVColumn
	{
		public int colSize;
		public String value;
		public ColumnType colType;
	}

	private @Nullable IOStream stm;
	private @Nonnull IOReader rdr;
	private int nCol;
	private int nHdr;
	private @Nonnull byte[] row;
	private @Nonnull CSVColumn[] cols;
	private @Nonnull byte[] hdr;
	private @Nonnull String[] hdrs;
	private boolean noHeader;
	private boolean nullIfEmpty;
	private int indexCol;
	private @Nullable QueryConditions condition;

	public CSVReader(@Nullable IOStream stm, @Nonnull IOReader rdr, boolean noHeader, boolean nullIfEmpty, @Nullable QueryConditions condition)
	{
		this.stm = stm;
		this.rdr = rdr;
		this.noHeader = noHeader;
		this.nullIfEmpty = nullIfEmpty;
		this.nCol = 0;
		this.indexCol = -1;
		this.row = new byte[16384];
		this.cols = new CSVColumn[128];
		this.hdrs = new String[128];
		this.condition = condition;

		int sptr;
		int currPtr;
		byte c;
		boolean colStart = true;
		boolean quote = false;
		currPtr = 0;
		if ((sptr = this.rdr.readLine(this.row, 0, this.row.length - 1)) >= 0)
		{
			while (true)
			{
				c = this.row[currPtr++];
				if (c == 0)
				{
					currPtr--;
					if (quote)
					{
						sptr = this.rdr.getLastLineBreak(this.row, currPtr);
						currPtr = sptr;
						if ((sptr = this.rdr.readLine(this.row, sptr, this.row.length - sptr - 1)) < 0)
							break;
						if (!this.rdr.isLineBreak() && sptr > this.row.length - 6)
						{
							byte[] newRow = new byte[this.row.length << 1];
							ByteTool.copyArray(newRow, 0, this.row, 0, this.row.length);
							this.row = newRow;
						}
					}
					else
					{
						break;
					}
				}
				else if (c == '"')
				{
					if (colStart && !quote)
					{
						quote = true;
						colStart = false;
					}
					else if (quote)
					{
						if (this.row[currPtr] == '"')
						{
							currPtr++;
						}
						else
						{
							quote = false;
						}
					}
					else
					{
					}
				}
				else if ((c == ',') && !quote)
				{
					colStart = true;
				}
				else
				{
					colStart = false;
				}
			}
		}

		this.hdr = new byte[currPtr + 1];
		ByteTool.copyArray(this.hdr, 0, this.row, 0, currPtr + 1);
		this.nHdr = StringUtil.csvSplit(this.hdrs, 128, new String(this.hdr, 0, currPtr, StandardCharsets.UTF_8));
		this.nCol = this.nHdr;
		int i = this.nCol;
		while (i-- > 0)
		{
			this.cols[i] = new CSVColumn();
			this.cols[i].colSize = this.hdrs[i].length();
		}
		i = 128;
		while (i-- > 0)
		{
			this.cols[i].colType = ColumnType.VarUTF8Char;
		}
	}

	@Override
	public void close() {
		this.rdr.close();
		if (this.stm != null) this.stm.close();
	}

	@Override
	public boolean readNext() {
		int sptr;
		int currPtr;
		int colStartPtr;
		byte c;
		boolean colStart;
		boolean quote = false;

		int nCol;
		if (this.noHeader)
		{
			this.noHeader = false;
			nCol = this.nHdr;
			this.nCol = nCol;
			while (nCol-- > 0)
			{
				this.cols[nCol].value = this.hdrs[nCol];
			}
			return true;
		}
		while (true)
		{
			while (true)
			{
				if ((sptr = this.rdr.readLine(this.row, 0, this.row.length - 1)) < 0)
				{
					this.nCol = 0;
					return false;
				}
				else if (sptr != 0)
				{
					break;
				}
			}

			nCol = 1;
			colStartPtr = 0;

			currPtr = 0;
			colStart = true;
			while (true)
			{
				c = this.row[currPtr];
				if (c == 0)
				{
					if (quote)
					{
						sptr = this.rdr.getLastLineBreak(this.row, currPtr);
						if ((sptr = this.rdr.readLine(this.row, sptr, this.row.length - sptr - 1)) < 0)
							break;
						if (!this.rdr.isLineBreak() && sptr > this.row.length - 6)
						{
							byte[] newRow = new byte[this.row.length << 1];
							ByteTool.copyArray(newRow, 0, this.row, 0, this.row.length);
							this.row = newRow;
						}
					}
					else
					{
						this.cols[nCol - 1].value = new String(this.row, colStartPtr, currPtr - colStartPtr, StandardCharsets.UTF_8);
						break;
					}
				}
				else if (c == '"')
				{
					if (quote)
					{
						if (this.row[currPtr + 1] == '"')
						{
							currPtr += 2;
						}
						else
						{
							quote = false;
							currPtr++;
						}
					}
					else if (colStart)
					{
						quote = true;
						colStart = false;
						currPtr++;
					}
					else
					{
						currPtr++;
					}
				}
				else if ((c == ',') && !quote)
				{
					this.cols[nCol - 1].value = new String(this.row, colStartPtr, currPtr - colStartPtr, StandardCharsets.UTF_8);
					currPtr++;
					nCol++;
					colStart = true;
					colStartPtr = currPtr;
				}
				else
				{
					colStart = false;
					currPtr++;
				}
			}
			QueryConditions nncondition;
			try
			{
				if ((nncondition = this.condition) == null || nncondition.isValid(this))
				{
					this.nCol = nCol;
					return true;
				}
			}
			catch (IllegalAccessException|InvocationTargetException ex)
			{
			}
		}
	}

	@Override
	public int colCount() {
		if (this.nHdr > this.nCol)
			return this.nHdr;
		return this.nCol;
	}

	@Override
	public int getRowChanged() {
		return -1;
	}

	@Override
	public int getInt32(int colIndex) {
		String s = this.getString(colIndex);
		if (s == null)
			return 0;
		return StringUtil.toIntegerS(s.trim(), 0);
	}

	@Override
	public long getInt64(int colIndex) {
		String s = this.getString(colIndex);
		if (s == null)
			return 0;
		return StringUtil.toLongS(s.trim(), 0);
	}

	@Override
	@Nullable
	public String getString(int colIndex) {
		if (colIndex >= nCol || colIndex < 0)
			return null;

		if (this.nullIfEmpty)
		{
			if (cols[colIndex].value.length() == 0)
			{
				return null;
			}
		}
		char[] sarr = cols[colIndex].value.toCharArray();
		int buff = 0;
		char c;
		boolean quote = false;
		c = sarr[0];
		if (c == '"')
		{
			int ptr = 0;
			while (ptr < sarr.length)
			{
				c = sarr[ptr++];
				if (c == 0)
					break;
				if (c == '"')
				{
					if (quote)
					{
						if (ptr < sarr.length && sarr[ptr] == '"')
						{
							sarr[buff++] = '"';
							ptr++;
						}
						else
						{
							quote = false;
						}
					}
					else
					{
						quote = true;
					}
				}
				else if ((c == ',') && !quote)
				{
					break;
				}
				else
				{
					sarr[buff++] = c;
				}
			}
			return new String(sarr, 0, buff);
		}
		else
		{
			return cols[colIndex].value;
		}
	}

	@Override
	@Nullable
	public ZonedDateTime getDate(int colIndex) {
		String s;
		if ((s = this.getString(colIndex)) != null)
			return DateTimeUtil.parse(s);
		return null;
	}

	@Override
	public double getDblOrNAN(int colIndex) {
		String s = this.getString(colIndex);
		if (s == null)
			return Double.NaN;
		return StringUtil.toDoubleS(s.trim(), Double.NaN);
	}

	@Override
	public boolean getBool(int colIndex) {
		String s = this.getString(colIndex);
		if (s == null)
			return false;
		if (s.toUpperCase().equals("TRUE"))
			return true;
		return StringUtil.toIntegerS(s.trim(), 0) != 0;
	}

	@Override
	@Nullable
	public byte[] getBinary(int colIndex) {
		return null;
	}

	@Override
	@Nullable
	public Vector2D getVector(int colIndex) {
		///////////////////
		return null;
	}

	@Override
	@Nullable
	public Geometry getGeometry(int colIndex) {
		///////////////////
		return null;
	}

	@Override
	@Nullable
	public Object getObject(int colIndex) {
		if (colIndex < 0 || colIndex >= this.nCol)
			return null;
		if (this.cols[colIndex].colType == ColumnType.DateTimeTZ)
		{
			return this.getDate(colIndex);
		}
		return this.getString(colIndex);
	}

	@Override
	public boolean isNull(int colIndex) {
		if (colIndex < 0 || colIndex >= this.nCol)
			return true;
		return false;
	}

	@Override
	@Nullable
	public String getName(int colIndex) {
		if (colIndex < 0 || colIndex >= this.nCol)
			return null;
		return this.hdrs[colIndex];
	}

	@Override
	@Nonnull
	public ColumnType getColumnType(int colIndex) {
		if (colIndex < 0 || colIndex >= this.nHdr)
			return ColumnType.Unknown;
		return this.cols[colIndex].colType;
	}

	@Override
	@Nullable
	public ColumnDef getColumnDef(int colIndex) {
		if (colIndex < 0 || colIndex >= nHdr)
			return null;
		ColumnDef colDef = new ColumnDef(this.hdrs[colIndex]);
		colDef.setColType(cols[colIndex].colType);
		colDef.setColSize(256);
		colDef.setColDP(0);
		colDef.setNotNull(true);
		colDef.setPk(colIndex == this.indexCol);
		colDef.setAutoInc(AutoIncType.None, 1, 1);
		colDef.setDefVal(null);
		colDef.setAttr(null);
		return colDef;
	}

	@Override
	public Object getObjectByName(@Nonnull String name) {
		int i = this.nHdr;
		while (i-- > 0)
		{
			if (this.hdrs[i].equals(name))
			{
				return this.getString(i);
			}
		}
		return null;
	}

	public void setIndexCol(int indexCol)
	{
		this.indexCol = indexCol;
	}

	public void addTimeCol(int timeCol)
	{
		if (timeCol >= 0 && timeCol < 128)
		{
			this.cols[timeCol].colType = ColumnType.DateTimeTZ;
		}
	}
}
