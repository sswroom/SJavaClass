package org.sswr.util.data;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.Inet4Address;
import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.locationtech.jts.geom.Geometry;
import org.sswr.util.db.QueryConditions;
import org.sswr.util.math.WKTWriter;
import org.sswr.util.math.geometry.Vector2D;

public class DataTools {
	public static <T> Set<Integer> createIntSet(Iterable<T> objs, String fieldName, QueryConditions<T> cond)
	{
		Iterator<T> it = objs.iterator();
		if (!it.hasNext())
		{
			return Collections.emptySet();
		}
		HashSet<Integer> intSet = new HashSet<Integer>();
		T obj = it.next();
		Class<?> cls = obj.getClass();
		try
		{
			FieldGetter<T> getter = new FieldGetter<T>(cls, fieldName);
			Class<?> fieldType = getter.getFieldType();
			if (fieldType.equals(Integer.class) || fieldType.equals(int.class))
			{
				if (cond == null || cond.isValid(obj))
					intSet.add((Integer)getter.get(obj));
				while (it.hasNext())
				{
					obj = it.next();
					if (cond == null || cond.isValid(obj))
						intSet.add((Integer)getter.get(obj));
				}
				return intSet;
			}
			else if (fieldType.equals(Set.class))
			{
				if (cond == null || cond.isValid(obj))
				{
					@SuppressWarnings("unchecked")
					Set<Integer> set = (Set<Integer>)getter.get(obj);
					intSet.addAll(set);
				}
				while (it.hasNext())
				{
					obj = it.next();
					if (cond == null || cond.isValid(obj))
					{
						@SuppressWarnings("unchecked")
						Set<Integer> set2 = (Set<Integer>)getter.get(obj);
						intSet.addAll(set2);
					}
				}
				return intSet;
			}
			else
			{
				System.out.println("DataTools.getIntSet: field type is not supported: "+fieldType.toString());
				return null;
			}
		}
		catch (NoSuchFieldException ex)
		{
			ex.printStackTrace();
			return null;
		}
		catch (IllegalAccessException ex)
		{
			ex.printStackTrace();
			return null;
		}
		catch (InvocationTargetException ex)
		{
			ex.printStackTrace();
			return null;
		}
	}

	public static <T> Set<String> createStringSet(Iterable<T> objs, String fieldName, QueryConditions<T> cond)
	{
		Iterator<T> it = objs.iterator();
		if (!it.hasNext())
		{
			return new HashSet<String>();
		}
		HashSet<String> strSet = new HashSet<String>();
		T obj = it.next();
		Class<?> cls = obj.getClass();
		try
		{
			FieldGetter<T> getter = new FieldGetter<T>(cls, fieldName);
			Class<?> fieldType = getter.getFieldType();
			if (fieldType.equals(String.class))
			{
				if (cond == null || cond.isValid(obj))
				{
					strSet.add((String)getter.get(obj));
				}
				while (it.hasNext())
				{
					obj = it.next();
					if (cond == null || cond.isValid(obj))
					{
						strSet.add((String)getter.get(obj));
					}
				}
				return strSet;
			}
			else
			{
				if (cond == null || cond.isValid(obj))
				{
					strSet.add(getter.get(obj).toString());
				}
				while (it.hasNext())
				{
					obj = it.next();
					if (cond == null || cond.isValid(obj))
					{
						strSet.add(getter.get(obj).toString());
					}
				}
				return strSet;
			}
		}
		catch (NoSuchFieldException ex)
		{
			ex.printStackTrace();
			return null;
		}
		catch (IllegalAccessException ex)
		{
			ex.printStackTrace();
			return null;
		}
		catch (InvocationTargetException ex)
		{
			ex.printStackTrace();
			return null;
		}
	}

