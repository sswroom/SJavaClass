package org.sswr.util.data.cond;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

import org.sswr.util.basic.CompareCondition;
import org.sswr.util.data.ObjectGetter;
import org.sswr.util.data.StringBuilderUTF8;
import org.sswr.util.db.DBColumnInfo;
import org.sswr.util.db.DBUtil;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class NumberCondition extends BooleanObject
{
	private NumberObject left;
	private NumberObject right;
	private CompareCondition cond;

	public static boolean intCompare(long left, long right, CompareCondition cond)
	{
		switch (cond)
		{
		case Equal:
			return left == right;
		case NotEqual:
			return left != right;
		case Greater:
			return left > right;
		case GreaterOrEqual:
			return left >= right;
		case Less:
			return left < right;
		case LessOrEqual:
			return left <= right;
		case Unknown:
		default:
			return false;
		}
	}

	public static boolean floatCompare(double left, double right, CompareCondition cond)
	{
		switch (cond)
		{
		case Equal:
			return left == right;
		case NotEqual:
			return left != right;
		case Greater:
			return left > right;
		case GreaterOrEqual:
			return left >= right;
		case Less:
			return left < right;
		case LessOrEqual:
			return left <= right;
		case Unknown:
		default:
			return false;
		}
	}

	public NumberCondition(@Nonnull NumberObject left, @Nonnull NumberObject right, @Nonnull CompareCondition cond)
	{
		this.left = left;
		this.right = right;
		this.cond = cond;
	}

	public @Nonnull ConditionObject clone()
	{
		return new NumberCondition((NumberObject)this.left.clone(), (NumberObject)this.right.clone(), this.cond);
	}

	public @Nonnull String toWhereClause(@Nullable Map<String, DBColumnInfo> colsMap, @Nonnull DBUtil.DBType dbType, byte tzQhr, int maxDbItem) throws IllegalAccessException
	{
		StringBuilderUTF8 sb = new StringBuilderUTF8();
		sb.append(this.left.toWhereClause(colsMap, dbType, tzQhr, maxDbItem));
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
		sb.append(this.right.toWhereClause(colsMap, dbType, tzQhr, maxDbItem));
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
		if (this.left.getNumberType(getter) == NumberType.F64 || this.right.getNumberType(getter) == NumberType.F64)
		{
			double leftVal = this.left.evalDouble(getter);
			Double rightVal = this.right.evalDouble(getter);
			return floatCompare(leftVal, rightVal, this.cond);
		}
		else
		{
			long leftVal = this.left.evalInt(getter);
			long rightVal = this.right.evalInt(getter);
			return intCompare(leftVal, rightVal, this.cond);
		}
	}

	public @Nonnull NumberObject getLeft()
	{
		return this.left;
	}

	public @Nonnull NumberObject getRight()
	{
		return this.right;
	}

	public @Nonnull CompareCondition getCond()
	{
		return this.cond;
	}
}
