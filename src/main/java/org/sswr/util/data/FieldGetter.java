package org.sswr.util.data;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class FieldGetter<T>
{
	private Field fields[];
	private Method getters[];

	public FieldGetter(Class<?> cls, String fieldName) throws NoSuchFieldException
	{
		String fieldNames[] = StringUtil.split(fieldName, ".");
		int i = 0;
		int j = fieldNames.length;
		this.fields = new Field[j];
		this.getters = new Method[j];
		while (i < j)
		{
			try
			{
				this.fields[i] = cls.getDeclaredField(fieldNames[i]);
				this.getters[i] = ReflectTools.findGetter(this.fields[i]);
				cls = this.fields[i].getType();
			}
			catch (NoSuchFieldException ex)
			{
				try
				{
					String funcName = ReflectTools.getFuncName(fieldNames[i], "get");
					this.fields[i] = null;
					this.getters[i] = cls.getMethod(funcName, new Class<?>[0]);
					cls = this.getters[i].getReturnType();
				}
				catch (NoSuchMethodException ex2)
				{
					throw ex;
				}
			}
			i++;
		}
	}

	public FieldGetter(Field field)
	{
		this.fields = new Field[]{field};
		this.getters = new Method[1];
		if (!ReflectTools.isPublic(field.getModifiers()))
		{
			this.getters[0] = ReflectTools.findGetter(field);
			if (this.getters[0] == null)
			{
				throw new IllegalArgumentException("Getter not found");
			}
		}
	}

	public Class<?> getFieldType()
	{
		if (this.fields[this.fields.length - 1] != null)
		{
			return this.fields[this.fields.length - 1].getType();
		}
		else
		{
			return this.getters[this.getters.length - 1].getReturnType();
		}
	}

	public Object get(T o) throws IllegalAccessException, InvocationTargetException
	{
		int i = 0;
		int j = this.fields.length;
		Object fieldObj = o;
		while (i < j)
		{
			if (this.getters[i] != null)
			{
				fieldObj = this.getters[i].invoke(fieldObj);
			}
			else
			{
				fieldObj = this.fields[i].get(fieldObj);
			}
			i++;
		}
		return fieldObj;
	}
}
