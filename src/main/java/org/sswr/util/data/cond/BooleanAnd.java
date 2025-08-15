package org.sswr.util.data.cond;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.sswr.util.data.ObjectGetter;
import org.sswr.util.data.StringBuilderUTF8;
import org.sswr.util.db.DBColumnInfo;
import org.sswr.util.db.DBUtil;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class BooleanAnd extends BooleanObject
{
	private List<BooleanObject> andList;

	public BooleanAnd()
	{
		this.andList = new ArrayList<BooleanObject>();
	}

	public @Nonnull ConditionObject clone()
	{
		BooleanAnd cond;
		cond = new BooleanAnd();
		int i = 0;
		int j = this.andList.size();
		while (i < j)
		{
			cond.andList.add((BooleanObject)this.andList.get(i).clone());
			i++;
		}
		return cond;
	}

	public @Nonnull String toWhereClause(@Nullable Map<String, DBColumnInfo> colsMap, @Nonnull DBUtil.DBType dbType, byte tzQhr, int maxDbItem) throws IllegalAccessException
	{
		int i = 0;
		int j = this.andList.size();
		if (j == 0)
			throw new IllegalAccessException("BooleanAnd not object inside");
		while (i < j)
		{
			if (!this.andList.get(i).canWhereClause(maxDbItem))
				throw new IllegalAccessException("BooleanAnd where clause not supported");
			i++;
		}
		StringBuilderUTF8 sb = new StringBuilderUTF8();
		BooleanObject cond;
		i = 0;
		while (i < j)
		{
			if (i > 0)
				sb.append(" and ");
			cond = this.andList.get(i);
			if ((cond instanceof BooleanOr) && ((BooleanOr)cond).getCount() > 1)
			{
				sb.appendUTF8Char((byte)'(');
				sb.append(cond.toWhereClause(colsMap, dbType, tzQhr, maxDbItem));
				sb.appendUTF8Char((byte)')');
			}
			else
			{
				sb.append(cond.toWhereClause(colsMap, dbType, tzQhr, maxDbItem));
			}
			i++;
		}
		return sb.toString();
	}

	public @Nonnull String toWhereClauseOrClient(@Nullable Map<String, DBColumnInfo> colsMap, @Nonnull DBUtil.DBType dbType, byte tzQhr, int maxDbItem, @Nonnull List<BooleanObject> clientConditions) throws IllegalAccessException
	{
		int i = 0;
		int j = this.andList.size();
		if (j == 0)
			throw new IllegalAccessException("BooleanAnd not object inside");
		while (i < j)
		{
			if (!this.andList.get(i).canWhereClause(maxDbItem))
				throw new IllegalAccessException("BooleanAnd where clause not supported");
			i++;
		}
		BooleanObject cond;
		StringBuilderUTF8 sb = new StringBuilderUTF8();
		String sTmp;
		boolean found = false;
		i = 0;
		while (i < j)
		{
			cond = this.andList.get(i);
			try
			{
				sTmp = cond.toWhereClause(colsMap, dbType, tzQhr, maxDbItem);
				if (found)
					sb.append(" and ");
				found = true;
				if ((cond instanceof BooleanOr) && ((BooleanOr)cond).getCount() > 1)
				{
					sb.appendUTF8Char((byte)'(');
					sb.append(sTmp);
					sb.appendUTF8Char((byte)')');
				}
				else
				{
					sb.append(sTmp);
				}
			}
			catch (IllegalAccessException ex)
			{
				clientConditions.add(cond);
			}
			i++;
		}
		return sb.toString();
	}
	public boolean canWhereClause(int maxDBItem)
	{
		int i = 0;
		int j = this.andList.size();
		if (j == 0)
			return false;
		while (i < j)
		{
			if (!this.andList.get(i).canWhereClause(maxDBItem))
				return false;
			i++;
		}
		return true;
	}

	public void getFieldList(@Nonnull List<String> fieldList)
	{
		int i = 0;
		int j = this.andList.size();
		while (i < j)
		{
			this.andList.get(i).getFieldList(fieldList);
			i++;
		}
	}

	public boolean eval(@Nonnull ObjectGetter getter) throws IllegalAccessException, InvocationTargetException
	{
		int i = 0;
		int j = this.andList.size();
		if (j == 0)
			return false;
		boolean v;
		while (i < j)
		{
			v = this.andList.get(i).eval(getter);
			if (!v)
			{
				return false;
			}
			i++;
		}
		return true;
	}

	public int getCount()
	{
		return this.andList.size();
	}

	public void addAnd(@Nonnull BooleanObject obj)
	{
		this.andList.add(obj);
	}
}