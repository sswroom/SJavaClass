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
import java.util.Set;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Table;

import org.locationtech.jts.geom.Geometry;
import org.sswr.util.data.DateTimeUtil;
import org.sswr.util.data.ReflectTools;
import org.sswr.util.io.LogLevel;
import org.sswr.util.io.LogTool;
import org.sswr.util.math.geometry.Vector2D;

public abstract class ReadingConnection
{
	protected LogTool logger;
	
	protected ReadingConnection(@Nullable LogTool logger)
	{
		this.logger = logger;
	}

	@Nullable
	protected static Table parseClassTable(@Nonnull Class<?> cls)
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

	@Nonnull
	protected static Map<String, DBColumnInfo> dbCols2Map(@Nonnull Iterable<DBColumnInfo> cols)
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

	@Nullable
	protected Integer fillColVals(@Nonnull DBReader r, @Nonnull Object o, @Nonnull List<DBColumnInfo> allCols) throws IllegalAccessException, InvocationTargetException
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
				col.setter.set(o, r.getGeometry(i));
			}
			else if (fieldType.equals(Vector2D.class))
			{
				col.setter.set(o, r.getVector(i));
			}
			else if (fieldType.equals(byte[].class))
			{
				col.setter.set(o, r.getBinary(i));
			}
			else
			{
//							col.setterMeth.invoke(obj, rs.getObject(i + 1));
				if (this.logger != null) this.logger.logMessage("ReadingConnection: Unknown fieldType for "+col.field.getName()+" ("+fieldType.toString()+")", LogLevel.ERROR);
			}
			i++;
		}
		return id;
	}

	@Nonnull
	protected <T> Constructor<T> getConstructor(@Nonnull Class<T> cls, @Nullable Object parent)
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

	@Nonnull
	protected <T> List<T> readAsList(@Nonnull DBReader r, @Nonnull PageStatus status, int dataOfst, int dataCnt, @Nullable Object parent, @Nonnull Constructor<T> constr, @Nonnull List<DBColumnInfo> cols, @Nonnull List<QueryConditions<T>.Condition> clientConditions)
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

	@Nonnull
	protected <T> Map<Integer, T> readAsMap(@Nonnull DBReader r, @Nullable Object parent, @Nonnull Constructor<T> constr, @Nonnull List<DBColumnInfo> cols, @Nonnull List<QueryConditions<T>.Condition> clientConditions)
	{
		Map<Integer, T> retMap = new HashMap<Integer, T>();
		while (r.readNext())
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
				Integer id = this.fillColVals(r, obj, cols);
				if (id != null && QueryConditions.objectValid(obj, clientConditions))
				{
					retMap.put(id, obj);
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
		return retMap;
	}

	@Nullable
	public <T> Map<Integer, T> loadItemsById(@Nonnull Class<T> cls, @Nonnull Set<Integer> idSet, @Nullable List<String> joinFields)
	{
		ArrayList<DBColumnInfo> cols = new ArrayList<DBColumnInfo>();
		ArrayList<DBColumnInfo> idCols = new ArrayList<DBColumnInfo>();
		DBUtil.parseDBCols(cls, cols, idCols, joinFields);
		if (idCols.size() > 1)
		{
			throw new IllegalArgumentException("Multiple id column found");
		}
		if (idCols.size() == 0)
		{
			throw new IllegalArgumentException("No Id column found");
		}
		try
		{
			QueryConditions<T> queryConditions = new QueryConditions<T>(cls).intIn(idCols.get(0).setter.getField().getName(), idSet);
			return this.loadItemsIClass(cls, null, queryConditions, joinFields);
		}
		catch (NoSuchFieldException ex)
		{
			if (this.logger != null) this.logger.logException(ex);
			return null;
		}
	}

	@Nullable 
	public abstract <T> List<T> loadItemsAsList(@Nonnull Class<T> cls, @Nullable Object parent, @Nullable QueryConditions<T> conditions, @Nullable List<String> joinFields, @Nullable String sortString, int dataOfst, int dataCnt);
	@Nullable
	public abstract <T> Map<Integer, T> loadItemsIClass(@Nonnull Class<T> cls, @Nullable Object parent, @Nullable QueryConditions<T> conditions, @Nullable List<String> joinFields);
}
