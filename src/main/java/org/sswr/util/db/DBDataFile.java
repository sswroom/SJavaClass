package org.sswr.util.db;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.sswr.util.data.ByteTool;
import org.sswr.util.data.FieldGetter;
import org.sswr.util.data.FieldSetter;
import org.sswr.util.data.SharedInt;
import org.sswr.util.data.UUID;

public class DBDataFile<T>
{
	public enum ItemType
	{
		Unknown,
		Null,
		Str,
		Date,
		F32,
		F64,
		I8,
		U8,
		I16,
		U16,
		I32,
		U32,
		I64,
		U64,
		BOOL,
		ByteArr,
		Vector,
		UUID
	}

	private FileOutputStream fs;
	private byte[] recordBuff;
	private ItemType []colTypes;
	private List<FieldGetter<T>> getters;

	private static ItemType getItemType(Field field)
	{
		Class<?> fieldType = field.getType();
		if (fieldType.equals(Integer.class) || fieldType.equals(int.class))
		{
			return ItemType.I32;
		}
		else if (fieldType.equals(Long.class) || fieldType.equals(long.class))
		{
			return ItemType.F64;
		}
		else if (fieldType.equals(Double.class) || fieldType.equals(double.class))
		{
			return ItemType.F64;
		}
		else if (fieldType.equals(Float.class) || fieldType.equals(float.class))
		{
			return ItemType.F32;
		}
		else if (fieldType.equals(Boolean.class) || fieldType.equals(boolean.class))
		{
			return ItemType.BOOL;
		}
		else if (fieldType.equals(String.class))
		{
			return ItemType.Str;
		}
		else if (fieldType.equals(Timestamp.class))
		{
			return ItemType.Date;
		}
		else
		{
			return ItemType.Unknown;
		}
	}

	private static int readInt(byte[] buff, int ofst, SharedInt outVal)
	{
		int val = 0;
		byte v;
		while (true)
		{
			v = buff[ofst];
			ofst++;
			if ((v & 0x80) != 0)
			{
				val = (val << 7) | (v & 0x7F);
			}
			else
			{
				outVal.value = (val << 7) | v;
				return ofst;
			}
		}
	}
	
	private static int writeInt(byte[] buff, int ofst, int val)
	{
		if (val < 0x80) // 00 - 7f
		{
			buff[ofst] = (byte)val;
			return ofst + 1;
		}
		else if (val <= 0x3F7F)
		{
			buff[ofst] = (byte)(0x80 | (val >> 7));
			buff[ofst + 1] = (byte)(val & 0x7F);
			return ofst + 2;
		}
		else if (val <= 0x1FBFFF)
		{
			buff[ofst] = (byte)(0x80 | (val >> 14));
			buff[ofst + 1] = (byte)(0x80 | ((val >> 7) & 0x7F));
			buff[ofst + 2] = (byte)(val & 0x7F);
			return ofst + 3;
		}
		else if (val <= 0xFDFFFFF)
		{
			buff[ofst] = (byte)(0x80 | (val >> 21));
			buff[ofst + 1] = (byte)(0x80 | ((val >> 14) & 0x7F));
			buff[ofst + 2] = (byte)(0x80 | ((val >> 7) & 0x7F));
			buff[ofst + 3] = (byte)(val & 0x7F);
			return ofst + 4;
		}
		else
		{
			buff[ofst] = (byte)(0x80 | (val >> 28));
			buff[ofst + 1] = (byte)(0x80 | ((val >> 21) & 0x7F));
			buff[ofst + 2] = (byte)(0x80 | ((val >> 14) & 0x7F));
			buff[ofst + 3] = (byte)(0x80 | ((val >> 7) & 0x7F));
			buff[ofst + 4] = (byte)(val & 0x7F);
			return ofst + 5;
		}
	}
	
	public DBDataFile(String fileName, Class<T> cls, String[] fieldOrder, boolean append) throws FileNotFoundException, IOException, NoSuchFieldException
	{
		this.recordBuff = null;
		this.colTypes = null;
		this.getters = null;
		this.fs = new FileOutputStream(fileName, append);
		int k = 0;
		int l = fieldOrder.length;
		this.recordBuff = new byte[65536];
		this.colTypes = new ItemType[l];
		this.getters = new ArrayList<FieldGetter<T>>(l);
		try
		{
			if (this.fs.getChannel().position() == 0)
			{
				this.recordBuff[0] = 'S';
				this.recordBuff[1] = 'M';
				this.recordBuff[2] = 'D';
				this.recordBuff[3] = 'f';
				while (k < l)
				{
					Field field = cls.getDeclaredField(fieldOrder[k]);
					this.getters.add(new FieldGetter<T>(field));
					this.recordBuff[k + 4] = (byte)(this.colTypes[k] = getItemType(field)).ordinal();
					k++;
				}
				this.recordBuff[l + 4] = -1;
				this.fs.write(this.recordBuff, 0, l + 5);
			}
			else
			{
				while (k < l)
				{
					Field field = cls.getDeclaredField(fieldOrder[k]);
					this.getters.add(new FieldGetter<T>(field));
					this.colTypes[k] = getItemType(field);
					k++;
				}
			}
		}
		catch (Exception ex)
		{
			try
			{
				this.fs.close();
			}
			catch (IOException ex2)
			{

			}
			throw ex;
		}
	}
	