	public static <T> Set<Timestamp> createTimestampSet(Iterable<T> objs, String fieldName, QueryConditions<T> cond)
	{
		Iterator<T> it = objs.iterator();
		if (!it.hasNext())
		{
			return new HashSet<Timestamp>();
		}
		HashSet<Timestamp> tsSet = new HashSet<Timestamp>();
		T obj = it.next();
		Class<?> cls = obj.getClass();
		try
		{
			FieldGetter<T> getter = new FieldGetter<T>(cls, fieldName);
			Class<?> fieldType = getter.getFieldType();
			if (fieldType.equals(Timestamp.class))
			{
				if (cond == null || cond.isValid(obj))
				{
					tsSet.add((Timestamp)getter.get(obj));
				}
				while (it.hasNext())
				{
					obj = it.next();
					if (cond == null || cond.isValid(obj))
					{
						tsSet.add((Timestamp)getter.get(obj));
					}
				}
				return tsSet;
			}
			else
			{
				return null;
			}
		}
		catch (NoSuchFieldException ex)
		{
			ex.printStackTrace();
			return null;
		}
		catch (IllegalAccessException ex)
		{
			ex.printStackTrace();
			return null;
		}
		catch (InvocationTargetException ex)
		{
			ex.printStackTrace();
			return null;
		}
	}

	public static <T> Map<Integer, T> createIntMap(Iterable<T> list, String fieldName, QueryConditions<T> cond)
	{
		Iterator<T> it = list.iterator();
		if (!it.hasNext())
		{
			return new HashMap<Integer, T>();
		}
		T o = it.next();
		try
		{
			FieldGetter<T> getter = new FieldGetter<T>(o.getClass(), fieldName);
			Class<?> t = getter.getFieldType();
			HashMap<Integer, T> retMap = new HashMap<Integer, T>();
			Integer v;
			if (t.equals(Integer.class) || t.equals(int.class))
			{
				if (cond == null || cond.isValid(o))
				{
					v = (Integer)getter.get(o);
					retMap.put(v, o);
				}
				while (it.hasNext())
				{
					o = it.next();
					if (cond == null || cond.isValid(o))
					{
						v = (Integer)getter.get(o);
						retMap.put(v, o);
					}
				}
				return retMap;
			}
			else
			{
				return null;
			}
		}
		catch (NoSuchFieldException ex)
		{
			ex.printStackTrace();
			return null;
		}
		catch (IllegalAccessException ex)
		{
			ex.printStackTrace();
			return null;
		}
		catch (IllegalArgumentException ex)
		{
			ex.printStackTrace();
			return null;
		}
		catch (InvocationTargetException ex)
		{
			ex.printStackTrace();
			return null;
		}
	}

	public static <T> Map<String, T> createStringMap(Iterable<T> list, String fieldName, QueryConditions<T> cond)
	{
		Iterator<T> it = list.iterator();
		if (!it.hasNext())
		{
			return new HashMap<String, T>();
		}
		T o = it.next();
		try
		{
			FieldGetter<T> getter = new FieldGetter<T>(o.getClass(), fieldName);
			Class<?> t = getter.getFieldType();
			HashMap<String, T> retMap = new HashMap<String, T>();
			String s;
			if (t.equals(String.class))
			{
				if (cond == null || cond.isValid(o))
				{
					s = (String)getter.get(o);
					retMap.put(s, o);
				}
				while (it.hasNext())
				{
					o = it.next();
					if (cond == null || cond.isValid(o))
					{
						s = (String)getter.get(o);
						retMap.put(s, o);
					}
				}
				return retMap;
			}
			else
			{
				if (cond == null || cond.isValid(o))
				{
					s = getter.get(o).toString();
					retMap.put(s, o);
				}
				while (it.hasNext())
				{
					o = it.next();
					if (cond == null || cond.isValid(o))
					{
						s = getter.get(o).toString();
						retMap.put(s, o);
					}
				}
				return retMap;
			}
		}
		catch (NoSuchFieldException ex)
		{
			ex.printStackTrace();
			return null;
		}
		catch (IllegalAccessException ex)
		{
			ex.printStackTrace();
			return null;
		}
		catch (IllegalArgumentException ex)
		{
			ex.printStackTrace();
			return null;
		}
		catch (InvocationTargetException ex)
		{
			ex.printStackTrace();
			return null;
		}
	}

