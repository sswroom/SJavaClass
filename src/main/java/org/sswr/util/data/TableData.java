package org.sswr.util.data;

import java.util.List;
import java.util.Optional;

import javax.xml.crypto.Data;

import org.sswr.util.db.ColumnDef;
import org.sswr.util.db.DBReader;
import org.sswr.util.db.QueryConditions;
import org.sswr.util.db.ReadingDB;
import org.w3c.dom.Text;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class TableData {
	public boolean needRelease;
	public @Nonnull ReadingDB db;
	public @Nullable String schemaName;
	public @Nonnull String tableName;
	public @Nullable QueryConditions cond;

	public TableData(@Nonnull ReadingDB db, boolean needRelease, @Nullable String schemaName, @Nonnull String tableName)
	{
		this.db = db;
		this.needRelease = needRelease;
		this.schemaName = schemaName;
		this.tableName = tableName;
		this.cond = null;
	}

	public void dispose()
	{
		if (this.needRelease)
			this.db.dispose();;
	}

	public @Nullable DBReader getTableData()
	{
		return this.db.queryTableData(this.schemaName, this.tableName, null, 0, 0, null, this.cond);
	}

	private @Nullable DBReader getTableData(@Nonnull QueryConditions cond)
	{
		QueryConditions tabCond;
		if ((tabCond = this.cond) != null)
		{
			cond.and((Conditions.BooleanObject)tabCond.getRootCond().Clone());
		}
		return this.db.queryTableData(this.schemaName, this.tableName, null, 0, 0, null, cond);
	}

	public boolean getColumnDataStr(@Nonnull String columnName, @Nonnull List<String> str)
	{
		String s;
		DBReader r;
		if ((r = this.getTableData()) == null)
			return false;
		int col = -1;
		int i = 0;
		int colCnt = r.colCount();
		while (i < colCnt)
		{
			if ((s = r.getName(i)) != null)
			{
				if (columnName.equals(s))
				{
					col = i;
					break;
				}
			}
			i++;
		}
		if (col == -1)
		{
			this.closeReader(r);
			return false;
		}
		while (r.readNext())
		{
			s = r.getString(col);
			if (s == null)
				s = "";
			str.add(s);
		}
		this.closeReader(r);
		return true;
	}

	public @Nullable DataSet getDataSet(@Nonnull String columnName)
	{
		DBReader r;
		if ((r = this.getTableData()) == null)
		{
			return null;
		}
		int keyCol = -1;
		int valueCol = -1;
		ColumnDef colDef;
		int colCnt = r.colCount();
		int i = 0;
		while (i < colCnt)
		{
			if ((colDef = r.getColumnDef(i)) != null)
			{
				if (colDef.isPk())
				{
					if (keyCol == -1)
					{
						keyCol = i;
					}
					else
					{
						this.closeReader(r);
						return null;
					}
				}
				if (colDef.getColName().equals(columnName))
				{
					valueCol = i;
				}
			}
			i++;
		}
		if (keyCol == -1 || valueCol == -1)
		{
			this.closeReader(r);
			return null;
		}
		DataSet ds = new DataSet();
		while (r.readNext())
		{
			if (r.isNull(keyCol))
			{
				System.out.println("TableData: Error in getting key column value");
			}
			else if (r.isNull(valueCol))
			{
				System.out.println("TableData: Error in getting value column value");
			}
			else
			{
				ds.addItem(r.getObject(keyCol), r.getObject(valueCol));
			}
		}
		this.closeReader(r);
		return ds;
	}

	public @Nullable DataSet getKeyDataSet()
	{
		DBReader r;
		if ((r = this.getTableData()) == null)
		{
			return null;
		}
		int keyCol = -1;
		ColumnDef colDef;
		int colCnt = r.colCount();
		int i = 0;
		while (i < colCnt)
		{
			if ((colDef = r.getColumnDef(i)) != null)
			{
				if (colDef.isPk())
				{
					if (keyCol == -1)
					{
						keyCol = i;
					}
					else
					{
						this.closeReader(r);
						return null;
					}
				}
			}
			i++;
		}
		if (keyCol == -1)
		{
			this.closeReader(r);
			return null;
		}
		Object keyItem;
		DataSet ds = new DataSet();
		while (r.readNext())
		{
			if (r.isNull(keyCol))
			{
				System.out.println("TableData: Error in getting key column value\r\n");
			}
			else
			{
				keyItem = r.getObject(keyCol);
				ds.addItem(keyItem, keyItem);
			}
		}
		this.closeReader(r);
		return ds;
	}

	public void closeReader(@Nonnull DBReader r)
	{
		this.db.closeReader(r);
	}

	public void setCondition(@Nullable QueryConditions cond)
	{
		this.cond = cond;
	}

	public @Nonnull TableData clone()
	{
		return new TableData(this.db, false, this.schemaName, this.tableName);
	}

	public @Nonnull TableData createSubTable(@Nonnull QueryConditions cond)
	{
		TableData data = this.clone();
		data.setCondition(cond);
		return data;
	}

	public int getRowCount()
	{
		int cnt = 0;
		DBReader r;
		if ((r = this.getTableData()) != null)
		{
			while (r.readNext())
			{
				cnt++;
			}
			this.db.closeReader(r);
		}
		return cnt;
	}

	public int getRowCount(@Nonnull QueryConditions cond)
	{
		int cnt = 0;
		DBReader r;
		if ((r = this.getTableData(cond)) != null)
		{
			while (r.readNext())
			{
				cnt++;
			}
			this.db.closeReader(r);
		}
		return cnt;
	}

	public @Nullable Object getFirstData(@Nonnull String columnName, @Nullable QueryConditions cond)
	{
		DBReader r;
		Object ret = null;
		if (cond != null)
		{
			if ((r = this.getTableData(cond)) == null)
			{
				return null;
			}
		}
		else
		{
			if ((r = this.getTableData()) == null)
			{
				return null;
			}
		}
		if (r.readNext())
		{
			String s;
			int i = r.colCount();
			while (i-- > 0)
			{
				if ((s = r.getName(i)) != null)
				{
					if (s.equals(columnName))
					{
						if (!r.isNull(i))
						{
							ret = r.getObject(i);
						}
						break;
					}
				}
			}
		}
		this.db.closeReader(r);
		return ret;
	}
}
