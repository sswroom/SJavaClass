package org.sswr.util.db;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.sswr.util.data.QueryConditions;
import org.sswr.util.data.TableData;
import org.sswr.util.io.FileStream;
import org.sswr.util.io.IOReader;
import org.sswr.util.io.SeekableStream;
import org.sswr.util.io.StreamData;
import org.sswr.util.io.StreamDataStream;
import org.sswr.util.io.StreamReader;
import org.sswr.util.io.UTF8Reader;
import org.sswr.util.io.FileStream.BufferType;
import org.sswr.util.io.FileStream.FileMode;
import org.sswr.util.io.FileStream.FileShare;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class CSVFile extends ReadingDB
{
	private @Nonnull String fileName;
	private @Nullable SeekableStream stm;
	private @Nullable StreamData fd;
	private int codePage;
	private boolean noHeader;
	private boolean nullIfEmpty;
	private int indexCol;
	private @Nonnull List<Integer> timeCols;

	private void initReader(@Nonnull CSVReader r)
	{
		r.setIndexCol(this.indexCol);
		int i = this.timeCols.size();
		while (i-- > 0)
		{
			r.addTimeCol(this.timeCols.get(i));
		}
	}

	public CSVFile(@Nonnull String fileName, int codePage)
	{
		super(fileName);
		this.timeCols = new ArrayList<Integer>(4);
		this.fileName = fileName;
		this.stm = null;
		this.fd = null;
		this.codePage = codePage;
		this.noHeader = false;
		this.nullIfEmpty = false;
		this.indexCol = -1;
	}

	public CSVFile(@Nonnull SeekableStream stm, int codePage)
	{
		super(stm.getSourceNameObj());
		this.timeCols = new ArrayList<Integer>(4);
		this.fileName = stm.getSourceNameObj();
		this.stm = stm;
		this.fd = null;
		this.codePage = codePage;
		this.noHeader = false;
		this.nullIfEmpty = false;
		this.indexCol = -1;
	}

	public CSVFile(@Nonnull StreamData fd, int codePage)
	{
		super(fd.getFullName());
		this.timeCols = new ArrayList<Integer>(4);
		this.fileName = fd.getFullName();
		this.stm = null;
		this.fd = fd.getPartialData(0, fd.getDataSize());
		this.codePage = codePage;
		this.noHeader = false;
		this.nullIfEmpty = false;
		this.indexCol = -1;
	}

	public void dispose()
	{
		if (this.fd != null)
		{
			this.fd.close();
			this.fd = null;
		}
	}

	@Override
	public @Nullable List<String> queryTableNames(@Nullable String schemaName) {
		if (schemaName == null || schemaName.length() == 0)
		{
			return List.of("CSVFile");
		}
		return null;
	}

	@Override
	public @Nullable DBReader queryTableData(@Nullable String schemaName, @Nonnull String tableName,
			@Nullable List<String> colNames, int dataOfst, int maxCnt, @Nullable String ordering,
			@Nullable QueryConditions condition) {
		StreamData fd;
		SeekableStream stm;
		IOReader rdr;
		CSVReader r;
		if ((fd = this.fd) != null)
		{
			stm = new StreamDataStream(fd);
			if (codePage == 65001)
			{
				rdr = new UTF8Reader(stm);
			}
			else
			{
				rdr = new StreamReader(stm, codePage);
			}
			r = new CSVReader(stm, rdr, this.noHeader, this.nullIfEmpty, condition);
			this.initReader(r);
			return r;
		}
		if ((stm = this.stm) != null)
		{
			stm.seekFromBeginning(0);
			if (codePage == 65001)
			{
				rdr = new UTF8Reader(stm);
			}
			else
			{
				rdr = new StreamReader(stm, codePage);
			}
			r = new CSVReader(null, rdr, this.noHeader, this.nullIfEmpty, condition);
			this.initReader(r);
			return r;
		}
		FileStream fs = new FileStream(this.fileName, FileMode.ReadOnly, FileShare.DenyNone, BufferType.Sequential);
		if (!fs.isError())
		{
			if (codePage == 65001)
			{
				rdr = new UTF8Reader(fs);
			}
			else
			{
				rdr = new StreamReader(fs, codePage);
			}
			r = new CSVReader(fs, rdr, this.noHeader, this.nullIfEmpty, condition);
			this.initReader(r);
			return r;
		}
		else
		{
			fs.close();
			return null;
		}
	}

	@Override
	public @Nullable TableDef getTableDef(@Nullable String schemaName, @Nonnull String tableName) {
		DBReader r;
		if ((r = this.queryTableData(schemaName, tableName, null, 0, 0, null, null)) != null)
		{
			TableDef tab;
			ColumnDef col;
			tab = new TableDef(schemaName, tableName);
			int i = 0;
			int j = r.colCount();
			while (i < j)
			{
				col = r.getColumnDef(i);
				if (col == null)
				{
					col = new ColumnDef("");
				}
				tab.addCol(col);
				i++;
			}
			this.closeReader(r);
			return tab;
		}
		return null;
	}

	@Override
	public void closeReader(@Nonnull DBReader r) {
		((CSVReader)r).close();
	}

	@Override
	public @Nullable String getLastErrorMsg() {
		return null;
	}

	@Override
	public void reconnect() {
	}

	public void setNoHeader(boolean noHeader)
	{
		this.noHeader = noHeader;
	}

	public void setNullIfEmpty(boolean nullIfEmpty)
	{
		this.nullIfEmpty = nullIfEmpty;
	}

	public void setIndexCol(int indexCol)
	{
		this.indexCol = indexCol;
	}

	public void setTimeCols(@Nonnull List<Integer> timeCols)
	{
		int i = 0;
		int j = timeCols.size();
		this.timeCols.clear();
		while (i < j)
		{
			this.timeCols.add(timeCols.get(i));
			i++;
		}
	}

	public static @Nullable TableData loadAsTableData(@Nonnull String fileName, int codePage, int indexCol, @Nullable List<Integer> timeCols)
	{
		File file = new File(fileName);
		if (!file.isFile())
			return null;
		CSVFile csv = new CSVFile(fileName, codePage);
		csv.setIndexCol(indexCol);
		if (timeCols != null)
		{
			csv.setTimeCols(timeCols);
		}
		return new TableData(csv, true, null, "");
	}
}