	public static <T> Map<String, T> createUpperStringMap(Iterable<T> list, String fieldName, QueryConditions<T> cond)
	{
		Iterator<T> it = list.iterator();
		if (!it.hasNext())
		{
			return new HashMap<String, T>();
		}
		T o = it.next();
		try
		{
			FieldGetter<T> getter = new FieldGetter<T>(o.getClass(), fieldName);
			Class<?> t = getter.getFieldType();
			HashMap<String, T> retMap = new HashMap<String, T>();
			String s;
			if (t.equals(String.class))
			{
				if (cond == null || cond.isValid(o))
				{
					s = (String)getter.get(o);
					retMap.put(s.toUpperCase(), o);
				}
				while (it.hasNext())
				{
					o = it.next();
					if (cond == null || cond.isValid(o))
					{
						s = (String)getter.get(o);
						retMap.put(s.toUpperCase(), o);
					}
				}
				return retMap;
			}
			else
			{
				if (cond == null || cond.isValid(o))
				{
					s = getter.get(o).toString();
					retMap.put(s.toUpperCase(), o);
				}
				while (it.hasNext())
				{
					o = it.next();
					if (cond == null || cond.isValid(o))
					{
						s = getter.get(o).toString();
						retMap.put(s.toUpperCase(), o);
					}
				}
				return retMap;
			}
		}
		catch (NoSuchFieldException ex)
		{
			ex.printStackTrace();
			return null;
		}
		catch (IllegalAccessException ex)
		{
			ex.printStackTrace();
			return null;
		}
		catch (IllegalArgumentException ex)
		{
			ex.printStackTrace();
			return null;
		}
		catch (InvocationTargetException ex)
		{
			ex.printStackTrace();
			return null;
		}
	}
	
	public static <T, K> List<K> createValueList(Class<K> cls, Iterable<T> objs, String fieldName, QueryConditions<T> cond)
	{
		Iterator<T> it = objs.iterator();
		if (!it.hasNext())
		{
			return new ArrayList<K>();
		}
		ArrayList<K> valueList = new ArrayList<K>();
		T obj = it.next();
		Class<?> clsT = obj.getClass();
		try
		{
			FieldGetter<T> getter = new FieldGetter<T>(clsT, fieldName);
			Class<?> fieldType = getter.getFieldType();
			if (fieldType.equals(cls))
			{
				if (cond == null || cond.isValid(obj))
				{
					@SuppressWarnings("unchecked")
					K val = (K)getter.get(obj);
					valueList.add(val);
				}
				while (it.hasNext())
				{
					obj = it.next();
					if (cond == null || cond.isValid(obj))
					{
						@SuppressWarnings("unchecked")
						K val = (K)getter.get(obj);
						valueList.add(val);
					}
				}
				return valueList;
			}
			else if (fieldType.equals(Set.class))
			{
				if (cond == null || cond.isValid(obj))
				{
					@SuppressWarnings("unchecked")
					Set<K> set = (Set<K>)getter.get(obj);
					valueList.addAll(set);
				}
				while (it.hasNext())
				{
					obj = it.next();
					if (cond == null || cond.isValid(obj))
					{
						@SuppressWarnings("unchecked")
						Set<K> set2 = (Set<K>)getter.get(obj);
						valueList.addAll(set2);
					}
				}
				return valueList;
			}
			else
			{
				System.out.println("DataTools.createValueList: field type is not supported: "+fieldType.toString());
				return null;
			}
		}
		catch (NoSuchFieldException ex)
		{
			ex.printStackTrace();
			return null;
		}
		catch (IllegalAccessException ex)
		{
			ex.printStackTrace();
			return null;
		}
		catch (InvocationTargetException ex)
		{
			ex.printStackTrace();
			return null;
		}
	}

