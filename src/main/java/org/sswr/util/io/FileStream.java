package org.sswr.util.io;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Set;

public class FileStream extends SeekableStream
{
	public enum BufferType
	{
		RandomAccess,
		Normal,
		Sequential,
		NoBuffer,
		NoWriteBuffer
	}

	public enum FileMode
	{
		Create,
		Append,
		ReadOnly,
		ReadWriteExisting,
		Device,
		CreateWrite
	}

	public enum FileShare
	{
		DenyNone,
		DenyRead,
		DenyWrite,
		DenyAll
	}

	private Path path;
	private FileChannel file;

	public FileStream(String fileName, FileMode mode, FileShare share, BufferType buffType)
	{
		super(fileName);
		Set<OpenOption> options;
		switch (mode)
		{
			case Append:
				options = Set.of(StandardOpenOption.CREATE, StandardOpenOption.READ, StandardOpenOption.WRITE);
				break;
			case Create:
				options = Set.of(StandardOpenOption.CREATE, StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
				break;
			case CreateWrite:
				options = Set.of(StandardOpenOption.CREATE, StandardOpenOption.WRITE);
				break;
			case Device:
				options = Set.of(StandardOpenOption.READ, StandardOpenOption.WRITE);
				break;
			case ReadOnly:
				options = Set.of(StandardOpenOption.READ);
				break;
			case ReadWriteExisting:
				options = Set.of(StandardOpenOption.READ, StandardOpenOption.WRITE);
				break;
			default:
				throw new IllegalArgumentException("Mode is not valid");
		}
		File f = new File(fileName);
		this.path = f.toPath();
		boolean exist = f.exists();
		if (!exist && mode == FileMode.ReadOnly)
		{
			this.file = null;
		}
		else
		{
			try
			{
				this.file = FileChannel.open(this.path, options);
				if (mode == FileMode.Append)
				{
					this.seekFromEnd(0);
				}
			}
			catch (IOException ex)
			{
				ex.printStackTrace();
				this.file = null;
			}
		}
	}

	public boolean isDown()
	{
		return this.file == null || !this.file.isOpen();
	}

	public int read(byte []buff, int ofst, int size)
	{
		if (this.file == null)
			return 0;
		try
		{
			int ret = this.file.read(ByteBuffer.wrap(buff, ofst, size));
			if (ret == -1)
			{
				return 0;
			}
			return ret;
		}
		catch (IOException ex)
		{
			return 0;
		}
		catch (IndexOutOfBoundsException ex)
		{
			System.out.println("Ofst = "+ofst+", size = "+size);
			ex.printStackTrace();
			return 0;
		}
	}

	public int write(byte []buff, int ofst , int size)
	{
		if (this.file == null)
			return 0;
		try
		{
			return this.file.write(ByteBuffer.wrap(buff, ofst, size));
		}
		catch (IOException ex)
		{
			return 0;
		}
		catch (IndexOutOfBoundsException ex)
		{
			System.out.println("Ofst = "+ofst+", size = "+size);
			ex.printStackTrace();
			return 0;
		}
	}

	public int flush()
	{
		return 0;
	}

	public void close()
	{
		if (this.file != null)
		{
			try
			{
				this.file.close();
				this.file = null;
			}
			catch (IOException ex)
			{
				ex.printStackTrace();
			}
		}
	}

	public boolean recover()
	{
		return false;
	}

	public long seekFromBeginning(long position)
	{
		try
		{
			this.file.position(position);
		}
		catch (IOException ex)
		{

		}
		return getPosition();
	}

	public long seekFromCurrent(long position)
	{
		try
		{
			return seekFromBeginning(position + this.file.position());
		}
		catch (IOException ex)
		{
			return getPosition();
		}
	}

	public long seekFromEnd(long position)
	{
		try
		{
			return seekFromBeginning(position + this.file.size());
		}
		catch (IOException ex)
		{
			return getPosition();
		}
	}

	public long getPosition()
	{
		try
		{
			return this.file.position();
		}
		catch (IOException ex)
		{
			return 0;
		}
	}

	public long getLength()
	{
		try
		{
			return this.file.size();
		}
		catch (IOException ex)
		{
			return 0;
		}
	}

	public void setLength(long newLength)
	{
		long len = getLength();
		if (newLength > len)
		{
			try
			{
				this.file.position(newLength);
			}
			catch (IOException ex)
			{

			}
		}
	}

	public boolean isError()
	{
		return this.file == null || !this.file.isOpen();
	}

	public int getErrCode()
	{
		return 0;
	}

	public ZonedDateTime getCreateTime()
	{
		try
		{
			BasicFileAttributes fileAtt = Files.readAttributes(this.path, BasicFileAttributes.class);
			return ZonedDateTime.ofInstant(fileAtt.creationTime().toInstant(), ZoneId.systemDefault());
		}
		catch (IOException ex)
		{
			return ZonedDateTime.ofInstant(Instant.ofEpochMilli(0), ZoneId.systemDefault());
		}
	}

	public ZonedDateTime getModifyTime()
	{
		try
		{
			BasicFileAttributes fileAtt = Files.readAttributes(this.path, BasicFileAttributes.class);
			return ZonedDateTime.ofInstant(fileAtt.lastModifiedTime().toInstant(), ZoneId.systemDefault());
		}
		catch (IOException ex)
		{
			return ZonedDateTime.ofInstant(Instant.ofEpochMilli(0), ZoneId.systemDefault());
		}
	}

	public boolean setModifyTime(ZonedDateTime dt)
	{
		try
		{
			Files.setLastModifiedTime(this.path, FileTime.from(dt.toInstant()));
			return true;
		}
		catch (IOException ex)
		{
			return false;
		}
		catch (SecurityException ex)
		{
			return false;
		}
	}
}
