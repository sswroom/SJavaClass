package org.sswr.util.db;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.sswr.util.data.DataTools;
import org.sswr.util.data.FieldGetter;
import org.sswr.util.data.FieldSetter;

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

	public static DBType connGetDBType(Connection conn)
	{
		String clsName = conn.getClass().getName();
		if (clsName.equals("com.microsoft.sqlserver.jdbc.SQLServerConnection"))
		{
			return DBType.DT_MSSQL;
		}
		else
		{
			System.out.println("DB class = "+clsName);
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
		Annotation anns[];
		int i;
		int j;
		anns = field.getAnnotations();
		enumType = EnumType.ORDINAL;
		isId = false;
		isTransient = false;
		isJoin = false;
		joinCol = null;
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
				else if (annType.equals(JoinColumn.class))
				{
					joinCol = (JoinColumn)anns[i];
					colName = joinCol.name();
					if (joinFields != null)
					{
						joinFields.add(field.getName());
					}
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
			try
			{
				col.setter = new FieldSetter(field);
				return col;
			}
			catch (IllegalArgumentException ex)
			{
				System.out.println(ex.getMessage());
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

	private static String getTableName(Table table)
	{
		StringBuilder sb = new StringBuilder();
		if (!table.catalog().equals(""))
		{
			sb.append(table.catalog());
			sb.append('.');
		}
		if (!table.schema().equals(""))
		{
			sb.append(table.schema());
			sb.append('.');
		}
		sb.append(table.name());
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

	/*
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

	public static void appendSelect(StringBuilder sb, List<DBColumnInfo> allCols, Table tableAnn)
	{
		sb.append("select ");
		int i = 0;
		int j = allCols.size();
		while (i < j)
		{
			if (i > 0)
			{
				sb.append(", ");
			}
			sb.append(allCols.get(i).colName);
			i++;
		}
		sb.append(" from ");
		sb.append(getTableName(tableAnn));
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
						System.out.println("DBUtil.fillColVals join idCols mismatch");
					}
				}
				catch (NoSuchMethodException ex)
				{
					ex.printStackTrace();
				}
				catch (InstantiationException ex)
				{
					ex.printStackTrace();
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
			else
			{
//							col.setterMeth.invoke(obj, rs.getObject(i + 1));
				System.out.println("Unknown fieldType for "+col.field.getName()+" ("+fieldType.toString()+")");
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
		appendSelect(sb, cols, tableAnn);

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
			System.out.println(sb.toString());

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
					ex.printStackTrace();
				}
				catch (InstantiationException ex)
				{
					ex.printStackTrace();
				}
				catch (IllegalAccessException ex)
				{
					ex.printStackTrace();
				}
			}
			rs.close();
			return retMap;
		}
		catch (SQLException ex)
		{
			System.out.println(sb.toString());
			ex.printStackTrace();
			return null;
		}
	}	

	/*
	* @param joinFields return fields which are joined with other tables, null = not returns
	*/
	public static <T> Map<Integer, T> loadItems(Class<T> cls, Connection conn, QueryConditions<T> conditions, List<String> joinFields)
	{
		StringBuilder sb;
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

		sb = new StringBuilder();
		appendSelect(sb, cols, tableAnn);

		if (conditions != null)
		{
			Map<String, DBColumnInfo> colsMap = dbCols2Map(cols);
			DBType dbType = connGetDBType(conn);
			sb.append(" where ");
			sb.append(conditions.toWhereClause(colsMap, dbType));
		}
		try
		{
			System.out.println(sb.toString());

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
					ex.printStackTrace();
				}
				catch (InstantiationException ex)
				{
					ex.printStackTrace();
				}
				catch (IllegalAccessException ex)
				{
					ex.printStackTrace();
				}
			}
			rs.close();
			return retMap;
		}
		catch (SQLException ex)
		{
			System.out.println(sb.toString());
			ex.printStackTrace();
			return null;
		}
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
		try
		{
			Constructor<?>[] constrs = cls.getDeclaredConstructors();
			System.out.println("Constr count = "+constrs.length);
			int i = 0;
			int j = constrs.length;
			while (i < j)
			{
				System.out.println("Constr: "+constrs[i]);
				i++;
			}
			constr = cls.getDeclaredConstructor(new Class<?>[]{parent.getClass()});
		}
		catch (NoSuchMethodException ex)
		{
			ex.printStackTrace();
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

		sb = new StringBuilder();
		appendSelect(sb, cols, tableAnn);

		if (conditions != null)
		{
			Map<String, DBColumnInfo> colsMap = dbCols2Map(cols);
			DBType dbType = connGetDBType(conn);
			sb.append(" where ");
			sb.append(conditions.toWhereClause(colsMap, dbType));
		}
		try
		{
			System.out.println(sb.toString());

			PreparedStatement stmt = conn.prepareStatement(sb.toString());
			ResultSet rs = stmt.executeQuery();
			HashMap<Integer, T> retMap = new HashMap<Integer, T>();
			while (rs.next())
			{
				try
				{
					T obj = constr.newInstance(parent);
					Integer id = fillColVals(rs, obj, cols);

					if (id != null)
					{
						retMap.put(id, obj);
					}
				}
				catch (InvocationTargetException ex)
				{
					ex.printStackTrace();
				}
				catch (InstantiationException ex)
				{
					ex.printStackTrace();
				}
				catch (IllegalAccessException ex)
				{
					ex.printStackTrace();
				}
			}
			rs.close();
			return retMap;
		}
		catch (SQLException ex)
		{
			System.out.println(sb.toString());
			ex.printStackTrace();
			return null;
		}
	}	

	/*
	* @param joinFields return fields which are joined with other tables, null = not returns
	*/
	public static <T> T loadItem(Class<T> cls, Connection conn, int id, List<String> joinFields)
	{
		StringBuilder sb;
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
		appendSelect(sb, cols, tableAnn);

		sb.append(" where ");
		sb.append(idCol.colName);
		sb.append(" = ");
		sb.append(id);
		try
		{
			System.out.println(sb.toString());

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
					ex.printStackTrace();
				}
				catch (InstantiationException ex)
				{
					ex.printStackTrace();
				}
				catch (IllegalAccessException ex)
				{
					ex.printStackTrace();
				}
			}
			rs.close();
			return ret;
		}
		catch (SQLException ex)
		{
			System.out.println(sb.toString());
			ex.printStackTrace();
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
		ArrayList<DBColumnInfo> cols = new ArrayList<DBColumnInfo>();
		ArrayList<DBColumnInfo> idCols = new ArrayList<DBColumnInfo>();
		parseDBCols(obj.getClass(), cols, idCols, null);
		Field field = obj.getClass().getDeclaredField(fieldName);
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
			i++;
		}
		Class<?> fieldType = field.getType();
		if (fieldType.equals(Set.class))
		{
			if (elemColl != null && collTab != null && column != null)
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
					System.out.println(sb.toString());
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
						ex.printStackTrace();
					}
					catch (IllegalAccessException ex)
					{
						ex.printStackTrace();
					}
					rs.close();
					stmt.close();
				}
				catch (SQLException ex)
				{
					ex.printStackTrace();
				}
			}
			else
			{
				throw new IllegalArgumentException("Unsupported annotations: "+field.toString());
			}
			return false;
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
}