	public static <T, K> Set<K> createValueSet(Class<K> cls, Iterable<T> objs, String fieldName, QueryConditions<T> cond)
	{
		Iterator<T> it = objs.iterator();
		if (!it.hasNext())
		{
			return new HashSet<K>();
		}
		Set<K> valueSet = new HashSet<K>();
		T obj = it.next();
		Class<?> clsT = obj.getClass();
		try
		{
			FieldGetter<T> getter = new FieldGetter<T>(clsT, fieldName);
			Class<?> fieldType = getter.getFieldType();
			if (fieldType.equals(cls))
			{
				if (cond == null || cond.isValid(obj))
				{
					@SuppressWarnings("unchecked")
					K val = (K)getter.get(obj);
					valueSet.add(val);
				}
				while (it.hasNext())
				{
					obj = it.next();
					if (cond == null || cond.isValid(obj))
					{
						@SuppressWarnings("unchecked")
						K val = (K)getter.get(obj);
						valueSet.add(val);
					}
				}
				return valueSet;
			}
			else if (fieldType.equals(Set.class))
			{
				if (cond == null || cond.isValid(obj))
				{
					@SuppressWarnings("unchecked")
					Set<K> set = (Set<K>)getter.get(obj);
					valueSet.addAll(set);
				}
				while (it.hasNext())
				{
					obj = it.next();
					if (cond == null || cond.isValid(obj))
					{
						@SuppressWarnings("unchecked")
						Set<K> set2 = (Set<K>)getter.get(obj);
						valueSet.addAll(set2);
					}
				}
				return valueSet;
			}
			else
			{
				System.out.println("DataTools.createValueSet: field type is not supported: "+fieldType.toString());
				return null;
			}
		}
		catch (NoSuchFieldException ex)
		{
			ex.printStackTrace();
			return null;
		}
		catch (IllegalAccessException ex)
		{
			ex.printStackTrace();
			return null;
		}
		catch (InvocationTargetException ex)
		{
			ex.printStackTrace();
			return null;
		}
	}

	public static <T, K> Map<K, T> createValueMap(Class<K> cls, Iterable<T> objs, String fieldName, QueryConditions<T> cond)
	{
		Iterator<T> it = objs.iterator();
		if (!it.hasNext())
		{
			return new HashMap<K, T>();
		}
		Map<K, T> valueMap = new HashMap<K, T>();
		T obj = it.next();
		Class<?> clsT = obj.getClass();
		try
		{
			FieldGetter<T> getter = new FieldGetter<T>(clsT, fieldName);
			Class<?> fieldType = getter.getFieldType();
			if (fieldType.equals(cls))
			{
				if (cond == null || cond.isValid(obj))
				{
					@SuppressWarnings("unchecked")
					K val = (K)getter.get(obj);
					valueMap.put(val, obj);
				}
				while (it.hasNext())
				{
					obj = it.next();
					if (cond == null || cond.isValid(obj))
					{
						@SuppressWarnings("unchecked")
						K val = (K)getter.get(obj);
						valueMap.put(val, obj);
					}
				}
				return valueMap;
			}
			else
			{
				System.out.println("DataTools.createValueMap: field type is not supported: "+fieldType.toString());
				return null;
			}
		}
		catch (NoSuchFieldException ex)
		{
			ex.printStackTrace();
			return null;
		}
		catch (IllegalAccessException ex)
		{
			ex.printStackTrace();
			return null;
		}
		catch (InvocationTargetException ex)
		{
			ex.printStackTrace();
			return null;
		}
	}

	public static <T> List<T> filterToList(T[] arr, QueryConditions<T> cond)
	{
		ArrayList<T> list = new ArrayList<T>();
		int i = 0;
		int j = arr.length;
		try
		{
			while (i < j)
			{
				if (cond == null || cond.isValid(arr[i]))
					list.add(arr[i]);
				i++;
			}
			return list;
		}
		catch (IllegalAccessException ex)
		{
			return null;
		}
		catch (InvocationTargetException ex)
		{
			return null;
		}
	}

	public static <T> List<T> filterToList(Iterable<T> values, QueryConditions<T> cond)
	{
		ArrayList<T> list = new ArrayList<T>();
		Iterator<T> it = values.iterator();
		try
		{
			T obj;
			while (it.hasNext())
			{
				obj = it.next();
				if (cond == null || cond.isValid(obj))
					list.add(obj);
			}
			return list;
		}
		catch (IllegalAccessException ex)
		{
			return null;
		}
		catch (InvocationTargetException ex)
		{
			return null;
		}
	}

