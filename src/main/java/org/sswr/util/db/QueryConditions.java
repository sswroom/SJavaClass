package org.sswr.util.db;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import org.sswr.util.basic.CompareCondition;
import org.sswr.util.data.DataTools;
import org.sswr.util.data.FieldGetter;

public class QueryConditions<T>
{
	public abstract class Condition
	{
		public abstract String toWhereClause(Map<String, DBColumnInfo> colsMap, DBUtil.DBType dbType, int maxDbItem);
		public abstract boolean isValid(T obj) throws IllegalAccessException, InvocationTargetException;
	}

	public class TimeBetweenCondition extends Condition
	{
		private String fieldName;
		private Timestamp t1;
		private Timestamp t2;
		private FieldGetter<T> getter;

		public TimeBetweenCondition(String fieldName, Timestamp t1, Timestamp t2) throws NoSuchFieldException
		{
			this.fieldName = fieldName;
			this.t1 = t1;
			this.t2 = t2;
			this.getter = new FieldGetter<T>(cls, fieldName);
			Class<?> fieldType = this.getter.getFieldType();
			if (fieldType.equals(Timestamp.class))
			{

			}
			else
			{
				throw new NoSuchFieldException("Not date time format: "+fieldType.toString());
			}
		}

		public String toWhereClause(Map<String, DBColumnInfo> colsMap, DBUtil.DBType dbType, int maxDbItem)
		{
			StringBuilder sb = new StringBuilder();
			DBColumnInfo col = colsMap.get(this.fieldName);
			sb.append(DBUtil.dbCol(dbType, col.colName));
			sb.append(" between ");
			sb.append(DBUtil.dbTS(dbType, t1));
			sb.append(" and ");
			sb.append(DBUtil.dbTS(dbType, t2));
			return sb.toString();
		}

		public boolean isValid(T obj) throws IllegalAccessException, InvocationTargetException
		{
			Object v = this.getter.get(obj);
			if (v == null)
			{
				return false;
			}
			Class<?> fieldType = v.getClass();
			if (fieldType.equals(Timestamp.class))
			{
				Timestamp ts = (Timestamp)v;
				return ts.compareTo(t1) >= 0 && ts.compareTo(t2) <= 0;
			}
			return false;
		}
	}

	public class IntCondition extends Condition
	{
		private String fieldName;
		private Integer val;
		private CompareCondition cond;
		private FieldGetter<T> getter;

		public IntCondition(String fieldName, Integer val, CompareCondition cond) throws NoSuchFieldException
		{
			this.fieldName = fieldName;
			this.val = val;
			this.cond = cond;
			this.getter = new FieldGetter<T>(cls, fieldName);
			Class<?> fieldType = this.getter.getFieldType();
			if (fieldType.equals(Integer.class) || fieldType.equals(int.class))
			{

			}
			else
			{
				throw new NoSuchFieldException("Not Integer format: "+fieldType.toString());
			}
		}

		public String getFieldName()
		{
			return this.fieldName;
		}

		public Integer getVal()
		{
			return this.val;
		}

		public CompareCondition getCompareCond()
		{
			return this.cond;
		}

		public String toWhereClause(Map<String, DBColumnInfo> colsMap, DBUtil.DBType dbType, int maxDbItem)
		{
			StringBuilder sb = new StringBuilder();
			DBColumnInfo col = colsMap.get(this.fieldName);
			sb.append(DBUtil.dbCol(dbType, col.colName));
			if (val == null)
			{
				if (cond != CompareCondition.NOT_EQUAL)
				{
					sb.append(" is null");
				}
				else
				{
					sb.append(" is not null");
				}
			}
			else
			{
				sb.append(getCondStr(cond));
				sb.append(this.val.toString());
			}
			return sb.toString();
		}

