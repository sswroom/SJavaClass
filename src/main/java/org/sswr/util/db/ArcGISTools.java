package org.sswr.util.db;

import java.lang.annotation.Annotation;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import jakarta.persistence.Table;

import org.sswr.util.db.DBUtil.DBType;

public class ArcGISTools {
	private static Integer getSDENextRowId(Connection conn, String dbName, String schemaName, String tableName)
	{
		dbName = DBUtil.uncol(dbName);
		schemaName = DBUtil.uncol(schemaName);
		tableName = DBUtil.uncol(tableName);
		DBType dbType = DBUtil.connGetDBType(conn);
		if (dbType == DBType.MSSQL)
		{
			if (schemaName == null || schemaName.equals(("")))
			{
				schemaName = "dbo";
			}
			try
			{
				String sql;
				if (dbName == null || dbName.length() == 0)
				{
					sql = "DECLARE @myval int;EXEC dbo.next_rowid ?, ?, @myval OUTPUT;SELECT @myval 'Next RowID';";
				}
				else
				{
					sql = "DECLARE @myval int;EXEC "+DBUtil.dbCol(dbType, dbName)+".dbo.next_rowid ?, ?, @myval OUTPUT;SELECT @myval 'Next RowID';";
				}
				PreparedStatement stmt = conn.prepareStatement(sql);
				stmt.setString(1, schemaName);
				stmt.setString(2, tableName);
				ResultSet rs = stmt.executeQuery();
				Integer id = null;
				if (rs.next())
				{
					id = rs.getInt(1);
				}
				rs.close();
				return id;
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
				return null;
			}
		}
		else if (dbType == DBType.PostgreSQL || dbType == DBType.PostgreSQLESRI)
		{
			if (schemaName == null || schemaName.equals(("")))
			{
				schemaName = "sde";
			}
			try
			{
				PreparedStatement stmt = conn.prepareStatement("select sde.next_rowid(?, ?);");
				stmt.setString(1, schemaName);
				stmt.setString(2, tableName);
				ResultSet rs = stmt.executeQuery();
				Integer id = null;
				if (rs.next())
				{
					id = rs.getInt(1);
				}
				rs.close();
				return id;
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
				return null;
			}
		}
		else
		{
			try
			{
				String s;
				if (dbName != null && dbName.length() > 0)
				{
					s = DBUtil.dbCol(dbType, dbName) + "." + DBUtil.dbCol(dbType, tableName);
				}
				else
				{
					s = DBUtil.dbCol(dbType, tableName);
				}
				PreparedStatement stmt = conn.prepareStatement("select max(OBJECTID) + 1 from "+s);
				ResultSet rs = stmt.executeQuery();
				Integer id = null;
				if (rs.next())
				{
					id = rs.getInt(1);
					if (id == 0)
					{
						id = 1;
					}
				}
				rs.close();
				return id;
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
				return null;
			}
		}
	}

	public static Integer getSDENextRowId(Connection conn, Class<?> cls)
	{
		Annotation anno[] = cls.getAnnotations();
		int i = 0;
		int j = anno.length;
		while (i < j)
		{
			if (anno[i].annotationType().equals(Table.class))
			{
				Table table = (Table)anno[i];
				return getSDENextRowId(conn, table.catalog(), table.schema(), table.name());
			}
			i++;
		}
		return null;
	}

	private static Long getSDENextRowId64(Connection conn, String dbName, String schemaName, String tableName)
	{
		dbName = DBUtil.uncol(dbName);
		schemaName = DBUtil.uncol(schemaName);
		tableName = DBUtil.uncol(tableName);
		DBType dbType = DBUtil.connGetDBType(conn);
		if (dbType == DBType.MSSQL)
		{
			if (schemaName == null || schemaName.equals(("")))
			{
				schemaName = "dbo";
			}
			try
			{
				String sql;
				if (dbName == null || dbName.length() == 0)
				{
					sql = "DECLARE @myval bigint;EXEC dbo.next_rowid64 ?, ?, @myval OUTPUT;SELECT @myval 'Next RowID';";
				}
				else
				{
					sql = "DECLARE @myval bigint;EXEC "+DBUtil.dbCol(dbType, dbName)+".dbo.next_rowid64 ?, ?, @myval OUTPUT;SELECT @myval 'Next RowID';";
				}
				PreparedStatement stmt = conn.prepareStatement(sql);
				stmt.setString(1, schemaName);
				stmt.setString(2, tableName);
				ResultSet rs = stmt.executeQuery();
				Long id = null;
				if (rs.next())
				{
					id = rs.getLong(1);
				}
				rs.close();
				return id;
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
				return null;
			}
		}
		else if (dbType == DBType.PostgreSQL || dbType == DBType.PostgreSQLESRI)
		{
			if (schemaName == null || schemaName.equals(("")))
			{
				schemaName = "sde";
			}
			try
			{
				PreparedStatement stmt = conn.prepareStatement("select sde.next_rowid64(?, ?);");
				stmt.setString(1, schemaName);
				stmt.setString(2, tableName);
				ResultSet rs = stmt.executeQuery();
				Long id = null;
				if (rs.next())
				{
					id = rs.getLong(1);
				}
				rs.close();
				return id;
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
				return null;
			}
		}
		else
		{
			try
			{
				String s;
				if (dbName != null && dbName.length() > 0)
				{
					s = DBUtil.dbCol(dbType, dbName) + "." + DBUtil.dbCol(dbType, tableName);
				}
				else
				{
					s = DBUtil.dbCol(dbType, tableName);
				}
				PreparedStatement stmt = conn.prepareStatement("select max(OBJECTID) + 1 from "+s);
				ResultSet rs = stmt.executeQuery();
				Long id = null;
				if (rs.next())
				{
					id = rs.getLong(1);
					if (id == 0)
					{
						id = 1L;
					}
				}
				rs.close();
				return id;
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
				return null;
			}
		}
	}

	public static Long getSDENextRowId64(Connection conn, Class<?> cls)
	{
		Annotation anno[] = cls.getAnnotations();
		int i = 0;
		int j = anno.length;
		while (i < j)
		{
			if (anno[i].annotationType().equals(Table.class))
			{
				Table table = (Table)anno[i];
				return getSDENextRowId64(conn, table.catalog(), table.schema(), table.name());
			}
			i++;
		}
		return null;
	}
}
