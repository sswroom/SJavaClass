package org.sswr.util.data;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.Inet4Address;
import java.sql.Timestamp;
import java.time.ZoneOffset;
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
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import org.locationtech.jts.geom.Geometry;
import org.sswr.util.data.textbinenc.Base64Enc;
import org.sswr.util.data.textbinenc.Base64Enc.B64Charset;
import org.sswr.util.math.WKTWriter;
import org.sswr.util.math.geometry.Vector2D;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class DataTools {
	@Nullable
	public static <T> Set<Integer> createIntSet(@Nonnull Iterable<T> objs, @Nonnull String fieldName, @Nullable QueryConditions cond)
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
				if (cond == null || cond.isValid(new ObjectFieldGetter<T>(obj)))
					intSet.add((Integer)getter.get(obj));
				while (it.hasNext())
				{
					obj = it.next();
					if (cond == null || cond.isValid(new ObjectFieldGetter<T>(obj)))
						intSet.add((Integer)getter.get(obj));
				}
				return intSet;
			}
			else if (fieldType.equals(Set.class))
			{
				if (cond == null || cond.isValid(new ObjectFieldGetter<T>(obj)))
				{
					@SuppressWarnings("unchecked")
					Set<Integer> set = (Set<Integer>)getter.get(obj);
					intSet.addAll(set);
				}
				while (it.hasNext())
				{
					obj = it.next();
					if (cond == null || cond.isValid(new ObjectFieldGetter<T>(obj)))
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

	@Nullable
	public static <T> Set<String> createStringSet(@Nonnull Iterable<T> objs, @Nonnull String fieldName, @Nullable QueryConditions cond)
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
				if (cond == null || cond.isValid(new ObjectFieldGetter<T>(obj)))
				{
					strSet.add((String)getter.get(obj));
				}
				while (it.hasNext())
				{
					obj = it.next();
					if (cond == null || cond.isValid(new ObjectFieldGetter<T>(obj)))
					{
						strSet.add((String)getter.get(obj));
					}
				}
				return strSet;
			}
			else
			{
				if (cond == null || cond.isValid(new ObjectFieldGetter<T>(obj)))
				{
					strSet.add(getter.getNN(obj).toString());
				}
				while (it.hasNext())
				{
					obj = it.next();
					if (cond == null || cond.isValid(new ObjectFieldGetter<T>(obj)))
					{
						strSet.add(getter.getNN(obj).toString());
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

	@Nullable
	public static <T> Set<Timestamp> createTimestampSet(@Nonnull Iterable<T> objs, @Nonnull String fieldName, @Nullable QueryConditions cond)
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
				if (cond == null || cond.isValid(new ObjectFieldGetter<T>(obj)))
				{
					tsSet.add((Timestamp)getter.get(obj));
				}
				while (it.hasNext())
				{
					obj = it.next();
					if (cond == null || cond.isValid(new ObjectFieldGetter<T>(obj)))
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

	@Nullable
	public static <T> Map<Integer, T> createIntMap(@Nonnull Iterable<T> list, @Nonnull String fieldName, @Nullable QueryConditions cond)
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
				if (cond == null || cond.isValid(new ObjectFieldGetter<T>(o)))
				{
					v = (Integer)getter.get(o);
					retMap.put(v, o);
				}
				while (it.hasNext())
				{
					o = it.next();
					if (cond == null || cond.isValid(new ObjectFieldGetter<T>(o)))
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

	@Nullable 
	public static <T> Map<String, T> createStringMapOrNull(@Nullable Iterator<T> it, @Nonnull String fieldName, @Nullable QueryConditions cond)
	{
		if (it == null)
			return null;
		return createStringMap(it, fieldName, cond);
	}

	@Nullable
	public static <T> Map<String, T> createStringMap(@Nonnull Iterator<T> it, @Nonnull String fieldName, @Nullable QueryConditions cond)
	{
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
				if (cond == null || cond.isValid(new ObjectFieldGetter<T>(o)))
				{
					s = (String)getter.get(o);
					retMap.put(s, o);
				}
				while (it.hasNext())
				{
					o = it.next();
					if (cond == null || cond.isValid(new ObjectFieldGetter<T>(o)))
					{
						s = (String)getter.get(o);
						retMap.put(s, o);
					}
				}
				return retMap;
			}
			else
			{
				if (cond == null || cond.isValid(new ObjectFieldGetter<T>(o)))
				{
					s = getter.getNN(o).toString();
					retMap.put(s, o);
				}
				while (it.hasNext())
				{
					o = it.next();
					if (cond == null || cond.isValid(new ObjectFieldGetter<T>(o)))
					{
						s = getter.getNN(o).toString();
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

	@Nullable
	public static <T> Map<String, T> createStringMap(@Nonnull Iterable<T> list, @Nonnull String fieldName, @Nullable QueryConditions cond)
	{
		return createStringMap(list.iterator(), fieldName, cond);
	}

	@Nullable
	public static <T> Map<String, T> createUpperStringMap(@Nonnull Iterable<T> list, @Nonnull String fieldName, @Nullable QueryConditions cond)
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
				if (cond == null || cond.isValid(new ObjectFieldGetter<T>(o)))
				{
					s = (String)getter.getNN(o);
					retMap.put(s.toUpperCase(), o);
				}
				while (it.hasNext())
				{
					o = it.next();
					if (cond == null || cond.isValid(new ObjectFieldGetter<T>(o)))
					{
						s = (String)getter.getNN(o);
						retMap.put(s.toUpperCase(), o);
					}
				}
				return retMap;
			}
			else
			{
				if (cond == null || cond.isValid(new ObjectFieldGetter<T>(o)))
				{
					s = getter.getNN(o).toString();
					retMap.put(s.toUpperCase(), o);
				}
				while (it.hasNext())
				{
					o = it.next();
					if (cond == null || cond.isValid(new ObjectFieldGetter<T>(o)))
					{
						s = getter.getNN(o).toString();
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
	
	@Nullable
	public static <T, K> List<K> createValueList(@Nonnull Class<K> cls, @Nonnull Iterable<T> objs, @Nonnull String fieldName, @Nullable QueryConditions cond)
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
				if (cond == null || cond.isValid(new ObjectFieldGetter<T>(obj)))
				{
					@SuppressWarnings("unchecked")
					K val = (K)getter.get(obj);
					valueList.add(val);
				}
				while (it.hasNext())
				{
					obj = it.next();
					if (cond == null || cond.isValid(new ObjectFieldGetter<T>(obj)))
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
				if (cond == null || cond.isValid(new ObjectFieldGetter<T>(obj)))
				{
					@SuppressWarnings("unchecked")
					Set<K> set = (Set<K>)getter.get(obj);
					valueList.addAll(set);
				}
				while (it.hasNext())
				{
					obj = it.next();
					if (cond == null || cond.isValid(new ObjectFieldGetter<T>(obj)))
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

	@Nullable
	public static <T, K> Set<K> createValueSet(@Nonnull Class<K> cls, @Nonnull Iterable<T> objs, @Nonnull String fieldName, @Nullable QueryConditions cond)
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
				if (cond == null || cond.isValid(new ObjectFieldGetter<T>(obj)))
				{
					@SuppressWarnings("unchecked")
					K val = (K)getter.get(obj);
					valueSet.add(val);
				}
				while (it.hasNext())
				{
					obj = it.next();
					if (cond == null || cond.isValid(new ObjectFieldGetter<T>(obj)))
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
				if (cond == null || cond.isValid(new ObjectFieldGetter<T>(obj)))
				{
					@SuppressWarnings("unchecked")
					Set<K> set = (Set<K>)getter.get(obj);
					valueSet.addAll(set);
				}
				while (it.hasNext())
				{
					obj = it.next();
					if (cond == null || cond.isValid(new ObjectFieldGetter<T>(obj)))
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

	@Nullable
	public static <T, K> Map<K, T> createValueMap(@Nonnull Class<K> cls, @Nonnull Iterable<T> objs, @Nonnull String fieldName, @Nullable QueryConditions cond)
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
				if (cond == null || cond.isValid(new ObjectFieldGetter<T>(obj)))
				{
					@SuppressWarnings("unchecked")
					K val = (K)getter.get(obj);
					valueMap.put(val, obj);
				}
				while (it.hasNext())
				{
					obj = it.next();
					if (cond == null || cond.isValid(new ObjectFieldGetter<T>(obj)))
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

	@Nullable
	public static <T> List<T> filterToList(@Nonnull T[] arr, @Nullable QueryConditions cond)
	{
		ArrayList<T> list = new ArrayList<T>();
		int i = 0;
		int j = arr.length;
		try
		{
			while (i < j)
			{
				if (cond == null || cond.isValid(new ObjectFieldGetter<T>(arr[i])))
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

	@Nullable
	public static <T> List<T> filterToList(@Nonnull Iterable<T> values, @Nullable QueryConditions cond)
	{
		ArrayList<T> list = new ArrayList<T>();
		Iterator<T> it = values.iterator();
		try
		{
			T obj;
			while (it.hasNext())
			{
				obj = it.next();
				if (cond == null || cond.isValid(new ObjectFieldGetter<T>(obj)))
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

	@Nonnull
	public static <T> String objectJoin(@Nonnull Iterable<T> list, @Nonnull String fieldName, @Nonnull String seperator)
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

	@Nonnull
	public static String intJoin(@Nonnull Iterable<Integer> list, @Nonnull String seperator)
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

	@Nullable
	public static <T extends Enum<T>> T getEnum(@Nonnull Class<T> cls, @Nonnull String name)
	{
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

	@Nonnull
	public static <T extends Enum<T>> T[] string2Enums(@Nonnull Class<T> cls, @Nonnull String names[])
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

	public static <T> void copyList(@Nonnull List<T> list, int destIndex, int srcIndex, int count)
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

	@Nonnull
	public static <T> List<T> sortAsList(@Nonnull Iterable<T> objs, @Nonnull String sortStr) throws NoSuchFieldException
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
	
	@Nonnull
	public static <T> List<T> toList(@Nonnull Iterable<T> objs)
	{
		ArrayList<T> retList = new ArrayList<T>();
		Iterator<T> it = objs.iterator();
		while (it.hasNext())
		{
			retList.add(it.next());
		}
		return retList;
	}

	@Nonnull
	public static <T> T[] toArray(@Nonnull Class<T> cls, @Nonnull List<T> objs)
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

	public static int objectCompare(@Nullable Object obj1, @Nullable Object obj2)
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

	@Nonnull
	private static String toObjectStringInner(@Nullable Object o, int maxLevel, int thisLevel, boolean wellformat)
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
			return "0x"+StringUtil.toHex(((Byte)o).byteValue());
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
		else if (cls.equals(Boolean.class) || cls.equals(boolean.class))
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
		else if (o instanceof ZoneOffset)
		{
			return "ZoneOffset{id='"+((ZoneOffset)o).getId()+"'}";
		}
		else if (o instanceof Class)
		{
			return "Class{name='"+((Class<?>)o).getName()+"'}";
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
				if (wellformat)
					sb.append("\r\n"+"\t".repeat(thisLevel + 1));
				sb.append(toObjectStringInner(it.next(), maxLevel - 1, thisLevel + 1, wellformat));
				while (it.hasNext())
				{
					sb.append(",");
					if (wellformat)
						sb.append("\r\n"+"\t".repeat(thisLevel + 1));
					sb.append(toObjectStringInner(it.next(), maxLevel - 1, thisLevel + 1, wellformat));
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
				if (wellformat)
					sb.append("\r\n"+"\t".repeat(thisLevel + 1));
				sb.append(toObjectStringInner(key, maxLevel - 1, thisLevel + 1, wellformat));
				sb.append('=');
				sb.append(toObjectStringInner(map.get(key), maxLevel - 1, thisLevel + 1, wellformat));
				while (it.hasNext())
				{
					sb.append(',');
					if (wellformat)
						sb.append("\r\n"+"\t".repeat(thisLevel + 1));
					else
						sb.append(' ');
					key = it.next();
					sb.append(toObjectStringInner(key, maxLevel - 1, thisLevel + 1, wellformat));
					sb.append('=');
					sb.append(toObjectStringInner(map.get(key), maxLevel - 1, thisLevel + 1, wellformat));
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
					sb.append(toObjectStringInner(Array.get(o, i), maxLevel - 1, thisLevel + 1, wellformat));
					i++;
				}

				sb.append(", ... ");
				i = j - 32;
				while (i < j)
				{
					sb.append(",");
					sb.append(toObjectStringInner(Array.get(o, i), maxLevel - 1, thisLevel + 1, wellformat));
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
					sb.append(toObjectStringInner(Array.get(o, i), maxLevel - 1, thisLevel + 1, wellformat));
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
			Field fields[] = cls.getFields();
			boolean found = false;
			int i = 0;
			int j = fields.length;
			while (i < j)
			{
				if ((fields[i].getModifiers() & Modifier.STATIC) == 0)
				{
					Object innerObj;
					try
					{
						if (fields[i].canAccess(o))
						{
							innerObj = fields[i].get(o);
						}
						else
						{
							Method getter = ReflectTools.findGetter(fields[i]);
							if (getter != null)
							{
								innerObj = getter.invoke(o);
							}
							else
							{
								innerObj = fields[i].get(o);
							}
						}
						if (found)
						{
							sb.append(',');
							sb.append(' ');
						}
						if (wellformat)
							sb.append("\r\n"+"\t".repeat(thisLevel + 1));
						sb.append(fields[i].getName());
						sb.append('=');
						if (innerObj == o)
						{
							sb.append("self");
						}
						else
						{
							sb.append(toObjectStringInner(innerObj, maxLevel - 1, thisLevel + 1, wellformat));
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
								if (wellformat)
									sb.append("\r\n"+"\t".repeat(thisLevel + 1));
								sb.append(Character.toLowerCase(methName.charAt(3))+methName.substring(4));
								sb.append('=');
								if (innerObj == o)
								{
									sb.append("self");
								}
								else
								{
									sb.append(toObjectStringInner(innerObj, maxLevel - 1, thisLevel + 1, wellformat));
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
								if (wellformat)
									sb.append("\r\n"+"\t".repeat(thisLevel + 1));
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

	@Nonnull
	public static String toObjectString(@Nullable Object o)
	{
		return toObjectStringInner(o, 5, 0, false);
	}

	@Nonnull
	public static String toObjectStringWF(@Nullable Object o)
	{
		return toObjectStringInner(o, 5, 0, true);
	}

	@Nonnull
	public static String toJSONStringInner(@Nullable Object o, int maxLevel)
	{
		if (o == null)
		{
			return "null";
		}
		Class<?> cls = o.getClass();
		if (cls.equals(String.class))
		{
			return JSText.dquoteString(o.toString());
		}
		else if (cls.equals(Character.class))
		{
			return JSText.dquoteString(o.toString());
		}
		else if (cls.equals(Byte.class) || cls.equals(byte.class))
		{
			return String.valueOf(((Byte)o).byteValue() & 255);
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
		else if (cls.equals(Boolean.class) || cls.equals(boolean.class))
		{
			return o.toString();
		}
		else if (cls.equals(Timestamp.class))
		{
			return JSText.dquoteString(((Timestamp)o).toString());
		}
		else if (cls.equals(UUID.class))
		{
			return JSText.dquoteString(o.toString());
		}
		else if (cls.equals(Inet4Address.class))
		{
			return JSText.dquoteString(((Inet4Address)o).getHostAddress());
		}
		else if (cls.equals(ZonedDateTime.class))
		{
			return JSText.dquoteString(DateTimeUtil.toStringNoZone((ZonedDateTime)o));
		}
		else if (cls.isEnum())
		{
			return JSText.dquoteString(o.toString());
		}
		else if (o instanceof Geometry)
		{
			Vector2D vec = GeometryUtil.toVector2D((Geometry)o);
			if (vec == null)
				return JSText.dquoteString(o.toString());
			else
				return JSText.dquoteString(new WKTWriter().generateWKT(vec));
		}
		else if (maxLevel <= 0)
		{
			return "{\"type\":"+JSText.dquoteString(cls.getSimpleName())+"}";
		}
		else if (o instanceof Iterable)
		{
			Iterator<?> it = ((Iterable<?>)o).iterator();
			StringBuilder sb = new StringBuilder();
			sb.append('[');
			if (it.hasNext())
			{
				sb.append(toJSONStringInner(it.next(), maxLevel - 1));
				while (it.hasNext())
				{
					sb.append(",");
					sb.append(toJSONStringInner(it.next(), maxLevel - 1));
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
			sb.append('{');
			sb.append("\"type\":");
			sb.append(JSText.dquoteString(cls.getSimpleName()));
			it = keySet.iterator();
			while (it.hasNext())
			{
				sb.append(',');
				key = it.next();
				sb.append(toJSONStringInner(key, maxLevel - 1));
				sb.append(':');
				sb.append(toJSONStringInner(map.get(key), maxLevel - 1));
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
					sb.append(toJSONStringInner(Array.get(o, i), maxLevel - 1));
					i++;
				}

				sb.append(",\"...\"");
				i = j - 32;
				while (i < j)
				{
					sb.append(",");
					sb.append(toJSONStringInner(Array.get(o, i), maxLevel - 1));
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
					sb.append(toJSONStringInner(Array.get(o, i), maxLevel - 1));
					i++;
				}
			}
			sb.append(']');
			return sb.toString();
		}
		else
		{
			StringBuilder sb = new StringBuilder();
			sb.append('{');
			sb.append("\"type\":");
			sb.append(JSText.dquoteString(cls.getSimpleName()));
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
						sb.append(',');
						sb.append(JSText.dquoteString(fields[i].getName()));
						sb.append(':');
						if (innerObj == o)
						{
							sb.append("\"self\"");
						}
						else
						{
							sb.append(toJSONStringInner(innerObj, maxLevel - 1));
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
								sb.append(',');
								sb.append(JSText.dquoteString(Character.toLowerCase(methName.charAt(3))+methName.substring(4)));
								sb.append(':');
								if (innerObj == o)
								{
									sb.append("\"self\"");
								}
								else
								{
									sb.append(toJSONStringInner(innerObj, maxLevel - 1));
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
								sb.append(',');
								sb.append(JSText.dquoteString(Character.toLowerCase(methName.charAt(2))+methName.substring(3)));
								sb.append(':');
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

	@Nonnull
	public static String toJSONString(@Nullable Object o)
	{
		return toJSONStringInner(o, 5);
	}

	@Nonnull
	public static <T> T cloneEntity(@Nonnull T o) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException
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

	public static <T> int getSize(@Nonnull Iterable<T> it)
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

	@Nonnull
	public static List<String> createList(@Nonnull String []sarr)
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

	@Nullable
	public static Map<String, String> toStringStringMap(@Nonnull Map<String, Object> map, @Nonnull String joinStr)
	{
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

	public static <T> boolean hasNullField(@Nullable T o, boolean checkEmpty)
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
			catch (NullPointerException ex)
			{
				return true;
			}
			
			i++;
		}

		return false;
	}

	@Nullable
	public static <T> T or(@Nullable T obj1, @Nullable T obj2)
	{
		if (obj1 != null)
		{
			return obj1;
		}
		return obj2;
	}

	@Nullable
	public static <T> T or(@Nullable T obj1, @Nullable T obj2, @Nullable T obj3)
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

	@Nullable
	public static Object getObjValue(@Nonnull Object o, @Nonnull String fieldName)
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

	@Nullable
	public static <T> T tryGetFirst(@Nullable List<T> dataList)
	{
		if (dataList == null || dataList.size() == 0)
		{
			return null;
		}
		return dataList.get(0);
	}

	private static void printClassTreeInt(@Nonnull Class<?> cls, int level)
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

	public static void printClassTree(@Nonnull Class<?> cls)
	{
		printClassTreeInt(cls, 0);
	}

	@Nullable
	public static String objectSerialize(@Nonnull Serializable o)
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try
		{
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(o);
			oos.flush();
			oos.close();
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
			return null;
		}
		byte[] input = baos.toByteArray();
		byte[] output = new byte[input.length + 5];
		Deflater deflate = new Deflater(Deflater.BEST_COMPRESSION, true);
		deflate.setInput(input);
		deflate.finish();
		int compSize = deflate.deflate(output, 4, output.length - 4);
		deflate.end();
		ByteTool.writeInt32(output, 0, input.length);
		Base64Enc b64 = new Base64Enc(B64Charset.NORMAL, true);
		return b64.encodeBin(output, 0, compSize + 4);
	}

	@Nullable
	public static Serializable objectDeserialize(@Nonnull String s)
	{
		Base64Enc b64 = new Base64Enc(B64Charset.NORMAL, true);
		byte[] buff = b64.decodeBin(s);
		int len = ByteTool.readInt32(buff, 0);
		byte[] outBuff = new byte[len];
		Inflater inflater = new Inflater(true);
		inflater.setInput(buff, 4, buff.length - 4);
		try
		{
			inflater.inflate(outBuff);
			inflater.end();
			ByteArrayInputStream bais = new ByteArrayInputStream(outBuff);
			ObjectInputStream ois = new ObjectInputStream(bais);
			return (Serializable)ois.readObject();
		}
		catch (DataFormatException ex)
		{
			ex.printStackTrace();
			return null;
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
			return null;
		}
		catch (ClassNotFoundException ex)
		{
			ex.printStackTrace();
			return null;
		}
	}
}
