package org.sswr.util.map;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import org.sswr.util.data.QueryConditions;
import org.sswr.util.data.StringUtil;
import org.sswr.util.db.ReadingDB;
import org.sswr.util.db.SortableDBReader;
import org.sswr.util.db.TableDef;
import org.sswr.util.db.DBReader;
import org.sswr.util.io.DirectoryPackage;
import org.sswr.util.io.LogTool;
import org.sswr.util.io.PackageFile;
import org.sswr.util.io.StreamData;

public class FileGDBDir extends ReadingDB
{
	private static boolean VERBOSE = false;
	private @Nonnull PackageFile pkg;
	private @Nonnull Map<String, Integer> tableMap;
	private @Nonnull Map<String, FileGDBTable> tables;
	private @Nonnull List<String> tableNames;

	public static void setVerbose(boolean verbose)
	{
		VERBOSE = verbose;
	}

	private FileGDBDir(@Nonnull PackageFile pkg, @Nonnull FileGDBTable systemCatalog)
	{
		super(pkg.getSourceNameObj());
		this.pkg = pkg.clone();
		this.tables = new HashMap<String, FileGDBTable>();
		this.tableMap = new HashMap<String, Integer>();
		this.tableNames = new ArrayList<String>();
		FileGDBReader reader;
		if ((reader = (FileGDBReader)systemCatalog.openReader(null, 0, 0, null, null)) == null)
		{
			systemCatalog.close();
			return;
		}
		this.tableMap.put(systemCatalog.getName(), 1);
		this.tables.put(systemCatalog.getName(), systemCatalog);
		this.tableNames.add(systemCatalog.getName());
		String s;
		while (reader.readNext())
		{
			int id = reader.getInt32(0);
			int fmt = reader.getInt32(2);
			s = reader.getString(1);
			if (id > 1 && s != null && s.length() > 0 && fmt == 0)
			{
				this.tableNames.add(s);
				this.tableMap.put(s, id);
			}
		}
		reader.close();
	}

	public void close()
	{
		Iterator<FileGDBTable> itTables = tables.values().iterator();
		while (itTables.hasNext())
		{
			itTables.next().close();
		}
		tables.clear();
		this.pkg.dispose();
	}

	public @Nullable List<String> queryTableNames(@Nullable String schemaName)
	{
		List<String> names = new ArrayList<String>();
		names.addAll(this.tableNames);
		return names;
	}

	@Nullable
	public DBReader queryTableData(@Nullable String schemaName, @Nonnull String tableName, @Nullable List<String> colNames, int dataOfst, int maxCnt, @Nullable String ordering, @Nullable QueryConditions condition)
	{
		FileGDBTable table = this.getTable(tableName);
		if (table == null)
		{
			if (VERBOSE)
				System.out.println("FileGDBDir: QueryTableData failed in getting table: " + tableName);
			return null;
		}
		if (ordering == null || ordering.length() == 0)
			return table.openReader(colNames, dataOfst, maxCnt, ordering, condition);
		else
			return new SortableDBReader(this, schemaName, tableName, colNames, dataOfst, maxCnt, ordering, condition);
	}

	@Nullable
	public TableDef getTableDef(@Nullable String schemaName, @Nonnull String tableName)
	{
		FileGDBTable table;
		if ((table = this.getTable(tableName)) == null)
		{
			return null;
		}
		TableDef tab;
		DBReader r;
		tab = new TableDef(schemaName, tableName);
		if ((r = table.openReader(null, 0, 0, null, null)) != null)
		{
			tab.colFromReader(r);
			this.closeReader(r);
			return tab;
		}
		else
		{
			return null;
		}
	}
	public void closeReader(@Nonnull DBReader r)
	{
		r.close();
	}

	public @Nullable String getLastErrorMsg()
	{
		return null;
	}

	public void reconnect()
	{
	}

	public boolean isError()
	{
		return this.tableMap.size() == 0;
	}

	public @Nullable FileGDBTable getTable(@Nonnull String name)
	{
		Integer id = this.tableMap.get(name);
		if (id == null)
		{
			return null;
		}
		FileGDBTable table;
		if ((table = this.tables.get(name)) != null)
		{
			return table;
		}
		StreamData indexFD;
		StreamData tableFD;
		FileGDBTable innerTable;
		String fileName = "a" + StringUtil.toHex32(id)+".gdbtablx";
		fileName = fileName.toLowerCase();
		indexFD = this.pkg.getItemStmData(fileName);
		fileName = "a" + StringUtil.toHex32(id)+".gdbtable";
		fileName = fileName.toLowerCase();
		if ((tableFD = pkg.getItemStmData(fileName)) != null)
		{
			innerTable = new FileGDBTable(name, tableFD, indexFD);
			tableFD.close();
			if (innerTable.isError())
			{
				innerTable.close();
				if (VERBOSE)
					System.out.println("FileGDBTable: Table "+fileName+" has error");
			}
			else
			{
				if (indexFD != null) indexFD.close();
				this.tables.put(innerTable.getName(), innerTable);
				return innerTable;
			}
		}
		else if (VERBOSE)
		{
			System.out.println("FileGDBTable: Cannot get item "+fileName+" in package file");
		}
		if (indexFD != null) indexFD.close();
		return null;
	}

	@Nullable
	public static FileGDBDir openDir(@Nonnull PackageFile pkg, @Nullable LogTool logger)
	{
		FileGDBTable table;
		StreamData indexFD = pkg.getItemStmData("a00000001.gdbtablx");
		StreamData tableFD;
		if ((tableFD = pkg.getItemStmData("a00000001.gdbtable")) == null)
		{
			if (indexFD != null) indexFD.close();
			return null;
		}
		table = new FileGDBTable("GDB_SystemCatalog", tableFD, indexFD);
		tableFD.close();
		if (table.isError())
		{
			table.close();
			return null;
		}
		FileGDBReader reader = (FileGDBReader)table.openReader(null, 0, 0, null, null);
		if (reader == null)
		{
			table.close();
			return null;
		}
		FileGDBDir dir = new FileGDBDir(pkg, table);
		dir.logger = logger;
		if (dir.isError())
		{
			dir.close();
			return null;
		}
		return dir;
	}

	@Nullable
	public static FileGDBDir openDir(@Nonnull String pathName, @Nullable LogTool logger)
	{
		DirectoryPackage pkg = new DirectoryPackage(pathName);
		return openDir(pkg, logger);
	}
}
