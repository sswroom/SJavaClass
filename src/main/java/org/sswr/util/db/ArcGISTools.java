package org.sswr.util.db;

import java.lang.annotation.Annotation;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.persistence.Table;

import org.sswr.util.db.DBUtil.DBType;

public class ArcGISTools {
	private static Integer getSDENextRowId(Connection conn, String dbName, String tableName)
	{
		dbName = DBUtil.uncol(dbName);
		tableName = DBUtil.uncol(tableName);
		DBType dbType = DBUtil.connGetDBType(conn);
		if (dbType == DBType.MSSQL)
		{
			try
			{
				PreparedStatement stmt = conn.prepareStatement("DECLARE @myval int;EXEC "+DBUtil.dbCol(dbType, dbName)+".dbo.next_rowid 'dbo', ?, @myval OUTPUT;SELECT @myval 'Next RowID';");
				stmt.setString(1, tableName);
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
				return getSDENextRowId(conn, table.catalog(), table.name());
			}
			i++;
		}
		return null;
	}

	private static Long getSDENextRowId64(Connection conn, String dbName, String tableName)
	{
		dbName = DBUtil.uncol(dbName);
		tableName = DBUtil.uncol(tableName);
		DBType dbType = DBUtil.connGetDBType(conn);
		if (dbType == DBType.MSSQL)
		{
			try
			{
				PreparedStatement stmt = conn.prepareStatement("DECLARE @myval bigint;EXEC "+DBUtil.dbCol(dbType, dbName)+".dbo.next_rowid64 'dbo', ?, @myval OUTPUT;SELECT @myval 'Next RowID';");
				stmt.setString(1, tableName);
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
				return getSDENextRowId64(conn, table.catalog(), table.name());
			}
			i++;
		}
		return null;
	}
}
