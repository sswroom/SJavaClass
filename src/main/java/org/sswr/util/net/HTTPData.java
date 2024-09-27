package org.sswr.util.net;

import java.time.ZonedDateTime;

import org.sswr.util.basic.MyThread;
import org.sswr.util.basic.ThreadEvent;
import org.sswr.util.io.FileStream;
import org.sswr.util.io.Path;
import org.sswr.util.io.PathType;
import org.sswr.util.io.StreamData;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class HTTPData implements StreamData, Runnable
{
	private static class HTTPDATAHANDLE
	{
		public FileStream file;
		public long fileLength;
		public long currentOffset;
		public String url;
		public String fileName;
		public String localFile;
		public int seekCnt;

		public HTTPClient cli;
		public SocketFactory sockf;
		public SSLEngine ssl;
		public HTTPQueue queue;
		public long loadSize;
		public boolean isLoading;
		public ThreadEvent evtTmp;
		public int objectCnt;
	};

	private HTTPDATAHANDLE fdh;
	private long dataOffset;
	private long dataLength;

	public void run()
	{
		byte[] buff = new byte[2048];
		int readSize;
	
		if (fdh.queue != null)
		{
			synchronized(fdh)
			{
				fdh.cli = fdh.queue.makeRequest(fdh.url, RequestMethod.HTTP_GET, true);
			}
		}
		else
		{
			fdh.cli = HTTPClient.createConnect(fdh.sockf, fdh.ssl, fdh.url, RequestMethod.HTTP_GET, true);
		}
		fdh.evtTmp.set();
		if (Path.getPathType(fdh.localFile) == PathType.File)
		{
			FileStream fs = new FileStream(fdh.localFile, FileStream.FileMode.ReadOnly, FileStream.FileShare.DenyNone, FileStream.BufferType.Normal);
			if (!fs.isError())
			{
				ZonedDateTime dt = fs.getModifyTime();
				fdh.cli.addTimeHeader("If-Modified-Since", dt);
			}
		}
		while (fdh.cli.getRespStatus() == 301)
		{
			String location = fdh.cli.getRespHeader("Location");
			if (location != null && (location.startsWith("http://") || location.startsWith("https://")))
			{
				if (fdh.queue != null)
				{
					synchronized (fdh)
					{
						fdh.queue.endRequest(fdh.cli);
						fdh.cli = fdh.queue.makeRequest(location, RequestMethod.HTTP_GET, true);
					}
				}
				else
				{
					fdh.cli.close();
					fdh.cli = HTTPClient.createConnect(fdh.sockf, fdh.ssl, location, RequestMethod.HTTP_GET, true);
				}
			}
			else
			{
				break;
			}
		}
	
		if (fdh.cli.getRespStatus() == 304)
		{
			fdh.file = new FileStream(fdh.localFile, FileStream.FileMode.ReadOnly, FileStream.FileShare.DenyNone, FileStream.BufferType.Normal);
			fdh.fileLength = fdh.file.getLength();
		}
		else if (fdh.cli.getRespStatus() == 200)
		{
			fdh.fileLength = fdh.cli.getContentLength();
			if (fdh.fileLength > 0)
			{
				fdh.file = new FileStream(fdh.localFile, FileStream.FileMode.Create, FileStream.FileShare.DenyWrite, FileStream.BufferType.Normal);
				while (fdh.loadSize < fdh.fileLength)
				{
					readSize = fdh.cli.read(buff, 0, 2048);
					if (readSize == 0)
					{
						synchronized(fdh)
						{
							fdh.file.close();
							fdh.file = null;
							fdh.fileLength = 0;
						}
						Path.deleteFile(fdh.localFile);
						break;
					}
					synchronized(fdh)
					{
						if (fdh.currentOffset != fdh.loadSize)
						{
							fdh.file.seekFromBeginning(fdh.loadSize);
							fdh.seekCnt++;
							fdh.currentOffset = fdh.loadSize;
						}
						fdh.file.write(buff, 0, readSize);
						fdh.loadSize += readSize;
						fdh.currentOffset = fdh.loadSize;
					}
				}
				ZonedDateTime dt;
				if (fdh.file != null && (dt = fdh.cli.getLastModified()) != null)
				{
					fdh.file.setModifyTime(dt);
				}
			}
			else
			{
				if (fdh.cli.getRespHeader("Content-Length") == null)
				{
					fdh.file = new FileStream(fdh.localFile, FileStream.FileMode.Create, FileStream.FileShare.DenyWrite, FileStream.BufferType.Normal);
					while (true)
					{
						readSize = fdh.cli.read(buff, 0, 2048);
						if (readSize <= 0)
						{
							fdh.cli.close();
							break;
						}
						synchronized (fdh)
						{
							if (fdh.currentOffset != fdh.loadSize)
							{
								fdh.file.seekFromBeginning(fdh.loadSize);
								fdh.seekCnt++;
							}
							fdh.file.write(buff, 0, readSize);
							fdh.loadSize += readSize;
							fdh.fileLength = fdh.loadSize;
							fdh.currentOffset = fdh.fileLength;
						}
					}
					ZonedDateTime dt;
					if (fdh.file != null && (dt = fdh.cli.getLastModified()) != null)
					{
						fdh.file.setModifyTime(dt);
					}
				}
			}
		}
		synchronized (fdh)
		{
			if (fdh.queue != null)
			{
				fdh.queue.endRequest(fdh.cli);
			}
			else
			{
				fdh.cli.close();
			}
			fdh.cli = null;
			fdh.isLoading = false;
		}
	}

	public HTTPData(@Nonnull HTTPData fd, long offset, long length)
	{
		dataOffset = offset + fd.dataOffset;
		long endOffset = fd.dataOffset + fd.getDataSize();
		dataLength = length;
		if (dataOffset > endOffset)
		{
			dataOffset = endOffset;
			dataLength = 0;
		}
		else if (dataOffset + length > endOffset)
		{
			dataLength = endOffset - dataOffset;
		}
		fdh = fd.fdh;
		fdh.objectCnt++;
	}

	public HTTPData(@Nullable SocketFactory sockf, @Nullable SSLEngine ssl, @Nonnull HTTPQueue queue, @Nonnull String url, @Nonnull String localFile, boolean forceReload)
	{
		int i;
		boolean needReload = forceReload;
		PathType pt = Path.getPathType(localFile);
		fdh = null;
		if (pt == PathType.Directory)
		{
			this.dataLength = 0;
			this.dataOffset = 0;
			return;
		}
		else if (pt == PathType.Unknown)
		{
			needReload = true;
		}
		if (!needReload)
		{
			FileStream fs = new FileStream(localFile, FileStream.FileMode.ReadOnly, FileStream.FileShare.DenyWrite, FileStream.BufferType.Normal);
			if (fs.isError())
			{
				fs.close();
				this.dataLength = 0;
				this.dataOffset = 0;
			}
			else
			{
				fdh = new HTTPDATAHANDLE();
				fdh.file = fs;
				dataOffset = 0;
				dataLength = fdh.fileLength = fs.getLength();
				fdh.currentOffset = fs.getPosition();
				fdh.objectCnt = 1;
				fdh.seekCnt = 0;
				fdh.url = url;
				fdh.localFile = localFile;
				fdh.isLoading = false;
				fdh.loadSize = 0;
				fdh.cli = null;
				i = fdh.url.lastIndexOf('/');
				if (i != -1)
				{
					fdh.fileName = fdh.url.substring(i + 1);
				}
				else
				{
					fdh.fileName = fdh.url;
				}
			}
		}
		else
		{
			dataOffset = 0;
			dataLength = -1;
			fdh = new HTTPDATAHANDLE();
			fdh.file = null;
			fdh.fileLength = 0;
			fdh.currentOffset = 0;
			fdh.objectCnt = 1;
			fdh.seekCnt = 0;
			fdh.url = url;
			fdh.localFile = localFile;
			fdh.isLoading = true;
			fdh.loadSize = 0;
			fdh.sockf = sockf;
			fdh.ssl = ssl;
			fdh.queue = queue;
			i = fdh.url.lastIndexOf('/');
			if (i != -1)
			{
				fdh.fileName = fdh.url.substring(i + 1);
			}
			else
			{
				fdh.fileName = fdh.url;
			}
			fdh.cli = null;
			fdh.evtTmp = new ThreadEvent(false);
			new Thread(this).start();
			while (fdh.cli == null && fdh.isLoading)
			{
				fdh.evtTmp.waitEvent(10);
			}
		}
	}

	public void close()
	{
		if (this.fdh != null)
		{
			if (--(fdh.objectCnt) == 0)
			{
				synchronized(fdh)
				{
					if (fdh.isLoading)
						fdh.cli.close();
				}
				while (fdh.isLoading)
				{
					MyThread.sleep(10);
				}
				fdh.file.close();
			}
		}
		fdh = null;
	}

	public int getRealData(long offset, int length, @Nonnull byte[] buffer, int buffOfst)
	{
		if (fdh == null)
			return 0;
		synchronized(fdh)
		{
			while (fdh.isLoading && (dataOffset + offset + length > fdh.loadSize))
			{
				MyThread.sleep(10);
			}
			if (fdh.currentOffset != dataOffset + offset)
			{
				if ((fdh.currentOffset = fdh.file.seekFromBeginning(dataOffset + offset)) != dataOffset + offset)
				{
					return 0;
				}
				fdh.seekCnt++;
			}
			int byteRead;
			if (length < this.getDataSize() - offset)
				byteRead = fdh.file.read(buffer, buffOfst, length);
			else
				byteRead = fdh.file.read(buffer, buffOfst, (int) (dataLength - offset));
			if (byteRead == 0)
			{
				return 0;
			}
			fdh.currentOffset += byteRead;
			return byteRead;
		}
	}

	@Nonnull
	public String getFullName()
	{
		if (fdh == null)
			return "";
		return fdh.url;
	}

	@Nullable
	public String getShortName()
	{
		if (fdh == null)
			return null;
		return fdh.fileName;
	}

	public void setFullName(@Nonnull String fullName)
	{
		if (fdh == null || fullName.length() == 0)
			return;
		int i;
		synchronized(fdh)
		{
			fdh.url = fullName;
			i = fdh.url.lastIndexOf('/');
			if (i != -1)
			{
				fdh.fileName = fdh.url.substring(i + 1);
			}
			else
			{
				fdh.fileName = fdh.url;
			}
		}
	}

	public long getDataSize()
	{
		if (dataLength == -1)
		{
			while (true)
			{
				if (fdh.fileLength != 0 || !fdh.isLoading)
				{
					dataLength = fdh.fileLength;
					break;
				}
				MyThread.sleep(10);
			}
		}
		return dataLength;
	}

	@Nonnull
	public StreamData getPartialData(long offset, long length)
	{
		return new HTTPData(this, offset, length);
	}

	public boolean isFullFile()
	{
		return this.dataOffset == 0;
	}

	@Override
	@Nonnull
	public String getFullFileName()
	{
		return this.getFullName();
	}

	public boolean isLoading()
	{
		if (fdh == null)
			return false;
		return fdh.isLoading;		
	}

	public int getSeekCount()
	{
		if (fdh == null)
			return 0;
		return fdh.seekCnt;
	}

}
