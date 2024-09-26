package org.sswr.util.io.stmdata;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.sswr.util.io.StreamData;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class FileData implements StreamData
{
	class FileDataHandle
	{
		public FileInputStream file;
		public long fileLength;
		public long currentOffset;
		public String fileName;
		public String fullName;
		public String filePath;
		public boolean deleteOnClose;
		public int seekCnt;
		public int objectCnt;
	}

	class FileDataName
	{
		public String fileName;
		public String fullName;
		public int objectCnt;
	}

	private FileDataHandle fdh;
	private FileDataName fdn;
	private long dataOffset;
	private long dataLength;

	private void reopenFile()
	{
		if (this.fdh == null)
			return;
		synchronized (this.fdh)
		{
			try
			{
				FileInputStream fs = new FileInputStream(this.fdh.filePath);
				try
				{
					this.fdh.file.close();
				}
				catch (IOException ex2)
				{

				}
				this.fdh.file = fs;
				this.fdh.currentOffset = fs.getChannel().position();
			}
			catch (IOException ex)
			{

			}
		}
	}
	
	private FileData(@Nonnull FileData fd, long offset, long length)
	{
		this.dataOffset = offset + fd.dataOffset;
		long endOffset = fd.dataOffset + fd.dataLength;
		this.dataLength = length;
		if (this.dataOffset > endOffset)
		{
			this.dataOffset = endOffset;
			this.dataLength = 0;
		}
		else if (this.dataOffset + length > endOffset)
		{
			this.dataLength = endOffset - this.dataOffset;
		}
		this.fdh = fd.fdh;
		synchronized (this.fdh)
		{
			this.fdh.objectCnt++;
		}
		this.fdn = fd.fdn;
		if (this.fdn != null)
		{
			synchronized (this.fdn)
			{
				this.fdn.objectCnt++;
			}
		}
	}

	public FileData(@Nonnull String fileName, boolean deleteOnClose)
	{
		this.fdh = null;
		FileInputStream fs;
		this.fdn = null;
		try
		{
			fs = new FileInputStream(fileName);
			this.fdh = new FileDataHandle();
			this.fdh.file = fs;
			this.fdh.filePath = fileName;
			this.dataLength = fdh.fileLength = fs.getChannel().size();
			this.fdh.currentOffset = fs.getChannel().position();
			this.fdh.objectCnt = 1;
			this.fdh.seekCnt = 0;
			this.dataOffset = 0;
			this.fdh.fullName = fileName;
			int i = fileName.lastIndexOf(File.separator);
			if (i >= 0)
			{
				this.fdh.fileName = fileName.substring(i + 1);
			}
			else
			{
				this.fdh.fileName = fileName;
			}
			fdh.deleteOnClose = deleteOnClose;
		}
		catch (IOException ex)
		{
			this.dataLength = 0;
			this.dataOffset = 0;
		}
	}

	public void close()
	{
		if (this.fdh != null)
		{
			synchronized(this.fdh)
			{
				if (--this.fdh.objectCnt == 0)
				{
					try
					{
						this.fdh.file.close();
						if (this.fdh.deleteOnClose)
						{
							new File(this.fdh.fullName).delete();
						}							
					}
					catch (IOException ex)
					{

					}
				}
			}
		}
		this.fdh = null;
	}

	public int getRealData(long offset, int length, @Nonnull byte[] buffer, int buffOfst)
	{
		if (this.fdh == null)
			return 0;
		synchronized(this.fdh)
		{
			if (this.fdh.currentOffset != this.dataOffset + offset)
			{
				try
				{
					this.fdh.file.getChannel().position(this.dataOffset + offset);
					this.fdh.currentOffset = this.dataOffset + offset;
				}
				catch (IOException ex)
				{
					if (this.dataOffset + offset < this.fdh.fileLength)
					{
						this.reopenFile();
					}
					return 0;
				}
				this.fdh.seekCnt++;
			}
			int byteRead;
			try
			{
				if (length < this.dataLength - offset)
					byteRead = this.fdh.file.read(buffer, buffOfst, length);
				else
					byteRead = this.fdh.file.read(buffer, buffOfst, (int)(this.dataLength - offset));
			}
			catch (IOException ex)
			{
				byteRead = 0;
			}
			if (byteRead <= 0)
			{
				return 0;
			}
			this.fdh.currentOffset += byteRead;
			return byteRead;
		}
	}

	@Nonnull
	public String getFullName()
	{
		if (this.fdn != null)
			return this.fdn.fullName;
		if (this.fdh != null)
			return this.fdh.fullName;
		return "";
	}

	@Nullable
	public String getShortName()
	{
		if (this.fdn != null)
			return this.fdn.fileName;
		if (this.fdh != null)
			return this.fdh.fileName;
		return null;		
	}

	public void setFullName(@Nullable String fullName)
	{
		if (this.fdn != null)
		{
			synchronized (this.fdn)
			{
				this.fdn.objectCnt--;
			}
			this.fdn = null;
		}
		if (fullName != null)
		{
			int i;
			this.fdn = new FileDataName();
			this.fdn.objectCnt = 1;
			this.fdn.fullName = fullName;
			i = this.fdn.fullName.lastIndexOf(File.separator);
			this.fdn.fileName = this.fdn.fullName.substring(i + 1);
		}		
	}

	public long getDataSize()
	{
		return dataLength;
	}

	@Nonnull
	public StreamData getPartialData(long offset, long length)
	{
		FileData data;
		data = new FileData(this, offset, length);
		return data;
	}

	public boolean isFullFile()
	{
		return this.dataOffset == 0;
	}

	@Nonnull
	public String getFullFileName()
	{
		if (this.fdh != null)
			return this.fdh.fullName;
		return "";
	}

	public boolean isLoading()
	{
		return false;
	}

	public int getSeekCount()
	{
		if (this.fdh != null)
			return this.fdh.seekCnt;
		return 0;
	}

	@Nullable
	public FileInputStream getFileStream()
	{
		if (this.fdh != null)
		{
			return this.fdh.file;
		}
		return null;
	}

	public boolean isError()
	{
		return this.fdh == null;
	}
}
