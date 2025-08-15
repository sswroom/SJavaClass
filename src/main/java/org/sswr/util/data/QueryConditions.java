package org.sswr.util.data;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.Timestamp;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import org.sswr.util.basic.CompareCondition;
import org.sswr.util.data.cond.BooleanAnd;
import org.sswr.util.data.cond.BooleanCondition;
import org.sswr.util.data.cond.BooleanObject;
import org.sswr.util.data.cond.BooleanOr;
import org.sswr.util.data.cond.EnumCondition;
import org.sswr.util.data.cond.EnumInCondition;
import org.sswr.util.data.cond.Float64Object;
import org.sswr.util.data.cond.Int32InCondition;
import org.sswr.util.data.cond.Int32Object;
import org.sswr.util.data.cond.Int64Object;
import org.sswr.util.data.cond.NotNullCondition;
import org.sswr.util.data.cond.NumberCondition;
import org.sswr.util.data.cond.NumberField;
import org.sswr.util.data.cond.NumberObject;
import org.sswr.util.data.cond.StringContainsCondition;
import org.sswr.util.data.cond.StringEqualsCondition;
import org.sswr.util.data.cond.StringInCondition;
import org.sswr.util.data.cond.StringNotInCondition;
import org.sswr.util.data.cond.TimeBetweenCondition;
import org.sswr.util.data.cond.TimeCondition;
import org.sswr.util.data.cond.TimeField;
import org.sswr.util.data.cond.TimeObject;
import org.sswr.util.data.cond.TimestampObject;
import org.sswr.util.db.DBColumnInfo;
import org.sswr.util.db.DBUtil;

public class QueryConditions
{
	private @Nonnull BooleanObject cond;
	private @Nonnull BooleanAnd andCond;
	private @Nullable BooleanOr orCond;

	public QueryConditions()
	{
		this.andCond = new BooleanAnd();
		this.cond = this.andCond;
		this.orCond = null;
	}

	public boolean isValid(@Nonnull ObjectGetter getter) throws IllegalAccessException, InvocationTargetException
	{
		return this.cond.eval(getter);
	}

	public @Nonnull String toWhereClause(@Nullable Map<String, DBColumnInfo> colsMap, @Nonnull DBUtil.DBType dbType, byte tzQhr, int maxDbItem, @Nonnull List<BooleanObject> clientConditions)
	{
		try
		{
			if (this.andCond == this.cond)
			{
				return this.andCond.toWhereClauseOrClient(colsMap, dbType, tzQhr, maxDbItem, clientConditions);
			}
			else
			{
				return this.cond.toWhereClause(colsMap, dbType, tzQhr, maxDbItem);
			}
		}
		catch (IllegalAccessException ex)
		{
			clientConditions.add(this.cond);
			return "";
		}
	}

	public boolean canWhereClause(int maxDbItem)
	{
		return this.cond.canWhereClause(maxDbItem);
	}

	public void getFieldList(@Nonnull List<String> fieldList)
	{
		this.cond.getFieldList(fieldList);
	}

	public @Nonnull BooleanObject getRootCond()
	{
		return this.cond;
	}

	public @Nonnull QueryConditions timeBetween(@Nonnull String fieldName, @Nonnull Timestamp t1, @Nonnull Timestamp t2) throws NoSuchFieldException
	{
		this.andCond.addAnd(new TimeBetweenCondition(fieldName, t1, t2));
		return this;
	}

	public @Nonnull QueryConditions timeCompare(@Nonnull String fieldName, @Nonnull CompareCondition cond, @Nonnull Timestamp t) throws NoSuchFieldException
	{
		this.andCond.addAnd(new TimeCondition(tsField(fieldName), timeObj(t), cond));
		return this;
	}

	public @Nonnull QueryConditions timeBefore(@Nonnull String fieldName, @Nonnull Timestamp t)
	{
		this.andCond.addAnd(new TimeCondition(tsField(fieldName), timeObj(t), CompareCondition.Less));
		return this;
	}

	public @Nonnull QueryConditions timeOnOrAfter(@Nonnull String fieldName, @Nonnull Timestamp t)
	{
		this.andCond.addAnd(new TimeCondition(tsField(fieldName), timeObj(t), CompareCondition.GreaterOrEqual));
		return this;
	}
	
