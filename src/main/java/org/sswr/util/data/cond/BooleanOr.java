package org.sswr.util.data.cond;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.sswr.util.data.ObjectGetter;
import org.sswr.util.data.StringBuilderUTF8;
import org.sswr.util.db.DBColumnInfo;
import org.sswr.util.db.DBUtil.DBType;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class BooleanOr extends BooleanObject
{
	private List<BooleanObject> orList;

	public BooleanOr()
	{
		this.orList = new ArrayList<BooleanObject>();
	}

	public @Nonnull ConditionObject clone()
	{
		BooleanOr cond = new BooleanOr();
		int i = 0;
		int j = this.orList.size();
		while (i < j)
		{
			cond.orList.add(((BooleanObject)this.orList.get(i).clone()));
			i++;
		}
		return cond;
	}

	public @Nonnull String toWhereClause(@Nullable Map<String, DBColumnInfo> colsMap, @Nonnull DBType dbType, byte tzQhr, int maxDBItem) throws IllegalAccessException
	{
		BooleanObject obj;
		int i = 0;
		int j = this.orList.size();
		if (j == 0)
			throw new IllegalAccessException("orList is empty");
		StringBuilderUTF8 sb = new StringBuilderUTF8();
		while (i < j)
		{
			obj = this.orList.get(i);
			if (i > 0)
			{
				sb.append(" or ");
			}
			if ((obj instanceof BooleanAnd) && ((BooleanAnd)obj).getCount() > 1)
			{
				sb.appendUTF8Char((byte)'(');
				sb.append(obj.toWhereClause(colsMap, dbType, tzQhr, maxDBItem));
				sb.appendUTF8Char((byte)')');
			}
			else
			{
				sb.append(obj.toWhereClause(colsMap, dbType, tzQhr, maxDBItem));
			}
			i++;
		}
		return sb.toString();
	}

	public boolean canWhereClause(int maxDBItem)
	{
		int i = this.orList.size();
		if (i == 0)
			return false;
		while (i-- > 0)
		{
			if (!this.orList.get(i).canWhereClause(maxDBItem))
				return false;
		}
		return true;
	}

	public void getFieldList(@Nonnull List<String> fieldList)
	{
		int i = this.orList.size();
		while (i-- > 0)
		{
			this.orList.get(i).getFieldList(fieldList);
		}
	}
	public boolean eval(@Nonnull ObjectGetter getter) throws IllegalAccessException, InvocationTargetException
	{
		boolean v;
		int i = 0;
		int j = this.orList.size();
		if (j == 0)
			return false;
		while (i < j)
		{
			v = this.orList.get(i).eval(getter);
			if (v)
			{
				return true;
			}
			i++;
		}
		return false;
	}

	public int getCount()
	{
		return this.orList.size();
	}

	public void addOr(@Nonnull BooleanObject obj)
	{
		this.orList.add(obj);
	}
}