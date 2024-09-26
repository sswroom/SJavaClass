package org.sswr.util.db;

import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.persistence.Table;

import org.sswr.util.data.FieldComparator;
import org.sswr.util.data.StringUtil;
import org.sswr.util.db.DBUtil.DBType;
import org.sswr.util.io.LogLevel;
import org.sswr.util.io.LogTool;

public class SQLConnection extends ReadingConnection
{
	private Connection conn;
	private DBType dbType;
	private String errorMsg;
	private List<String> tableNames;

	public SQLConnection(@Nonnull Connection conn, @Nullable LogTool logger)
	{
		super(logger);
		this.conn = conn;
		this.logger = logger;
		this.dbType = DBUtil.connGetDBType(this.conn);
		this.errorMsg = null;
		this.tableNames = null;
	}

	public void close()
	{
		if (this.conn != null)
		{
			try
			{
				this.conn.close();
			}
			catch (SQLException ex)
			{
				this.errorMsg = ex.getMessage();
			}
			this.conn = null;
		}
	}

	public int getTableNames(@Nonnull List<String> names)
	{
		if (this.tableNames == null)
		{
			this.tableNames = new ArrayList<String>();
			///////////////////////////////////
		}
		names.addAll(this.tableNames);
		return this.tableNames.size();
	}

	@Nullable
	public DBReader getTableData(@Nonnull String name, int maxCnt, @Nullable String sortString, @Nullable QueryConditions<?> condition)
	{
/*		FileGDBTable table = this.tables.get(name);
		if (table == null)
		{
			return null;
		}
		return table.openReader();*/
		return null;
	}

	@Nullable
	public <T> List<T> loadItemsAsList(@Nonnull Class<T> cls, @Nullable Object parent, @Nullable QueryConditions<T> conditions, @Nullable List<String> joinFields, @Nullable String sortString, int dataOfst, int dataCnt)
	{
		StringBuilder sb;
		Table tableAnn = parseClassTable(cls);
		if (tableAnn == null)
		{
			throw new IllegalArgumentException("Class annotation is not valid");
		}
		DBType dbType = this.dbType;
		if (dbType == DBType.PostgreSQL && "sde".equals(tableAnn.schema()))
		{
			dbType = DBType.PostgreSQLESRI;
		}

		Constructor<T> constr = getConstructor(cls, parent);
		FieldComparator<T> fieldComp;
		if (sortString == null)
		{
			fieldComp = null;
		}
		else
		{
			try
			{
				fieldComp = new FieldComparator<T>(cls, sortString);
			}
			catch (NoSuchFieldException ex)
			{
				if (this.logger != null) this.logger.logException(ex);
				throw new IllegalArgumentException("sortString is not valid ("+sortString+")");
			}
		}

		ArrayList<DBColumnInfo> cols = new ArrayList<DBColumnInfo>();
		ArrayList<DBColumnInfo> idCols = new ArrayList<DBColumnInfo>();
		DBUtil.parseDBCols(cls, cols, idCols, joinFields);

		if (cols.size() == 0)
		{
			throw new IllegalArgumentException("No selectable column found");
		}

		Map<String, DBColumnInfo> colsMap = dbCols2Map(cols);
		sb = new StringBuilder();
		PageStatus status = DBUtil.appendSelect(sb, cols, tableAnn, dbType, dataOfst, dataCnt);

		List<QueryConditions<T>.Condition> clientConditions = new ArrayList<QueryConditions<T>.Condition>();
		if (conditions != null)
		{
			String whereClause = conditions.toWhereClause(colsMap, dbType, clientConditions, DBUtil.MAX_SQL_ITEMS);
			if (!StringUtil.isNullOrEmpty(whereClause))
			{
				sb.append(" where ");
				sb.append(whereClause);
			}
		}
		if (fieldComp != null)
		{
			sb.append(" order by ");
			sb.append(fieldComp.toOrderClause(colsMap, dbType));
		}
		if ((dataOfst != 0 || dataCnt != 0) && status != PageStatus.SUCC)
		{
			if (dbType == DBType.MySQL)
			{
				sb.append(" LIMIT ");
				sb.append(dataOfst);
				sb.append(", ");
				sb.append(dataCnt);
				status = PageStatus.SUCC;
			}
			else if (dbType == DBType.MSSQL)
			{
				if (fieldComp != null)
				{
					sb.append(" offset ");
					sb.append(dataOfst);
					sb.append(" row fetch next ");
					sb.append(dataCnt);
					sb.append(" row only");
					status = PageStatus.SUCC;
				}
			}
			else if (dbType == DBType.PostgreSQL || dbType == DBType.PostgreSQLESRI)
			{
				if (dataCnt != 0)
				{
					sb.append(" LIMIT ");
					sb.append(dataCnt);
				}
				if (dataOfst != 0)
				{
					sb.append(" OFFSET ");
					sb.append(dataOfst);
				}
				status = PageStatus.SUCC;
			}
		}
		try
		{
			if (this.logger != null) this.logger.logMessage(sb.toString(), LogLevel.COMMAND);

			PreparedStatement stmt = conn.prepareStatement(sb.toString());
			DBReader r = new SQLReader(dbType, stmt.executeQuery());
			List<T> retList = this.readAsList(r, status, dataOfst, dataCnt, parent, constr, cols, clientConditions);
			r.close();
			return retList;
		}
		catch (SQLException ex)
		{
			if (this.logger != null) this.logger.logException(ex);
			return null;
		}
	}

