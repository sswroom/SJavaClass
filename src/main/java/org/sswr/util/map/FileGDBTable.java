package org.sswr.util.map;

import java.util.List;

import org.sswr.util.data.ByteTool;
import org.sswr.util.data.QueryConditions;
import org.sswr.util.db.DBReader;
import org.sswr.util.io.StreamData;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class FileGDBTable
{
	private @Nonnull String tableName;
	private @Nonnull StreamData gdbtableFD;
	private @Nullable StreamData gdbtablxFD;
	private int indexCnt;
	private long dataOfst;
	private int maxRowSize;
	private @Nullable FileGDBTableInfo tableInfo;

	public FileGDBTable(@Nonnull String tableName, @Nonnull StreamData gdbtableFD, @Nullable StreamData gdbtablxFD)
	{
		this.tableName = tableName;
		this.gdbtableFD = gdbtableFD.getPartialData(0, gdbtableFD.getDataSize());
		this.gdbtablxFD = null;
		this.indexCnt = 0;
		this.tableInfo = null;
		this.dataOfst = 0;
		this.maxRowSize = 0;

		StreamData nngdbtablxFD;
		byte[] hdrBuff = new byte[44];
		if ((nngdbtablxFD = gdbtablxFD) != null && nngdbtablxFD.getRealData(0, 16, hdrBuff, 0) == 16)
		{
			if (ByteTool.readInt32(hdrBuff, 0) == 3 && ByteTool.readInt32(hdrBuff, 12) == 5 && (ByteTool.readInt32(hdrBuff, 4) >= 1))
			{
				int n1024Blocks = ByteTool.readInt32(hdrBuff, 4);
				this.indexCnt = ByteTool.readInt32(hdrBuff, 8);
				if (nngdbtablxFD.getDataSize() >= 16 + n1024Blocks * 1024 && nngdbtablxFD.getDataSize() >= this.indexCnt * 5)
				{
					this.gdbtablxFD = nngdbtablxFD.getPartialData(0, nngdbtablxFD.getDataSize());
				}
			}
		}
		if (this.gdbtableFD.getRealData(0, 44, hdrBuff, 0) != 44)
		{
			return;
		}
		if (ByteTool.readInt32(hdrBuff, 0) != 3 || ByteTool.readInt32(hdrBuff, 12) != 5 || ByteTool.readInt32(hdrBuff, 20) != 0 || ByteTool.readInt64(hdrBuff, 24) != this.gdbtableFD.getDataSize())
		{
			return;
		}
		this.maxRowSize = ByteTool.readInt32(hdrBuff, 8);
		long fieldDescOfst = ByteTool.readInt64(hdrBuff, 32);
		long fileLength = this.gdbtableFD.getDataSize();
		if (fieldDescOfst == 40)
		{
			int fieldSize = ByteTool.readInt32(hdrBuff, 40);
			byte[] fieldDesc = new byte[fieldSize + 4];
			this.gdbtableFD.getRealData(40, fieldSize + 4, fieldDesc, 0);
			this.tableInfo = FileGDBUtil.parseFieldDesc(fieldDesc);
			this.dataOfst = 40 + 4 + fieldSize;
		}
		else if (fieldDescOfst >= 40 && fieldDescOfst + 4 <= fileLength)
		{
			this.gdbtableFD.getRealData(fieldDescOfst, 4, hdrBuff, 40);
			int fieldSize = ByteTool.readInt32(hdrBuff, 40);
			if (fieldDescOfst + 4 + fieldSize <= fileLength)
			{
				byte[] fieldDesc = new byte[fieldSize + 4];
				this.gdbtableFD.getRealData(fieldDescOfst, fieldSize + 4, fieldDesc, 0);
				this.tableInfo = FileGDBUtil.parseFieldDesc(fieldDesc);
				this.dataOfst = fieldDescOfst + 4 + fieldSize;
			}
		}
	}
	
	public void close()
	{
		this.gdbtableFD.close();
		if (this.gdbtablxFD != null) this.gdbtablxFD.close();
		this.tableInfo = null;
	}

	public boolean isError()
	{
		return this.tableInfo == null;
	}

	@Nonnull
	public String getName()
	{
		return this.tableName;
	}

	@Nonnull
	public String getFileName()
	{
		return this.gdbtableFD.getFullFileName();
	}


	@Nullable
	public DBReader openReader(@Nullable List<String> columnNames, int dataOfst, int maxCnt, @Nullable String ordering, @Nullable QueryConditions conditions)
	{
		FileGDBTableInfo tableInfo;
		if ((tableInfo = this.tableInfo) == null)
		{
			return null;
		}
		FileGDBReader reader = new FileGDBReader(this.gdbtableFD, this.dataOfst, tableInfo, columnNames, dataOfst, maxCnt, conditions, this.maxRowSize);
		StreamData fd;
		if ((fd = this.gdbtablxFD) != null)
		{
			reader.setIndex(fd, this.indexCnt);
		}
		return reader;
	}
}
