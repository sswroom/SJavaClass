package org.sswr.util.db;

import java.lang.reflect.InvocationTargetException;
import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.locationtech.jts.geom.Geometry;
import org.sswr.util.basic.ArrayListStr;
import org.sswr.util.data.ArtificialQuickSort;
import org.sswr.util.data.DateTimeUtil;
import org.sswr.util.data.MapFieldComparator;
import org.sswr.util.data.MapObjectGetter;
import org.sswr.util.data.QueryConditions;
import org.sswr.util.data.VariItemUtil;
import org.sswr.util.math.geometry.Vector2D;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class SortableDBReader extends DBReader
{
	private int currIndex;
	private @Nonnull List<Map<String, Object>> objList;
	private @Nonnull List<ColumnDef> cols;

	@Nullable
	private Object getItem(int colIndex)
	{
		Map<String, Object> obj;
		if (this.currIndex >= this.objList.size() || this.currIndex < 0)
			return null;
		if ((obj = this.objList.get(this.currIndex)) == null)
			return null;
		ColumnDef col;
		if ((col = this.cols.get(colIndex)) == null)
			return null;
		return obj.get(col.getColName());
	}

	public SortableDBReader(@Nonnull ReadingDB db, @Nullable String schemaName, @Nonnull String tableName, @Nullable List<String> colNames, int dataOfst, int maxCnt, @Nullable String ordering, @Nullable QueryConditions condition)
	{
		this.currIndex = -1;
		this.objList = new ArrayList<Map<String, Object>>();
		this.cols = new ArrayList<ColumnDef>();
		int i;
		int j;
		ColumnDef colDef = new ColumnDef("");
		Map<String, Object> obj;
		List<String> nncolNames;
		QueryConditions nncondition;
		if ((nncolNames = colNames) == null || nncolNames.size() == 0)
		{
			DBReader r;
			if ((r = db.queryTableData(schemaName, tableName, null, 0, 0, null, null)) == null)
			{
				return;
			}

			i = 0;
			j = r.colCount();
			while (i < j)
			{
				if ((colDef = r.getColumnDef(i)) != null)
				{
					this.cols.add(colDef);
				}
				i++;
			}
			while (r.readNext())
			{
				obj = r.getRowMap();
				try
				{
					if ((nncondition = condition) == null || nncondition.isValid(new MapObjectGetter(obj)))
					{
						this.objList.add(obj);
					}
				}
				catch (InvocationTargetException | IllegalAccessException ex)
				{
					this.objList.add(obj);
				}
			}
			db.closeReader(r);
		}
		else
		{
			ArrayListStr dbColNames = new ArrayListStr();
			i = 0;
			j = nncolNames.size();
			while (i < j)
			{
				String colName;
				if ((colName = nncolNames.get(i)) != null && dbColNames.sortedIndexOf(colName) < 0)
				{
					dbColNames.sortedInsert(colName);
				}
				i++;
			}
			if ((nncondition = condition) != null)
			{
				ArrayListStr condColNames = new ArrayListStr();
				nncondition.getFieldList(condColNames);
				i = 0;
				j = condColNames.size();
				while (i < j)
				{
					String colName;
					if ((colName = condColNames.get(i)) != null && dbColNames.sortedIndexOf(colName) < 0)
					{
						dbColNames.sortedInsert(colName);
					}
					i++;
				}
			}
			DBReader r;
			if ((r = db.queryTableData(schemaName, tableName, dbColNames, 0, 0, null, null)) == null)
			{
				return;
			}

			Map<String, ColumnDef> tmpCols = new HashMap<String, ColumnDef>();
			i = 0;
			j = r.colCount();
			while (i < j)
			{
				if ((colDef = r.getColumnDef(i)) != null)
				{
					tmpCols.put(colDef.getColName(), colDef);
				}
				i++;
			}
			while (r.readNext())
			{
				obj = r.getRowMap();
				try
				{
					if ((nncondition = condition) == null || nncondition.isValid(new MapObjectGetter(obj)))
					{
						this.objList.add(obj);
					}
				}
				catch (IllegalAccessException | InvocationTargetException ex)
				{
					this.objList.add(obj);
				}
			}
			db.closeReader(r);

			ColumnDef col;
			Iterator<String> it = nncolNames.iterator();
			while (it.hasNext())
			{
				col = tmpCols.get(it.next());
				if (col != null)
				{
					this.cols.add(col);
				}
				i++;
			}

			tmpCols.clear();
		}
		if (ordering != null && ordering.length() > 0)
		{
			MapFieldComparator comparator = new MapFieldComparator(ordering);
			ArtificialQuickSort.sort(this.objList, comparator);
		}
		if (dataOfst > 0)
		{
			if (dataOfst >= this.objList.size())
			{
				this.objList.clear();
			}
			else
			{
				this.objList.removeAll(this.objList.subList(0, dataOfst));
			}
		}
		if (maxCnt != 0 && maxCnt < this.objList.size())
		{
			i = this.objList.size();
			while (i-- > maxCnt)
			{
				this.objList.remove(i);
			}
		}
	}
	@Override
	public void close() {
	}

	@Override
	public boolean readNext() {
		if (this.currIndex == this.objList.size())
		{
			return false;
		}
		this.currIndex++;
		if (this.currIndex == this.objList.size())
		{
			return false;
		}
		return true;
	}

	@Override
	public int colCount() {
		return this.cols.size();
	}

	@Override
	public int getRowChanged() {
		return 0;
	}

	@Override
	public int getInt32(int colIndex) {
		Object item = this.getItem(colIndex);
		if (item == null)
			return 0;
		return VariItemUtil.asI32(item);
	}

	@Override
	public long getInt64(int colIndex) {
		Object item = this.getItem(colIndex);
		if (item == null)
			return 0;
		return VariItemUtil.asI64(item);
	}

	@Override
	@Nullable
	public String getString(int colIndex) {
		Object item = this.getItem(colIndex);
		if (item == null)
			return null;
		return VariItemUtil.asStr(item);
	}

	@Override
	@Nullable
	public ZonedDateTime getDate(int colIndex) {
		Object item = this.getItem(colIndex);
		if (item == null)
			return null;
		Timestamp ts = VariItemUtil.asTimestamp(item);
		if (ts == null)
			return null;
		return DateTimeUtil.newZonedDateTime(ts);
	}

	@Override
	public double getDblOrNAN(int colIndex) {
		Object item = this.getItem(colIndex);
		if (item == null)
			return Double.NaN;
		return VariItemUtil.asF64(item);
	}

	@Override
	public boolean getBool(int colIndex) {
		Object item = this.getItem(colIndex);
		if (item == null)
			return false;
		return VariItemUtil.asBool(item);
	}

	@Override
	@Nullable
	public byte[] getBinary(int colIndex) {
		Object item = this.getItem(colIndex);
		if (item == null)
			return null;
		return VariItemUtil.asByteArr(item);
	}

	@Override
	@Nullable
	public Vector2D getVector(int colIndex) {
		Object item = this.getItem(colIndex);
		if (item == null)
			return null;
		return VariItemUtil.asVector(item);
	}

	@Override
	@Nullable
	public Geometry getGeometry(int colIndex) {
		Object item = this.getItem(colIndex);
		if (item == null)
			return null;
		return VariItemUtil.asGeometry(item);
	}

	@Override
	@Nullable
	public Object getObject(int colIndex) {
		return this.getItem(colIndex);
	}

	@Override
	public boolean isNull(int colIndex) {
		return this.getItem(colIndex) == null;
	}

	@Override
	@Nullable
	public String getName(int colIndex) {
		ColumnDef col;
		if (colIndex < 0 || colIndex >= this.cols.size()) return null;
		if ((col = this.cols.get(colIndex)) != null)
		{
			return col.getColName();
		}
		return null;
	}

	@Override
	@Nonnull
	public ColumnType getColumnType(int colIndex) {
		ColumnDef col;
		if (colIndex < 0 || colIndex >= this.cols.size()) return ColumnType.Unknown;
		if ((col = this.cols.get(colIndex)) != null)
		{
			return col.getColType();
		}
		return ColumnType.Unknown;
	}

	@Override
	@Nullable
	public ColumnDef getColumnDef(int colIndex) {
		if (colIndex < 0 || colIndex >= this.cols.size()) return null;
		return this.cols.get(colIndex);
	}
}
