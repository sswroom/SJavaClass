package org.sswr.util.db;

import java.util.List;

import org.sswr.util.data.DateTimeUtil;
import org.sswr.util.data.QueryConditions;
import org.sswr.util.io.ParsedObject;
import org.sswr.util.io.ParserType;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public abstract class ReadingDB extends ParsedObject {
	public ReadingDB(@Nonnull String sourceName)
	{
		super(sourceName);
	}

	public void dispose()
	{

	}

	public @Nullable List<String> querySchemaNames()
	{
		return null;
	}

	public abstract @Nullable List<String> queryTableNames(@Nullable String schemaName);
	public abstract @Nullable DBReader queryTableData(@Nullable String schemaName, @Nonnull String tableName, @Nullable List<String> colNames, int dataOfst, int maxCnt, @Nullable String ordering, @Nullable QueryConditions condition);
	public abstract @Nullable TableDef getTableDef(@Nullable String schemaName, @Nonnull String tableName);
	public abstract void closeReader(@Nonnull DBReader r);
	public abstract @Nullable String getLastErrorMsg();
	public abstract void reconnect();
	public byte getTzQhr()
	{
		return DateTimeUtil.getLocalTZQhr();
	}

	public @Nullable List<String> getDatabaseNames()
	{
		return null;
	}

	public boolean changeDatabase(@Nonnull String databaseName)
	{
		return false;
	}

	public @Nullable String getCurrDBName()
	{
		return null;
	}

	public @Nonnull ParserType getParserType()
	{
		return ParserType.ReadingDB;
	}

	public boolean isFullConn()
	{
		return false;
	}
	
	public boolean isDBTool()
	{
		return false;
	}

	public static boolean isDBObj(@Nonnull ParsedObject pobj)
	{
		ParserType pt = pobj.getParserType();
		if (pt == ParserType.MapLayer || pt == ParserType.ReadingDB)
		{
			return true;
		}
		return false;
	}
}
