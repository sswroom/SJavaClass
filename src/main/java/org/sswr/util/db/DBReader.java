package org.sswr.util.db;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

import org.locationtech.jts.geom.Geometry;
import org.sswr.util.math.geometry.Vector2D;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public abstract class DBReader
{
	public abstract void close();

	public abstract boolean readNext();
	public abstract int colCount();
	public abstract int getRowChanged(); //-1 = error
	public abstract int getInt32(int colIndex);
	public abstract long getInt64(int colIndex);
	@Nullable
	public abstract String getString(int colIndex);
	@Nullable
	public abstract ZonedDateTime getDate(int colIndex);
	public abstract double getDblOrNAN(int colIndex);
	public abstract boolean getBool(int colIndex);
	@Nullable
	public abstract byte[] getBinary(int colIndex);
	@Nullable
	public abstract Vector2D getVector(int colIndex);
	@Nullable
	public abstract Geometry getGeometry(int colIndex);
	@Nullable
	public abstract Object getObject(int colIndex);

	public abstract boolean isNull(int colIndex);
	@Nullable
	public abstract String getName(int colIndex);
	@Nonnull 
	public abstract ColumnType getColumnType(int colIndex);
	@Nullable
	public abstract ColumnDef getColumnDef(int colIndex);	

	@Nonnull
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
