package org.sswr.util.db;

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

public class PoolConnection implements Connection
{
	private Connection conn;
	private PoolDataSource dataSource;
	private String url;
	private String username;
	private String password;
	private boolean using;
	private int recvTimeoutMS;

	private Connection getConn() throws SQLException
	{
		if (this.conn == null)
		{
			this.conn = DriverManager.getConnection(this.url, this.username, this.password);
			this.conn.setNetworkTimeout(null, recvTimeoutMS);
			return this.conn;
		}
		else if (this.conn.isClosed())
		{
			try
			{
				this.conn.close();
			}
			catch (SQLException ex)
			{

			}
			this.conn = null;
			this.conn = DriverManager.getConnection(this.url, this.username, this.password);
			this.conn.setNetworkTimeout(null, recvTimeoutMS);
			return this.conn;
		}
		else
		{
			return this.conn;
		}
	}

	public PoolConnection(PoolDataSource dataSource, String url, String username, String password)
	{
		this.dataSource = dataSource;
		this.url = url;
		this.username = username;
		this.password = password;
		this.using = false;
		this.recvTimeoutMS = 30000;
	}

	public void setRecvTimeout(int timeoutMS)
	{
		this.recvTimeoutMS = timeoutMS;
	}

	void open()
	{
		this.using = true;
	}
	
	boolean isUsing()
	{
		return this.using;
	}

	void endConn()
	{
		if (this.conn != null)
		{
			try
			{
				this.conn.close();
			}
			catch (SQLException ex)
			{

			}
			this.conn = null;
		}
	}

	public Class<? extends Connection> getConnClass()
	{
		try
		{
			return getConn().getClass();
		}
		catch (SQLException ex)
		{
			return this.getClass();
		}
	}

	@Override
	public boolean isWrapperFor(Class<?> arg0) throws SQLException {
		return getConn().isWrapperFor(arg0);
	}

	@Override
	public <T> T unwrap(Class<T> arg0) throws SQLException {
		return getConn().unwrap(arg0);
	}

	@Override
	public void abort(Executor arg0) throws SQLException {
		getConn().abort(arg0);
	}

	@Override
	public void clearWarnings() throws SQLException {
		getConn().clearWarnings();
	}

	@Override
	public void close() throws SQLException {
		this.using = false;
		this.dataSource.endConn();
	}

	@Override
	public void commit() throws SQLException {
		getConn().commit();
	}

	@Override
	public Array createArrayOf(String arg0, Object[] arg1) throws SQLException {
		return getConn().createArrayOf(arg0, arg1);
	}

	@Override
	public Blob createBlob() throws SQLException {
		return getConn().createBlob();
	}

	@Override
	public Clob createClob() throws SQLException {
		return getConn().createClob();
	}

	@Override
	public NClob createNClob() throws SQLException {
		return getConn().createNClob();
	}

	@Override
	public SQLXML createSQLXML() throws SQLException {
		return getConn().createSQLXML();
	}

	@Override
	public Statement createStatement() throws SQLException {
		return getConn().createStatement();
	}

	@Override
	public Statement createStatement(int arg0, int arg1) throws SQLException {
		return getConn().createStatement(arg0, arg1);
	}

	@Override
	public Statement createStatement(int arg0, int arg1, int arg2) throws SQLException {
		return getConn().createStatement(arg0, arg1, arg2);
	}

	@Override
	public Struct createStruct(String arg0, Object[] arg1) throws SQLException {
		return getConn().createStruct(arg0, arg1);
	}

	@Override
	public boolean getAutoCommit() throws SQLException {
		return getConn().getAutoCommit();
	}

	@Override
	public String getCatalog() throws SQLException {
		return getConn().getCatalog();
	}

	@Override
	public Properties getClientInfo() throws SQLException {
		return getConn().getClientInfo();
	}

	@Override
	public String getClientInfo(String arg0) throws SQLException {
		return getConn().getClientInfo(arg0);
	}

	@Override
	public int getHoldability() throws SQLException {
		return getConn().getHoldability();
	}

	@Override
	public DatabaseMetaData getMetaData() throws SQLException {
		return getConn().getMetaData();
	}

	@Override
	public int getNetworkTimeout() throws SQLException {
		return getConn().getNetworkTimeout();
	}

	@Override
	public String getSchema() throws SQLException {
		return getConn().getSchema();
	}

	@Override
	public int getTransactionIsolation() throws SQLException {
		return getConn().getTransactionIsolation();
	}

