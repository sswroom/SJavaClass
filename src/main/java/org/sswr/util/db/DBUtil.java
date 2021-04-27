package org.sswr.util.db;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKBWriter;
import org.sswr.util.data.DataTools;
import org.sswr.util.data.FieldComparator;
import org.sswr.util.data.FieldGetter;
import org.sswr.util.data.FieldSetter;
import org.sswr.util.data.ReflectTools;
import org.sswr.util.data.StringUtil;
import org.sswr.util.io.LogLevel;
import org.sswr.util.io.LogTool;

public class DBUtil {
	public enum DBType
	{
		DT_UNKNOWN,
		DT_MSSQL,
		DT_MYSQL,
		DT_SQLITE,
		DT_ACCESS,
		DT_ORACLE
	}

	private static DBUpdateHandler updateHandler = null;
	private static LogTool sqlLogger = new LogTool();

	public static void setUpdateHandler(DBUpdateHandler updateHandler)
	{
		DBUtil.updateHandler = updateHandler;
	}

	public static void setSqlLogger(LogTool sqlLogger)
	{
		if (sqlLogger != null)
		{
			DBUtil.sqlLogger = sqlLogger;
		}
	}

	public static DBType connGetDBType(Connection conn)
	{
		String clsName = conn.getClass().getName();
		if (clsName.startsWith("com.microsoft.sqlserver.jdbc.SQLServerConnection"))
		{
			return DBType.DT_MSSQL;
		}
		else if (clsName.startsWith("com.mysql.cj.jdbc.ConnectionImpl"))
		{
			return DBType.DT_MYSQL;
		}
		else if (clsName.startsWith("net.ucanaccess.jdbc.UcanaccessConnection"))
		{
			return DBType.DT_ACCESS;
		}
		else
		{
			sqlLogger.logMessage("DB class = "+clsName, LogLevel.ERROR);
			return DBType.DT_UNKNOWN;
		}
	}

	private static String getFieldDefColName(Field field)
	{
		StringBuilder sb = new StringBuilder();
		String name = field.getName();
		char c;
		int i = 0;
		int j = name.length();
		while (i < j)
		{
			c = name.charAt(i);
			if (c >= 'A' && c <= 'Z')
			{
				sb.append('_');
				sb.append((char)(c + 32));
			}
			else
			{
				sb.append(c);
			}
			i++;
		}
		return sb.toString();
	}

	private static DBColumnInfo parseField2ColInfo(Field field, List<String> joinFields)
	{
		EnumType enumType;
		String colName;
		boolean isId;
		boolean isTransient;
		boolean isJoin;
		JoinColumn joinCol;
		GeneratedValue genVal;
		Annotation anns[];
		int i;
		int j;
		anns = field.getAnnotations();
		enumType = EnumType.ORDINAL;
		isId = false;
		isTransient = false;
		isJoin = false;
		joinCol = null;
		genVal = null;
		colName = getFieldDefColName(field);
		if (field.getName().startsWith("$SWITCH_TABLE$"))
		{
			isTransient = true;
		}
		else if (field.getName().startsWith("this$"))
		{
			isTransient = true;
		}
		else
		{
			
			i = 0;
			j = anns.length;
			while (i < j)
			{
				Class<? extends Annotation> annType = anns[i].annotationType();
				if (annType.equals(Transient.class))
				{
					isTransient = true;
				}
				else if (annType.equals(Id.class))
				{
					isId = true;
				}
				else if (annType.equals(Column.class))
				{
					Column col = (Column)anns[i];
					colName = col.name();
				}
				else if (annType.equals(Enumerated.class))
				{
					Enumerated en = (Enumerated)anns[i];
					enumType = en.value();
				}
				else if (annType.equals(JoinTable.class))
				{
					isJoin = true;
				}
				else if (annType.equals(OneToMany.class))
				{
					isJoin = true;
				}
				else if (annType.equals(ElementCollection.class))
				{
					isJoin = true;
				}
				else if (annType.equals(ManyToMany.class))
				{
					isJoin = true;
				}
				else if (annType.equals(JoinColumn.class))
				{
					joinCol = (JoinColumn)anns[i];
					colName = joinCol.name();
					if (joinFields != null)
					{
						joinFields.add(field.getName());
					}
				}
				else if (annType.equals(GeneratedValue.class))
				{
					genVal = (GeneratedValue)anns[i];
				}
				else if (annType.getSimpleName().equals("Formula"))
				{
					isTransient = true;
				}
				i++;
			}
		}

		if (isJoin)
		{
			if (joinFields != null)
			{
				joinFields.add(field.getName());
			}
			return null;
		}
		else if (!isTransient)
		{
			DBColumnInfo col = new DBColumnInfo();
			col.field = field;
			col.isId = isId;
			col.colName = colName;
			col.enumType = enumType;
			col.joinCol = joinCol;
			if (genVal != null)
			{
				col.genType = genVal.strategy();
			}
			else
			{
				col.genType = GenerationType.AUTO;
			}
			try
			{
				col.setter = new FieldSetter(field);
				col.getter = new FieldGetter<>(field);
				return col;
			}
			catch (IllegalArgumentException ex)
			{
				sqlLogger.logException(ex);
				return null;
			}
		}
		else
		{
			return null;
		}
	}

	private static Table parseClassTable(Class<?> cls)
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

	private static String uncol(String name)
	{
		if (name.startsWith("[") && name.endsWith("]"))
		{
			return name.substring(1, name.length() - 1);
		}
		if (name.startsWith("`") && name.endsWith("`"))
		{
			return name.substring(1, name.length() - 1);
		}
		return name;
	}

	private static String getTableName(Table table, DBType dbType)
	{
		StringBuilder sb = new StringBuilder();
		String catalog = uncol(table.catalog());
		String schema = uncol(table.schema());
		String tableName = uncol(table.name());
		if (dbType == DBType.DT_MYSQL)
		{
			if (!catalog.equals(""))
			{
				sb.append(dbCol(dbType, catalog));
				sb.append('.');
			}
			sb.append(dbCol(dbType, tableName));
		}
		else
		{
			if (!catalog.equals(""))
			{
				sb.append(dbCol(dbType, catalog));
				sb.append('.');
			}
			if (!schema.equals(""))
			{
				sb.append(dbCol(dbType, schema));
				sb.append('.');
			}
			sb.append(dbCol(dbType, tableName));
		}
		return sb.toString();
	}