	public @Nonnull QueryConditions or()
	{
		BooleanOr cond = this.orCond;
		if (cond == null)
		{
			cond = new BooleanOr();
			this.orCond = cond;
			this.cond = cond;
			cond.addOr(this.andCond);
		}
		this.andCond = new BooleanAnd();
		cond.addOr(this.andCond);
		return this;
	}

	public @Nonnull QueryConditions and(@Nonnull BooleanObject innerCond)
	{
		this.andCond.addAnd(innerCond);
		return this;
	}

	public @Nonnull QueryConditions int32Equals(@Nonnull String fieldName, int val)
	{
		this.andCond.addAnd(new NumberCondition(numField(fieldName), int32Obj(val), CompareCondition.Equal));
		return this;
	}

	public @Nonnull QueryConditions int32GE(@Nonnull String fieldName, int val)
	{
		this.andCond.addAnd(new NumberCondition(numField(fieldName), int32Obj(val), CompareCondition.GreaterOrEqual));
		return this;
	}

	public @Nonnull QueryConditions int32LE(@Nonnull String fieldName, int val)
	{
		this.andCond.addAnd(new NumberCondition(numField(fieldName), int32Obj(val), CompareCondition.LessOrEqual));
		return this;
	}

	public @Nonnull QueryConditions int32GT(@Nonnull String fieldName, int val)
	{
		this.andCond.addAnd(new NumberCondition(numField(fieldName), int32Obj(val), CompareCondition.Greater));
		return this;
	}

	public @Nonnull QueryConditions int32LT(@Nonnull String fieldName, int val)
	{
		this.andCond.addAnd(new NumberCondition(numField(fieldName), int32Obj(val), CompareCondition.Less));
		return this;
	}

	public @Nonnull QueryConditions int32In(@Nonnull String fieldName, @Nonnull Iterable<Integer> vals)
	{
		if (vals.iterator().hasNext())
		{
			this.andCond.addAnd(new Int32InCondition(fieldName, vals));
		}
		return this;
	}

	public @Nonnull QueryConditions int64Equals(@Nonnull String fieldName, long val)
	{
		this.andCond.addAnd(new NumberCondition(numField(fieldName), int64Obj(val), CompareCondition.Equal));
		return this;
	}

	public @Nonnull QueryConditions int64GE(@Nonnull String fieldName, long val)
	{
		this.andCond.addAnd(new NumberCondition(numField(fieldName), int64Obj(val), CompareCondition.GreaterOrEqual));
		return this;
	}

	public @Nonnull QueryConditions int64LE(@Nonnull String fieldName, long val)
	{
		this.andCond.addAnd(new NumberCondition(numField(fieldName), int64Obj(val), CompareCondition.LessOrEqual));
		return this;
	}

	public @Nonnull QueryConditions int64GT(@Nonnull String fieldName, long val)
	{
		this.andCond.addAnd(new NumberCondition(numField(fieldName), int64Obj(val), CompareCondition.Greater));
		return this;
	}

	public @Nonnull QueryConditions int64LT(@Nonnull String fieldName, long val)
	{
		this.andCond.addAnd(new NumberCondition(numField(fieldName), int64Obj(val), CompareCondition.Less));
		return this;
	}

	public @Nonnull QueryConditions doubleEquals(@Nonnull String fieldName, double val)
	{
		this.andCond.addAnd(new NumberCondition(numField(fieldName), doubleObj(val), CompareCondition.Equal));
		return this;
	}

	public @Nonnull QueryConditions doubleGE(@Nonnull String fieldName, double val)
	{
		this.andCond.addAnd(new NumberCondition(numField(fieldName), doubleObj(val), CompareCondition.GreaterOrEqual));
		return this;
	}
	
	public @Nonnull QueryConditions doubleLE(@Nonnull String fieldName, double val)
	{
		this.andCond.addAnd(new NumberCondition(numField(fieldName), doubleObj(val), CompareCondition.LessOrEqual));
		return this;
	}

	public @Nonnull QueryConditions doubleGT(@Nonnull String fieldName, double val)
	{
		this.andCond.addAnd(new NumberCondition(numField(fieldName), doubleObj(val), CompareCondition.Greater));
		return this;
	}
	
	public @Nonnull QueryConditions doubleLT(@Nonnull String fieldName, double val)
	{
		this.andCond.addAnd(new NumberCondition(numField(fieldName), doubleObj(val), CompareCondition.Less));
		return this;
	}