	public static <T> String objectJoin(Iterable<T> list, String fieldName, String seperator)
	{
		Iterator<T> it = list.iterator();
		if (!it.hasNext())
		{
			return "";
		}
		T o = it.next();
		try
		{
			FieldGetter<T> getter = new FieldGetter<T>(o.getClass(), fieldName);
			StringBuilder sb = new StringBuilder();
			sb.append(getter.get(o));
			while (it.hasNext())
			{
				sb.append(seperator);
				sb.append(getter.get(it.next()));
			}
			return sb.toString();
		}
		catch (NoSuchFieldException ex)
		{
			ex.printStackTrace();
			return "";
		}
		catch (IllegalAccessException ex)
		{
			ex.printStackTrace();
			return "";
		}
		catch (IllegalArgumentException ex)
		{
			ex.printStackTrace();
			return "";
		}
		catch (InvocationTargetException ex)
		{
			ex.printStackTrace();
			return "";
		}
	}

	public static String intJoin(Iterable<Integer> list, String seperator)
	{
		StringBuilder sb = new StringBuilder();
		Iterator<Integer> it = list.iterator();
		if (!it.hasNext())
		{
			return "";
		}
		sb.append(it.next().toString());
		while (it.hasNext())
		{
			sb.append(seperator);
			sb.append(it.next().toString());
		}
		return sb.toString();
	}

	public static <T extends Enum<T>> T getEnum(Class<T> cls, String name)
	{
		if (name == null)
		{
			return null;
		}
		T[] enums = cls.getEnumConstants();
		if (enums == null)
		{
			return null;
		}
		int i = 0;
		int j = enums.length;
		while (i < j)
		{
			if (enums[i].name().equals(name))
			{
				return enums[i];
			}
			i++;
		}
		return null;
	}

	public static <T extends Enum<T>> T[] string2Enums(Class<T> cls, String names[])
	{
		ArrayList<T> tList = new ArrayList<T>();
		int i = 0;
		int j = names.length;
		while (i < j)
		{
			T t = getEnum(cls, names[i]);
			if (t != null)
			{
				tList.add(t);
			}
			i++;
		}
		return toArray(cls, tList);
	}

	public static <T> void copyList(List<T> list, int destIndex, int srcIndex, int count)
	{
		if (destIndex > srcIndex)
		{
			destIndex += count;
			srcIndex += count;
			while (count-- > 0)
			{
				list.set(--destIndex, list.get(--srcIndex));
			}
		}
		else if (destIndex < srcIndex)
		{
			while (count-- > 0)
			{
				list.set(destIndex++, list.get(srcIndex++));
			}
		}
	}

	public static <T> List<T> sortAsList(Iterable<T> objs, String sortStr) throws NoSuchFieldException
	{
		ArrayList<T> retList = new ArrayList<T>();
		Iterator<T> it = objs.iterator();
		if (!it.hasNext())
		{
			return retList;
		}
		T obj = it.next();
		FieldComparator<T> comparator = new FieldComparator<T>(obj.getClass(), sortStr);
		retList.add(obj);
		while (it.hasNext())
			retList.add(it.next());
		ArtificialQuickSort.sort(retList, comparator);
		return retList;
	}
	
	public static <T> List<T> toList(Iterable<T> objs)
	{
		ArrayList<T> retList = new ArrayList<T>();
		Iterator<T> it = objs.iterator();
		while (it.hasNext())
		{
			retList.add(it.next());
		}
		return retList;
	}

	public static <T> T[] toArray(Class<T> cls, List<T> objs)
	{
		@SuppressWarnings("unchecked")
		T tArr[] = (T[])Array.newInstance(cls, objs.size());
		int i = 0;
		int j = objs.size();
		while (i < j)
		{
			tArr[i] = objs.get(i);
			i++;
		}
		return tArr;
	}

	public static int objectCompare(Object obj1, Object obj2)
	{
		if (obj1 == null)
		{
			if (obj2 == null)
			{
				return 0;
			}
			else
			{
				return -1;
			}
		}
		else if (obj2 == null)
		{
			return 1;
		}
		Class<?> cls = obj1.getClass();
		if (!cls.equals(obj2.getClass()))
		{
			throw new ClassCastException("The objects are not in the same class");
		}
		if (cls.equals(Integer.class))
		{
			return ((Integer)obj1).compareTo((Integer)obj2);
		}
		else if (cls.equals(Double.class))
		{
			return ((Double)obj1).compareTo((Double)obj2);
		}
		else if (cls.equals(String.class))
		{
			return ((String)obj1).compareTo((String)obj2);
		}
		else if (cls.equals(Timestamp.class))
		{
			return ((Timestamp)obj1).compareTo((Timestamp)obj2);
		}
		throw new IllegalArgumentException("Object class is not supported: "+cls.toString());
	}

