package org.sswr.util.db;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

import org.sswr.util.math.Vector2D;

public abstract class DBReader
{
	public abstract void close();

	public abstract boolean readNext();
	public abstract int colCount();
	public abstract int getRowChanged(); //-1 = error
	public abstract int getInt32(int colIndex);
	public abstract long getInt64(int colIndex);
	public abstract String getString(int colIndex);
	public abstract ZonedDateTime getDate(int colIndex);
	public abstract double getDbl(int colIndex);
	public abstract boolean getBool(int colIndex);
	public abstract byte[] getBinary(int colIndex);
	public abstract Vector2D getVector(int colIndex);
	public abstract Object getObject(int colIndex);

	public abstract boolean isNull(int colIndex);
	public abstract String getName(int colIndex);
	public abstract ColumnType getColumnType(int colIndex);
	public abstract ColumnDef getColumnDef(int colIndex);	

	public Map<String, Object> getRowMap()
	{
		int i = 0;
		int j = this.colCount();
		Map<String, Object> map = new HashMap<String, Object>();
		while (i < j)
		{
			map.put(this.getName(i), this.getObject(i));
			i++;
		}
		return map;
	}
}