	public void close()
	{
		try
		{
			this.fs.close();
		}
		catch (IOException ex2)
		{

		}
	}

	public boolean addRecord(T obj)
	{
		try
		{
			int k;
			int l;
			int m;
			m = 6;
			k = 0;
			l = this.colTypes.length;
			while (k < l)
			{
				switch (this.colTypes[k])
				{
				case F32:
					{
						Float o = (Float)this.getters.get(k).get(obj);
						if (o == null)
						{
							ByteTool.writeSingle(this.recordBuff, m, 0);
						}
						else
						{
							ByteTool.writeSingle(this.recordBuff, m, o.floatValue());
						}
						m += 4;
					}
					break;
				case F64:
					{
						Double o = (Double)this.getters.get(k).get(obj);
						if (o == null)
						{
							ByteTool.writeDouble(this.recordBuff, m, 0);
						}
						else
						{
							ByteTool.writeDouble(this.recordBuff, m, o.doubleValue());
						}
						m += 8;
					}
					break;
				case I8:
					{
						Integer o = (Integer)this.getters.get(k).get(obj);
						if (o == null)
						{
							this.recordBuff[m] = 0;
						}
						else
						{
							this.recordBuff[m] = (byte)(o.intValue() & 0xff);
						}
						m += 1;
					}
					break;
				case U8:
					{
						Integer o = (Integer)this.getters.get(k).get(obj);
						if (o == null)
						{
							this.recordBuff[m] = 0;
						}
						else
						{
							this.recordBuff[m] = (byte)(o.intValue() & 0xff);
						}
						m += 1;
					}
					break;
				case I16:
					{
						Integer o = (Integer)this.getters.get(k).get(obj);
						if (o == null)
						{
							ByteTool.writeInt16(this.recordBuff, m, 0);
						}
						else
						{
							ByteTool.writeInt16(this.recordBuff, m, o.intValue());
						}
						m += 2;
					}
					break;
				case U16:
					{
						Integer o = (Integer)this.getters.get(k).get(obj);
						if (o == null)
						{
							ByteTool.writeInt16(this.recordBuff, m, 0);
						}
						else
						{
							ByteTool.writeInt16(this.recordBuff, m, o.intValue());
						}
						m += 2;
					}
					break;
				case I32:
					{
						Integer o = (Integer)this.getters.get(k).get(obj);
						if (o == null)
						{
							ByteTool.writeInt32(this.recordBuff, m, 0);
						}
						else
						{
							ByteTool.writeInt32(this.recordBuff, m, o.intValue());
						}
						m += 4;
					}
					break;
				case U32:
					{
						Integer o = (Integer)this.getters.get(k).get(obj);
						if (o == null)
						{
							ByteTool.writeInt32(this.recordBuff, m, 0);
						}
						else
						{
							ByteTool.writeInt32(this.recordBuff, m, o.intValue());
						}
						m += 4;
					}
					break;
				case I64:
					{
						Long o = (Long)this.getters.get(k).get(obj);
						if (o == null)
						{
							ByteTool.writeInt64(this.recordBuff, m, 0);
						}
						else
						{
							ByteTool.writeInt64(this.recordBuff, m, o.longValue());
						}
						m += 8;
					}
					break;
				case U64:
					{
						Long o = (Long)this.getters.get(k).get(obj);
						if (o == null)
						{
							ByteTool.writeInt64(this.recordBuff, m, 0);
						}
						else
						{
							ByteTool.writeInt64(this.recordBuff, m, o.longValue());
						}
						m += 8;
					}
					break;
				case BOOL:
					{
						Boolean o = (Boolean)this.getters.get(k).get(obj);
						if (o == null)
						{
							this.recordBuff[m] = 0;
						}
						else
						{
							this.recordBuff[m] = (byte)(o.booleanValue()?1:0);
						}
						m += 1;
					}
					break;
				case Str:
					{
						String o = (String)this.getters.get(k).get(obj);
						if (o == null)
						{
							this.recordBuff[m] = (byte)-1;
							m += 1;
						}
						else
						{
							byte []barr = o.getBytes(StandardCharsets.UTF_8);
							m = writeInt(this.recordBuff, m, barr.length);
							ByteTool.copyArray(this.recordBuff, m, barr, 0, barr.length);
							m += barr.length;
						}
					}
					break;
				case Date:
					{
						Timestamp o = (Timestamp)this.getters.get(k).get(obj);
						if (o == null)
						{
							ByteTool.writeInt64(this.recordBuff, m, -1);
							m += 8;
						}
						else
						{
							ByteTool.writeInt64(this.recordBuff, m, o.getTime());
							m += 8;
						}
					}
					break;
				case ByteArr:
					{
						byte[] o = (byte[])this.getters.get(k).get(obj);
						if (o == null)
						{
							this.recordBuff[m] = -1;
							m += 1;
						}
						else
						{
							m = writeInt(this.recordBuff, m, o.length);
							ByteTool.copyArray(this.recordBuff, m, o, 0, o.length);
							m += o.length;
						}
					}
					break;
				case Vector:
					//////////////////////////////////
					break;
				case UUID:
					{
						UUID o = (UUID)this.getters.get(k).get(obj);
						if (o == null)
						{
							ByteTool.writeInt64(this.recordBuff, m, -1);
							ByteTool.writeInt64(this.recordBuff, m + 8, -1);
							m += 16;
						}
						else
						{
							o.getValue(this.recordBuff, m);
							m += 16;
						}
					}
					break;
				case Null:
				case Unknown:
				default:
					this.recordBuff[m] = -1;
					m += 1;
					break;
				}
				k++;
			}
			m -= 6;
			if (m < 0x80) // 00 - 7f
			{
				this.recordBuff[5] = (byte)m;
				this.fs.write(this.recordBuff, 5, m + 1);
			}
			else if (m <= 0x3F7F)
			{
				this.recordBuff[4] = (byte)(0x80 | (m >> 7));
				this.recordBuff[5] = (byte)(m & 0x7F);
				this.fs.write(this.recordBuff, 4, m + 2);
			}
			else if (m <= 0x1FBFFF)
			{
				this.recordBuff[3] = (byte)(0x80 | (m >> 14));
				this.recordBuff[4] = (byte)(0x80 | ((m >> 7) & 0x7F));
				this.recordBuff[5] = (byte)(m & 0x7F);
				this.fs.write(this.recordBuff, 3, m + 3);
			}
			else
			{
				this.recordBuff[2] = (byte)(0x80 | (m >> 21));
				this.recordBuff[3] = (byte)(0x80 | ((m >> 14) & 0x7F));
				this.recordBuff[4] = (byte)(0x80 | ((m >> 7) & 0x7F));
				this.recordBuff[5] = (byte)(m & 0x7F);
				this.fs.write(this.recordBuff, 2, m + 4);
			}
			return true;
		}
		catch (IllegalAccessException ex)
		{
			ex.printStackTrace();
			return false;
		}
		catch (InvocationTargetException ex)
		{
			ex.printStackTrace();
			return false;
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
			return false;
		}
	}