	private static String toObjectStringInner(Object o, int maxLevel)
	{
		if (o == null)
		{
			return "null";
		}
		Class<?> cls = o.getClass();
		if (cls.equals(String.class))
		{
			return JSText.quoteString(o.toString());
		}
		else if (cls.equals(Byte.class) || cls.equals(byte.class))
		{
			return o.toString();
		}
		else if (cls.equals(Short.class) || cls.equals(short.class))
		{
			return o.toString();
		}
		else if (cls.equals(Integer.class) || cls.equals(int.class))
		{
			return o.toString();
		}
		else if (cls.equals(Long.class) || cls.equals(long.class))
		{
			return o.toString();
		}
		else if (cls.equals(Float.class) || cls.equals(float.class))
		{
			return o.toString();
		}
		else if (cls.equals(Double.class) || cls.equals(double.class))
		{
			return o.toString();
		}
		else if (cls.equals(Timestamp.class))
		{
			return JSText.quoteString(DateTimeUtil.toStringNoZone((Timestamp)o));
		}
		else if (cls.equals(UUID.class))
		{
			return JSText.quoteString(o.toString());
		}
		else if (cls.equals(Inet4Address.class))
		{
			return JSText.quoteString(((Inet4Address)o).getHostAddress());
		}
		else if (cls.equals(ZonedDateTime.class))
		{
			return JSText.quoteString(DateTimeUtil.toStringNoZone((ZonedDateTime)o));
		}
		else if (cls.isEnum())
		{
			return JSText.quoteString(o.toString());
		}
		else if (o instanceof Geometry)
		{
			Vector2D vec = GeometryUtil.toVector2D((Geometry)o);
			if (vec == null)
				return JSText.quoteString(o.toString());
			else
				return JSText.quoteString(new WKTWriter().generateWKT(vec));
		}
		else if (maxLevel <= 0)
		{
			return cls.getSimpleName();
		}
		else if (o instanceof Iterable)
		{
			Iterator<?> it = ((Iterable<?>)o).iterator();
			StringBuilder sb = new StringBuilder();
			sb.append('[');
			if (it.hasNext())
			{
				sb.append(toObjectStringInner(it.next(), maxLevel - 1));
				while (it.hasNext())
				{
					sb.append(",");
					sb.append(toObjectStringInner(it.next(), maxLevel - 1));
				}
			}
			sb.append(']');
			return sb.toString();
		}
		else if (o instanceof Map)
		{
			Map<?, ?> map = (Map<?, ?>)o;
			Set<?> keySet = map.keySet();
			Iterator<?> it;
			Object key;
			StringBuilder sb = new StringBuilder();
			sb.append(cls.getSimpleName());
			sb.append('{');
			it = keySet.iterator();
			if (it.hasNext())
			{
				key = it.next();
				sb.append(toObjectStringInner(key, maxLevel - 1));
				sb.append('=');
				sb.append(toObjectStringInner(map.get(key), maxLevel - 1));
				while (it.hasNext())
				{
					sb.append(',');
					sb.append(' ');
					key = it.next();
					sb.append(toObjectStringInner(key, maxLevel - 1));
					sb.append('=');
					sb.append(toObjectStringInner(map.get(key), maxLevel - 1));
				}
			}
			sb.append('}');
			return sb.toString();
		}
		else if (cls.isArray())
		{
			int i = 0;
			int j = Array.getLength(o);
			StringBuilder sb = new StringBuilder();
			sb.append('[');
			if (j > 64)
			{
				while (i < 32)
				{
					if (i > 0)
					{
						sb.append(",");
					}
					sb.append(toObjectStringInner(Array.get(o, i), maxLevel - 1));
					i++;
				}

				sb.append(", ... ");
				i = j - 32;
				while (i < j)
				{
					sb.append(",");
					sb.append(toObjectStringInner(Array.get(o, i), maxLevel - 1));
					i++;
				}
			}
			else
			{
				while (i < j)
				{
					if (i > 0)
					{
						sb.append(",");
					}
					sb.append(toObjectStringInner(Array.get(o, i), maxLevel - 1));
					i++;
				}
			}
			sb.append(']');
			return sb.toString();
		}
		else
		{
			StringBuilder sb = new StringBuilder();
			sb.append(cls.getSimpleName());
			sb.append('{');
			Field fields[] = cls.getDeclaredFields();
			boolean found = false;
			int i = 0;
			int j = fields.length;
			while (i < j)
			{
				if ((fields[i].getModifiers() & Modifier.STATIC) == 0)
				{
					try
					{
						Method getter = ReflectTools.findGetter(fields[i]);
						Object innerObj;
						if (getter != null)
						{
							innerObj = getter.invoke(o);
						}
						else
						{
							innerObj = fields[i].get(o);
						}
						if (found)
						{
							sb.append(',');
							sb.append(' ');
						}
						sb.append(fields[i].getName());
						sb.append('=');
						if (innerObj == o)
						{
							sb.append("self");
						}
						else
						{
							sb.append(toObjectStringInner(innerObj, maxLevel - 1));
						}
						found = true;
					}
					catch (IllegalAccessException ex)
					{

					}
					catch (IllegalArgumentException ex)
					{

					}
					catch (InvocationTargetException ex)
					{

					}
				}
				i++;
			}
			if (!found)
			{
				Method meths[] = cls.getMethods();
				i = 0;
				j = meths.length;
				while (i < j)
				{
					if (meths[i].getParameterCount() == 0)
					{
						String methName = meths[i].getName();
					
						if (methName.equals("getClass"))
						{
	
						}
						else if (methName.startsWith("get") && methName.length() > 3 && Character.isUpperCase(methName.charAt(3)))
						{
							try
							{
								Object innerObj = meths[i].invoke(o);
								if (found)
								{
									sb.append(',');
									sb.append(' ');
								}
								sb.append(Character.toLowerCase(methName.charAt(3))+methName.substring(4));
								sb.append('=');
								if (innerObj == o)
								{
									sb.append("self");
								}
								else
								{
									sb.append(toObjectStringInner(innerObj, maxLevel - 1));
								}
								found = true;
							}
							catch (Exception ex)
							{

							}	
						}
						else if (methName.startsWith("is") && methName.length() > 2 && Character.isUpperCase(methName.charAt(2)) && meths[i].getReturnType().equals(boolean.class))
						{
							try
							{
								Boolean res = (Boolean)meths[i].invoke(o);
								if (found)
								{
									sb.append(',');
									sb.append(' ');
								}
								sb.append(Character.toLowerCase(methName.charAt(2))+methName.substring(3));
								sb.append('=');
								sb.append(res.toString());
								found = true;
							}
							catch (Exception ex)
							{

							}
						}
					}
					i++;
				}
			}
			sb.append('}');
			return sb.toString();
		}
	}