	@Override
	public Map<String, Class<?>> getTypeMap() throws SQLException {
		return getConn().getTypeMap();
	}

	@Override
	public SQLWarning getWarnings() throws SQLException {
		return getConn().getWarnings();
	}

	@Override
	public boolean isClosed() throws SQLException {
		return !this.using;
	}

	@Override
	public boolean isReadOnly() throws SQLException {
		return getConn().isReadOnly();
	}

	@Override
	public boolean isValid(int arg0) throws SQLException {
		return getConn().isValid(arg0);
	}

	@Override
	public String nativeSQL(String arg0) throws SQLException {
		return getConn().nativeSQL(arg0);
	}

	@Override
	public CallableStatement prepareCall(String arg0) throws SQLException {
		return getConn().prepareCall(arg0);
	}

	@Override
	public CallableStatement prepareCall(String arg0, int arg1, int arg2) throws SQLException {
		return getConn().prepareCall(arg0, arg1, arg2);
	}

	@Override
	public CallableStatement prepareCall(String arg0, int arg1, int arg2, int arg3) throws SQLException {
		return getConn().prepareCall(arg0, arg1, arg2, arg3);
	}

	@Override
	public PreparedStatement prepareStatement(String arg0) throws SQLException {
		return getConn().prepareStatement(arg0);
	}

	@Override
	public PreparedStatement prepareStatement(String arg0, int arg1) throws SQLException {
		return getConn().prepareStatement(arg0, arg1);
	}

	@Override
	public PreparedStatement prepareStatement(String arg0, int[] arg1) throws SQLException {
		return getConn().prepareStatement(arg0, arg1);
	}

	@Override
	public PreparedStatement prepareStatement(String arg0, String[] arg1) throws SQLException {
		return getConn().prepareStatement(arg0, arg1);
	}

	@Override
	public PreparedStatement prepareStatement(String arg0, int arg1, int arg2) throws SQLException {
		return getConn().prepareStatement(arg0, arg1, arg2);
	}

	@Override
	public PreparedStatement prepareStatement(String arg0, int arg1, int arg2, int arg3) throws SQLException {
		return getConn().prepareStatement(arg0, arg1, arg2, arg3);
	}

	@Override
	public void releaseSavepoint(Savepoint arg0) throws SQLException {
		getConn().releaseSavepoint(arg0);
	}

	@Override
	public void rollback() throws SQLException {
		getConn().rollback();
	}

	@Override
	public void rollback(Savepoint arg0) throws SQLException {
		getConn().rollback(arg0);
	}

	@Override
	public void setAutoCommit(boolean arg0) throws SQLException {
		try
		{
			getConn().setAutoCommit(arg0);
		}
		catch (SQLException ex)
		{
			this.endConn();
			throw ex;
		}
	}

	@Override
	public void setCatalog(String arg0) throws SQLException {
		getConn().setCatalog(arg0);
	}

	@Override
	public void setClientInfo(Properties arg0) throws SQLClientInfoException {
		try
		{
			getConn().setClientInfo(arg0);
		}
		catch (SQLException ex)
		{
			throw new SQLClientInfoException(ex.getMessage(), null);
		}
	}

	@Override
	public void setClientInfo(String arg0, String arg1) throws SQLClientInfoException {
		try
		{
			getConn().setClientInfo(arg0, arg1);
		}
		catch (SQLException ex)
		{
			throw new SQLClientInfoException(ex.getMessage(), null);
		}
	}

	@Override
	public void setHoldability(int arg0) throws SQLException {
		getConn().setHoldability(arg0);
	}

	@Override
	public void setNetworkTimeout(Executor arg0, int arg1) throws SQLException {
		getConn().setNetworkTimeout(arg0, arg1);
	}

	@Override
	public void setReadOnly(boolean arg0) throws SQLException {
		getConn().setReadOnly(arg0);
	}

	@Override
	public Savepoint setSavepoint() throws SQLException {
		return getConn().setSavepoint();
	}

	@Override
	public Savepoint setSavepoint(String arg0) throws SQLException {
		return getConn().setSavepoint(arg0);
	}

	@Override
	public void setSchema(String arg0) throws SQLException {
		getConn().setSchema(arg0);
	}

	@Override
	public void setTransactionIsolation(int arg0) throws SQLException {
		getConn().setTransactionIsolation(arg0);
	}

	@Override
	public void setTypeMap(Map<String, Class<?>> arg0) throws SQLException {
		getConn().setTypeMap(arg0);
	}
}
