package org.sswr.util.data;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.locationtech.jts.geom.Geometry;
import org.sswr.util.math.Coord2DDbl;
import org.sswr.util.math.geometry.CurvePolygon;
import org.sswr.util.math.geometry.LineString;
import org.sswr.util.math.geometry.LinearRing;
import org.sswr.util.math.geometry.MultiPolygon;
import org.sswr.util.math.geometry.Point2D;
import org.sswr.util.math.geometry.Polygon;
import org.sswr.util.math.geometry.Polyline;
import org.sswr.util.math.geometry.Vector2D;
import org.sswr.util.math.geometry.Vector2D.VectorType;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class JSONBuilder
{
	public enum ObjectType
	{
		OT_OBJECT,
		OT_ARRAY,
		OT_END
	}

	private StringBuilder sb;
	private List<ObjectType> objTypes;
	private ObjectType currType;
	private boolean isFirst;

	private void appendStr(@Nonnull String val)
	{
		char carr[] = val.toCharArray();
		char c;
		int i = 0;
		int j = carr.length;
		this.sb.append('\"');
		while (i < j)
		{
			c = carr[i];
			if (c == '\\')
			{
				this.sb.append('\\');
				this.sb.append('\\');
			}
			else if (c == '\"')
			{
				this.sb.append('\\');
				this.sb.append('\"');
			}
			else if (c == '\r')
			{
				this.sb.append('\\');
				this.sb.append('r');
			}
			else if (c == '\n')
			{
				this.sb.append('\\');
				this.sb.append('n');
			}
			else
			{
				this.sb.append(c);
			}
			i++;
		}
		this.sb.append('\"');
	}

	private void appendDouble(double val)
	{
		if (Double.isNaN(val))
		{
			this.sb.append("null");
		}
		else
		{
			this.sb.append(val);
		}
	}

	private void appendTSStr(@Nullable Timestamp ts)
	{
		if (ts == null)
		{
			this.sb.append("null");
		}
		else
		{
			this.appendStr(DateTimeUtil.toString(ts));
		}
	}

	private void appendDateStr(@Nullable Date dat)
	{
		if (dat == null)
		{
			this.sb.append("null");
		}
		else
		{
			this.appendStr(DateTimeUtil.toString(dat));
		}
	}

	private void appendCoord2D(@Nonnull Coord2DDbl coord)
	{
		this.sb.append('[');
		this.appendDouble(coord.x);
		this.sb.append(',');
		this.appendDouble(coord.y);
		this.sb.append(']');
	}

	private void appendCoord2DArray(@Nullable Coord2DDbl[] coordList)
	{
		this.sb.append('[');
		if (coordList != null && coordList.length > 0)
		{
			this.appendCoord2D(coordList[0]);
			int i = 1;
			while (i < coordList.length)
			{
				this.sb.append(',');
				this.appendCoord2D(coordList[i]);
				i++;
			}
		}
		this.sb.append(']');
	}

	private void appendCoordPL(@Nonnull Polyline pl)
	{
		Iterator<LineString> it = pl.iterator();
		this.sb.append('[');
		LineString ls;
		if (it.hasNext())
		{
			ls = it.next();
			Coord2DDbl[] ptList = ls.getPointList();
			this.appendCoord2DArray(ptList);
			while (it.hasNext())
			{
				ls = it.next();
				ptList = ls.getPointList();
				this.sb.append(',');
				this.appendCoord2DArray(ptList);
			}
		}
		this.sb.append(']');
	}

	private void appendCoordPG(@Nonnull Polygon pg)
	{
		Iterator<LinearRing> it = pg.iterator();
		this.sb.append('[');
		LinearRing lr;
		if (it.hasNext())
		{
			lr = it.next();
			Coord2DDbl[] ptList = lr.getPointList();
			this.appendCoord2DArray(ptList);
			while (it.hasNext())
			{
				lr = it.next();
				ptList = lr.getPointList();
				this.sb.append(',');
				this.appendCoord2DArray(ptList);
			}
		}
		this.sb.append(']');
	}

	private void appendGeometry(@Nonnull Vector2D vec)
	{
		VectorType vecType = vec.getVectorType();
		if (vecType == VectorType.Point)
		{
			Point2D pt = (Point2D)vec;
			this.sb.append("{\"type\":\"Point\",\"coordinates\":");
			this.appendCoord2D(pt.getCenter());
			this.sb.append('}');
		}
		else if (vecType == VectorType.LineString)
		{
			LineString ls = (LineString)vec;
			this.sb.append("{\"type\":\"LineString\",\"coordinates\":");
			Coord2DDbl[] ptList = ls.getPointList();
			this.appendCoord2DArray(ptList);
			this.sb.append('}');
		}
		else if (vecType == VectorType.Polyline)
		{
			Polyline pl = (Polyline)vec;
			this.sb.append("{\"type\":\"MultiLineString\",\"coordinates\":");
			this.appendCoordPL(pl);
			this.sb.append('}');
		}
		else if (vecType == VectorType.Polygon)
		{
			Polygon pg = (Polygon)vec;
			this.sb.append("{\"type\":\"Polygon\",\"coordinates\":");
			this.appendCoordPG(pg);
			this.sb.append('}');
		}
		else if (vecType == VectorType.MultiPolygon)
		{
			MultiPolygon mpg = (MultiPolygon)vec;
			this.sb.append("{\"type\":\"MultiPolygon\",\"coordinates\":");
			Iterator<Polygon> it = mpg.iterator();
			this.sb.append('[');
			Polygon pg;
			if (it.hasNext())
			{
				pg = it.next();
				this.appendCoordPG(pg);
				while (it.hasNext())
				{
					pg = it.next();
					this.sb.append(',');
					this.appendCoordPG(pg);
				}
			}
			this.sb.append(']');
			this.sb.append('}');
		}
		else if (vecType == VectorType.CurvePolygon)
		{
			CurvePolygon cpg = (CurvePolygon)vec;
			Polygon pg = (Polygon)cpg.curveToLine();
			this.sb.append("{\"type\":\"Polygon\",\"coordinates\":");
			this.appendCoordPG(pg);
			this.sb.append('}');
		}
		else
		{
			this.sb.append("null");
			System.out.println("JSONBuilder: Unsupport Geometry Type: "+vecType.toString());
		}
	}

	private void appendObject(@Nonnull Object o, int level)
	{
		if (o instanceof Integer)
		{
			this.sb.append(((Integer)o).intValue());
		}
		else if (o instanceof Long)
		{
			this.sb.append(((Long)o).longValue());
		}
		else if (o instanceof String)
		{
			this.appendStr((String)o);
		}
		else if (o instanceof Boolean)
		{
			this.sb.append(((Boolean)o).booleanValue()?"true":"false");
		}
		else if (o instanceof Enum)
		{
			this.appendStr(o.toString());
		}
		else if (o instanceof Timestamp)
		{
			this.appendStr(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").format((Timestamp)o));
		}
		else if (o instanceof Collection)
		{
			Iterable<?> coll = (Iterable<?>) o;
			boolean isFirst = true;
			Iterator<?> it = coll.iterator();
			this.sb.append('[');
			while (it.hasNext())
			{
				if (isFirst)
					isFirst = false;
				else
					this.sb.append(',');
				this.appendObject(it.next(), level + 1);
			}
			this.sb.append(']');
		}
		else
		{
			if (level > 10)
			{
				this.sb.append("{}");
				return;
			}
			Set<String> names = new HashSet<String>();
			boolean isFirst = true;
			this.sb.append("{");
			Class<?> cls = o.getClass();
			Field[] fields = cls.getFields();
			String name;
			Object v;
			int k;
			boolean skip;
			int i = 0;
			int j = fields.length;
			while (i < j)
			{
				try
				{
					if ((fields[i].getModifiers() & Modifier.PUBLIC) != 0 && (fields[i].getModifiers() & Modifier.STATIC) == 0)
					{
						name = fields[i].getName();
						v = fields[i].get(o);
						if (!names.contains(name))
						{
							skip = false;
							Annotation[] ann = fields[i].getAnnotations();
							String clsName;
							k = ann.length;
							while (k-- > 0)
							{
								clsName = ann[k].getClass().toString();
								if (clsName.endsWith(".Tr"))
								{
									skip = true;
									break;
								}
							}
							if (!skip)
							{
								names.add(name);
								if (isFirst)
								{
									isFirst = false;
								}
								else
								{
									this.sb.append(',');
								}
								this.appendStr(name);
								this.sb.append(':');
								if (v == null)
								{
									this.sb.append("null");
								}
								else
								{
									this.appendObject(v, level + 1);
								}
							}
						}
					}
				}
				catch (IllegalAccessException|IllegalArgumentException ex)
				{
					ex.printStackTrace();
				}
				i++;
			}

			Method[] methods = cls.getMethods();
			Class<?> returnType;

			i = 0;
			j = methods.length;
			while (i < j)
			{
				try
				{
					if ((methods[i].getModifiers() & Modifier.STATIC) == 0 && methods[i].getParameterCount() == 0)
					{
						name = methods[i].getName();
						returnType = methods[i].getReturnType();
						if (!returnType.equals(void.class) && !name.equals("getClass"))
						{
							skip = true;
							if (returnType.equals(boolean.class) || returnType.equals(Boolean.class))
							{
								if (name.length() >= 4 && (name.startsWith("can") || name.startsWith("has")))
								{
									if (Character.isUpperCase(name.charAt(3)))
									{
										name = Character.toLowerCase(name.charAt(3))+name.substring(4);
										skip = false;
									}
								}
								else if (name.length() >= 3 && name.startsWith("is"))
								{
									if (Character.isUpperCase(name.charAt(2)))
									{
										name = Character.toLowerCase(name.charAt(2))+name.substring(3);
										skip = false;
									}
								}
							}
							else
							{
								if (name.length() >= 4 && name.startsWith("get"))
								{
									if (Character.isUpperCase(name.charAt(3)))
									{
										name = Character.toLowerCase(name.charAt(3))+name.substring(4);
										skip = false;
									}								
								}
							}
							if (!skip && !names.contains(name))
							{
								v = methods[i].invoke(o);
								names.add(name);
								if (isFirst)
								{
									isFirst = false;
								}
								else
								{
									this.sb.append(',');
								}
								this.appendStr(name);
								this.sb.append(':');
								if (v == null)
								{
									this.sb.append("null");
								}
								else
								{
									this.appendObject(v, level + 1);
								}								
							}
						}
					}
				}
				catch (InvocationTargetException|IllegalAccessException ex)
				{
					ex.printStackTrace();
				}
				i++;
			}
			this.sb.append("}");
		}
	}

	public JSONBuilder(@Nonnull ObjectType rootType)
	{
		this.objTypes = new ArrayList<ObjectType>();
		this.sb = new StringBuilder();
		this.currType = rootType;
		this.isFirst = true;
		if (rootType == ObjectType.OT_ARRAY)
		{
			this.sb.append('[');
		}
		else if (rootType == ObjectType.OT_OBJECT)
		{
			this.sb.append('{');
		}
	}

	public boolean arrayAddFloat64(double val)
	{
		if (this.currType != ObjectType.OT_ARRAY)
			return false;
		if (this.isFirst)
			this.isFirst = false;
		else
		{
			this.sb.append(",");
		}
		this.appendDouble(val);
		return true;
	}

	public boolean arrayAddInt32(int val)
	{
		if (this.currType != ObjectType.OT_ARRAY)
			return false;
		if (this.isFirst)
			this.isFirst = false;
		else
		{
			this.sb.append(",");
		}
		this.sb.append(val);
		return true;
	}

	public boolean arrayAddInt64(long val)
	{
		if (this.currType != ObjectType.OT_ARRAY)
			return false;
		if (this.isFirst)
			this.isFirst = false;
		else
		{
			this.sb.append(",");
		}
		this.sb.append(val);
		return true;
	}

	public boolean arrayAddStr(@Nullable String val)
	{
		if (this.currType != ObjectType.OT_ARRAY)
			return false;
		if (this.isFirst)
			this.isFirst = false;
		else
		{
			this.sb.append(",");
		}
		if (val == null)
			this.sb.append("null");
		else
			this.appendStr(val);
		return true;
	}

	public boolean arrayBeginObject()
	{
		if (this.currType != ObjectType.OT_ARRAY)
			return false;
		if (this.isFirst)
			this.isFirst = false;
		else
		{
			this.sb.append(",");
		}
		this.objTypes.add(ObjectType.OT_ARRAY);
		this.currType = ObjectType.OT_OBJECT;
		this.isFirst = true;
		this.sb.append('{');
		return true;
	}

	public boolean arrayBeginArray()
	{
		if (this.currType != ObjectType.OT_ARRAY)
			return false;
		if (this.isFirst)
			this.isFirst = false;
		else
		{
			this.sb.append(",");
		}
		this.objTypes.add(ObjectType.OT_ARRAY);
		this.currType = ObjectType.OT_ARRAY;
		this.isFirst = true;
		this.sb.append('[');
		return true;		
	}

	public boolean arrayEnd()
	{
		if (this.currType != ObjectType.OT_ARRAY)
			return false;
		int i = this.objTypes.size();
		if (i <= 0)
		{
			this.currType = ObjectType.OT_END;
			return true;
		}
		this.currType = this.objTypes.remove(i - 1);
		this.isFirst = false;
		this.sb.append(']');
		return true;
	}

	public boolean arrayAddObject(@Nullable Object obj)
	{
		if (this.currType != ObjectType.OT_ARRAY)
			return false;
		if (this.isFirst)
			this.isFirst = false;
		else
		{
			this.sb.append(",");
		}
		if (obj == null)
		{
			this.sb.append("null");
		}
		else
		{
			this.appendObject(obj, 0);
		}
		return true;
	}

	public boolean objectAddFloat64(@Nonnull String name, double val)
	{
		if (this.currType != ObjectType.OT_OBJECT)
			return false;
		if (this.isFirst)
			this.isFirst = false;
		else
		{
			this.sb.append(",");
		}
		this.appendStr(name);
		this.sb.append(":");
		this.appendDouble(val);
		return true;
	}

	public boolean objectAddNFloat64(@Nonnull String name, Double val)
	{
		if (this.currType != ObjectType.OT_OBJECT)
			return false;
		if (this.isFirst)
			this.isFirst = false;
		else
		{
			this.sb.append(",");
		}
		this.appendStr(name);
		this.sb.append(":");
		if (val == null)
			this.sb.append("null");
		else
			this.appendDouble(val.doubleValue());
		return true;
	}

	public boolean objectAddInt32(@Nonnull String name, int val)
	{
		if (this.currType != ObjectType.OT_OBJECT)
			return false;
		if (this.isFirst)
			this.isFirst = false;
		else
		{
			this.sb.append(",");
		}
		this.appendStr(name);
		this.sb.append(":");
		this.sb.append(val);
		return true;
	}

	public boolean objectAddNInt32(@Nonnull String name, @Nullable Integer val)
	{
		if (this.currType != ObjectType.OT_OBJECT)
			return false;
		if (this.isFirst)
			this.isFirst = false;
		else
		{
			this.sb.append(",");
		}
		this.appendStr(name);
		this.sb.append(":");
		if (val == null)
			this.sb.append("null");
		else
			this.sb.append(val.intValue());
		return true;
	}

	public boolean objectAddInt64(@Nonnull String name, long val)
	{
		if (this.currType != ObjectType.OT_OBJECT)
			return false;
		if (this.isFirst)
			this.isFirst = false;
		else
		{
			this.sb.append(",");
		}
		this.appendStr(name);
		this.sb.append(":");
		this.sb.append(val);
		return true;
	}

	public boolean objectAddBool(@Nonnull String name, boolean val)
	{
		if (this.currType != ObjectType.OT_OBJECT)
			return false;
		if (this.isFirst)
			this.isFirst = false;
		else
		{
			this.sb.append(",");
		}
		this.appendStr(name);
		this.sb.append(":");
		this.sb.append(val?"true":"false");
		return true;
	}

	public boolean objectAddStr(@Nonnull String name, String val)
	{
		if (this.currType != ObjectType.OT_OBJECT)
			return false;
		if (this.isFirst)
			this.isFirst = false;
		else
		{
			this.sb.append(",");
		}
		this.appendStr(name);
		this.sb.append(":");
		if (val == null)
		{
			this.sb.append("null");
		}
		else
		{
			this.appendStr(val);
		}
		return true;
	}

	public boolean objectAddArrayStr(@Nonnull String name, @Nonnull String value, @Nonnull String splitChar)
	{
		if (this.currType != ObjectType.OT_OBJECT)
			return false;
		if (this.isFirst)
			this.isFirst = false;
		else
		{
			this.sb.append(",");
		}
		this.appendStr(name);
		this.sb.append(':');
		this.sb.append('[');
		if (value.length() > 0)
		{
			String[] strs = StringUtil.split(value, splitChar);
			int i = 0;
			int j = strs.length;
			while (i < j)
			{
				if (i > 0)
					this.sb.append(",");
				this.appendStr(strs[i]);
				i++;
			}
		}
		this.sb.append(']');
		return true;
	}

	public boolean objectAddChar(@Nonnull String name, char val)
	{
		if (this.currType != ObjectType.OT_OBJECT)
			return false;
		if (this.isFirst)
			this.isFirst = false;
		else
		{
			this.sb.append(",");
		}
		this.appendStr(name);
		this.sb.append(":");
		if (val == 0)
		{
			this.sb.append("null");
		}
		else
		{
			this.appendStr(val+"");
		}
		return true;
	}


	public boolean objectAddTSStr(@Nonnull String name, @Nullable Timestamp ts)
	{
		if (this.currType != ObjectType.OT_OBJECT)
			return false;
		if (this.isFirst)
			this.isFirst = false;
		else
		{
			this.sb.append(",");
		}
		this.appendStr(name);
		this.sb.append(':');
		this.appendTSStr(ts);
		return true;
	}

	public boolean objectAddDateStr(@Nonnull String name, @Nullable Date dat)
	{
		if (this.currType != ObjectType.OT_OBJECT)
			return false;
		if (this.isFirst)
			this.isFirst = false;
		else
		{
			this.sb.append(",");
		}
		this.appendStr(name);
		this.sb.append(':');
		this.appendDateStr(dat);
		return true;
	}

	public boolean objectAddNull(@Nonnull String name)
	{
		if (this.currType != ObjectType.OT_OBJECT)
			return false;
		if (this.isFirst)
			this.isFirst = false;
		else
		{
			this.sb.append(",");
		}
		this.appendStr(name);
		this.sb.append(":null");
		return true;
	}

	public boolean objectAddGeometry(@Nonnull String name, @Nonnull Geometry geom)
	{
		return objectAddGeometry(name, GeometryUtil.toVector2D(geom));
	}
	
	public boolean objectAddGeometry(@Nonnull String name, @Nullable Vector2D vec)
	{
		if (this.currType != ObjectType.OT_OBJECT)
			return false;
		if (this.isFirst)
			this.isFirst = false;
		else
		{
			this.sb.append(",");
		}
		this.appendStr(name);
		this.sb.append(':');
		if (vec != null)
		{
			this.appendGeometry(vec);
		}
		else
		{
			this.sb.append("null");
		}
		return true;
	}

	public boolean objectBeginArray(@Nonnull String name)
	{
		if (this.currType != ObjectType.OT_OBJECT)
			return false;
		if (this.isFirst)
			this.isFirst = false;
		else
		{
			this.sb.append(",");
		}
		this.appendStr(name);
		this.sb.append(":[");
		this.objTypes.add(ObjectType.OT_OBJECT);
		this.currType = ObjectType.OT_ARRAY;
		this.isFirst = true;
		return true;
	}

	public boolean objectBeginObject(@Nonnull String name)
	{
		if (this.currType != ObjectType.OT_OBJECT)
			return false;
		if (this.isFirst)
			this.isFirst = false;
		else
		{
			this.sb.append(",");
		}
		this.appendStr(name);
		this.sb.append(":{");
		this.objTypes.add(ObjectType.OT_OBJECT);
		this.currType = ObjectType.OT_OBJECT;
		this.isFirst = true;
		return true;
	}

	public boolean objectEnd()
	{
		if (this.currType != ObjectType.OT_OBJECT)
			return false;
		int i = this.objTypes.size();
		if (i <= 0)
		{
			this.currType = ObjectType.OT_END;
			return true;
		}
		this.currType = this.objTypes.remove(i - 1);
		this.isFirst = false;
		this.sb.append('}');
		return true;

	}

	public boolean objectAddObject(@Nonnull String name, @Nullable Object o)
	{
		if (this.currType != ObjectType.OT_OBJECT)
			return false;
		if (this.isFirst)
			this.isFirst = false;
		else
		{
			this.sb.append(",");
		}
		this.appendStr(name);
		this.sb.append(":");
		if (o == null)
		{
			this.sb.append("null");
		}
		else
		{
			this.appendObject(o, 0);
		}
		return true;
	}

	public void endBuild()
	{
		int i;
		if (this.currType == ObjectType.OT_ARRAY)
		{
			this.sb.append(']');
		}
		else if (this.currType == ObjectType.OT_OBJECT)
		{
			this.sb.append('}');
		}
		i = this.objTypes.size();
		while (i-- > 0)
		{
			if (this.objTypes.get(i) == ObjectType.OT_OBJECT)
			{
				this.sb.append('}');
			}
			else
			{
				this.sb.append(']');
			}
		}
		this.currType = ObjectType.OT_END;		
	}

	@Nonnull
	public String toString()
	{
		this.endBuild();
		return this.sb.toString();
	}

	@Nonnull
	public static String build(@Nonnull Object o)
	{
		JSONBuilder json = new JSONBuilder(ObjectType.OT_END);
		json.appendObject(o, 0);
		return json.toString();
	}
}
