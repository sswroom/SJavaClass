package org.sswr.util.data;

import java.nio.charset.StandardCharsets;
import java.util.Iterator;

import org.sswr.util.db.CSVUtil;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class CSVTableBuilder implements TableBuilder
{
	private StringBuilder sb;

	public CSVTableBuilder()
	{
		sb = new StringBuilder();
	}

	public void appendBOM()
	{
		sb.append((char)65279);
	}

	@Override
	public void appendRow()
	{
		sb.append("\r\n");
	}
	
	@Override
	public void appendRow(@Nullable Iterable<?> rowData)
	{
		if (rowData != null)
		{
			boolean found = false;
			Iterator<?> it = rowData.iterator();
			while (it.hasNext())
			{
				if (found)
				{
					sb.append(",");
				}
				else
				{
					found = true;
				}
				Object o = it.next();
				if (o == null)
				{
					sb.append("NULL");
				}
				else
				{
					String s;
					if (o instanceof String)
					{
						s = (String)o;
					}
					else
					{
						s = o.toString();
					}
					if (s.indexOf('\r') >= 0 || s.indexOf('\n') >= 0 || s.indexOf(",") >= 0 || s.indexOf('"') >= 0)
					{
						sb.append(CSVUtil.quote(s));
					}
					else
					{
						sb.append(s);
					}
				}
			}
		}
		sb.append("\r\n");
	}

	@Override
	@Nonnull
	public byte[] build()
	{
		return sb.toString().getBytes(StandardCharsets.UTF_8);
	}
}
