package org.sswr.util.io;

import java.util.List;

import org.sswr.util.data.JSText;
import org.sswr.util.data.StringUtil;
import org.sswr.util.data.TableData;
import org.sswr.util.db.DBReader;
import org.sswr.util.media.Size2DInt;
import org.sswr.util.media.StandardColor;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class TextWriteUtil
{
	public static void writeString(@Nonnull StyledTextWriter writer, @Nonnull String s)
	{
		String nns = JSText.toJSText(s);
		writer.setTextColor(StandardColor.Blue);
		writer.write(nns);
		writer.resetTextColor();
	}

	public static void writeArrayRange(@Nonnull StyledTextWriter writer, @Nonnull List<String> arr, int startIndex, int endIndex)
	{
		writer.writeChar((byte)'[');
		if (endIndex > arr.size())
		{
			endIndex = arr.size();
		}
		if (startIndex < endIndex)
		{
			writeString(writer, arr.get(startIndex));
			startIndex++;
			while (startIndex < endIndex)
			{
				writer.write(", ");
				writeString(writer, arr.get(startIndex));
				startIndex++;
			}
		}
		writer.writeChar((byte)']');		
	}

	public static void writeArrayIntRange(@Nonnull StyledTextWriter writer, @Nonnull List<Integer> arr, int startIndex, int endIndex)
	{
		String s;
		writer.writeChar((byte)'[');
		if (startIndex < 0)
		{
			startIndex = 0;
		}
		if (endIndex > arr.size())
		{
			endIndex = arr.size();
		}
		if (startIndex < endIndex)
		{
			s = String.valueOf(arr.get(startIndex));
			writer.setTextColor(StandardColor.Magenta);
			writer.write(s);
			writer.resetTextColor();
			startIndex++;
			while (startIndex < endIndex)
			{
				writer.write(", ");
				s = String.valueOf(arr.get(startIndex));
				writer.setTextColor(StandardColor.Magenta);
				writer.write(s);
				writer.resetTextColor();
				startIndex++;
			}
		}
		writer.writeChar((byte)']');
	}

	public static void writeArray(@Nonnull StyledTextWriter writer, @Nonnull List<String> arr)
	{
		writeArrayRange(writer, arr, 0, arr.size());
	}

	public static void writeArrayInt(@Nonnull StyledTextWriter writer, @Nonnull List<Integer> arr)
	{
		writeArrayIntRange(writer, arr, 0, arr.size());
	}

	public static void writeTableData(@Nonnull StyledTextWriter writer, @Nonnull TableData data)
	{
		String s;
		DBReader r;
		if ((r = data.getTableData()) == null)
		{
			writer.setTextColor(StandardColor.Red);
			writer.writeLine("Error in reading TableData");
			writer.resetTextColor();
			return;
		}
		Size2DInt thisSize;
		int colCnt = r.colCount();
		int[] maxW = new int[colCnt];
		int i;
		i = 0;
		while (i < colCnt)
		{
			if ((s = r.getName(i)) != null)
			{
				thisSize = StringUtil.getMonospaceSize(s);
				maxW[i] = thisSize.getWidth();
			}
			else
			{
				maxW[i] = 6;
			}
			i++;
		}
		while (r.readNext())
		{
			i = 0;
			while (i < colCnt)
			{
				if ((s = r.getString(i)) != null)
				{
					thisSize = StringUtil.getMonospaceSize(s);
				}
				else
				{
					thisSize = new Size2DInt(6, 1);
				}
				if (thisSize.getWidth() > maxW[i])
				{
					maxW[i] = thisSize.getWidth();
				}
				i++;
			}
		}
		data.closeReader(r);

		if ((r = data.getTableData()) == null)
		{
			writer.setTextColor(StandardColor.Red);
			writer.writeLine("Error in reading TableData");
			writer.resetTextColor();
			return;
		}

		boolean hasMoreLine;
		int j;
		String[] valArr = new String[colCnt];
		String[] sArr = new String[colCnt];
		hasMoreLine = false;
		i = 0;
		while (i < colCnt)
		{
			if (i > 0)
				writer.writeChar((byte)'|');
			if ((s = r.getName(i)) != null)
			{
				valArr[i] = s;
				writer.setTextColor(StandardColor.ConsoleDarkGreen);
				sArr[i] = s;
				sArr[i] = writeColumnLine(writer, sArr[i], maxW[i]);
				writer.resetTextColor();
				if (sArr[i] != null)
					hasMoreLine = true;
				else
				{
					valArr[i] = null;
				}
			}
			else
			{
				writer.setTextColor(StandardColor.DarkGray);
				writer.write("(null)");
				j = 6;
				while (j < maxW[i])
				{
					writer.writeChar((byte)' ');
					j++;
				}
				writer.resetTextColor();
				valArr[i] = null;
			}
			i++;
		}
		writer.writeLine();
		while (hasMoreLine)
		{
			hasMoreLine = false;
			if (i > 0)
				writer.writeChar((byte)'|');
			if ((s = valArr[i]) != null)
			{
				writer.setTextColor(StandardColor.ConsoleDarkGreen);
				sArr[i] = writeColumnLine(writer, sArr[i], maxW[i]);
				writer.resetTextColor();
				if (sArr[i] != null)
					hasMoreLine = true;
				else
				{
					valArr[i] = null;
				}
			}
			else
			{
				j = 0;
				while (j < maxW[i])
				{
					writer.writeChar((byte)' ');
					j++;
				}
			}
		}
		i = 0;
		while (i < colCnt)
		{
			if (i > 0)
				writer.writeChar((byte)'+');
			j = 0;
			while (j < maxW[i])
			{
				writer.writeChar((byte)'-');
				j++;
			}
			i++;
		}
		writer.writeLine();
		while (r.readNext())
		{
			hasMoreLine = false;
			i = 0;
			while (i < colCnt)
			{
				if (i > 0)
					writer.writeChar((byte)'|');
				if ((s = r.getString(i)) != null)
				{
					valArr[i] = s;
					writer.setTextColor(StandardColor.Yellow);
					sArr[i] = s;
					sArr[i] = writeColumnLine(writer, sArr[i], maxW[i]);
					writer.resetTextColor();
					if (sArr[i] != null)
						hasMoreLine = true;
					else
					{
						valArr[i] = null;
					}
				}
				else
				{
					writer.setTextColor(StandardColor.DarkGray);
					writer.write("(null)");
					j = 6;
					while (j < maxW[i])
					{
						writer.writeChar((byte)' ');
						j++;
					}
					writer.resetTextColor();
					valArr[i] = null;
				}
				i++;
			}
			writer.writeLine();
			while (hasMoreLine)
			{
				hasMoreLine = false;
				if (i > 0)
					writer.writeChar((byte)'|');
				if ((s = valArr[i]) != null)
				{
					writer.setTextColor(StandardColor.ConsoleDarkGreen);
					sArr[i] = writeColumnLine(writer, sArr[i], maxW[i]);
					writer.resetTextColor();
					if (sArr[i] != null)
						hasMoreLine = true;
					else
					{
						valArr[i] = null;
					}
				}
				else
				{
					j = 0;
					while (j < maxW[i])
					{
						writer.writeChar((byte)' ');
						j++;
					}
				}
			}		
		}
		data.closeReader(r);
	}

	public static void writeTableDataPart(@Nonnull StyledTextWriter writer, @Nonnull TableData data, int nTop, int nBottom)
	{
		String s;
		DBReader r;
		int rowCnt = 0;
		int currRow;
		if ((r = data.getTableData()) == null)
		{
			writer.setTextColor(StandardColor.Red);
			writer.writeLine("Error in reading TableData");
			writer.resetTextColor();
			return;
		}
		while (r.readNext())
		{
			rowCnt++;
		}	
		data.closeReader(r);
		if (rowCnt <= nTop + nBottom)
		{
			writeTableData(writer, data);
			return;
		}

		if ((r = data.getTableData()) == null)
		{
			writer.setTextColor(StandardColor.Red);
			writer.writeLine("Error in reading TableData");
			writer.resetTextColor();
			return;
		}
		Size2DInt thisSize;
		int colCnt = r.colCount();
		int[] maxW = new int[colCnt];
		int i;
		i = 0;
		while (i < colCnt)
		{
			if ((s = r.getName(i)) != null)
			{
				thisSize = StringUtil.getMonospaceSize(s);
				maxW[i] = thisSize.getWidth();
			}
			else
			{
				maxW[i] = 6;
			}
			i++;
		}
		currRow = 0;
		while (r.readNext())
		{
			if (currRow < nTop || currRow >= rowCnt - nBottom)
			{
				i = 0;
				while (i < colCnt)
				{
					if ((s = r.getString(i)) != null)
					{
						thisSize = StringUtil.getMonospaceSize(s);
					}
					else
					{
						thisSize = new Size2DInt(6, 1);
					}
					if (thisSize.getWidth() > maxW[i])
					{
						maxW[i] = thisSize.getWidth();
					}
					i++;
				}
			}
			currRow++;
		}
		data.closeReader(r);

		if ((r = data.getTableData()) == null)
		{
			writer.setTextColor(StandardColor.Red);
			writer.writeLine("Error in reading TableData");
			writer.resetTextColor();
			return;
		}

		boolean hasMoreLine;
		int j;
		String[] valArr = new String[colCnt];
		String[] sArr = new String[colCnt];
		hasMoreLine = false;
		i = 0;
		while (i < colCnt)
		{
			if (i > 0)
				writer.writeChar((byte)'|');
			if ((s = r.getName(i)) != null)
			{
				valArr[i] = s;
				writer.setTextColor(StandardColor.ConsoleDarkGreen);
				sArr[i] = s;
				sArr[i] = writeColumnLine(writer, sArr[i], maxW[i]);
				writer.resetTextColor();
				if (sArr[i] != null)
					hasMoreLine = true;
				else
				{
					valArr[i] = null;
				}
			}
			else
			{
				writer.setTextColor(StandardColor.DarkGray);
				writer.write("(null)");
				j = 6;
				while (j < maxW[i])
				{
					writer.writeChar((byte)' ');
					j++;
				}
				writer.resetTextColor();
				valArr[i] = null;
			}
			i++;
		}
		writer.writeLine();
		while (hasMoreLine)
		{
			hasMoreLine = false;
			if (i > 0)
				writer.writeChar((byte)'|');
			if ((s = valArr[i]) != null)
			{
				writer.setTextColor(StandardColor.ConsoleDarkGreen);
				sArr[i] = writeColumnLine(writer, sArr[i], maxW[i]);
				writer.resetTextColor();
				if (sArr[i] != null)
					hasMoreLine = true;
				else
				{
					valArr[i] = null;
				}
			}
			else
			{
				j = 0;
				while (j < maxW[i])
				{
					writer.writeChar((byte)' ');
					j++;
				}
			}
		}
		i = 0;
		while (i < colCnt)
		{
			if (i > 0)
				writer.writeChar((byte)'+');
			j = 0;
			while (j < maxW[i])
			{
				writer.writeChar((byte)'-');
				j++;
			}
			i++;
		}
		writer.writeLine();
		currRow = 0;
		while (r.readNext())
		{
			if (currRow < nTop || currRow >= rowCnt - nBottom)
			{
				hasMoreLine = false;
				i = 0;
				while (i < colCnt)
				{
					if (i > 0)
						writer.writeChar((byte)'|');
					if ((s = r.getString(i)) != null)
					{
						valArr[i] = s;
						writer.setTextColor(StandardColor.Yellow);
						sArr[i] = s;
						sArr[i] = writeColumnLine(writer, sArr[i], maxW[i]);
						writer.resetTextColor();
						if (sArr[i] != null)
							hasMoreLine = true;
						else
						{
							valArr[i] = s;
						}
					}
					else
					{
						writer.setTextColor(StandardColor.DarkGray);
						writer.write("(null)");
						j = 6;
						while (j < maxW[i])
						{
							writer.writeChar((byte)' ');
							j++;
						}
						writer.resetTextColor();
						valArr[i] = null;
					}
					i++;
				}
				writer.writeLine();
				while (hasMoreLine)
				{
					hasMoreLine = false;
					if (i > 0)
						writer.writeChar((byte)'|');
					if ((s = valArr[i]) != null)
					{
						writer.setTextColor(StandardColor.ConsoleDarkGreen);
						sArr[i] = writeColumnLine(writer, sArr[i], maxW[i]);
						writer.resetTextColor();
						if (sArr[i] != null)
							hasMoreLine = true;
						else
						{
							valArr[i] = null;
						}
					}
					else
					{
						j = 0;
						while (j < maxW[i])
						{
							writer.writeChar((byte)' ');
							j++;
						}
					}
				}
			}
			currRow++;
			if (currRow == nTop || (nTop == 0 && currRow == 1))
			{
				i = 0;
				while (i < colCnt)
				{
					if (i > 0)
						writer.writeChar((byte)' ');
					if (maxW[i] < 3)
					{
						j = 0;
						while (j < maxW[i])
						{
							writer.writeChar((byte)' ');
							j++;
						}
					}
					else
					{
						j = (maxW[i] - 3) >> 1;
						while (j-- > 0)
						{
							writer.writeChar((byte)' ');
						}
						writer.write("...");
						j = ((maxW[i] - 3) >> 1) + 3;
						while (j < maxW[i])
						{
							writer.writeChar((byte)' ');
							j++;
						}
					}
					i++;
				}
				writer.writeLine();
			}
		}
		data.closeReader(r);
	}

	public static @Nullable String writeColumnLine(@Nonnull StyledTextWriter writer, @Nonnull String column, int colSize) //return lines exist
	{
		String[] sarr = StringUtil.splitLine(column, 2);
		Size2DInt size = StringUtil.getMonospaceSize(sarr[0]);
		writer.write(sarr[0]);
		int i = size.getWidth();
		while (i < colSize)
		{
			writer.writeChar((byte)' ');
			i++;
		}
		if (sarr.length == 2)
		{
			return sarr[1];
		}
		return null;
	}
}
