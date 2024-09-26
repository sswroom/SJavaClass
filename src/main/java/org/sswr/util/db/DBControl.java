package org.sswr.util.db;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.sswr.util.db.DBUtil.DBType;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class DBControl
{
	private DataSource ds;
	private String url;
	private String userName;
	private String password;
	private Connection conn;
	private Charset charset;
	private DBType dbType;
	private String dbVersion;

	public DBControl(@Nonnull DataSource ds) throws SQLException
	{
		this.ds = ds;
		this.init(this.ds.getConnection());
	}

	public DBControl(@Nonnull String url, @Nullable String userName, @Nullable String password) throws SQLException
	{
		this.url = url;
		this.userName = userName;
		this.password = password;
		this.init(DriverManager.getConnection(this.url, this.userName, this.password));
	}

	public void init(@Nonnull Connection conn)
	{
		this.conn = conn;
		this.charset = StandardCharsets.UTF_8;
		this.dbType = DBUtil.connGetDBType(this.conn);
		if (this.dbType == DBType.MSSQL)
		{
			this.dbVersion = this.getSingleValue("select @@VERSION");
			String coll = this.getSingleValue("SELECT collation_name FROM sys.databases");
			if(coll != null)
			{
				this.charset = DBUtil.mssqlCollationGetCharset(coll);
			}
		}
	}

	@Nullable
	public String getSingleValue(@Nonnull String sql)
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

	public int getStringLen(@Nullable String s)
	{
		if (s == null)
		{
			return 0;
		}
		switch (this.dbType)
		{
		case MSSQL:
			return getNVarcharLen(s);
		case Unknown:
		default:
			return s.length();
		}
	}

	public int getNVarcharLen(@Nonnull String s)
	{
		byte b[] = s.getBytes(StandardCharsets.UTF_16LE);
		return b.length >> 1;
	}

	public int getVarcharLen(@Nonnull String s)
	{
		byte b[] = s.getBytes(charset);
		return b.length;
	}

	@Nullable
	public String getDbVersion()
	{
		return this.dbVersion;
	}

	@Nullable
	public synchronized Connection getConn()
	{
		try
		{
			if (this.conn == null || this.conn.isClosed())
			{
				this.reconn();
			}
		}
		catch (SQLException ex)
		{

		}
		return this.conn;
	}

	private void reconn() throws SQLException
	{
		Connection newConn;
		if (this.ds != null)
		{
			newConn = this.ds.getConnection();
		}
		else
		{
			newConn = DriverManager.getConnection(this.url, this.userName, this.password);
		}
		try
		{
			if (this.conn != null)
			{
				this.conn.close();
			}
		}
		catch (SQLException ex)
		{

		}
		this.conn = newConn;
	}

	public void close()
	{
		try
		{
			if (this.conn != null)
			{
				this.conn.close();
			}
		}
		catch (SQLException ex)
		{
		}
		this.conn = null;
	}
}