	public static <T> List<T> loadFile(String fileName, Class<T> cls, String []fieldOrder)
	{
		try
		{
			Constructor<T> constr = cls.getConstructor();
			int maxBuffSize = 65536;
			FileInputStream fis = new FileInputStream(fileName);
			byte []buff = new byte[maxBuffSize];
			int k;
			int l;
			int m;
			int m2;
			SharedInt m3 = new SharedInt();
			SharedInt rowSize = new SharedInt();
			boolean succ;
			int buffSize;
			ItemType []colTypes = new ItemType[fieldOrder.length];
			FieldSetter []setters = new FieldSetter[fieldOrder.length];
			List<T> retList = null;
			buffSize = fis.read(buff, 0, maxBuffSize);
			if (buff[0] == 'S' && buff[1] == 'M' && buff[2] == 'D' && buff[3] == 'f')
			{
				succ = true;
				k = 0;
				l = fieldOrder.length;
				while (k < l)
				{
					try
					{
						Field field = cls.getDeclaredField(fieldOrder[k]);
						colTypes[k] = getItemType(field);
						if (colTypes[k] == ItemType.Unknown)
						{
							succ = false;
							break;
						}
						setters[k] = new FieldSetter(field);
						
						if (buff[k + 4] != colTypes[k].ordinal())
						{
							succ = false;
							break;
						}
					}
					catch (NoSuchFieldException ex)
					{
						succ = false;
						break;
					}
					k++;
				}
				if (buff[l + 4] != -1)
				{
					succ = false;
				}
				m = l + 5;
				if (succ)
				{
					retList = new ArrayList<T>();
					while (succ)
					{
						if (m + 4 > buffSize)
						{
							if (m == 0)
							{
							}
							else if (m != buffSize)
							{
								ByteTool.copyArray(buff, 0, buff, m, buffSize - m);
								buffSize -= m;
								m = 0;
							}
							else
							{
								buffSize = 0;
								m = 0;
							}
							m2 = fis.read(buff, buffSize, maxBuffSize - buffSize);
							if (m2 <= 0)
							{
								break;
							}
							buffSize += m2;
						}
						m2 = readInt(buff, m, rowSize);
						while (m2 + rowSize.value > buffSize)
						{
							if (m == 0)
							{
							}
							else if (m != buffSize)
							{
								ByteTool.copyArray(buff, 0, buff, m, buffSize - m);
								buffSize -= m;
								m2 -= m;
								m = 0;
							}
							else
							{
								buffSize = 0;
								m2 -= m;
								m = 0;
							}
							k = fis.read(buff, buffSize, maxBuffSize - buffSize);
							if (k == 0)
							{
								succ = false;
								break;
							}
							buffSize += k;
						}
						if (succ)
						{
							try
							{
								T obj = constr.newInstance();
								m = m2 + rowSize.value;
								k = 0;
								while (k < l)
								{
									switch (colTypes[k])
									{
									case F32:
										setters[k].set(obj, ByteTool.readSingle(buff, m2));
										m2 += 4;
										break;
									case F64:
										setters[k].set(obj, ByteTool.readDouble(buff, m2));
										m2 += 8;
										break;
									case I8:
										setters[k].set(obj, buff[m2]);
										m2 += 1;
										break;
									case U8:
										setters[k].set(obj, buff[m2]);
										m2 += 1;
										break;
									case I16:
										setters[k].set(obj, ByteTool.readInt16(buff, m2));
										m2 += 2;
										break;
									case U16:
										setters[k].set(obj, ByteTool.readUInt16(buff, m2));
										m2 += 2;
										break;
									case I32:
										setters[k].set(obj, ByteTool.readInt32(buff, m2));
										m2 += 4;
										break;
									case U32:
										setters[k].set(obj, ByteTool.readInt32(buff, m2));
										m2 += 4;
										break;
									case I64:
										setters[k].set(obj, ByteTool.readInt64(buff, m2));
										m2 += 8;
										break;
									case U64:
										setters[k].set(obj, ByteTool.readInt64(buff, m2));
										m2 += 8;
										break;
									case BOOL:
										setters[k].set(obj, buff[m2] != 0);
										m2 += 1;
										break;
									case Str:
										if (buff[m2] == -1)
										{
											m2 += 1;
										}
										else
										{
											m2 = readInt(buff, m2, m3);
											setters[k].set(obj, new String(buff, m2, m3.value, StandardCharsets.UTF_8));
											m2 += m3.value;
										}
										break;
									case Date:
										{
											long ticks = ByteTool.readInt64(buff, m2);
											if (ticks == -1)
											{
											}
											else
											{
												setters[k].set(obj, new Timestamp(ticks));
											}
											m2 += 8;
										}
										break;
									case ByteArr:
										if (buff[m2] == -1)
										{
											m2 += 1;
										}
										else
										{
											m2 = readInt(buff, m2, m3);
											setters[k].set(obj, Arrays.copyOfRange(buff, m2, m2 + m3.value));
											m2 += m3.value;
										}
										break;
									case Vector:
										//////////////////////////////////
										break;
									case UUID:
										{
											setters[k].set(obj, new UUID(buff, m2));
											m2 += 16;
										}
										break;
									case Null:
									case Unknown:
									default:
										m2 += 1;
										break;
									}
									k++;
								}
								retList.add(obj);
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
							catch (IllegalArgumentException ex)
							{
								ex.printStackTrace();
								succ = false;
							}
							catch (InstantiationException ex)
							{
								ex.printStackTrace();
							}
						}
					}
				}
			}

			fis.close();
			return retList;
		}
		catch (FileNotFoundException ex)
		{
			return null;
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
			return null;
		}
		catch (NoSuchMethodException ex)
		{
			ex.printStackTrace();
			return null;
		}
	}

	public static <T> boolean saveFile(String fileName, List<T> dataList, Class<T> cls, String []fieldOrder) throws FileNotFoundException
	{
		try
		{
			DBDataFile<T> file = new DBDataFile<T>(fileName, cls, fieldOrder, false);
			boolean succ = true;
			int i = 0;
			int j = dataList.size();
			while (succ && i < j)
			{
				succ = succ && file.addRecord(dataList.get(i));
				i++;
			}
			file.close();
			return succ;
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
			return false;
		}
		catch (NoSuchFieldException ex)
		{
			ex.printStackTrace();
			return false;
		}
	}
}
