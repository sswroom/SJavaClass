package org.sswr.util.db;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.sswr.util.db.DBUtil.DBType;

public class DBControl {
	private Connection conn;
	private Charset charset;
	private DBType dbType;
	private String dbVersion;

	public DBControl(Connection conn)
	{
		this.conn = conn;
		this.charset = StandardCharsets.UTF_8;
		this.dbType = DBUtil.connGetDBType(this.conn);
		if (this.dbType == DBType.DT_MSSQL)
		{
			this.dbVersion = this.getSingleValue("select @@VERSION");
			String coll = this.getSingleValue("SELECT collation_name FROM sys.databases");
			this.charset = DBUtil.mssqlCollationGetCharset(coll);
		}
	}

	public String getSingleValue(String sql)
	{
		String ret = null;
		try
		{
			PreparedStatement stmt = conn.prepareStatement(sql);
			ResultSet rs = stmt.executeQuery();
			if (rs.next())
			{
				ret = rs.getString(1);
			}
			rs.close();
		}
		catch (SQLException ex)
		{

		}
		return ret;
	}

	public int getStringLen(String s)
	{
		if (s == null)
		{
			return 0;
		}
		switch (this.dbType)
		{
		case DT_MSSQL:
			return getNVarcharLen(s);
		case DT_UNKNOWN:
		default:
			return getVarcharLen(s);
		}
	}

	public int getNVarcharLen(String s)
	{
		byte b[] = s.getBytes(StandardCharsets.UTF_16LE);
		return b.length >> 1;
	}

	public int getVarcharLen(String s)
	{
		byte b[] = s.getBytes(charset);
		return b.length;
	}

	public String getDbVersion()
	{
		return this.dbVersion;
	}
}
