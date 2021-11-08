package org.sswr.util.db;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Table;

import org.locationtech.jts.geom.Geometry;
import org.sswr.util.data.DateTimeUtil;
import org.sswr.util.data.ReflectTools;
import org.sswr.util.io.LogLevel;
import org.sswr.util.io.LogTool;
import org.sswr.util.math.Vector2D;

public abstract class DBConnection
{
	protected LogTool logger;
	
	protected DBConnection(LogTool logger)
	{
		this.logger = logger;
	}

	protected static Table parseClassTable(Class<?> cls)
	{
		boolean entityFound = false;
		Table tableAnn = null;
		int i;
		int j;
		Annotation anns[] = cls.getAnnotations();
		i = 0;
		j = anns.length;
		while (i < j)
		{
			Class<? extends Annotation> annType = anns[i].annotationType();
			if (annType.equals(Entity.class))
			{
				entityFound = true;
			}
			else if (annType.equals(Table.class))
			{
				tableAnn = (Table)anns[i];
			}
			i++;
		}

		if (!entityFound || tableAnn == null)
		{
			return null;
		}
		return tableAnn;
	}

	protected static Map<String, DBColumnInfo> dbCols2Map(Iterable<DBColumnInfo> cols)
	{
		Map<String, DBColumnInfo> colsMap = new HashMap<String, DBColumnInfo>();
		Iterator<DBColumnInfo> it = cols.iterator();
		while (it.hasNext())
		{
			DBColumnInfo col = it.next();
			colsMap.put(col.field.getName(), col);
			if (col.joinCol != null)
			{
				ArrayList<DBColumnInfo> innerAllCols = new ArrayList<DBColumnInfo>();
				ArrayList<DBColumnInfo> innerIdCols = new ArrayList<DBColumnInfo>();
				DBUtil.parseDBCols(col.field.getType(), innerAllCols, innerIdCols, null);
				if (innerIdCols.size() == 1)
				{
					colsMap.put(col.field.getName()+"."+innerIdCols.get(0).field.getName(), col);
				}
			}
		}
		return colsMap;
	}

	protected Integer fillColVals(DBReader r, Object o, List<DBColumnInfo> allCols) throws IllegalAccessException, InvocationTargetException
	{
		Class<?> fieldType;
		Integer id = null;
		DBColumnInfo col;
		int i = 0;
		int j = allCols.size();
		while (i < j)
		{
			col = allCols.get(i);
			fieldType = col.field.getType();
			if (fieldType.isEnum())
			{
				if (col.enumType == EnumType.ORDINAL)
				{
					col.setter.set(o, fieldType.getEnumConstants()[r.getInt32(i)]);
				}
				else
				{
					Object[] enums = fieldType.getEnumConstants();
					String enumName = r.getString(i);
					if (enumName == null)
					{
						col.setter.set(o, null);
					}
					else
					{
						Object e = null;
						int k = enums.length;
						while (k-- > 0)
						{
							if (enums[k].toString().equals(enumName))
							{
								e = enums[k];
								break;
							}
						}
						col.setter.set(o, e);
					}
				}
			}
			else if (col.joinCol != null)
			{
				try
				{
					Constructor<?> constr = fieldType.getConstructor(new Class<?>[0]);
					Object obj = constr.newInstance(new Object[0]);
					List<DBColumnInfo> fieldCols = new ArrayList<DBColumnInfo>();
					List<DBColumnInfo> idCols = new ArrayList<DBColumnInfo>();
					DBUtil.parseDBCols(fieldType, fieldCols, idCols, null);
					if (idCols.size() == 1)
					{
						idCols.get(0).setter.set(obj, r.getInt32(i));
						col.setter.set(o, obj);
					}
					else
					{
						if (this.logger != null) this.logger.logMessage("fillColVals join idCols mismatch", LogLevel.ERROR);
					}
				}
				catch (NoSuchMethodException ex)
				{
					if (this.logger != null) this.logger.logException(ex);
				}
				catch (InstantiationException ex)
				{
					if (this.logger != null) this.logger.logException(ex);
				}
			}
			else if (fieldType.equals(int.class))
			{
				int v = r.getInt32(i);
				col.setter.set(o, v);
				if (col.isId)
				{
					id = v;
				}
			}
			else if (fieldType.equals(Integer.class))
			{
				Integer v = r.getInt32(i);
				if (r.isNull(i))
				{
					v = null;
				}
				col.setter.set(o, v);
				if (col.isId)
				{
					id = v;
				}
			}
			else if (fieldType.equals(Long.class))
			{
				Long v = r.getInt64(i);
				if (r.isNull(i))
				{
					v = null;
				}
				col.setter.set(o, v);
			}
			else if (fieldType.equals(double.class))
			{
				double v = r.getDbl(i);
				col.setter.set(o, v);
			}
			else if (fieldType.equals(Double.class))
			{
				Double v = r.getDbl(i);
				if (r.isNull(i))
				{
					v = null;
				}
				col.setter.set(o, v);
			}
			else if (fieldType.equals(Timestamp.class))
			{
				col.setter.set(o, DateTimeUtil.toTimestamp(r.getDate(i)));
			}
			else if (fieldType.equals(String.class))
			{
				col.setter.set(o, r.getString(i));
			}
			else if (fieldType.equals(Geometry.class))
			{
				Vector2D vec = r.getVector(i);
				Geometry geom = null;
				if (vec != null)
				{
					geom = vec.toGeometry();
				}
				col.setter.set(o, geom);
			}
			else
			{
//							col.setterMeth.invoke(obj, rs.getObject(i + 1));
				if (this.logger != null) this.logger.logMessage("Unknown fieldType for "+col.field.getName()+" ("+fieldType.toString()+")", LogLevel.ERROR);
			}
			i++;
		}
		return id;
	}

