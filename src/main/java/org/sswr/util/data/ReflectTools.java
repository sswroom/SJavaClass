package org.sswr.util.data;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ReflectTools {
	public static String getFuncName(String fieldName, String action)
	{
		char c = fieldName.charAt(0);
		if (c >= 'a' && c <= 'z')
		{
			return action+((char)(c - 32))+fieldName.substring(1);
		}
		else
		{
			return action+fieldName;
		}
	}

	private static String getGetterName(Field field)
	{
		Class<?> cls = field.getType();
		if (cls.equals(boolean.class) || cls.equals(Boolean.class))
		{
			return getFuncName(field.getName(), "is");
		}
		else
		{
			return getFuncName(field.getName(), "get");
		}
	}

	private static String getSetterName(String fieldName)
	{
		return getFuncName(fieldName, "set");
	}

	public static Method findGetter(Field field)
	{
		Class<?> cls = field.getDeclaringClass();
		String funcName = getGetterName(field);
		try
		{
			Method meth = cls.getMethod(funcName, new Class<?>[0]);
			if (meth.getReturnType().equals(field.getType()))
			{
				return meth;
			}
			else
			{
				System.out.println("ReflectTools.findGetter: Field = "+field.getDeclaringClass().getName()+"."+field.getName()+", funcName = "+funcName+", wrong return type: "+meth.getReturnType().toString());
				return null;
			}
		}
		catch (NoSuchMethodException ex)
		{
//			System.out.println("ReportBase.findGetter: Field = "+fieldName+", funcName = "+funcName);
//			ex.printStackTrace();
			return null;
		}
	}

	public static Method findGetter(Class<?> cls, String fieldName)
	{

		try
		{
			String funcName = getGetterName(cls.getDeclaredField(fieldName));
			Method meth = cls.getMethod(funcName, new Class<?>[0]);
			if (!meth.getReturnType().equals(void.class))
			{
				return meth;
			}
			else
			{
				System.out.println("ReflectTools.findGetter: Field = "+cls.getName()+"."+fieldName+", funcName = "+funcName+", wrong return type: "+meth.getReturnType().toString());
				return null;
			}
		}
		catch (NoSuchMethodException ex)
		{
			return null;
		}
		catch (NoSuchFieldException ex)
		{
			return null;
		}
	}

	public static Method findSetter(Field field)
	{
		Class<?> cls = field.getDeclaringClass();
		String fieldName = field.getName();
		String funcName = getSetterName(fieldName);
		try
		{
			return cls.getMethod(funcName, new Class<?>[]{field.getType()});
		}
		catch (NoSuchMethodException ex)
		{
//			System.out.println("ReportBase.findSetter: Field = "+fieldName+", funcName = "+funcName);
//			ex.printStackTrace();
			return null;
		}
	}

	public static boolean isPublic(int modifiers)
	{
		return (modifiers & Modifier.PUBLIC) != 0;
	}

	private static void addTypeArguments(Type t, List<Type> typeList)
	{
		if (t instanceof ParameterizedType)
		{
			ParameterizedType pt = (ParameterizedType)t;
			Type types[] = pt.getActualTypeArguments();
			int i = 0;
			int j = types.length;
			while (i < j)
			{
				typeList.add(types[i]);
				i++;
			}
		}
	}

	public static List<Type> getTypeParameters(Type t)
	{
		List<Type> typeList = new ArrayList<Type>();
		if (t instanceof ParameterizedType)
		{
			addTypeArguments(t, typeList);
			return typeList;
		}

		return typeList;
	}
}
