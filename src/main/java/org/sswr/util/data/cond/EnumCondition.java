package org.sswr.util.data.cond;

import java.text.AttributedCharacterIterator.Attribute;
import java.util.Map;

import org.sswr.util.basic.CompareCondition;
import org.sswr.util.data.QueryConditions;
import org.sswr.util.db.DBColumnInfo;
import org.sswr.util.db.DBUtil;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.EnumType;

public class EnumCondition extends FieldCondition
{
	private @Nonnull Class<?> cls;
	private Enum<?> val;
	private @Nullable EnumType enumType;
	private CompareCondition cond;

	public EnumCondition(@Nonnull Class<?> cls, @Nonnull String fieldName, @Nonnull Enum<?> val, @Nonnull CompareCondition cond) throws NoSuchFieldException
	{
		super(fieldName);
		this.cls = cls;
		this.val = val;
		this.enumType = QueryConditions.parseEnumType(cls, fieldName);
		this.cond = cond;
		if (cond != CompareCondition.Equal && cond != CompareCondition.NotEqual)
		{
			throw new IllegalArgumentException(cond.name()+" is not supported");
		}
	}

	public @Nonnull ConditionObject clone()
	{
		try
		{
			return new EnumCondition(cls, fieldName, val, cond);
		}
		catch (NoSuchFieldException ex)
		{
			throw new IllegalArgumentException(ex);
		}
	}

	@Nonnull
	public String toWhereClause(@Nullable Map<String, DBColumnInfo> colsMap, @Nonnull DBUtil.DBType dbType, byte tzQhr, int maxDbItem)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(DBUtil.dbCol(dbType, toFieldName(colsMap, fieldName)));
		AttributeConverter<Object, Object> converter = null;
		DBColumnInfo col;
		if ((col = colsMap.get(this.fieldName)) != null)
		{
			converter = col.converter;
		}

		if (this.cond == CompareCondition.Equal)
		{
			if (val == null)
			{
				sb.append(" is null");
			}
			else
			{
				sb.append(" = ");
				if (converter == null)
				{
					if (this.enumType == EnumType.STRING)
					{
						sb.append(DBUtil.dbStr(dbType, this.val.name()));
					}
					else
					{
						sb.append(this.val.ordinal());
					}
				}
				else
				{
					Object o = converter.convertToDatabaseColumn(this.val);
					if (o instanceof String)
					{
						sb.append(DBUtil.dbStr(dbType, (String)o));
					}
					else if (o instanceof Integer)
					{
						sb.append((Integer)o);
					}
					else
					{
						System.out.println("EnumCondition: Unsupported converter return type "+o.getClass().toString());
						sb.append(DBUtil.dbStr(dbType, o.toString()));
					}
				}
			}
		}
		else if (this.cond == CompareCondition.NotEqual)
		{
			if (val == null)
			{
				sb.append(" is not null");
			}
			else
			{
				sb.append(" <> ");
				if (converter == null)
				{
					if (this.enumType == EnumType.STRING)
					{
						sb.append(DBUtil.dbStr(dbType, this.val.name()));
					}
					else
					{
						sb.append(this.val.ordinal());
					}
				}
				else
				{
					Object o = converter.convertToDatabaseColumn(this.val);
					if (o instanceof String)
					{
						sb.append(DBUtil.dbStr(dbType, (String)o));
					}
					else if (o instanceof Integer)
					{
						sb.append((Integer)o);
					}
					else
					{
						System.out.println("EnumCondition: Unsupported converter return type "+o.getClass().toString());
						sb.append(DBUtil.dbStr(dbType, o.toString()));
					}
				}
			}
		}
		return sb.toString();
	}

	public boolean canWhereClause(int maxDBItem)
	{
		return true;
	}

	public boolean testValid(@Nullable Object v)
	{
		if (this.val == null)
		{
			if (this.cond == CompareCondition.Equal)
			{
				return v == null;
			}
			else
			{
				return v != null;
			}
		}
		else if (this.cond == CompareCondition.Equal)
		{
			return v == this.val;
		}
		else
		{
			return v != this.val;
		}
	}
}
