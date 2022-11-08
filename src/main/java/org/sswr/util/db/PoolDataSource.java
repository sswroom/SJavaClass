package org.sswr.util.db;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.sswr.util.basic.ThreadEvent;

public class PoolDataSource implements DataSource
{
	private static final boolean debug = true;
	private String url;
	private String username;
	private String password;
	private PrintWriter logWriter;
	private int loginTimeout;
	private int recvTimeoutMS;
	private PoolConnection []conns;
	private StackTraceElement [][]connCaller;
	private ThreadEvent evt;

	public PoolDataSource(int maxCnt)
	{
		this.conns = new PoolConnection[maxCnt];
		this.evt = new ThreadEvent(true);
		this.loginTimeout = 10;
		if (debug)
		{
			this.connCaller = new StackTraceElement[maxCnt][];
		}
	}

	public void setDriverClassName(String className)
	{
		try
		{
			Class.forName(className);
		}
		catch (ClassNotFoundException ex)
		{
			ex.printStackTrace();
		}
	}

	public void setUrl(String url)
	{
		this.url = url;
	}

	public void setUsername(String username)
	{
		this.username = username;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}

	public void setRecvTimeout(int timeoutMS)
	{
		this.recvTimeoutMS = timeoutMS;
	}

	void endConn()
	{
		this.evt.set();
	}

	public void close()
	{
		int i = this.conns.length;
		while (i-- > 0)
		{
			this.conns[i].endConn();
		}
	}

	@Override
	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
		return null;
	}

	@Override
	public boolean isWrapperFor(Class<?> arg0) throws SQLException {
		return false;
	}

	@Override
	public <T> T unwrap(Class<T> arg0) throws SQLException {
		return null;
	}

	@Override
	public Connection getConnection() throws SQLException {
		synchronized (this)
		{
			long t = System.currentTimeMillis();
			while (true)
			{
				int i = 0;
				int j = this.conns.length;
				while (i < j)
				{
					if (this.conns[i] == null)
					{
						this.conns[i] = new PoolConnection(this, this.url, this.username, this.password);
						this.conns[i].setRecvTimeout(this.recvTimeoutMS);
						if (debug)
						{
							this.connCaller[i] = Thread.currentThread().getStackTrace();
						}
						this.conns[i].open();
						return this.conns[i];
					}
					else if (!this.conns[i].isUsing())
					{
						if (debug)
						{
							this.connCaller[i] = Thread.currentThread().getStackTrace();
						}
						this.conns[i].open();
						return this.conns[i];
					}
					i++;
				}
				if (System.currentTimeMillis() - t >= this.loginTimeout * 1000)
				{
					break;
				}
				this.evt.waitEvent(1000);
			}
		}
		if (debug)
		{
			StringBuilder sb = new StringBuilder();
			sb.append("Connection busy: ");
			int i = 0;
			int j = this.connCaller[0].length;
			while (i < j)
			{
				sb.append("\r\n"+this.connCaller[0][i].toString());
				i++;
			}
			throw new SQLException(sb.toString());
		}
		else
		{
			throw new SQLException("Connection busy");
		}
	}

	@Override
	public Connection getConnection(String arg0, String arg1) throws SQLException {
		return getConnection();
	}

	@Override
	public PrintWriter getLogWriter() throws SQLException {
		return this.logWriter;
	}

	@Override
	public int getLoginTimeout() throws SQLException {
		return this.loginTimeout;
	}

	@Override
	public void setLogWriter(PrintWriter logWriter) throws SQLException {
		this.logWriter = logWriter;
	}

	@Override
	public void setLoginTimeout(int seconds) throws SQLException {
		this.loginTimeout = seconds;
		DriverManager.setLoginTimeout(seconds);
	}
}
