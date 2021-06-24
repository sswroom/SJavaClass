package org.sswr.util.db;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.sswr.util.data.FieldGetter;

public class CSVUtil {
	public static String quote(String s)
	{
		if (s == null)
		{
			return "\"\"";
		}
		return "\""+s.replace("\"", "\"\"")+"\"";
	}
	public static String join(String s[])
	{
		StringBuilder sb = new StringBuilder();
		int i = 0;
		int j = s.length;
		while (i < j)
		{
			if (i > 0) sb.append(",");
			sb.append(quote(s[i]));
			i++;
		}
		return sb.toString();
	}

	public static String[] readLine(BufferedReader reader) throws IOException
	{
		ArrayList<String> cols;
		boolean isQuoted = false;
		StringBuilder sb = new StringBuilder();
		cols = new ArrayList<String>();
		while (true)
		{
			String line = reader.readLine();
			if (line == null)
			{
				return null;
			}

			int i = 0;
			int j = line.length();
			char c;
			while (i < j)
			{
				c = line.charAt(i);
				if (c == '"')
				{
					if (!isQuoted)
					{
						isQuoted = true;
					}
					else if (i + 1 < j && line.charAt(i + 1) == '"')
					{
						i++;
						sb.append(c);
					}
					else
					{
						isQuoted = false;
					}
				}
				else if (isQuoted)
				{
					sb.append(c);
				}
				else if (c == ',')
				{
					cols.add(sb.toString());
					sb.setLength(0);
				}
				else
				{
					sb.append(c);
				}
				i++;
			}
			if (!isQuoted)
			{
				cols.add(sb.toString());
				break;
			}
			sb.append("\r\n");
		}
		return cols.toArray(new String[cols.size()]);
	}

	private static <T> void appendRow(StringBuilder sb, T data, List<FieldGetter<T>> getters) throws IllegalAccessException, InvocationTargetException
	{
		int i = 0;
		int j = getters.size();
		while (i < j)
		{
			if (i > 0)
			{
				sb.append(",");
			}
			Object o;
			try
			{
				o = getters.get(i).get(data);
			}
			catch (Exception ex)
			{
				o = null;
			}
			if (o == null)
			{
				sb.append("\"\"");
			}
			else
			{
				sb.append(quote(o.toString()));
			}
			i++;
		}
	}

	public static <T> boolean createFile(Writer writer, Iterable<T> datas, String cols[])
	{
		try
		{
			StringBuilder sb;
			int i = 0;
			int j = cols.length;
			sb = new StringBuilder();
			while (i < j)
			{
				if (i > 0)
				{
					sb.append(",");
				}
				sb.append(quote(cols[i]));
				i++;
			}
			sb.append("\r\n");
			writer.write(sb.toString());
			
			boolean succ = true;

			try
			{
				Iterator<T> it = datas.iterator();
				if (it.hasNext())
				{
					T data = it.next();
					ArrayList<FieldGetter<T>> getters = new ArrayList<FieldGetter<T>>(j);
					i = 0;
					while (i < j)
					{
						getters.add(new FieldGetter<T>(data.getClass(), cols[i]));
						i++;
					}

					sb.setLength(0);
					appendRow(sb, data, getters);
					sb.append("\r\n");
					writer.write(sb.toString());
					while (it.hasNext())
					{
						data = it.next();
						sb.setLength(0);
						appendRow(sb, data, getters);
						sb.append("\r\n");
						writer.write(sb.toString());
					}
				}
			}
			catch (NoSuchFieldException ex)
			{
				ex.printStackTrace();
				succ = false;
			}
			catch (IllegalAccessException ex)
			{
				ex.printStackTrace();
				succ = false;
			}
			catch (InvocationTargetException ex)
			{
				ex.printStackTrace();
				succ = false;
			}

			return succ;
		}
		catch (IOException ex)
		{
			return false;
		}
	}

	public static <T> boolean createFile(File file, Charset cs, Iterable<T> datas, String cols[])
	{
		try
		{
			return createFile(new FileWriter(file, cs), datas, cols);
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
			return false;
		}
	}