		public boolean isValid(T obj) throws IllegalAccessException, InvocationTargetException
		{
			Object v = this.getter.get(obj);
			if (this.val == null)
			{
				if (cond != CompareCondition.NOT_EQUAL)
				{
					return v == null;
				}
				else
				{
					return v != null;
				}
			}

			if (v == null)
			{
				return false;
			}
			int iVal = (Integer)v;
			switch (cond)
			{
			case EQUAL:
				return iVal == this.val;
			case NOT_EQUAL:
				return iVal != this.val;
			case GREATER:
				return iVal > this.val;
			case GREATER_OR_EQUAL:
				return iVal >= this.val;
			case LESS:
				return iVal < this.val;
			case LESS_OR_EQUAL:
				return iVal <= this.val;
			}
			return false;
		}
	}

	public class IntInCondition extends Condition
	{
		private String fieldName;
		private Iterable<Integer> vals;
		private FieldGetter<T> getter;

		public IntInCondition(String fieldName, Iterable<Integer> vals) throws NoSuchFieldException
		{
			this.fieldName = fieldName;
			this.vals = vals;
			this.getter = new FieldGetter<T>(cls, fieldName);
			Class<?> fieldType = this.getter.getFieldType();
			if (fieldType.equals(Integer.class) || fieldType.equals(int.class))
			{

			}
			else
			{
				throw new NoSuchFieldException("Not Integer format: "+fieldType.toString());
			}
		}

		public String toWhereClause(Map<String, DBColumnInfo> colsMap, DBUtil.DBType dbType, int maxDbItem)
		{
			if (DataTools.getSize(vals) > maxDbItem)
			{
				return "";
			}
			else
			{
				StringBuilder sb = new StringBuilder();
				DBColumnInfo col = colsMap.get(this.fieldName);
				sb.append(DBUtil.dbCol(dbType, col.colName));
				sb.append(" in (");
				sb.append(DataTools.intJoin(vals, ", "));
				sb.append(")");
				return sb.toString();
			}
		}

		public boolean isValid(T obj) throws IllegalAccessException, InvocationTargetException
		{
			Object v = this.getter.get(obj);
			if (v == null)
			{
				return false;
			}
			int iVal = (Integer)v;
			Iterator<Integer> it = this.vals.iterator();
			while (it.hasNext())
			{
				if (iVal == it.next())
				{
					return true;
				}
			}
			return false;
		}
	}

	public class DoubleCondition extends Condition
	{
		private String fieldName;
		private Double val;
		private CompareCondition cond;
		private FieldGetter<T> getter;

		public DoubleCondition(String fieldName, Double val, CompareCondition cond) throws NoSuchFieldException
		{
			this.fieldName = fieldName;
			this.val = val;
			this.cond = cond;
			this.getter = new FieldGetter<T>(cls, fieldName);
			Class<?> fieldType = this.getter.getFieldType();
			if (fieldType.equals(Double.class) || fieldType.equals(double.class))
			{

			}
			else
			{
				throw new NoSuchFieldException("Not Double format: "+fieldType.toString());
			}
		}

		public String toWhereClause(Map<String, DBColumnInfo> colsMap, DBUtil.DBType dbType, int maxDbItem)
		{
			StringBuilder sb = new StringBuilder();
			DBColumnInfo col = colsMap.get(this.fieldName);
			sb.append(DBUtil.dbCol(dbType, col.colName));
			if (val == null)
			{
				if (cond != CompareCondition.NOT_EQUAL)
				{
					sb.append(" is null");
				}
				else
				{
					sb.append(" is not null");
				}
			}
			else
			{
				sb.append(getCondStr(cond));
				sb.append(this.val.toString());
			}
			return sb.toString();
		}

		public boolean isValid(T obj) throws IllegalAccessException, InvocationTargetException
		{
			Object v = this.getter.get(obj);
			if (this.val == null)
			{
				if (cond != CompareCondition.NOT_EQUAL)
				{
					return v == null;
				}
				else
				{
					return v != null;
				}
			}

			if (v == null)
			{
				return false;
			}
			double dVal = (Double)v;
			switch (cond)
			{
			case EQUAL:
				return dVal == this.val;
			case NOT_EQUAL:
				return dVal != this.val;
			case GREATER:
				return dVal > this.val;
			case GREATER_OR_EQUAL:
				return dVal >= this.val;
			case LESS:
				return dVal < this.val;
			case LESS_OR_EQUAL:
				return dVal <= this.val;
			}
			return false;
		}
	}

