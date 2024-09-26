package org.sswr.util.map;

import java.util.List;

import org.sswr.util.data.ByteTool;
import org.sswr.util.db.DBReader;
import org.sswr.util.io.StreamData;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class FileGDBTable
{
	private String tableName;
	private StreamData fd;
	private long dataOfst;
	private FileGDBTableInfo tableInfo;

	public FileGDBTable(@Nonnull String tableName, @Nonnull StreamData fd)
	{
		this.tableName = tableName;
		this.fd = fd.getPartialData(0, fd.getDataSize());
		this.tableInfo = null;
		this.dataOfst = 0;
	
		byte []hdrBuff = new byte[44];
		if (this.fd.getRealData(0, 44, hdrBuff, 0) != 44)
		{
			return;
		}
		if (ByteTool.readInt32(hdrBuff, 0) != 3 || ByteTool.readInt32(hdrBuff, 12) != 5 || ByteTool.readInt32(hdrBuff, 20) != 0 || ByteTool.readInt64(hdrBuff, 24) != this.fd.getDataSize())
		{
			return;
		}
		if (ByteTool.readInt64(hdrBuff, 32) == 40)
		{
			int fieldSize = ByteTool.readInt32(hdrBuff, 40);
			byte []fieldDesc = new byte[fieldSize + 4];
			this.fd.getRealData(40, fieldSize + 4, fieldDesc, 0);
			this.tableInfo = FileGDBUtil.parseFieldDesc(fieldDesc);
			this.dataOfst = 40 + 4 + fieldSize;
		}	
	}
	
	public void close()
	{
		if (this.fd != null)
		{
			this.fd.close();
			this.fd = null;
		}
		this.tableInfo = null;
	}

	public boolean isError()
	{
		return this.tableInfo == null || this.fd == null;
	}

	@Nonnull
	public String getName()
	{
		return this.tableName;
	}

	@Nullable
	public DBReader openReader(@Nullable List<String> colList)
	{
		if (this.tableInfo == null || this.fd == null)
		{
			return null;
		}
		return new FileGDBReader(this.fd, this.dataOfst, this.tableInfo, colList);
	}
}
