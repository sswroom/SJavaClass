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

import org.locationtech.jts.geom.Geometry;
import org.sswr.util.data.ArtificialQuickSort;
import org.sswr.util.data.DataTools;
import org.sswr.util.data.DateTimeUtil;
import org.sswr.util.data.FieldComparator;
import org.sswr.util.data.ObjectFieldGetter;
import org.sswr.util.data.QueryConditions;
import org.sswr.util.data.ReflectTools;
import org.sswr.util.data.cond.BooleanObject;
import org.sswr.util.io.LogLevel;
import org.sswr.util.io.LogTool;
import org.sswr.util.io.ParsedObject;
import org.sswr.util.io.ParserType;
import org.sswr.util.math.geometry.Vector2D;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Table;

public abstract class ReadingDB extends ParsedObject {
	protected LogTool logger;
	public ReadingDB(@Nonnull String sourceName)
	{
		super(sourceName);
		this.logger = null;
	}

	public void dispose()
	{

	}

	public @Nullable List<String> querySchemaNames()
	{
		return null;
	}

	public abstract @Nullable List<String> queryTableNames(@Nullable String schemaName);
	public abstract @Nullable DBReader queryTableData(@Nullable String schemaName, @Nonnull String tableName, @Nullable List<String> colNames, int dataOfst, int maxCnt, @Nullable String ordering, @Nullable QueryConditions condition);
	public abstract @Nullable TableDef getTableDef(@Nullable String schemaName, @Nonnull String tableName);
	public abstract void closeReader(@Nonnull DBReader r);
	public abstract @Nullable String getLastErrorMsg();
	public abstract void reconnect();
	public byte getTzQhr()
	{
		return DateTimeUtil.getLocalTZQhr();
	}

	public @Nullable List<String> getDatabaseNames()
	{
		return null;
	}

	public boolean changeDatabase(@Nonnull String databaseName)
	{
		return false;
	}

	public @Nullable String getCurrDBName()
	{
		return null;
	}

	public @Nonnull ParserType getParserType()
	{
		return ParserType.ReadingDB;
	}

	public boolean isFullConn()
	{
		return false;
	}
	
	public boolean isDBTool()
	{
		return false;
	}

	public static boolean isDBObj(@Nonnull ParsedObject pobj)
	{
		ParserType pt = pobj.getParserType();
		if (pt == ParserType.MapLayer || pt == ParserType.ReadingDB)
		{
			return true;
		}
		return false;
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
				double v = r.getDblOrNAN(i);
				col.setter.set(o, v);
			}
			else if (fieldType.equals(Double.class))
			{
				Double v = r.getDblOrNAN(i);
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
				if (this.logger != null) this.logger.logMessage("ReadingDB: Unknown fieldType for "+col.field.getName()+" ("+fieldType.toString()+")", LogLevel.ERROR);
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
	protected <T> List<T> readAsList(@Nonnull DBReader r, @Nonnull PageStatus status, int dataOfst, int dataCnt, @Nullable Object parent, @Nonnull Constructor<T> constr, @Nonnull List<DBColumnInfo> cols, @Nonnull List<BooleanObject> clientConditions)
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
					ObjectFieldGetter<T> getter = new ObjectFieldGetter<T>(obj);
					this.fillColVals(r, obj, cols);
					if (QueryConditions.objectValid(getter, clientConditions))
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
	protected <T> Map<Integer, T> readAsMap(@Nonnull DBReader r, @Nullable Object parent, @Nonnull Constructor<T> constr, @Nonnull List<DBColumnInfo> cols, @Nonnull List<BooleanObject> clientConditions)
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
				ObjectFieldGetter<T> getter = new ObjectFieldGetter<T>(obj);
				if (id != null && QueryConditions.objectValid(getter, clientConditions))
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
		QueryConditions queryConditions = new QueryConditions().int32In(idCols.get(0).setter.getField().getName(), idSet);
		return this.loadItemsIClass(cls, null, queryConditions, joinFields);
	}

	@Nullable
	public <T> List<T> loadItemsAsList(@Nonnull Class<T> cls, @Nullable Object parent, @Nullable QueryConditions conditions, @Nullable List<String> joinFields, @Nullable String sortString, int dataOfst, int dataCnt)
	{
		Table tableAnn = parseClassTable(cls);
		if (tableAnn == null)
		{
			throw new IllegalArgumentException("Class annotation is not valid");
		}
		Constructor<T> constr = getConstructor(cls, parent);
		ArrayList<DBColumnInfo> cols = new ArrayList<DBColumnInfo>();
		ArrayList<DBColumnInfo> idCols = new ArrayList<DBColumnInfo>();
		DBUtil.parseDBCols(cls, cols, idCols, joinFields);
		DBReader r = this.queryTableData(tableAnn.schema(), tableAnn.name(), DataTools.createValueList(String.class, cols, "colName", null), 0, 0, null, conditions);
		if (r == null)
		{
			return null;
		}
		List<T> retList;
		List<BooleanObject> clientConditions;
		if (conditions == null)
		{
			clientConditions = List.of();
		}
		else
		{
			clientConditions = conditions.toList();
		}

		if (sortString != null)
		{
			FieldComparator<T> fieldComp;
			try
			{
				fieldComp = new FieldComparator<T>(cls, sortString);
			}
			catch (NoSuchFieldException ex)
			{
				if (this.logger != null) this.logger.logException(ex);
				throw new IllegalArgumentException("sortString is not valid ("+sortString+")");
			}
			retList = this.readAsList(r, PageStatus.NO_PAGE, 0, 0, parent, constr, cols, clientConditions);
			ArtificialQuickSort.sort(retList, fieldComp);
			if (dataOfst > 0)
			{
				if (dataOfst >= retList.size())
				{
					retList.clear();
				}
				else
				{
					ArrayList<T> remList = new ArrayList<T>();
					int i = 0;
					while (i < dataOfst)
					{
						remList.add(retList.get(i));
						i++;
					}
					retList.removeAll(remList);
				}
			}
			if (dataCnt > 0)
			{
				if (dataCnt < retList.size())
				{
					int i = retList.size();
					while (i-- > dataCnt)
					{
						retList.remove(i);
					}
				}
			}
		}
		else
		{
			retList = this.readAsList(r, PageStatus.NO_PAGE, dataOfst, dataCnt, parent, constr, cols, clientConditions);
		}
		r.close();
		return retList;
	}

	@Nullable
	public <T> Map<Integer, T> loadItemsIClass(@Nonnull Class<T> cls, @Nullable Object parent, @Nullable QueryConditions conditions, @Nullable List<String> joinFields)
	{
		Table tableAnn = parseClassTable(cls);
		if (tableAnn == null)
		{
			throw new IllegalArgumentException("Class annotation is not valid");
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
		DBReader r = this.queryTableData(tableAnn.schema(), tableAnn.name(), DataTools.createValueList(String.class, cols, "colName", null), 0, 0, null, conditions);
		if (r == null)
		{
			return null;
		}
		Map<Integer, T> retMap = this.readAsMap(r, parent, constr, cols, conditions == null?List.of():conditions.toList());
		r.close();
		return retMap;
	}
}