	public @Nonnull QueryConditions strIn(@Nonnull String fieldName, @Nonnull Iterable<String> vals) throws NoSuchFieldException
	{
		this.andCond.addAnd(new StringInCondition(fieldName, vals));
		return this;
	}

	public @Nonnull QueryConditions strNotIn(@Nonnull String fieldName, @Nonnull Iterable<String> vals) throws NoSuchFieldException
	{
		this.andCond.addAnd(new StringNotInCondition(fieldName, vals));
		return this;
	}

	public @Nonnull QueryConditions strContains(@Nonnull String fieldName, @Nonnull String val) throws NoSuchFieldException
	{
		this.andCond.addAnd(new StringContainsCondition(fieldName, val));
		return this;
	}

	@Nonnull
	public QueryConditions strEquals(@Nonnull String fieldName, @Nonnull String val) throws NoSuchFieldException
	{
		this.andCond.addAnd(new StringEqualsCondition(fieldName, val));
		return this;
	}

	@Nonnull
	public QueryConditions boolEquals(@Nonnull String fieldName, boolean val) throws NoSuchFieldException
	{
		this.andCond.addAnd(new BooleanCondition(fieldName, val));
		return this;
	}

	@Nonnull
	private EnumType parseEnumType(@Nonnull Class<?> cls, @Nonnull String fieldName) throws NoSuchFieldException
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

	private @Nonnull EnumCondition parseEnumCondition(@Nonnull String fieldName, @Nonnull Enum<?> val, @Nonnull CompareCondition cond) throws NoSuchFieldException
	{
		return new EnumCondition(fieldName, val, parseEnumType(val.getClass(), fieldName), cond);
	}

	public @Nonnull QueryConditions enumEquals(@Nonnull String fieldName, @Nonnull Enum<?> val) throws NoSuchFieldException
	{
		this.andCond.addAnd(parseEnumCondition(fieldName, val, CompareCondition.Equal));
		return this;
	}

	public @Nonnull QueryConditions enumNotEquals(@Nonnull String fieldName, @Nonnull Enum<?> val) throws NoSuchFieldException
	{
		this.andCond.addAnd(parseEnumCondition(fieldName, val, CompareCondition.NotEqual));
		return this;
	}

	public @Nonnull QueryConditions enumIn(@Nonnull String fieldName, @Nonnull Class<?> enumClass, @Nonnull Iterable<Enum<?>> vals) throws NoSuchFieldException
	{
		this.andCond.addAnd(new EnumInCondition(fieldName, vals, parseEnumType(enumClass, fieldName)));
		return this;
	}
	
	public @Nonnull QueryConditions notNull(@Nonnull String fieldName) throws NoSuchFieldException
	{
		this.andCond.addAnd(new NotNullCondition(fieldName));
		return this;
	}

	public @Nonnull List<BooleanObject> toList()
	{
		return List.of(cond);
	}

	public static @Nonnull QueryConditions create()
	{
		return new QueryConditions();
	}

	public static @Nonnull String compareConditionGetStr(@Nonnull CompareCondition cond)
	{
		switch (cond)
		{
		case Equal:
			return " = ";
		case Greater:
			return " > ";
		case Less:
			return " < ";
		case GreaterOrEqual:
			return " >= ";
		case LessOrEqual:
			return " <= ";
		case NotEqual:
			return " <> ";
		case Unknown:
			return "";
		}
		return "";
	}

	public static boolean objectValid(@Nonnull ObjectGetter getter, @Nonnull Iterable<BooleanObject> conditionList) throws IllegalAccessException, InvocationTargetException
	{
		boolean ret = true;
		BooleanObject cond;
		Iterator<BooleanObject> it = conditionList.iterator();
		while (it.hasNext())
		{
			cond = it.next();
			ret = ret && cond.eval(getter);
		}
		return ret;
	}

	private @Nonnull TimeObject tsField(@Nonnull String fieldName)
	{
		return new TimeField(fieldName);
	}

	private @Nonnull NumberObject numField(@Nonnull String fieldName)
	{
		return new NumberField(fieldName);
	}

	private @Nonnull TimeObject timeObj(@Nonnull Timestamp val)
	{
		return new TimestampObject(val);
	}

	private @Nonnull NumberObject int32Obj(int val)
	{
		return new Int32Object(val);
	}

	private @Nonnull NumberObject int64Obj(long val)
	{
		return new Int64Object(val);
	}

	private @Nonnull NumberObject doubleObj(double val)
	{
		return new Float64Object(val);
	}

}