	public static String toObjectString(Object o)
	{
		return toObjectStringInner(o, 5);
	}

	public static <T> T cloneEntity(T o)
	{
		try
		{
			FieldGetter<T> getter;
			FieldSetter setter;
			Class<?> classT = o.getClass();
			@SuppressWarnings("unchecked")
			T newO = (T)classT.getConstructor(new Class<?>[0]).newInstance();
			Field fields[] = classT.getDeclaredFields();
			int i = 0;
			int j = fields.length;
			while (i < j)
			{
				getter = new FieldGetter<T>(fields[i]);
				setter = new FieldSetter(fields[i]);
				setter.set(newO, getter.get(o));
				i++;
			}
			return newO;
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			return null;
		}
	}

	public static <T> int getSize(Iterable<T> it)
	{
		if (it instanceof List)
		{
			return ((List<T>)it).size();
		}
		else if (it instanceof Set)
		{
			return ((Set<T>)it).size();
		}
		else if (it instanceof Collection)
		{
			return ((Collection<T>)it).size();
		}
		else
		{
			System.out.println("getSize: "+it.getClass().toString());
			int ret = 0;
			Iterator<T> itObj = it.iterator();
			while (itObj.hasNext())
			{
				ret++;
				itObj.next();
			}
			return ret;
		}
	}