	@Nullable
	public <T> Map<Integer, T> loadItemsIClass(@Nonnull Class<T> cls, @Nullable Object parent, @Nullable QueryConditions<T> conditions, @Nullable List<String> joinFields)
	{
		StringBuilder sb;
		Table tableAnn = parseClassTable(cls);
		if (tableAnn == null)
		{
			throw new IllegalArgumentException("Class annotation is not valid");
		}
		DBType dbType = this.dbType;
		if (dbType == DBType.PostgreSQL && "sde".equals(tableAnn.schema()))
		{
			dbType = DBType.PostgreSQLESRI;
		}

		Constructor<T> constr = getConstructor(cls, parent);

		ArrayList<DBColumnInfo> cols = new ArrayList<DBColumnInfo>();
		ArrayList<DBColumnInfo> idCols = new ArrayList<DBColumnInfo>();
		DBUtil.parseDBCols(cls, cols, idCols, joinFields);

		if (cols.size() == 0)
		{
			throw new IllegalArgumentException("No selectable column found");
		}
		if (idCols.size() > 1)
		{
			throw new IllegalArgumentException("Multiple id column found");
		}
		if (idCols.size() == 0)
		{
			throw new IllegalArgumentException("No Id column found");
		}

		sb = new StringBuilder();
		DBUtil.appendSelect(sb, cols, tableAnn, dbType, 0, 0);

		List<QueryConditions<T>.Condition> clientConditions = new ArrayList<QueryConditions<T>.Condition>();
		if (conditions != null)
		{
			Map<String, DBColumnInfo> colsMap = dbCols2Map(cols);
			String whereClause = conditions.toWhereClause(colsMap, dbType, clientConditions, DBUtil.MAX_SQL_ITEMS);
			if (!StringUtil.isNullOrEmpty(whereClause))
			{
				sb.append(" where ");
				sb.append(whereClause);
			}
		}
		try
		{
			if (this.logger != null) this.logger.logMessage(sb.toString(), LogLevel.COMMAND);

			PreparedStatement stmt = conn.prepareStatement(sb.toString());
			DBReader r = new SQLReader(dbType, stmt.executeQuery());
			Map<Integer, T> retMap = this.readAsMap(r, parent, constr, cols, clientConditions);
			r.close();
			return retMap;
		}
		catch (SQLException ex)
		{
			if (this.logger != null) this.logger.logException(ex);
			return null;
		}
	}

	public void closeReader(@Nonnull DBReader r)
	{
		r.close();
	}

	public void getErrorMsg(@Nonnull StringBuilder str)
	{
		if (this.errorMsg != null)
			str.append(this.errorMsg);
	}

	public void reconnect()
	{
	}

}
