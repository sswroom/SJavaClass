package org.sswr.util.db;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.sswr.util.db.DBUtil.DBType;

public class DBIterator<T> implements Iterator<T>
{
	private T nextItem;
	private ResultSet rs;
	private Constructor<T> constr;
	private DBType dbType;
	private ArrayList<DBColumnInfo> cols;
	private Object parent;
	private List<QueryConditions<T>.Condition> clientConditions;

	DBIterator(ResultSet rs, Object parent, Constructor<T> constr, DBType dbType, ArrayList<DBColumnInfo> cols, List<QueryConditions<T>.Condition> clientConditions)
	{
		this.rs = rs;
		this.constr = constr;
		this.dbType = dbType;
		this.parent = parent;
		this.cols = cols;
		this.clientConditions = clientConditions;
		this.readNext();
	}

	private void readNext()
	{
		this.nextItem = null;
		if (this.rs != null)
		{
			try
			{
				while (rs.next())
				{
					try
					{
						T obj;
						if (parent == null)
						{
							obj = constr.newInstance(new Object[0]);
						}
						else
						{
							obj = constr.newInstance(parent);
						};
						DBUtil.fillColVals(dbType, this.rs, obj, cols);
						if (QueryConditions.objectValid(obj, clientConditions))
						{
							this.nextItem = obj;
							return;
						}
					}
					catch (InvocationTargetException ex)
					{
						DBUtil.sqlLogger.logException(ex);
					}
					catch (InstantiationException ex)
					{
						DBUtil.sqlLogger.logException(ex);
					}
					catch (IllegalAccessException ex)
					{
						DBUtil.sqlLogger.logException(ex);
					}
				}
			}
			catch (SQLException ex)
			{
				DBUtil.sqlLogger.logException(ex);
			}
			this.close();
		}
	}

	@Override
	public boolean hasNext() {
		return this.nextItem != null;
	}

	@Override
	public T next() {
		if (this.nextItem == null)
			return null;
		T ret = this.nextItem;
		this.readNext();
		return ret;
	}

	public void close()
	{
		if (this.rs != null)
		{
			try
			{
				rs.close();
			}
			catch (SQLException ex)
			{
	
			}
			this.rs = null;
		}
	}
}