	protected <T> Constructor<T> getConstructor(Class<T> cls, Object parent)
	{
		Constructor<T> constr;
		if (parent == null)
		{
			constr = ReflectTools.getEmptyConstructor(cls);
		}
		else
		{
			try
			{
				constr = cls.getDeclaredConstructor(new Class<?>[]{parent.getClass()});
			}
			catch (NoSuchMethodException ex)
			{
				if (this.logger != null) this.logger.logException(ex);
				throw new IllegalArgumentException("No suitable constructor found");
			}
		}
		if (constr == null)
		{
			throw new IllegalArgumentException("No suitable constructor found");
		}
		return constr;
	}

	protected <T> List<T> readAsList(DBReader r, PageStatus status, int dataOfst, int dataCnt, Object parent, Constructor<T> constr, List<DBColumnInfo> cols, List<QueryConditions<T>.Condition> clientConditions)
	{
		ArrayList<T> retList = new ArrayList<T>();
		if (status == PageStatus.SUCC)
		{
			dataOfst = 0;
			dataCnt = Integer.MAX_VALUE;
		}
		else if (status == PageStatus.NO_OFFSET)
		{
			dataCnt = Integer.MAX_VALUE;
		}
		else if (status == PageStatus.NO_PAGE)
		{
			if (dataCnt == 0)
			{
				dataCnt = Integer.MAX_VALUE;
			}
		}

		while (r.readNext())
		{
			if (dataOfst > 0)
			{
				dataOfst--;
			}
			else if (dataCnt <= 0)
			{
				break;
			}
			else
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
					}
					this.fillColVals(r, obj, cols);
					if (QueryConditions.objectValid(obj, clientConditions))
					{
						retList.add(obj);
						dataCnt--;
					}
				}
				catch (InvocationTargetException ex)
				{
					if (this.logger != null) this.logger.logException(ex);
				}
				catch (InstantiationException ex)
				{
					if (this.logger != null) this.logger.logException(ex);
				}
				catch (IllegalAccessException ex)
				{
					if (this.logger != null) this.logger.logException(ex);
				}
			}
		}
		return retList;
	}

	public abstract <T> List<T> loadItemsAsList(Class<T> cls, Object parent, QueryConditions<T> conditions, List<String> joinFields, String sortString, int dataOfst, int dataCnt);
}