	private static Map<String, DBColumnInfo> dbCols2Map(Iterable<DBColumnInfo> cols)
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
				parseDBCols(col.field.getType(), innerAllCols, innerIdCols, null);
				if (innerIdCols.size() == 1)
				{
					colsMap.put(col.field.getName()+"."+innerIdCols.get(0).field.getName(), col);
				}
			}
		}
		return colsMap;
	}

	/**
	* @param cls class to parse
	* @param allCols return all columns info
	* @param idCols return all id columns info
	* @param joinFields return fields which are joined with other tables, null = not returns
	*/
	public static void parseDBCols(Class<?> cls, List<DBColumnInfo> allCols, List<DBColumnInfo> idCols, List<String> joinFields)
	{
		Field fields[] = cls.getDeclaredFields();
		DBColumnInfo col;

		int i = 0;
		int j = fields.length;
		while (i < j)
		{
			col = parseField2ColInfo(fields[i], joinFields);
			if (col != null)
			{
				allCols.add(col);
				if (col.isId)
				{
					idCols.add(col);
				}
			}
			i++;
		}
	}

	public static PageStatus appendSelect(StringBuilder sb, List<DBColumnInfo> allCols, Table tableAnn, DBType dbType, int dataOfst, int dataCnt)
	{
		PageStatus status;
		if (dataOfst == 0 && dataCnt == 0)
		{
			status = PageStatus.SUCC;
		}
		else
		{
			status = PageStatus.NO_PAGE;
		}
		sb.append("select ");
		if (status == PageStatus.NO_PAGE && dbType == DBType.DT_ACCESS)
		{
			sb.append("TOP ");
			sb.append(dataOfst + dataCnt);
			status = PageStatus.NO_OFFSET;
		}
		int i = 0;
		int j = allCols.size();
		while (i < j)
		{
			if (i > 0)
			{
				sb.append(", ");
			}
			sb.append(dbCol(dbType, allCols.get(i).colName));
			i++;
		}
		sb.append(" from ");
		sb.append(getTableName(tableAnn, dbType));
		return status;
	}

	public static Integer fillColVals(ResultSet rs, Object o, List<DBColumnInfo> allCols) throws IllegalAccessException, InvocationTargetException, SQLException
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
					col.setter.set(o, fieldType.getEnumConstants()[rs.getInt(i + 1)]);
				}
				else
				{
					Object[] enums = fieldType.getEnumConstants();
					String enumName = rs.getString(i + 1);
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
					parseDBCols(fieldType, fieldCols, idCols, null);
					if (idCols.size() == 1)
					{
						idCols.get(0).setter.set(obj, rs.getInt(i + 1));
						col.setter.set(o, obj);
					}
					else
					{
						sqlLogger.logMessage("DBUtil.fillColVals join idCols mismatch", LogLevel.ERROR);
					}
				}
				catch (NoSuchMethodException ex)
				{
					sqlLogger.logException(ex);
				}
				catch (InstantiationException ex)
				{
					sqlLogger.logException(ex);
				}
			}
			else if (fieldType.equals(int.class))
			{
				int v = rs.getInt(i + 1);
				col.setter.set(o, v);
				if (col.isId)
				{
					id = v;
				}
			}
			else if (fieldType.equals(Integer.class))
			{
				Integer v = rs.getInt(i + 1);
				if (rs.wasNull())
				{
					v = null;
				}
				col.setter.set(o, v);
				if (col.isId)
				{
					id = v;
				}
			}
			else if (fieldType.equals(double.class))
			{
				double v = rs.getDouble(i + 1);
				col.setter.set(o, v);
			}
			else if (fieldType.equals(Double.class))
			{
				Double v = rs.getDouble(i + 1);
				if (rs.wasNull())
				{
					v = null;
				}
				col.setter.set(o, v);
			}
			else if (fieldType.equals(Timestamp.class))
			{
				col.setter.set(o, rs.getTimestamp(i + 1));
			}
			else if (fieldType.equals(String.class))
			{
				col.setter.set(o, rs.getString(i + 1));
			}
			else if (fieldType.equals(Geometry.class))
			{
				WKBReader reader = new WKBReader();
				try
				{
					col.setter.set(o, reader.read(rs.getBytes(i + 1)));
				}
				catch (ParseException ex)
				{
					sqlLogger.logException(ex);
				}
			}
			else
			{
//							col.setterMeth.invoke(obj, rs.getObject(i + 1));
				sqlLogger.logMessage("Unknown fieldType for "+col.field.getName()+" ("+fieldType.toString()+")", LogLevel.ERROR);
			}
			i++;
		}
		return id;
	}

	/*
	* @param joinFields return fields which are joined with other tables, null = not returns
	*/
	public static <T> Map<Integer, T> loadItemsById(Class<T> cls, Connection conn, Set<Integer> idSet, List<String> joinFields)
	{
		StringBuilder sb;
		Table tableAnn = parseClassTable(cls);
		if (tableAnn == null)
		{
			throw new IllegalArgumentException("Class annotation is not valid");
		}

		Constructor<T> constr = ReflectTools.getEmptyConstructor(cls);
		if (constr == null)
		{
			throw new IllegalArgumentException("No empty constructor found");
		}		

		ArrayList<DBColumnInfo> cols = new ArrayList<DBColumnInfo>();
		ArrayList<DBColumnInfo> idCols = new ArrayList<DBColumnInfo>();
		parseDBCols(cls, cols, idCols, joinFields);

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
		DBColumnInfo idCol = idCols.get(0);

		DBType dbType = connGetDBType(conn);
		sb = new StringBuilder();
		appendSelect(sb, cols, tableAnn, dbType, 0, 0);

		if (idSet != null && idSet.size() > 0)
		{
			Iterator<Integer> it = idSet.iterator();
			boolean hasFirst = false;
			sb.append(" where ");
			sb.append(idCol.colName);
			sb.append(" in (");
			while (it.hasNext())
			{
				if (hasFirst)
				{
					sb.append(", ");
				}
				hasFirst = true;
				sb.append(it.next().toString());
			}
			sb.append(")");
		}
		try
		{
			sqlLogger.logMessage(sb.toString(), LogLevel.COMMAND);

			PreparedStatement stmt = conn.prepareStatement(sb.toString());
			ResultSet rs = stmt.executeQuery();
			HashMap<Integer, T> retMap = new HashMap<Integer, T>();
			while (rs.next())
			{
				try
				{
					T obj = constr.newInstance(new Object[0]);
					Integer id = fillColVals(rs, obj, cols);

					if (id != null)
					{
						retMap.put(id, obj);
					}
				}
				catch (InvocationTargetException ex)
				{
					sqlLogger.logException(ex);
				}
				catch (InstantiationException ex)
				{
					sqlLogger.logException(ex);
				}
				catch (IllegalAccessException ex)
				{
					sqlLogger.logException(ex);
				}
			}
			rs.close();
			return retMap;
		}
		catch (SQLException ex)
		{
			sqlLogger.logException(ex);
			return null;
		}
	}	

	/*
	* @param joinFields return fields which are joined with other tables, null = not returns
	*/
	public static <T> Map<Integer, T> loadItems(Class<T> cls, Connection conn, QueryConditions<T> conditions, List<String> joinFields)
	{
		return loadItemsIClass(cls, null, conn, conditions, joinFields);
	}	

	/*
	* @param joinFields return fields which are joined with other tables, null = not returns
	*/
	public static <T> Map<Integer, T> loadItemsIClass(Class<T> cls, Object parent, Connection conn, QueryConditions<T> conditions, List<String> joinFields)
	{
		StringBuilder sb;
		Table tableAnn = parseClassTable(cls);
		if (tableAnn == null)
		{
			throw new IllegalArgumentException("Class annotation is not valid");
		}

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
				sqlLogger.logException(ex);
				throw new IllegalArgumentException("No suitable constructor found");
			}
		}
		if (constr == null)
		{
			throw new IllegalArgumentException("No suitable constructor found");
		}

		ArrayList<DBColumnInfo> cols = new ArrayList<DBColumnInfo>();
		ArrayList<DBColumnInfo> idCols = new ArrayList<DBColumnInfo>();
		parseDBCols(cls, cols, idCols, joinFields);

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

		DBType dbType = connGetDBType(conn);
		sb = new StringBuilder();
		appendSelect(sb, cols, tableAnn, dbType, 0, 0);

		if (conditions != null)
		{
			Map<String, DBColumnInfo> colsMap = dbCols2Map(cols);
			sb.append(" where ");
			sb.append(conditions.toWhereClause(colsMap, dbType));
		}
		try
		{
			sqlLogger.logMessage(sb.toString(), LogLevel.COMMAND);

			PreparedStatement stmt = conn.prepareStatement(sb.toString());
			ResultSet rs = stmt.executeQuery();
			HashMap<Integer, T> retMap = new HashMap<Integer, T>();
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
					}
					Integer id = fillColVals(rs, obj, cols);

					if (id != null)
					{
						retMap.put(id, obj);
					}
				}
				catch (InvocationTargetException ex)
				{
					sqlLogger.logException(ex);
				}
				catch (InstantiationException ex)
				{
					sqlLogger.logException(ex);
				}
				catch (IllegalAccessException ex)
				{
					sqlLogger.logException(ex);
				}
			}
			rs.close();
			return retMap;
		}
		catch (SQLException ex)
		{
			sqlLogger.logException(ex);
			return null;
		}
	}	


	/*
	* @param joinFields return fields which are joined with other tables, null = not returns
	*/
	public static <T> List<T> loadItemsAsList(Class<T> cls, Object parent, Connection conn, QueryConditions<T> conditions, List<String> joinFields, String sortString, int dataOfst, int dataCnt)
	{
		StringBuilder sb;
		Table tableAnn = parseClassTable(cls);
		if (tableAnn == null)
		{
			throw new IllegalArgumentException("Class annotation is not valid");
		}

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
				sqlLogger.logException(ex);
				throw new IllegalArgumentException("No suitable constructor found");
			}
		}
		if (constr == null)
		{
			throw new IllegalArgumentException("No suitable constructor found");
		}
		FieldComparator<T> fieldComp;
		if (sortString == null)
		{
			fieldComp = null;
		}
		else
		{
			try
			{
				fieldComp = new FieldComparator<T>(cls, sortString);
			}
			catch (NoSuchFieldException ex)
			{
				sqlLogger.logException(ex);
				throw new IllegalArgumentException("sortString is not valid ("+sortString+")");
			}
		}

		ArrayList<DBColumnInfo> cols = new ArrayList<DBColumnInfo>();
		ArrayList<DBColumnInfo> idCols = new ArrayList<DBColumnInfo>();
		parseDBCols(cls, cols, idCols, joinFields);

		if (cols.size() == 0)
		{
			throw new IllegalArgumentException("No selectable column found");
		}

		DBType dbType = connGetDBType(conn);
		Map<String, DBColumnInfo> colsMap = dbCols2Map(cols);
		sb = new StringBuilder();
		PageStatus status = appendSelect(sb, cols, tableAnn, dbType, dataOfst, dataCnt);

		if (conditions != null)
		{
			sb.append(" where ");
			sb.append(conditions.toWhereClause(colsMap, dbType));
		}
		if (fieldComp != null)
		{
			sb.append(" order by ");
			sb.append(fieldComp.toOrderClause(colsMap, dbType));
		}
		if ((dataOfst != 0 || dataCnt != 0) && status != PageStatus.SUCC)
		{
			if (dbType == DBType.DT_MYSQL)
			{
				sb.append(" LIMIT ");
				sb.append(dataOfst);
				sb.append(", ");
				sb.append(dataCnt);
				status = PageStatus.SUCC;
			}
			else if (dbType == DBType.DT_MSSQL)
			{
				if (fieldComp != null)
				{
					sb.append(" offset ");
					sb.append(dataOfst);
					sb.append(" row fetch next ");
					sb.append(dataCnt);
					sb.append(" row only");
					status = PageStatus.SUCC;
				}
			}
		}
		try
		{
			sqlLogger.logMessage(sb.toString(), LogLevel.COMMAND);

			PreparedStatement stmt = conn.prepareStatement(sb.toString());
			ResultSet rs = stmt.executeQuery();
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

			}

			while (rs.next())
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
						fillColVals(rs, obj, cols);
						retList.add(obj);
					}
					catch (InvocationTargetException ex)
					{
						sqlLogger.logException(ex);
					}
					catch (InstantiationException ex)
					{
						sqlLogger.logException(ex);
					}
					catch (IllegalAccessException ex)
					{
						sqlLogger.logException(ex);
					}
				}
			}
			rs.close();
			return retList;
		}
		catch (SQLException ex)
		{
			sqlLogger.logException(ex);
			return null;
		}
	}	

	/*
	* @param joinFields return fields which are joined with other tables, null = not returns
	*/
	public static <T, K> T loadItem(Class<T> cls, Connection conn, K id, List<String> joinFields)
	{
		StringBuilder sb;
		DBType dbType = connGetDBType(conn);
		Table tableAnn = parseClassTable(cls);
		if (tableAnn == null)
		{
			throw new IllegalArgumentException("Class annotation is not valid");
		}

		Constructor<T> constr;
		try
		{
			constr = cls.getConstructor(new Class<?>[0]);
		}
		catch (NoSuchMethodException ex)
		{
			throw new IllegalArgumentException("No empty constructor found");
		}


		ArrayList<DBColumnInfo> cols = new ArrayList<DBColumnInfo>();
		ArrayList<DBColumnInfo> idCols = new ArrayList<DBColumnInfo>();
		parseDBCols(cls, cols, idCols, joinFields);

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
		DBColumnInfo idCol = idCols.get(0);

		sb = new StringBuilder();
		appendSelect(sb, cols, tableAnn, dbType, 0, 0);

		sb.append(" where ");
		sb.append(idCol.colName);
		sb.append(" = ");
		sb.append(dbVal(dbType, idCol, id));
		try
		{
			sqlLogger.logMessage(sb.toString(), LogLevel.COMMAND);
			PreparedStatement stmt = conn.prepareStatement(sb.toString());
			ResultSet rs = stmt.executeQuery();
			T ret = null;
			if (rs.next())
			{
				try
				{
					ret = constr.newInstance(new Object[0]);
					fillColVals(rs, ret, cols);
				}
				catch (InvocationTargetException ex)
				{
					sqlLogger.logException(ex);
				}
				catch (InstantiationException ex)
				{
					sqlLogger.logException(ex);
				}
				catch (IllegalAccessException ex)
				{
					sqlLogger.logException(ex);
				}
			}
			rs.close();
			return ret;
		}
		catch (SQLException ex)
		{
			sqlLogger.logException(ex);
			return null;
		}
	}

	public static <T> boolean loadJoinItems(Connection conn, Iterable<T> items, String fieldName) throws NoSuchFieldException
	{
		Iterator<T> it = items.iterator();
		if (!it.hasNext())
		{
			return true;
		}
		T obj = it.next();
		Class<?> tClass = obj.getClass();
		ArrayList<DBColumnInfo> cols = new ArrayList<DBColumnInfo>();
		ArrayList<DBColumnInfo> idCols = new ArrayList<DBColumnInfo>();
		parseDBCols(tClass, cols, idCols, null);
		Field field = tClass.getDeclaredField(fieldName);
		Annotation[] anns = field.getAnnotations();
		Annotation ann;
		if (idCols.size() == 0 || idCols.size() > 1)
		{
			throw new IllegalArgumentException("Entity is not single id");
		}
		FieldGetter<T> getter = new FieldGetter<T>(field);

		ElementCollection elemColl = null;
		CollectionTable collTab = null;
		Column column = null;
		JoinColumn joinColumn = null;
		ManyToOne manyToOne = null;
		ManyToMany manyToMany = null;
		OneToMany oneToMany = null;
		JoinTable joinTable = null;

		int i = 0;
		int j = anns.length;
		while (i < j)
		{
			ann = anns[i];
			Class<? extends Annotation> annType = ann.annotationType();
			if (annType.equals(ElementCollection.class))
			{
				elemColl = (ElementCollection)ann;
			}
			else if (annType.equals(CollectionTable.class))
			{
				collTab = (CollectionTable)ann;
			}
			else if (annType.equals(Column.class))
			{
				column = (Column)ann;
			}
			else if (annType.equals(ManyToOne.class))
			{
				manyToOne = (ManyToOne)ann;
			}
			else if (annType.equals(JoinColumn.class))
			{
				joinColumn = (JoinColumn)ann;
			}
			else if (annType.equals(ManyToMany.class))
			{
				manyToMany = (ManyToMany)ann;
			}
			else if (annType.equals(JoinTable.class))
			{
				joinTable = (JoinTable)ann;
			}
			else if (annType.equals(OneToMany.class))
			{
				oneToMany = (OneToMany)ann;
			}
			i++;
		}
		Class<?> fieldType = field.getType();
		if (elemColl != null && collTab != null && column != null)
		{
			if (fieldType.equals(Set.class))
			{
				Map<Integer, T> objMap = DataTools.createIntMap(items, idCols.get(0).field.getName(), null);
				StringBuilder sb = new StringBuilder();
				String idName = collTab.joinColumns()[0].name();
				sb.append("select ");
				sb.append(idName);
				sb.append(", ");
				sb.append(column.name());
				sb.append(" from ");
				if (!collTab.catalog().equals(""))
				{
					sb.append(collTab.catalog());
					sb.append('.');
				}
				if (!collTab.schema().equals(""))
				{
					sb.append(collTab.schema());
					sb.append('.');
				}
				sb.append(collTab.name());
				sb.append(" where ");
				sb.append(idName);
				sb.append(" in (");
				sb.append(DataTools.intJoin(objMap.keySet(), ", "));
				sb.append(")");
				try
				{
					sqlLogger.logMessage(sb.toString(), LogLevel.COMMAND);
					PreparedStatement stmt;
					ResultSet rs;
					stmt = conn.prepareStatement(sb.toString());
					rs = stmt.executeQuery();
					try
					{
						while (rs.next())
						{
							obj = objMap.get(rs.getInt(1));
							if (obj != null)
							{
								@SuppressWarnings("unchecked")
								Set<Integer> set = (Set<Integer>)getter.get(obj);
								set.add(rs.getInt(2));
							}
						}
					}
					catch(InvocationTargetException ex)
					{
						sqlLogger.logException(ex);
					}
					catch (IllegalAccessException ex)
					{
						sqlLogger.logException(ex);
					}
					rs.close();
					stmt.close();
				}
				catch (SQLException ex)
				{
					sqlLogger.logException(ex);
				}
			}
			else
			{
				throw new IllegalArgumentException("Unsupported annotations: "+field.toString());
			}
			return false;
		}
		else if (joinColumn != null && manyToOne != null)
		{
			List<DBColumnInfo> joinAllCols = new ArrayList<DBColumnInfo>();
			List<DBColumnInfo> joinIdCols = new ArrayList<DBColumnInfo>();
			parseDBCols(fieldType, joinAllCols, joinIdCols, null);
			if (joinIdCols.size() == 1)
			{
				Set<Integer> idSet = DataTools.createIntSet(items, fieldName+"."+joinIdCols.get(0).colName, null);
				Map<Integer, ?> innerItems = DBUtil.loadItemsById(fieldType, conn, idSet, null);
				DBColumnInfo idCol = joinIdCols.get(0);
				FieldSetter setter = new FieldSetter(field);
				it = items.iterator();
				while (it.hasNext())
				{
					obj = it.next();
					try
					{
						Object joinObj = getter.get(obj);
						
						joinObj = innerItems.get((Integer)idCol.getter.get(joinObj));
						if (joinObj != null)
						{
							setter.set(obj, joinObj);
						}
					}
					catch (IllegalAccessException ex)
					{
						sqlLogger.logException(ex);
					}
					catch (InvocationTargetException ex)
					{
						sqlLogger.logException(ex);
					}
				}
				return true;
			}
			else
			{
				throw new IllegalArgumentException("Field type not supported: "+fieldType.toString());
			}
		}
		else if (oneToMany != null && fieldType.equals(Set.class))
		{
			Class<?> targetClass = oneToMany.targetEntity();			
			if (targetClass.equals(void.class))
			{
				Type t = field.getGenericType();
				if (t instanceof ParameterizedType)
				{
					ParameterizedType pt = (ParameterizedType)t;
					targetClass = (Class<?>)pt.getActualTypeArguments()[0];
				}
			}
			if (targetClass.equals(void.class))
			{
				throw new IllegalArgumentException("Field type not supported: "+fieldType.toString());
			}
			if (idCols.size() != 1)
			{
				throw new IllegalArgumentException("Field type not supported: "+fieldType.toString());
			}
			DBColumnInfo idCol = idCols.get(0);
			Field targetField = targetClass.getDeclaredField(oneToMany.mappedBy());
			if (!targetField.getType().equals(tClass))
			{
				throw new IllegalArgumentException("Field type not supported: "+fieldType.toString());
			}
			Map<Integer, T> tMap;
			if (idCol.field.getType().equals(int.class))
			{
				tMap = DataTools.createValueMap(int.class, items, idCol.field.getName(), null);
			}
			else
			{
				tMap = DataTools.createValueMap(Integer.class, items, idCol.field.getName(), null);
			}
			FieldGetter<Object> targetGetter = new FieldGetter<>(targetField);
			@SuppressWarnings("unchecked")
			Class<Object> tmpClass = (Class<Object>)targetClass;
			Map<Integer, ?> targetMap = loadItems(tmpClass, conn, new QueryConditions<>(tmpClass).intIn(oneToMany.mappedBy()+"."+idCol.field.getName(), DataTools.createIntSet(items, idCol.field.getName(), null)), null);
			Iterator<?> itTarget = targetMap.values().iterator();
			try
			{
				while (itTarget.hasNext())
				{
					Object targetObj = itTarget.next();
					T tObj = tMap.get(idCol.getter.get(targetGetter.get(targetObj)));
					@SuppressWarnings("unchecked")
					Set<Object> targetSet = ((Set<Object>)getter.get(tObj));
					targetSet.add(targetObj);
				}
				return true;
			}
			catch (InvocationTargetException ex)
			{
				sqlLogger.logException(ex);
				throw new IllegalArgumentException("Field type not supported: "+fieldType.toString());
			}
			catch (IllegalAccessException ex)
			{
				sqlLogger.logException(ex);
				throw new IllegalArgumentException("Field type not supported: "+fieldType.toString());
			}
		}
		else if (manyToMany != null && joinTable == null && (fieldType.equals(Set.class) || fieldType.equals(List.class)))
		{
			Class<?> targetClass = manyToMany.targetEntity();
			FieldSetter setter = new FieldSetter(field);
			if (targetClass.equals(void.class))
			{
				Type t = field.getGenericType();
				if (t instanceof ParameterizedType)
				{
					ParameterizedType pt = (ParameterizedType)t;
					targetClass = (Class<?>)pt.getActualTypeArguments()[0];
				}
			}
			if (targetClass.equals(void.class))
			{
				throw new IllegalArgumentException("Field type not supported: "+fieldType.toString());
			}
			if (idCols.size() != 1)
			{
				throw new IllegalArgumentException("Field type not supported: "+fieldType.toString());
			}

			Field mapField = targetClass.getDeclaredField(manyToMany.mappedBy());
			Annotation mapAnns[] = mapField.getAnnotations();
			ManyToMany mapManyToMany = null;
			JoinTable mapJoinTable = null;
			i = 0;
			j = mapAnns.length;
			while (i < j)
			{
				ann = mapAnns[i];
				Class<? extends Annotation> annType = ann.annotationType();
				if (annType.equals(ManyToMany.class))
				{
					mapManyToMany = (ManyToMany)ann;
				}
				else if (annType.equals(JoinTable.class))
				{
					mapJoinTable = (JoinTable)ann;
				}
				i++;
			}
			if (mapManyToMany == null || mapJoinTable == null)
			{
				throw new IllegalArgumentException("Joined Field is not supported: "+targetClass.toString()+"."+mapField.getName());
			}

			List<DBColumnInfo> targetCols = new ArrayList<DBColumnInfo>();
			List<DBColumnInfo> targetIdCols = new ArrayList<DBColumnInfo>();
			parseDBCols(targetClass, targetCols, targetIdCols, null);
			if (targetIdCols.size() != 1)
			{
				throw new IllegalArgumentException("Joined Field is not supported: "+targetClass.toString()+"."+mapField.getName());
			}

			Set<Integer> idSet = DataTools.createIntSet(items, idCols.get(0).field.getName(), null);
			StringBuilder sb = new StringBuilder();
			String idName = mapJoinTable.inverseJoinColumns()[0].name();
			sb.append("select ");
			sb.append(idName);
			sb.append(", ");
			sb.append(mapJoinTable.joinColumns()[0].name());
			sb.append(" from ");
			if (!mapJoinTable.catalog().equals(""))
			{
				sb.append(mapJoinTable.catalog());
				sb.append('.');
			}
			if (!mapJoinTable.schema().equals(""))
			{
				sb.append(mapJoinTable.schema());
				sb.append('.');
			}
			sb.append(mapJoinTable.name());
			sb.append(" where ");
			sb.append(idName);
			sb.append(" in (");
			sb.append(DataTools.intJoin(idSet, ", "));
			sb.append(")");
			List<JoinItem> joinItemList = new ArrayList<JoinItem>();
			JoinItem joinItem;
			try
			{
				sqlLogger.logMessage(sb.toString(), LogLevel.COMMAND);
				PreparedStatement stmt;
				ResultSet rs;
				stmt = conn.prepareStatement(sb.toString());
				rs = stmt.executeQuery();
				while (rs.next())
				{
					joinItem = new JoinItem();
					joinItem.id = rs.getInt(1);
					joinItem.joinId = rs.getInt(2);
					joinItemList.add(joinItem);
				}
				rs.close();
				stmt.close();
			}
			catch (SQLException ex)
			{
				sqlLogger.logException(ex);
				throw new IllegalArgumentException("Error in joining table");
			}
			Map<Integer, ? extends Object> targetMap = loadItemsById(targetClass, conn, DataTools.createIntSet(joinItemList, "joinId", null), null);
			Map<Integer, T> itemMap = DataTools.createIntMap(items, idCols.get(0).field.getName(), null);
			it = items.iterator();
			try
			{
				if (fieldType.equals(Set.class))
				{
					while (it.hasNext())
					{
						setter.set(it.next(), new HashSet<>());
					}
					i = 0;
					j = joinItemList.size();
					while (i < j)
					{
						joinItem = joinItemList.get(i);
						obj = itemMap.get(joinItem.id);
						@SuppressWarnings("unchecked")
						Set<Object> itemSet = (Set<Object>)getter.get(obj);
						Object o = targetMap.get(joinItem.joinId);
						if (o != null)
						{
							itemSet.add(o);
						}
						i++;
					}
					return true;
				}
				else if (fieldType.equals(List.class))
				{
					while (it.hasNext())
					{
						setter.set(it.next(), new ArrayList<>());
					}
					i = 0;
					j = joinItemList.size();
					while (i < j)
					{
						joinItem = joinItemList.get(i);
						obj = itemMap.get(joinItem.id);
						@SuppressWarnings("unchecked")
						List<Object> itemSet = (List<Object>)getter.get(obj);
						Object o = targetMap.get(joinItem.joinId);
						if (o != null)
						{
							itemSet.add(o);
						}
						i++;
					}
					return true;
				}
			}
			catch (IllegalAccessException ex)
			{
				sqlLogger.logException(ex);
			}
			catch (InvocationTargetException ex)
			{
				sqlLogger.logException(ex);
			}

			throw new IllegalArgumentException("Field type not supported: "+fieldType.toString());
		}
		else if (manyToMany != null && joinTable != null && (fieldType.equals(Set.class) || fieldType.equals(List.class)))
		{
			Class<?> targetClass = manyToMany.targetEntity();
			FieldSetter setter = new FieldSetter(field);
			if (targetClass.equals(void.class))
			{
				Type t = field.getGenericType();
				if (t instanceof ParameterizedType)
				{
					ParameterizedType pt = (ParameterizedType)t;
					targetClass = (Class<?>)pt.getActualTypeArguments()[0];
				}
			}
			if (targetClass.equals(void.class))
			{
				throw new IllegalArgumentException("Field type not supported: "+fieldType.toString());
			}
			if (idCols.size() != 1)
			{
				throw new IllegalArgumentException("Field type not supported: "+fieldType.toString());
			}

			List<DBColumnInfo> targetCols = new ArrayList<DBColumnInfo>();
			List<DBColumnInfo> targetIdCols = new ArrayList<DBColumnInfo>();
			parseDBCols(targetClass, targetCols, targetIdCols, null);
			if (targetIdCols.size() != 1)
			{
				throw new IllegalArgumentException("Joined Class is not supported: "+targetClass.toString());
			}

			Set<Integer> idSet = DataTools.createIntSet(items, idCols.get(0).field.getName(), null);
			StringBuilder sb = new StringBuilder();
			String idName = joinTable.joinColumns()[0].name();
			sb.append("select ");
			sb.append(idName);
			sb.append(", ");
			sb.append(joinTable.inverseJoinColumns()[0].name());
			sb.append(" from ");
			if (!joinTable.catalog().equals(""))
			{
				sb.append(joinTable.catalog());
				sb.append('.');
			}
			if (!joinTable.schema().equals(""))
			{
				sb.append(joinTable.schema());
				sb.append('.');
			}
			sb.append(joinTable.name());
			sb.append(" where ");
			sb.append(idName);
			sb.append(" in (");
			sb.append(DataTools.intJoin(idSet, ", "));
			sb.append(")");
			List<JoinItem> joinItemList = new ArrayList<JoinItem>();
			JoinItem joinItem;
			try
			{
				sqlLogger.logMessage(sb.toString(), LogLevel.COMMAND);
				PreparedStatement stmt;
				ResultSet rs;
				stmt = conn.prepareStatement(sb.toString());
				rs = stmt.executeQuery();
				while (rs.next())
				{
					joinItem = new JoinItem();
					joinItem.id = rs.getInt(1);
					joinItem.joinId = rs.getInt(2);
					joinItemList.add(joinItem);
				}
				rs.close();
				stmt.close();
			}
			catch (SQLException ex)
			{
				sqlLogger.logException(ex);
				throw new IllegalArgumentException("Error in joining table");
			}
			Map<Integer, ? extends Object> targetMap = loadItemsById(targetClass, conn, DataTools.createIntSet(joinItemList, "joinId", null), null);
			Map<Integer, T> itemMap = DataTools.createIntMap(items, idCols.get(0).field.getName(), null);
			it = items.iterator();
			try
			{
				if (fieldType.equals(Set.class))
				{
					while (it.hasNext())
					{
						setter.set(it.next(), new HashSet<>());
					}
					i = 0;
					j = joinItemList.size();
					while (i < j)
					{
						joinItem = joinItemList.get(i);
						obj = itemMap.get(joinItem.id);
						@SuppressWarnings("unchecked")
						Set<Object> itemSet = (Set<Object>)getter.get(obj);
						Object o = targetMap.get(joinItem.joinId);
						if (o != null)
						{
							itemSet.add(o);
						}
						i++;
					}
					return true;
				}
				else if (fieldType.equals(List.class))
				{
					while (it.hasNext())
					{
						setter.set(it.next(), new ArrayList<>());
					}
					i = 0;
					j = joinItemList.size();
					while (i < j)
					{
						joinItem = joinItemList.get(i);
						obj = itemMap.get(joinItem.id);
						@SuppressWarnings("unchecked")
						List<Object> itemSet = (List<Object>)getter.get(obj);
						Object o = targetMap.get(joinItem.joinId);
						if (o != null)
						{
							itemSet.add(o);
						}
						i++;
					}
					return true;
				}
			}
			catch (IllegalAccessException ex)
			{
				sqlLogger.logException(ex);
			}
			catch (InvocationTargetException ex)
			{
				sqlLogger.logException(ex);
			}

			throw new IllegalArgumentException("Field type not supported: "+fieldType.toString());
		}
		else
		{
			throw new IllegalArgumentException("Field type not supported: "+fieldType.toString());
		}
	}

	public static Charset mssqlCollationGetCharset(String coll)
	{
		/*
		SQL_Latin1_General_CP1_CI_AS

		latin1 makes the server treat strings using charset latin 1, basically ascii
		CP1 stands for Code Page 1252
		CI case insensitive comparisons so 'ABC' would equal 'abc'
		AS accent sensitive, so 'ü' does not equal 'u'
		*/
		if (coll.indexOf("_CP1_") >= 0)
		{
			return StandardCharsets.ISO_8859_1;
		}
		return StandardCharsets.UTF_8;
	}

	public static String dbStr(DBType dbType, String val)
	{
		if (dbType == DBType.DT_MYSQL)
		{
			val = val.replace("\\", "\\\\");
			val = val.replace("\'", "\\\'");
			val = val.replace("\"", "\\\"");
			val = val.replace("\r", "\\r");
			val = val.replace("\n", "\\n");
			val = val.replace("\t", "\\t");
			val = val.replace("\b", "\\b");
			val = val.replace("\u001a", "\\Z");
			return "'"+val+"'";
		}
		else if (dbType == DBType.DT_SQLITE)
		{
			val = val.replace("\\", "\\\\");
			val = val.replace("\'", "\\\'");
			val = val.replace("\"", "\\\"");
			val = val.replace("\r", "\\r");
			val = val.replace("\n", "\\n");
			val = val.replace("\t", "\\t");
			return "'"+val+"'";
		}
		else if (dbType == DBType.DT_MSSQL)
		{
			val = val.replace("\\", "\\\\");
			val = val.replace("\'", "\\\'");
			return "N'"+val+"'";
		}
		else if (dbType == DBType.DT_ACCESS)
		{
			val = val.replace("\'", "\'\'");
			return "'"+val+"'";
		}
		else
		{
			val = val.replace("\\", "\\\\");
			val = val.replace("\'", "\\\'");
			return "'"+val+"'";
		}
	}

	public static String dbTime(DBType dbType, Timestamp val)
	{
		if (val == null)
		{
			return "null";
		}
		if (dbType == DBType.DT_ACCESS)
		{
			return "#"+val.toString()+"#";
		}
		else if (dbType == DBType.DT_MSSQL || dbType == DBType.DT_SQLITE)
		{
			return "'"+new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(val)+"'";
		}
		else if (dbType == DBType.DT_ORACLE)
		{
			return "TIMESTAMP '"+new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(val)+"'";
		}
		else
		{
			return "'"+new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(val)+"'";
		}
	}

	public static String dbBin(DBType dbType, byte val[])
	{
		if (val == null)
		{
			return "null";
		}
		if (dbType == DBType.DT_MYSQL || dbType == DBType.DT_SQLITE)
		{
			return "x'"+StringUtil.toHex(val)+"'";
		}
		else if (dbType == DBType.DT_MSSQL)
		{
			return "0x"+StringUtil.toHex(val);
		}
		else
		{
			return "''";
		}
	}

	public static String dbVal(DBType dbType, DBColumnInfo col, Object val)
	{
		if (val == null)
		{
			return "null";
		}
		Class<?> fieldType = col.field.getType();
		if (fieldType.equals(Timestamp.class) && val.getClass().equals(Timestamp.class))
		{
			return dbTime(dbType, (Timestamp)val);
		}
		else if (fieldType.equals(Integer.class) || fieldType.equals(int.class))
		{
			if (val instanceof Integer)
			{
				return ((Integer)val).toString();
			}
		}
		else if (fieldType.equals(Double.class) || fieldType.equals(double.class))
		{
			if (val instanceof Double)
			{
				return ((Double)val).toString();
			}
		}
		else if (fieldType.equals(Geometry.class))
		{
			if (val instanceof Geometry)
			{
				WKBWriter writer = new WKBWriter();
				return dbBin(dbType, writer.write((Geometry)val));
			}
		}
		if (fieldType.equals(String.class) && val.getClass().equals(String.class))
		{
			return dbStr(dbType, (String)val);
		}
		else if (fieldType.isEnum() && val instanceof Enum)
		{
			if (col.enumType == EnumType.ORDINAL)
			{
				return ""+((Enum<?>)val).ordinal();
			}
			else
			{
				return dbStr(dbType, ((Enum<?>)val).name());
			}
		}
		else if (fieldType.equals(val.getClass()))
		{
			ArrayList<DBColumnInfo> allCols = new ArrayList<DBColumnInfo>();
			ArrayList<DBColumnInfo> idCols = new ArrayList<DBColumnInfo>();
			parseDBCols(fieldType, allCols, idCols, null);
			if (idCols.size() == 1)
			{
				col = idCols.get(0);
				try
				{
					return dbVal(dbType, col, col.getter.get(val));
				}
				catch (IllegalAccessException ex)
				{
					sqlLogger.logException(ex);
				}
				catch (InvocationTargetException ex)
				{
					sqlLogger.logException(ex);
				}
			}
		}

		sqlLogger.logMessage("DBUtil.dbVal: Unsupport field type: " + fieldType.toString() + ", Object type: "+val.getClass().toString(), LogLevel.ERR_DETAIL);
		return "?";
	}


	public static String dbCol(DBType dbType, String val)
	{
		if (dbType == DBType.DT_MYSQL)
		{
			return "`"+val+"`";
		}
		else if (dbType == DBType.DT_MSSQL || dbType == DBType.DT_ACCESS)
		{
			return "["+val+"]";
		}
		else
		{
			return val;
		}
	}

	public static int getLastIdentity32(Connection conn)
	{
		DBType dbType = connGetDBType(conn);
		if (dbType == DBType.DT_MYSQL || dbType == DBType.DT_MSSQL || dbType == DBType.DT_ACCESS)
		{
			int id = 0;
			try
			{
				PreparedStatement stmt = conn.prepareStatement("select @@identity");
				ResultSet rs = stmt.executeQuery();
				if (rs != null)
				{
					if (rs.next())
					{
						id = rs.getInt(1);
					}
					rs.close();
				}
			}
			catch (SQLException ex)
			{
				sqlLogger.logException(ex);
			}
			return id;
		}
		else
		{
			return 0;
		}
	}

	public static <T> boolean update(Connection conn, T oriObj, T newObj)
	{
		Class<?> targetClass;
		if (oriObj != null)
		{
			targetClass = oriObj.getClass();
		}
		else if (newObj != null)
		{
			targetClass = newObj.getClass();
		}
		else
		{
			return false;
		}
		Table tableAnn = parseClassTable(targetClass);
		if (tableAnn == null)
		{
			throw new IllegalArgumentException("Class annotation is not valid");
		}

		List<DBColumnInfo> targetCols = new ArrayList<DBColumnInfo>();
		List<DBColumnInfo> targetIdCols = new ArrayList<DBColumnInfo>();
		DBColumnInfo col;
		parseDBCols(targetClass, targetCols, targetIdCols, null);
		StringBuilder sb;
		DBType dbType = connGetDBType(conn);
		int i;
		int j;
		try
		{
			if (newObj == null)
			{
				if (targetIdCols.size() == 0)
				{
					return false;
				}
				sb = new StringBuilder();
				sb.append("delete from ");
				sb.append(getTableName(tableAnn, dbType));
				sb.append(" where ");
				i = 0;
				j = targetIdCols.size();
				while (i < j)
				{
					col = targetIdCols.get(i);
					if (i > 0)
					{
						sb.append(" and ");
					}
					sb.append(dbCol(dbType, col.colName));
					sb.append(" = ");
					sb.append(dbVal(dbType, col, col.getter.get(oriObj)));
					i++;
				}
				boolean ret = executeNonQuery(conn, sb.toString());
				if (ret && updateHandler != null)
				{
					updateHandler.dbUpdated(oriObj, newObj);
				}
				return ret;
			}
			else if (oriObj == null)
			{
				sb = new StringBuilder();
				sb.append("insert into ");
				sb.append(getTableName(tableAnn, dbType));
				sb.append(" (");
				boolean found = false;
				i = 0;
				j = targetCols.size();
				while (i < j)
				{
					col = targetCols.get(i);
					if (col.genType != GenerationType.IDENTITY)
					{
						if (found)
						{
							sb.append(", ");
						}
						found = true;
						sb.append(dbCol(dbType, col.colName));
					}
					i++;
				}
				sb.append(") values (");
				found = false;
				i = 0;
				while (i < j)
				{
					col = targetCols.get(i);
					if (col.genType != GenerationType.IDENTITY)
					{
						if (found)
						{
							sb.append(", ");
						}
						found = true;
						sb.append(dbVal(dbType, col, col.getter.get(newObj)));
					}
					i++;
				}
				sb.append(")");
				found = executeNonQuery(conn, sb.toString());
				if (found)
				{
					if (targetIdCols.size() == 1)
					{
						col = targetIdCols.get(0);
						if (col.genType == GenerationType.IDENTITY)
						{
							col.setter.set(newObj, getLastIdentity32(conn));
						}
					}
					if (updateHandler != null)
					{
						updateHandler.dbUpdated(oriObj, newObj);
					}
				}
				return found;
			}
			else
			{
				Object o1;
				Object o2;
				boolean found = false;
				sb = new StringBuilder();
				sb.append("update ");
				sb.append(getTableName(tableAnn, dbType));
				sb.append(" set ");
				i = 0;
				j = targetCols.size();
				while (i < j)
				{
					col = targetCols.get(i);
					o1 = col.getter.get(oriObj);
					o2 = col.getter.get(newObj);
					if (!Objects.equals(o1, o2))
					{
						if (found)
						{
							sb.append(", ");
						}
						sb.append(dbCol(dbType, col.colName));
						sb.append(" = ");
						sb.append(dbVal(dbType, col, o2));
						found = true;
					}
					i++;
				}
				if (!found)
				{
					return true;
				}
				sb.append(" where ");
				i = 0;
				j = targetIdCols.size();
				while (i < j)
				{
					col = targetIdCols.get(i);
					if (i > 0)
					{
						sb.append(" and ");
					}
					sb.append(dbCol(dbType, col.colName));
					sb.append(" = ");
					sb.append(dbVal(dbType, col, col.getter.get(oriObj)));
					i++;
				}
				if (executeNonQuery(conn, sb.toString()))
				{
					if (updateHandler != null)
					{
						updateHandler.dbUpdated(oriObj, newObj);
					}
					i = 0;
					j = targetCols.size();
					while (i < j)
					{
						col = targetCols.get(i);
						o1 = col.getter.get(oriObj);
						o2 = col.getter.get(newObj);
						if (!Objects.equals(o1, o2))
						{
							col.setter.set(oriObj, o2);
						}
						i++;
					}
					return true;
				}
				return false;
			}
		}
		catch (IllegalAccessException ex)
		{
			sqlLogger.logException(ex);
			return false;
		}
		catch (InvocationTargetException ex)
		{
			sqlLogger.logException(ex);
			return false;
		}
	}

	public static boolean executeNonQuery(Connection conn, String sql)
	{
		try
		{
			sqlLogger.logMessage(sql, LogLevel.COMMAND);
			PreparedStatement stmt;
			stmt = conn.prepareStatement(sql);
			int rowCnt = stmt.executeUpdate();
			return rowCnt >= 0;
		}
		catch (SQLException ex)
		{
			sqlLogger.logException(ex);
			return false;
		}
	}

	public static Connection openAccessFile(String accessPath)
	{
		String jdbcStr;
		if (File.separatorChar == '\\')
		{
			jdbcStr = "jdbc:ucanaccess://"+accessPath.replace("\\", "/");
		}
		else
		{
			jdbcStr = "jdbc:ucanaccess://"+accessPath;
		}
		try
		{
			return DriverManager.getConnection(jdbcStr);
		}
		catch (SQLException ex)
		{
			sqlLogger.logException(ex);
			return null;
		}
	}
}