	public class StrInCondition extends Condition
	{
		private String fieldName;
		private Iterable<String> vals;
		private FieldGetter<T> getter;

		public StrInCondition(String fieldName, Iterable<String> vals) throws NoSuchFieldException
		{
			this.fieldName = fieldName;
			this.vals = vals;
			this.getter = new FieldGetter<T>(cls, fieldName);
		}

		public String toWhereClause(Map<String, DBColumnInfo> colsMap, DBUtil.DBType dbType, int maxDbItem)
		{
			if (DataTools.getSize(this.vals) > maxDbItem)
			{
				return "";
			}
			else
			{
				StringBuilder sb = new StringBuilder();
				DBColumnInfo col = colsMap.get(this.fieldName);
				sb.append(DBUtil.dbCol(dbType, col.colName));
				Iterator<String> it = this.vals.iterator();
				if (it.hasNext())
				{
					sb.append(" in (");
					sb.append(DBUtil.dbStr(dbType, it.next()));
					while (it.hasNext())
					{
						sb.append(", ");
						sb.append(DBUtil.dbStr(dbType, it.next()));
					}
					sb.append(")");
		
				}
				return sb.toString();
			}
		}

		public boolean isValid(T obj) throws IllegalAccessException, InvocationTargetException
		{
			Object v = this.getter.get(obj);
			if (v == null)
			{
				return false;
			}
			String sVal = v.toString();
			Iterator<String> it = this.vals.iterator();
			while (it.hasNext())
			{
				if (sVal.equals(it.next()))
				{
					return true;
				}
			}
			return false;
		}
	}

	public class StrContainsCondition extends Condition
	{
		private String fieldName;
		private String val;
		private FieldGetter<T> getter;

		public StrContainsCondition(String fieldName, String val) throws NoSuchFieldException
		{
			this.fieldName = fieldName;
			this.val = val;
			this.getter = new FieldGetter<T>(cls, fieldName);
		}

		public String toWhereClause(Map<String, DBColumnInfo> colsMap, DBUtil.DBType dbType, int maxDbItem)
		{
			StringBuilder sb = new StringBuilder();
			DBColumnInfo col = colsMap.get(this.fieldName);
			sb.append(DBUtil.dbCol(dbType, col.colName));
			sb.append(" like ");
			sb.append(DBUtil.dbStr(dbType, "%"+this.val+"%"));
			return sb.toString();
		}

		public boolean isValid(T obj) throws IllegalAccessException, InvocationTargetException
		{
			Object v = this.getter.get(obj);
			if (v == null)
			{
				return false;
			}
			String sVal = v.toString();
			return sVal.indexOf(this.val) >= 0;
		}
	}

	public class StrEqualsCondition extends Condition
	{
		private String fieldName;
		private String val;
		private FieldGetter<T> getter;

		public StrEqualsCondition(String fieldName, String val) throws NoSuchFieldException
		{
			this.fieldName = fieldName;
			this.val = val;
			this.getter = new FieldGetter<T>(cls, fieldName);
		}

		public String getFieldName()
		{
			return this.fieldName;
		}

		public String getVal()
		{
			return this.val;
		}
		
		public String toWhereClause(Map<String, DBColumnInfo> colsMap, DBUtil.DBType dbType, int maxDbItem)
		{
			StringBuilder sb = new StringBuilder();
			DBColumnInfo col = colsMap.get(this.fieldName);
			sb.append(DBUtil.dbCol(dbType, col.colName));
			sb.append(" = ");
			sb.append(DBUtil.dbStr(dbType, this.val));
			return sb.toString();
		}

