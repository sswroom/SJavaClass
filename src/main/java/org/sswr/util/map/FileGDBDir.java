package org.sswr.util.map;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.Table;

import org.sswr.util.data.ArtificialQuickSort;
import org.sswr.util.data.DataTools;
import org.sswr.util.data.FieldComparator;
import org.sswr.util.data.StringUtil;
import org.sswr.util.db.DBColumnInfo;
import org.sswr.util.db.DBConnection;
import org.sswr.util.db.DBReader;
import org.sswr.util.db.DBUtil;
import org.sswr.util.db.PageStatus;
import org.sswr.util.db.QueryConditions;
import org.sswr.util.io.DirectoryPackage;
import org.sswr.util.io.LogTool;
import org.sswr.util.io.PackageFile;
import org.sswr.util.io.StreamData;

public class FileGDBDir extends DBConnection
{
	private Map<String, FileGDBTable> tables;

	private FileGDBDir(String sourceName, LogTool logger)
	{
		super(logger);
		this.tables = new HashMap<String, FileGDBTable>();
	}

	public void close()
	{
		Iterator<FileGDBTable> itTables = tables.values().iterator();
		while (itTables.hasNext())
		{
			itTables.next().close();
		}
		tables.clear();
	}

	public int getTableNames(List<String> names)
	{
		names.addAll(this.tables.keySet());
		return this.tables.size();
	}

	public DBReader getTableData(String name, List<String> colNames, int maxCnt, String ordering, QueryConditions<?> condition)
	{
		FileGDBTable table = this.tables.get(name);
		if (table == null)
		{
			return null;
		}
		return table.openReader(colNames);
	}

	public void closeReader(DBReader r)
	{
		r.close();
	}

	public void getErrorMsg(StringBuilder str)
	{
	}

	public void reconnect()
	{
	}

	public void addTable(FileGDBTable table)
	{
		this.tables.put(table.getName(), table);
	}

	public static FileGDBDir openDir(PackageFile pkg, LogTool logger)
	{
		StreamData fd = pkg.getItemStmData("a00000001.gdbtable");
		FileGDBTable table;
		if (fd == null)
		{
			return null;
		}
		table = new FileGDBTable("GDB_SystemCatalog", fd);
		fd.close();
		if (table.isError())
		{
			table.close();
			return null;
		}
		FileGDBReader reader = (FileGDBReader)table.openReader(null);
		if (reader == null)
		{
			table.close();
			return null;
		}
		FileGDBDir dir = new FileGDBDir(pkg.getSourceNameObj(), logger);
		dir.addTable(table);
		while (reader.readNext())
		{
			int id = reader.getInt32(0);
			String name = reader.getString(1);
			int fmt = reader.getInt32(2);
			if (id > 1 && name != null && fmt == 0)
			{
				FileGDBTable innerTable;
				String fileName = "a" + StringUtil.toHex32(id).toLowerCase() + ".gdbtable";
				fd = pkg.getItemStmData(fileName);
				if (fd != null)
				{
					innerTable = new FileGDBTable(name, fd);
					fd.close();
					if (innerTable.isError())
					{
						innerTable.close();
					}
					else
					{
						dir.addTable(innerTable);
					}
				}
			}
		}
		reader.close();
		return dir;
	}

	public static FileGDBDir openDir(String pathName, LogTool logger)
	{
		DirectoryPackage pkg = new DirectoryPackage(pathName);
		return openDir(pkg, logger);
	}

	@Override
	public <T> List<T> loadItemsAsList(Class<T> cls, Object parent, QueryConditions<T> conditions, List<String> joinFields, String sortString, int dataOfst, int dataCnt)
	{
		Table tableAnn = parseClassTable(cls);
		if (tableAnn == null)
		{
			throw new IllegalArgumentException("Class annotation is not valid");
		}
		Constructor<T> constr = getConstructor(cls, parent);
		ArrayList<DBColumnInfo> cols = new ArrayList<DBColumnInfo>();
		ArrayList<DBColumnInfo> idCols = new ArrayList<DBColumnInfo>();
		DBUtil.parseDBCols(cls, cols, idCols, joinFields);
		DBReader r = this.getTableData(tableAnn.name(), DataTools.createValueList(String.class, cols, "colName", null), 0, null, conditions);
		List<T> retList;
		List<QueryConditions<T>.Condition> clientConditions;
		if (conditions == null)
		{
			clientConditions = List.of();
		}
		else
		{
			clientConditions = conditions.toList();
		}

		if (sortString != null)
		{
			FieldComparator<T> fieldComp;
			try
			{
				fieldComp = new FieldComparator<T>(cls, sortString);
			}
			catch (NoSuchFieldException ex)
			{
				if (this.logger != null) this.logger.logException(ex);
				throw new IllegalArgumentException("sortString is not valid ("+sortString+")");
			}
			retList = this.readAsList(r, PageStatus.NO_PAGE, 0, 0, parent, constr, cols, clientConditions);
			ArtificialQuickSort.sort(retList, fieldComp);
			if (dataOfst > 0)
			{
				if (dataOfst >= retList.size())
				{
					retList.clear();
				}
				else
				{
					ArrayList<T> remList = new ArrayList<T>();
					int i = 0;
					while (i < dataOfst)
					{
						remList.add(retList.get(i));
						i++;
					}
					retList.removeAll(remList);
				}
			}
			if (dataCnt > 0)
			{
				if (dataCnt < retList.size())
				{
					int i = retList.size();
					while (i-- > dataCnt)
					{
						retList.remove(i);
					}
				}
			}
		}
		else
		{
			retList = this.readAsList(r, PageStatus.NO_PAGE, dataOfst, dataCnt, parent, constr, cols, clientConditions);
		}
		r.close();
		return retList;
	}
}
