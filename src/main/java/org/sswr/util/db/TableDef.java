package org.sswr.util.db;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class TableDef {
	private @Nullable String databaseName;
	private @Nullable String schemaName;
	private @Nonnull String tableName;
	private @Nullable String engine;
	private @Nullable String charset;
	private @Nullable String attr;
	private @Nullable String comments;
	private @Nonnull SQLType sqlType;
	private @Nonnull List<ColumnDef> cols;

	public TableDef(@Nullable String schemaName, @Nonnull String tableName)
	{
		this.databaseName = null;
		this.schemaName = schemaName;
		this.tableName = tableName;
		this.engine = null;
		this.charset = null;
		this.attr = null;
		this.comments = null;
		this.sqlType = SQLType.Unknown;
		this.cols = new ArrayList<ColumnDef>();
	}


	public @Nullable String getDatabaseName()
	{
		return this.databaseName;
	}

	public @Nullable String getSchemaName()
	{
		return this.schemaName;
	}

	public @Nonnull String getTableName()
	{
		return this.tableName;
	}

	public @Nullable String getEngine()
	{
		return this.engine;
	}

	public @Nullable String getCharset()
	{
		return this.charset;
	}

	public @Nullable String getAttr()
	{
		return this.attr;
	}

	public @Nullable String getComments()
	{
		return this.comments;
	}

	public @Nonnull SQLType getSQLType()
	{
		return this.sqlType;
	}

	public int getColCnt()
	{
		return this.cols.size();
	}

	public @Nullable ColumnDef getCol(int index)
	{
		if (index < 0 || index >= this.cols.size())
			return null;
		return this.cols.get(index);
	}

	public @Nullable ColumnDef getSinglePKCol()
	{
		@Nullable ColumnDef retCol = null;
		ColumnDef col;
		Iterator<ColumnDef> it = this.cols.iterator();
		while (it.hasNext())
		{
			col = it.next();
			if (col.isPk())
			{
				if (retCol != null)
				{
					return null;
				}
				retCol = col;
			}
		}
		return retCol;
	}

	public int countPK()
	{
		int cnt = 0;
		Iterator<ColumnDef> it = this.cols.iterator();
		while (it.hasNext())
		{
			if (it.next().isPk())
				cnt++;
		}
		return cnt;
	}

	public Iterator<ColumnDef> colIterator()
	{
		return this.cols.iterator();
	}

	public @Nonnull TableDef addCol(@Nonnull ColumnDef col)
	{
		this.cols.add(col);
		return this;
	}

	public @Nonnull TableDef setDatabaseName(@Nullable String databaseName)
	{
		this.databaseName = databaseName;
		return this;
	}

	public @Nonnull TableDef setSchemaName(@Nullable String schemaName)
	{
		this.schemaName = schemaName;
		return this;
	}

	public @Nonnull TableDef setTableName(@Nonnull String tableName)
	{
		this.tableName = tableName;
		return this;
	}

	public @Nonnull TableDef setEngine(@Nullable String engine)
	{
		this.engine = engine;
		return this;
	}

	public @Nonnull TableDef setCharset(@Nullable String charset)
	{
		this.charset = charset;
		return this;
	}

	public @Nonnull TableDef setAttr(@Nullable String attr)
	{
		this.attr = attr;
		return this;
	}

	public @Nonnull TableDef setComments(@Nullable String comments)
	{
		this.comments = comments;
		return this;
	}

	public @Nonnull TableDef setSQLType(@Nonnull SQLType sqlType)
	{
		this.sqlType = sqlType;
		return this;
	}

	public void colFromReader(@Nonnull DBReader r)
	{
		int i = 0;
		int j = r.colCount();
		ColumnDef col;
		while (i < j)
		{
			col = r.getColumnDef(i);
			if (col == null)
			{
				col = new ColumnDef("");
			}
			this.addCol(col);
			i++;
		}
	}

	public @Nonnull TableDef clone()
	{
		TableDef newObj = new TableDef(this.schemaName, this.tableName);
		newObj.setDatabaseName(this.databaseName);
		newObj.setEngine(this.engine);
		newObj.setCharset(this.charset);
		newObj.setAttr(this.attr);
		newObj.setComments(this.comments);
		newObj.setSQLType(this.sqlType);
		Iterator<ColumnDef> it = this.cols.iterator();
		while (it.hasNext())
		{
			newObj.addCol(it.next().clone());
		}
		return newObj;
	}
}
