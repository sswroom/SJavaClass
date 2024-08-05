package org.sswr.util.db;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKBWriter;
import org.sswr.util.data.DataTools;
import org.sswr.util.data.DateTimeUtil;
import org.sswr.util.data.FieldGetter;
import org.sswr.util.data.FieldSetter;
import org.sswr.util.data.GeometryUtil;
import org.sswr.util.data.MSGeography;
import org.sswr.util.data.ReflectTools;
import org.sswr.util.data.SharedInt;
import org.sswr.util.data.StringUtil;
import org.sswr.util.io.LogLevel;
import org.sswr.util.io.LogTool;

public class DBUtil {
	public enum DBType
	{
		Unknown,
		MSSQL,
		MySQL,
		SQLite,
		Access,
		Oracle,
		PostgreSQL,
		PostgreSQLESRI
	}

	public static final int MAX_SQL_ITEMS = 100;
	private static List<DBUpdateHandler> updateHandlers = null;
	static LogTool sqlLogger = new LogTool();

	public static void addUpdateHandler(DBUpdateHandler updateHandler)
	{
		if (DBUtil.updateHandlers == null)
		{
			DBUtil.updateHandlers = new ArrayList<DBUpdateHandler>();
		}
		DBUtil.updateHandlers.add(updateHandler);
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
		String clsName;
		if (conn instanceof PoolConnection)
		{
			clsName = ((PoolConnection)conn).getConnClass().getName();
		}
		else
		{
			clsName = conn.getClass().getName();
			if (clsName.equals("com.zaxxer.hikari.pool.HikariProxyConnection"))
			{
				clsName = conn.toString();
				int i = clsName.indexOf(" wrapping ");
				if (i >= 0)
				{
					clsName = clsName.substring(i + 10);
				}
				i = clsName.indexOf("@");
				if (i >= 0)
				{
					clsName = clsName.substring(0, i);
				}
			}
		}
		if (clsName.startsWith("com.microsoft.sqlserver.jdbc.SQLServerConnection"))
		{
			return DBType.MSSQL;
		}
		else if (clsName.startsWith("com.mysql.cj.jdbc.ConnectionImpl"))
		{
			return DBType.MySQL;
		}
		else if (clsName.startsWith("net.ucanaccess.jdbc.UcanaccessConnection"))
		{
			return DBType.Access;
		}
		else if (clsName.equals("org.postgresql.jdbc.PgConnection"))
		{
			return DBType.PostgreSQL;
		}
		else if (clsName.equals("com.zaxxer.hikari.pool.HikariProxyConnection"))
		{
			sqlLogger.logMessage("HikariProxyConnection: "+conn.toString(), LogLevel.ERROR);
			return DBType.Unknown;
		}
		else
		{
//			System.out.println(clsName);
			sqlLogger.logMessage("DB class = "+clsName, LogLevel.ERROR);
			return DBType.Unknown;
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
		else if (Modifier.isStatic(field.getModifiers()))
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

	public static TableInfo parseTableInfo(Class<?> cls)
	{
		TableInfo table = new TableInfo();
		table.tableAnn = parseClassTable(cls);
		if (table.tableAnn == null)
		{
			throw new IllegalArgumentException("Class annotation is not valid");
		}

		table.allCols = new ArrayList<DBColumnInfo>();
		table.idCols = new ArrayList<DBColumnInfo>();
		parseDBCols(cls, table.allCols, table.idCols, null);
		return table;
	}

	public static String uncol(String name)
	{
		if (name == null)
			return null;
		if (name.length() >= 2)
		{
			if (name.startsWith("[") && name.endsWith("]"))
			{
				return name.substring(1, name.length() - 1);
			}
			if (name.startsWith("`") && name.endsWith("`"))
			{
				return name.substring(1, name.length() - 1);
			}
			if (name.startsWith("\"") && name.endsWith("\""))
			{
				return name.substring(1, name.length() - 1);
			}
		}
		return name;
	}

	private static String getTableName(Table table, DBType dbType)
	{
		StringBuilder sb = new StringBuilder();
		String catalog = uncol(table.catalog());
		String schema = uncol(table.schema());
		String tableName = uncol(table.name());
		if (dbType == DBType.MySQL)
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
/*		Class<?> supercls = cls.getSuperclass();
		if (supercls != null && !supercls.equals(Object.class))
		{
			parseDBCols(supercls, allCols, idCols, joinFields);
		}*/
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
		if (status == PageStatus.NO_PAGE && dbType == DBType.Access)
		{
			sb.append("TOP ");
			sb.append(dataOfst + dataCnt);
			status = PageStatus.NO_OFFSET;
		}
		int i = 0;
		int j = allCols.size();
		DBColumnInfo col;
		while (i < j)
		{
			if (i > 0)
			{
				sb.append(", ");
			}
			col = allCols.get(i);
			if ((dbType == DBType.PostgreSQLESRI || (dbType == DBType.PostgreSQL && "sde".equals(tableAnn.schema()))) && col.field.getType().equals(Geometry.class))
			{
				sb.append("sde.st_asbinary("+dbCol(dbType, col.colName)+")");
			}
			else
			{
				sb.append(dbCol(dbType, col.colName));
			}
			i++;
		}
		sb.append(" from ");
		sb.append(getTableName(tableAnn, dbType));
		return status;
	}

	public static Integer fillColVals(DBType dbType, ResultSet rs, Object o, List<DBColumnInfo> allCols) throws IllegalAccessException, InvocationTargetException, SQLException
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
			try
			{
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
				else if (fieldType.equals(Long.class))
				{
					Long v = rs.getLong(i + 1);
					if (rs.wasNull())
					{
						v = null;
					}
					col.setter.set(o, v);
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
				else if (fieldType.equals(float.class))
				{
					float v = (float)rs.getDouble(i + 1);
					col.setter.set(o, v);
				}
				else if (fieldType.equals(Float.class))
				{
					Float v = (float)rs.getDouble(i + 1);
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
				else if (fieldType.equals(Date.class))
				{
					col.setter.set(o, rs.getDate(i + 1));
				}
				else if (fieldType.equals(Time.class))
				{
					col.setter.set(o, rs.getTime(i + 1));
				}
				else if (fieldType.equals(String.class))
				{
					col.setter.set(o, rs.getString(i + 1));
				}
				else if (fieldType.equals(Boolean.class))
				{
					Object dbO = rs.getObject(i + 1);
					Boolean v;
					if (rs.wasNull())
					{
						v = null;
					}
					else if (dbO instanceof String)
					{
						v = ((String)dbO).startsWith("t") || ((String)dbO).startsWith("T");
					}
					else if (dbO instanceof Integer)
					{
						v = ((Integer)dbO).intValue() != 0;
					}
					else if (dbO instanceof Boolean)
					{
						v = (Boolean)dbO;
					}
					else
					{
						System.out.println("DBUtil: unsupported Boolean type: "+dbO.getClass().toString());
						v = false;
					}
					col.setter.set(o, v);
				}
				else if (fieldType.equals(Geometry.class))
				{
					byte bytes[] = rs.getBytes(i + 1);
					if (bytes == null)
					{
						col.setter.set(o, null);
					}
					else if (dbType == DBType.MSSQL)
					{
						col.setter.set(o, GeometryUtil.fromVector2D(MSGeography.parseBinary(bytes)));
					}
					else if (dbType == DBType.PostgreSQL || dbType == DBType.PostgreSQLESRI)
					{
						bytes = StringUtil.hex2Bytes(new String(bytes));
						WKBReader reader = new WKBReader();
						try
						{
							col.setter.set(o, reader.read(bytes));
						}
						catch (ParseException ex)
						{
							sqlLogger.logException(ex);
						}
					}
					else
					{
						WKBReader reader = new WKBReader();
						try
						{
							col.setter.set(o, reader.read(bytes));
						}
						catch (ParseException ex)
						{
							sqlLogger.logException(ex);
						}
					}
				}
				else if (fieldType.equals(byte[].class))
				{
					col.setter.set(o, rs.getBytes(i + 1));
				}
				else
				{
	//							col.setterMeth.invoke(obj, rs.getObject(i + 1));
					sqlLogger.logMessage("DBUtil: Unknown fieldType for "+col.field.getName()+" ("+fieldType.toString()+")", LogLevel.ERROR);
				}
			}
			catch (SQLException ex)
			{
				sqlLogger.logMessage("Error in setting field: "+col.field.getName(), LogLevel.ERROR);
				throw ex;
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

		if (idSet != null && idSet.size() > 0 && idSet.size() <= MAX_SQL_ITEMS)
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
					Integer id = fillColVals(dbType, rs, obj, cols);

					if (id != null && idSet.contains(id))
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

	static class LoadDataSession<T>
	{
		StringBuilder sql;
		Constructor<T> constr;
		DBType dbType;
		ArrayList<DBColumnInfo> cols;
		ArrayList<DBColumnInfo> idCols;
		List<QueryConditions<T>.Condition> clientConditions;
	}

	private static <T> LoadDataSession<T> parseLoadSession(Class<T> cls, Object parent, Connection conn, QueryConditions<T> conditions, List<String> joinFields, boolean requireIdCol)
	{
		LoadDataSession<T> sess = new LoadDataSession<T>();
		Table tableAnn = parseClassTable(cls);
		if (tableAnn == null)
		{
			throw new IllegalArgumentException("Class annotation is not valid");
		}

		
		if (parent == null)
		{
			sess.constr = ReflectTools.getEmptyConstructor(cls);
		}
		else
		{
			try
			{
				sess.constr = cls.getDeclaredConstructor(new Class<?>[]{parent.getClass()});
			}
			catch (NoSuchMethodException ex)
			{
				sqlLogger.logException(ex);
				throw new IllegalArgumentException("No suitable constructor found");
			}
		}
		if (sess.constr == null)
		{
			throw new IllegalArgumentException("No suitable constructor found");
		}

		sess.cols = new ArrayList<DBColumnInfo>();
		sess.idCols = new ArrayList<DBColumnInfo>();
		parseDBCols(cls, sess.cols, sess.idCols, joinFields);

		if (sess.cols.size() == 0)
		{
			throw new IllegalArgumentException("No selectable column found");
		}
		if (requireIdCol)
		{
			if (sess.idCols.size() > 1)
			{
				throw new IllegalArgumentException("Multiple id column found");
			}
			if (sess.idCols.size() == 0)
			{
				throw new IllegalArgumentException("No Id column found");
			}
		}

		sess.dbType = connGetDBType(conn);
		sess.sql = new StringBuilder();
		appendSelect(sess.sql, sess.cols, tableAnn, sess.dbType, 0, 0);

		sess.clientConditions = new ArrayList<QueryConditions<T>.Condition>();
		if (conditions != null)
		{
			Map<String, DBColumnInfo> colsMap = dbCols2Map(sess.cols);
			String whereClause = conditions.toWhereClause(colsMap, sess.dbType, sess.clientConditions, MAX_SQL_ITEMS);
			if (!StringUtil.isNullOrEmpty(whereClause))
			{
				sess.sql.append(" where ");
				sess.sql.append(whereClause);
			}
		}
		return sess;
	}

	/*
	* @param joinFields return fields which are joined with other tables, null = not returns
	*/
	public static <T> Map<Integer, T> loadItemsIClass(Class<T> cls, Object parent, Connection conn, QueryConditions<T> conditions, List<String> joinFields)
	{
		LoadDataSession<T> sess = parseLoadSession(cls, parent, conn, conditions, joinFields, true);
		try
		{
			sqlLogger.logMessage(sess.sql.toString(), LogLevel.COMMAND);

			PreparedStatement stmt = conn.prepareStatement(sess.sql.toString());
			ResultSet rs = stmt.executeQuery();
			HashMap<Integer, T> retMap = new HashMap<Integer, T>();
			while (rs.next())
			{
				try
				{
					T obj;
					if (parent == null)
					{
						obj = sess.constr.newInstance(new Object[0]);
					}
					else
					{
						obj = sess.constr.newInstance(parent);
					}
					Integer id = fillColVals(sess.dbType, rs, obj, sess.cols);

					if (id != null && QueryConditions.objectValid(obj, sess.clientConditions))
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

	public static <T> DBIterator<T> loadData(Class<T> cls, Object parent, Connection conn, QueryConditions<T> conditions, List<String> joinFields)
	{
		LoadDataSession<T> sess = parseLoadSession(cls, parent, conn, conditions, joinFields, false);
		try
		{
			sqlLogger.logMessage(sess.sql.toString(), LogLevel.COMMAND);
			PreparedStatement stmt = conn.prepareStatement(sess.sql.toString());
			ResultSet rs = stmt.executeQuery();
			return new DBIterator<T>(rs, parent, sess.constr, sess.dbType, sess.cols, sess.clientConditions);
		}
		catch (SQLException ex)
		{
			sqlLogger.logException(ex);
			return null;
		}
	}

	public static <T> DBIterator<T> loadDataScript(Class<T> cls, Object parent, Connection conn, String catalog, String schema, String procName, List<Object> params)
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
		parseDBCols(cls, cols, idCols, null);
		if (cols.size() == 0)
		{
			throw new IllegalArgumentException("No selectable column found");
		}

		StringBuilder sql = new StringBuilder();
		DBType dbType = connGetDBType(conn);
		sql.append("exec ");
		if (catalog != null)
		{
			sql.append(dbCol(dbType, catalog));
			sql.append('.');
		}
		if (schema != null)
		{
			sql.append(dbCol(dbType, schema));
			sql.append('.');
		}
		sql.append(dbCol(dbType, procName));
		if (params != null)
		{
			int i = 0;
			int j = params.size();
			Object param;
			while (i < j)
			{
				param = params.get(i);
				if (i > 0)
					sql.append(',');
				else
					sql.append(' ');
				if (param instanceof String)
				{
					sql.append(dbStr(dbType, (String)param));
				}
				else
				{
					System.out.println("Unknown param type");
					throw new IllegalArgumentException("Unknown param type");
				}
				i++;
			}
		}
		try
		{
			sqlLogger.logMessage(sql.toString(), LogLevel.COMMAND);
			PreparedStatement stmt = conn.prepareStatement(sql.toString());
			ResultSet rs = stmt.executeQuery();
			return new DBIterator<T>(rs, parent, constr, dbType, cols, List.of());
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
		return new SQLConnection(conn, sqlLogger).loadItemsAsList(cls, parent, conditions, joinFields, sortString, dataOfst, dataCnt);
	}	

	public static <T> List<T> loadItemsAsListWithJoins(Class<T> cls, Object parent, Connection conn, QueryConditions<T> conditions, String sortString, int dataOfst, int dataCnt) throws NoSuchFieldException
	{
		List<String> joinFields = new ArrayList<String>();
		List<T> list = new SQLConnection(conn, sqlLogger).loadItemsAsList(cls, parent, conditions, joinFields, sortString, dataOfst, dataCnt);
		int i = 0;
		int j = joinFields.size();
		while (i < j)
		{
			DBUtil.loadJoinItems(conn, list, joinFields.get(i));
			i++;
		}
		return list;
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
		if (dbType == DBType.PostgreSQL && "sde".equals(tableAnn.schema()))
		{
			dbType = DBType.PostgreSQLESRI;
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
					fillColVals(dbType, rs, ret, cols);
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
		boolean clientCheck = false;
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
				if (objMap.size() > MAX_SQL_ITEMS)
				{
					clientCheck = true;
				}
				else
				{
					sb.append(" where ");
					sb.append(idName);
					sb.append(" in (");
					sb.append(DataTools.intJoin(objMap.keySet(), ", "));
					sb.append(")");
				}
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
		else if (oneToMany != null && (fieldType.equals(Set.class) || fieldType.equals(List.class)))
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
					Collection<Object> targetSet = ((Collection<Object>)getter.get(tObj));
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
			if (idSet.size() > MAX_SQL_ITEMS)
			{
				clientCheck = true;
			}
			else
			{
				sb.append(" where ");
				sb.append(idName);
				sb.append(" in (");
				sb.append(DataTools.intJoin(idSet, ", "));
				sb.append(")");
			}
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
					if (!clientCheck || idSet.contains(rs.getInt(1)))
					{
						joinItem = new JoinItem();
						joinItem.id = rs.getInt(1);
						joinItem.joinId = rs.getInt(2);
						joinItemList.add(joinItem);
					}
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
		if (dbType == DBType.MySQL)
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
		else if (dbType == DBType.SQLite)
		{
			val = val.replace("\\", "\\\\");
			val = val.replace("\'", "\\\'");
			val = val.replace("\"", "\\\"");
			val = val.replace("\r", "\\r");
			val = val.replace("\n", "\\n");
			val = val.replace("\t", "\\t");
			return "'"+val+"'";
		}
		else if (dbType == DBType.MSSQL)
		{
			val = val.replace("\'", "\'\'");
			return "N'"+val+"'";
		}
		else if (dbType == DBType.Access)
		{
			val = val.replace("\'", "\'\'");
			return "'"+val+"'";
		}
		else if (dbType == DBType.PostgreSQL || dbType == DBType.PostgreSQLESRI)
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

	public static String dbBool(DBType dbType, boolean val)
	{
		return val?"1":"0";
	}

	public static String dbTS(DBType dbType, Timestamp val)
	{
		if (val == null)
		{
			return "null";
		}
		if (dbType == DBType.Access)
		{
			return "#"+val.toString()+"#";
		}
		else if (dbType == DBType.MSSQL)
		{
			return "CAST('"+DateTimeUtil.toString(val, "yyyy-MM-dd HH:mm:ss.fffffff")+"' as datetime2(7))";
		}
		else if (dbType == DBType.SQLite)
		{
			return "'"+DateTimeUtil.toString(val, "yyyy-MM-dd HH:mm:ss.fff")+"'";
		}
		else if (dbType == DBType.Oracle)
		{
			return "TIMESTAMP '"+DateTimeUtil.toString(val, "yyyy-MM-dd HH:mm:ss.fffffffff")+"'";
		}
		else if (dbType == DBType.MySQL)
		{
			return "'"+DateTimeUtil.toString(val, "yyyy-MM-dd HH:mm:ss.ffffff")+"'";
		}
		else if (dbType == DBType.PostgreSQL || dbType == DBType.PostgreSQLESRI)
		{
			return "'"+DateTimeUtil.toString(val, "yyyy-MM-dd HH:mm:ss.ffffff")+"'";
		}
		else
		{
			return "'"+DateTimeUtil.toString(val, "yyyy-MM-dd HH:mm:ss")+"'";
		}
	}

	public static String dbDate(DBType dbType, Date val)
	{
		if (val == null)
		{
			return "null";
		}
		if (dbType == DBType.Access)
		{
			return "#"+val.toString()+"#";
		}
		else if (dbType == DBType.MSSQL)
		{
			return "'"+DateTimeUtil.toString(val, "yyyy-MM-dd")+"'";
		}
		else if (dbType == DBType.SQLite)
		{
			return "'"+DateTimeUtil.toString(val, "yyyy-MM-dd")+"'";
		}
		else if (dbType == DBType.Oracle)
		{
			return "DATE '"+DateTimeUtil.toString(val, "yyyy-MM-dd")+"'";
		}
		else if (dbType == DBType.MySQL)
		{
			return "'"+DateTimeUtil.toString(val, "yyyy-MM-dd")+"'";
		}
		else if (dbType == DBType.PostgreSQL || dbType == DBType.PostgreSQLESRI)
		{
			return "'"+DateTimeUtil.toString(val, "yyyy-MM-dd")+"'";
		}
		else
		{
			return "'"+DateTimeUtil.toString(val, "yyyy-MM-dd")+"'";
		}
	}

	public static String dbTime(DBType dbType, Time val)
	{
		if (val == null)
		{
			return "null";
		}
		if (dbType == DBType.Access)
		{
			return "#"+val.toString()+"#";
		}
		else if (dbType == DBType.MSSQL)
		{
			return "'"+DateTimeUtil.toString(val, "HH:mm:ss.fffffff")+"'";
		}
		else if (dbType == DBType.SQLite)
		{
			return "'"+DateTimeUtil.toString(val, "HH:mm:ss.fff")+"'";
		}
		else if (dbType == DBType.Oracle)
		{
			return "TIMESTAMP '"+DateTimeUtil.toString(val, "HH:mm:ss.fffffffff")+"'";
		}
		else if (dbType == DBType.MySQL)
		{
			return "'"+DateTimeUtil.toString(val, "HH:mm:ss.ffffff")+"'";
		}
		else if (dbType == DBType.PostgreSQL || dbType == DBType.PostgreSQLESRI)
		{
			return "'"+DateTimeUtil.toString(val, "HH:mm:ss.ffffff")+"'";
		}
		else
		{
			return "'"+DateTimeUtil.toString(val, "HH:mm:ss")+"'";
		}
	}

	public static String dbBin(DBType dbType, byte val[])
	{
		if (val == null)
		{
			return "null";
		}
		if (dbType == DBType.MySQL || dbType == DBType.SQLite)
		{
			return "x'"+StringUtil.toHex(val)+"'";
		}
		else if (dbType == DBType.MSSQL)
		{
			return "0x"+StringUtil.toHex(val);
		}
		else
		{
			return "''";
		}
	}

	public static String dbGeometry(DBType dbType, Geometry geometry)
	{
		if (geometry == null)
		{
			return "NULL";
		}
		if (dbType == DBType.MySQL)
		{
			return "GeomFromText('"+GeometryUtil.toWKT(geometry)+"', "+geometry.getSRID()+")";
		}
		else if (dbType == DBType.MSSQL)
		{
			return "geometry::STGeomFromText('"+GeometryUtil.toWKT(geometry)+"', "+geometry.getSRID()+")";
		}
		else if (dbType == DBType.PostgreSQL)
		{
			return "ST_GeomFromText('"+GeometryUtil.toWKT(geometry)+"', "+geometry.getSRID()+")";
		}
		else if (dbType == DBType.PostgreSQLESRI)
		{
			return "sde.st_geometry('"+GeometryUtil.toWKT(geometry)+"', "+geometry.getSRID()+")";
		}
		else
		{
			WKBWriter writer = new WKBWriter();
			return dbBin(dbType, writer.write(geometry));
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
			return dbTS(dbType, (Timestamp)val);
		}
		else if (fieldType.equals(Date.class) && val.getClass().equals(Date.class))
		{
			return dbDate(dbType, (Date)val);
		}
		else if (fieldType.equals(Time.class) && val.getClass().equals(Time.class))
		{
			return dbTime(dbType, (Time)val);
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
		else if (fieldType.equals(Float.class) || fieldType.equals(Float.class))
		{
			if (val instanceof Float)
			{
				return ((Float)val).toString();
			}
		}
		else if (fieldType.equals(Boolean.class))
		{
			if (val instanceof Boolean)
			{
				return dbBool(dbType, (Boolean)val);
			}
			else if (val instanceof Integer)
			{
				return dbBool(dbType, ((Integer)val) != 0);
			}
		}
		else if (fieldType.equals(Geometry.class))
		{
			if (val instanceof Geometry)
			{
				return dbGeometry(dbType, (Geometry)val);
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
		if (dbType == DBType.MySQL)
		{
			return "`"+val+"`";
		}
		else if (dbType == DBType.MSSQL || dbType == DBType.Access)
		{
			return "["+val+"]";
		}
		else if (dbType == DBType.PostgreSQL || dbType == DBType.PostgreSQLESRI)
		{
			return "\""+val+"\"";
		}
		else
		{
			return val;
		}
	}

	public static void dbCol(StringBuilder sb, DBType dbType, String val)
	{
		if (dbType == DBType.MySQL)
		{
			sb.append('`');
			sb.append(val);
			sb.append('`');
		}
		else if (dbType == DBType.MSSQL || dbType == DBType.Access)
		{
			sb.append('[');
			sb.append(val);
			sb.append(']');
		}
		else if (dbType == DBType.PostgreSQL || dbType == DBType.PostgreSQLESRI)
		{
			sb.append('\"');
			sb.append(val);
			sb.append('\"');
		}
		else
		{
			sb.append(val);
		}
	}

	public static int getLastIdentity32(Connection conn)
	{
		DBType dbType = connGetDBType(conn);
		if (dbType == DBType.MySQL || dbType == DBType.MSSQL || dbType == DBType.Access)
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
		else if (dbType == DBType.PostgreSQL || dbType == DBType.PostgreSQLESRI)
		{
			int id = 0;
			try
			{
				PreparedStatement stmt = conn.prepareStatement("select lastval()");
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

	private static <T> boolean update(Connection conn, TableInfo table, T oriObj, T newObj, DBOptions options)
	{
		DBType dbType = connGetDBType(conn);
		if (dbType == DBType.PostgreSQL && (table.tableAnn != null) && "sde".equals(table.tableAnn.schema()))
		{
			dbType = DBType.PostgreSQLESRI;
		}
		StringBuilder sb;
		DBColumnInfo col;
		int i;
		int j;
		try
		{
			if (newObj == null)
			{
				if (table.idCols.size() == 0)
				{
					return false;
				}
				sb = new StringBuilder();
				sb.append("delete from ");
				sb.append(getTableName(table.tableAnn, dbType));
				sb.append(" where ");
				i = 0;
				j = table.idCols.size();
				while (i < j)
				{
					col = table.idCols.get(i);
					if (i > 0)
					{
						sb.append(" and ");
					}
					dbCol(sb, dbType, col.colName);
					sb.append(" = ");
					sb.append(dbVal(dbType, col, col.getter.get(oriObj)));
					i++;
				}
				boolean ret = executeNonQuery(conn, sb.toString(), options);
				if (ret && updateHandlers != null)
				{
					i = updateHandlers.size();
					while (i-- > 0)
					{
						try
						{
							updateHandlers.get(i).dbUpdated(oriObj, newObj);
						}
						catch (Exception ex)
						{

						}
					}
				}
				return ret;
			}
			else if (oriObj == null)
			{
				sb = new StringBuilder();
				sb.append("insert into ");
				sb.append(getTableName(table.tableAnn, dbType));
				sb.append(" (");
				boolean found = false;
				i = 0;
				j = table.allCols.size();
				while (i < j)
				{
					col = table.allCols.get(i);
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
					col = table.allCols.get(i);
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
				found = executeNonQuery(conn, sb.toString(), options);
				if (found)
				{
					if (table.idCols.size() == 1)
					{
						col = table.idCols.get(0);
						if (col.genType == GenerationType.IDENTITY)
						{
							col.setter.set(newObj, getLastIdentity32(conn));
						}
					}
					if (updateHandlers != null)
					{
						i = updateHandlers.size();
						while (i-- > 0)
						{
							try
							{
								updateHandlers.get(i).dbUpdated(oriObj, newObj);
							}
							catch (Exception ex)
							{
	
							}
						}
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
				sb.append(getTableName(table.tableAnn, dbType));
				sb.append(" set ");
				i = 0;
				j = table.allCols.size();
				while (i < j)
				{
					col = table.allCols.get(i);
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
				j = table.idCols.size();
				while (i < j)
				{
					col = table.idCols.get(i);
					if (i > 0)
					{
						sb.append(" and ");
					}
					sb.append(dbCol(dbType, col.colName));
					sb.append(" = ");
					sb.append(dbVal(dbType, col, col.getter.get(oriObj)));
					i++;
				}
				if (executeNonQuery(conn, sb.toString(), options))
				{
					if (updateHandlers != null)
					{
						i = updateHandlers.size();
						while (i-- > 0)
						{
							try
							{
								updateHandlers.get(i).dbUpdated(oriObj, newObj);
							}
							catch (Exception ex)
							{
	
							}
						}
					}
					i = 0;
					j = table.allCols.size();
					while (i < j)
					{
						col = table.allCols.get(i);
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

	public static <T> boolean objInsert(Connection conn, T newObj, DBOptions options)
	{
		TableInfo table = null;
		if (newObj != null)
		{
			table = parseTableInfo(newObj.getClass());
		}
		else
		{
			return false;
		}
		return update(conn, table, null, newObj, options);
	}

	public static <T> boolean objInsertAll(Connection conn, Class<T> cls, Iterable<T> objList, DBOptions options)
	{
		TableInfo table = null;
		if (cls != null)
		{
			table = parseTableInfo(cls);
		}
		else
		{
			return false;
		}
		DBType dbType = connGetDBType(conn);
		if (dbType == DBType.PostgreSQL && (table.tableAnn != null) && "sde".equals(table.tableAnn.schema()))
		{
			dbType = DBType.PostgreSQLESRI;
		}
		DBColumnInfo col;
		StringBuilder sb = new StringBuilder();
		sb.append("insert into ");
		sb.append(getTableName(table.tableAnn, dbType));
		sb.append(" (");
		boolean found = false;
		int i = 0;
		int j = table.allCols.size();
		while (i < j)
		{
			col = table.allCols.get(i);
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
		sb.append(") values ");
		boolean objFound = false;
		Iterator<T> it = objList.iterator();
		T newObj;
		try
		{
			while (it.hasNext())
			{
				newObj = it.next();
				if (objFound)
				{
					sb.append(',');
				}
				else
				{
					objFound = true;
				}
				sb.append("(");
				found = false;
				i = 0;
				while (i < j)
				{
					col = table.allCols.get(i);
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

		found = executeNonQuery(conn, sb.toString(), options);
		if (found)
		{
			if (updateHandlers != null)
			{
				it = objList.iterator();
				while (it.hasNext())
				{
					newObj = it.next();
					i = updateHandlers.size();
					while (i-- > 0)
					{
						try
						{
							updateHandlers.get(i).dbUpdated(null, newObj);
						}
						catch (Exception ex)
						{

						}
					}
				}
			}
		}
		return found;
	}

	public static <T> boolean objDelete(Connection conn, T oriObj, DBOptions options)
	{
		TableInfo table = null;
		if (oriObj != null)
		{
			table = parseTableInfo(oriObj.getClass());
		}
		else
		{
			return false;
		}
		return update(conn, table, oriObj, null, options);
	}

	public static <T> boolean objUpdate(Connection conn, T oriObj, T newObj, DBOptions options)
	{
		TableInfo table = null;
		if (oriObj == null || newObj == null)
		{
			return false;
		}
		table = parseTableInfo(oriObj.getClass());
		return update(conn, table, oriObj, newObj, options);
	}

	public static boolean executeNonQuery(Connection conn, String sql, DBOptions options)
	{
		try
		{
			if (options == null || !options.skipLog)
			{
				sqlLogger.logMessage(sql, LogLevel.COMMAND);
			}
			PreparedStatement stmt;
			stmt = conn.prepareStatement(sql);
			int rowCnt = stmt.executeUpdate();
			return rowCnt >= 0;
		}
		catch (SQLException ex)
		{
			if (options == null || !options.skipLog)
			{
				sqlLogger.logException(ex);
			}
			return false;
		}
	}
	
	public static <T> boolean deleteRecords(Connection conn, Class<T> cls, QueryConditions<T> conditions)
	{
		StringBuilder sb;
		Table tableAnn = parseClassTable(cls);
		if (tableAnn == null)
		{
			throw new IllegalArgumentException("Class annotation is not valid");
		}
		DBType dbType = connGetDBType(conn);
		sb = new StringBuilder();
		if (conditions == null)
		{
			sb.append("truncate table ");
			sb.append(getTableName(tableAnn, dbType));
			return executeNonQuery(conn, sb.toString(), null);
		}
		else
		{
			ArrayList<DBColumnInfo> cols = new ArrayList<DBColumnInfo>();
			ArrayList<DBColumnInfo> idCols = new ArrayList<DBColumnInfo>();
			parseDBCols(cls, cols, idCols, null);
			List<QueryConditions<T>.Condition> clientConditions = new ArrayList<QueryConditions<T>.Condition>();
			Map<String, DBColumnInfo> colsMap = dbCols2Map(cols);
			String whereClause = conditions.toWhereClause(colsMap, dbType, clientConditions, MAX_SQL_ITEMS);
			if (StringUtil.isNullOrEmpty(whereClause) && clientConditions.size() == 0)
			{
				sb.append("truncate table ");
				sb.append(getTableName(tableAnn, dbType));
				return executeNonQuery(conn, sb.toString(), null);
			}
			else if (clientConditions.size() == 0)
			{
				sb.append("delete from ");
				sb.append(getTableName(tableAnn, dbType));
				sb.append(" where ");
				sb.append(whereClause);
				return executeNonQuery(conn, sb.toString(), null);
			}
			else
			{
				/////////////////////////////
				return false;
			}
		}
	}

	public static Connection openAccessFile(String accessPath, String accessPwd)
	{
		String jdbcStr;
		try
		{
			Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
		}
		catch (ClassNotFoundException ex)
		{
			ex.printStackTrace();
		}
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
			if (accessPwd != null)
			{
				return DriverManager.getConnection(jdbcStr, null, accessPwd);
			}
			else
			{
				return DriverManager.getConnection(jdbcStr);
			}
		}
		catch (SQLException ex)
		{
			sqlLogger.logException(ex);
			return null;
		}
	}

	public static ColumnType parseColType(DBType svrType, String typeName, SharedInt colSize)
	{
		typeName = typeName.toUpperCase();
		if (colSize == null)
		{
			colSize = new SharedInt(-1);
		}
		if (svrType == DBType.MySQL)
		{
			switch (typeName)
			{
			case "VARCHAR":
				return ColumnType.VarUTF32Char;
			case "CHAR":
				return ColumnType.UTF32Char;
			case "BIGINT":
				return ColumnType.Int64;
			case "BIGINT UNSIGNED":
				return ColumnType.UInt64;
			case "INT":
				return ColumnType.Int32;
			case "INT UNSIGNED":
				return ColumnType.UInt32;
			case "SMALLINT":
				return ColumnType.Int16;
			case "SMALLINT UNSIGNED":
				return ColumnType.UInt16;
			case "DATE":
				return ColumnType.Date;
			case "DATETIME":
				if (colSize.value < 0)
					colSize.value = 0;
				return ColumnType.DateTime;
			case "DATETIMEOFFSET":
				if (colSize.value < 0)
					colSize.value = 0;
				return ColumnType.DateTimeTZ;
			case "TIMESTAMP":
				if (colSize.value < 0)
					colSize.value = 0;
				return ColumnType.DateTime;
			case "DOUBLE":
				colSize.value = 8;
				return ColumnType.Double;
			case "FLOAT":
				colSize.value = 4;
				return ColumnType.Float;
			case "LONGTEXT":
				colSize.value = Integer.MAX_VALUE;
				return ColumnType.VarUTF32Char;
			case "TEXT":
				colSize.value = 65535;
				return ColumnType.VarUTF32Char;
			case "TINYINT":
			case "TINYINT UNSIGNED":
				if (colSize.value == 1)
				{
					colSize.value = 1;
					return ColumnType.Bool;
				}
				else
				{
					colSize.value = 1;
					return ColumnType.Byte;
				}
			case "BIT":
				colSize.value = 1;
				return ColumnType.Bool;
			case "GEOMETRY":
				return ColumnType.Vector;
			case "BLOB":
				colSize.value = Integer.MAX_VALUE;
				return ColumnType.Binary;
			default:
				colSize.value = 0;
				return ColumnType.Unknown;
			}
		}
		else if (svrType == DBType.MSSQL)
		{
			switch (typeName)
			{
			case "VARCHAR":
				return ColumnType.VarUTF8Char;
			case "TEXT":
				colSize.value = 0x7FFFFFFF;
				return ColumnType.VarUTF8Char;
			case "CHAR":
				return ColumnType.UTF8Char;
			case "INT":
				return ColumnType.Int32;
			case "DATETIME":
				colSize.value = 3;
				return ColumnType.DateTime;
			case "DATETIME2":
				if (colSize.value == -1)
				{
					colSize.value = 7;
				}
				return ColumnType.DateTime;
			case "DATETIMEOFFSET":
				if (colSize.value == -1)
				{
					colSize.value = 7;
				}
				return ColumnType.DateTimeTZ;
			case "FLOAT":
				return ColumnType.Double;
			case "BIT":
				return ColumnType.Bool;
			case "BIGINT":
				return ColumnType.Int64;
			case "SMALLINT":
				return ColumnType.Int16;
			case "NVARCHAR":
				return ColumnType.VarUTF16Char;
			case "NTEXT":
				colSize.value = 0x3FFFFFFF;
				return ColumnType.VarUTF16Char;
			case "NCHAR":
				return ColumnType.UTF16Char;
			case "NUMERIC":
				return ColumnType.Double;
			case "GEOMETRY":
				return ColumnType.Vector;
			case "DATE":
				return ColumnType.Date;
			case "SYSNAME":
				colSize.value = 128;
				return ColumnType.VarUTF16Char;
			case "BINARY":
				return ColumnType.Binary;
			case "VARBINARY":
				return ColumnType.Binary;
			case "IMAGE":
				colSize.value = 0x7FFFFFFF;
				return ColumnType.Binary;
			case "UNIQUEIDENTIFIER":
				return ColumnType.UUID;
			case "XML":
				colSize.value = 1073741823;
				return ColumnType.VarUTF16Char;
			default:
				return ColumnType.Unknown;
			}
		}
		else if (svrType == DBType.SQLite)
		{
			switch (typeName)
			{
			case "INTEGER":
				colSize.value = 4;
				return ColumnType.Int32;
			case "INT":
				colSize.value = 4;
				return ColumnType.Int32;
			case "MEDIUMINT":
				colSize.value = 2;
				return ColumnType.Int16;
			case "TINYINT":
				colSize.value = 1;
				return ColumnType.Byte;
			case "REAL":
				colSize.value = 8;
				return ColumnType.Double;
			case "DOUBLE":
				colSize.value = 8;
				return ColumnType.Double;
			case "DATETIME":
				colSize.value = 3;
				return ColumnType.DateTime;
			case "BLOB":
				colSize.value = 2147483647;
				return ColumnType.Binary;
			case "TEXT":
				colSize.value = 2147483647;
				return ColumnType.VarUTF8Char;
			case "POINT":
				colSize.value = 2147483647;
				return ColumnType.Vector;
			case "LINESTRING":
				colSize.value = 2147483647;
				return ColumnType.Vector;
			case "POLYGON":
				colSize.value = 2147483647;
				return ColumnType.Vector;
			case "BOOLEAN":
				colSize.value = 1;
				return ColumnType.Byte;
			default:
				colSize.value = 0;
				return ColumnType.Unknown;
			}
		}
		else
		{
			return ColumnType.Unknown;
		}
	}
}
