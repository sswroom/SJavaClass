package org.sswr.util.map;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.sswr.util.data.StringUtil;
import org.sswr.util.db.DBReader;
import org.sswr.util.db.QueryConditions;
import org.sswr.util.io.DirectoryPackage;
import org.sswr.util.io.PackageFile;
import org.sswr.util.io.StreamData;

public class FileGDBDir
{
	private Map<String, FileGDBTable> tables;

	private FileGDBDir(String sourceName)
	{
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

	public DBReader getTableData(String name, int maxCnt, String ordering, QueryConditions<?> condition)
	{
		FileGDBTable table = this.tables.get(name);
		if (table == null)
		{
			return null;
		}
		return table.openReader();
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

	public static FileGDBDir openDir(PackageFile pkg)
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
		FileGDBReader reader = (FileGDBReader)table.openReader();
		if (reader == null)
		{
			table.close();
			return null;
		}
		FileGDBDir dir = new FileGDBDir(pkg.getSourceNameObj());
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

	public static FileGDBDir openDir(String pathName)
	{
		DirectoryPackage pkg = new DirectoryPackage(pathName);
		return openDir(pkg);
	}
}