		public boolean isValid(T obj) throws IllegalAccessException, InvocationTargetException
		{
			Object v = this.getter.get(obj);
			if (v == null)
			{
				return false;
			}
			String sVal = v.toString();
			return sVal.equals(this.val);
		}
	}

	public class EnumCondition extends Condition
	{
		private String fieldName;
		private Enum<?> val;
		private EnumType enumType;
		private CompareCondition cond;
		private FieldGetter<T> getter;

		public EnumCondition(String fieldName, Enum<?> val, EnumType enumType, CompareCondition cond) throws NoSuchFieldException
		{
			this.fieldName = fieldName;
			this.val = val;
			this.enumType = enumType;
			this.cond = cond;
			this.getter = new FieldGetter<T>(cls, fieldName);
			if (cond != CompareCondition.EQUAL && cond != CompareCondition.NOT_EQUAL)
			{
				throw new IllegalArgumentException(cond.name()+" is not supported");
			}
			if (val != null && !this.getter.getFieldType().equals(val.getClass()))
			{
				throw new NoSuchFieldException("Field is not the same type as val");
			}
		}

		public String toWhereClause(Map<String, DBColumnInfo> colsMap, DBUtil.DBType dbType, int maxDbItem)
		{
			StringBuilder sb = new StringBuilder();
			DBColumnInfo col = colsMap.get(this.fieldName);
			sb.append(DBUtil.dbCol(dbType, col.colName));
			if (this.cond == CompareCondition.EQUAL)
			{
				if (val == null)
				{
					sb.append(" is null");
				}
				else
				{
					sb.append(" = ");
					if (this.enumType == EnumType.STRING)
					{
						sb.append(DBUtil.dbStr(dbType, this.val.name()));
					}
					else
					{
						sb.append(this.val.ordinal());
					}
				}
			}
			else if (this.cond == CompareCondition.NOT_EQUAL)
			{
				if (val == null)
				{
					sb.append(" is not null");
				}
				else
				{
					sb.append(" <> ");
					if (this.enumType == EnumType.STRING)
					{
						sb.append(DBUtil.dbStr(dbType, this.val.name()));
					}
					else
					{
						sb.append(this.val.ordinal());
					}
				}
			}
			return sb.toString();
		}

		public boolean isValid(T obj) throws IllegalAccessException, InvocationTargetException
		{
			Object v = this.getter.get(obj);
			if (this.val == null)
			{
				if (this.cond == CompareCondition.EQUAL)
				{
					return v == null;
				}
				else
				{
					return v != null;
				}
			}
			else if (this.cond == CompareCondition.EQUAL)
			{
				return v == this.val;
			}
			else
			{
				return v != this.val;
			}
		}
	}

	public class EnumInCondition extends Condition
	{
		private String fieldName;
		private Iterable<Enum<?>> vals;
		private EnumType enumType;
		private FieldGetter<T> getter;

		public EnumInCondition(String fieldName, Iterable<Enum<?>> vals, EnumType enumType) throws NoSuchFieldException
		{
			this.fieldName = fieldName;
			this.vals = vals;
			this.enumType = enumType;
			this.getter = new FieldGetter<T>(cls, fieldName);
			Iterator<Enum<?>> it = vals.iterator();
			Class<?> fieldType = this.getter.getFieldType();

			while (it.hasNext())
			{
				if (!fieldType.equals(it.next().getClass()))
					throw new NoSuchFieldException("Field is not the same type as val");
			}
		}

