package org.sswr.util.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.sswr.util.data.QueryConditions;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class TextDB extends ReadingDB
{
	public static class DBData
	{
		public String name;
		public List<String> colList;
		public List<String[]> valList;
	}

	private Map<String, DBData> dbMap;
	private @Nullable DBData currDB;

	public TextDB(@Nonnull String sourceName)
	{
		super(sourceName);
		this.dbMap = new HashMap<String, DBData>();
		this.currDB = null;
	}

	@Override
	public @Nullable List<String> queryTableNames(@Nullable String schemaName) {
		List<String> names = new ArrayList<String>();
		Iterator<String> it = this.dbMap.keySet().iterator();
		while (it.hasNext())
		{
			names.add(it.next());
		}
		return names;
	}

	@Override
	public @Nullable DBReader queryTableData(@Nullable String schemaName, @Nonnull String tableName,
			@Nullable List<String> colNames, int dataOfst, int maxCnt, @Nullable String ordering,
			@Nullable QueryConditions condition) {
		DBData data;
		if ((data = this.dbMap.get(tableName)) == null)
		{
			return null;
		}
		DBReader reader = new TextDBReader(data);
		return reader;
	}

	@Override
	public @Nullable TableDef getTableDef(@Nullable String schemaName, @Nonnull String tableName) {
		DBData data;
		if ((data = this.dbMap.get(tableName)) == null)
		{
			return null;
		}
		TableDef tab = new TableDef(schemaName, data.name);
		ColumnDef colDef;
		Iterator<String> it = data.colList.iterator();
		while (it.hasNext())
		{
			colDef = new ColumnDef(it.next());
			colDef.setColSize(256);
			colDef.setColType(ColumnType.VarUTF8Char);
			colDef.setAttr(null);
			colDef.setColDP(0);
			colDef.setDefVal(null);
			colDef.setAutoIncNone();
			colDef.setNotNull(false);
			colDef.setPk(false);
			tab.addCol(colDef);
		}
		return tab;
	}

	@Override
	public void closeReader(@Nonnull DBReader r) {
	}

	@Override
	public @Nullable String getLastErrorMsg() {
		return null;
	}

	@Override
	public void reconnect() {
	}

	public boolean addTable(@Nonnull String tableName, @Nonnull List<String> colList)
	{
		DBData data;
		if ((data = this.dbMap.get(tableName)) != null)
			return false;
		int i;
		int j;
		data = new DBData();
		data.colList = new ArrayList<String>();
		data.valList = new ArrayList<String[]>();
		data.name = tableName;
		i = 0;
		j = colList.size();
		while (i < j)
		{
			data.colList.add(colList.get(i));
			i++;
		}
		this.dbMap.put(tableName, data);
		this.currDB = data;
		return true;
	}

	public boolean addTable(@Nonnull String tableName, @Nonnull String[] colArr)
	{
		DBData data;
		if ((data = this.dbMap.get(tableName)) != null)
			return false;
		int i;
		int j;
		data = new DBData();
		data.colList = new ArrayList<String>();
		data.valList = new ArrayList<String[]>();
		data.name = tableName;
		i = 0;
		j = colArr.length;
		while (i < j)
		{
			data.colList.add(colArr[i]);
			i++;
		}
		this.dbMap.put(tableName, data);
		this.currDB = data;
		return true;
	}

	public boolean addTableData(@Nonnull List<String> valList)
	{
		DBData currDB;
		if ((currDB = this.currDB) == null)
			return false;
		if (currDB.colList.size() != valList.size())
		{
			return false;
		}
		int i = 0;
		int j = valList.size();
		String[] vals = new String[j];
		while (i < j)
		{
			vals[i] = valList.get(i);
			i++;
		}
		currDB.valList.add(vals);
		return true;
	}

	public boolean addTableData(@Nonnull String[] valArr)
	{
		DBData currDB;
		if ((currDB = this.currDB) == null)
			return false;
		int colCount = valArr.length;
		if (currDB.colList.size() != colCount)
		{
			return false;
		}
		int i = 0;
		String[] vals = new String[colCount];
		while (i < colCount)
		{
			vals[i] = valArr[i];
			i++;
		}
		currDB.valList.add(vals);
		return true;
	}
}
