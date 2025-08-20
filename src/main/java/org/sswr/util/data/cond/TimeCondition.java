package org.sswr.util.data.cond;

import java.lang.reflect.InvocationTargetException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import org.sswr.util.basic.CompareCondition;
import org.sswr.util.data.ObjectGetter;
import org.sswr.util.data.StringBuilderUTF8;
import org.sswr.util.db.DBColumnInfo;
import org.sswr.util.db.DBUtil.DBType;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class TimeCondition extends BooleanObject
{
	private TimeObject left;
	private TimeObject right;
	private CompareCondition cond;

	private static boolean tsCompare(@Nonnull Timestamp left, @Nonnull Timestamp right, @Nonnull CompareCondition cond)
	{
		switch (cond)
		{
		case Equal:
			return left.compareTo(right) == 0;
		case NotEqual:
			return left.compareTo(right) != 0;
		case Greater:
			return left.compareTo(right) > 0;
		case GreaterOrEqual:
			return left.compareTo(right) >= 0;
		case Less:
			return left.compareTo(right) < 0;
		case LessOrEqual:
			return left.compareTo(right) <= 0;
		case Unknown:
		default:
			return false;
		}
	}

	public TimeCondition(@Nonnull TimeObject left, @Nonnull TimeObject right, @Nonnull CompareCondition cond)
	{
		this.left = left;
		this.right = right;
		this.cond = cond;
	}

	public @Nonnull ConditionObject clone()
	{
		return new TimeCondition((TimeObject)this.left.clone(), (TimeObject)this.right.clone(), this.cond);
	}

	public @Nonnull String toWhereClause(@Nullable Map<String, DBColumnInfo> colsMap, @Nonnull DBType dbType, byte tzQhr, int maxDBItem) throws IllegalAccessException
	{
		StringBuilderUTF8 sb = new StringBuilderUTF8();
		sb.append(this.left.toWhereClause(colsMap, dbType, tzQhr, maxDBItem));
		switch (this.cond)
		{
		case Equal:
			sb.append(" = ");
			break;
		case NotEqual:
			sb.append(" <> ");
			break;
		case Greater:
			sb.append(" > ");
			break;
		case GreaterOrEqual:
			sb.append(" >= ");
			break;
		case Less:
			sb.append(" < ");
			break;
		case LessOrEqual:
			sb.append(" <= ");
			break;
		case Unknown:
		default:
			throw new IllegalAccessException("Unsupported condition type: "+cond.toString());
		}
		sb.append(this.right.toWhereClause(colsMap, dbType, tzQhr, maxDBItem));
		return sb.toString();		
	}

	public boolean canWhereClause(int maxDBItem)
	{
		return this.left.canWhereClause(maxDBItem) && this.right.canWhereClause(maxDBItem);
	}

	public void getFieldList(@Nonnull List<String> fieldList)
	{
		this.left.getFieldList(fieldList);
		this.right.getFieldList(fieldList);
	}

	public boolean eval(@Nonnull ObjectGetter getter) throws IllegalAccessException, InvocationTargetException
	{
		Timestamp leftVal = this.left.eval(getter);
		Timestamp rightVal = this.right.eval(getter);
		if (leftVal == null || rightVal == null)
			throw new IllegalAccessError("Error in getting date time value");
		return tsCompare(leftVal, rightVal, this.cond);
	}

	public @Nonnull TimeObject getLeft()
	{
		return this.left;
	}

	public @Nonnull TimeObject getRight()
	{
		return this.right;
	}

	public @Nonnull CompareCondition getCond()
	{
		return this.cond;
	}	
}