		public String toWhereClause(Map<String, DBColumnInfo> colsMap, DBUtil.DBType dbType, int maxDbItem)
		{
			if (DataTools.getSize(this.vals) > maxDbItem)
			{
				return "";
			}
			else
			{
				StringBuilder sb = new StringBuilder();
				DBColumnInfo col = colsMap.get(this.fieldName);
				sb.append(DBUtil.dbCol(dbType, col.colName));
				Iterator<Enum<?>> it = vals.iterator();
				if (!it.hasNext())
				{
					sb.append(" is null");
				}
				else
				{
					sb.append(" in (");
					if (this.enumType == EnumType.STRING)
					{
						sb.append(DBUtil.dbStr(dbType, it.next().name()));
						while (it.hasNext())
						{
							sb.append(", ");
							sb.append(DBUtil.dbStr(dbType, it.next().name()));
						}
					}
					else
					{
						sb.append(it.next().ordinal());
						while (it.hasNext())
						{
							sb.append(", ");
							sb.append(it.next().ordinal());
						}
					}
					sb.append(")");
				}
	
				return sb.toString();
			}
		}

		public boolean isValid(T obj) throws IllegalAccessException, InvocationTargetException
		{
			Object v = this.getter.get(obj);
			Iterator<Enum<?>> it = vals.iterator();
			if (v == null)
			{
				return !it.hasNext();
			}
			else
			{
				while (it.hasNext())
				{
					if (it.next() == v)
					{
						return true;
					}
				}
				return false;
			}
		}
	}

	public class BooleanCondition extends Condition
	{
		private String fieldName;
		private boolean val;
		private FieldGetter<T> getter;

		public BooleanCondition(String fieldName, boolean val) throws NoSuchFieldException
		{
			this.fieldName = fieldName;
			this.val = val;
			this.getter = new FieldGetter<T>(cls, fieldName);
			Class<?> fieldType = this.getter.getFieldType();
			if (fieldType.equals(Boolean.class) || fieldType.equals(boolean.class))
			{

			}
			else
			{
				throw new NoSuchFieldException("Not Boolean format: "+fieldType.toString());
			}
		}

		public String toWhereClause(Map<String, DBColumnInfo> colsMap, DBUtil.DBType dbType, int maxDbItem)
		{
			StringBuilder sb = new StringBuilder();
			DBColumnInfo col = colsMap.get(this.fieldName);
			if (!val)
			{
				sb.append("NOT ");
			}
			sb.append(DBUtil.dbCol(dbType, col.colName));
			return sb.toString();
		}

		public boolean isValid(T obj) throws IllegalAccessException, InvocationTargetException
		{
			Object v = this.getter.get(obj);
			return (Boolean)v == this.val;
		}
	}


	public class NotNullCondition extends Condition
	{
		private String fieldName;
		private FieldGetter<T> getter;

		public NotNullCondition(String fieldName) throws NoSuchFieldException
		{
			this.fieldName = fieldName;
			this.getter = new FieldGetter<T>(cls, fieldName);
		}

		public String toWhereClause(Map<String, DBColumnInfo> colsMap, DBUtil.DBType dbType, int maxDbItem)
		{
			StringBuilder sb = new StringBuilder();
			DBColumnInfo col = colsMap.get(this.fieldName);
			sb.append(DBUtil.dbCol(dbType, col.colName));
			sb.append(" is not null");
			return sb.toString();
		}

		public boolean isValid(T obj) throws IllegalAccessException, InvocationTargetException
		{
			Object v = this.getter.get(obj);
			return v != null;
		}
	}
	public class InnerCondition extends Condition
	{
		private QueryConditions<T> innerCond;

		public InnerCondition(QueryConditions<T> innerCond)
		{
			this.innerCond = innerCond;
		}

		public String toWhereClause(Map<String, DBColumnInfo> colsMap, DBUtil.DBType dbType, int maxDbItem)
		{
			List<Condition> clientConditions = new ArrayList<Condition>();
			String whereClause = this.innerCond.toWhereClause(colsMap, dbType, clientConditions, maxDbItem);
			if (clientConditions.size() > 0)
			{
				return "";
			}
			else
			{
				return "("+whereClause+")";
			}
		}

		public boolean isValid(T obj) throws IllegalAccessException, InvocationTargetException
		{
			return this.innerCond.isValid(obj);
		}

		public QueryConditions<T> getConditions()
		{
			return this.innerCond;
		}
	}

	public class OrCondition extends Condition
	{
		public OrCondition()
		{
		}

