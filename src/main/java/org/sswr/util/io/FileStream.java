package org.sswr.util.io;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;
import java.util.Set;

public class FileStream extends SeekableStream
{
	public enum BufferType
	{
		RANDOM_ACCESS,
		NORMAL,
		SEQUENTIAL,
		NO_BUFFER,
		NO_WRITE_BUFFER
	}

	public enum FileMode
	{
		CREATE,
		APPEND,
		READONLY,
		READWRITEEXISTING,
		DEVICE,
		CREATEWRITE
	}

	public enum FileShare
	{
		DENY_NONE,
		DENY_READ,
		DENY_WRITE,
		DENY_ALL
	}

	private FileChannel file;

	public FileStream(String fileName, FileMode mode, FileShare share, BufferType buffType)
	{
		super(fileName);
		Set<OpenOption> options;
		switch (mode)
		{
			case APPEND:
				options = Set.of(StandardOpenOption.CREATE, StandardOpenOption.READ, StandardOpenOption.WRITE);
				break;
			case CREATE:
				options = Set.of(StandardOpenOption.CREATE, StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
				break;
			case CREATEWRITE:
				options = Set.of(StandardOpenOption.CREATE, StandardOpenOption.WRITE);
				break;
			case DEVICE:
				options = Set.of(StandardOpenOption.READ, StandardOpenOption.WRITE);
				break;
			case READONLY:
				options = Set.of(StandardOpenOption.READ);
				break;
			case READWRITEEXISTING:
				options = Set.of(StandardOpenOption.READ, StandardOpenOption.WRITE);
				break;
			default:
				throw new IllegalArgumentException("Mode is not valid");
		}
		File f = new File(fileName);
		try
		{
			this.file = FileChannel.open(f.toPath(), options);
			if (mode == FileMode.APPEND)
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

	public int read(byte []buff, int ofst, int size)
	{
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
		try
		{
			this.file.close();
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
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
}