	public static List<String> createList(String []sarr)
	{
		List<String> retList = new ArrayList<String>();
		int i = 0;
		int j = sarr.length;
		while (i < j)
		{
			retList.add(sarr[i]);
			i++;
		}
		return retList;
	}

	public static Map<String, String> toStringStringMap(Map<String, Object> map, String joinStr)
	{
		if (map == null)
		{
			return null;
		}
		Map<String, String> retMap = new HashMap<String, String>();
		Iterator<String> itName = map.keySet().iterator();
		String name;
		Object v;
		while (itName.hasNext())
		{
			name = itName.next();
			v = map.get(name);
			if (v == null)
			{
				retMap.put(name, null);
			}
			else if (v instanceof Iterable)
			{
				@SuppressWarnings("unchecked")
				Iterable<Object> varr = (Iterable<Object>)v;
				Iterator<Object> itV = varr.iterator();
				StringBuilder sb = new StringBuilder();
				boolean found = false;
				while (itV.hasNext())
				{
					v = itV.next();
					if (found)
					{
						sb.append(joinStr);
					}
					found = true;
					sb.append(v.toString());
				}
				retMap.put(name, sb.toString());
			}
			else
			{
				retMap.put(name, v.toString());
			}
		}
		return retMap;
	}
	public static <T> boolean hasNullField(T o, boolean checkEmpty)
	{
		if (o == null)
		{
			return true;
		}
		Class<?> cls = o.getClass();
		Field[] fields = cls.getDeclaredFields();
		int i = 0;
		int j = fields.length;
		while (i < j)
		{
			try
			{
				FieldGetter<T> getter = new FieldGetter<T>(fields[i]);
				Object val = getter.get(o);
				if (val == null)
				{
					return true;
				}
				if (checkEmpty && (val instanceof String))
				{
					if (((String)val).length() == 0)
					{
						return true;
					}
				}
			}
			catch (IllegalArgumentException ex)
			{
				return true;
			}
			catch (InvocationTargetException ex)
			{
				ex.printStackTrace();
				return true;
			}
			catch (IllegalAccessException ex)
			{
				ex.printStackTrace();
				return true;
			}
			
			i++;
		}

		return false;
	}

	public static <T> T or(T obj1, T obj2)
	{
		if (obj1 != null)
		{
			return obj1;
		}
		return obj2;
	}

	public static <T> T or(T obj1, T obj2, T obj3)
	{
		if (obj1 != null)
		{
			return obj1;
		}
		if (obj2 != null)
		{
			return obj2;
		}
		return obj3;
	}

	public static Object getObjValue(Object o, String fieldName)
	{
		try
		{
			FieldGetter<Object> getter = new FieldGetter<Object>(o.getClass(), fieldName);
			return getter.get(o);
		}
		catch (NoSuchFieldException ex)
		{
			return null;
		}
		catch (IllegalAccessException ex)
		{
			return null;
		}
		catch (InvocationTargetException ex)
		{
			return null;
		}
	}

	public static <T> T tryGetFirst(List<T> dataList)
	{
		if (dataList == null || dataList.size() == 0)
		{
			return null;
		}
		return dataList.get(0);
	}

	private static void printClassTreeInt(Class<?> cls, int level)
	{
		int i = level;
		while (i-- > 0)
		{
			System.out.print("\t");
		}
		if (cls.isInterface())
		{
			System.out.print("interface ");
		}
		else
		{
			System.out.print("class ");
		}
		System.out.println(cls.getName());
		Class<?> []interfaces = cls.getInterfaces();
		int j = interfaces.length;
		i = 0;
		while (i < j)
		{
			printClassTreeInt(interfaces[i], level + 1);
			i++;
		}
		Class<?> superCls = cls.getSuperclass();
		if (superCls != null)
		{
			printClassTreeInt(superCls, level + 1);
		}
	}

	public static void printClassTree(Class<?> cls)
	{
		printClassTreeInt(cls, 0);
	}
}