		public String toWhereClause(Map<String, DBColumnInfo> colsMap, DBUtil.DBType dbType, int maxDbItem)
		{
			return " or ";
		}

		public boolean isValid(T obj)
		{
			return true;
		}
	}

	private Class<T> cls;
	private List<Condition> conditionList;

	public QueryConditions(Class<T> cls)
	{
		this.cls = cls;
		this.conditionList = new ArrayList<Condition>();
	}

	public boolean isValid(T obj) throws IllegalAccessException, InvocationTargetException
	{
		return objectValid(obj, this.conditionList);
	}

	public static <T> boolean objectValid(T obj, List<QueryConditions<T>.Condition> conditionList) throws IllegalAccessException, InvocationTargetException
	{
		boolean ret = true;
		QueryConditions<T>.Condition cond;
		int i = 0;
		int j = conditionList.size();
		while (i < j)
		{
			cond = conditionList.get(i);
			if (cond.getClass().equals(QueryConditions.OrCondition.class))
			{
				if (ret)
					return true;
				ret = true;
			}
			else
			{
				ret = ret && cond.isValid(obj);
			}
			i++;
		}
		return ret;
	}

	public QueryConditions<T> timeBetween(String fieldName, Timestamp t1, Timestamp t2) throws NoSuchFieldException
	{
		this.conditionList.add(new TimeBetweenCondition(fieldName, t1, t2));
		return this;
	}

	public QueryConditions<T> or()
	{
		if (this.conditionList.size() == 0)
		{
			throw new IllegalStateException("Or must have at least 1 condition before calling");
		}
		this.conditionList.add(new OrCondition());
		return this;
	}

	public QueryConditions<T> innerCond(QueryConditions<T> cond)
	{
		this.conditionList.add(new InnerCondition(cond));
		return this;
	}

	public QueryConditions<T> intEquals(String fieldName, Integer val) throws NoSuchFieldException
	{
		this.conditionList.add(new IntCondition(fieldName, val, CompareCondition.EQUAL));
		return this;
	}

	public QueryConditions<T> intIn(String fieldName, Iterable<Integer> vals) throws NoSuchFieldException
	{
		if (vals.iterator().hasNext())
		{
			this.conditionList.add(new IntInCondition(fieldName, vals));
		}
		else
		{
			this.conditionList.add(new IntCondition(fieldName, null, CompareCondition.EQUAL));
		}
		return this;
	}

	public QueryConditions<T> doubleGE(String fieldName, double val) throws NoSuchFieldException
	{
		this.conditionList.add(new DoubleCondition(fieldName, val, CompareCondition.GREATER_OR_EQUAL));
		return this;
	}

	public QueryConditions<T> doubleLE(String fieldName, double val) throws NoSuchFieldException
	{
		this.conditionList.add(new DoubleCondition(fieldName, val, CompareCondition.LESS_OR_EQUAL));
		return this;
	}

	public QueryConditions<T> strIn(String fieldName, Iterable<String> vals) throws NoSuchFieldException
	{
		this.conditionList.add(new StrInCondition(fieldName, vals));
		return this;
	}

	public QueryConditions<T> strContains(String fieldName, String val) throws NoSuchFieldException
	{
		this.conditionList.add(new StrContainsCondition(fieldName, val));
		return this;
	}

	public QueryConditions<T> strEquals(String fieldName, String val) throws NoSuchFieldException
	{
		this.conditionList.add(new StrEqualsCondition(fieldName, val));
		return this;
	}

	public QueryConditions<T> boolEquals(String fieldName, boolean val) throws NoSuchFieldException
	{
		this.conditionList.add(new BooleanCondition(fieldName, val));
		return this;
	}

	private EnumType parseEnumType(String fieldName) throws NoSuchFieldException
	{
		EnumType enumType = EnumType.ORDINAL;
		Field field = cls.getDeclaredField(fieldName);
		Annotation anns[] = field.getAnnotations();
		int i = 0;
		int j = anns.length;
		while (i < j)
		{
			if (anns[i].annotationType().equals(Enumerated.class))
			{
				Enumerated en = (Enumerated)anns[i];
				enumType = en.value();
			}
			i++;
		}
		return enumType;
	}