	public static boolean createFile(Writer writer, Connection conn, String sql)
	{
		try
		{
			StringBuilder sb = new StringBuilder();
			boolean succ = true;
			try
			{
				PreparedStatement stmt = conn.prepareStatement(sql);
				ResultSet rs = stmt.executeQuery();
				ResultSetMetaData metadata = rs.getMetaData();
				int i = 0;
				int j = metadata.getColumnCount();
				while (i < j)
				{
					if (i > 0)
					{
						sb.append(",");
					}
					sb.append(quote(metadata.getColumnLabel(i + 1)));
					i++;
				}
				sb.append("\r\n");
				writer.write(sb.toString());
				
				while (rs.next())
				{
					sb.setLength(0);
					i = 0;
					while (i < j)
					{
						if (i > 0)
						{
							sb.append(',');
						}
						
						switch (metadata.getColumnClassName(i + 1))
						{
						case "java.lang.Integer":
							int iVal = rs.getInt(i + 1);
							if (rs.wasNull())
							{
								sb.append("\"\"");
							}
							else
							{
								sb.append("\""+iVal+"\"");
							}
							break;
						case "java.lang.Short":
							short sVal = rs.getShort(i + 1);
							if (rs.wasNull())
							{
								sb.append("\"\"");
							}
							else
							{
								sb.append("\""+sVal+"\"");
							}
							break;
						case "java.lang.String":
							sb.append(quote(rs.getString(i + 1)));
							break;
						case "java.lang.Float":
							float fVal = rs.getFloat(i + 1);
							if (rs.wasNull())
							{
								sb.append("\"\"");
							}
							else
							{
								sb.append("\""+fVal+"\"");
							}
							break;
						case "java.lang.Double":
							double dVal = rs.getDouble(i + 1);
							if (rs.wasNull())
							{
								sb.append("\"\"");
							}
							else
							{
								sb.append("\""+dVal+"\"");
							}
							break;
						case "java.sql.Date":
							Date date = rs.getDate(i + 1);
							if (date == null)
							{
								sb.append("\"\"");
							}
							else
							{
								sb.append("\""+date+"\"");
							}
							break;
						case "java.sql.Timestamp":
							Timestamp ts = rs.getTimestamp(i + 1);
							if (ts == null)
							{
								sb.append("\"\"");
							}
							else
							{
								sb.append("\""+ts+"\"");
							}
							break;
						case "java.time.LocalDateTime":
							LocalDateTime ldt = (LocalDateTime)rs.getObject(i + 1);
							if (ldt == null)
							{
								sb.append("\"\"");
							}
							else
							{
								sb.append("\""+ldt+"\"");
							}
							break;
						case "java.math.BigDecimal":
							BigDecimal bd = rs.getBigDecimal(i + 1);
							if (bd == null)
							{
								sb.append("\"\"");
							}
							else
							{
								sb.append("\""+bd+"\"");
							}
							break;
						case "[B":
							byte barr[] = rs.getBytes(i + 1);
							if (barr == null)
							{
								sb.append("\"\"");
							}
							else
							{
								String hex = "0123456789ABCDEF";
								int k = 0;
								int l = barr.length;
								sb.append('"');
								while (k < l)
								{
									sb.append(hex.charAt((barr[k] >> 4) & 0xf));
									sb.append(hex.charAt(barr[k] & 0xf));
									k++;
								}
								sb.append('"');
							}
							break;
						default:
							System.out.println("Unknown type: "+metadata.getColumnClassName(i + 1));
							sb.append(quote(rs.getString(i + 1)));
							break;
						}
						i++;
					}
					sb.append("\r\n");
					writer.write(sb.toString());
				}
			}
			catch (SQLException ex)
			{
				ex.printStackTrace();
				succ = false;
			}

			return succ;
		}
		catch (IOException ex)
		{
			return false;
		}
	}	

	public static boolean createFile(File file, Charset cs, Connection conn, String sql)
	{
		try
		{
			return createFile(new FileWriter(file, cs), conn, sql);
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
			return false;
		}
	}
}