	private EnumCondition parseEnumCondition(String fieldName, Enum<?> val, CompareCondition cond) throws NoSuchFieldException
	{
		return new EnumCondition(fieldName, val, parseEnumType(fieldName), cond);
	}

	public QueryConditions<T> enumEquals(String fieldName, Enum<?> val) throws NoSuchFieldException
	{
		this.conditionList.add(parseEnumCondition(fieldName, val, CompareCondition.EQUAL));
		return this;
	}

	public QueryConditions<T> enumNotEquals(String fieldName, Enum<?> val) throws NoSuchFieldException
	{
		this.conditionList.add(parseEnumCondition(fieldName, val, CompareCondition.NOT_EQUAL));
		return this;
	}

	public QueryConditions<T> enumIn(String fieldName, Iterable<Enum<?>> vals) throws NoSuchFieldException
	{
		this.conditionList.add(new EnumInCondition(fieldName, vals, parseEnumType(fieldName)));
		return this;
	}

	public QueryConditions<T> notNull(String fieldName) throws NoSuchFieldException
	{
		this.conditionList.add(new NotNullCondition(fieldName));
		return this;
	}

	public String toWhereClause(Map<String, DBColumnInfo> colsMap, DBUtil.DBType dbType, List<Condition> clientConditions, int maxDbItem)
	{
		StringBuilder sb = new StringBuilder();
		boolean hasOr = false;
		int i = 0;
		int j = this.conditionList.size();
		while (i < j)
		{
			Condition condition = this.conditionList.get(i);
			if (condition.getClass() == OrCondition.class)
			{
				hasOr = true;
				break;
			}
			i++;
		}

		if (hasOr)
		{
			int splitType = 1;
			i = 0;
			j = this.conditionList.size();
			while (i < j)
			{
				Condition condition = this.conditionList.get(i);
				if (condition.getClass() == OrCondition.class)
				{
					if (splitType != 0)
					{

					}
					else
					{
						sb.append(")");
						splitType = 2;
					}
				}
				else
				{
					String whereClause = condition.toWhereClause(colsMap, dbType, maxDbItem);
					if (whereClause.equals(""))
					{
						clientConditions.add(condition);
					}
					else
					{
						if (splitType == 2)
						{
							sb.append(" or (");
							splitType = 0;
						}
						else if (splitType != 0)
						{
							sb.append("(");
							splitType = 0;
						}
						else
						{
							sb.append(" and ");
						}
						sb.append(whereClause);
					}
				}
				i++;
			}
			if (splitType == 0)
			{
				sb.append(")");
			}
		}
		else
		{
			boolean hasCond = false;
			i = 0;
			j = this.conditionList.size();
			while (i < j)
			{
				Condition condition = this.conditionList.get(i);
				String whereClause = condition.toWhereClause(colsMap, dbType, maxDbItem);
				if (whereClause.equals(""))
				{
					clientConditions.add(condition);
				}
				else
				{
					if (hasCond)
					{
						sb.append(" and ");
					}
					sb.append(whereClause);
					hasCond = true;
				}
				i++;
			}
		}
		return sb.toString();
	}

	public int size()
	{
		return this.conditionList.size();
	}

	public Condition get(int index)
	{
		return this.conditionList.get(index);
	}

	public List<Condition> toList()
	{
		return this.conditionList;
	}

	public static String getCondStr(CompareCondition cond)
	{
		switch (cond)
		{
		case EQUAL:
			return " = ";
		case GREATER:
			return " > ";
		case LESS:
			return " < ";
		case GREATER_OR_EQUAL:
			return " >= ";
		case LESS_OR_EQUAL:
			return " <= ";
		case NOT_EQUAL:
			return " <> ";
		}
		return "";
	}
}
